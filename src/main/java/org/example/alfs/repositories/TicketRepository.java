package org.example.alfs.repositories;

import org.example.alfs.entities.Ticket;
import org.example.alfs.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Anonym anmälare ser sitt fall med token
    Optional<Ticket> findByReporterToken(String reporterToken);

    // Inloggad anmälare ser sitt/sina fall
    List<Ticket> findByReporterId(Long reporterId);

    // Utredare ser sina tilldelade fall
    List<Ticket> findByInvestigatorId(Long investigatorId);

    // Filtrera fall efter status
    List<Ticket> findByStatus(TicketStatus status);

    // Filtrera fall efter status och utredare
    List<Ticket> findByStatusAndInvestigatorId(TicketStatus status, Long investigatorId);

    // Hämta alla fall, paginerat
    Page<Ticket> findAll(Pageable pageable);

}
