package org.example.alfs.services;

import org.example.alfs.dto.auth.SignupRequestDTO;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.example.alfs.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Login tests")
    class Login {

        @Test
        @DisplayName("User not found should throw Unauthorized")
        void whenUserNotFound_throwsUnauthorized() {
            // Arrange
            when(userRepository.findByUsername("no-user"))
                    .thenReturn(Optional.empty());

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.login("no-user", "password"));

            // Assert
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Wrong password should throw Unauthorized")
        void whenWrongPassword_throwsUnauthorized() {
            // Arrange
            User user = new User();
            user.setPasswordHash("hashed-password");

            when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.login("username", "wrong-password"));

            // Assert
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Correct credentials should return user")
        void whenCredentialsCorrect_returnsUser() {
            // Arrange
            User user = new User();
            user.setUsername("username");
            user.setPasswordHash("hashed-password");

            when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);

            // Act
            User result = authService.login("username", "correct-password");

            // Assert
            assertThat(result.getUsername()).isEqualTo("username");
            assertThat(result.getPasswordHash()).isEqualTo("hashed-password");
        }
    }

    @Nested
    @DisplayName("Signup tests")
    class Signup {

        @Test
        @DisplayName("Username already taken should throw Bad Request")
        void whenUsernameTaken_throwsBadRequest() {
            // Arrange
            when(userRepository.findByUsername("username")).thenReturn(Optional.of(new User()));

            SignupRequestDTO request = new SignupRequestDTO();
            request.setUsername("username");
            request.setPassword("password");

            // Act
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> authService.signup(request));

            // Assert
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getReason()).isEqualTo("Username already exists");
        }

        @Test
        @DisplayName("Username available should save user with hashed password and reporter role")
        void whenUsernameAvailable_savesUserWithHashedPasswordAndReporterRole() {
            // Arrange
            when(userRepository.findByUsername("username")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("password")).thenReturn("hashed-password");

            SignupRequestDTO request = new SignupRequestDTO();
            request.setUsername("username");
            request.setPassword("password");

            authService.signup(request);

            // Act
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            // Assert
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();
            assertThat(saved.getUsername()).isEqualTo("username");
            assertThat(saved.getPasswordHash()).isEqualTo("hashed-password");
            assertThat(saved.getRole()).isEqualTo(Role.REPORTER);
        }
    }
}