package org.example.alfs.services;

import org.example.alfs.entities.Attachment;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorizationServiceTest {

    private AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService();
    }

    private static User userWithRole(Role role, Long id) {
        User u = new User();
        u.setRole(role);
        u.setId(id);
        return u;
    }

    private static Attachment attachmentWithReporter(Long reporterId) {
        Ticket t = new Ticket();
        if (reporterId != null) {
            User reporter = new User();
            reporter.setId(reporterId);
            reporter.setRole(Role.REPORTER);
            t.setReporter(reporter);
        } else {
            t.setReporter(null);
        }
        Attachment a = new Attachment();
        a.setTicket(t);
        return a;
    }

    @Test
    void admin_should_have_access() {
        User admin = userWithRole(Role.ADMIN, 1L);
        Attachment att = attachmentWithReporter(2L);
        assertTrue(authorizationService.canAccessAttachment(admin, att));
    }

    @Test
    void investigator_should_have_access() {
        User inv = userWithRole(Role.INVESTIGATOR, 10L);
        Attachment att = attachmentWithReporter(2L);
        assertTrue(authorizationService.canAccessAttachment(inv, att));
    }

    @Test
    void reporter_owner_should_have_access() {
        User rep = userWithRole(Role.REPORTER, 5L);
        Attachment att = attachmentWithReporter(5L);
        assertTrue(authorizationService.canAccessAttachment(rep, att));
    }

    @Test
    void reporter_non_owner_should_be_denied() {
        User rep = userWithRole(Role.REPORTER, 5L);
        Attachment att = attachmentWithReporter(6L);
        assertFalse(authorizationService.canAccessAttachment(rep, att));
    }

    @Test
    void reporter_when_ticket_has_no_reporter_should_be_denied() {
        User rep = userWithRole(Role.REPORTER, 5L);
        Attachment att = attachmentWithReporter(null);
        assertFalse(authorizationService.canAccessAttachment(rep, att));
    }

    @Test
    void null_user_should_be_denied() {
        Attachment att = attachmentWithReporter(1L);
        assertFalse(authorizationService.canAccessAttachment(null, att));
    }

    @Test
    void null_attachment_should_be_denied() {
        User admin = userWithRole(Role.ADMIN, 1L);
        assertFalse(authorizationService.canAccessAttachment(admin, null));
    }
}
