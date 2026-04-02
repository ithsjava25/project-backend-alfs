package org.example.alfs.controllers;

import org.example.alfs.dto.auth.LoginRequestDTO;
import org.example.alfs.dto.auth.LoginResponseDTO;
import org.example.alfs.entities.User;
import org.example.alfs.services.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles user login by validating credentials and returning user details.
     */
    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {

        User user = authService.login(
                request.getUsername(),
                request.getPassword()
        );

        return new LoginResponseDTO(
                user.getUsername(),
                user.getRole().name()
        );
    }
}
