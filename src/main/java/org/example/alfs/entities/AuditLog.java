package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
Represents audit log for Ticket.
Logs all events such as status change, assignment and comments.
Should be written automatically.
 */
@Entity
@Table(name="audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue
    private Long id;

    private String action;

    private String fieldName;

    private String oldValue;

    private String newValue;

    private LocalDateTime createdAt;
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @ManyToOne
    private Ticket ticket;

    @ManyToOne
    private User user;
}
