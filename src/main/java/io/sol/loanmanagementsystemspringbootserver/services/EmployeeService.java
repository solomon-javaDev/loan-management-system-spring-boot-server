package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.EmployeeDTO;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Employee;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 * The EmployeeService class provides services related to employees, including retrieval
 * of employees by their roles. It acts as a service layer in the application, managing
 * business logic and interacting with the repository layer.
 */

@Service
public class EmployeeService {


    private EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository){
       this.employeeRepository = employeeRepository;
    }

    public Result<List<EmployeeDTO>> getEmployeeByRole(Role role){
        List<Employee> employeeList = employeeRepository.findByRole(role);
        if(!employeeList.isEmpty()){
            return Result.success("Employees retrieved", 
                employeeList.stream().map(DTOMapper::toDTO).collect(Collectors.toList()));
        }

        return Result.notFound("No employees found with " + role + " role, Please add them", null);
    }
}
