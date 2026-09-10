package io.sol.loanmanagementsystemspringbootserver.services;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import io.sol.loanmanagementsystemspringbootserver.dtos.PaymentDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransaction;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final ReportService reportService;

    public byte[] generateDailyLoanReport(LocalDate date) {
        return reportService.generateDailyLoanReport(date);
    }

    public byte[] generateDailyExpenseReport(LocalDate date) {
        return reportService.generateDailyExpenseReport(date);
    }

    public byte[] generateDailySavingsReport(LocalDate date) {
        return reportService.generateDailySavingsReport(date);
    }

    public byte[] generateLoanStatement(Integer loanId) {
        return reportService.generateLoanStatement(loanId);
    }

    public byte[] generateAgingAnalysis() {
        return reportService.generateAgingAnalysis();
    }

    public byte[] generateDailyReport(LocalDate date) {
        return reportService.generateDailyReport(date);
    }

    public byte[] generatePaymentReceipt(PaymentDTO payment) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("PAYMENT RECEIPT").setBold().setFontSize(18));
            document.add(new Paragraph("Receipt #: " + (payment != null && payment.getId() != null ? payment.getId() : "N/A")));
            document.add(new Paragraph("Date: " + (payment != null && payment.getDate() != null ? payment.getDate().toString() : "N/A")));
            document.add(new Paragraph("Customer: " + (payment != null ? payment.getCustomerName() : "N/A")));
            document.add(new Paragraph("Loan Reference: " + (payment != null ? payment.getLoanReference() : "N/A")));

            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2}));
            table.setWidth(UnitValue.createPercentValue(70));
            table.addCell("Amount received");
            table.addCell(formatMoney(payment != null ? payment.getAmountReceived() : BigDecimal.ZERO));
            table.addCell("Remaining balance");
            table.addCell(formatMoney(payment != null ? payment.getRemainingBalance() : BigDecimal.ZERO));
            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate payment receipt", e);
        }
    }

    public byte[] generateSavingsReceipt(CashTransaction transaction, String customerName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("SAVINGS RECEIPT").setBold().setFontSize(18));
            document.add(new Paragraph("Customer: " + (customerName != null ? customerName : "N/A")));
            document.add(new Paragraph("Date: " + (transaction != null && transaction.getDate() != null ? transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A")));
            document.add(new Paragraph("Type: " + (transaction != null && transaction.getType() != null ? transaction.getType() : "N/A")));
            document.add(new Paragraph("Description: " + (transaction != null ? transaction.getDescription() : "N/A")));

            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2}));
            table.setWidth(UnitValue.createPercentValue(70));
            table.addCell("Amount");
            table.addCell(formatMoney(transaction != null ? transaction.getAmount() : BigDecimal.ZERO));
            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate savings receipt", e);
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "UGX 0.00";
        }
        return "UGX " + amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
