package org.example.alfs.services;

import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.example.alfs.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllInvestigators() {
        return userRepository.findByRole(Role.INVESTIGATOR);
    }
}
