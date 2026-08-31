package io.sol.loanmanagementsystemspringbootserver.dtos;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanCreateDTO implements Serializable {
    private LocalDate startDate;
    private LocalDate maturityDate;
    private BigDecimal principal;
    private BigDecimal interestRate;
    private int tenor;
    private String collateral;
    private BigDecimal fees;
    private LoanStatus status;
    private Integer fieldOfficerId;
    private Long guarantorId;
    private Integer customerId;

    public LoanCreateDTO() {}

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }

    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public int getTenor() { return tenor; }
    public void setTenor(int tenor) { this.tenor = tenor; }

    public String getCollateral() { return collateral; }
    public void setCollateral(String collateral) { this.collateral = collateral; }

    public BigDecimal getFees() { return fees; }
    public void setFees(BigDecimal fees) { this.fees = fees; }

    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    public Integer getFieldOfficerId() { return fieldOfficerId; }
    public void setFieldOfficerId(Integer fieldOfficerId) { this.fieldOfficerId = fieldOfficerId; }

    public Long getGuarantorId() { return guarantorId; }
    public void setGuarantorId(Long guarantorId) { this.guarantorId = guarantorId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
}
