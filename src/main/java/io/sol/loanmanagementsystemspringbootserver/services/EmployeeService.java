package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.EmployeeDTO;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Employee;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * The EmployeeService class provides services related to employees, including retrieval
 * of employees by their roles. It acts as a service layer in the application, managing
 * business logic and interacting with the repository layer.
 */

@Service
public class EmployeeService {


    private EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
     public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder){
       this.employeeRepository = employeeRepository;
         this.passwordEncoder = passwordEncoder;
    }

    public Result<List<EmployeeDTO>> getEmployeeByRole(Role role){
        List<Employee> employeeList = employeeRepository.findByRole(role);
        if(!employeeList.isEmpty()){
            return Result.success("Employees retrieved", 
                employeeList.stream().map(DTOMapper::toDTO).collect(Collectors.toList()));
        }

        return Result.notFound("No employees found with " + role + " role, Please add them", null);
    }

    public Result<EmployeeDTO> createEmployee(EmployeeDTO employeeDTO) {
        if (employeeDTO == null || employeeDTO.getUsername() == null || employeeDTO.getUsername().isBlank()) {
            return Result.invalid("Username is required", null);
        }
        if (employeeDTO.getPassword() == null || employeeDTO.getPassword().isBlank()) {
            return Result.invalid("Password is required", null);
        }
        Employee employee = DTOMapper.toEntity(employeeDTO);
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        } else {
            employee.setPassword(passwordEncoder.encode("0000"));
        }
        Employee saved = employeeRepository.save(employee);
        return Result.success("Employee created successfully", DTOMapper.toDTO(saved));
    }

    public Result<List<EmployeeDTO>> getAllEmployees() {
        return Result.success("All employees retrieved", 
            employeeRepository.findAll().stream().map(DTOMapper::toDTO).collect(Collectors.toList()));
    }

    public Result<EmployeeDTO> updateEmployeeStatus(Integer id, boolean active) {
        return employeeRepository.findById(id).map(employee -> {
            employee.setActive(active);
            Employee saved = employeeRepository.save(employee);
            return Result.success("Employee status updated", DTOMapper.toDTO(saved));
        }).orElse(Result.notFound("Employee not found", null));
    }

    public Result<EmployeeDTO> upDateEmployee(EmployeeDTO selectedEmployee) {
        Optional<Employee> employee = employeeRepository.findByFirstName(selectedEmployee.getFirstName());

        if(employee.isEmpty()){
            return Result.notFound("The employee provided is not present in the database", selectedEmployee);

        }else{
            return Result.success("The employee has been updated", DTOMapper.toDTO(employeeRepository.save(DTOMapper.toEntity(selectedEmployee))));
        }
    }

    public Result<Void> deleteEmployee(EmployeeDTO selectedEmployee){
        Optional<Employee> employee = employeeRepository.findByFirstName(selectedEmployee.getFirstName());

        if(employee.isPresent()){
            employeeRepository.delete(DTOMapper.toEntity(selectedEmployee));
            return Result.success("Employee deleted", null);
        }
        else{
            return Result.notFound("The employee is non existent", null);
        }
    }
}
