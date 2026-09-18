package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Expense;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query(value = "SELECT * FROM expenses WHERE deleted = true", nativeQuery = true)
    List<Expense> findAllDeleted();

    @Query(value = "SELECT * FROM expenses WHERE id = :id", nativeQuery = true)
    java.util.Optional<Expense> findByIncludingDeleted(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE expenses SET deleted = false WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Long id);
    List<Expense> findByDateBetween(LocalDateTime start, LocalDateTime end);
    List<Expense> findByCategory(ExpenseCategory category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.date >= :start AND e.date < :end")
    BigDecimal sumAmountByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE CAST(e.date AS date) = :date")
    BigDecimal sumAmountByDate(@Param("date") LocalDate date);
}
