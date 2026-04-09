package org.example.alfs.controllers;

import org.example.alfs.dto.auth.LoginRequestDTO;
import org.example.alfs.dto.auth.LoginResponseDTO;
import org.example.alfs.dto.auth.SignupRequestDTO;
import org.example.alfs.entities.User;
import org.example.alfs.security.JwtService;
import org.example.alfs.services.AuthService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
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

        String token = jwtService.generateToken(user);
        return new LoginResponseDTO(token);
    }


    /**
     * Handles user signup by validating input and creating a new account.
     */
    @PostMapping("/signup")
    public void signup(@Valid @RequestBody SignupRequestDTO request) {
        authService.signup(request);
    }

}
