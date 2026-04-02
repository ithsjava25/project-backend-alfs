package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.alfs.enums.Role;

/*
Represents a system user.
Users have different roles such as admin or investigator.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

}
