package io.sol.loanmanagementsystemspringbootserver.services;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import io.sol.loanmanagementsystemspringbootserver.dtos.PaymentDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransaction;
import io.sol.loanmanagementsystemspringbootserver.utilities.DocumentIdGenerator;
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

        public boolean printPaymentReceipt(PaymentDTO payment) {
        String documentId = DocumentIdGenerator.receipt("PAY", payment != null && payment.getId() != null
            ? String.valueOf(payment.getId()) : null);
        String receipt = escPosReceipt(
            "PAYMENT RECEIPT",
            documentId,
            "Date: " + (payment != null && payment.getDate() != null ? payment.getDate() : "N/A"),
            "Customer: " + (payment != null ? payment.getCustomerName() : "N/A"),
            "Loan Reference: " + (payment != null ? payment.getLoanReference() : "N/A"),
            "Amount received: " + formatMoney(payment != null ? payment.getAmountReceived() : BigDecimal.ZERO),
            "Remaining balance: " + formatMoney(payment != null ? payment.getRemainingBalance() : BigDecimal.ZERO));
        return printReceipt(receipt);
        }

        public boolean printSavingsReceipt(CashTransaction transaction, String customerName) {
        String documentId = DocumentIdGenerator.receipt("SAV", transaction != null && transaction.getId() != null
            ? String.valueOf(transaction.getId()) : null);
        String receipt = escPosReceipt(
            "SAVINGS RECEIPT",
            documentId,
            "Customer: " + (customerName != null ? customerName : "N/A"),
            "Date: " + (transaction != null && transaction.getDate() != null
                ? transaction.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A"),
            "Type: " + (transaction != null && transaction.getType() != null ? transaction.getType() : "N/A"),
            "Description: " + (transaction != null ? transaction.getDescription() : "N/A"),
            "Amount: " + formatMoney(transaction != null ? transaction.getAmount() : BigDecimal.ZERO));
        return printReceipt(receipt);
        }

    public byte[] generatePaymentReceipt(PaymentDTO payment) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("PAYMENT RECEIPT").setBold().setFontSize(18));
            document.add(new Paragraph("Document ID: " + DocumentIdGenerator.receipt("PAY", payment != null && payment.getId() != null ? String.valueOf(payment.getId()) : null)));
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
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to generate payment receipt", e);
        }
    }

    public byte[] generateSavingsReceipt(CashTransaction transaction, String customerName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("SAVINGS RECEIPT").setBold().setFontSize(18));
            document.add(new Paragraph("Document ID: " + DocumentIdGenerator.receipt("SAV", transaction != null && transaction.getId() != null ? String.valueOf(transaction.getId()) : null)));
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
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to generate savings receipt", e);
        }
    }

    private boolean printReceipt(String receipt) {
        try {
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            PrintService printer = PrintServiceLookup.lookupDefaultPrintService();
            if (printer == null || !printer.isDocFlavorSupported(flavor)) {
                PrintService[] printers = PrintServiceLookup.lookupPrintServices(flavor, null);
                printer = printers.length == 0 ? null : printers[0];
            }
            if (printer == null) {
                return false;
            }

            DocPrintJob job = printer.createPrintJob();
            Doc document = new SimpleDoc(receipt.getBytes(StandardCharsets.US_ASCII), flavor, null);
            job.print(document, null);
            return true;
        } catch (PrintException | IllegalArgumentException | SecurityException ignored) {
            return false;
        }
    }

    private String escPosReceipt(String title, String documentId, String... lines) {
        StringBuilder receipt = new StringBuilder();
        receipt.append((char) 0x1B).append('@');
        receipt.append((char) 0x1B).append('a').append((char) 1);
        receipt.append(title).append('\n');
        receipt.append((char) 0x1B).append('a').append((char) 0);
        receipt.append("Document ID: ").append(documentId).append('\n');
        receipt.append("--------------------------------\n");
        for (String line : lines) {
            receipt.append(line).append('\n');
        }
        receipt.append("--------------------------------\n");
        receipt.append("Thank you\n\n\n");
        receipt.append((char) 0x1D).append('V').append((char) 1);
        return receipt.toString();
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "UGX 0.00";
        }
        return "UGX " + amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
