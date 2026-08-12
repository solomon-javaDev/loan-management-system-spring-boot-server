package io.sol.loanmanagementsystemspringbootserver.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class SettingsController {

    @FXML
    private TextField adminEmailsField;

    @FXML
    private CheckBox dailyReportCheckbox;

    @FXML
    private TextField dailyTimeField;

    @FXML
    private ComboBox<String> agingFreq;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        agingFreq.getItems().addAll("Daily","Every 3 days","Weekly","Monthly");
        dailyReportCheckbox.setSelected(true);
        dailyTimeField.setText("07:00");
    }

    @FXML
    private void handleSave() {
        // TODO: persist into settings table/entity. For now just show a message
        messageLabel.setText("Settings saved (not persisted).");
    }
}
