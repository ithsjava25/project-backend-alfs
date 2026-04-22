package org.example.alfs.services;

import org.example.alfs.entities.AuditLog;
import org.example.alfs.entities.Ticket;
import org.example.alfs.entities.User;
import org.example.alfs.enums.AuditAction;
import org.example.alfs.repositories.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }


    // new with user
    public void log(AuditAction action, String fieldName, String oldValue, String newValue, Ticket ticket, User user) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setTicket(ticket);
        log.setUser(user); // 🔥 VIKTIGT
        auditLogRepository.save(log);
    }

    // keeping old for safety
    public void log(AuditAction action, String fieldName, String oldValue, String newValue, Ticket ticket) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setTicket(ticket);
        // createdAt sätts automatiskt via @PrePersist i AuditLog
        auditLogRepository.save(log);
    }
}
