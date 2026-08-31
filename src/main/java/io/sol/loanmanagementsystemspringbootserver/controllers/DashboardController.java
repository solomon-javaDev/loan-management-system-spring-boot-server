package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.services.AutoupdateService;
import io.sol.loanmanagementsystemspringbootserver.utilities.StageManager;
import io.sol.loanmanagementsystemspringbootserver.services.SearchService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
 * Inject new StageManager into the DashboardController.
 * When a user clicks a navigation label, clear the existing layout inside the StackPane and inject the newly loaded root node.
 */

/**
 * The DashboardController class serves as the main controller for the dashboard of the
 * application. It provides functionality to navigate between different views of the
 * application and manage interactions with the user interface components.
 * This controller is responsible for:
 * - Managing and switching between various dashboard views such as Customers, Loans,
 *   Applications, Payments, Settings, and Reports.
 * - Handling the user logout functionality by switching to the login view.
 * - Synchronizing the search bar's text field with the SearchService for real-time search updates.
 *
 * The class uses JavaFX annotations (@FXML) to bind methods and UI components from the
 * respective FXML files. It also leverages dependency injection for managing its dependencies,
 * such as SearchService and StageManager.
 */
@Component
public class DashboardController {

    private final StageManager stageManager;
    private final AutoupdateService autoupdateService;
    private final io.sol.loanmanagementsystemspringbootserver.services.FinancialStateService financialStateService;
    private final io.sol.loanmanagementsystemspringbootserver.utilities.UserSession userSession;

    @FXML
    private Label dashboardLabel;
    @FXML
    private Button customersButton;
    @FXML
    private Button loansButton;
    @FXML
    private Button applicationsButton;
    @FXML
    private Button paymentsButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button reportsButton;

    @FXML
    private Label cashAtHand;

    @FXML
    private Label totalLoanPortifolio;

    @FXML
    private TextField searchBar;

    @FXML
    private StackPane mainContent;

    private final ApplicationContext applicationContext;

    public DashboardController(ApplicationContext applicationContext, StageManager stageManager, AutoupdateService autoupdateService, io.sol.loanmanagementsystemspringbootserver.services.FinancialStateService financialStateService, io.sol.loanmanagementsystemspringbootserver.utilities.UserSession userSession) {
        this.applicationContext = applicationContext;
        this.stageManager = stageManager;
        this.autoupdateService = autoupdateService;
        this.financialStateService = financialStateService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        updateTopBar();
        autoupdateService.checkForUpdatesAsync();
        
        if (settingsButton != null) {
            settingsButton.setVisible(userSession.isAdmin());
        }
        if (reportsButton != null) {
            // Requirement doesn't explicitly say hide reports from cashier, 
            // but says "CASHIERS are not allowed entirely to view the settings panel"
        }
    }

    public void updateTopBar(){
        io.sol.loanmanagementsystemspringbootserver.entities.Finance.SystemFinancialState state = financialStateService.getCurrentState();
        if (cashAtHand != null) {
            cashAtHand.setText(String.format("%,.2f", state.getCashOnHand()));
        }
        if (totalLoanPortifolio != null) {
            totalLoanPortifolio.setText(String.format("%,.2f", state.getGrossLoanPortfolio()));
        }
    }

    @FXML
    private void onLogoutClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login/LoginView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent loginRoot = loader.load();

            Stage stage = (Stage) dashboardLabel.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Loan Management System");
            stage.setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showHomeView(){
        try{
            Parent homeView = stageManager.loadView("/ui/dashboard/HomeView.fxml");
            mainContent.getChildren().setAll(homeView);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    public void showCustomerView(){
        try{
            Parent customerView = stageManager.loadView("/ui/dashboard/CustomerView.fxml");
            mainContent.getChildren().setAll(customerView);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showLoanView(){
        try{
            Parent loanView = stageManager.loadView("/ui/dashboard/LoanView.fxml");
            mainContent.getChildren().setAll(loanView);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showPaymentsView(){
        try{
            Parent paymentsView = stageManager.loadView("/ui/dashboard/PaymentsView.fxml");
            mainContent.getChildren().setAll(paymentsView);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showReportsView(){
        try{
            Parent reportsView = stageManager.loadView("/ui/dashboard/ReportsView.fxml");
            mainContent.getChildren().setAll(reportsView);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showSettingsView(){
        try{
            Parent settingsView = stageManager.loadView("/ui/dashboard/SettingsView.fxml");
            mainContent.getChildren().setAll(settingsView);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showFinanceView(){
        try {
            mainContent.getChildren().setAll(stageManager.loadView("/ui/dashboard/FinanceView.fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showEmployeesView(){
        try {
            mainContent.getChildren().setAll(stageManager.loadView("/ui/dashboard/EmployeesView.fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
