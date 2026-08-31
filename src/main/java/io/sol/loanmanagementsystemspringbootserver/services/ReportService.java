package io.sol.loanmanagementsystemspringbootserver.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Expense;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Payment;
import io.sol.loanmanagementsystemspringbootserver.repositories.ExpenseRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final LoansRepository loansRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final FinancialStateService financialStateService;

    public byte[] generateDailyReport(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Loan> loans = loansRepository.findAll().stream()
                .filter(l -> l.getStartDate().equals(date))
                .toList();
        List<Payment> payments = paymentRepository.findByDate(date);
        List<Expense> expenses = expenseRepository.findByDateBetween(startOfDay, endOfDay);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("Daily Report - " + date).setBold().setFontSize(18));
            
            document.add(new Paragraph("Loans Disbursed").setBold());
            Table loanTable = new Table(new float[]{1, 3, 2});
            loanTable.addCell("ID");
            loanTable.addCell("Customer");
            loanTable.addCell("Principal");
            for (Loan l : loans) {
                loanTable.addCell(String.valueOf(l.getId()));
                loanTable.addCell(l.getCustomer().getCustomerName());
                loanTable.addCell(l.getPrincipal().toPlainString());
            }
            document.add(loanTable);

            document.add(new Paragraph("Payments Received").setBold());
            Table paymentTable = new Table(new float[]{1, 3, 2});
            paymentTable.addCell("ID");
            paymentTable.addCell("Customer");
            paymentTable.addCell("Amount");
            for (Payment p : payments) {
                paymentTable.addCell(String.valueOf(p.getId()));
                paymentTable.addCell(p.getLoan().getCustomer().getCustomerName());
                paymentTable.addCell(p.getAmountReceived().toPlainString());
            }
            document.add(paymentTable);

            document.add(new Paragraph("Expenses").setBold());
            Table expenseTable = new Table(new float[]{3, 2});
            expenseTable.addCell("Description");
            expenseTable.addCell("Amount");
            for (Expense e : expenses) {
                expenseTable.addCell(e.getDescription());
                expenseTable.addCell(e.getAmount().toPlainString());
            }
            document.add(expenseTable);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report", e);
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

            document.add(new Paragraph("Aging Analysis - " + LocalDate.now()).setBold().setFontSize(18));
            
            Table table = new Table(new float[]{2, 2, 2, 2, 2, 2, 1, 2});
            table.addCell("Customer");
            table.addCell("Contact");
            table.addCell("Guarantor");
            table.addCell("Disbursed");
            table.addCell("Maturity");
            table.addCell("Outstanding");
            table.addCell("Aging Days");
            table.addCell("Deadline");

            for (Loan l : loans) {
                table.addCell(l.getCustomer().getCustomerName());
                table.addCell(l.getCustomer().getTelephone());
                table.addCell(l.getGuarantor().getCustomerName());
                table.addCell(l.getStartDate().toString());
                table.addCell(l.getMaturityDate().toString());
                table.addCell(l.getOutstandingBalance().toPlainString());
                table.addCell(String.valueOf(l.getAgingDays(LocalDate.now())));
                table.addCell(l.getMaturityDate().toString()); // Deadline is maturity date
            }
            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate aging analysis", e);
        }
    }

    public byte[] generateLoanStatement(Integer loanId) {
        Loan loan = loansRepository.findById(loanId).orElseThrow();
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("Loan Statement").setBold().setFontSize(18));
            document.add(new Paragraph("Loan ID: " + loan.getId()));
            document.add(new Paragraph("Customer: " + loan.getCustomer().getCustomerName()));
            document.add(new Paragraph("Principal: " + loan.getPrincipal()));
            
            document.add(new Paragraph("Transactions").setBold());
            Table table = new Table(new float[]{2, 2, 2, 2, 2, 2});
            table.addCell("Date");
            table.addCell("Total Paid");
            table.addCell("Principal");
            table.addCell("Interest");
            table.addCell("Fees");
            table.addCell("Surcharge");

            for (Payment p : loan.getPayments()) {
                table.addCell(p.getDate().toString());
                table.addCell(p.getAmountReceived().toPlainString());
                table.addCell(p.getPrincipalAmount().toPlainString());
                table.addCell(p.getInterestAmount().toPlainString());
                table.addCell(p.getFeeAmount().toPlainString());
                table.addCell(p.getSurchargeAmount().toPlainString());
            }
            document.add(table);
            document.add(new Paragraph("Remaining Balance: " + loan.getOutstandingBalance()));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate loan statement", e);
        }
    }
}
