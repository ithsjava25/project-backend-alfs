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
    public CommentViewDTO addComment(Long ticketId, CommentCreateDTO dto, User author, String token) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        boolean internalNote = dto.isInternalNote();

        checkAccess(ticket, author, token);
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
    public List<CommentViewDTO> getComments(Long ticketId, User user, String token) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        checkAccess(ticket, user, token);

        boolean isReporter = user != null && user.getRole() == Role.REPORTER;
        boolean isAnonymous = user == null;

        List<TicketComment> all = (isReporter || isAnonymous)
                ? ticketCommentRepository.findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(ticketId)
                : ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);

        return all.stream()
                .map(ticketCommentMapper::entityToViewDTO)
                .toList();
    }

    // helpers
    private void checkAccess(Ticket ticket, User user, String token) {
        // Authenticated
        if (user != null) {
            if (user.getRole() == Role.ADMIN) return;

            if (user.getRole() == Role.INVESTIGATOR &&
                    ticket.getInvestigator() != null &&
                    ticket.getInvestigator().getId().equals(user.getId())) {
                return;
            }

            if (user.getRole() == Role.REPORTER &&
                    ticket.getReporter() != null &&
                    ticket.getReporter().getId().equals(user.getId())) {
                return;
            }
        }

        // Anonymous
        if (token != null && token.equals(ticket.getReporterToken())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void checkInternalNotePermission(boolean internalNote, User author) {
        if (!internalNote) return;

        if (author == null || (author.getRole() != Role.ADMIN && author.getRole() != Role.INVESTIGATOR)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only investigators/admins can create internal notes");
        }
    }
}