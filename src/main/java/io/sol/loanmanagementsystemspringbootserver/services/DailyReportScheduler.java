package io.sol.loanmanagementsystemspringbootserver.services;

import io.sol.loanmanagementsystemspringbootserver.services.ReportService;
import io.sol.loanmanagementsystemspringbootserver.services.EmailService;
import io.sol.loanmanagementsystemspringbootserver.services.SystemSettingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class DailyReportScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DailyReportScheduler.class);

    private final ReportService reportService;
    private final EmailService emailService;
    private final SystemSettingService settingService;

    public DailyReportScheduler(ReportService reportService, EmailService emailService, SystemSettingService settingService) {
        this.reportService = reportService;
        this.emailService = emailService;
        this.settingService = settingService;
    }

    // Run every hour to check if it's time to send the report
    @Scheduled(cron = "0 0 * * * *")
    public void checkAndSendReport() {
        boolean enabled = Boolean.parseBoolean(settingService.getSetting("report.enabled", "false"));
        if (!enabled) return;

        String reportTimeStr = settingService.getSetting("report.time", "07:00");
        LocalTime reportTime = LocalTime.parse(reportTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime now = LocalTime.now();

        // If it's the hour for the report, send it
        if (now.getHour() == reportTime.getHour()) {
            sendDailyReport();
        }
    }

    public void sendDailyReport() {
        String emails = settingService.getSetting("report.emails", "");
        if (emails.isBlank()) {
            logger.warn("No emails configured for daily report.");
            return;
        }

        try {
            Map<String, Object> reportData = reportService.compileDailyReportData(LocalDate.now());
            emailService.sendDailyReport(emails, reportData);
            logger.info("Daily report sent to: {}", emails);
        } catch (Exception e) {
            logger.error("Failed to send daily report", e);
        }
    }
}
