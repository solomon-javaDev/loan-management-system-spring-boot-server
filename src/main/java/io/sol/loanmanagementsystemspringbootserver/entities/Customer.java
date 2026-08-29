package io.sol.loanmanagementsystemspringbootserver.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.SQLDelete;

import java.util.ArrayList;
import java.util.List;

@Entity
@SQLDelete(sql = "update Customer SET deleted = true where id = ?")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private boolean deleted = Boolean.FALSE;

    @Column(unique = true)
    private String accountNumber;

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
    @NotBlank(message = "NIN is required")
    private String nin;

    @Column
    @NotBlank(message = "Telephone number is required")
    private String telephone;

    @Column
    private String address;

    private String guarantorName;

    private String guarantorPhone;

    private String guarantorNin;
    
    @Column(precision = 19, scale = 4)
    private java.math.BigDecimal savingsBalance = java.math.BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "field_officer_id")
    private Employee fieldOfficer;

    @Column(nullable = false)
    private boolean active = true;

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

    public Customer(String firstName, String lastName, String otherNames, String nin, String telephone, String address) {
        this.customerName = firstName + " " + lastName + " " + otherNames;
        this.firstName = firstName;
        this.lastName = lastName;
        this.otherNames = otherNames;
        this.nin = nin;
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
        refreshCustomerName();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        refreshCustomerName();
    }

    public String getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(String otherNames) {
        this.otherNames = otherNames;
        refreshCustomerName();
    }

    public String getNin() {
        return nin;
    }

    public void setNin(String nin) {
        this.nin = nin;
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

    public String getGuarantorName() {
        return guarantorName;
    }

    public void setGuarantorName(String guarantorName) {
        this.guarantorName = guarantorName;
    }

    public String getGuarantorPhone() {
        return guarantorPhone;
    }

    public void setGuarantorPhone(String guarantorPhone) {
        this.guarantorPhone = guarantorPhone;
    }

    public String getGuarantorNin() {
        return guarantorNin;
    }

    public void setGuarantorNin(String guarantorNin) {
        this.guarantorNin = guarantorNin;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public java.math.BigDecimal getSavingsBalance() {
        return savingsBalance;
    }

    public void setSavingsBalance(java.math.BigDecimal savingsBalance) {
        this.savingsBalance = savingsBalance;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Employee getFieldOfficer() {
        return fieldOfficer;
    }

    public void setFieldOfficer(Employee fieldOfficer) {
        this.fieldOfficer = fieldOfficer;
    }

    private void refreshCustomerName() {
        this.customerName = String.join(" ",
                firstName == null ? "" : firstName,
                lastName == null ? "" : lastName,
                otherNames == null ? "" : otherNames).trim().replaceAll(" +", " ");
    }
}
