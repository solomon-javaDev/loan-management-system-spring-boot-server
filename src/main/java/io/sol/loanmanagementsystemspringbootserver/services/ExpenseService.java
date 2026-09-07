package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Expense;
import io.sol.loanmanagementsystemspringbootserver.events.ExpenseIncurredEvent;
import io.sol.loanmanagementsystemspringbootserver.repositories.ExpenseRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UserSession;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserSession userSession;
    private final AuditService auditService;

    public ExpenseService(ExpenseRepository expenseRepository, ApplicationEventPublisher applicationEventPublisher,
                          UserSession userSession, AuditService auditService) {
        this.expenseRepository = expenseRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.userSession = userSession;
        this.auditService = auditService;
    }

    @Transactional
    public Result<Expense> recordExpense(Expense expense) {
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.invalid("Amount must be greater than zero", null);
        }
        if (expense.getDescription() == null || expense.getDescription().isBlank()) {
            return Result.invalid("Description is required", null);
        }
        if (expense.getCategory() == null) {
            return Result.invalid("Category is required", null);
        }

        Expense saved = expenseRepository.save(expense);
        auditService.record("CREATE", "EXPENSE", saved.getId(), "Expense recorded");
        
        applicationEventPublisher.publishEvent(new ExpenseIncurredEvent(
            saved.getId(),
            saved.getAmount(),
            saved.getDate()
        ));

        return Result.success("Expense recorded successfully", saved);
    }

    public Result<List<Expense>> getAllExpenses() {
        return Result.success("Expenses retrieved successfully", expenseRepository.findAll());
    }

    public Result<List<Expense>> getExpensesByDateRange(LocalDateTime start, LocalDateTime end) {
        return Result.success("Expenses retrieved successfully", expenseRepository.findByDateBetween(start, end));
    }

    public Result<BigDecimal> getTotalExpenses(LocalDateTime start, LocalDateTime end) {
        List<Expense> expenses = expenseRepository.findByDateBetween(start, end);
        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Result.success("Total expenses calculated", total);
    }

    @Transactional
    public Result<Void> deleteExpense(Long id) {
        Result<Void> authorization = userSession.requireAdmin();
        if (authorization.isFailure()) return Result.forbidden(authorization.message(), null);
        if (!expenseRepository.existsById(id)) return Result.notFound("Expense not found", null);
        Expense expense = expenseRepository.findById(id).orElse(null);
        if (expense == null) return Result.notFound("Expense not found", null);
        expenseRepository.deleteById(id);
        applicationEventPublisher.publishEvent(new ExpenseIncurredEvent(
            expense.getId(), expense.getAmount().negate(), expense.getDate()));
        auditService.record("SOFT_DELETE", "EXPENSE", id, "Expense soft deleted");
        return Result.success("Expense deleted", null);
    }
}
