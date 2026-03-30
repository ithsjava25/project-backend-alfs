package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;

/*
Representing a whistleblower report.
The ticket can be followed by the anonymous reporter by using the reporterToken.
 */

@Entity
@Table(name = "ticket")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String description;

    private String status;

    @Column(nullable = false, unique = true, length = 128, updatable = false)
    private String reporterToken;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments;

    @ManyToOne
    private User assignedHandler;
}
