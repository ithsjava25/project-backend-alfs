package org.example.alfs.services;

import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.dto.ticket.TicketViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.example.alfs.enums.TicketStatus;
import org.example.alfs.mapper.TicketMapper;
import org.example.alfs.repositories.TicketRepository;
import org.example.alfs.repositories.UserRepository;
import org.example.alfs.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;


@Service
public class TicketService {


    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository,  TicketMapper ticketMapper, SecurityUtils securityUtils, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.securityUtils = securityUtils;
        this.userRepository = userRepository;
    }

    //createNewTicket - need to be signed in atm - this should change later when we have anonymous access
    public TicketViewDTO createNewTicket(TicketCreateDTO ticketCreateDTO) {

        Ticket ticket = new Ticket();

        ticket.setTitle(ticketCreateDTO.getTitle());
        ticket.setDescription(ticketCreateDTO.getDescription());

        User user = securityUtils.getCurrentUser();
        ticket.setReporter(user);

        Ticket save = ticketRepository.save(ticket);

        return ticketMapper.entityToViewDTO(save);
    }

    // View by token - for anonymous users
    // TODO: filter sensitive data for anonymous users (e.g. internal comments, investigator info)
    public TicketViewDTO getTicketByToken(String token) {

        Ticket ticket = ticketRepository.findByReporterToken(token).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        return ticketMapper.entityToViewDTO(ticket);
    }

    //findById
    public TicketViewDTO getTicketById(Long id) {

        Ticket ticket = ticketRepository.findById(id).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        // DEBUG START
        System.out.println("==== GET TICKET DEBUG ====");
        System.out.println("Ticket ID: " + ticket.getId());
        System.out.println("Ticket reporter ID: " +
                (ticket.getReporter() != null ? ticket.getReporter().getId() : "null"));
        // DEBUG END

        checkAccess(ticket);

        return ticketMapper.entityToViewDTO(ticket);

    }

    // Get all tickets for a reporter
    public List<TicketViewDTO> getMyTickets() {

        User user = securityUtils.getCurrentUser();

        return ticketRepository.findByReporterId(user.getId())
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }

    // Get all tickets assigned to me - investigator
    public List<TicketViewDTO> getMyAssignedTickets() {

        User user = securityUtils.getCurrentUser();

        return ticketRepository.findByInvestigatorId(user.getId())
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }


    //findAll (pageable)

    // Assign ticket method
    public void assignTicket(Long ticketId, Long investigatorId) {

        User currentUser = securityUtils.getCurrentUser();

        // Only ADMIN can assign tickets
        requireAdmin(currentUser);

        // Get ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        // Get investigator user
        User investigator = userRepository.findById(investigatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Ensure user is actually an INVESTIGATOR
        if (investigator.getRole() != Role.INVESTIGATOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an investigator");
        }

        if (ticket.getInvestigator() != null) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket already assigned");
        }

        // Assign ticket
        ticket.setInvestigator(investigator);

        ticketRepository.save(ticket);
    }


    public void updateStatus(Long ticketId, TicketStatus status) {

        User user = securityUtils.getCurrentUser();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        // ADMIN can update all tickets
        if (user.getRole() == Role.ADMIN) {
            ticket.setStatus(status);
            ticketRepository.save(ticket);
            return;
        }

        // INVESTIGATOR can only update assigned tickets
        if (user.getRole() == Role.INVESTIGATOR) {
            if (ticket.getInvestigator() != null &&
                    ticket.getInvestigator().getId().equals(user.getId())) {

                ticket.setStatus(status);
                ticketRepository.save(ticket);
                return;
            }
        }

        // All others denied
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }


    // ----------------- filters -----------------

    //findByStatus
    public List<TicketViewDTO> getTicketsByStatus(TicketStatus status) {

        User user = securityUtils.getCurrentUser();

        // Only ADMIN can get all tickets with a specific status
        requireAdmin(user);

        return ticketRepository.findByStatus(status)
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }

    //findByStatusAndInvestigatorId
    public List<TicketViewDTO> getTicketsByStatusAndInvestigator(TicketStatus status, Long investigatorId) {

        User user = securityUtils.getCurrentUser();

        // ADMIN can see everything
        if (user.getRole() == Role.ADMIN) {
            return ticketRepository
                    .findByStatusAndInvestigatorId(status, investigatorId)
                    .stream()
                    .map(ticketMapper::entityToViewDTO)
                    .toList();
        }

        // INVESTIGATOR can only see their own tickets
        if (user.getRole() == Role.INVESTIGATOR &&
                user.getId().equals(investigatorId)) {

            return ticketRepository
                    .findByStatusAndInvestigatorId(status, investigatorId)
                    .stream()
                    .map(ticketMapper::entityToViewDTO)
                    .toList();
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }


    // ----------------- helpers -----------------
    private void checkAccess(Ticket ticket) {

        // Get the currently authenticated user from SecurityContext
        User user = securityUtils.getCurrentUser();

        // DEBUG START
        System.out.println("==== ACCESS CHECK ====");
        System.out.println("Logged in user: " + user.getUsername() + " (id=" + user.getId() + ")");
        System.out.println("User role: " + user.getRole());

        System.out.println("Ticket reporter: " +
                (ticket.getReporter() != null ? ticket.getReporter().getId() : "null"));

        System.out.println("Ticket investigator: " +
                (ticket.getInvestigator() != null ? ticket.getInvestigator().getId() : "null"));
        System.out.println("======================");
        // DEBUG END

        // ADMIN - always allowed to access any ticket
        if (user.getRole() == Role.ADMIN) return;

        // INVESTIGATOR - allowed only if the ticket is assigned to this user
        if (user.getRole() == Role.INVESTIGATOR) {
            if (ticket.getInvestigator() != null &&
                    ticket.getInvestigator().getId().equals(user.getId())) {
                return;
            }
        }

        // REPORTER - allowed only if the user is the creator of the ticket
        if (user.getRole() == Role.REPORTER) {
            if (ticket.getReporter() != null &&
                    ticket.getReporter().getId().equals(user.getId())) {
                return;
            }
        }

        // If none of the above conditions match -> deny access
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void requireAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }
}
