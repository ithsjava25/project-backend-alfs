package org.example.alfs.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/403")
    public String forbidden(Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("error", "Access denied");
        return "error";
    }
}
