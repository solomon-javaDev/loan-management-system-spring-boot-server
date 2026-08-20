package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.LoanParameterChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanParameterChangeRepository extends JpaRepository<LoanParameterChange, Long> {
    List<LoanParameterChange> findByLoanId(int loanId);
}
