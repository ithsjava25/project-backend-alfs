package org.example.alfs.controllers;

import io.minio.GetObjectResponse;
import org.example.alfs.entities.Attachment;
import org.example.alfs.dto.attachment.PresignedUrlResponseDTO;
import org.example.alfs.repositories.AttachmentRepository;
import org.example.alfs.security.SecurityUtils;
import org.example.alfs.entities.User;
import org.example.alfs.services.storage.MinioStorageService;
import org.example.alfs.services.AuditService;
import org.example.alfs.enums.AuditAction;
import org.example.alfs.services.AuthorizationService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class AttachmentDownloadController {

    private final AttachmentRepository attachmentRepository;
    private final MinioStorageService storageService;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;
    private final AuthorizationService authorizationService;

    public AttachmentDownloadController(AttachmentRepository attachmentRepository,
                                        MinioStorageService storageService,
                                        SecurityUtils securityUtils,
                                        AuditService auditService,
                                        AuthorizationService authorizationService) {
        this.attachmentRepository = attachmentRepository;
        this.storageService = storageService;
        this.securityUtils = securityUtils;
        this.auditService = auditService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Attachment att = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found: " + id));

        // Säkerställ att den som laddar ner har rättigheter (samma regler som för presign)
        User current = securityUtils.getCurrentUser();
        if (!authorizationService.canAccessAttachment(current, att)) {
            auditService.log(
                    AuditAction.ACCESS_DENIED,
                    "attachment",
                    null,
                    "download denied: attachmentId=" + att.getId(),
                    att.getTicket(),
                    current
            );
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this attachment");
        }

        // Audit: registrera nedladdningsförsök
        auditService.log(
                AuditAction.FILE_DOWNLOAD_REQUESTED,
                "attachment",
                null,
                "download requested: attachmentId=" + att.getId(),
                att.getTicket(),
                current
        );

        final GetObjectResponse object;
        try {
            object = storageService.download(att.getS3Key());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to download attachment content", ex);
        }

        String fileName = att.getFileName() != null ? att.getFileName() : "file";
        String contentDisposition = ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString();

        // Wrap the stream in InputStreamResource for streaming response
        InputStreamResource resource = new InputStreamResource(object);

        // Get content length from response headers if available
        long contentLength = object.headers().get("Content-Length") != null
                ? Long.parseLong(object.headers().get("Content-Length"))
                : -1;

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        if (contentLength > 0) {
            builder.contentLength(contentLength);
        }

        return builder.body(resource);
    }

    /**
     * Minimal presign-endpoint (ingen auth ännu). Returnerar en tidsbegränsad URL
     * för direkt nedladdning från MinIO.
     *
     * Steg 1 för vecka 2: endast 200/404 och enkel TTL-hantering.
     */
    @PostMapping("/{id}/presign")
    public ResponseEntity<PresignedUrlResponseDTO> presign(@PathVariable Long id,
                                                           @RequestParam(name = "ttl", required = false) Integer ttlSeconds) {
        Attachment att = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found: " + id));

        // Enkel behörighetskontroll (steg 2, vecka 2):
        // - ADMIN och INVESTIGATOR får alltid presigna
        // - REPORTER får endast presigna om hen äger ärendet (ticket.reporter)
        User current = securityUtils.getCurrentUser();
        if (!authorizationService.canAccessAttachment(current, att)) {
            // Audit: access denied to presign for this attachment
            auditService.log(
                    AuditAction.ACCESS_DENIED,
                    "attachment",
                    null,
                    "presign denied: attachmentId=" + att.getId(),
                    att.getTicket(),
                    current
            );
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this attachment");
        }

        int ttl = (ttlSeconds == null ? 120 : ttlSeconds);
        if (ttl <= 0) ttl = 120;
        // Skydda mot extremt långa tider (t.ex. > 1 dag) i detta tidiga steg
        if (ttl > 86400) ttl = 86400; // 24h

        final String url;
        try {
            // Använd variant som sätter Content-Disposition till originalfilnamnet
            url = storageService.generatePresignedGetUrlWithContentDisposition(att.getS3Key(), ttl, att.getFileName());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to create presigned URL", ex);
        }

        // Audit: presigned URL issued
        auditService.log(
                AuditAction.FILE_PRESIGNED,
                "attachment",
                null,
                "presign issued: attachmentId=" + att.getId() + ", ttl=" + ttl,
                att.getTicket(),
                current
        );

        return ResponseEntity.ok(new PresignedUrlResponseDTO(url, ttl));
    }
}
