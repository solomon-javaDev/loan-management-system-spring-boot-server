package io.sol.loanmanagementsystemspringbootserver.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate maturityDate;

    @Column(nullable = false)
    private LocalDate fullPaidDate;

    @Column(nullable = false)
    private BigDecimal principal;

    @Column(nullable = false)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private int tenor;

    @Column(nullable = false)
    private String collateral;

    @Column(nullable = false)
    private BigDecimal fees;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String fieldOfficer;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public LocalDate getFullPaidDate() {
        return fullPaidDate;
    }

    public void setFullPaidDate(LocalDate fullPaidDate) {
        this.fullPaidDate = fullPaidDate;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public int getTenor() {
        return tenor;
    }

    public void setTenor(int tenor) {
        this.tenor = tenor;
    }

    public String getCollateral() {
        return collateral;
    }

    public void setCollateral(String collateral) {
        this.collateral = collateral;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFieldOfficer() {
        return fieldOfficer;
    }

    public void setFieldOfficer(String fieldOfficer) {
        this.fieldOfficer = fieldOfficer;
    }
}
