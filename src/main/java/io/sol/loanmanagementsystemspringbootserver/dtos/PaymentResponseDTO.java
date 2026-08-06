package io.sol.loanmanagementsystemspringbootserver.dtos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentResponseDTO implements Serializable {
    private Integer id;
    private LocalDate date;
    private BigDecimal amountReceived;
    private Integer loanId;
    private String loanReference;
    private Integer customerId;
    private String customerName;

    public PaymentResponseDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getAmountReceived() { return amountReceived; }
    public void setAmountReceived(BigDecimal amountReceived) { this.amountReceived = amountReceived; }

    public Integer getLoanId() { return loanId; }
    public void setLoanId(Integer loanId) { this.loanId = loanId; }

    public String getLoanReference() { return loanReference; }
    public void setLoanReference(String loanReference) { this.loanReference = loanReference; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
