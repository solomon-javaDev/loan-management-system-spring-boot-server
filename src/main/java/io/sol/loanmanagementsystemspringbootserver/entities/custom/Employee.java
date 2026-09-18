package io.sol.loanmanagementsystemspringbootserver.entities.custom;

import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "employees")
@SQLDelete(sql = "UPDATE employees SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    private String firstName;
    private String lastName;
    private int salary;

    public Employee(String firstName, String lastName, int salary, String email, Role role, String telephone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.email = email;
        this.role = role;
        this.username = firstName + " " + lastName;
        this.password = "0000";
        this.telephone = telephone;
        this.active = true;
    }

      @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
