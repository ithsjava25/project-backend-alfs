package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.*;

/*
Represents a system user.
Users have different roles such as admin or investigator.
 */
@Entity
@Table(name="users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @GeneratedValue
    @Id
    private Long id;

    private String username;
    private String password;

    private String role;

}
