package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.services.ReportService;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

@Component
public class ReportsController {

    private final ReportService reportService;

    @FXML
    private VBox dailySummaryBox;

    @FXML
    private VBox agingBox;

    @FXML
    private LineChart<String, Number> trendChart;

    @FXML
    private ComboBox<String> timeRangeCombo;

    @FXML
    private Label messageLabel;

    public ReportsController(ReportService reportService) {
        this.reportService = reportService;
    }

    @FXML
    public void initialize() {
        timeRangeCombo.getItems().addAll("Day", "Week", "Month");
        timeRangeCombo.setValue("Day");
        timeRangeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateChart());
        
        loadDailySummary(LocalDate.now());
        loadAgingAnalysis();
        updateChart();
    }

    private void loadDailySummary(LocalDate date) {
        Map<String, Object> summary = reportService.getDailyCashReport(date);
        dailySummaryBox.getChildren().clear();
        Label title = new Label("Daily Summary (" + date + ")");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        dailySummaryBox.getChildren().add(title);
        summary.forEach((k,v) -> dailySummaryBox.getChildren().add(new Label(k + ": " + v)));
    }

    private void loadAgingAnalysis() {
        Map<String, BigDecimal> aging = reportService.getAgingAnalysis();
        agingBox.getChildren().clear();
        Label title = new Label("Aging Analysis");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        agingBox.getChildren().add(title);
        aging.forEach((k,v) -> agingBox.getChildren().add(new Label(k + ": " + v)));
    }

    private void updateChart() {
        trendChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Money Collected (Cumulative)");

        LocalDate now = LocalDate.now();
        Map<LocalDate, BigDecimal> data;
        String range = timeRangeCombo.getValue();
        
        if ("Month".equals(range)) {
            data = reportService.getCollectionsPerDay(now.minusMonths(1), now);
        } else if ("Week".equals(range)) {
            data = reportService.getCollectionsPerDay(now.minusWeeks(1), now);
        } else {
            data = reportService.getCollectionsPerDay(now.minusDays(7), now); // Default to last 7 days
        }

        Map<LocalDate, BigDecimal> sortedData = new TreeMap<>(data);
        BigDecimal cumulative = BigDecimal.ZERO;
        for (Map.Entry<LocalDate, BigDecimal> entry : sortedData.entrySet()) {
            cumulative = cumulative.add(entry.getValue());
            series.getData().add(new XYChart.Data<>(entry.getKey().toString(), cumulative));
        }

        trendChart.getData().add(series);
    }

    @FXML
    private void handleRefresh() {
        loadDailySummary(LocalDate.now());
        loadAgingAnalysis();
        updateChart();
        messageLabel.setText("Reports refreshed");
        messageLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void handleSendDaily() {
        String result = reportService.sendDailyReport(LocalDate.now());
        messageLabel.setText(result);
        if (result.contains("Error")) {
            messageLabel.setStyle("-fx-text-fill: red;");
        } else {
            messageLabel.setStyle("-fx-text-fill: green;");
        }
    }

    @FXML
    private void handleSendAging() {
        messageLabel.setText("Send aging report not yet implemented.");
        messageLabel.setStyle("-fx-text-fill: orange;");
    }
}
