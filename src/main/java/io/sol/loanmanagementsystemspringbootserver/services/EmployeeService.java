package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.EmployeeDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.User;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.UserRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.Employee;
import io.sol.loanmanagementsystemspringbootserver.repositories.EmployeeRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.UserSession;
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


    private final UserSession userSession;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final LoansRepository loansRepository;

    @Autowired
     public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, UserSession userSession, UserRepository userRepository, LoansRepository loansRepository){
       this.employeeRepository = employeeRepository;
         this.passwordEncoder = passwordEncoder;
        this.userSession = userSession;
        this.userRepository = userRepository;
        this.loansRepository = loansRepository;
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
        if(employeeDTO.getRole() != Role.FIELD_OFFICER){
            User user = new User();
            user.setEmail(employeeDTO.getEmail());
            user.setRole(employeeDTO.getRole());
            user.setUsername(employeeDTO.getUsername());
            user.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));

            userRepository.save(user);
            return Result.success("A new System user has been created successfully ", DTOMapper.toDTO(user));
        }else {
            Employee employee = DTOMapper.toEntity(employeeDTO);
            if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
                employee.setPassword(passwordEncoder.encode(employee.getPassword()));
            } else {
                employee.setPassword(passwordEncoder.encode("0000"));
            }
            Employee saved = employeeRepository.save(employee);
            return Result.success("Employee created successfully", DTOMapper.toDTO(saved));
        }
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

    // New robust update by ID
    public Result<EmployeeDTO> updateEmployee(EmployeeDTO dto) {
        if (dto.getId() == null) {
            return Result.invalid("Employee ID is required for update", null);
        }
        return employeeRepository.findById(dto.getId()).map(existing -> {
            existing.setFirstName(dto.getFirstName());
            existing.setLastName(dto.getLastName());
            existing.setUsername(dto.getUsername());
            existing.setEmail(dto.getEmail());
            existing.setTelephone(dto.getTelephone());
            existing.setRole(dto.getRole());
            existing.setSalary(dto.getSalary());
            // update password only if provided
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            Employee saved = employeeRepository.save(existing);
            return Result.success("Employee updated successfully", DTOMapper.toDTO(saved));
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
        if(!userSession.isAdmin()){
            return Result.unauthorized("This action id meant for admin only", null);
        }

        Optional<Employee> employee = employeeRepository.findByFirstName(selectedEmployee.getFirstName());

        if(employee.isPresent()){
            employeeRepository.delete(DTOMapper.toEntity(selectedEmployee));
            return Result.success("Employee deleted", null);
        }
        else{
            return Result.notFound("The employee is non existent", null);
        }
    }

    public Result<Void> deleteEmployeeById(Integer id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if(employee.isEmpty()){
            return Result.notFound("Employee not found", null);
        }
        else{
         if(loansRepository.existsByFieldOfficerAndStatus(employee.get(), LoanStatus.ACTIVE)){
             return Result.invalid("You cannot delete a field officer with active loans", null);

         }else {
             return employeeRepository.findById(id).map(emp -> {
                 employeeRepository.delete(emp);
                 return Result.success("Employee deleted", (Void) null);
             }).orElse(Result.notFound("Employee not found", null));
         }
        }
    }
}
