package io.sol.loanmanagementsystemspringbootserver.services;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransaction;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransactionType;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Expense;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Payment;
import io.sol.loanmanagementsystemspringbootserver.repositories.CashTransactionRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.ExpenseRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.PaymentRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.DocumentIdGenerator;
import io.sol.loanmanagementsystemspringbootserver.utilities.Logger;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final LoansRepository loansRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final CashTransactionRepository cashTransactionRepository;

    private void addHeader(Document document, String title, String documentId) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        document.add(new Paragraph(title).setBold().setFontSize(18));
        document.add(new Paragraph("Generated on: " + now.format(formatter)));
        document.add(new Paragraph("Document ID: " + documentId));
        document.add(new Paragraph("\n"));
    }

    private Table createTable(float... widths) {
        return new Table(UnitValue.createPercentArray(widths))
                .setWidth(UnitValue.createPercentValue(100))
                .setFixedLayout()
                .setFontSize(8);
    }

    public byte[] generateDailyLoanReport(LocalDate date) {
        List<Loan> loans = loansRepository.findByStartDate(date);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

                addHeader(document, "DAILY LOANS REPORT - " + date,
                    DocumentIdGenerator.report("DLR", date.toString()));

                Table table = createTable(3, 2, 2, 1, 2, 2, 2);
            table.addCell("Customer");
            table.addCell("Contact");
            table.addCell("Disbursed");
            table.addCell("Rate");
            table.addCell("Expected");
            table.addCell("Maturity");
            table.addCell("Officer");

            BigDecimal totalDisbursed = BigDecimal.ZERO;
            BigDecimal totalInterest = BigDecimal.ZERO;

            for (Loan l : loans) {
                table.addCell(l.getCustomer().getCustomerName());
                table.addCell(l.getCustomer().getTelephone());
                table.addCell(l.getDisbursedAmount().toPlainString());
                table.addCell(l.getInterestRate().multiply(BigDecimal.valueOf(100)) + "%");
                table.addCell(l.getFullPayment().toPlainString());
                table.addCell(l.getMaturityDate().toString());
                table.addCell(l.getFieldOfficer() != null ? l.getFieldOfficer().getUsername() : "N/A");

                totalDisbursed = totalDisbursed.add(l.getDisbursedAmount());
                totalInterest = totalInterest.add(l.getFullPayment().subtract(l.getPrincipal()));
            }
            document.add(table);
            document.add(new Paragraph("\nTotal Disbursed: " + totalDisbursed));
            document.add(new Paragraph("Total Expected Interest: " + totalInterest));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate daily loan report", e);
        }
    }

    public byte[] generateDailyExpenseReport(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<Expense> expenses = expenseRepository.findByDateBetween(start, end);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

                addHeader(document, "DAILY EXPENSES REPORT - " + date,
                    DocumentIdGenerator.report("DER", date.toString()));

                Table table = createTable(4, 2, 2, 2);
            table.addCell("Description");
            table.addCell("Category");
            table.addCell("Amount");
            table.addCell("Reference");

            BigDecimal total = BigDecimal.ZERO;
            for (Expense e : expenses) {
                table.addCell(e.getDescription());
                table.addCell(e.getCategory().getDescription());
                table.addCell(e.getAmount().toPlainString());
                table.addCell(e.getReference() != null ? e.getReference() : "");
                total = total.add(e.getAmount());
            }
            document.add(table);
            document.add(new Paragraph("\nTotal Expenses: " + total));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate daily expense report", e);
        }
    }

    public byte[] generateDailySavingsReport(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<CashTransaction> transactions = cashTransactionRepository.findByTypeInAndDateBetween(
                List.of(CashTransactionType.SAVINGS_DEPOSIT, CashTransactionType.SAVINGS_WITHDRAWAL),
                start, end);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

                addHeader(document, "DAILY SAVINGS REPORT - " + date,
                    DocumentIdGenerator.report("DSR", date.toString()));

                Table table = createTable(3, 2, 2, 3);
            table.addCell("Customer ID");
            table.addCell("Type");
            table.addCell("Amount");
            table.addCell("Description");

            BigDecimal total = BigDecimal.ZERO;
            for (CashTransaction t : transactions) {
                table.addCell(String.valueOf(t.getCustomerId()));
                table.addCell(t.getType().toString());
                table.addCell(t.getAmount().toPlainString());
                table.addCell(t.getDescription());
                
                if (t.getType() == CashTransactionType.SAVINGS_DEPOSIT) {
                    total = total.add(t.getAmount());
                } else {
                    total = total.subtract(t.getAmount());
                }
            }
            document.add(table);
            document.add(new Paragraph("\nNet Savings Change: " + total));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate daily savings report", e);
        }
    }

    public byte[] generateLoanStatement(Integer loanId) {
        Loan loan = loansRepository.findById(loanId).orElseThrow();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            addHeader(document, "LOAN STATEMENT", DocumentIdGenerator.report("LST", String.valueOf(loanId)));
            document.add(new Paragraph("Customer: " + loan.getCustomer().getCustomerName()));
            document.add(new Paragraph("Loan ID: " + loan.getId()));
            document.add(new Paragraph("Disbursed Date: " + loan.getStartDate()));
            document.add(new Paragraph("\n"));

            Table table = createTable(3, 3, 4);
            table.addCell("Date");
            table.addCell("Amount Paid");
            table.addCell("Balance");

            BigDecimal currentBalance = loan.getFullPayment();
            BigDecimal totalRepayments = BigDecimal.ZERO;

            for (Payment p : loan.getPayments()) {
                totalRepayments = totalRepayments.add(p.getAmountReceived());
                currentBalance = currentBalance.subtract(p.getAmountReceived());

                table.addCell(p.getDate().toString());
                table.addCell(p.getAmountReceived().toPlainString());
                table.addCell(currentBalance.toPlainString());
            }
            document.add(table);
            document.add(new Paragraph("\nTotal Repayments: " + totalRepayments));
            document.add(new Paragraph("Remaining Balance: " + loan.getOutstandingBalance()));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate loan statement", e);
        }
    }

    public byte[] generateAgingAnalysis() {
        List<Loan> loans = loansRepository.findAll().stream()
                .filter(l -> l.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            addHeader(document, "AGING ANALYSIS", DocumentIdGenerator.report("AGE", null));

            Table table = createTable(2.4f, 1.7f, 2.2f, 1.7f, 1.7f, 1.7f, 1.8f, 1.8f, 1.6f, 1.0f, 1.7f);
            table.setMarginTop(4);
            table.addHeaderCell("Customer").setBold();
            table.addHeaderCell("Contact").setBold();
            table.addHeaderCell("Guarantor").setBold();
            table.addHeaderCell("G. Contact").setBold();
            table.addHeaderCell("Disbursed Date").setBold();
            table.addHeaderCell("Due Date").setBold();
            table.addHeaderCell("Disbursed Amt").setBold();
            table.addHeaderCell("Outstanding").setBold();
            table.addHeaderCell("Arrears").setBold();
            table.addHeaderCell("Aging").setBold();
            table.addHeaderCell("Maturity").setBold();

            for (Loan l : loans) {
                table.addCell(l.getCustomer().getCustomerName());
                table.addCell(l.getCustomer().getTelephone());
                table.addCell(l.getGuarantor().getCustomerName());
                table.addCell(l.getGuarantor().getTelephone());
                table.addCell(l.getStartDate().toString());
                table.addCell(l.getMaturityDate().toString());
                table.addCell(l.getDisbursedAmount().toPlainString());
                table.addCell(l.getOutstandingBalance().toPlainString());
                
                // Arrears calculation: scheduled total for elapsed days minus total paid
                BigDecimal ir = l.getInterestRate() == null ? BigDecimal.ZERO : l.getInterestRate();
                BigDecimal scheduledTotal = l.getPrincipal().add(l.getPrincipal().multiply(ir));
                BigDecimal dailyInstallment = scheduledTotal.divide(BigDecimal.valueOf(l.getTenor()), 10, RoundingMode.HALF_UP);
                long elapsed = ChronoUnit.DAYS.between(l.getStartDate(), LocalDate.now()) + 1;
                BigDecimal expectedSoFar = dailyInstallment.multiply(BigDecimal.valueOf(Math.min(elapsed, l.getTenor())));
                BigDecimal arrears = expectedSoFar.subtract(l.getTotalPaid()).max(BigDecimal.ZERO);
                
                table.addCell(arrears.setScale(2, RoundingMode.HALF_UP).toPlainString());
                table.addCell(String.valueOf(l.getAgingDays(LocalDate.now())));
                table.addCell(l.getMaturityDate().toString());
            }
            document.add(table);

            document.close();
            Logger.logError("Generated aging analysis!");

            return baos.toByteArray();
        } catch (Exception e) {
            Logger.logError("Failed to generate againg analysis");
            throw new RuntimeException("Failed to generate aging analysis", e);
        }
    }

    public byte[] generateDailyReport(LocalDate date) {
        return generateDailyLoanReport(date);
    }
}
