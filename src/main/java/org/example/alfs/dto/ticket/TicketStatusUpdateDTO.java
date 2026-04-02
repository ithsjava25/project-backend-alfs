package org.example.alfs.dto.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.alfs.enums.TicketStatus;

/*
*Investigator changes tickets status
*The status represents the lifecycle of the ticket
* (e.g. OPEN, IN_PROGRESS, RESOLVED, CLOSED).
*/
@Data
public class TicketStatusUpdateDTO {

    @NotNull
    private TicketStatus status;


}
