package org.example.alfs.repositories;

import java.util.List;
import org.example.alfs.entities.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

  // Ladda alla kommentarer i ett fall, äldst först
  List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

  // Ladda interna meddelanden för utredare/admins, äldst först
  List<TicketComment> findByTicketIdAndInternalNoteOrderByCreatedAtAsc(
      Long ticketId, boolean isInternalNote);

  // Ladda kommentarer men utelämna interna meddelanden, äldst först
  List<TicketComment> findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(Long ticketId);
}
