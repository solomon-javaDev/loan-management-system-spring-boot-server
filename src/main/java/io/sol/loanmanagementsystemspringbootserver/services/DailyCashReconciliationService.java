package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.entities.CashTransactionType;
import io.sol.loanmanagementsystemspringbootserver.entities.DailyCashPool;
import io.sol.loanmanagementsystemspringbootserver.repositories.CashTransactionRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.DailyCashPoolRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.ExpenseRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.PaymentRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.SavingsTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DailyCashReconciliationService {
    private final DailyCashPoolRepository dailyCashPoolRepository;
    private final PaymentRepository paymentRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final ExpenseRepository expenseRepository;
    private final LoansRepository loansRepository;
    private final CashTransactionRepository cashTransactionRepository;

    public DailyCashReconciliationService(DailyCashPoolRepository dailyCashPoolRepository,
                                         PaymentRepository paymentRepository,
                                         SavingsTransactionRepository savingsTransactionRepository,
                                         ExpenseRepository expenseRepository,
                                         LoansRepository loansRepository,
                                         CashTransactionRepository cashTransactionRepository) {
        this.dailyCashPoolRepository = dailyCashPoolRepository;
        this.paymentRepository = paymentRepository;
        this.savingsTransactionRepository = savingsTransactionRepository;
        this.expenseRepository = expenseRepository;
        this.loansRepository = loansRepository;
        this.cashTransactionRepository = cashTransactionRepository;
    }

    @Transactional
    public DailyCashPool reconcileDay(LocalDate businessDate) {
        if (businessDate == null) {
            businessDate = LocalDate.now();
        }

        BigDecimal openingBalance = dailyCashPoolRepository.findTopByBusinessDateBeforeOrderByBusinessDateDesc(businessDate)
                .map(DailyCashPool::getClosingBalance)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalCollections = paymentRepository.sumAmountReceivedByDate(businessDate);
        BigDecimal totalSavingsDeposits = savingsTransactionRepository.sumDepositsByDate(businessDate);
        BigDecimal totalDisbursements = loansRepository.sumPrincipalByStartDate(businessDate);
        BigDecimal totalExpenses = expenseRepository.sumAmountByDate(businessDate);

        LocalDateTime startOfDay = businessDate.atStartOfDay();
        LocalDateTime endOfNextDay = businessDate.plusDays(1).atStartOfDay();
        BigDecimal capitalIn = cashTransactionRepository.sumAmountByTypeAndDateBetween(CashTransactionType.CAPITAL_INJECTION, startOfDay, endOfNextDay);
        BigDecimal cashOut = cashTransactionRepository.sumAmountByTypeAndDateBetween(CashTransactionType.BANK_DEPOSIT, startOfDay, endOfNextDay);
        BigDecimal expenseOut = cashTransactionRepository.sumAmountByTypeAndDateBetween(CashTransactionType.EXPENSE, startOfDay, endOfNextDay);

        BigDecimal totalInflow = totalCollections.add(totalSavingsDeposits).add(capitalIn);
        BigDecimal totalOutflow = totalDisbursements.add(totalExpenses).add(cashOut).add(expenseOut);
        BigDecimal closingBalance = openingBalance.add(totalInflow).subtract(totalOutflow);

        DailyCashPool pool = dailyCashPoolRepository.findByBusinessDate(businessDate)
                .orElse(new DailyCashPool());

        pool.setBusinessDate(businessDate);
        pool.setOpeningBalance(openingBalance);
        pool.setTotalCollections(totalCollections);
        pool.setTotalSavingsDeposits(totalSavingsDeposits);
        pool.setTotalDisbursements(totalDisbursements);
        pool.setTotalExpenses(totalExpenses);
        pool.setClosingBalance(closingBalance);
        pool.setClosed(true);
        return dailyCashPoolRepository.save(pool);
    }
}
