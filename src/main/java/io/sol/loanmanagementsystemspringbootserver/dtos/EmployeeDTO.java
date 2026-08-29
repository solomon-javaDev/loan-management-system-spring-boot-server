package io.sol.loanmanagementsystemspringbootserver.dtos;

import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmployeeDTO implements Serializable {
    private Integer id;
    private String firstName;
    private String lastName;
    private int salary;
    private String email;
    private String telephone;
    private Role role;
    private String username;
    private String password;
    private boolean active = true;

     @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
