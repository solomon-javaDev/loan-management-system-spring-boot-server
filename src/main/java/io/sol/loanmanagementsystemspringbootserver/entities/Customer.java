package io.sol.loanmanagementsystemspringbootserver.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    @NotNull(message = "First name cannot be empty")
    private String firstName;

    @Column
    @NotNull(message = "Last name cannot be empty")
    private String lastName;

    @Column
    private String otherNames;

    @Column(unique = true)
    @Email(message = "Please submit an email")
    private String email;

    @Column
    private String telephone;

    public Customer() {

    }

    public Customer(String firstName, String lastName, String otherNames, String email, String telephone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.otherNames = otherNames;
        this.email = email;
        this.telephone = telephone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(String otherNames) {
        this.otherNames = otherNames;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
