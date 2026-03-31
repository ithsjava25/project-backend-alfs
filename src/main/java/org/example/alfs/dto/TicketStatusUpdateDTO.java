package org.example.alfs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.alfs.enums.TicketStatus;

/*
*Investigator changes tickets status
*The status represents the lifecycle of the ticket
* (e.g. OPEN, IN_PROGRESS, RESOLVED, CLOSED).
*/
@Data
public class TicketStatusUpdateDTO {

    @NotBlank
    private TicketStatus status;


}
