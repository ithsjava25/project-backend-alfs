package org.example.alfs.dto.ticket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.Data;
import org.example.alfs.enums.TicketStatus;

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
  private String token;
  private String title;
  private String description;
  private TicketStatus status;
  private LocalDateTime createdAt;

  private Long assignedInvestigatorId;
  private String assignedInvestigatorName;

  private static final DateTimeFormatter DISPLAY_FORMATTER =
      DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);

  public String getFormattedCreatedAt() {
    if (createdAt == null) return "";

    return createdAt.format(DISPLAY_FORMATTER);
  }
}
