package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.List;

/*
Representing a whistleblower report.
The ticket can be followed by the anonymous reporter by using the reporterToken.
 */

@Entity
@Table(name="ticket")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String description;

    private String status;

    private String reporterToken;

    private LocalDateTime createdAt;
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "ticket")
    private List<Attachment> attachment;

    @ManyToOne
    private SystemUser assignedHandler;
}
