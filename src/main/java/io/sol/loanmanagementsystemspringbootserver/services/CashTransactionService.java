package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransaction;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransactionType;
import io.sol.loanmanagementsystemspringbootserver.events.*;
import io.sol.loanmanagementsystemspringbootserver.repositories.CashTransactionRepository;
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
    private final ApplicationEventPublisher eventPublisher;
    private final UserSession userSession;
    private final AuditService auditService;

    @Transactional
    public Result<CashTransaction> recordTransaction(CashTransaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.invalid("Amount must be greater than zero", null);
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
        }

        return Result.success("Transaction recorded successfully", saved);
    }

    public List<CashTransaction> getAllTransactions() {
        return repository.findAll();
    }

    @Transactional
    public Result<Void> deleteTransaction(Long id) {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        if (!repository.existsById(id)) return Result.notFound("Cash transaction not found", null);
        repository.deleteById(id);
        auditService.record("SOFT_DELETE", "CASH_TRANSACTION", id, "Cash transaction soft deleted");
        return Result.success("Cash transaction deleted", null);
    }
}
