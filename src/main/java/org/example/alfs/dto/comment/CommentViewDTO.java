package org.example.alfs.dto.comment;

import lombok.Data;

import java.time.LocalDateTime;

/*
 * DTO returned when retrieving comments for a ticket.
 *
 * Contains comment message, creation timestamp and information
 * about who created the comment. The author can be either an
 * anonymous reporter or a logged-in user such as an investigator
 * or admin.
 *
 * Used when displaying ticket conversation history.
 */
@Data
public class CommentViewDTO {

    private Long id;
    private String message;
    private String author;
    private String role;
    private LocalDateTime createdAt;
}
