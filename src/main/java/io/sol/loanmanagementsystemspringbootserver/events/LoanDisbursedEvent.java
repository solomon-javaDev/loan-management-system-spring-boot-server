package io.sol.loanmanagementsystemspringbootserver.events;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanDisbursedEvent(
    long loanId,
    BigDecimal principalAmount,
    BigDecimal interestAmount,
    BigDecimal fees,
    LocalDate date
) {}
