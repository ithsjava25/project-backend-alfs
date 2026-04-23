package org.example.alfs.controllers;

import org.example.alfs.dto.attachment.AttachmentViewDTO;
import org.example.alfs.entities.User;
import org.example.alfs.security.SecurityUtils;
import org.example.alfs.services.AttachmentService;
import org.example.alfs.repositories.AttachmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@Controller
@RequestMapping("/api/files")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final SecurityUtils securityUtils;

    public AttachmentController(AttachmentService attachmentService, SecurityUtils securityUtils) {
        this.attachmentService = attachmentService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("ticketId") Long ticketId,
                         @RequestParam("file") MultipartFile file,
                         @RequestParam(required = false) String token) throws Exception {

        if (ticketId == null || ticketId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ticketId");
        }

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        var user = getCurrentUserOrNull();

        attachmentService.uploadToTicket(ticketId, file, user, token);

        if (token != null && !token.isBlank()) {
            return "redirect:/tickets/token/" + token;
        }

        return "redirect:/tickets/" + ticketId;
    }

    // List attachments for a ticket
    // List attachments for a ticket
    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<?> listByTicket(@RequestParam(name = "ticketId") Long ticketId) {
        if (ticketId <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid ticketId",
                    "detail", "Provide a positive numeric ticketId as a query parameter"
            ));
        }

        var attachments = attachmentService.getAttachmentsByTicketId(ticketId);

        var dtoList = attachments.stream().map(att -> new AttachmentViewDTO(
                att.getId(),
                att.getTicket() != null ? att.getTicket().getId() : null,
                att.getFileName(),
                "/api/files/" + att.getId() + "/download",
                att.getUploadedAt(),
                att.getUploadedBy() != null ? att.getUploadedBy().getUsername() : "Anonymous"
        )).toList();

        return ResponseEntity.ok(dtoList);
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
}
