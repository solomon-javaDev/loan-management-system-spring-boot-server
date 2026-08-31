package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.custom.Employee;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findByRole(Role role);

   Optional<Employee> findByFirstName(String firstName);
}
