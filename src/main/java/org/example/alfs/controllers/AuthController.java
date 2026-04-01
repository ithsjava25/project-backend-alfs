package org.example.alfs.controllers;

import org.example.alfs.entities.User;
import org.example.alfs.services.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> body) {

        String username = body.get("username");
        String password = body.get("password");

        User user = authService.login(username, password);

        return "Login success: " + user.getUsername();
    }
}
