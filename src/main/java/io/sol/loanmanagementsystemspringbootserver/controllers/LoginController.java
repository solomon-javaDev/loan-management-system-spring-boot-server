package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.services.AuthenticationService;
import org.springframework.context.ApplicationContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button creatAccountButton;

    private final AuthenticationService authenticationService;
    private final ApplicationContext applicationContext;

    public LoginController(AuthenticationService authenticationService, ApplicationContext applicationContext) {
        this.authenticationService = authenticationService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {

    }

    @FXML
    private void handleCreateAccountClick(){

    }

    @FXML
    private void onLoginClicked() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        authenticationService.authenticate(username, password).ifPresentOrElse(
                user -> loadDashboard(),
                () -> messageLabel.setText("Invalid username or password. Please try again."));
    }

    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/dashboard/DashboardView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent dashboardRoot = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(dashboardRoot));
            stage.setTitle("Loan Management System - Dashboard");
            stage.setResizable(true);
        } catch (IOException e) {
            messageLabel.setText("Unable to open dashboard. Please contact support.");
            e.printStackTrace();
        }
    }
}
