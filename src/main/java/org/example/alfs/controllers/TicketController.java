package org.example.alfs.controllers;
import org.example.alfs.security.SecurityUtils;
import org.example.alfs.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.example.alfs.dto.ticket.TicketAssignDTO;
import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.dto.ticket.TicketStatusUpdateDTO;
import org.example.alfs.dto.ticket.TicketViewDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

//TODO: Decide final routes and redirects

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketCommentService ticketCommentService;
    private final AttachmentService attachmentService;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;
    private final UserService userService;

    public TicketController(TicketService ticketService, TicketCommentService ticketCommentService,AttachmentService attachmentService, AuditService auditService,  SecurityUtils securityUtils, UserService userService) {
        this.ticketService = ticketService;
        this.ticketCommentService = ticketCommentService;
        this.attachmentService = attachmentService;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
        this.userService = userService;
    }

    //create ticket
    //@PreAuthorize("hasRole('REPORTER')") // should change later for anonymous access
    @GetMapping("/create")
    public String createNewTicketForm(Model model) {
        model.addAttribute("ticket", new TicketCreateDTO());
        return "create";
    }

    //@PreAuthorize("hasRole('REPORTER')") // should change later for anonymous access
    @PostMapping("/create")
    public String createNewTicket(
            @ModelAttribute("ticket") @Valid TicketCreateDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("ticket", dto);
            return "create";
        }

        TicketViewDTO ticket = ticketService.createNewTicket(dto);
        redirectAttributes.addFlashAttribute("success", "Ticket created successfully");

        if (ticket.getToken() != null) {
            return "redirect:/tickets/ticket-created?token=" + ticket.getToken();
        }

        return "redirect:/tickets/" + ticket.getId();
    }




    //view ticket by token
    @GetMapping("/token/{token}")
    public String viewTicketByToken(@PathVariable String token, Model model) {

        try {
            TicketViewDTO ticket = ticketService.getTicketByToken(token);

            var user = securityUtils.getCurrentUserOrNull();

            var comments = ticketCommentService.getComments(ticket.getId(), user, token);
            var attachments = attachmentService.getAttachmentsByTicketId(ticket.getId());
            var auditLogs = auditService.getAuditLogsForTicket(ticket.getId());
            var investigators = userService.getAllInvestigators();

            model.addAttribute("ticket", ticket);
            model.addAttribute("comments", comments);
            model.addAttribute("attachments", attachments);
            model.addAttribute("auditLogs", auditLogs);
            model.addAttribute("investigators", investigators);
            model.addAttribute("accessToken", token);

            return "view";

        } catch (ResponseStatusException ex) {

            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "redirect:/login?tokenError=true";
            }

            throw ex;
        }
    }


    //view ticket by id
    @PreAuthorize("hasAnyRole('ADMIN','INVESTIGATOR','REPORTER')")
    @GetMapping("/{id}")
    public String viewTicketById(@PathVariable Long id, Model model) {

        TicketViewDTO ticket = ticketService.getTicketById(id);

        var user = securityUtils.getCurrentUserOrNull();

        // get comments
        var comments = ticketCommentService.getComments(id, user, null);

        // get attachments
        var attachments = attachmentService.getAttachmentsByTicketId(id);

        // get audit logs
        var auditLogs = auditService.getAuditLogsForTicket(id);

        var investigators = userService.getAllInvestigators();


        model.addAttribute("ticket", ticket);
        model.addAttribute("comments", comments);
        model.addAttribute("attachments", attachments);
        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("investigators", investigators);
        model.addAttribute("accessToken", null);


        return "view";
    }


    //assign ticket
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/assign")
    public String assignTicket(@PathVariable Long id, @Valid @ModelAttribute TicketAssignDTO dto) {

        ticketService.assignInvestigator(id, dto.getInvestigatorId());

        return "redirect:/tickets/" + id;
    }

    //update status
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','INVESTIGATOR')")
    public String updateStatus(@PathVariable Long id, @Valid @ModelAttribute TicketStatusUpdateDTO dto) {

        ticketService.updateTicketStatus(id, dto.getStatus());

        return "redirect:/tickets/" + id;
    }


    @PreAuthorize("hasRole('REPORTER')")
    @GetMapping("/my")
    public String myTickets(Model model) {

        model.addAttribute("tickets", ticketService.getMyTickets());
        return "my-tickets";
    }

    @PreAuthorize("hasRole('INVESTIGATOR')")
    @GetMapping("/assigned")
    public String myAssignedTickets(Model model) {

        model.addAttribute("tickets", ticketService.getMyAssignedTickets());
        return "assigned-tickets";
    }


    @GetMapping("/ticket-created")
    public String ticketCreated(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "ticket-created";
    }
    //create comment
    //View comment
    //upload attachment
}
