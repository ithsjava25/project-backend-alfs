package org.example.alfs.services;

import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.dto.ticket.TicketViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.repositories.TicketRepository;
import org.springframework.stereotype.Service;

@Service
public class TicketService {


    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    //createNewTicket
    public TicketViewDTO createNewTicket(TicketCreateDTO ticketCreateDTO) {

        Ticket  ticket = new Ticket();

        Ticket save = ticketRepository.save(ticket);

        return ticketCreateDTO;
    }
}
