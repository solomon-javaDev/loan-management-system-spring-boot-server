package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.config.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.*;
import io.sol.loanmanagementsystemspringbootserver.repositories.CashSessionRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.CashTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CashService {

    private final CashSessionRepository cashSessionRepository;
    private final CashTransactionRepository cashTransactionRepository;

    public CashService(CashSessionRepository cashSessionRepository, CashTransactionRepository cashTransactionRepository) {
        this.cashSessionRepository = cashSessionRepository;
        this.cashTransactionRepository = cashTransactionRepository;
    }

    @Transactional
    public Result<CashSession> openDay(BigDecimal openingBalance, User cashier) {
        CashSession session = new CashSession();
        session.setOpeningBalance(openingBalance);
        session.setClosingBalance(openingBalance);
        session.setDate(LocalDate.now());
        session.setCashier(cashier);
        session.setStatus(CashSessionStatus.OPEN);
        return Result.success("Cash session opened.", cashSessionRepository.save(session));
    }

    @Transactional
    public Result<CashTransaction> recordTransaction(CashTransaction transaction) {
        if (transaction == null || transaction.getAmount() == null) {
            return Result.invalid("A valid transaction is required.", null);
        }
        return Result.success("Transaction recorded.", cashTransactionRepository.save(transaction));
    }

    @Transactional
    public Result<CashSession> closeDay(Integer sessionId) {
        if (sessionId == null) {
            return Result.invalid("A valid session id is required.", null);
        }
        CashSession session = cashSessionRepository.findById(sessionId).orElseThrow();
        session.setStatus(CashSessionStatus.CLOSED);
        session.setClosingBalance(calculateClosingBalance(session));
        return Result.success("Cash session closed.", cashSessionRepository.save(session));
    }

    public BigDecimal calculateClosingBalance(CashSession session) {
        if (session == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal balance = session.getOpeningBalance();
        List<CashTransaction> transactions = cashTransactionRepository.findAll();
        for (CashTransaction transaction : transactions) {
            if (transaction.getType() == CashTransactionType.CASH_IN || transaction.getType() == CashTransactionType.REPAYMENT_RECEIVED) {
                balance = balance.add(transaction.getAmount());
            } else if (transaction.getType() == CashTransactionType.CASH_OUT || transaction.getType() == CashTransactionType.BANK_DEPOSIT) {
                balance = balance.subtract(transaction.getAmount());
            }
        }
        return balance;
    }
}
