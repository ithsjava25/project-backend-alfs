package org.example.alfs.controllers;

import io.minio.GetObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.*;
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
    @Operation(
            summary = "Download attachment",
            description = "Downloads a file attached to a ticket."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "File downloaded successfully",
                    content = @Content(
                            mediaType = "application/octet-stream",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Attachment not found"),
            @ApiResponse(responseCode = "502", description = "Failed to fetch file from storage")
    })
    public ResponseEntity<Resource> download(
            @Parameter(description = "ID of the attachment", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Optional access token")
            @RequestParam(required = false) String token
    ) {

        Attachment att = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attachment not found: " + id
                ));

        final GetObjectResponse object;
        try {
            object = storageService.download(att.getS3Key());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to download attachment content",
                    ex
            );
        }

        String fileName = att.getFileName() != null ? att.getFileName() : "file";

        String contentDisposition = ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString();

        InputStreamResource resource = new InputStreamResource(object);

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