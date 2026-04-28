package org.example.alfs.repositories;

import java.util.List;
import org.example.alfs.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

  // Hämta logghistoriken i ett fall, nyast först
  List<AuditLog> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
