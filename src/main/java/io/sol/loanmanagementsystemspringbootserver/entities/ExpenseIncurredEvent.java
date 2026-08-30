package io.sol.loanmanagementsystemspringbootserver.entities;

import java.math.BigDecimal;
import java.util.UUID;

// Emitted by ExpenseService management modules
public record ExpenseIncurredEvent(long expenseId, BigDecimal amount) {}
