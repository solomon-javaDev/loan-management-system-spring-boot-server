package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.SavingsTransaction;
import io.sol.loanmanagementsystemspringbootserver.entities.SavingsTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SavingsTransactionRepository extends JpaRepository<SavingsTransaction, Long> {
    List<SavingsTransaction> findByDateBetween(LocalDateTime start, LocalDateTime end);
    List<SavingsTransaction> findByCustomerIdAndDateBetween(Integer customerId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SavingsTransaction st WHERE st.type = :type AND CAST(st.date AS date) = :date")
    BigDecimal sumByTypeAndDate(@Param("type") SavingsTransactionType type, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SavingsTransaction st WHERE st.type = io.sol.loanmanagementsystemspringbootserver.entities.SavingsTransactionType.DEPOSIT AND CAST(st.date AS date) = :date")
    BigDecimal sumDepositsByDate(@Param("date") LocalDate date);
}
