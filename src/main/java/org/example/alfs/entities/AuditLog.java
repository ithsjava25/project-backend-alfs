package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.alfs.enums.AuditAction;

import java.time.LocalDateTime;

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
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    private String fieldName;

    @Column(length = 4000)
    private String oldValue;

    @Column(length = 4000)
    private String newValue;

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
}
