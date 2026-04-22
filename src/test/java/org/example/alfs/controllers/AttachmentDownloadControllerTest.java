package org.example.alfs.controllers;

import org.example.alfs.config.S3Properties;
import org.example.alfs.entities.Attachment;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.example.alfs.repositories.AttachmentRepository;
import org.example.alfs.security.SecurityUtils;
import org.example.alfs.services.AuditService;
import org.example.alfs.services.AuthorizationService;
import org.example.alfs.services.storage.MinioStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AttachmentDownloadControllerTest {

    private MockMvc mockMvc;

    private AttachmentRepository attachmentRepository;
    private MinioStorageService storageService;
    private SecurityUtils securityUtils;
    private AuditService auditService;
    private AuthorizationService authorizationService;
    private S3Properties s3Properties;

    @BeforeEach
    void setup() {
        attachmentRepository = mock(AttachmentRepository.class);
        storageService = mock(MinioStorageService.class);
        securityUtils = mock(SecurityUtils.class);
        auditService = mock(AuditService.class);
        authorizationService = mock(AuthorizationService.class);
        s3Properties = mock(S3Properties.class);

        AttachmentDownloadController controller = new AttachmentDownloadController(
                attachmentRepository,
                storageService,
                securityUtils,
                auditService,
                authorizationService,
                s3Properties
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice()
                .build();
    }

    private static Attachment sampleAttachment() {
        Ticket t = new Ticket();
        User reporter = new User();
        reporter.setId(10L);
        reporter.setRole(Role.REPORTER);
        t.setReporter(reporter);

        Attachment a = new Attachment();
        a.setId(1L);
        a.setTicket(t);
        a.setFileName("test.pdf");
        a.setS3Key("some/key");
        return a;
    }

    private static User sampleUser() {
        User u = new User();
        u.setId(99L);
        u.setRole(Role.ADMIN);
        u.setUsername("admin");
        return u;
    }

    @Test
    void presign_ttl_exceeds_max_returns_400() throws Exception {
        // Arrange
        Attachment att = sampleAttachment();
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(att));
        when(securityUtils.getCurrentUser()).thenReturn(sampleUser());
        when(authorizationService.canAccessAttachment(any(User.class), any(Attachment.class))).thenReturn(true);

        // Configure max TTL to 60 seconds
        when(s3Properties.getPresignMaxTtlSeconds()).thenReturn(60);

        // Act & Assert
        mockMvc.perform(post("/api/files/1/presign")
                        .param("ttl", "120")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Verify no presign call attempted due to validation failure
        Mockito.verify(storageService, Mockito.never()).generatePresignedGetUrlWithContentDisposition(any(), any(Integer.class), any());
    }
}
