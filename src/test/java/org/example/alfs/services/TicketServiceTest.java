package org.example.alfs.services;

import org.example.alfs.dto.ticket.TicketViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.example.alfs.enums.TicketStatus;
import org.example.alfs.mapper.TicketMapper;
import org.example.alfs.repositories.TicketRepository;
import org.example.alfs.repositories.UserRepository;
import org.example.alfs.security.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TicketService Test")
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    TicketRepository ticketRepository;
    @Mock
    TicketMapper ticketMapper;
    @Mock
    UserRepository userRepository;
    @Mock
    SecurityUtils securityUtils;

    @InjectMocks
    TicketService ticketService;

    private Ticket openTicket() {
        Ticket t = new Ticket();
        t.setId(1L);
        t.setStatus(TicketStatus.OPEN);
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

    @Nested
    @DisplayName("updateTicketStatus tests")
    class updateTicketStatusTests {

        @Test
        @DisplayName("Valid transition should succeed")
        void validTransition_shouldSucceed() {
            // Arrange
            Ticket ticket = openTicket();
            User investigator = investigatorUser();
            ticket.setInvestigator(investigator);

            when(securityUtils.getCurrentUser()).thenReturn(investigator);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any())).thenReturn(ticket);
            when(ticketMapper.entityToViewDTO(any())).thenReturn(new TicketViewDTO());

            // Act
            ticketService.updateTicketStatus(1L, TicketStatus.IN_PROGRESS);

            // Assert
            assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("Invalid transition should throw Bad Request")
        void invalidTransition_shouldThrowBadRequest() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.updateTicketStatus(1L, TicketStatus.CLOSED));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Reporter should not be able to update status")
        void reporterStatusUpdate_shouldThrowForbidden() {
            // Arrange
            Ticket ticket = openTicket();
            User reporter = reporterUser();

            when(securityUtils.getCurrentUser()).thenReturn(reporter);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.updateTicketStatus(1L, TicketStatus.CLOSED));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Same status should return without changes")
        void sameStatus_shouldReturn() {
            //Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketMapper.entityToViewDTO(any())).thenReturn(new TicketViewDTO());

            // Act
            TicketViewDTO result = ticketService.updateTicketStatus(1L, TicketStatus.OPEN);

            // Assert
            assertNotNull(result);
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Status change without investigator should throw BadRequest")
        void statusChange_withoutInvestigator_shouldThrowBadRequest() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.updateTicketStatus(1L, TicketStatus.IN_PROGRESS));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            verify(ticketRepository, never()).save(any());
        }

    }
}