package org.example.alfs.controllers;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.alfs.dto.auth.SignupRequestDTO;
import org.example.alfs.entities.User;
import org.example.alfs.security.JwtService;
import org.example.alfs.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

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
    public String loginPage(@RequestParam(required = false) String error, @RequestParam(required = false) String tokenError, Model model) {
        model.addAttribute("error", error);
        model.addAttribute("tokenError", tokenError);
        return "login";
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
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User user = authService.login(username, password);

            String token = jwtService.generateToken(user);

            ResponseCookie cookie = ResponseCookie.from("JWT", token)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("Lax")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());

            redirectAttributes.addFlashAttribute("success", "You are signed in!");
            return "redirect:/";

        } catch (ResponseStatusException ex) {

            // login-error -> redirect to form and show error
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return "redirect:/login?error=true";
            }

            // other error throw
            throw ex;
        }
    }

    @PostMapping("/auth/logout")
    public String logout(HttpServletResponse response, RedirectAttributes redirectAttributes) {

        ResponseCookie cookie = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        redirectAttributes.addFlashAttribute("success", "Successfully signed out");
        return "redirect:/";
    }
}