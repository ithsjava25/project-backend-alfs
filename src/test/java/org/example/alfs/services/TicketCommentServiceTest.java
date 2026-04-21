package org.example.alfs.services;

import org.example.alfs.dto.comment.CommentCreateDTO;
import org.example.alfs.dto.comment.CommentViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.TicketComment;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    private Ticket anonymousTicket(String token) {
        Ticket t = new Ticket();
        t.setId(400L);
        t.setStatus(TicketStatus.OPEN);
        t.setReporterToken(token);
        return t;
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

        @Test
        @DisplayName("Unassigned investigator should be forbidden")
        void unassignedInvestigator_shouldThrowForbidden() {
            // Arrange
            User investigator = investigatorUser();
            Ticket ticket = openTicketWithReporter(reporterUser());

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketCommentService.addComment(1L, dto("Note", false), investigator, null));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            verify(ticketCommentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Reporter who owns the ticket can add a comment")
        void owningReporter_canAddComment() {
            // Arrange
            User reporter = reporterUser();
            Ticket ticket = openTicketWithReporter(reporter);

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketCommentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(ticketCommentMapper.entityToViewDTO(any())).thenReturn(new CommentViewDTO());

            // Act + Assert
            assertDoesNotThrow(() ->
                    ticketCommentService.addComment(1L, dto("My comment", false), reporter, null));
        }

        @Test
        @DisplayName("Reporter cannot create an internal note")
        void reporter_cannotCreateInternalNote() {
            // Arrange
            User reporter = reporterUser();
            Ticket ticket = openTicketWithReporter(reporter);

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketCommentService.addComment(1L, dto("Secret", true), reporter, null));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            verify(ticketCommentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Reporter who does not own the ticket should be forbidden")
        void nonOwningReporter_shouldThrowForbidden() {
            // Arrange
            User reporter = reporterUser();

            User otherReporter = new User();
            otherReporter.setId(301L);
            otherReporter.setRole(Role.REPORTER);

            Ticket ticket = openTicketWithReporter(otherReporter);

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketCommentService.addComment(1L, dto("Note", false), reporter, null));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            verify(ticketCommentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Anonymous user with valid token can add a comment")
        void anonymous_withValidToken_canAddComment() {
            // Arrange
            String token = "valid-token";
            Ticket ticket = anonymousTicket(token);

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketCommentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(ticketCommentMapper.entityToViewDTO(any())).thenReturn(new CommentViewDTO());

            // Act + Assert
            assertDoesNotThrow(() ->
                    ticketCommentService.addComment(1L, dto("Anonymous comment", false), null, token));
        }

        @Test
        @DisplayName("Anonymous user cannot create an internal note")
        void anonymous_cannotCreateInternalNote() {
            // Arrange
            String token = "valid-token";
            Ticket ticket = anonymousTicket(token);

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketCommentService.addComment(1L, dto("Secret", true), null, token));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            verify(ticketCommentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Anonymous user with wrong token should be forbidden")
        void anonymous_withWrongToken_shouldThrowForbidden() {
            // Arrange
            Ticket ticket = anonymousTicket("correct-token");

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketCommentService.addComment(1L, dto("Note", false), null, "wrong-token"));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            verify(ticketCommentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Anonymous user with no token should be forbidden")
        void anonymous_withNoToken_shouldThrowForbidden() {
            // Arrange
            Ticket ticket = anonymousTicket("valid-token");

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketCommentService.addComment(1L, dto("Note", false), null, null));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        @Test
        @DisplayName("Should throw Not Found when ticket does not exist")
        void addComment_ticketNotFound_shouldThrowNotFound() {
            // Arrange
            when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketCommentService.addComment(1L, dto("Hello", false), adminUser(), null));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            verify(ticketCommentRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("getComments tests")
    class GetCommentsTests {

        @Test
        @DisplayName("Admin sees all comments including internal notes")
        void admin_seesAllComments() {
            // Arrange
            User admin = adminUser();
            Ticket ticket = openTicketWithReporter(reporterUser());

            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

            // Act
            ticketCommentService.getComments(1L, admin, null);

            // Assert
            verify(ticketCommentRepository).findByTicketIdOrderByCreatedAtAsc(1L);
            verify(ticketCommentRepository, never()).findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(any());
        }
    }

    @Test
    @DisplayName("Assigned investigator sees all comments including internal notes")
    void assignedInvestigator_seesAllComments() {
        // Arrange
        User investigator = investigatorUser();
        Ticket ticket = openTicketWithReporter(reporterUser());
        ticket.setInvestigator(investigator);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        // Act
        ticketCommentService.getComments(1L, investigator, null);

        // Assert
        verify(ticketCommentRepository).findByTicketIdOrderByCreatedAtAsc(1L);
    }

    @Test
    @DisplayName("Reporter only sees public comments")
    void reporter_seesOnlyPublicComments() {
        // Arrange
        User reporter = reporterUser();
        Ticket ticket = openTicketWithReporter(reporter);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketCommentRepository.findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        // Act
        ticketCommentService.getComments(1L, reporter, null);

        // Assert
        verify(ticketCommentRepository).findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(1L);
        verify(ticketCommentRepository, never()).findByTicketIdOrderByCreatedAtAsc(any());
    }

    @Test
    @DisplayName("Anonymous reporter with valid token only sees public comments")
    void anonymous_withValidToken_seesOnlyPublicComments() {
        // Arrange
        String token = "valid-token";
        Ticket ticket = anonymousTicket(token);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketCommentRepository.findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        // Act
        ticketCommentService.getComments(1L, null, token);

        // Assert
        verify(ticketCommentRepository).findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(1L);
        verify(ticketCommentRepository, never()).findByTicketIdOrderByCreatedAtAsc(any());
    }

    @Test
    @DisplayName("Anonymous reporter with wrong token should be forbidden")
    void anonymous_withWrongToken_shouldThrowForbidden() {
        // Arrange
        Ticket ticket = anonymousTicket("correct-token");

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        // Act
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                ticketCommentService.getComments(1L, null, "wrong-token"));

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    @DisplayName("Should throw Not Found when ticket does not exist")
    void getComments_ticketNotFound_shouldThrowNotFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                ticketCommentService.getComments(1L, adminUser(), null));

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    @DisplayName("Returned comments are mapped correctly")
    void getComments_returnsMappedDTOs() {
        // Arrange
        User admin = adminUser();
        Ticket ticket = openTicketWithReporter(reporterUser());
        TicketComment comment = new TicketComment();
        CommentViewDTO expected = new CommentViewDTO();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(comment));
        when(ticketCommentMapper.entityToViewDTO(comment)).thenReturn(expected);

        // Act
        List<CommentViewDTO> result = ticketCommentService.getComments(1L, admin, null);

        // Assert
        assertEquals(1, result.size());
        assertSame(expected, result.getFirst());
    }

}