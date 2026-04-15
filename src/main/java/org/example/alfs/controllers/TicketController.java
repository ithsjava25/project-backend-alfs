package org.example.alfs.controllers;

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
    public String createNewTicket(@ModelAttribute("ticket") @Valid TicketCreateDTO ticketCreateDTO,  BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("ticket", ticketCreateDTO);
            return "create";
        }

        TicketViewDTO ticket = ticketService.createNewTicket(ticketCreateDTO);

        return "redirect:/tickets/" + ticket.getId();

    }
    //Preview ticket
    @PostMapping("/previewTicket")
    public String previewTicket(@ModelAttribute("ticket") TicketCreateDTO dto, Model model) {

        model.addAttribute("ticket", dto);
        return "previewTicket";
    }

    //Submit ticket anonymous
    @PostMapping("/submit-anonymous")
    public String submitAnonymous(@ModelAttribute TicketCreateDTO dto) {

        TicketViewDTO ticket = ticketService.createAnonymousTicket(dto);

        return "redirect:/tickets/token/" + ticket.getToken();
    }

    //Submit ticket as logged in user
    @PostMapping("/submit-authenticated")
    public String submitAuthenticated(
            @ModelAttribute TicketCreateDTO dto
    ) {

        TicketViewDTO ticket = ticketService.createNewTicket(dto);

        return "redirect:/tickets/" + ticket.getId();
    }

    //view ticket by token

    @GetMapping("/token/{token}")
    public String viewTicketByToken(@PathVariable String token, Model model) {

        TicketViewDTO ticket = ticketService.getTicketByToken(token);
        model.addAttribute("ticket", ticket);

        return "view";
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

    //create comment
    //View comment
    //upload attachment
}
