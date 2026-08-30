package io.sol.loanmanagementsystemspringbootserver.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Payment> payments = new ArrayList<>();

    @Column(nullable = true)
    private BigDecimal fullPayment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guarantor_customer_id", nullable = false)
    private Customer guarantor;



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


    /**
     * Aging days = number of scheduled daily installments the customer is behind.
     * Based on coverage: daysPaidFor = totalPaid / dailyInstallment, where
     * dailyInstallment = scheduledTotal / tenor. A larger-than-expected payment
     * therefore covers prior missed days and reduces aging.
     */
    public long getAgingDays(LocalDate asAt) {
        if (asAt == null) asAt = LocalDate.now();
        if (fullPaidDate != null || getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) return 0;
        if (startDate == null || tenor <= 0) return 0;

        LocalDate end = asAt.isBefore(maturityDate) ? asAt : maturityDate;
        if (end.isBefore(startDate)) return 0;

        long elapsedDays = ChronoUnit.DAYS.between(startDate, end) + 1; // inclusive of both ends

        BigDecimal ir = interestRate == null ? BigDecimal.ZERO : interestRate;
        BigDecimal scheduledTotal = principal.add(principal.multiply(ir));
        if (scheduledTotal.compareTo(BigDecimal.ZERO) <= 0) return 0;

        BigDecimal dailyInstallment = scheduledTotal.divide(BigDecimal.valueOf(tenor), 10, RoundingMode.DOWN);
        BigDecimal daysPaidFor = getTotalPaid().divide(dailyInstallment, 10, RoundingMode.DOWN);

        long aging = elapsedDays - daysPaidFor.longValue();
        return Math.max(aging, 0);
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



    public String getReference() {
        String statusPart = status != null ? status.toString() : "";
        String principalPart = principal != null ? principal.toString() : "";
        String customerPart = customer != null && customer.getLastName() != null ? customer.getLastName() : "";
        return String.join("-", String.valueOf(id), statusPart, principalPart, customerPart);
    }

}
