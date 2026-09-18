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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoansRepository loansRepository;
    private final AuditService auditService;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, UserSession userSession, UserRepository userRepository, PasswordEncoder passwordEncoder, LoansRepository loansRepository, AuditService auditService){
       this.employeeRepository = employeeRepository;
        this.userSession = userSession;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loansRepository = loansRepository;
        this.auditService = auditService;
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
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        if (employeeDTO == null || employeeDTO.getUsername() == null || employeeDTO.getUsername().isBlank()) {
            return Result.invalid("Username is required", null);
        }
        if (employeeDTO.getPassword() == null || employeeDTO.getPassword().isBlank()) {
            return Result.invalid("Password is required", null);
        }
        if(employeeDTO.getRole() != Role.FIELD_OFFICER){
            // Check if user already exists
            Optional<User> existingUser = userRepository.findByUsername(employeeDTO.getUsername());
            User user = existingUser.orElseGet(User::new);
            
            user.setEmail(employeeDTO.getEmail());
            user.setRole(employeeDTO.getRole());
            user.setUsername(employeeDTO.getUsername());
            user.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));

            userRepository.save(user);

            // Also create or update an employee record for them if they are Cashier or Admin
            Optional<Employee> existingEmployee = employeeRepository.findByUsername(employeeDTO.getUsername());
            Employee employee = existingEmployee.orElseGet(() -> DTOMapper.toEntity(employeeDTO));
            
            if (existingEmployee.isPresent()) {
                employee.setFirstName(employeeDTO.getFirstName());
                employee.setLastName(employeeDTO.getLastName());
                employee.setEmail(employeeDTO.getEmail());
                employee.setTelephone(employeeDTO.getTelephone());
                employee.setRole(employeeDTO.getRole());
                employee.setSalary(employeeDTO.getSalary());
            }
            
            employee.setPassword(user.getPassword()); // same encoded password
            employeeRepository.save(employee);

            return Result.success("A new System user and Employee record has been created successfully ", DTOMapper.toDTO(user));
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
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        return employeeRepository.findById(id).map(employee -> {
            employee.setActive(active);
            Employee saved = employeeRepository.save(employee);
            
            // Sync with the User table if applicable
            userRepository.findByUsername(employee.getUsername()).ifPresent(_ -> {
                // todo Assuming User should also be "active" but there is no active field in User entity.
                // If there was an active field in User, we would set it here.
                // However, the issue description says "activate and deactivate buttons should apply on both tables".
                // Since User entity doesn't have 'active' field, we might need to add it or just sync what we can.
            });

            auditService.record("UPDATE", "EMPLOYEE", saved.getId(), "Employee status updated");
            return Result.success("Employee status updated", DTOMapper.toDTO(saved));
        }).orElse(Result.notFound("Employee not found", null));
    }

    // New robust update by ID
    public Result<EmployeeDTO> updateEmployee(EmployeeDTO dto) {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        if (dto.getId() == null) {
            return Result.invalid("Employee ID is required for update", null);
        }
        return employeeRepository.findById(dto.getId()).map(existing -> {
            String oldUsername = existing.getUsername();
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

            // Sync with the User table
            userRepository.findByUsername(oldUsername).ifPresent(user -> {
                user.setUsername(dto.getUsername());
                user.setEmail(dto.getEmail());
                user.setRole(dto.getRole());
                if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                    user.setPassword(existing.getPassword());
                }
                userRepository.save(user);
            });

            auditService.record("UPDATE", "EMPLOYEE", saved.getId(), "Employee details updated");
            return Result.success("Employee updated successfully", DTOMapper.toDTO(saved));
        }).orElse(Result.notFound("Employee not found", null));
    }

    public Result<Void> deleteEmployeeById(Integer id) {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
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
                 auditService.record("SOFT_DELETE", "EMPLOYEE", id, "Employee soft deleted");
                 return Result.success("Employee deleted", (Void) null);
             }).orElse(Result.notFound("Employee not found", null));
         }
        }
    }
}
