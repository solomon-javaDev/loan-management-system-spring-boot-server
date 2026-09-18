package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransaction;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransactionType;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.Customer;
import io.sol.loanmanagementsystemspringbootserver.events.*;
import io.sol.loanmanagementsystemspringbootserver.repositories.CashTransactionRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CashTransactionService {

    private final CashTransactionRepository repository;
    private final CustomerRepository customerRepository;
    private final LoansRepository loansRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserSession userSession;
    private final AuditService auditService;
    private final FinancialStateService financialStateService;

    @Transactional
    public Result<CashTransaction> recordTransaction(CashTransaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.invalid("Amount must be greater than zero", null);
        }

        if (transaction.getType() == CashTransactionType.BANK_DEPOSIT
                && transaction.getAmount().compareTo(financialStateService.getCurrentState().getCashOnHand()) > 0) {
            return Result.invalid("Bank deposit exceeds available cash on hand", null);
        }
        if (transaction.getType() == CashTransactionType.BANK_WITHDRAWAL
                && transaction.getAmount().compareTo(financialStateService.getCurrentState().getBankBalance()) > 0) {
            return Result.invalid("Bank withdrawal exceeds the bank balance", null);
        }
        if (transaction.getType() == CashTransactionType.CAPITAL_WITHDRAWAL
                && transaction.getAmount().compareTo(financialStateService.getCurrentState().getCashOnHand()) > 0) {
            return Result.invalid("Capital withdrawal exceeds available cash on hand", null);
        }
        if (transaction.getType() == CashTransactionType.SAVINGS_WITHDRAWAL
                && transaction.getAmount().compareTo(financialStateService.getCurrentState().getCashOnHand()) > 0) {
            return Result.invalid("Savings withdrawal exceeds available cash on hand", null);
        }

        if (transaction.getType() == CashTransactionType.SAVINGS_DEPOSIT
                || transaction.getType() == CashTransactionType.SAVINGS_WITHDRAWAL) {
            Result<Void> savingsValidation = validateSavingsTransaction(transaction);
            if (savingsValidation.isFailure()) {
                return Result.invalid(savingsValidation.message(), null);
            }
        }

        CashTransaction saved = repository.save(transaction);
        auditService.record("CREATE", "CASH_TRANSACTION", saved.getId(), "Cash transaction recorded");
        LocalDate date = saved.getDate().toLocalDate();

        switch (saved.getType()) {
            case CAPITAL_INJECTION -> eventPublisher.publishEvent(new CapitalTransactionEvent(saved.getAmount(), true, date));
            case CAPITAL_WITHDRAWAL -> eventPublisher.publishEvent(new CapitalTransactionEvent(saved.getAmount(), false, date));
            case BANK_DEPOSIT -> eventPublisher.publishEvent(new BankTransactionEvent(saved.getAmount(), true, date));
            case BANK_WITHDRAWAL -> eventPublisher.publishEvent(new BankTransactionEvent(saved.getAmount(), false, date));
            case SAVINGS_DEPOSIT -> eventPublisher.publishEvent(new SavingsTransactionEvent(saved.getCustomerId(), saved.getAmount(), true, date));
            case SAVINGS_WITHDRAWAL -> eventPublisher.publishEvent(new SavingsTransactionEvent(saved.getCustomerId(), saved.getAmount(), false, date));
            case REPAYMENT_RECEIVED, EXPENSE_PAID -> { }
        }

        return Result.success("Transaction recorded successfully", saved);
    }

    public boolean hasOutstandingLoan(int customerId) {
        return loansRepository.existsByCustomerIdAndStatusIn(customerId,
                List.of(LoanStatus.PENDING, LoanStatus.ACTIVE, LoanStatus.DEFAULTED));
    }

    private Result<Void> validateSavingsTransaction(CashTransaction transaction) {
        if (transaction.getCustomerId() == null) {
            return Result.invalid("A customer is required for a savings transaction", null);
        }

        Customer customer = customerRepository.findById(transaction.getCustomerId()).orElse(null);
        if (customer == null) {
            return Result.notFound("Customer not found", null);
        }

        boolean outstandingLoan = hasOutstandingLoan(customer.getId());
        if (outstandingLoan && !userSession.isAdmin()) {
            return Result.forbidden("A customer with an uncleared loan cannot make savings transactions. An administrator must process this transaction.", null);
        }

        BigDecimal currentBalance = customer.getSavingsBalance() == null
                ? BigDecimal.ZERO : customer.getSavingsBalance();
        if (transaction.getType() == CashTransactionType.SAVINGS_WITHDRAWAL
                && transaction.getAmount().compareTo(currentBalance) > 0) {
            return Result.invalid("Withdrawal exceeds the customer's savings balance", null);
        }

        BigDecimal updatedBalance = transaction.getType() == CashTransactionType.SAVINGS_DEPOSIT
                ? currentBalance.add(transaction.getAmount())
                : currentBalance.subtract(transaction.getAmount());
        customer.setSavingsBalance(updatedBalance);
        customerRepository.save(customer);
        transaction.setRecordedBy(userSession.getCurrentUser());
        return Result.success("Savings transaction is valid", null);
    }

    public List<CashTransaction> getAllTransactions() {
        return repository.findAll();
    }

    @Transactional
    public Result<Void> deleteTransaction(Long id) {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        CashTransaction transaction = repository.findById(id).orElse(null);
        if (transaction == null) return Result.notFound("Cash transaction not found", null);
        if (transaction.getType() == CashTransactionType.SAVINGS_DEPOSIT
            || transaction.getType() == CashTransactionType.SAVINGS_WITHDRAWAL) {
            Customer customer = transaction.getCustomerId() == null ? null
                : customerRepository.findById(transaction.getCustomerId()).orElse(null);
            if (customer != null) {
            BigDecimal balance = customer.getSavingsBalance() == null ? BigDecimal.ZERO : customer.getSavingsBalance();
            BigDecimal corrected = transaction.getType() == CashTransactionType.SAVINGS_DEPOSIT
                ? balance.subtract(transaction.getAmount()) : balance.add(transaction.getAmount());
            if (corrected.compareTo(BigDecimal.ZERO) < 0) {
                return Result.invalid("This transaction cannot be deleted because the customer balance has already been withdrawn.", null);
            }
            customer.setSavingsBalance(corrected);
            customerRepository.save(customer);
            eventPublisher.publishEvent(new SavingsTransactionEvent(customer.getId(), transaction.getAmount(),
                transaction.getType() == CashTransactionType.SAVINGS_WITHDRAWAL,
                transaction.getDate().toLocalDate()));
            }
        }
        repository.deleteById(id);
        auditService.record("SOFT_DELETE", "CASH_TRANSACTION", id, "Cash transaction soft deleted");
        return Result.success("Cash transaction deleted", null);
    }
}
