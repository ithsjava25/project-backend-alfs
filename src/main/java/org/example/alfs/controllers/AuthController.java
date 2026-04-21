package org.example.alfs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.example.alfs.dto.auth.LoginRequestDTO;
import org.example.alfs.dto.auth.LoginResponseDTO;
import org.example.alfs.dto.auth.SignupRequestDTO;
import org.example.alfs.entities.User;
import org.example.alfs.security.JwtService;
import org.example.alfs.services.AuthService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


/**
 * REST-based authentication controller used for API clients such as Postman.
 *
 * This controller handles JSON-based authentication requests and returns JWT tokens.
 *
 * NOTE:
 * The application also contains a separate AuthViewController which handles
 * browser-based login using HTML forms and cookies.
 *
 * We intentionally separate these concerns:
 *
 * - AuthController → API (JSON, used for testing and potential future clients)
 * - AuthViewController → UI (HTML forms, browser login flow)
 *
 * This separation keeps the API clean and allows independent development
 * of backend logic and user interface.
 */
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
     * Authenticates a user using JSON input and returns a JWT.
     *
     * This endpoint is mainly used for API testing (e.g. Postman).
     * For browser-based login, see AuthViewController.
     */
    @Operation(summary = "Log in a user", description = "Authenticate with username and password to receive a JWT token.")
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
    @Operation(summary = "Sign up new user", description = "Makes a new user with username and password")
    @PostMapping("/signup")
    public void signup(@Valid @RequestBody SignupRequestDTO request) {
        authService.signup(request);
    }

}
