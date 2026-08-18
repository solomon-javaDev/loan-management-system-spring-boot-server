package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.services.DailyReportScheduler;
import io.sol.loanmanagementsystemspringbootserver.services.ReportService;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
public class ReportsController {

    private final ReportService reportService;
    private final DailyReportScheduler reportScheduler;

    @FXML
    private VBox dailySummaryBox;

    @FXML
    private VBox agingBox;

    @FXML
    private LineChart<?,?> trendChart;

    @FXML
    private Label messageLabel;

    public ReportsController(ReportService reportService, DailyReportScheduler reportScheduler) {
        this.reportService = reportService;
        this.reportScheduler = reportScheduler;
    }

    @FXML
    public void initialize() {
        loadDailySummary(LocalDate.now());
        loadAgingAnalysis();
    }

    private void loadDailySummary(LocalDate date) {
        Map<String, Object> summary = reportService.getDailyCashReport(date);
        dailySummaryBox.getChildren().clear();
        summary.forEach((k,v) -> dailySummaryBox.getChildren().add(new Label(k + ": " + v)));
    }

    private void loadAgingAnalysis() {
        Map<String, BigDecimal> aging = reportService.getAgingAnalysis();
        agingBox.getChildren().clear();
        aging.forEach((k,v) -> agingBox.getChildren().add(new Label(k + ": " + v)));
    }

    @FXML
    private void handleRefresh() {
        loadDailySummary(LocalDate.now());
        loadAgingAnalysis();
        messageLabel.setText("Reports refreshed");
    }

    @FXML
    private void handleSendDaily() {
        try {
            reportScheduler.sendDailyReport();
            messageLabel.setText("Daily report email sent (if configured).");
        } catch (Exception e) {
            messageLabel.setText("Failed to send report: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendAging() {
        messageLabel.setText("Send aging report not yet implemented.");
    }
}
