package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByFirstName(String firstName);
    Optional<Customer> findByNin(String nin);

    @Query("SELECT c, COUNT(l) FROM Customer c LEFT JOIN c.loans l GROUP BY c")
    List<Object[]> findAllWithLoanCount();
}
