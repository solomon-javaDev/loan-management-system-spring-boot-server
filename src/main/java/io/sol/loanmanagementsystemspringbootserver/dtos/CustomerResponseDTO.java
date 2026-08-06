package io.sol.loanmanagementsystemspringbootserver.dtos;

import java.io.Serializable;

public class CustomerResponseDTO implements Serializable {
    private int id;
    private String accountNumber;
    private String firstName;
    private String lastName;
    private String otherNames;
    private String customerName;
    private String email;
    private String telephone;
    private String address;
    private int loanCount;

    public CustomerResponseDTO() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getOtherNames() { return otherNames; }
    public void setOtherNames(String otherNames) { this.otherNames = otherNames; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getLoanCount() { return loanCount; }
    public void setLoanCount(int loanCount) { this.loanCount = loanCount; }

    @Override
    public String toString() {
        return customerName != null ? customerName : (firstName + " " + lastName);
    }
}
