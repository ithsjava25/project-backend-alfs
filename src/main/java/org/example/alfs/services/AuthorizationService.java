package org.example.alfs.services;

import org.example.alfs.entities.Attachment;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    /**
     * Kontrollerar om användaren får åtkomst till en bilaga kopplad till ett ärende.
     * Regler (vecka 2):
     * - ADMIN och INVESTIGATOR: alltid tillåtna
     * - REPORTER: endast om användaren är reporter för ticketen
     */
    public boolean canAccessAttachment(User user, Attachment attachment) {
        if (user == null || attachment == null) return false;

        Role role = user.getRole();
        if (role == Role.ADMIN || role == Role.INVESTIGATOR) return true;

        if (role == Role.REPORTER) {
            var ticket = attachment.getTicket();
            return ticket != null
                    && ticket.getReporter() != null
                    && ticket.getReporter().getId().equals(user.getId());
        }

        return false;
    }
}
