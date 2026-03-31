package org.example.alfs.repositories;

import org.example.alfs.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Hämta logghistoriken i ett fall, nyast först
    List<AuditLog> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

}
