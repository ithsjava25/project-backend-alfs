package org.example.alfs.services;

import org.example.alfs.entities.Attachment;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.example.alfs.repositories.AttachmentRepository;
import org.example.alfs.repositories.TicketRepository;
import org.example.alfs.security.SecurityUtils;
import org.example.alfs.services.storage.MinioStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private MinioStorageService storageService;
    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private AttachmentService attachmentService;

    private Ticket ticket;
    private User admin;
    private User investigator;
    private User reporter;
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(100L);
        admin.setRole(Role.ADMIN);

        investigator = new User();
        investigator.setId(200L);
        investigator.setRole(Role.INVESTIGATOR);

        reporter = new User();
        reporter.setId(300L);
        reporter.setRole(Role.REPORTER);

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setReporter(reporter);
        ticket.setInvestigator(investigator);
        ticket.setReporterToken("valid-token");

        file = mock(MultipartFile.class);
    }

    @Nested
    @DisplayName("uploadToTicket tests")
    class UploadToTicketTest {

        @Test
        @DisplayName("Authenticated user uploads to valid ticket")
        void authenticatedUser_shouldUploadSuccessfully_whenValidTicket() throws Exception {
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
            when(storageService.upload(file)).thenReturn("s3-key");
            when(file.getOriginalFilename()).thenReturn("report.pdf");

            Attachment result = attachmentService.uploadToTicket(10L, file, admin, null);

            assertThat(result.getFileName()).isEqualTo("report.pdf");
            assertThat(result.getS3Key()).isEqualTo("s3-key");
            verify(attachmentRepository).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Anonymous user uploads to valid ticket")
        void anonymousUser_shouldUploadSuccessfully_whenValidToken() throws Exception {
            when(ticketRepository.findByReporterToken("valid-token")).thenReturn(Optional.of(ticket));
            when(storageService.upload(file)).thenReturn("s3-key");
            when(file.getOriginalFilename()).thenReturn("evidence.pdf");

            Attachment result = attachmentService.uploadToTicket(10L, file, null, "valid-token");

            assertThat(result.getFileName()).isEqualTo("evidence.pdf");
            verify(attachmentRepository).save(any(Attachment.class));
        }

        @Test
        @DisplayName("Anonymous user with no token should throw Unauthorized")
        void anonymousUser_missingToken_throwsUnauthorized() {
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> attachmentService.uploadToTicket(10L, file, null, null));

            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verifyNoInteractions(storageService);
        }

        @Test
        @DisplayName("Anonymous user with blank token should throw Unauthorized")
        void anonymousUser_blankToken_throwsUnauthorized() {
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> attachmentService.uploadToTicket(10L, file, null, "   "));

            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verifyNoInteractions(storageService);
        }

        @Test
        @DisplayName("Ticket not found should throw Not Found")
        void ticketNotFound_throwsNotFound() {
            when(ticketRepository.findById(11L)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> attachmentService.uploadToTicket(11L, file, admin, null));

            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Null file name should fall back to default name")
        void nullFileName_fallsBackToDefaultName() throws Exception {
            when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
            when(storageService.upload(file)).thenReturn("s3-key");
            when(file.getOriginalFilename()).thenReturn(null);

            Attachment result = attachmentService.uploadToTicket(10L, file, admin, null);

            assertThat(result.getFileName()).isEqualTo("file");
        }
    }
}