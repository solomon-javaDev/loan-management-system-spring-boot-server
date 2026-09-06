package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.custom.Employee;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    @Query(value = "SELECT * FROM employees WHERE deleted = true", nativeQuery = true)
    List<Employee> findAllDeleted();

    @Query(value = "SELECT * FROM employees WHERE id = :id", nativeQuery = true)
    Optional<Employee> findByIncludingDeleted(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE employees SET deleted = false WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Integer id);
    List<Employee> findByRole(Role role);

   Optional<Employee> findByFirstName(String firstName);
}
