package org.example.alfs.repositories;

import org.example.alfs.entities.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    // Ladda alla kommentarer i ett fall, äldst först
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    // Ladda interna meddelanden för utredare/admins, äldst först
    List<TicketComment> findByTicketIdAndIsInternalNoteOrderByCreatedAtAsc(Long ticketId, boolean isInternalNote);
}
