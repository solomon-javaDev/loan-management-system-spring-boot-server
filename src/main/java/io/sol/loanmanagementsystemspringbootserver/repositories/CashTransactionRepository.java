package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.CashTransaction;
import io.sol.loanmanagementsystemspringbootserver.entities.CashTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {
    List<CashTransaction> findByDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(ct.amount), 0) FROM CashTransaction ct WHERE ct.type = :type AND ct.date >= :start AND ct.date < :end")
    BigDecimal sumAmountByTypeAndDateBetween(@Param("type") CashTransactionType type,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);
}
