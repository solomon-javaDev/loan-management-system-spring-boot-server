package io.sol.loanmanagementsystemspringbootserver.dtos;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class LoanResponseDTO implements Serializable {
    private int id;
    private LocalDate startDate;
    private LocalDate maturityDate;
    private LocalDate fullPaidDate;
    private BigDecimal fullPayment;
    private BigDecimal principal;
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

    public LoanResponseDTO() {}


}
