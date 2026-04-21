package org.example.alfs.services;

import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.dto.ticket.TicketViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.AuditAction;
import org.example.alfs.enums.Role;
import org.example.alfs.enums.TicketStatus;
import org.example.alfs.mapper.TicketMapper;
import org.example.alfs.repositories.TicketRepository;
import org.example.alfs.repositories.UserRepository;
import org.example.alfs.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public TicketService(TicketRepository ticketRepository,
                         TicketMapper ticketMapper,
                         SecurityUtils securityUtils,
                         UserRepository userRepository,
                         AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.securityUtils = securityUtils;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    //createNewTicket
    @Transactional
    public TicketViewDTO createNewTicket(TicketCreateDTO dto) {

        Ticket ticket = new Ticket();

        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());

        User user = securityUtils.getCurrentUserOrNull();

        String token = null;

        if (user != null) {
            ticket.setReporter(user);
        } else {
            token = java.util.UUID.randomUUID().toString();
            ticket.setReporterToken(token);
        }

        Ticket saved = ticketRepository.save(ticket);

        auditService.log(
                AuditAction.CREATED,
                "title",
                null,
                saved.getTitle(),
                saved,
                user
        );

        TicketViewDTO view = ticketMapper.entityToViewDTO(saved);

        if (token != null) {
            view.setToken(token);
        }

        return view;
    }


    // View by token
    public TicketViewDTO getTicketByToken(String token) {
        Ticket ticket = ticketRepository.findByReporterToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        TicketViewDTO view = ticketMapper.entityToViewDTO(ticket);

        view.setToken(ticket.getReporterToken());

        return view;
    }

    //findById
    public TicketViewDTO getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        checkAccess(ticket);

        return ticketMapper.entityToViewDTO(ticket);
    }

    // Get all tickets for a reporter
    public List<TicketViewDTO> getMyTickets() {
        User user = requireCurrentUser();

        return ticketRepository.findByReporterId(user.getId())
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }

    // Get all tickets assigned to me
    public List<TicketViewDTO> getMyAssignedTickets() {
        User user = requireCurrentUser();

        return ticketRepository.findByInvestigatorId(user.getId())
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }

    // this is for admin page
    public List<TicketViewDTO> getAllTickets() {

        User user = requireCurrentUser();
        requireAdmin(user);

        return ticketRepository.findAll()
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }

    // ----------------- filters -----------------

    public List<TicketViewDTO> getTicketsByStatus(TicketStatus status) {
        User user = requireCurrentUser();
        requireAdmin(user);

        return ticketRepository.findByStatus(status)
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }

    public List<TicketViewDTO> getTicketsByStatusAndInvestigator(TicketStatus status, Long investigatorId) {
        User user = requireCurrentUser();

        if (user.getRole() == Role.ADMIN) {
            return ticketRepository.findByStatusAndInvestigatorId(status, investigatorId)
                    .stream()
                    .map(ticketMapper::entityToViewDTO)
                    .toList();
        }

        if (user.getRole() == Role.INVESTIGATOR &&
                user.getId().equals(investigatorId)) {

            return ticketRepository.findByStatusAndInvestigatorId(status, investigatorId)
                    .stream()
                    .map(ticketMapper::entityToViewDTO)
                    .toList();
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    // ----------------- helpers -----------------

    private void checkAccess(Ticket ticket) {
        User user = requireCurrentUser();

        if (user.getRole() == Role.ADMIN) return;

        if (user.getRole() == Role.INVESTIGATOR) {
            if (ticket.getInvestigator() != null &&
                    ticket.getInvestigator().getId().equals(user.getId())) {
                return;
            }
        }

        if (user.getRole() == Role.REPORTER) {
            if (ticket.getReporter() != null &&
                    ticket.getReporter().getId().equals(user.getId())) {
                return;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void requireAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private User requireCurrentUser() {
        try {
            return securityUtils.getCurrentUser();
        } catch (RuntimeException ex) {

            String message = ex.getMessage();

            boolean authFailure =
                    "No authenticated user in security context".equals(message) ||
                            "Authenticated user not found in database".equals(message);

            if (authFailure) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authentication required",
                        ex
                );
            }

            throw ex;
        }
    }


    // ----------------- status logic -----------------
    @Transactional
    public TicketViewDTO updateTicketStatus(Long id, TicketStatus newStatus) {

        User user = requireCurrentUser();
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        if (user.getRole() != Role.ADMIN) {
            boolean isAssignedInvestigator =
                    user.getRole() == Role.INVESTIGATOR &&
                            ticket.getInvestigator() != null &&
                            ticket.getInvestigator().getId().equals(user.getId());

            if (!isAssignedInvestigator) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Access denied");
            }
        }

        TicketStatus currentStatus = ticket.getStatus();

        if (currentStatus == newStatus) {
            return ticketMapper.entityToViewDTO(ticket);
        }

        Set<TicketStatus> allowedTransitions = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        boolean isValidTransition = allowedTransitions.contains(newStatus);

        if (!isValidTransition) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid transition from " + currentStatus + " to " + newStatus);
        }

        if (newStatus == TicketStatus.IN_PROGRESS && ticket.getInvestigator() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Cannot move to IN_PROGRESS without investigator");
        }

        ticket.setStatus(newStatus);

        Ticket savedTicket = ticketRepository.save(ticket);

        auditService.log(
                AuditAction.STATUS_CHANGED,
                "status",
                currentStatus.name(),
                newStatus.name(),
                savedTicket,
                user
        );
        return ticketMapper.entityToViewDTO(savedTicket);
    }

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.OPEN, Set.of(TicketStatus.IN_PROGRESS),
            TicketStatus.IN_PROGRESS, Set.of(TicketStatus.RESOLVED),
            TicketStatus.RESOLVED, Set.of(TicketStatus.CLOSED),
            TicketStatus.CLOSED, Set.of()
    );


    @Transactional
    public TicketViewDTO assignInvestigator(Long id, Long investigatorId) {

        if (investigatorId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Investigator ID is required");
        }

        User user = requireCurrentUser();
        requireAdmin(user);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getInvestigator() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ticket already has an investigator assigned");
        }

        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ticket must be in OPEN status to assign an investigator");
        }

        User investigator = userRepository.findById(investigatorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Investigator not found"));

        if (investigator.getRole() != Role.INVESTIGATOR) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User is not an investigator");
        }

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setInvestigator(investigator);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        Ticket savedTicket = ticketRepository.save(ticket);


        auditService.log(
                AuditAction.STATUS_CHANGED,
                "status",
                oldStatus.name(),
                TicketStatus.IN_PROGRESS.name(),
                savedTicket,
                user
        );

        auditService.log(
                AuditAction.ASSIGNED,
                "investigator",
                null,
                investigator.getUsername(),
                savedTicket,
                user
        );


        return ticketMapper.entityToViewDTO(savedTicket);
    }

    @Transactional
    public TicketViewDTO unassignInvestigator(Long id) {

        User user = requireCurrentUser();
        requireAdmin(user);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getInvestigator() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No investigator assigned");
        }

        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Must be IN_PROGRESS");
        }

        String oldInvestigator = ticket.getInvestigator().getUsername();

        ticket.setInvestigator(null);
        ticket.setStatus(TicketStatus.OPEN);

        Ticket savedTicket = ticketRepository.save(ticket);

        auditService.log(
                AuditAction.UNASSIGNED,
                "investigator",
                oldInvestigator,
                null,
                savedTicket,
                user
        );

        return ticketMapper.entityToViewDTO(savedTicket);
    }
}