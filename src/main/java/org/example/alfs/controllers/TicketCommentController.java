package org.example.alfs.controllers;

import jakarta.validation.Valid;
import org.example.alfs.dto.comment.CommentCreateDTO;
import org.example.alfs.dto.comment.CommentViewDTO;
import org.example.alfs.entities.User;
import org.example.alfs.security.SecurityUtils;
import org.example.alfs.services.TicketCommentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/tickets")
public class TicketCommentController {

    private final TicketCommentService commentService;
    private final SecurityUtils securityUtils;

    public TicketCommentController(TicketCommentService commentService,
                                   SecurityUtils securityUtils) {
        this.commentService = commentService;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/{ticketId}/comments")
    public String addComment(
            @PathVariable Long ticketId,
            @Valid @ModelAttribute CommentCreateDTO dto,
            @RequestParam(required = false) String token
    ) {
        User user = getCurrentUserOrNull();

        commentService.addComment(ticketId, dto, user, token);

        String base = "redirect:/tickets/" + ticketId;
        return token != null
                ? base + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                : base;

    }

    @GetMapping("/{ticketId}/comments")
    @ResponseBody
    public List<CommentViewDTO> getComments(
            @PathVariable Long ticketId,
            @RequestParam(required = false) String token
    ) {
        User user = getCurrentUserOrNull();

        return commentService.getComments(ticketId, user, token);
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