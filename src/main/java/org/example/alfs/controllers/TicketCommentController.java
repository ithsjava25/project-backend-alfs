package org.example.alfs.controllers;

import jakarta.validation.Valid;
import org.example.alfs.dto.comment.CommentCreateDTO;
import org.example.alfs.dto.comment.CommentViewDTO;
import org.example.alfs.entities.User;
import org.example.alfs.security.SecurityUtils;
import org.example.alfs.services.TicketCommentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
            @Valid @ModelAttribute CommentCreateDTO dto
    ) {
        User user = getCurrentUserOrNull(); // unauthenticated users currently have no access; anonymous flow will be added later.

        commentService.addComment(ticketId, dto, user);

        return "redirect:/tickets/" + ticketId;
    }

    @GetMapping("/{ticketId}/comments")
    @ResponseBody
    public List<CommentViewDTO> getComments(@PathVariable Long ticketId) {

        User user = getCurrentUserOrNull();

        return commentService.getComments(ticketId, user);
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