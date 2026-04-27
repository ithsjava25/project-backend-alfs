package org.example.alfs.mapper;

import org.example.alfs.dto.ticket.TicketViewDTO;
import org.example.alfs.entities.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

  public TicketViewDTO entityToViewDTO(Ticket ticket) {

    TicketViewDTO ticketViewDTO = new TicketViewDTO();

    ticketViewDTO.setId(ticket.getId());
    ticketViewDTO.setTitle(ticket.getTitle());
    ticketViewDTO.setDescription(ticket.getDescription());
    ticketViewDTO.setStatus(ticket.getStatus());
    ticketViewDTO.setCreatedAt(ticket.getCreatedAt());

    if (ticket.getInvestigator() != null) {
      ticketViewDTO.setAssignedInvestigatorId(ticket.getInvestigator().getId());
      ticketViewDTO.setAssignedInvestigatorName(ticket.getInvestigator().getUsername());
    }

    return ticketViewDTO;
  }
}
