package org.example.alfs.services;

import org.example.alfs.entities.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;

    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User login(String username, String password) {


        // hash av "1234"
        String hashedPassword = passwordEncoder.encode("1234");

        // fake user until userRepository exist
        User user = new User();
        user.setUsername("adam");
        user.setPasswordHash(hashedPassword);
        System.out.println(hashedPassword);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }
}
