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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Test
    @DisplayName("createNewTicket should set reporter as user")
    void createNewTicket_shouldSetReporterAsUser() {
        // Arrange
        TicketCreateDTO dto = new TicketCreateDTO();
        dto.setTitle("Test Ticket");
        dto.setDescription("This is a test ticket");
        User reporter = reporterUser();

        when(securityUtils.getCurrentUser()).thenReturn(reporter);
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ticketMapper.entityToViewDTO(any())).thenReturn(new TicketViewDTO());

        // Act
        ticketService.createNewTicket(dto);

        // Assert
        verify(ticketRepository).save(argThat(ticket ->
                ticket.getReporter().equals(reporter) &&
                        ticket.getTitle().equals("Test Ticket") &&
                        ticket.getDescription().equals("This is a test ticket")
        ));
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
        @DisplayName("Status change without investigator should throw Bad Request")
        void statusChange_withoutInvestigator_shouldThrowBadRequest() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();
            ticket.setInvestigator(null);

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.updateTicketStatus(1L, TicketStatus.IN_PROGRESS));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Investigator not assigned should be forbidden")
        void investigator_notAssigned_shouldBeForbidden() {
            // Arrange
            Ticket ticket = openTicket();
            User investigator = investigatorUser();

            User otherInvestigator = new User();
            otherInvestigator.setId(201L);
            otherInvestigator.setRole(Role.INVESTIGATOR);
            ticket.setInvestigator(otherInvestigator);

            when(securityUtils.getCurrentUser()).thenReturn(investigator);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.updateTicketStatus(1L, TicketStatus.IN_PROGRESS));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
            verify(ticketRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("assignInvestigator tests")
    class assignInvestigatorTests {

        @Test
        @DisplayName("Assigning investigator should succeed")
        void assignInvestigator_shouldSucceed() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();
            User investigator = investigatorUser();

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(investigator.getId())).thenReturn(Optional.of(investigator));
            when(ticketRepository.save(any())).thenReturn(ticket);
            when(ticketMapper.entityToViewDTO(any())).thenReturn(new TicketViewDTO());

            // Act
            ticketService.assignInvestigator(1L, investigator.getId());

            // Assert
            assertEquals(investigator, ticket.getInvestigator());
            assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("Assigning investigator should throw Bad Request when investigatorId is null")
        void assignInvestigator_shouldThrowBadRequest_whenInvestigatorIsNull() {
            // Arrange + Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.assignInvestigator(1L, null));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Assigning investigator should throw Conflict when ticket is already assigned")
        void assignInvestigator_shouldThrowConflict_whenTicketAlreadyAssigned() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();
            User investigator = investigatorUser();
            ticket.setInvestigator(investigator);

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.assignInvestigator(1L, investigator.getId()));

            // Assert
            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Assigning investigator should throw Bad Request when ticket is not open")
        void assignInvestigator_shouldThrowBadRequest_whenIsNotOpen() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();
            User investigator = investigatorUser();
            ticket.setStatus(TicketStatus.CLOSED);

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.assignInvestigator(1L, investigator.getId()));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Assigning investigator should throw Bad Request when user is not investigator")
        void assignInvestigator_shouldThrowBadRequest_whenUserIsNotInvestigator() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();
            User reporter = reporterUser();

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(userRepository.findById(reporter.getId())).thenReturn(Optional.of(reporter));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.assignInvestigator(1L, reporter.getId()));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            verify(ticketRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("unassignInvestigator tests")
    class unassignInvestigatorTests {

        @Test
        @DisplayName("Unassigning investigator should succeed")
        void unassignInvestigator_shouldSucceed() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();
            User investigator = investigatorUser();
            ticket.setInvestigator(investigator);
            ticket.setStatus(TicketStatus.IN_PROGRESS);

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(any())).thenReturn(ticket);
            when(ticketMapper.entityToViewDTO(any())).thenReturn(new TicketViewDTO());

            // Act
            ticketService.unassignInvestigator(1L);

            // Assert
            assertNull(ticket.getInvestigator());
            assertEquals(TicketStatus.OPEN, ticket.getStatus());
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("Unassigning investigator should throw Bad Request when no investigator is assigned")
        void unassignInvestigator_shouldThrowBadRequest_whenNoInvestigatorAssigned() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();
            ticket.setInvestigator(null);

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.unassignInvestigator(1L));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            verify(ticketRepository, never()).save(any());
        }

        @Test
        @DisplayName("Unassigning investigator should throw Bad Request when ticket is not in progress")
        void unassignInvestigator_shouldThrowBadRequest_whenTicketNotInProgress() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();
            User investigator = investigatorUser();
            ticket.setInvestigator(investigator);
            ticket.setStatus(TicketStatus.OPEN);

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.unassignInvestigator(1L));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            verify(ticketRepository, never()).save(any());
        }
    }
}