package org.example.alfs.services;

import org.example.alfs.entities.AuditLog;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.AuditAction;
import org.example.alfs.enums.Role;
import org.example.alfs.repositories.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditServiceDataJpaTest {

    private AuditLogRepository auditLogRepository;
    private AuditService auditService;

    @BeforeEach
    void setup() {
        auditLogRepository = mock(AuditLogRepository.class);
        // echo back the entity when save is called
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));
        auditService = new AuditService(auditLogRepository);
    }

    @Test
    void log_should_populate_and_call_repository_save() {
        // Arrange
        User reporter = new User();
        reporter.setId(5L);
        reporter.setUsername("reporter1");
        reporter.setPasswordHash("x");
        reporter.setRole(Role.REPORTER);

        Ticket t = new Ticket();
        t.setId(11L);
        t.setTitle("Test case");
        t.setDescription("Desc");
        t.setReporter(reporter);

        // Act
        auditService.log(AuditAction.FILE_PRESIGNED,
                "attachment",
                null,
                "presign issued: attachmentId=1, ttl=120",
                t,
                reporter);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
}
