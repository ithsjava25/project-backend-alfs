package org.example.alfs.controllers;

import org.example.alfs.services.TicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final TicketService ticketService;

    public AdminController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/tickets")
    public String getAllTicketsForAdmin(Model model) {
        model.addAttribute("tickets", ticketService.getAllTickets());
        return "admin-tickets";
    }
}