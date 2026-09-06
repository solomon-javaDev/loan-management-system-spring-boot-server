package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LoansRepository extends JpaRepository<Loan, Integer> {
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByStartDate(LocalDate date);
    List<Loan> findByStatusIn(List<LoanStatus> statuses);

    boolean existsByFieldOfficerAndStatus(Employee fieldOfficer, LoanStatus status);

    @Query("SELECT COALESCE(SUM(l.principal), 0) FROM Loan l WHERE l.startDate = :date")
    BigDecimal sumPrincipalByStartDate(@Param("date") LocalDate date);
}
