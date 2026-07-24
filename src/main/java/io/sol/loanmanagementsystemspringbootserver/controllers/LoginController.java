package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.config.Result;
import io.sol.loanmanagementsystemspringbootserver.config.StageManager;
import io.sol.loanmanagementsystemspringbootserver.entities.User;
import io.sol.loanmanagementsystemspringbootserver.services.AuthenticationService;
import org.springframework.context.ApplicationContext;
import javafx.fxml.FXML;
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

    private final StageManager stageManager;
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
    public LoginController(AuthenticationService authenticationService, ApplicationContext applicationContext, StageManager stageManager) {
        this.authenticationService = authenticationService;
        this.stageManager = stageManager;
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

        Result<User> authResult = authenticationService.authenticate(username, password);
        messageLabel.setText(authResult.message());

        if (authResult.isSuccess()) {
            loadDashboard();
        }
    }

    private void loadDashboard() {
        try {
            Parent dashboardRoot = stageManager.loadView("/ui/dashboard/DashboardView.fxml");

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
