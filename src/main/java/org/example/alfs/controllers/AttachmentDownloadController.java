package org.example.alfs.controllers;

import io.minio.GetObjectResponse;
import org.example.alfs.entities.Attachment;
import org.example.alfs.dto.attachment.PresignedUrlResponseDTO;
import org.example.alfs.repositories.AttachmentRepository;
import org.example.alfs.security.SecurityUtils;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.example.alfs.services.storage.MinioStorageService;
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

    public AttachmentDownloadController(AttachmentRepository attachmentRepository,
                                        MinioStorageService storageService,
                                        SecurityUtils securityUtils) {
        this.attachmentRepository = attachmentRepository;
        this.storageService = storageService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Attachment att = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found: " + id));

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
        if (!canPresign(current, att)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this attachment");
        }

        int ttl = (ttlSeconds == null ? 120 : ttlSeconds);
        if (ttl <= 0) ttl = 120;
        // Skydda mot extremt långa tider (t.ex. > 1 dag) i detta tidiga steg
        if (ttl > 86400) ttl = 86400; // 24h

        final String url;
        try {
            url = storageService.generatePresignedGetUrl(att.getS3Key(), ttl);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to create presigned URL", ex);
        }

        return ResponseEntity.ok(new PresignedUrlResponseDTO(url, ttl));
    }

    private boolean canPresign(User user, Attachment att) {
        if (user == null) return false;
        Role role = user.getRole();
        if (role == Role.ADMIN || role == Role.INVESTIGATOR) return true;
        if (role == Role.REPORTER) {
            var ticket = att.getTicket();
            return ticket != null && ticket.getReporter() != null && ticket.getReporter().getId().equals(user.getId());
        }
        return false;
    }
}
