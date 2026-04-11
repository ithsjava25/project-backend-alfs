package org.example.alfs.mapper;

import org.example.alfs.dto.comment.CommentViewDTO;
import org.example.alfs.entities.TicketComment;
import org.springframework.stereotype.Component;

@Component
public class TicketCommentMapper {
    public CommentViewDTO entityToViewDTO(TicketComment comment) {
        CommentViewDTO dto = new CommentViewDTO();

        dto.setId(comment.getId());
        dto.setMessage(comment.getMessage());
        dto.setCreatedAt(comment.getCreatedAt());

        if (comment.getAuthor() != null) {
            dto.setAuthor(comment.getAuthor().getUsername());
            dto.setRole(comment.getAuthor().getRole().name());
        } else {
            dto.setAuthor("Anonymous");
            dto.setRole(null);
        }

        return dto;
    }
}
