package org.example.alfs.services;

import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.dto.ticket.TicketViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.enums.TicketStatus;
import org.example.alfs.mapper.TicketMapper;
import org.example.alfs.repositories.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketService(TicketRepository ticketRepository, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    //createNewTicket
    public TicketViewDTO createNewTicket(TicketCreateDTO ticketCreateDTO) {
        Ticket ticket = new Ticket();

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

    //findById
    public TicketViewDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Ticket not found"));

        return ticketMapper.entityToViewDTO(ticket);
    }

    //findByReporterId

    //findByInvestigatorId

    //findByStatus

    //findByStatusAndInvestigatorId

    //findAll (pageable)

    @Transactional
    public TicketViewDTO updateTicketStatus(Long id, TicketStatus newStatus) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // No-op check
        if (ticket.getStatus() == newStatus) {
            return ticketMapper.entityToViewDTO(ticket);
        }

        // TODO: Check role

        Set<TicketStatus> allowedTransitions = ALLOWED_TRANSITIONS.getOrDefault(ticket.getStatus(), Set.of());

        if (!allowedTransitions.contains(newStatus)) {
            throw new IllegalStateException("Invalid ticket status transition: Cannot transition from " + ticket.getStatus() + " to " + newStatus);
        }

        ticket.setStatus(newStatus);
        Ticket saved = ticketRepository.save(ticket);

        // TODO: Audit log(-service?), auditLogService.log()

        return ticketMapper.entityToViewDTO(saved);
    }

    // Bestäm vilka övergångar/transitions som är tillåtna
    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS),
            TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED),
            TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS),
            TicketStatus.CLOSED, Set.of()
    );

}
