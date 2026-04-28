package org.example.alfs.repositories;

import java.util.List;
import org.example.alfs.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

  // Hämta alla bilagor i ett fall
  List<Attachment> findByTicketId(Long ticketId);
}
