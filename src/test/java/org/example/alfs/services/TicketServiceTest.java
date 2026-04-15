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
    @DisplayName("getTicketByToken tests")
    class GetTicketByTokenTests {

        @Test
        @DisplayName("Valid token should return ticket")
        void getTicketByToken_shouldReturnTicket() {
            // Arrange
            Ticket ticket = new Ticket();
            String token = "valid-token";

            when(ticketRepository.findByReporterToken(token)).thenReturn(Optional.of(ticket));
            when(ticketMapper.entityToViewDTO(any())).thenReturn(new TicketViewDTO());

            // Act + Assert
            assertDoesNotThrow(() -> ticketService.getTicketByToken(token));
            verify(ticketMapper).entityToViewDTO(ticket);
        }

        @Test
        @DisplayName("Invalid token should throw Not found")
        void getTicketByToken_invalidToken_shouldThrowNotFound() {
            // Arrange
            when(ticketRepository.findByReporterToken(any())).thenReturn(Optional.empty());

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.getTicketByToken("invalid-token"));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getTicketById tests")
    class GetTicketByIdTests {

        @Test
        @DisplayName("getTicketById should return ticket when found and access is granted")
        void getTicketById_shouldReturnTicket_whenFound() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();
            TicketViewDTO expected = new TicketViewDTO();

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketMapper.entityToViewDTO(ticket)).thenReturn(expected);

            // Act
            TicketViewDTO result = ticketService.getTicketById(1L);

            // Assert
            assertSame(expected, result);
            verify(ticketMapper).entityToViewDTO(ticket);
        }

        @Test
        @DisplayName("getTicketById should throw Not Found when ticket does not exist")
        void getTicketById_shouldThrow_whenNotFound() {
            // Arrange
            when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.getTicketById(1L));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getMyTickets tests")
    class GetMyTicketsTests {

        @Test
        @DisplayName("Should return tickets for the current reporter")
        void getMyTickets_shouldReturnTicketsForCurrentUser() {
            // Arrange
            User reporter = reporterUser();
            Ticket ticket = openTicket();
            TicketViewDTO expected = new TicketViewDTO();

            when(securityUtils.getCurrentUser()).thenReturn(reporter);
            when(ticketRepository.findByReporterId(reporter.getId())).thenReturn(List.of(ticket));
            when(ticketMapper.entityToViewDTO(ticket)).thenReturn(expected);

            // Act
            List<TicketViewDTO> result = ticketService.getMyTickets();

            // Assert
            assertEquals(1, result.size());
            assertSame(expected, result.getFirst());
            verify(ticketRepository).findByReporterId(reporter.getId());
        }

        @Test
        @DisplayName("Should return empty list when reporter has no tickets")
        void getMyTickets_shouldReturnEmptyList_whenNoTickets() {
            // Arrange
            User reporter = reporterUser();

            when(securityUtils.getCurrentUser()).thenReturn(reporter);
            when(ticketRepository.findByReporterId(reporter.getId())).thenReturn(List.of());

            // Act
            List<TicketViewDTO> result = ticketService.getMyTickets();

            // Assert
            assertTrue(result.isEmpty());
            verify(ticketMapper, never()).entityToViewDTO(any());
        }
    }

    @Nested
    @DisplayName("getMyAssignedTickets tests")
    class GetMyAssignedTickets {

        @Test
        @DisplayName("Should return tickets for the current investigator")
        void getMyAssignedTickets_shouldReturnTicketsForCurrentUser() {
            // Arrange
            User investigator = investigatorUser();
            Ticket ticket = openTicket();
            TicketViewDTO expected = new TicketViewDTO();

            when(securityUtils.getCurrentUser()).thenReturn(investigator);
            when(ticketRepository.findByInvestigatorId(investigator.getId())).thenReturn(List.of(ticket));
            when(ticketMapper.entityToViewDTO(ticket)).thenReturn(expected);

            // Act
            List<TicketViewDTO> result = ticketService.getMyAssignedTickets();

            // Assert
            assertEquals(1, result.size());
            assertSame(expected, result.getFirst());
            verify(ticketRepository).findByInvestigatorId(investigator.getId());
        }

        @Test
        @DisplayName("Should return empty list when investigator has no tickets")
        void getMyAssignedTickets_shouldReturnEmptyList_whenNoTickets() {
            // Arrange
            User investigator = investigatorUser();

            when(securityUtils.getCurrentUser()).thenReturn(investigator);
            when(ticketRepository.findByInvestigatorId(investigator.getId())).thenReturn(List.of());

            // Act
            List<TicketViewDTO> result = ticketService.getMyAssignedTickets();

            // Assert
            assertTrue(result.isEmpty());
            verify(ticketMapper, never()).entityToViewDTO(any());
        }
    }

    @Nested
    @DisplayName("getTicketsByStatus tests")
    class GetTicketsByStatusTests {

        @Test
        @DisplayName("Admin can get tickets by status")
        void admin_canGetTicketsByStatus() {
            // Arrange
            when(securityUtils.getCurrentUser()).thenReturn(adminUser());
            when(ticketRepository.findByStatus(TicketStatus.OPEN)).thenReturn(List.of());

            // Act + Assert
            assertDoesNotThrow(() -> ticketService.getTicketsByStatus(TicketStatus.OPEN));
        }

        @Test
        @DisplayName("Non-admin cannot get tickets by status")
        void nonAdmin_cannotGetTicketsByStatus() {
            // Arrange
            when(securityUtils.getCurrentUser()).thenReturn(investigatorUser());

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.getTicketsByStatus(TicketStatus.OPEN));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getTicketsByStatusAndInvestigator tests")
    class GetTicketsByStatusAndInvestigatorTests {

        @Test
        @DisplayName("Admin can filter any investigator's tickets")
        void admin_canFilterAnyInvestigator() {
            // Arrange
            User admin = adminUser();

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findByStatusAndInvestigatorId(any(), any())).thenReturn(List.of());

            // Act + Assert
            assertDoesNotThrow(() -> ticketService.getTicketsByStatusAndInvestigator(TicketStatus.OPEN, 200L));
        }

        @Test
        @DisplayName("Investigator can filter their own tickets")
        void investigator_canFilterOwnTickets() {
            // Arrange
            User investigator = investigatorUser();

            when(securityUtils.getCurrentUser()).thenReturn(investigator);
            when(ticketRepository.findByStatusAndInvestigatorId(any(), any())).thenReturn(List.of());

            // Act + Assert
            assertDoesNotThrow(() -> ticketService.getTicketsByStatusAndInvestigator(TicketStatus.IN_PROGRESS, 200L));
        }

        @Test
        @DisplayName("Investigator cannot filter other investigator's tickets")
        void investigator_cannotFilterOthersTickets() {
            // Arrange
            User investigator = investigatorUser();

            when(securityUtils.getCurrentUser()).thenReturn(investigator);

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.getTicketsByStatusAndInvestigator(TicketStatus.IN_PROGRESS, 201L));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        @Test
        @DisplayName("Reporter should be forbidden from filtering by investigator ID")
        void reporter_cannotFilterTickets_byInvestigatorId() {
            // Arrange
            User reporter = reporterUser();

            when(securityUtils.getCurrentUser()).thenReturn(reporter);

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.getTicketsByStatusAndInvestigator(TicketStatus.IN_PROGRESS, 200L));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("checkAccess tests")
    class CheckAccessTests {

        @Test
        @DisplayName("Admin should always have access")
        void admin_shouldHaveAccess() {
            // Arrange
            Ticket ticket = openTicket();
            User admin = adminUser();

            when(securityUtils.getCurrentUser()).thenReturn(admin);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketMapper.entityToViewDTO(any())).thenReturn(new TicketViewDTO());

            // Act + Assert
            assertDoesNotThrow(() -> ticketService.getTicketById(1L));
        }

        @Test
        @DisplayName("Assigned investigator should have access")
        void investigator_shouldHaveAccessIfAssigned() {
            // Arrange
            Ticket ticket = openTicket();
            User investigator = investigatorUser();
            ticket.setInvestigator(investigator);

            when(securityUtils.getCurrentUser()).thenReturn(investigator);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketMapper.entityToViewDTO(any())).thenReturn(new TicketViewDTO());

            // Act + Assert
            assertDoesNotThrow(() -> ticketService.getTicketById(1L));
        }

        @Test
        @DisplayName("Unassigned investigator should not have access")
        void unassignedInvestigator_shouldThrowForbidden() {
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
                    ticketService.getTicketById(1L));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        @Test
        @DisplayName("Reporter who owns the ticket should have access")
        void owningReporter_shouldHaveAccess() {
            // Arrange
            Ticket ticket = openTicket();
            User reporter = reporterUser();
            ticket.setReporter(reporter);

            when(securityUtils.getCurrentUser()).thenReturn(reporter);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
            when(ticketMapper.entityToViewDTO(any())).thenReturn(new TicketViewDTO());

            // Act + Assert
            assertDoesNotThrow(() -> ticketService.getTicketById(1L));
        }

        @Test
        @DisplayName("Reporter who does not own the ticket should not have access")
        void nonOwningReporter_shouldThrowForbidden() {
            // Arrange
            Ticket ticket = openTicket();
            User reporter = reporterUser();

            User otherReporter = new User();
            otherReporter.setId(301L);
            otherReporter.setRole(Role.REPORTER);
            ticket.setReporter(otherReporter);

            when(securityUtils.getCurrentUser()).thenReturn(reporter);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.getTicketById(1L));

            // Assert
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        @Test
        @DisplayName("Investigator with null investigator set on ticket should be forbidden")
        void investigator_nullInvestigatorOnTicket_shouldThrowForbidden() {
            Ticket ticket = openTicket();
            User investigator = investigatorUser();
            ticket.setInvestigator(null);

            when(securityUtils.getCurrentUser()).thenReturn(investigator);
            when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.getTicketById(1L));

            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("requireCurrentUser tests")
    class RequireCurrentUserTests {

        @Test
        @DisplayName("No authenticated user in security context should throw Unauthorized")
        void noAuthenticatedUser_shouldThrowUnauthorized() {
            // Arrange
            when(securityUtils.getCurrentUser())
                    .thenThrow(new RuntimeException("No authenticated user in security context"));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.createNewTicket(new TicketCreateDTO()));

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        }

        @Test
        @DisplayName("Authenticated user not found in database should throw Unauthorized")
        void userNotFoundInDatabase_shouldThrowUnauthorized() {
            // Arrange
            when(securityUtils.getCurrentUser())
                    .thenThrow(new RuntimeException("Authenticated user not found in database"));

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                    ticketService.createNewTicket(new TicketCreateDTO()));

            // Assert
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("updateTicketStatus tests")
    class UpdateTicketStatusTests {

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
    class AssignInvestigatorTests {

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
    class UnassignInvestigatorTests {

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