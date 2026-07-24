package io.sol.loanmanagementsystemspringbootserver.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

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

    @Column
    private String customerName;

    @Column(unique = true)
    @Email(message = "Please submit an email")
    private String email;

    @Column
    @NotBlank(message = "Telephone number is required")
    private String telephone;

    @Column
    private String address;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans = new ArrayList<>();



    /*
    Methods to keep the loans and customer entities in sync because they're the backbone of the app
     */
    public  void addLoan(Loan loan){
        loans.add(loan);
        loan.setCustomer(this);
    }

    public void removeLoan(Loan loan){
        loans.remove(loan);
        loan.setCustomer(null);
    }

    public List<Loan> getLoans() {
        return loans;
    }

    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }

    public Customer() {
        this.customerName = firstName + " " + lastName + " " + otherNames;

    }

    public Customer(String firstName, String lastName, String otherNames, String email, String telephone, String address) {
        this.customerName = firstName + " " + lastName + " " + otherNames;
        this.firstName = firstName;
        this.lastName = lastName;
        this.otherNames = otherNames;
        this.email = email;
        this.telephone = telephone;
        this.address = address;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

     public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
