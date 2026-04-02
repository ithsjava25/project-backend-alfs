package org.example.alfs.repositories;

import org.example.alfs.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    // Hämta alla bilagor i ett fall
    List<Attachment> findByTicketId(Long ticketId);

}
