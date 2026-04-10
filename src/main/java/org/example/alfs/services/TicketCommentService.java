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

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setMessage(dto.getMessage());

        boolean internalNote = dto.isInternalNote();
        if (internalNote && (author == null || (author.getRole() != Role.ADMIN && author.getRole() != Role.INVESTIGATOR))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only investigators/admins can create internal notes");
        }
        comment.setInternalNote(internalNote);

        TicketComment savedComment = ticketCommentRepository.save(comment);

        return ticketCommentMapper.entityToViewDTO(savedComment);
    }

    public List<CommentViewDTO> getComments(Long ticketId, User actor) {
        List<TicketComment> all = ticketCommentRepository
                .findByTicketIdOrderByCreatedAtAsc(ticketId);

        if (all.isEmpty()) {
            ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        }

        if (actor.getRole() == Role.REPORTER) {
            return all.stream()
                    .filter(comment -> !comment.isInternalNote())
                    .map(ticketCommentMapper::entityToViewDTO)
                    .toList();
        }

        return all.stream()
                .map(ticketCommentMapper::entityToViewDTO)
                .toList();
    }
}