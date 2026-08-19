package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.PaymentDTO;
import io.sol.loanmanagementsystemspringbootserver.mappers.DTOMapper;
import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.Payment;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.PaymentRepository;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailDetails;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class ReportService {

    private final LoansRepository loansRepository;
    private final PaymentRepository paymentRepository;
    private final EmailsService emailsService;
    private final SystemSettingService settingService;

    public ReportService(LoansRepository loansRepository, PaymentRepository paymentRepository, 
                         EmailsService emailsService, SystemSettingService settingService) {
        this.loansRepository = loansRepository;
        this.paymentRepository = paymentRepository;
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

        sb.append("\n--- Expenses ---\n");
        sb.append("Total Expenses: ").append(data.get("totalExpenses")).append("\n");

        return sb.toString();
    }

    public Map<String, Object> getDailyCashReport(LocalDate date) {
        List<Payment> dailyPayments = paymentRepository.findByDate(date);
        BigDecimal totalTransactions = dailyPayments.stream()
                .map(Payment::getAmountReceived)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new HashMap<>();
        report.put("date", date);
        report.put("openingBalance", BigDecimal.ZERO); // Placeholder
        report.put("totalTransactions", totalTransactions);
        report.put("closingBalance", totalTransactions); 
        return report;
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
        List<Loan> activeLoans = loansRepository.findAll().stream()
                .filter(l -> l.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        BigDecimal bucket30 = BigDecimal.ZERO;
        BigDecimal bucket60 = BigDecimal.ZERO;
        BigDecimal bucket90 = BigDecimal.ZERO;

        LocalDate now = LocalDate.now();
        for (Loan loan : activeLoans) {
            // Simple logic: if maturity date is passed, it's overdue
            if (loan.getMaturityDate().isBefore(now)) {
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(loan.getMaturityDate(), now);
                if (daysOverdue > 90) bucket90 = bucket90.add(loan.getOutstandingBalance());
                else if (daysOverdue > 60) bucket60 = bucket60.add(loan.getOutstandingBalance());
                else if (daysOverdue > 30) bucket30 = bucket30.add(loan.getOutstandingBalance());
            }
        }

        Map<String, BigDecimal> report = new HashMap<>();
        report.put("30_days", bucket30);
        report.put("60_days", bucket60);
        report.put("90_plus_days", bucket90);
        return report;
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
        data.put("aging", getAgingAnalysis());
        
        // 3. Employee Performance
        data.put("employeePerformance", getFieldOfficerWorkRate());
        
        // 4. Loans Given Out (Today)
        List<Loan> todayLoans = loansRepository.findAll().stream()
                .filter(l -> date.equals(l.getStartDate()))
                .collect(Collectors.toList());
        data.put("todayLoans", todayLoans.stream().map(DTOMapper::toDTO).collect(Collectors.toList()));
        
        BigDecimal totalDisbursed = todayLoans.stream()
                .map(Loan::getPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalDisbursed", totalDisbursed);

        // 5. Expenses (Placeholder as requested, but structured)
        // In a real system, we'd have an ExpenseRepository
        data.put("totalExpenses", BigDecimal.ZERO); 
        
        return data;
    }
}
