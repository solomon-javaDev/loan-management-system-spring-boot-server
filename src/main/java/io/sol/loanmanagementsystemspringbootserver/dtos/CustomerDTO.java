package io.sol.loanmanagementsystemspringbootserver.dtos;

import java.io.Serializable;

public class CustomerDTO implements Serializable {
    private int id;
    private String accountNumber;
    private String firstName;
    private String lastName;
    private String otherNames;
    private String customerName;
    private String nin;
    private String telephone;
    private String address;
    private String guarantorName;
    private String guarantorPhone;
    private String guarantorNin;
    private java.math.BigDecimal savingsBalance;
    private int loanCount;

    public CustomerDTO() {}

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

    public int getLoanCount() { return loanCount; }
    public void setLoanCount(int loanCount) { this.loanCount = loanCount; }

    public java.math.BigDecimal getSavingsBalance() { return savingsBalance; }
    public void setSavingsBalance(java.math.BigDecimal savingsBalance) { this.savingsBalance = savingsBalance; }

    @Override
    public String toString() {
        return customerName != null ? customerName : (firstName + " " + lastName);
    }
}
