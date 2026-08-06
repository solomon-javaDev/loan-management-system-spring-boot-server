package io.sol.loanmanagementsystemspringbootserver.dtos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentCreateDTO implements Serializable {
    private LocalDate date;
    private BigDecimal amountReceived;
    private Integer loanId;

    public PaymentCreateDTO() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getAmountReceived() { return amountReceived; }
    public void setAmountReceived(BigDecimal amountReceived) { this.amountReceived = amountReceived; }

    public Integer getLoanId() { return loanId; }
    public void setLoanId(Integer loanId) { this.loanId = loanId; }
}
