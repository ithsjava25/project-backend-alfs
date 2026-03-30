package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.alfs.Role;

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

    @GeneratedValue
    @Id
    private Long id;

    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

}
