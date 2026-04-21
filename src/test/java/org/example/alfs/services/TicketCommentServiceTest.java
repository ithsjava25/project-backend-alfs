package org.example.alfs.services;

import org.example.alfs.dto.comment.CommentCreateDTO;
import org.example.alfs.dto.comment.CommentViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.example.alfs.enums.TicketStatus;
import org.example.alfs.mapper.TicketCommentMapper;
import org.example.alfs.repositories.TicketCommentRepository;
import org.example.alfs.repositories.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("TicketCommentService Test")
@ExtendWith(MockitoExtension.class)
class TicketCommentServiceTest {

    @Mock
    TicketRepository ticketRepository;
    @Mock
    TicketCommentRepository ticketCommentRepository;
    @Mock
    TicketCommentMapper ticketCommentMapper;

    @InjectMocks
    TicketCommentService ticketCommentService;

    private Ticket openTicketWithReporter(User reporter) {
        Ticket t = new Ticket();
        t.setId(1L);
        t.setStatus(TicketStatus.OPEN);
        t.setReporter(reporter);
        return t;
    }

    private User adminUser() {
        User u = new User();
        u.setId(100L);
        u.setRole(Role.ADMIN);
        return u;
    }

    private User investigatorUser() {
        User u = new User();
        u.setId(200L);
        u.setRole(Role.INVESTIGATOR);
        return u;
    }

    private User reporterUser() {
        User u = new User();
        u.setId(300L);
        u.setRole(Role.REPORTER);
        return u;
    }

    private CommentCreateDTO dto(String message, boolean internalNote) {
        CommentCreateDTO dto = new CommentCreateDTO();
        dto.setMessage(message);
        dto.setInternalNote(internalNote);
        return dto;
    }

    @Nested
    @DisplayName("addComment tests")
    class AddCommentTests {

        @Test
        @DisplayName("Admin can add a public comment")
        void admin_canAddPublicComment() {
            // Arrange
            User admin = adminUser();
            Ticket ticket = openTicketWithReporter(reporterUser());
            CommentViewDTO expected = new CommentViewDTO();

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketCommentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(ticketCommentMapper.entityToViewDTO(any())).thenReturn(expected);

            // Act
            CommentViewDTO result = ticketCommentService.addComment(1L, dto("Hello", false), admin, null);

            // Assert
            assertSame(expected, result);
            verify(ticketCommentRepository).save(any());
        }

        @Test
        @DisplayName("Admin can add an internal note")
        void admin_canAddInternalNote() {
            // Arrange
            User admin = adminUser();
            Ticket ticket = openTicketWithReporter(reporterUser());

            // Act
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketCommentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(ticketCommentMapper.entityToViewDTO(any())).thenReturn(new CommentViewDTO());

            // Assert
            assertDoesNotThrow(() ->
                    ticketCommentService.addComment(1L, dto("Internal", true), admin, null));
        }

        @Test
        @DisplayName("Assigned investigator can add a comment")
        void assignedInvestigator_canAddComment() {
            // Arrange
            User investigator = investigatorUser();
            Ticket ticket = openTicketWithReporter(reporterUser());
            ticket.setInvestigator(investigator);

            // Act
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketCommentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(ticketCommentMapper.entityToViewDTO(any())).thenReturn(new CommentViewDTO());

            // Assert
            assertDoesNotThrow(() ->
                    ticketCommentService.addComment(1L, dto("Note", false), investigator, null));
        }

        @Test
        @DisplayName("Assigned investigator can add an internal note")
        void assignedInvestigator_canAddInternalNote() {
            // Arrange
            User investigator = investigatorUser();
            Ticket ticket = openTicketWithReporter(reporterUser());
            ticket.setInvestigator(investigator);

            // Act
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketCommentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(ticketCommentMapper.entityToViewDTO(any())).thenReturn(new CommentViewDTO());

            // Assert
            assertDoesNotThrow(() ->
                    ticketCommentService.addComment(1L, dto("Internal", true), investigator, null));
        }

    }

}