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

    @Column(nullable = false)
    private BigDecimal principal;


    @Column(nullable = false)
    private BigDecimal interestRate;

    @Column(nullable = true)
    private int tenor;

    @Column(nullable = false)
    private String collateral;

    @Column(nullable = false)
    private BigDecimal fees;

    @Column(nullable = false)
    private BigDecimal surchargeRate = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal surchargeAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee fieldOfficer;

    @ManyToOne
    @JoinColumn(name = "guarantor_id")
    private Guarantor guarantor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Payment> payments = new ArrayList<>();

    @Column(nullable = true)
    private BigDecimal fullPayment;

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

    public BigDecimal getDisbursedAmount(){
        return principal.subtract(this.fees);
    }

    public BigDecimal getTotalPaid(){
        return payments.stream()
                .map(Payment::getAmountReceived)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @PrePersist
    @PreUpdate
    public void calculateFullpayment(){
        if(this.principal != null || this.interestRate != null){
            BigDecimal interestAmount = this.principal.multiply(this.interestRate);
            this.fullPayment = this.principal.add(interestAmount);
        }
        else {
            this.fullPayment = BigDecimal.ZERO;
        }
    }
    public BigDecimal getOutstandingBalance(){
        return getTotalDue().subtract(getTotalPaid());
    }

    public BigDecimal getTotalDue(){
        return this.principal.add(this.principal.multiply(this.interestRate)).add(getSurchargeAmount());
    }

    public BigDecimal calculateSurcharge(LocalDate asAt) {
        if (asAt == null || maturityDate == null || !asAt.isAfter(maturityDate)) {
            return BigDecimal.ZERO;
        }
        long daysLate = java.time.temporal.ChronoUnit.DAYS.between(maturityDate, asAt);
        return principal.multiply(surchargeRate).multiply(BigDecimal.valueOf(daysLate));
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public void addPayment(Payment payment) {
        if (payment == null) {
            return;
        }
        payments.add(payment);
        payment.setLoan(this);
    }

    public void removePayment(Payment payment) {
        if (payment == null) {
            return;
        }
        payments.remove(payment);
        if (payment.getLoan() == this) {
            payment.setLoan(null);
        }
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

    public BigDecimal getSurchargeRate() {
        return surchargeRate;
    }

    public void setSurchargeRate(BigDecimal surchargeRate) {
        this.surchargeRate = surchargeRate == null ? BigDecimal.ZERO : surchargeRate;
    }

    public BigDecimal getSurchargeAmount() {
        return surchargeAmount == null ? BigDecimal.ZERO : surchargeAmount;
    }

    public void setSurchargeAmount(BigDecimal surchargeAmount) {
        this.surchargeAmount = surchargeAmount == null ? BigDecimal.ZERO : surchargeAmount;
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

    public BigDecimal getFullPayment() {
        return fullPayment;
    }



    public String getReference() {
        String statusPart = status != null ? status.toString() : "";
        String principalPart = principal != null ? principal.toString() : "";
        String customerPart = customer != null && customer.getLastName() != null ? customer.getLastName() : "";
        return String.join("-", String.valueOf(id), statusPart, principalPart, customerPart);
    }

}
