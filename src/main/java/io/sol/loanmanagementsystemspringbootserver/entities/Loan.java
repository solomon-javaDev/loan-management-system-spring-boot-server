package io.sol.loanmanagementsystemspringbootserver.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = true)
    private LocalDate fullPaidDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principal;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = true)
    private int tenor;

    @Column(nullable = false)
    private String collateral;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fees;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal processingFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingPrincipal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingInterest = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmountDue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee fieldOfficer;

    @ManyToOne
    @JoinColumn(name = "guarantor_id")
    private Guarantor guarantor;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanInstallment> installments = new ArrayList<>();

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Repayment> repayments = new ArrayList<>();

    public Loan() {
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Loan(LocalDate startDate, LocalDate maturityDate, LocalDate fullPaidDate, BigDecimal principal, BigDecimal interestRate, int tenor, String collateral, BigDecimal fees) {
        this.startDate = startDate;
        this.maturityDate = maturityDate;
        this.fullPaidDate = fullPaidDate;
        this.principal = principal;
        this.interestRate = interestRate;
        this.tenor = tenor;
        this.collateral = collateral;
        this.fees = fees;
    }

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

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public void setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
    }

    public BigDecimal getOutstandingPrincipal() {
        return outstandingPrincipal;
    }

    public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) {
        this.outstandingPrincipal = outstandingPrincipal;
    }

    public BigDecimal getOutstandingInterest() {
        return outstandingInterest;
    }

    public void setOutstandingInterest(BigDecimal outstandingInterest) {
        this.outstandingInterest = outstandingInterest;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getTotalAmountDue() {
        return totalAmountDue;
    }

    public void setTotalAmountDue(BigDecimal totalAmountDue) {
        this.totalAmountDue = totalAmountDue;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public Employee getFieldOfficer() {
        return fieldOfficer;
    }

    public void setFieldOfficer(Employee fieldOfficer) {
        this.fieldOfficer = fieldOfficer;
    }

    public Guarantor getGuarantor() {
        return guarantor;
    }

    public void setGuarantor(Guarantor guarantor) {
        this.guarantor = guarantor;
    }

    public List<LoanInstallment> getInstallments() {
        return installments;
    }

    public void setInstallments(List<LoanInstallment> installments) {
        this.installments = installments;
    }

    public List<Repayment> getRepayments() {
        return repayments;
    }

    public void setRepayments(List<Repayment> repayments) {
        this.repayments = repayments;
    }
}
