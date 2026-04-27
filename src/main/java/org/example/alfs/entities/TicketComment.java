package org.example.alfs.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/*
Represents comment on a ticket.
The comments are written by a User.
 */
@Entity
@Table(name = "ticket_comment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketComment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Basic(fetch = FetchType.LAZY)
  @Column(nullable = false, columnDefinition = "TEXT")
  private String message;

  // Möjlighet att skriva internt meddelande (synligt för utredare/admin, inte för anmälare)
  @Column(nullable = false)
  private boolean internalNote = false;

  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    createdAt = LocalDateTime.now();
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ticket_id", nullable = false)
  private Ticket ticket;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = true)
  private User author;
}
