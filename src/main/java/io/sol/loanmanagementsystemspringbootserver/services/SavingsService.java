package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.entities.SavingsTransaction;
import io.sol.loanmanagementsystemspringbootserver.entities.SavingsTransactionType;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.SavingsTransactionRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SavingsService {
    private final SavingsTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    public SavingsService(SavingsTransactionRepository transactionRepository, CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Result<SavingsTransaction> recordTransaction(int customerId, SavingsTransactionType type,
                                                         BigDecimal amount, String reference) {
        if (type == null) return Result.invalid("Savings transaction type is required", null);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.invalid("Amount must be greater than zero", null);
        }

        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) return Result.notFound("Customer not found", null);

        BigDecimal balance = customer.getSavingsBalance() == null ? BigDecimal.ZERO : customer.getSavingsBalance();
        if (type == SavingsTransactionType.WITHDRAWAL && balance.compareTo(amount) < 0) {
            return Result.invalid("Insufficient savings balance", null);
        }

        customer.setSavingsBalance(type == SavingsTransactionType.DEPOSIT
                ? balance.add(amount) : balance.subtract(amount));
        customerRepository.save(customer);

        SavingsTransaction transaction = new SavingsTransaction();
        transaction.setCustomer(customer);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setReference(reference);
        return Result.success("Savings transaction recorded", transactionRepository.save(transaction));
    }

    public Result<List<SavingsTransaction>> getTransactions(LocalDateTime start, LocalDateTime end) {
        return Result.success("Savings transactions retrieved", transactionRepository.findByDateBetween(start, end));
    }
}
