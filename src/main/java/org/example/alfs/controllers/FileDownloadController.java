package org.example.alfs.controllers;

import io.minio.GetObjectResponse;
import org.example.alfs.entities.Attachment;
import org.example.alfs.repositories.AttachmentRepository;
import org.example.alfs.service.storage.MinioStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileDownloadController {

    private final AttachmentRepository attachmentRepository;
    private final MinioStorageService storageService;

    public FileDownloadController(AttachmentRepository attachmentRepository,
                                  MinioStorageService storageService) {
        this.attachmentRepository = attachmentRepository;
        this.storageService = storageService;
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws Exception {
        Attachment att = attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + id));

        try (GetObjectResponse object = storageService.download(att.getS3Key())) {
            byte[] bytes = object.readAllBytes();
            String fileName = att.getFileName() != null ? att.getFileName() : "file";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        }
    }
}
