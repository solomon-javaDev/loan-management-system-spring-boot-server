package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @Query(value = "SELECT * FROM payments WHERE deleted = true", nativeQuery = true)
    List<Payment> findAllDeleted();

    @Query(value = "SELECT * FROM payments WHERE id = :id", nativeQuery = true)
    java.util.Optional<Payment> findByIncludingDeleted(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE payments SET deleted = false WHERE id = :id", nativeQuery = true)
    int restoreById(@Param("id") Integer id);
    
    @EntityGraph(attributePaths = {
            "loan",
            "loan.customer"
    })
    List<Payment> findAllByOrderByDateDesc();

    List<Payment> findByDate(LocalDate date);

    List<Payment> findByDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(p.amountReceived), 0) FROM Payment p WHERE p.date = :date")
    BigDecimal sumAmountReceivedByDate(@Param("date") LocalDate date);
}
