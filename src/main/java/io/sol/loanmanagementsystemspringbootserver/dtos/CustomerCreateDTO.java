
package io.sol.loanmanagementsystemspringbootserver.dtos;

import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;

import java.io.Serializable;

public class CustomerCreateDTO implements Serializable {
    private String firstName;
    private String lastName;
    private String otherNames;
    private String email;
    private String telephone;
    private String address;
    public String accountNumber;

    private CustomerService service;
    public CustomerCreateDTO() {
        this.service = service;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getOtherNames() { return otherNames; }
    public void setOtherNames(String otherNames) { this.otherNames = otherNames; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAccountNumber() {
         return (service.generateAccountNumber());
    }

    public String getCustomerName() {
        return firstName + " " + lastName;
    }
}
