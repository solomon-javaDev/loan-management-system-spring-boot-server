package io.sol.loanmanagementsystemspringbootserver.events;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SavingsTransactionEvent(
    Integer customerId,
    BigDecimal amount,
    boolean isDeposit,
    LocalDate date
) {}
