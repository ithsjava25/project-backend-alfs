package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.alfs.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, updatable = false)
    private String title;

    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, columnDefinition = "TEXT", updatable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TicketStatus status;

    @Column(nullable = false, unique = true, length = 128, updatable = false)
    private String reporterToken;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = TicketStatus.OPEN;
        if (reporterToken == null || reporterToken.isBlank())
            reporterToken = UUID.randomUUID().toString(); // Skapa token för anonyma anmälare
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "ticket")
    private List<TicketComment> comments;

    @OneToMany(mappedBy = "ticket")
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "ticket")
    private List<AuditLog> auditLogs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = true)  // null if anonymous
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigator_id", nullable = true)
    private User investigator;
}
