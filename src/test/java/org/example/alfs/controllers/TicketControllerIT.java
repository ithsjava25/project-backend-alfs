package org.example.alfs.controllers;

import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.services.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("TicketController Integration Tests")
class TicketControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketService ticketService;

    @Nested
    @DisplayName("Anonymous Reporter")
    class AnonymousReporter {

        @Test
        @DisplayName("Anonymous reporter can access create form")
        void anonymousReporter_canAccessCreateForm() throws Exception {
            mockMvc.perform(get("/tickets/create"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("create"))
                    .andExpect(model().attributeExists("ticket"));
        }

        @Test
        @DisplayName("Anonymous reporter can view a created ticket with valid token")
        void anonymousReporter_validToken_returnsView() throws Exception {
            var dto = new TicketCreateDTO();
            dto.setTitle("Test");
            dto.setDescription("Test");

            var ticket = ticketService.createNewTicket(dto);

            mockMvc.perform(get("/tickets/token/" + ticket.getToken()))
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
            mockMvc.perform(get("/tickets/token/invalid-token"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?tokenError=true"));
        }
    }
}