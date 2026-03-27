package org.example.alfs.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
Represents a system user.
Users have different roles such as admin or investigator.
 */
@Entity
@Table(name="system_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemUser {

    @GeneratedValue
    @Id
    private Long id;

    private String username;
    private String password;

    private String role;

}
