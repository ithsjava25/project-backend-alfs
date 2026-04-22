package org.example.alfs.controllers;

import org.example.alfs.repositories.AttachmentRepository;
import org.example.alfs.security.SecurityUtils;
import org.example.alfs.services.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AttachmentControllerUploadTest {

    private MockMvc mockMvc;

    private AttachmentService attachmentService;
    private AttachmentRepository attachmentRepository;
    private SecurityUtils securityUtils;

    @BeforeEach
    void setup() {
        attachmentService = mock(AttachmentService.class);
        attachmentRepository = mock(AttachmentRepository.class);
        securityUtils = mock(SecurityUtils.class);

        // For upload in dev we allow anonymous (controller handles null user gracefully)
        when(securityUtils.getCurrentUser()).thenThrow(new RuntimeException("No authenticated user in security context"));

        AttachmentController controller = new AttachmentController(
                attachmentService,
                attachmentRepository,
                securityUtils
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void upload_with_ticketId_zero_should_return_400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("ticketId", "0"))
                .andExpect(status().isBadRequest());

        verify(attachmentService, never()).uploadToTicket(any(), any(), any(), any());
    }

    @Test
    void upload_with_empty_file_should_return_400() throws Exception {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.txt", MediaType.TEXT_PLAIN_VALUE, new byte[0]
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(empty)
                        .param("ticketId", "1"))
                .andExpect(status().isBadRequest());

        verify(attachmentService, never()).uploadToTicket(any(), any(), any(), any());
    }

    @Test
    void upload_success_should_redirect_to_ticket_page_and_call_service() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("ticketId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tickets/1"));

        verify(attachmentService, times(1)).uploadToTicket(eq(1L), any(), isNull(), isNull());
    }
}
