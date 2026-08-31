package io.sol.loanmanagementsystemspringbootserver.events;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CapitalTransactionEvent(
    BigDecimal amount,
    boolean isInjection,
    LocalDate date
) {}
