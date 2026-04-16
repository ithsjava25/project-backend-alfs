package org.example.alfs.controllers;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.example.alfs.dto.ticket.TicketAssignDTO;
import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.dto.ticket.TicketStatusUpdateDTO;
import org.example.alfs.dto.ticket.TicketViewDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.example.alfs.services.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

//TODO: Decide final routes and redirects

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;


    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;

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
            model.addAttribute("ticket", ticket);
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
        model.addAttribute("ticket", ticket);

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
