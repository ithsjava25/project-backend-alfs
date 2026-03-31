package org.example.alfs.dto.ticket;

import lombok.Data;

import java.time.LocalDateTime;

/*
 * DTO returned when retrieving ticket information.
 *
 * Contains the main ticket data such as title, description, status,
 * creation timestamp and assigned handler.
 *
 * Used by admin and investigators when viewing tickets.
 */
@Data
public class TicketViewDTO {

    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    private Long assignedHandlerId;
}
