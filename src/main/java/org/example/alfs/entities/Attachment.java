package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
Represent a file uploaded with Ticket.
Stores metadata about the file and the file reference s3key.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String s3Key;

    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        uploadedAt = LocalDateTime.now();
    }

    @ManyToOne
    private Ticket ticket;
}
