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
        User user = getCurrentUserOrNull(); // anonymous user should be able to comment.

        commentService.addComment(ticketId, dto, user);

        return "redirect:/view/id/" + ticketId;
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
        } catch (Exception e) {
            return null;
        }
    }
}