package org.example.alfs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            @Valid @ModelAttribute CommentCreateDTO dto,
            @RequestParam(required = false) String token
    ) {
        User user = getCurrentUserOrNull();

        commentService.addComment(ticketId, dto, user, token);

        if (token != null && !token.isBlank()) {
            return "redirect:/tickets/token/" + token;
        }

        return "redirect:/tickets/" + ticketId;
    }

    @Operation(
            summary = "Get comments for a ticket",
            description = "Returns all comments for a ticket. Supports both authenticated users and anonymous users via token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comments retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentViewDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{ticketId}/comments")
    @ResponseBody
    public List<CommentViewDTO> getComments(

            @Parameter(description = "ID of the ticket", example = "1")
            @PathVariable Long ticketId,

            @Parameter(description = "Optional token for anonymous access")
            @RequestParam(required = false) String token
    )
    {
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