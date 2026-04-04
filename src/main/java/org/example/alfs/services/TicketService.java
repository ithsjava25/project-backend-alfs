package org.example.alfs.services;

import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.dto.ticket.TicketViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.mapper.TicketMapper;
import org.example.alfs.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TicketService {


    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketService(TicketRepository ticketRepository,  TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    //createNewTicket
    public TicketViewDTO createNewTicket(TicketCreateDTO ticketCreateDTO) {

        Ticket  ticket = new Ticket();

        ticket.setTitle(ticketCreateDTO.getTitle());
        ticket.setDescription(ticketCreateDTO.getDescription());

        Ticket save = ticketRepository.save(ticket);

        return ticketMapper.entityToViewDTO(save);
    }

    // View by token
    public TicketViewDTO getTicketByToken(String token) {

        Ticket ticket = ticketRepository.findByReporterToken(token).
                orElseThrow(() -> new RuntimeException("Ticket not found"));
        return ticketMapper.entityToViewDTO(ticket);
    }

    //findByReporterId

    //findByInvestigatorId

    //findByStatus

    //findByStatusAndInvestigatorId

    //findAll (pageable)
}
