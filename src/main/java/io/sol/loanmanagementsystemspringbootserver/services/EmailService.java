package io.sol.loanmanagementsystemspringbootserver.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDailyReport(String to, Map<String, Object> data) throws MessagingException {
        if (to == null || to.isBlank()) return;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to.split(","));
        helper.setSubject("Daily Business Report - " + data.get("date"));
        
        String htmlContent = buildHtmlReport(data);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildHtmlReport(Map<String, Object> data) {
        LocalDate date = (LocalDate) data.get("date");
        Map<String, Object> dailyCash = (Map<String, Object>) data.get("dailyCash");
        Map<String, BigDecimal> aging = (Map<String, BigDecimal>) data.get("aging");
        Map<String, Double> employeePerf = (Map<String, Double>) data.get("employeePerformance");
        BigDecimal totalDisbursed = (BigDecimal) data.get("totalDisbursed");
        BigDecimal totalExpenses = (BigDecimal) data.get("totalExpenses");

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h1>Daily Business Report for ").append(date).append("</h1>");

        // 1. Daily Performance
        sb.append("<h2>1. Daily Cash Performance</h2>");
        sb.append("<ul>");
        sb.append("<li>Total Transactions: ").append(dailyCash.get("totalTransactions")).append("</li>");
        sb.append("<li>Closing Balance: ").append(dailyCash.get("closingBalance")).append("</li>");
        sb.append("</ul>");

        // 2. Aging Analysis
        sb.append("<h2>2. Aging Analysis</h2>");
        sb.append("<table border='1'><tr><th>Bucket</th><th>Outstanding Balance</th></tr>");
        aging.forEach((k, v) -> sb.append("<tr><td>").append(k).append("</td><td>").append(v).append("</td></tr>"));
        sb.append("</table>");

        // 3. Employee Performance
        sb.append("<h2>3. Employee Performance (Collection Rate)</h2>");
        sb.append("<ul>");
        employeePerf.forEach((k, v) -> sb.append("<li>").append(k).append(": ").append(String.format("%.2f%%", v * 100)).append("</li>"));
        sb.append("</ul>");

        // 4. Loans & Expenses
        sb.append("<h2>4. Loans & Expenses</h2>");
        sb.append("<ul>");
        sb.append("<li>Total Loans Disbursed Today: ").append(totalDisbursed).append("</li>");
        sb.append("<li>Total Expenses Today: ").append(totalExpenses).append("</li>");
        sb.append("</ul>");

        sb.append("<p><i>This is an automated report from the Loan Management System.</i></p>");
        sb.append("</body></html>");

        return sb.toString();
    }
}
