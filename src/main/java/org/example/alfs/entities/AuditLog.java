package org.example.alfs.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.example.alfs.enums.AuditAction;

/*
Represents audit log for Ticket.
Logs all events such as status change, assignment and comments.
Should be written automatically.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private AuditAction action;

  @Column(nullable = false)
  private String fieldName;

  @Column(length = 4000)
  private String oldValue;

  @Column(length = 4000)
  private String newValue;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    createdAt = LocalDateTime.now();
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ticket_id")
  private Ticket ticket;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  public String getFormattedCreatedAt() {
    if (createdAt == null) return "";
    return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
  }
}
