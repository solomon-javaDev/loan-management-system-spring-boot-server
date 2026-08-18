package io.sol.loanmanagementsystemspringbootserver.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReportGenerationTest {

    @Autowired
    private ReportService reportService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    public void testCompileDailyReportData() {
        LocalDate date = LocalDate.now();
        Map<String, Object> report = reportService.compileDailyReportData(date);
        
        assertNotNull(report);
        assertEquals(date, report.get("date"));
        assertTrue(report.containsKey("dailyCash"));
        assertTrue(report.containsKey("aging"));
        assertTrue(report.containsKey("employeePerformance"));
        assertTrue(report.containsKey("todayLoans"));
        assertTrue(report.containsKey("totalDisbursed"));
        assertTrue(report.containsKey("totalExpenses"));
    }
}
