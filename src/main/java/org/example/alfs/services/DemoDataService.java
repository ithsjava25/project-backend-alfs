package org.example.alfs.services;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.alfs.entities.*;
import org.example.alfs.enums.AuditAction;
import org.example.alfs.enums.Role;
import org.example.alfs.enums.TicketStatus;
import org.example.alfs.repositories.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DemoDataService {

  private final UserRepository userRepository;
  private final TicketRepository ticketRepository;
  private final TicketCommentRepository commentRepository;
  private final AttachmentRepository attachmentRepository;
  private final AuditLogRepository auditLogRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void seedDemoData() {
    if (userRepository.existsByUsername("admin")) {
      log.info("Database already contains data, skipping demo data seeding.");
      return;
    }

    log.info("Seeding demo data...");

    SeedUsers users = createUsers();
    SeedTickets tickets = createTickets(users);
    createComments(tickets, users);
    createAttachments(tickets, users);

    log.info("Demo data seeding completed.");
  }

  private SeedUsers createUsers() {
    User admin = new User();
    admin.setUsername("admin");
    admin.setPasswordHash(passwordEncoder.encode("admin"));
    admin.setRole(Role.ADMIN);

    User investigator1 = new User();
    investigator1.setUsername("investigator1");
    investigator1.setPasswordHash(passwordEncoder.encode("investigator1"));
    investigator1.setRole(Role.INVESTIGATOR);

    User investigator2 = new User();
    investigator2.setUsername("investigator2");
    investigator2.setPasswordHash(passwordEncoder.encode("investigator2"));
    investigator2.setRole(Role.INVESTIGATOR);

    User reporter1 = new User();
    reporter1.setUsername("reporter1");
    reporter1.setPasswordHash(passwordEncoder.encode("reporter1"));
    reporter1.setRole(Role.REPORTER);

    User reporter2 = new User();
    reporter2.setUsername("reporter2");
    reporter2.setPasswordHash(passwordEncoder.encode("reporter2"));
    reporter2.setRole(Role.REPORTER);

    userRepository.saveAll(List.of(admin, investigator1, investigator2, reporter1, reporter2));

    return new SeedUsers(admin, investigator1, investigator2, reporter1, reporter2);
  }

  private record SeedUsers(
      User admin, User investigator1, User investigator2, User reporter1, User reporter2) {}

  private SeedTickets createTickets(SeedUsers u) {
    Ticket t1 =
        addTicket(
            "Corruption case", "Procurement issue", u.reporter1(), u.investigator1(), u.admin());
    changeStatus(t1, u.investigator1(), TicketStatus.IN_PROGRESS);

    Ticket t2 =
        addTicket(
            "Anonymous harassment",
            "Ongoing issue",
            null, // Anonymous reporter
            u.investigator2(),
            u.admin());
    changeStatus(t2, u.investigator2(), TicketStatus.IN_PROGRESS);

    Ticket t3 =
        addTicket(
            "Financial misreporting",
            "Accounting irregularities",
            u.reporter2(),
            u.investigator1(),
            u.admin());
    changeStatus(t3, u.investigator1(), TicketStatus.IN_PROGRESS);
    changeStatus(t3, u.investigator1(), TicketStatus.RESOLVED);

    Ticket t4 = addTicket("Unauthorized access", "Security issue", u.reporter1(), null, u.admin());

    return new SeedTickets(t1, t2, t3, t4);
  }

  private record SeedTickets(Ticket t1, Ticket t2, Ticket t3, Ticket t4) {}

  private Ticket addTicket(
      String title, String description, User reporter, User investigator, User admin) {
    Ticket t = new Ticket();

    t.setTitle(title);
    t.setDescription(description);
    t.setStatus(TicketStatus.OPEN);
    t.setReporter(reporter);
    t.setInvestigator(investigator);

    if (reporter == null) {
      t.setReporterToken(UUID.randomUUID().toString());
    }

    t = ticketRepository.save(t);

    if (reporter == null) {
      addAuditLog(t, null, AuditAction.CREATED, "ticket", null, "created (anonymous)");
    } else {
      addAuditLog(t, reporter, AuditAction.CREATED, "ticket", null, "created");
    }

    if (investigator != null) {
      addAuditLog(t, admin, AuditAction.ASSIGNED, "investigator", null, investigator.getUsername());
    }

    return t;
  }

  private void createComments(SeedTickets t, SeedUsers u) {
    addComment(t.t1(), u.reporter1(), "Can someone look into this ASAP?");
    addComment(t.t1(), u.investigator1(), "We are investigating this matter.");
    addComment(t.t1(), u.reporter1(), "Thank you for the update.");

    addComment(t.t2(), null, "This is ongoing for months.");
    addComment(t.t2(), u.investigator2(), "We take this seriously and will escalate.");

    addComment(t.t3(), u.investigator1(), "Issue identified and resolved.");

    addComment(t.t4(), u.reporter1(), "No response yet.");
  }

  private void addComment(Ticket ticket, User author, String message) {
    TicketComment c = new TicketComment();

    c.setTicket(ticket);
    c.setAuthor(author);
    c.setMessage(message);
    c.setInternalNote(false);

    commentRepository.save(c);

    addAuditLog(ticket, author, AuditAction.COMMENT_ADDED, "comment", null, message);
  }

  private void createAttachments(SeedTickets t, SeedUsers u) {
    addAttachment(t.t1(), "procurement-doc.pdf", u.reporter1());

    addAttachment(t.t2(), "complaint-evidence.txt", null);

    addAttachment(t.t3(), "financial-report.xlsx", u.investigator1());
  }

  private void addAttachment(Ticket ticket, String fileName, User actor) {
    Attachment a = new Attachment();

    a.setTicket(ticket);
    a.setFileName(fileName);
    a.setS3Key("demo/" + UUID.randomUUID());
    a.setUploadedBy(actor);

    attachmentRepository.save(a);

    addAuditLog(ticket, actor, AuditAction.ATTACHMENT_ADDED, "attachment", null, fileName);
  }

  private void addAuditLog(
      Ticket ticket, User user, AuditAction action, String field, String oldVal, String newVal) {
    AuditLog entry = new AuditLog();

    entry.setTicket(ticket);
    entry.setUser(user);
    entry.setAction(action);
    entry.setFieldName(field);
    entry.setOldValue(oldVal);
    entry.setNewValue(newVal);

    auditLogRepository.save(entry);
  }

  private void changeStatus(Ticket t, User user, TicketStatus newStatus) {
    String oldStatus = t.getStatus().name();

    if (t.getStatus() == newStatus) return;

    t.setStatus(newStatus);
    ticketRepository.save(t);

    addAuditLog(t, user, AuditAction.STATUS_CHANGED, "status", oldStatus, newStatus.name());
  }
}
