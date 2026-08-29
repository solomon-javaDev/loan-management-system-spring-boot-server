package io.sol.loanmanagementsystemspringbootserver.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employees")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return getId() != null && getId().equals(employee.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
