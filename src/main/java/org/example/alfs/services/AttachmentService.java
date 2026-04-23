package org.example.alfs.services;

import org.example.alfs.entities.Attachment;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.AuditAction;
import org.example.alfs.enums.Role;
import org.example.alfs.repositories.AttachmentRepository;
import org.example.alfs.repositories.TicketRepository;
import org.example.alfs.security.SecurityUtils;
import org.example.alfs.services.storage.MinioStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AttachmentService {

    private final MinioStorageService storageService;
    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public AttachmentService(MinioStorageService storageService,
                             AttachmentRepository attachmentRepository,
                             TicketRepository ticketRepository,
                             AuditService auditService,
                             SecurityUtils securityUtils) {
        this.storageService = storageService;
        this.attachmentRepository = attachmentRepository;
        this.ticketRepository = ticketRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public Attachment uploadToTicket(Long ticketId, MultipartFile file, User user, String token) throws Exception {

        Ticket ticket;

        if (user != null) {
            // logged in
            ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        } else {

            // validate token first
            if (token == null || token.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
            }

            // anonymous via token
            ticket = ticketRepository.findByReporterToken(token)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        }

        checkAccess(ticket, user, token);

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
            att.setUploadedBy(user);

            attachmentRepository.save(att);

            auditService.log(
                    AuditAction.ATTACHMENT_ADDED,
                    "attachments",
                    null,
                    att.getFileName(),
                    ticket,
                    user
            );

            return att;

        } catch (Exception e) {
            try {
                storageService.delete(objectKey);
            } catch (Exception deleteEx) {
                e.addSuppressed(deleteEx);
            }
            throw e;
        }
    }

    private User getCurrentUserOrNull() {
        try {
            return securityUtils.getCurrentUser();
        } catch (RuntimeException ex) {
            String message = ex.getMessage();

            boolean authFailure =
                    "No authenticated user in security context".equals(message) ||
                            "Authenticated user not found in database".equals(message);

            if (authFailure) {
                return null;
            }

            throw ex;
        }
    }

    private void checkAccess(Ticket ticket, User user, String token) {

        // ANONYMOUS VIA TOKEN
        if (user == null) {
            if (token != null && token.equals(ticket.getReporterToken())) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or missing token");
        }

        // ADMIN
        if (user.getRole() == Role.ADMIN) return;

        // INVESTIGATOR
        if (user.getRole() == Role.INVESTIGATOR) {
            if (ticket.getInvestigator() != null &&
                    ticket.getInvestigator().getId().equals(user.getId())) {
                return;
            }
        }

        // REPORTER
        if (user.getRole() == Role.REPORTER) {
            if (ticket.getReporter() != null &&
                    ticket.getReporter().getId().equals(user.getId())) {
                return;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    public List<Attachment> getAttachmentsByTicketId(Long ticketId) {
        return attachmentRepository.findByTicketId(ticketId);
    }

    public Attachment getAttachmentById(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attachment not found: " + id
                ));
    }
}
