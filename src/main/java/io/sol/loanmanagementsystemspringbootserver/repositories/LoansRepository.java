package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoansRepository extends JpaRepository<Loan, Integer> {
    @Query(value = "SELECT * FROM loans WHERE deleted = true", nativeQuery = true)
    List<Loan> findAllDeleted();

    @Query(value = "SELECT * FROM loans WHERE id = :id", nativeQuery = true)
    Optional<Loan> findByIncludingDeleted(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE loans SET deleted = false WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Integer id);

    @Query(value = "SELECT COUNT(*) FROM loans", nativeQuery = true)
    long countAllIncludingDeleted();

    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByStartDate(LocalDate date);
    List<Loan> findByStatusIn(List<LoanStatus> statuses);

    @Query("SELECT COUNT(l) > 0 FROM Loan l WHERE l.customer.id = :customerId AND l.status IN :statuses")
    boolean existsByCustomerIdAndStatusIn(@Param("customerId") int customerId,
                                          @Param("statuses") List<LoanStatus> statuses);

    boolean existsByFieldOfficerAndStatus(Employee fieldOfficer, LoanStatus status);

    @Query("SELECT COALESCE(SUM(l.principal), 0) FROM Loan l WHERE l.startDate = :date")
    BigDecimal sumPrincipalByStartDate(@Param("date") LocalDate date);

    List<Loan> findAllByStatus(LoanStatus status);
}
