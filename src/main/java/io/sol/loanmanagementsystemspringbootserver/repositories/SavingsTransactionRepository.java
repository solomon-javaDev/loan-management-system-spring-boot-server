package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.SavingsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SavingsTransactionRepository extends JpaRepository<SavingsTransaction, Long> {
    List<SavingsTransaction> findByDateBetween(LocalDateTime start, LocalDateTime end);
    List<SavingsTransaction> findByCustomerIdAndDateBetween(Integer customerId, LocalDateTime start, LocalDateTime end);
}
