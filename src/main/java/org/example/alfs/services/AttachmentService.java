package org.example.alfs.services;

import org.example.alfs.entities.Attachment;
import org.example.alfs.entities.Ticket;
import org.example.alfs.enums.AuditAction;
import org.example.alfs.repositories.AttachmentRepository;
import org.example.alfs.repositories.TicketRepository;
import org.example.alfs.services.storage.MinioStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttachmentService {

    private final MinioStorageService storageService;
    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final AuditService auditService;

    public AttachmentService(MinioStorageService storageService,
                             AttachmentRepository attachmentRepository,
                             TicketRepository ticketRepository,
                             AuditService auditService) {
        this.storageService = storageService;
        this.attachmentRepository = attachmentRepository;
        this.ticketRepository = ticketRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Attachment uploadToTicket(Long ticketId, MultipartFile file) throws Exception {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: " + ticketId));

        String objectKey = storageService.upload(file);

        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.isBlank()) {
                fileName = "file";
            }

            Attachment att = new Attachment();
            att.setFileName(fileName);
            att.setS3Key(objectKey);
            att.setTicket(ticket);
            attachmentRepository.save(att);

            auditService.log(AuditAction.ATTACHMENT_ADDED, "attachments", null, "objectKey:" + objectKey, ticket);

            return att;
        } catch (Exception e) {
            // Compensation: delete uploaded object if DB operations fail
            try {
                storageService.delete(objectKey);
            } catch (Exception deleteEx) {
                // Log but don't mask original exception
                e.addSuppressed(deleteEx);
            }
            throw e;
        }
    }
}
