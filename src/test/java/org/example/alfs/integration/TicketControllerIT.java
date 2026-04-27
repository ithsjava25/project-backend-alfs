package org.example.alfs.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.example.alfs.repositories.TicketRepository;
import org.example.alfs.repositories.UserRepository;
import org.example.alfs.services.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("TicketController Integration Tests")
class TicketControllerIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private TicketService ticketService;
  @Autowired private UserRepository userRepository;
  @Autowired private TicketRepository ticketRepository;

  private Long ticketId;
  private User admin;
  private User investigator;
  private User reporter;

  @BeforeEach
  void setUp() {
    admin = new User();
    admin.setRole(Role.ADMIN);
    admin.setUsername("admin");
    admin.setPasswordHash("hashed-password");
    admin = userRepository.save(admin);

    investigator = new User();
    investigator.setRole(Role.INVESTIGATOR);
    investigator.setUsername("investigator");
    investigator.setPasswordHash("hashed-password");
    investigator = userRepository.save(investigator);

    reporter = new User();
    reporter.setRole(Role.REPORTER);
    reporter.setUsername("reporter");
    reporter.setPasswordHash("hashed-password");
    reporter = userRepository.save(reporter);

    TicketCreateDTO dto = new TicketCreateDTO();
    dto.setTitle("Test");
    dto.setDescription("Test");
    ticketId = ticketService.createNewTicket(dto).getId();
  }

  @Nested
  @DisplayName("Anonymous Reporter")
  class AnonymousReporter {

    @Test
    @DisplayName("Anonymous reporter can access create form")
    void anonymousReporter_canAccessCreateForm() throws Exception {
      mockMvc
          .perform(get("/tickets/create"))
          .andExpect(status().isOk())
          .andExpect(view().name("create"))
          .andExpect(model().attributeExists("ticket"));
    }

    @Test
    @DisplayName("Anonymous reporter can view a created ticket with valid token")
    void anonymousReporter_validToken_returnsView() throws Exception {
      var ticket = ticketRepository.findById(ticketId).orElseThrow();

      mockMvc
          .perform(get("/tickets/token/" + ticket.getReporterToken()))
          .andExpect(status().isOk())
          .andExpect(view().name("view"))
          .andExpect(model().attributeExists("ticket"))
          .andExpect(model().attributeExists("comments"))
          .andExpect(model().attributeExists("attachments"))
          .andExpect(model().attributeExists("auditLogs"))
          .andExpect(model().attributeExists("investigators"))
          .andExpect(model().attributeExists("accessToken"));
    }

    @Test
    @DisplayName("Anonymous reporter is redirected when token is invalid")
    void anonymousReporter_invalidToken_redirectsToLogin() throws Exception {
      mockMvc
          .perform(get("/tickets/token/invalid-token"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login?tokenError=true"));
    }

    @Test
    @DisplayName("Anonymous reporter submits valid ticket and is redirected to ticket-created")
    void anonymousReporter_validPost_redirectsToTicketCreated() throws Exception {
      mockMvc
          .perform(
              post("/tickets/create")
                  .param("title", "Test title")
                  .param("description", "Test description")
                  .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrlPattern("/tickets/ticket-created?token=*"));
    }

    @Test
    @DisplayName("Anonymous reporter submits blank form and sees create page again")
    void anonymousReporter_blankPost_returnsCreateForm() throws Exception {
      mockMvc
          .perform(post("/tickets/create").param("title", "").param("description", "").with(csrf()))
          .andExpect(status().isOk())
          .andExpect(view().name("create"))
          .andExpect(model().attributeHasFieldErrors("ticket", "title", "description"));
    }
  }

  @Nested
  @DisplayName("Authenticated reporter")
  class AuthenticatedReporter {

    @Test
    @WithMockUser(username = "reporter", roles = "REPORTER")
    @DisplayName("Reporter can view their own tickets")
    void reporter_canViewOwnTickets() throws Exception {
      mockMvc
          .perform(get("/tickets/my"))
          .andExpect(status().isOk())
          .andExpect(view().name("my-tickets"))
          .andExpect(model().attributeExists("tickets"));
    }
  }

  @Nested
  @DisplayName("Authenticated investigator")
  class AuthenticatedInvestigator {

    @Test
    @WithMockUser(username = "investigator", roles = "INVESTIGATOR")
    @DisplayName("Investigator can view their assigned tickets")
    void investigator_canViewAssignedTickets() throws Exception {
      mockMvc
          .perform(get("/tickets/assigned"))
          .andExpect(status().isOk())
          .andExpect(view().name("assigned-tickets"))
          .andExpect(model().attributeExists("tickets"));
    }
  }

  @Nested
  @DisplayName("Admin")
  class Admin {

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Admin can view ticket by id")
    void admin_canViewTicketById() throws Exception {
      mockMvc
          .perform(get("/tickets/{id}", ticketId))
          .andExpect(status().isOk())
          .andExpect(view().name("view"))
          .andExpect(model().attributeExists("ticket"))
          .andExpect(model().attributeExists("comments"))
          .andExpect(model().attributeExists("attachments"))
          .andExpect(model().attributeExists("auditLogs"))
          .andExpect(model().attributeExists("investigators"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Admin can assign an investigator to a ticket")
    void admin_canAssignInvestigator() throws Exception {
      mockMvc
          .perform(
              post("/tickets/{id}/assign", ticketId)
                  .param("investigatorId", investigator.getId().toString())
                  .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/tickets/" + ticketId));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Admin can update status on a ticket")
    void admin_canUpdateTicketStatus() throws Exception {
      ticketService.assignInvestigator(ticketId, investigator.getId());

      mockMvc
          .perform(post("/tickets/{id}/status", ticketId).param("status", "RESOLVED").with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/tickets/" + ticketId));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Admin cannot perform invalid status transition")
    void admin_cannotPerformInvalidStatusTransition() throws Exception {
      var ticket = ticketRepository.findById(ticketId).orElseThrow();
      assertEquals(org.example.alfs.enums.TicketStatus.OPEN, ticket.getStatus());

      mockMvc
          .perform(
              post("/tickets/{id}/status", ticketId)
                  .param("status", "RESOLVED") // Transition OPEN -> RESOLVED is invalid
                  .with(csrf()))
          .andExpect(status().isBadRequest());
    }
  }
}
