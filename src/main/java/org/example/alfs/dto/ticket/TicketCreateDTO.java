package org.example.alfs.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/*
 * Reporter creates ticket
 * The reporter provides the initial information describing the incident.
 *
 * The reporter cannot modify the ticket after creation. Any additional
 * information must be provided through comments.
 */

@Data
public class TicketCreateDTO {


    @NotBlank
    private String title;

    @NotBlank
    private String description;
}
