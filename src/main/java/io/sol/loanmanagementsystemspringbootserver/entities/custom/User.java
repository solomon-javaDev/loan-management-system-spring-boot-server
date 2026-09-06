package io.sol.loanmanagementsystemspringbootserver.entities.custom;

import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user entity in the system.
 * This class is mapped to the "users" table in the database using JPA annotations.
 * It includes the user's basic details such as username, password, email, and their role.
 * The username and email fields are enforced to be unique, and the username is mandatory.
 * The role is represented as an enumerated value.
 */
@Entity
@Table(name = "users")
@Getter @Setter @AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Email
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column
    private Role role;


}
