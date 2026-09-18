package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransaction;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {
    @Query(value = "SELECT * FROM cash_transactions WHERE deleted = true", nativeQuery = true)
    List<CashTransaction> findAllDeleted();

    @Query(value = "SELECT * FROM cash_transactions WHERE id = :id", nativeQuery = true)
    Optional<CashTransaction> findByIncludingDeleted(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE cash_transactions SET deleted = false WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Long id);
    List<CashTransaction> findByTypeInAndDateBetween(List<CashTransactionType> types, LocalDateTime start, LocalDateTime end);
}
