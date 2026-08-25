
package io.sol.loanmanagementsystemspringbootserver.dtos;

import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;

import java.io.Serializable;

public class CustomerCreateDTO implements Serializable {
    private String firstName;
    private String lastName;
    private String otherNames;
    private String nin;
    private String telephone;
    private String address;
    private String guarantorName;
    private String guarantorPhone;
    private String guarantorNin;
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

    public String getNin() { return nin; }
    public void setNin(String nin) { this.nin = nin; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGuarantorName() { return guarantorName; }
    public void setGuarantorName(String guarantorName) { this.guarantorName = guarantorName; }

    public String getGuarantorPhone() { return guarantorPhone; }
    public void setGuarantorPhone(String guarantorPhone) { this.guarantorPhone = guarantorPhone; }

    public String getGuarantorNin() { return guarantorNin; }
    public void setGuarantorNin(String guarantorNin) { this.guarantorNin = guarantorNin; }

    public String getAccountNumber() {
         return (service.generateAccountNumber());
    }

    public String getCustomerName() {
        return firstName + " " + lastName;
    }
}
