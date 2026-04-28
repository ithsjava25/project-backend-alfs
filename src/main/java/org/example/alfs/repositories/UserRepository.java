package org.example.alfs.repositories;

import java.util.List;
import java.util.Optional;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  List<User> findByRole(Role role);
}
