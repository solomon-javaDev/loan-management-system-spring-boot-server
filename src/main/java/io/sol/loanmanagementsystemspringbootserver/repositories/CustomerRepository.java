package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByFirstName(String firstName);
    Optional<Customer> findByNin(String nin);

    @Query("SELECT c, COUNT(l) FROM Customer c LEFT JOIN c.loans l WHERE c.deleted = false GROUP BY c")
    List<Object[]> findAllWithLoanCount();

    @Query("SELECT DISTINCT C FROM Customer C JOIN C.loans l WHERE l.status = 'ACTIVE' AND l.fieldOfficer.username = :fieldOfficer AND l.maturityDate >= :today"
    )
    List<Customer> findCustomersByDueForFieldOfficer(@Param("field_officer")String fieldOfficer, @Param("today")LocalDate today);

    @Query("SELECT DISTINCT c FROM Customer c JOIN c.loans l WHERE l.status = 'ACTIVE' AND l.maturityDate <= :today")
    List<Customer> findAllCustomersDue(@Param("today") LocalDate today);

    @Query(value = "SELECT COUNT(*) FROM Customer", nativeQuery = true)
    long countAllIncludingDeleted();

}
