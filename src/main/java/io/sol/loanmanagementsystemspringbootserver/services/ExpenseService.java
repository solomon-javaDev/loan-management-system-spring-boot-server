package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Expense;
import io.sol.loanmanagementsystemspringbootserver.repositories.ExpenseRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

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
}
