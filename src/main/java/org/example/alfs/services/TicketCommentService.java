package org.example.alfs.services;

import org.example.alfs.dto.comment.CommentCreateDTO;
import org.example.alfs.dto.comment.CommentViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.TicketComment;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.example.alfs.mapper.TicketCommentMapper;
import org.example.alfs.repositories.TicketCommentRepository;
import org.example.alfs.repositories.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TicketCommentService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final TicketCommentMapper ticketCommentMapper;

    public TicketCommentService(TicketRepository ticketRepository,
                                TicketCommentRepository ticketCommentRepository,
                                TicketCommentMapper ticketCommentMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketCommentRepository = ticketCommentRepository;
        this.ticketCommentMapper = ticketCommentMapper;
    }

    @Transactional
    public CommentViewDTO addComment(Long ticketId, CommentCreateDTO dto, User author) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        boolean internalNote = dto.isInternalNote();

        checkAccess(ticket, author);
        checkInternalNotePermission(internalNote, author);

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setMessage(dto.getMessage());
        comment.setInternalNote(internalNote);

        TicketComment savedComment = ticketCommentRepository.save(comment);
        return ticketCommentMapper.entityToViewDTO(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentViewDTO> getComments(Long ticketId, User actor) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        checkAccess(ticket, actor);

        boolean isReporter = actor.getRole() == Role.REPORTER;

        List<TicketComment> all = isReporter
                ? ticketCommentRepository.findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(ticketId)
                : ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);

        return all.stream()
                .map(ticketCommentMapper::entityToViewDTO)
                .toList();
    }

    // helpers
    private void checkAccess(Ticket ticket, User user) {

        // If no user (anonymous) → deny access for now. Will be fixed later.
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication required");
        }

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

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Access denied");
    }

    private void checkInternalNotePermission(boolean internalNote, User author) {
        if (!internalNote) return;

        if (author == null || (author.getRole() != Role.ADMIN && author.getRole() != Role.INVESTIGATOR)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only investigators/admins can create internal notes");
        }
    }
}