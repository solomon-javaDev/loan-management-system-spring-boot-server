package io.sol.loanmanagementsystemspringbootserver.mailing;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import org.springframework.stereotype.Component;

@Component
public class EmailSettingsController {

    private final EmailSettingsService service;

    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private RadioButton startTlsRadio;
    @FXML private RadioButton sslRadio;
    @FXML private Button testButton;
    @FXML private Button saveButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    public EmailSettingsController(EmailSettingsService service) {
        this.service = service;
    }

    @FXML
    private void initialize() {
        ToggleGroup group = new ToggleGroup();
        startTlsRadio.setToggleGroup(group);
        sslRadio.setToggleGroup(group);
        MailSettings settings = service.getSettings();
        hostField.setText(settings.host());
        portField.setText(Integer.toString(settings.port()));
        usernameField.setText(settings.username());
        startTlsRadio.setSelected(settings.startTls());
        sslRadio.setSelected(settings.ssl());
        passwordField.clear();
    }

    @FXML
    private void handleTestConnection() {
        MailSettings settings;
        String password = service.savedPasswordOr(passwordField.getText());
        try {
            settings = readSettings(password);
        } catch (IllegalArgumentException ex) {
            showStatus(ex.getMessage(), false);
            return;
        }

        setBusy(true);
        statusLabel.setText("Testing SMTP connection...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                service.testConnection(settings, password);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            setBusy(false);
            showStatus("Connection successful. SMTP authentication worked.", true);
        });
        task.setOnFailed(event -> {
            setBusy(false);
            showStatus("Could not connect or authenticate. Check host, port, encryption, credentials and network.", false);
        });
        Thread thread = new Thread(task, "smtp-connection-test");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleSave() {
        try {
            String password = service.savedPasswordOr(passwordField.getText());
            MailSettings settings = readSettings(password);
            service.save(settings, password);
            showStatus("Settings saved. The password is protected for this Windows user.", true);
        } catch (IllegalArgumentException ex) {
            showStatus(ex.getMessage(), false);
        } catch (Exception ex) {
            showStatus("Could not save settings. Please try again.", false);
        }
    }

    private MailSettings readSettings(String password) {
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a valid numeric SMTP port.");
        }
        MailSettings settings = new MailSettings(hostField.getText().trim(), port,
                usernameField.getText().trim(), startTlsRadio.isSelected(), sslRadio.isSelected());
        EmailSettingsService.validate(settings, password);
        return settings;
    }

    private void setBusy(boolean busy) {
        testButton.setDisable(busy);
        saveButton.setDisable(busy);
        progressIndicator.setVisible(busy);
        progressIndicator.setManaged(busy);
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success ? "-fx-text-fill: #16803c;" : "-fx-text-fill: #c62828;");
    }
}