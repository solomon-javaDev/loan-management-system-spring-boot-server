package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanInstallment;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanCalculationServiceTest {

    @Test
    void generateScheduleShouldCreateFixedInstallments() {
        LoanCalculationService service = new LoanCalculationService();

        Loan loan = new Loan();
        loan.setPrincipal(new BigDecimal("1000.00"));
        loan.setInterestRate(new BigDecimal("12.00"));
        loan.setTenor(4);
        loan.setStartDate(LocalDate.of(2026, 1, 1));
        loan.setMaturityDate(LocalDate.of(2026, 5, 1));
        loan.setStatus(LoanStatus.ACTIVE);

        List<LoanInstallment> installments = service.generateSchedule(loan);

        assertEquals(4, installments.size());
        assertEquals(new BigDecimal("250.00"), installments.get(0).getPrincipalPortion());
        assertEquals(new BigDecimal("30.00"), installments.get(0).getInterestPortion());
        assertEquals(new BigDecimal("280.00"), installments.get(0).getAmountDue());
        assertEquals(new BigDecimal("120.00"), service.calculateTotalInterest(loan));
    }
}
