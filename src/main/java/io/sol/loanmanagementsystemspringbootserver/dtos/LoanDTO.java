package io.sol.loanmanagementsystemspringbootserver.dtos;

import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanDTO implements Serializable {
    private int id;
    private LocalDate startDate;
    private LocalDate maturityDate;
    private LocalDate fullPaidDate;
    private BigDecimal principal;
    private BigDecimal fullPayment;
    private BigDecimal interestRate;
    private int tenor;
    private String collateral;
    private BigDecimal fees;
    private BigDecimal surchargeRate;
    private BigDecimal surchargeAmount;
    private LoanStatus status;
    private Integer fieldOfficerId;
    private String fieldOfficerName;
    private Long guarantorId;
    private String guarantorName;
    private Integer customerId;
    private String customerName;
    private BigDecimal totalPaid;
    private BigDecimal outstandingBalance;
    private BigDecimal totalDue;
    private String reference;

    public LoanDTO() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }

    public LocalDate getFullPaidDate() { return fullPaidDate; }
    public void setFullPaidDate(LocalDate fullPaidDate) { this.fullPaidDate = fullPaidDate; }

    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }

    public BigDecimal getFullPayment() {
        return fullPayment;
    }

    public void setFullPayment(BigDecimal fullPayment) {
        this.fullPayment = fullPayment;
    }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public int getTenor() { return tenor; }
    public void setTenor(int tenor) { this.tenor = tenor; }

    public String getCollateral() { return collateral; }
    public void setCollateral(String collateral) { this.collateral = collateral; }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }

    public BigDecimal getSurchargeRate() { return surchargeRate; }
    public void setSurchargeRate(BigDecimal surchargeRate) { this.surchargeRate = surchargeRate; }

    public BigDecimal getSurchargeAmount() { return surchargeAmount; }
    public void setSurchargeAmount(BigDecimal surchargeAmount) { this.surchargeAmount = surchargeAmount; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public Integer getFieldOfficerId() { return fieldOfficerId; }
    public void setFieldOfficerId(Integer fieldOfficerId) { this.fieldOfficerId = fieldOfficerId; }

    public String getFieldOfficerName() { return fieldOfficerName; }
    public void setFieldOfficerName(String fieldOfficerName) { this.fieldOfficerName = fieldOfficerName; }

    public Long getGuarantorId() { return guarantorId; }
    public void setGuarantorId(Long guarantorId) { this.guarantorId = guarantorId; }

    public String getGuarantorName() { return guarantorName; }
    public void setGuarantorName(String guarantorName) { this.guarantorName = guarantorName; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public BigDecimal getTotalPaid() { return totalPaid; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }

    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }

    public BigDecimal getTotalDue() { return totalDue; }
    public void setTotalDue(BigDecimal totalDue) { this.totalDue = totalDue; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    @Override
    public String toString() {
        return reference != null ? reference : String.valueOf(id);
    }
}
