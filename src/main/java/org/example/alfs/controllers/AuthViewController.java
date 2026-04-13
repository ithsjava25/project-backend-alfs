package org.example.alfs.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.alfs.dto.auth.SignupRequestDTO;
import org.example.alfs.entities.User;
import org.example.alfs.security.JwtService;
import org.example.alfs.services.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Handles login for the browser (UI).
 *
 * Uses HTML forms and stores the JWT in a cookie so the user stays logged in.
 *
 * Separate from AuthController, which is used for API login via JSON.
 */
@Controller
public class AuthViewController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthViewController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.jte
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup-form")
    public String signupForm(
            @Valid @ModelAttribute SignupRequestDTO request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "signup";
        }

        authService.signup(request);

        return "redirect:/login";


    }

    @PostMapping("/login-form")
    public String loginForm(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response
    ) {
        User user = authService.login(username, password);

        String token = jwtService.generateToken(user);

        Cookie cookie = new Cookie("JWT", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);

        response.addCookie(cookie);

        return "redirect:/my"; // should redirect to my tickets?
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("JWT", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return "redirect:/login";
    }
}