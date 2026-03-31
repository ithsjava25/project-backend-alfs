package org.example.alfs.dto.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/*
Admin assigns ticket to investigator
 */
@Data
public class TicketAssignDTO {

    @NotNull
    private Long handlerId;
}
