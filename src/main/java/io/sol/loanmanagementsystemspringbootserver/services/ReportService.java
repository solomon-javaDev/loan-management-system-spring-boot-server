package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.PaymentDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.*;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.PaymentRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.ExpenseRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.CashTransactionRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.SavingsTransactionRepository;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailDetails;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class ReportService {

    private final LoansRepository loansRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final CashTransactionRepository cashTransactionRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;
    private final EmailsService emailsService;
    private final SystemSettingService settingService;

    public ReportService(LoansRepository loansRepository, PaymentRepository paymentRepository, 
                         ExpenseRepository expenseRepository,
                         CashTransactionRepository cashTransactionRepository,
                         SavingsTransactionRepository savingsTransactionRepository,
                         EmailsService emailsService, SystemSettingService settingService) {
        this.loansRepository = loansRepository;
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
        this.cashTransactionRepository = cashTransactionRepository;
        this.savingsTransactionRepository = savingsTransactionRepository;
        this.emailsService = emailsService;
        this.settingService = settingService;
    }

    public String sendDailyReport(LocalDate date) {
        String recipientStr = settingService.getSetting("report.emails", "");
        if (recipientStr.isBlank()) {
            return "No recipient emails configured.";
        }

        Map<String, Object> data = compileDailyReportData(date);
        String reportText = formatReportAsText(data);

        String[] recipients = recipientStr.split("[,;]");
        StringBuilder results = new StringBuilder();
        for (String recipient : recipients) {
            EmailDetails details = new EmailDetails();
            details.setRecipient(recipient.trim());
            details.setSubject("Daily Business Report - " + date);
            details.setBody(reportText);
            results.append(emailsService.sendSimpleMail(details)).append(" (").append(recipient.trim()).append("); ");
        }

        return results.toString();
    }

    private String formatReportAsText(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Daily Performance Report\n");
        sb.append("Date: ").append(data.get("date")).append("\n\n");

        sb.append("--- Daily Cash Summary ---\n");
        Map<String, Object> dailyCash = (Map<String, Object>) data.get("dailyCash");
        dailyCash.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));

        sb.append("\n--- Aging Analysis ---\n");
        Map<String, BigDecimal> aging = (Map<String, BigDecimal>) data.get("aging");
        aging.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));

        sb.append("\n--- Employee Performance (Work Rate) ---\n");
        Map<String, Double> perf = (Map<String, Double>) data.get("employeePerformance");
        perf.forEach((k, v) -> sb.append(k).append(": ").append(String.format("%.2f%%", v * 100)).append("\n"));

        sb.append("\n--- Loans Disbursed Today ---\n");
        sb.append("Total Amount Disbursed: ").append(data.get("totalDisbursed")).append("\n");
        List<LoanDTO> loans = (List<LoanDTO>) data.get("todayLoans");
        sb.append("Number of Loans: ").append(loans.size()).append("\n");

        sb.append("\n--- Customers Due Today ---\n");
        List<Loan> dueLoans = (List<Loan>) data.get("dueLoans");
        if (dueLoans.isEmpty()) {
            sb.append("No customers are due today.\n");
        } else {
            dueLoans.forEach(loan -> sb.append(loan.getCustomer().getCustomerName())
                    .append(" | ").append(loan.getCustomer().getTelephone())
                    .append(" | Balance: ").append(loan.getOutstandingBalance())
                     .append(" | Aging days: ").append(loan.getAgingDays((LocalDate) data.get("date")))
                    .append("\n"));
        }

        sb.append("\n--- Expenses ---\n");
        sb.append("Total Expenses: ").append(data.get("totalExpenses")).append("\n");

        return sb.toString();
    }

    public Map<String, Object> getDailyCashReport(LocalDate date) {
        List<Payment> dailyPayments = paymentRepository.findByDate(date);
        BigDecimal totalTransactions = dailyPayments.stream()
                .map(Payment::getAmountReceived)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(23, 59, 59);
        List<io.sol.loanmanagementsystemspringbootserver.entities.CashTransaction> cashTransactions =
            cashTransactionRepository.findByDateBetween(dayStart, dayEnd);
        BigDecimal cashIn = cashTransactions.stream()
            .filter(t -> t.getType() == CashTransactionType.CAPITAL_INJECTION
                    || t.getType() == CashTransactionType.LOAN_COLLECTION
                    || t.getType() == CashTransactionType.SAVINGS_DEPOSIT)
            .map(io.sol.loanmanagementsystemspringbootserver.entities.CashTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cashOut = cashTransactions.stream()
            .filter(t -> t.getType() == CashTransactionType.BANK_DEPOSIT
                    || t.getType() == CashTransactionType.EXPENSE)
            .map(io.sol.loanmanagementsystemspringbootserver.entities.CashTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenses = expenseRepository.findByDateBetween(dayStart, dayEnd).stream()
            .map(io.sol.loanmanagementsystemspringbootserver.entities.Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new HashMap<>();
        report.put("date", date);
        report.put("openingBalance", getCashMovementBefore(dayStart));
        report.put("totalTransactions", totalTransactions);
        report.put("cashInput", cashIn);
        report.put("cashOut", cashOut);
        report.put("expenses", expenses);
        report.put("closingBalance", getCashMovementBefore(dayStart)
            .add(totalTransactions).add(cashIn).subtract(cashOut).subtract(expenses));
        return report;
    }

        private BigDecimal getCashMovementBefore(LocalDateTime dateTime) {
        BigDecimal payments = paymentRepository.findAll().stream()
            .filter(payment -> payment.getDate() != null && payment.getDate().isBefore(dateTime.toLocalDate()))
            .map(Payment::getAmountReceived)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<io.sol.loanmanagementsystemspringbootserver.entities.CashTransaction> cash =
            cashTransactionRepository.findAll().stream()
                .filter(transaction -> transaction.getDate() != null && transaction.getDate().isBefore(dateTime))
                .toList();
        BigDecimal cashMovement = cash.stream()
            .map(t -> (t.getType() == CashTransactionType.BANK_DEPOSIT || t.getType() == CashTransactionType.EXPENSE)
                    ? t.getAmount().negate() : t.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenses = expenseRepository.findAll().stream()
            .filter(expense -> expense.getDate() != null && expense.getDate().isBefore(dateTime))
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return payments.add(cashMovement).subtract(expenses);
        }

    public Map<String, Object> getLoanPortfolioReport() {
        List<Loan> allLoans = loansRepository.findAll();
        Map<String, Long> totalsByStatus = allLoans.stream()
                .collect(Collectors.groupingBy(l -> l.getStatus().name(), Collectors.counting()));
        
        BigDecimal totalActiveValue = allLoans.stream()
                .filter(l -> l.getStatus().name().equals("ACTIVE"))
                .map(Loan::getPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new HashMap<>();
        report.put("totalLoans", allLoans.size());
        report.put("totalsByStatus", totalsByStatus);
        report.put("totalActiveValue", totalActiveValue);
        return report;
    }

    public List<PaymentDTO> getRepaymentReport(LocalDate start, LocalDate end) {
        return paymentRepository.findByDateBetween(start, end).stream()
                .map(DTOMapper::toDTO).collect(Collectors.toList());
    }

    public Map<String, BigDecimal> getCollectionsPerCustomer(LocalDate start, LocalDate end) {
        List<Payment> payments = paymentRepository.findByDateBetween(start, end);
        return payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getLoan().getCustomer().getCustomerName(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmountReceived, BigDecimal::add)
                ));
    }

    public Map<LocalDate, BigDecimal> getCollectionsPerDay(LocalDate start, LocalDate end) {
        List<Payment> payments = paymentRepository.findByDateBetween(start, end);
        return payments.stream()
                .collect(Collectors.groupingBy(
                        Payment::getDate,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmountReceived, BigDecimal::add)
                ));
    }

    public Map<String, BigDecimal> getCollectionsPerMonth(LocalDate start, LocalDate end) {
        List<Payment> payments = paymentRepository.findByDateBetween(start, end);
        return payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getDate().getMonth().name() + " " + p.getDate().getYear(),
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmountReceived, BigDecimal::add)
                ));
    }

    public Map<String, BigDecimal> getAgingAnalysis() {
        return getAgingAnalysis(LocalDate.now());
    }

    public Map<String, BigDecimal> getAgingAnalysis(LocalDate asAt) {
        List<Loan> activeLoans = loansRepository.findAll().stream()
                .filter(l -> l.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        BigDecimal bucket30 = BigDecimal.ZERO;
        BigDecimal bucket60 = BigDecimal.ZERO;
        BigDecimal bucket90 = BigDecimal.ZERO;

        for (Loan loan : activeLoans) {
            long aging = loan.getAgingDays(asAt);
            if (aging > 90) bucket90 = bucket90.add(loan.getOutstandingBalance());
            else if (aging > 60) bucket60 = bucket60.add(loan.getOutstandingBalance());
            else if (aging > 30) bucket30 = bucket30.add(loan.getOutstandingBalance());
        }

        Map<String, BigDecimal> report = new HashMap<>();
        report.put("30_days", bucket30);
        report.put("60_days", bucket60);
        report.put("90_plus_days", bucket90);
        return report;
    }

    public List<Loan> getLoansDueOn(LocalDate date) {
        return loansRepository.findAll().stream()
                .filter(loan -> loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
                .filter(loan -> date.equals(loan.getMaturityDate()))
                .toList();
    }

    public Map<String, Double> getFieldOfficerWorkRate() {
        List<Loan> allLoans = loansRepository.findAll();
        return allLoans.stream()
                .filter(l -> l.getFieldOfficer() != null)
                .collect(Collectors.groupingBy(
                        l -> l.getFieldOfficer().getFirstName() + " " + l.getFieldOfficer().getLastName(),
                        Collectors.averagingDouble(l -> {
                            BigDecimal due = l.getTotalDue();
                            if (due == null || due.compareTo(BigDecimal.ZERO) == 0) return 0.0;
                            return l.getTotalPaid().doubleValue() / due.doubleValue();
                        })
                ));
    }

    public Map<String, Object> compileDailyReportData(LocalDate date) {
        Map<String, Object> data = new HashMap<>();
        data.put("date", date);
        
        // 1. Daily Performance
        data.put("dailyCash", getDailyCashReport(date));
        
        // 2. Aging Analysis
        data.put("aging", getAgingAnalysis(date));
        data.put("dueLoans", getLoansDueOn(date));
        
        // 3. Employee Performance
        data.put("employeePerformance", getFieldOfficerWorkRate());
        
        // 4. Loans Given Out (Today)
        List<Loan> todayLoans = loansRepository.findAll().stream()
                .filter(l -> date.equals(l.getStartDate()))
                .toList();
        data.put("todayLoans", todayLoans.stream().map(DTOMapper::toDTO).collect(Collectors.toList()));
        
        BigDecimal totalDisbursed = todayLoans.stream()
                .map(Loan::getPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalDisbursed", totalDisbursed);

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(23, 59, 59);
        data.put("totalExpenses", expenseRepository.findByDateBetween(dayStart, dayEnd).stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return data;
    }
    public void sendMonthlyStatement() {
        LocalDate start = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        String recipient = settingService.getSetting("admin.email", "admin@company.com");

        StringBuilder content = new StringBuilder("Monthly Business Statement (" + start + " to " + end + ")\n\n");

        BigDecimal totalIncome = paymentRepository.findByDateBetween(start, end).stream()
                .map(Payment::getAmountReceived)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(23, 59, 59);
        BigDecimal savingsDeposits = savingsTransactionRepository.findByDateBetween(startTime, endTime).stream()
            .filter(t -> t.getType() == SavingsTransactionType.DEPOSIT)
            .map(SavingsTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal savingsWithdrawals = savingsTransactionRepository.findByDateBetween(startTime, endTime).stream()
            .filter(t -> t.getType() == SavingsTransactionType.WITHDRAWAL)
            .map(SavingsTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cashInput = cashTransactionRepository.findByDateBetween(startTime, endTime).stream()
            .filter(t -> t.getType() != CashTransactionType.EXPENSE)
            .map(CashTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = expenseRepository.findByDateBetween(start.atStartOfDay(), end.atTime(23, 59, 59)).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        content.append("Money Input (Repayments): ").append(totalIncome).append("\n");
        content.append("Money Input (Cash/Capital): ").append(cashInput).append("\n");
        content.append("Savings Deposits: ").append(savingsDeposits).append("\n");
        content.append("Savings Withdrawals: ").append(savingsWithdrawals).append("\n");
        content.append("Money Spent (Expenses): ").append(totalExpenses).append("\n");
        content.append("Net Cash Flow: ").append(totalIncome.subtract(totalExpenses)).append("\n\n");

        content.append("Aging Analysis:\n");
        Map<String, BigDecimal> aging = getAgingAnalysis(LocalDate.now());
        aging.forEach((k, v) -> content.append(k).append(": ").append(v).append("\n"));

        emailsService.sendSimpleMail(new EmailDetails(recipient, "Monthly Business Statement", content.toString(), null));
    }

    @Scheduled(cron = "0 30 7 * * ?") // 7:30 AM daily
    public void sendDailyAgingAndPayments() {
        LocalDate today = LocalDate.now();
        String recipient = settingService.getSetting("admin.email", "admin@company.com");

        StringBuilder content = new StringBuilder("Daily Collections & Aging Report - " + today + "\n\n");
        
        content.append("Customers supposed to pay today:\n");
        List<Loan> dueToday = getLoansDueOn(today);
        if (dueToday.isEmpty()) {
            content.append("None\n");
        } else {
            dueToday.forEach(l -> content.append("- ").append(l.getCustomer().getCustomerName())
                    .append(" (").append(l.getOutstandingBalance()).append(")\n"));
        }

        content.append("\nAging Analysis Summary:\n");
        Map<String, BigDecimal> aging = getAgingAnalysis(today);
        aging.forEach((k, v) -> content.append(k).append(": ").append(v).append("\n"));

        emailsService.sendSimpleMail(new EmailDetails(recipient, "Daily Aging & Collections Report", content.toString(), null));
    }

    @Scheduled(cron = "0 0 8 * * ?") // 8:00 AM daily
    public void sendDailyLoanStatementsToAdmin() {
        String recipient = settingService.getSetting("admin.email", "admin@company.com");
        List<Loan> allActive = loansRepository.findByStatus(io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus.ACTIVE);
        
        StringBuilder content = new StringBuilder("Daily Customer Loan Statements - " + LocalDate.now() + "\n\n");
        for (Loan loan : allActive) {
            content.append("Customer: ").append(loan.getCustomer().getCustomerName()).append("\n");
            content.append("Principal: ").append(loan.getPrincipal()).append("\n");
            content.append("Balance: ").append(loan.getOutstandingBalance()).append("\n");
            content.append("Status: ").append(loan.getStatus()).append("\n");
            content.append("-----------------------------------\n");
        }
        
        emailsService.sendSimpleMail(new EmailDetails(recipient, "Daily Loan Statements", content.toString(), null));
    }
}
