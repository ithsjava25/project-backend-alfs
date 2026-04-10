package org.example.alfs.services;

import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.dto.ticket.TicketViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.TicketStatus;
import org.example.alfs.mapper.TicketMapper;
import org.example.alfs.repositories.TicketRepository;
import org.example.alfs.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository, TicketMapper ticketMapper, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.userRepository = userRepository;
    }

    //createNewTicket
    public TicketViewDTO createNewTicket(TicketCreateDTO ticketCreateDTO) {
        Ticket ticket = new Ticket();

        ticket.setTitle(ticketCreateDTO.getTitle());
        ticket.setDescription(ticketCreateDTO.getDescription());

        Ticket savedTicket = ticketRepository.save(ticket);

        return ticketMapper.entityToViewDTO(savedTicket);
    }

    // View by token
    public TicketViewDTO getTicketByToken(String token) {
        Ticket ticket = ticketRepository.findByReporterToken(token).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        return ticketMapper.entityToViewDTO(ticket);
    }

    //findById
    public TicketViewDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        return ticketMapper.entityToViewDTO(ticket);
    }

    //findByReporterId
    public List<TicketViewDTO> getTicketsByReporterId(Long reporterId) {
        return ticketRepository.findByReporterId(reporterId)
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }

    //findByInvestigatorId
    public List<TicketViewDTO> getTicketsByInvestigatorId(Long investigatorId) {
        return ticketRepository.findByInvestigatorId(investigatorId)
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }

    //findByStatus
    public List<TicketViewDTO> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status)
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }

    //findByStatusAndInvestigatorId
    public List<TicketViewDTO> getTicketsByStatusAndInvestigator(
            TicketStatus status,
            Long investigatorId) {
        return ticketRepository
                .findByStatusAndInvestigatorId(status, investigatorId)
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }

    //findAll (pageable)

    @Transactional
    public TicketViewDTO updateTicketStatus(Long id, TicketStatus newStatus) {
        // TODO: Check role? Is user is Admin or Investigator?
//        Typ/Placeholder:
//        if (user.getRole() != Role.ADMIN && user.getRole() != Role.INVESTIGATOR) {
//            throw new AccessDeniedException("Only admins or investigators can update ticket status");
//        }
//        */

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        TicketStatus oldStatus = ticket.getStatus();

        // No-op check
        if (oldStatus == newStatus) {
            return ticketMapper.entityToViewDTO(ticket);
        }

        Set<TicketStatus> allowedTransitions = ALLOWED_TRANSITIONS.getOrDefault(ticket.getStatus(), Set.of());

        if (!allowedTransitions.contains(newStatus)) {
            throw new IllegalStateException("Invalid ticket status transition: Cannot transition from " + ticket.getStatus() + " to " + newStatus);
        }

        ticket.setStatus(newStatus);
        Ticket savedTicket = ticketRepository.save(ticket);

        // TODO: Audit log(-service?), auditLogService.log()
//        Typ/Placeholder:
//        auditLogService.log(ticket.getId(), user, "STATUS_CHANGED",
//                "Status changed from " + oldStatus + " to " + newStatus);

        return ticketMapper.entityToViewDTO(savedTicket);
    }

    // Bestäm vilka övergångar/transitions som är tillåtna
    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS),
            TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED),
            TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS),
            TicketStatus.CLOSED, Set.of()
    );

    @Transactional
    public TicketViewDTO assignInvestigator(Long id, Long investigatorId) {
        // TODO Check if user is admin
//        Typ/Placeholder:
//        if (user.getRole() != Role.ADMIN) {
//            throw new AccessDeniedException("Only admins can assign handlers");
//        }
//        */

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getInvestigator() != null) {
            throw new IllegalStateException("Ticket already has an investigator assigned");
        }

        // TODO: Check if user is investigator
        // Typ/Placeholder:
//        if (user.getRole() != Role.INVESTIGATOR) {
//            throw new IllegalArgumentException("User is not an investigator");
//        }

        User investigator = userRepository.findById(investigatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Investigator not found"));

        ticket.setInvestigator(investigator);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        Ticket savedTicket = ticketRepository.save(ticket);

        // TODO: auditLogService.log()
//        Typ/Placeholder:
//        auditLogService.log(ticket.getId(), user, "ASSIGNED",
//                "Ticket assigned to " + investigatorId);

        return ticketMapper.entityToViewDTO(savedTicket);
    }

    @Transactional
    public TicketViewDTO unassignInvestigator(Long id) {
        // TODO: Check if user is admin
//        Typ/Placeholder:
//        if (actor.getRole() != Role.ADMIN) {
//            throw new AccessDeniedException("Only admins can unassign handlers");
//        }
//        */
//
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getInvestigator() == null) {
            throw new IllegalStateException("Ticket does not have an investigator assigned");
        }

        ticket.setInvestigator(null);
        ticket.setStatus(TicketStatus.OPEN);

        Ticket savedTicket = ticketRepository.save(ticket);

        // TODO: auditLogService.log()
//        Typ/Placeholder:
//        auditLogService.log(ticket.getId(), user, "UNASSIGNED",
//                "Ticket unassigned from " + investigatorId);

        return ticketMapper.entityToViewDTO(savedTicket);
    }

}
