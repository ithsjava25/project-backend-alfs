package org.example.alfs.controllers;

import jakarta.validation.Valid;
import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.dto.ticket.TicketViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.services.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

//TODO: Decide final routes and redirects

@Controller
@RequestMapping("/tickets")
public class TicketController {

    TicketService ticketService;
    private static final Logger log = LoggerFactory.getLogger(TicketController.class);


    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;

    }

    //create ticket

    @GetMapping("/create")
    public String createNewTicketForm(Model model) {
        model.addAttribute("ticket", new TicketCreateDTO());
        return "create";
    }

    //TODO REDIRECT, WHERE??
    @PostMapping("/create")
    public String createNewTicket(@ModelAttribute("ticket") @Valid TicketCreateDTO ticketCreateDTO,  BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            log.info("Title: {}", ticketCreateDTO.getTitle());
            log.info("Description: {}", ticketCreateDTO.getDescription());
            model.addAttribute("ticket", ticketCreateDTO);
            return "create";
        }

        TicketViewDTO ticket = ticketService.createNewTicket(ticketCreateDTO);

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
    @GetMapping("/{id}")
    public String viewTicketById(@PathVariable Long id, Model model) {

        TicketViewDTO ticket = ticketService.getTicketById(id);
        model.addAttribute("ticket", ticket);

        return "view";
    }

    //assign ticket
    // TODO implement DTO + service
    @PostMapping("/{id}/assign")
    public String assignTicket(@PathVariable Long id) {

        return "redirect:/tickets/" + id;
    }

    //update status
    // TODO implement DTO + service
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id) {

        return "redirect:/tickets/" + id;
    }

    //create comment
    //View comment
    //upload attachment
}
