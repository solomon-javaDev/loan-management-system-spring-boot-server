package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.CapitalAccount;
import io.sol.loanmanagementsystemspringbootserver.entities.ExpenseIncurredEvent;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanDisbursedEvent;
import io.sol.loanmanagementsystemspringbootserver.entities.RepaymentReceivedEvent;
import io.sol.loanmanagementsystemspringbootserver.repositories.CapitalAccountsRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CentralisedCapitalObserver {

    private static final UUID SYSTEM_ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final CapitalAccountsRepository capitalAccountsRepository;

    public CentralisedCapitalObserver(CapitalAccountsRepository capitalAccountsRepository) {
        this.capitalAccountsRepository = capitalAccountsRepository;
    }

    @EventListener
    @Transactional
    public void onLoanDisbursed(LoanDisbursedEvent event){
        CapitalAccount account = capitalAccountsRepository.findAndLockAccountById(SYSTEM_ACCOUNT_ID);
        account.recordLoanDisbursement(event.principalAmount());
        capitalAccountsRepository.save(account);
    }

    @EventListener
    @Transactional
    public void onPaymentReceived(RepaymentReceivedEvent event){
        CapitalAccount account =capitalAccountsRepository.findAndLockAccountById(SYSTEM_ACCOUNT_ID);
        account.processRepayment(event.amount());
        capitalAccountsRepository.save(account);
    }

    @EventListener
    @Transactional
    public void onExpenseOcurred(ExpenseIncurredEvent event){
        CapitalAccount account = capitalAccountsRepository.findAndLockAccountById(SYSTEM_ACCOUNT_ID);

        account.recordExpense(event.amount());
        capitalAccountsRepository.save(account);
    }
}
