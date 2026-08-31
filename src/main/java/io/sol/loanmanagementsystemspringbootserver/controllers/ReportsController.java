package io.sol.loanmanagementsystemspringbootserver.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.chart.LineChart;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReportsController {

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

    }

    private void loadAgingAnalysis() {

    }

    private void updateChart() {

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

    }

    @FXML
    private void handleSendAging() {
        messageLabel.setText("Send aging report not yet implemented.");
        messageLabel.setStyle("-fx-text-fill: orange;");
    }
}
