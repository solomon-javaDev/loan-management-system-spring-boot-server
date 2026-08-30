package io.sol.loanmanagementsystemspringbootserver.entities;

import java.math.BigDecimal;
import java.util.UUID;

// Emitted by your existing LoanService when a loan is disbursed to a user
public record LoanDisbursedEvent(long loanId, BigDecimal principalAmount) {}
