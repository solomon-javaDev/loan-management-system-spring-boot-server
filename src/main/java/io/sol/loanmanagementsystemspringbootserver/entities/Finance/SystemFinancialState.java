package io.sol.loanmanagementsystemspringbootserver.entities.Finance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@Entity
@Table(name = "system_financial_state")
public class SystemFinancialState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    // Actual Balances
    private BigDecimal cashOnHand = BigDecimal.ZERO;
    private BigDecimal bankBalance = BigDecimal.ZERO;

    // Capital
    private BigDecimal ownerCapital = BigDecimal.ZERO;

    // Loans & Principal
    private BigDecimal totalCashDisbursedInLoans = BigDecimal.ZERO;
    private BigDecimal totalPrincipalCollected = BigDecimal.ZERO;
    private BigDecimal outstandingPrincipal = BigDecimal.ZERO;
    private BigDecimal grossLoanPortfolio = BigDecimal.ZERO;

    // Interest
    private BigDecimal totalInterestCharged = BigDecimal.ZERO;
    private BigDecimal interestReceived = BigDecimal.ZERO;
    private BigDecimal interestWrittenOff = BigDecimal.ZERO;
    private BigDecimal interestReceivable = BigDecimal.ZERO;

    // Fees & Surcharges
    private BigDecimal totalFeesCollected = BigDecimal.ZERO;
    private BigDecimal totalSurchargeCharged = BigDecimal.ZERO;
    private BigDecimal totalSurchargeCollected = BigDecimal.ZERO;
    private BigDecimal surchargeReceivable = BigDecimal.ZERO;

    // Expenses & Savings
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal customerSavings = BigDecimal.ZERO;

    // Profit & Collections
    private BigDecimal netProfit = BigDecimal.ZERO;
    private BigDecimal totalCollections = BigDecimal.ZERO;

    // Liquidity
    private BigDecimal grossLiquidity = BigDecimal.ZERO;
    private BigDecimal availableLiquidity = BigDecimal.ZERO;

    // Cash Control
    private BigDecimal expectedCash = BigDecimal.ZERO;
    private BigDecimal actualCash = BigDecimal.ZERO;
    private BigDecimal cashVariance = BigDecimal.ZERO;

    // Settings
    private String adminEmails;


    @Version
    private Long version;

    private LocalDateTime updateAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate(){
        this.updateAt = LocalDateTime.now();
    }

}
