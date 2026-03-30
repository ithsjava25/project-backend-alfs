package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

/*
Represents comment on a ticket.
The comments are written by a User.
 */
@Entity
@Table(name="ticket_comment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketComment {

    @Id
    @GeneratedValue
    private Long id;

    private String message;

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
