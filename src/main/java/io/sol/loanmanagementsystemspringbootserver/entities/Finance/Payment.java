package io.sol.loanmanagementsystemspringbootserver.entities.Finance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private LocalDate date; // Represents the date on which the payment was made

    @Column
    private BigDecimal amountReceived;

    @Column
    private BigDecimal principalAmount = BigDecimal.ZERO;

    @Column
    private BigDecimal interestAmount = BigDecimal.ZERO;

    @Column
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column
    private BigDecimal surchargeAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;


}
