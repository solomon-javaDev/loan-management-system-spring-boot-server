package io.sol.loanmanagementsystemspringbootserver.repositories;

import io.sol.loanmanagementsystemspringbootserver.entities.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
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
