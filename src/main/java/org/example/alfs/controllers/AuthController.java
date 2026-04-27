package org.example.alfs.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.example.alfs.dto.auth.LoginRequestDTO;
import org.example.alfs.dto.auth.LoginResponseDTO;
import org.example.alfs.dto.auth.SignupRequestDTO;
import org.example.alfs.entities.User;
import org.example.alfs.security.JwtService;
import org.example.alfs.services.AuthService;
import org.springframework.web.bind.annotation.*;

/**
 * REST-based authentication controller used for API clients such as Postman.
 *
 * <p>This controller handles JSON-based authentication requests and returns JWT tokens.
 *
 * <p>NOTE: The application also contains a separate AuthViewController which handles browser-based
 * login using HTML forms and cookies.
 *
 * <p>We intentionally separate these concerns:
 *
 * <p>- AuthController → API (JSON, used for testing and potential future clients) -
 * AuthViewController → UI (HTML forms, browser login flow)
 *
 * <p>This separation keeps the API clean and allows independent development of backend logic and
 * user interface.
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
   * <p>This endpoint is mainly used for API testing (e.g. Postman). For browser-based login, see
   * AuthViewController.
   */
  @Operation(
      summary = "Log in a user",
      description = "Authenticate with username and password and receive a JWT token")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Invalid username or password"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
      })
  @PostMapping("/login")
  public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {

    User user = authService.login(request.getUsername(), request.getPassword());

    String token = jwtService.generateToken(user);

    return new LoginResponseDTO(token);
  }

  /** Handles user signup by validating input and creating a new account. */
  @Operation(
      summary = "Sign up new user",
      description = "Create a new user account with username and password")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Username already exists")
      })
  @PostMapping("/signup")
  public void signup(@Valid @RequestBody SignupRequestDTO request) {
    authService.signup(request);
  }
}
