package io.sol.loanmanagementsystemspringbootserver.events;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepaymentReceivedEvent(
    Integer paymentId,
    Integer loanId,
    BigDecimal principalPortion,
    BigDecimal interestPortion,
    BigDecimal feePortion,
    BigDecimal surchargePortion,
    BigDecimal totalAmount,
    LocalDate date
) {}
