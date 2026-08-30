package io.sol.loanmanagementsystemspringbootserver.entities;

import java.math.BigDecimal;
import java.util.UUID;

// Emitted by the PaymentService when a borrower pays back money
public record RepaymentReceivedEvent(long repaymentId, BigDecimal amount) {}
