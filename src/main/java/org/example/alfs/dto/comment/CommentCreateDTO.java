package org.example.alfs.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * DTO used to create a comment on a ticket.
 * Can be used by reporter, investigator and admin.
 */
@Data
public class CommentCreateDTO {

    @NotBlank
    private String message;

    private boolean internalNote = false;
}
