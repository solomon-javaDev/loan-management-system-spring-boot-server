package io.sol.loanmanagementsystemspringbootserver.entities;

import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 *This class represents the core treasury ledger balance
 * It segregates funds into distinct buckets and uses @Version filed to provide safe,
 * concurrent database state updates
 */

@Entity
@Table(name = "capital_accounts")
@Getter
public class CapitalAccount {

    @Id
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal workingCash;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal activePortfolioPrincipal;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal accumulatedInterestEarned;

    @Version
    private long version;

    protected CapitalAccount() {
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public static CapitalAccount initialiseNewBusiness() {
        CapitalAccount account = new CapitalAccount();
        account.id = UUID.randomUUID();
        account.workingCash = BigDecimal.valueOf(1000);
        account.activePortfolioPrincipal = BigDecimal.valueOf(20);
        account.accumulatedInterestEarned = BigDecimal.valueOf(20);
        return account;
    }

    public void injectCapital(BigDecimal amount) {
        validatePositive(amount);
        this.workingCash = this.workingCash.add(amount);
    }

    public void recordLoanDisbursement(BigDecimal amount) {
        validatePositive(amount);
        if (this.workingCash.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available cash to disburse the loan.");
        }
        this.workingCash = this.workingCash.subtract(amount);
        this.activePortfolioPrincipal = this.activePortfolioPrincipal.add(amount);
    }

    public Result<Void> processRepayment(BigDecimal amount) {
        validatePositive(amount);
        this.workingCash = this.workingCash.add(amount);
        this.activePortfolioPrincipal = this.activePortfolioPrincipal.subtract(
                this.activePortfolioPrincipal.min(amount));
        return Result.success("Payment received", null);
    }

    public Result<Void> recordExpense(BigDecimal amount) {
        validatePositive(amount);
        if (this.workingCash.compareTo(amount) < 0) {
            return Result.invalid("Operational cost is unavailable", null);
        }
        this.workingCash = this.workingCash.subtract(amount);
        return Result.success("Expense recorded", null);
    }

    public void credit(BigDecimal amount) {
        validatePositive(amount);
        this.workingCash = this.workingCash.add(amount);
    }

    public void debit(BigDecimal amount) {
        validatePositive(amount);
        if (this.workingCash.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available cash for this transaction.");
        }
        this.workingCash = this.workingCash.subtract(amount);
    }

    private void validatePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Financial transaction amounts must be strictly positive values.");
        }
    }

    public BigDecimal getCashAtHand() {
        //TODO while recoding expenses, working cash is updated, even when loans are disbursed
        return workingCash.add(accumulatedInterestEarned);
    }

}
