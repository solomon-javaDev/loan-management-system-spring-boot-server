package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Integer> {
    List<LoanInstallment> findByLoan(Loan loan);
}
