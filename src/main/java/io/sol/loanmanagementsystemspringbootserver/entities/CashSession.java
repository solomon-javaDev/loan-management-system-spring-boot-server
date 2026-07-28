package io.sol.loanmanagementsystemspringbootserver.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cash_sessions")
public class CashSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal openingBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal closingBalance;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id")
    private User cashier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashSessionStatus status = CashSessionStatus.OPEN;

    public CashSession() {
    }

    public Integer getId() {
        return id;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public User getCashier() {
        return cashier;
    }

    public void setCashier(User cashier) {
        this.cashier = cashier;
    }

    public CashSessionStatus getStatus() {
        return status;
    }

    public void setStatus(CashSessionStatus status) {
        this.status = status;
    }
}
