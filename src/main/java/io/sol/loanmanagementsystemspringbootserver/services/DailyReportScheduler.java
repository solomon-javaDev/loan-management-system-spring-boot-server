package io.sol.loanmanagementsystemspringbootserver.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Component
public class DailyReportScheduler {

    private final ReportService reportService;
    private final SystemSettingService settingService;

    public DailyReportScheduler(ReportService reportService, SystemSettingService settingService) {
        this.reportService = reportService;
        this.settingService = settingService;
    }

    // Runs every minute to check if it's time to send the report
    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendDailyReport() {
        boolean enabled = Boolean.parseBoolean(settingService.getSetting("report.enabled", "false"));
        if (!enabled) {
            return;
        }

        String scheduledTimeStr = settingService.getSetting("report.time", "07:00");
        try {
            LocalTime scheduledTime = LocalTime.parse(scheduledTimeStr);
            LocalTime now = LocalTime.now().withSecond(0).withNano(0);

            if (now.equals(scheduledTime)) {
                reportService.sendDailyReport(LocalDate.now());
            }
        } catch (DateTimeParseException e) {
            // Log error or handle invalid time format
            System.err.println("Invalid report time format: " + scheduledTimeStr);
        }
    }
}
