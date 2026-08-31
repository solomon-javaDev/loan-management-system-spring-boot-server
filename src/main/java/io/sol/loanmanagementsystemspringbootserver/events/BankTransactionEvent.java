package io.sol.loanmanagementsystemspringbootserver.events;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BankTransactionEvent(
    BigDecimal amount,
    boolean isDeposit,
    LocalDate date
) {}
