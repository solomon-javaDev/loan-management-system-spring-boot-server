package io.sol.loanmanagementsystemspringbootserver.events;

import io.sol.loanmanagementsystemspringbootserver.services.FinancialStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FinancialStateEventsListener {

    private final FinancialStateService financialStateService;

    @EventListener
    public void handleLoanDisbursed(LoanDisbursedEvent event) {
        financialStateService.applyLoanDisbursement(
            event.principalAmount(),
            event.interestAmount(),
            event.fees()
        );
    }

    @EventListener
    public void handleRepaymentReceived(RepaymentReceivedEvent event) {
        financialStateService.applyRepayment(
            event.principalPortion(),
            event.interestPortion(),
            event.feePortion(),
            event.surchargePortion()
        );
    }

    @EventListener
    public void handleExpenseIncurred(ExpenseIncurredEvent event) {
        financialStateService.applyExpense(event.amount());
    }

    @EventListener
    public void handleSavingsTransaction(SavingsTransactionEvent event) {
        financialStateService.applySavingsTransaction(event.amount(), event.isDeposit());
    }

    @EventListener
    public void handleCapitalTransaction(CapitalTransactionEvent event) {
        financialStateService.applyCapitalTransaction(event.amount(), event.isInjection());
    }

    @EventListener
    public void handleBankTransaction(BankTransactionEvent event) {
        financialStateService.applyBankTransaction(event.amount(), event.isDeposit());
    }
}
