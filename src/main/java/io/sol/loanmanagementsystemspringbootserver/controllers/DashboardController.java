package io.sol.loanmanagementsystemspringbootserver.controllers;

import java.io.IOException;

import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.SystemFinancialState;
import io.sol.loanmanagementsystemspringbootserver.events.FinancialStateUpdatedEvent;
import io.sol.loanmanagementsystemspringbootserver.services.AutoupdateService;
import io.sol.loanmanagementsystemspringbootserver.services.FinancialStateService;
import io.sol.loanmanagementsystemspringbootserver.utilities.MoneyHelper;
import io.sol.loanmanagementsystemspringbootserver.utilities.StageManager;
import io.sol.loanmanagementsystemspringbootserver.utilities.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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
 * The class uses JavaFX annotations (@FXML) to bind methods and UI components from the
 * respective FXML files. It also leverages dependency injection for managing its dependencies,
 * such as SearchService and StageManager.
 */
@Component
public class DashboardController {

    private final StageManager stageManager;
    private final AutoupdateService autoupdateService;
    private final FinancialStateService financialStateService;
    private final UserSession userSession;
    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.application.version:unknown}")
    private String applicationVersion;

    @FXML
    private Label dashboardLabel;
    @FXML
    private Button customersButton;
    @FXML
    private Button loansButton;
    @FXML
    private Button employeesButton;
    @FXML
    private Button paymentsButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button reportsButton;

    @FXML
    private Label cashAtHandLabel;

    @FXML
    private Label bankBalanceLabel;

    @FXML
    private Label availableLiquidityLabel;

    @FXML
    private Label totalLoanPortifolioLabel;

    @FXML
    private Label databaseStatusLabel;

    @FXML
    private Label versionLabel;

    @FXML
    private StackPane mainContent;

    public DashboardController(StageManager stageManager, AutoupdateService autoupdateService, io.sol.loanmanagementsystemspringbootserver.services.FinancialStateService financialStateService, io.sol.loanmanagementsystemspringbootserver.utilities.UserSession userSession, JdbcTemplate jdbcTemplate) {
        this.stageManager = stageManager;
        this.autoupdateService = autoupdateService;
        this.financialStateService = financialStateService;
        this.userSession = userSession;
        this.jdbcTemplate = jdbcTemplate;
    }

    @FXML
    public void initialize() {
        autoupdateService.checkForUpdatesAsync();
        updateTopBar();
        updateFooterStatus();

        if(userSession.isCashier()){
            reportsButton.disableProperty().setValue(true);
            settingsButton.disableProperty().setValue(true);
            employeesButton.disableProperty().setValue(true);

        }else if(userSession.isAdmin()){

            if (settingsButton != null) {
                settingsButton.setVisible(userSession.isAdmin());
            }
                  showHomeView();
        } else {

        }
    }

    private void updateFooterStatus() {
        if (versionLabel != null) {
            versionLabel.setText("VERSION " + applicationVersion);
        }
        if (databaseStatusLabel != null) {
            try {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                databaseStatusLabel.setText("DB: ONLINE");
                databaseStatusLabel.setStyle("-fx-text-fill: green;");
            } catch (Exception exception) {
                databaseStatusLabel.setText("DB: OFFLINE - " + exception.getClass().getSimpleName());
                databaseStatusLabel.setStyle("-fx-text-fill: red;");
                UIHelper.showError("THE DATABASE PATH CORRUPTED", "A fatal error occurred in the system. Contact the developers");

            }
        }
    }

    public void updateTopBar(){
        SystemFinancialState state = financialStateService.getCurrentState();
        if (cashAtHandLabel != null) {
            cashAtHandLabel.setText(MoneyHelper.formatMoney(state.getCashOnHand()));
        }
        if (bankBalanceLabel != null) {
            bankBalanceLabel.setText(MoneyHelper.formatMoney(state.getBankBalance()));
        }
        if (availableLiquidityLabel != null) {
            availableLiquidityLabel.setText(MoneyHelper.formatMoney(state.getAvailableLiquidity()));
        }
        if (totalLoanPortifolioLabel != null) {
            totalLoanPortifolioLabel.setText(MoneyHelper.formatMoney(state.getGrossLoanPortfolio()));
        }
    }

    @EventListener
    public void onFinancialStateUpdated(FinancialStateUpdatedEvent event) {
        try {
            if (Platform.isFxApplicationThread()) {
                updateTopBar();
                return;
            }
            Platform.runLater(this::updateTopBar);
        } catch (IllegalStateException | IllegalArgumentException | UnsupportedOperationException ignored) {
            UIHelper.showError("ERROR LOADING", "A fatal error occurred in the system. Contact the developers");

            // The JavaFX toolkit is not initialised in headless/test startup paths.
        }
    }

    @FXML
    private void onLogoutClicked() {
        try {
            Parent loginRoot = stageManager.loadView("/ui/login/LoginView.fxml");

            Stage stage = (Stage) dashboardLabel.getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Loan Management System");
            stage.setResizable(false);
        } catch (IOException e) {
            UIHelper.showError("ERROR LOADING", "A fatal error occurred in the system. Contact the developers");

        }
    }

    @FXML
    public void showHomeView(){
        try{
            Parent homeView = stageManager.loadView("/ui/dashboard/HomeView.fxml");
            mainContent.getChildren().setAll(homeView);
        } catch (IOException e) {
            UIHelper.showError("ERROR LOADING DASHBOARD DAILY STATS", "A fatal error occurred in the system. Contact the developers");

            throw new RuntimeException(e);
        }
    }
    @FXML
    public void showCustomerView(){
        try{
            Parent customerView = stageManager.loadView("/ui/dashboard/CustomerView.fxml");
            mainContent.getChildren().setAll(customerView);
        } catch (IOException e) {
            UIHelper.showError("ERROR LOADING CUSTOMERS", "A fatal error occurred in the system. Contact the developers");

            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showLoanView(){
        try{
            Parent loanView = stageManager.loadView("/ui/dashboard/LoanView.fxml");
            mainContent.getChildren().setAll(loanView);
        } catch (IOException e) {
            UIHelper.showError("ERROR LOADING LOANS", "A fatal error occurred in the system. Contact the developers");

            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showPaymentsView(){
        try{
            Parent paymentsView = stageManager.loadView("/ui/dashboard/PaymentsView.fxml");
            mainContent.getChildren().setAll(paymentsView);
        } catch (IOException e) {
            UIHelper.showError("ERROR LOADING PAYMENTS", "A fatal error occurred in the system. Contact the developers");

            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showReportsView(){
        try{
            Parent reportsView = stageManager.loadView("/ui/dashboard/ReportsView.fxml");
            mainContent.getChildren().setAll(reportsView);
        } catch (IOException e) {
            UIHelper.showError("ERROR LOADING REPORTS", "A fatal error occurred in the system. Contact the developers");

            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showSettingsView(){
        try{
            Parent settingsView = stageManager.loadView("/ui/dashboard/SettingsView.fxml");
            mainContent.getChildren().setAll(settingsView);
        } catch (IOException e) {
            UIHelper.showError("ERROR LOADING SETTINGS", "A fatal error occurred in the system. Contact the developers");

            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showFinanceView(){
        try {
            mainContent.getChildren().setAll(stageManager.loadView("/ui/dashboard/FinanceView.fxml"));
        } catch (IOException e) {
            UIHelper.showError("ERROR LOADING FINANCE", "A fatal error occurred in the system. Contact the developers");

            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showEmployeesView(){
        try {
            mainContent.getChildren().setAll(stageManager.loadView("/ui/dashboard/EmployeesView.fxml"));
        } catch (IOException e) {
            UIHelper.showError("ERROR LOADING EMPLOYEES", "A fatal error occurred in the system. Contact the developers");
            throw new RuntimeException(e);
        }
    }

}
