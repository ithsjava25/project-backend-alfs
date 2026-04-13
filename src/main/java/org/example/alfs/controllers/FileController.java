package org.example.alfs.controllers;

import org.example.alfs.dto.attachment.AttachmentViewDTO;
import org.example.alfs.entities.Attachment;
import org.example.alfs.service.AttachmentService;
import org.example.alfs.repositories.AttachmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final AttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;

    public FileController(AttachmentService attachmentService, AttachmentRepository attachmentRepository) {
        this.attachmentService = attachmentService;
        this.attachmentRepository = attachmentRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("ticketId") Long ticketId,
                                    @RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "File is empty",
                    "detail", "Please provide a non-empty file in form-data field 'file'"
            ));
        }

        Attachment att = attachmentService.uploadToTicket(ticketId, file);
        AttachmentViewDTO dto = new AttachmentViewDTO(
                att.getId(),
                att.getTicket() != null ? att.getTicket().getId() : null,
                att.getFileName(),
                att.getS3Key(),
                att.getUploadedAt()
        );
        return ResponseEntity.ok(dto);
    }

    // List attachments for a ticket
    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<?> listByTicket(@RequestParam(name = "ticketId") Long ticketId) {
        if (ticketId <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid ticketId",
                    "detail", "Provide a positive numeric ticketId as a query parameter"
            ));
        }

        var attachments = attachmentRepository.findByTicketId(ticketId);
        var dtoList = attachments.stream().map(att -> new AttachmentViewDTO(
                att.getId(),
                att.getTicket() != null ? att.getTicket().getId() : null,
                att.getFileName(),
                att.getS3Key(),
                att.getUploadedAt()
        )).toList();
        return ResponseEntity.ok(dtoList);
    }
}
