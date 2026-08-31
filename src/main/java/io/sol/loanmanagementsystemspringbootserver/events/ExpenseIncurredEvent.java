package io.sol.loanmanagementsystemspringbootserver.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseIncurredEvent(
    Long expenseId,
    BigDecimal amount,
    LocalDateTime date
) {}
