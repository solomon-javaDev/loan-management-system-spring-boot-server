package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.config.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.*;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoanInstallmentRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.RepaymentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepaymentServiceTest {

    @Test
    void applyPaymentShouldBlockOverpayment() {
        LoansRepository loansRepository = Mockito.mock(LoansRepository.class);
        LoanInstallmentRepository installmentRepository = Mockito.mock(LoanInstallmentRepository.class);
        RepaymentRepository repaymentRepository = Mockito.mock(RepaymentRepository.class);

        Loan loan = new Loan();
        loan.setId(1);
        loan.setPrincipal(new BigDecimal("1000.00"));
        loan.setOutstandingPrincipal(new BigDecimal("100.00"));
        loan.setOutstandingInterest(new BigDecimal("10.00"));
        loan.setTotalPaid(BigDecimal.ZERO);
        loan.setStatus(LoanStatus.ACTIVE);

        LoanInstallment installment = new LoanInstallment();
        installment.setId(1);
        installment.setAmountDue(new BigDecimal("100.00"));
        installment.setPaidAmount(new BigDecimal("50.00"));
        installment.setStatus(LoanStatus.ACTIVE);

        Mockito.when(loansRepository.findById(1)).thenReturn(Optional.of(loan));
        Mockito.when(installmentRepository.findById(1)).thenReturn(Optional.of(installment));
        Mockito.when(repaymentRepository.save(Mockito.any(Repayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RepaymentService service = new RepaymentService(loansRepository, installmentRepository, repaymentRepository);
        Result<Repayment> result = service.applyPayment(1, 1, new BigDecimal("100.00"), "cashier");

        assertTrue(result.isFailure());
        assertEquals("Overpayment is not allowed for this installment.", result.message());
    }
}
