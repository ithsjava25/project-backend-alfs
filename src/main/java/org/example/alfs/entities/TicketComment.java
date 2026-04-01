package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

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
    @GeneratedValue
    private Long id;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private String message;

    // Möjlighet att skriva internt meddelande (synligt för utredare/admin, inte för anmälare)
    @Column(nullable = false)
    private boolean isInternalNote = false;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @ManyToOne
    private Ticket ticket;

    @ManyToOne
    private User author;
}
