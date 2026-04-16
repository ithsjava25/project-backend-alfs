package org.example.alfs.controllers;

import io.minio.GetObjectResponse;
import org.example.alfs.dto.attachment.PresignedUrlDTO;
import org.example.alfs.entities.Attachment;
import org.example.alfs.repositories.AttachmentRepository;
import org.example.alfs.services.AuditService;
import org.example.alfs.enums.AuditAction;
import org.example.alfs.services.storage.MinioStorageService;
import org.example.alfs.security.SecurityUtils;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class AttachmentDownloadController {

    private final AttachmentRepository attachmentRepository;
    private final MinioStorageService storageService;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public AttachmentDownloadController(AttachmentRepository attachmentRepository,
                                        MinioStorageService storageService,
                                        AuditService auditService,
                                        SecurityUtils securityUtils) {
        this.attachmentRepository = attachmentRepository;
        this.storageService = storageService;
        this.auditService = auditService;
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

    @GetMapping("/{id}/presigned")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PresignedUrlDTO> presigned(@PathVariable Long id,
                                                     @org.springframework.web.bind.annotation.RequestParam(name = "reporterToken", required = false)
                                                     String reporterToken) {
        Attachment att = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found: " + id));

        Ticket ticket = att.getTicket();

        // 1) Om reporterToken skickas in: tillåt anonym åtkomst om token matchar ticketens reporterToken
        boolean accessedViaReporterToken = false;
        if (reporterToken != null && !reporterToken.isBlank()) {
            if (ticket == null || ticket.getReporterToken() == null || !ticket.getReporterToken().equals(reporterToken)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid reporterToken for this attachment");
            }
            accessedViaReporterToken = true;
        } else {
            // 2) Annars krävs inloggning: ADMIN eller INVESTIGATOR tilldelad ärendet
            User current = securityUtils.getCurrentUser();
            if (current.getRole() != Role.ADMIN) {
                if (current.getRole() == Role.INVESTIGATOR) {
                    if (ticket == null || ticket.getInvestigator() == null ||
                            !ticket.getInvestigator().getId().equals(current.getId())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this attachment");
                    }
                } else {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this attachment");
                }
            }
        }

        try {
            String url = storageService.createPresignedGetUrl(att.getS3Key(), java.time.Duration.ofMinutes(10));
            // Auditlogga generering av presigned URL som nedladdningshändelse
            if (ticket != null) {
                String newVal = "attachmentId:" + att.getId() + (accessedViaReporterToken ? ",via:reporterToken" : ",via:authenticated");
                auditService.log(AuditAction.ATTACHMENT_DOWNLOADED, "attachments", null, newVal, ticket);
            }
            return ResponseEntity.ok(new PresignedUrlDTO(url));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to generate presigned URL", ex);
        }
    }
}
