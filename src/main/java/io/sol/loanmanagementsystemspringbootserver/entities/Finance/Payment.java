package io.sol.loanmanagementsystemspringbootserver.entities.Finance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@SQLDelete(sql = "UPDATE payments SET deleted = true WHERE id = ?")
@SQLRestriction("(deleted = false OR deleted IS NULL)")
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private Boolean deleted = false;

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

    @Column
    private BigDecimal remainingBalance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;


}
