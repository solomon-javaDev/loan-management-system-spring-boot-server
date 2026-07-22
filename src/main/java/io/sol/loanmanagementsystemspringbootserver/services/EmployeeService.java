package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.config.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Employee;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {


    private EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository){
       this.employeeRepository = employeeRepository;
    }

    public Result<List<Employee>> getEmployeeByRole(Role role){
        List<Employee> employeeList = employeeRepository.findByRole(role);
        if(!employeeList.isEmpty()){
            return Result.success("Employees retrieved", employeeList);
        }

        return Result.notFound("No employees found with Field Officer role, Please add field officers", null);
    }
}
