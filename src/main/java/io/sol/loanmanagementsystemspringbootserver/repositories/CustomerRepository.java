package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.custom.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @Query(value = "SELECT * FROM customer WHERE deleted = true", nativeQuery = true)
    List<Customer> findAllDeleted();

    @Query(value = "SELECT * FROM customer WHERE id = :id", nativeQuery = true)
    Optional<Customer> findByIncludingDeleted(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE customer SET deleted = false WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Integer id);
    Customer findByFirstName(String firstName);
    Optional<Customer> findByNin(String nin);

    @Query("SELECT c, COUNT(l) FROM Customer c LEFT JOIN c.loans l WHERE c.deleted = false GROUP BY c")
    List<Object[]> findAllWithLoanCount();

    @EntityGraph(attributePaths = "loans")
    @Query("SELECT DISTINCT c FROM Customer c JOIN c.loans l WHERE l.status = 'ACTIVE' AND l.fieldOfficer.username = :fieldOfficer AND l.maturityDate >= :today")
    List<Customer> findCustomersByDueForFieldOfficer(@Param("fieldOfficer") String fieldOfficer, @Param("today") LocalDate today);

    @Query("SELECT DISTINCT c FROM Customer c JOIN c.loans l WHERE l.status = 'ACTIVE' AND l.maturityDate <= :today")
    List<Customer> findAllCustomersDue(@Param("today") LocalDate today);

    @Query(value = "SELECT COUNT(*) FROM Customer", nativeQuery = true)
    long countAllIncludingDeleted();

}
