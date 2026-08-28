package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.CashTransaction;
import io.sol.loanmanagementsystemspringbootserver.repositories.CashTransactionRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CashTransactionService {
    private final CashTransactionRepository repository;

    public CashTransactionService(CashTransactionRepository repository) {
        this.repository = repository;
    }

    public Result<CashTransaction> record(CashTransaction transaction) {
        if (transaction == null || transaction.getType() == null) {
            return Result.invalid("Cash transaction type is required", null);
        }
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.invalid("Amount must be greater than zero", null);
        }
        if (transaction.getDescription() == null || transaction.getDescription().isBlank()) {
            return Result.invalid("Description is required", null);
        }
        return Result.success("Cash transaction recorded", repository.save(transaction));
    }

    public Result<List<CashTransaction>> getTransactions(LocalDateTime start, LocalDateTime end) {
        return Result.success("Cash transactions retrieved", repository.findByDateBetween(start, end));
    }
}
