package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface LoansRepository extends JpaRepository<Loan, Integer> {
    List<Loan> findByStatus(LoanStatus status);
    List<Loan> findByStartDate(LocalDate date);
}
