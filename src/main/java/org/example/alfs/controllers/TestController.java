package org.example.alfs.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// This controller demonstrates role-based access control using @PreAuthorize.
// Use this as a reference when implementing real endpoints.
@RestController
public class TestController {

    @PreAuthorize("hasAnyRole('ADMIN','INVESTIGATOR','REPORTER')")
    @GetMapping("/api/test/all-roles")
    public String allRoles() {
        return "all roles allowed";
    }

    @PreAuthorize("hasRole('REPORTER')")
    @GetMapping("/api/test/create-ticket")
    public String createTicket() {
        return "reporter can create ticket";
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/test/assign-ticket")
    public String assignTicket() {
        return "admin can assign ticket";
    }

    @PreAuthorize("hasRole('INVESTIGATOR')")
    @GetMapping("/api/test/update-status")
    public String updateStatus() {
        return "investigator can update status";
    }

    // need to be signed in - all roles
    @GetMapping("/api/hello")
    public String hello() {
        return "hello secured";
    }

    // Only ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/test")
    public String adminOnly() {
        return "only admin";
    }

    // Only INVESTIGATOR
    @PreAuthorize("hasRole('INVESTIGATOR')")
    @GetMapping("/api/investigator/test")
    public String investigatorOnly() {
        return "only investigator";
    }

    // Only REPORTER
    @PreAuthorize("hasRole('REPORTER')")
    @GetMapping("/api/reporter/test")
    public String reporterOnly() {
        return "only reporter";
    }

}