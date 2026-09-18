package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.repositories.UserRepository;
import io.sol.loanmanagementsystemspringbootserver.utilities.Logger;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.StageManager;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.User;
import io.sol.loanmanagementsystemspringbootserver.services.AuthenticationService;
import io.sol.loanmanagementsystemspringbootserver.utilities.UserSession;
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

/**
 * The LoginController class is a JavaFX controller responsible for managing the login
 * functionality in the application. It handles user input and interactions, including
 * username and password entry, login button clicks, and account creation.
 * It communicates with the {@code AuthenticationService} to authenticate users
 * and navigates to the dashboard upon successful login.
 *
 * This controller is managed as a Spring component, allowing for dependency injection
 * of services and context required for its operation.
 */

@Component
public class LoginController {

    private final StageManager stageManager;
    private final UserRepository userRepository;

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
    private final UserSession userSession;
    public LoginController(AuthenticationService authenticationService, ApplicationContext applicationContext, StageManager stageManager, UserRepository userRepository, UserSession userSession) {
        this.authenticationService = authenticationService;
        this.stageManager = stageManager;
        this.userRepository = userRepository;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {

    }

    @FXML
    private void onLoginClicked() {
        Logger.logInfo("Login clicked");
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        Result<User> authResult = authenticationService.authenticate(username, password);
        messageLabel.setText(authResult.message());

        if (authResult.isSuccess()) {
            userSession.login(authResult.value());
            loadDashboard();
        }
    }


    @FXML
    private void loadCreateAccount(){
        try{
            Parent createAccountView = stageManager.loadView("ui/login/CreateAccountView.fxml");

            Stage stage = (Stage) creatAccountButton.getScene().getWindow();
            stage.setScene(new Scene(createAccountView));

            stage.setMaximized(false);
            stage.setTitle("Create Account - LMS");
            stage.setResizable(false);
        }catch(Exception e){
            Logger.logError(e.getMessage());
            messageLabel.setText("Unable to load the Create Account form!");
        }
    }

    private void loadDashboard() {
        try {
            Parent dashboardRoot = stageManager.loadView("/ui/dashboard/DashboardView.fxml");

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(dashboardRoot));
            stage.setMaximized(true);
            stage.setTitle("Loan Management System - Dashboard");
            stage.setResizable(true);
        } catch (IOException e) {
            messageLabel.setText("Unable to open dashboard. Please contact support.");
            Logger.logError(e.getMessage());
        }
    }
}
