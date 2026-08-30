
package io.sol.loanmanagementsystemspringbootserver.dtos;

import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
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

    public CustomerCreateDTO() {
    }


    public String getCustomerName() {
        return firstName + " " + lastName;
    }

}
