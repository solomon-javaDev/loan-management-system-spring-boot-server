package io.sol.loanmanagementsystemspringbootserver.utilities;

import java.math.BigDecimal;

public record CapitalSummaryView(
        BigDecimal cashAtHand, BigDecimal totalLoanPortfolio, BigDecimal interestEarned, BigDecimal totalWorkingCapital
) {
}
