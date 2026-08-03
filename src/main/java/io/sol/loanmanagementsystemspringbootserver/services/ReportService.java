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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final LoansRepository loansRepository;
    private final PaymentRepository paymentRepository;

    public ReportService(LoansRepository loansRepository, PaymentRepository paymentRepository) {
        this.loansRepository = loansRepository;
        this.paymentRepository = paymentRepository;
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
                        Collectors.averagingDouble(l -> l.getTotalPaid().doubleValue() / l.getTotalDue().doubleValue())
                ));
    }
}
