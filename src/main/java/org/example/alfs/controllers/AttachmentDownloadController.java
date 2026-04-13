package org.example.alfs.controllers;

import io.minio.GetObjectResponse;
import org.example.alfs.entities.Attachment;
import org.example.alfs.repositories.AttachmentRepository;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class AttachmentDownloadController {

    private final AttachmentRepository attachmentRepository;
    private final MinioStorageService storageService;

    public AttachmentDownloadController(AttachmentRepository attachmentRepository,
                                        MinioStorageService storageService) {
        this.attachmentRepository = attachmentRepository;
        this.storageService = storageService;
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
}
