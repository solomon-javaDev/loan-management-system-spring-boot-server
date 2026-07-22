package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.config.StageManager;
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
@Component
public class DashboardController {

    private final SearchService searchService;
    private final StageManager stageManager;
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
    private TextField searchBar;

    @FXML
    private StackPane mainContent;

    private final ApplicationContext applicationContext;

    public DashboardController(ApplicationContext applicationContext, SearchService searchService, StageManager stageManager) {
        this.applicationContext = applicationContext;
        this.searchService = searchService;
        this.stageManager = stageManager;
    }

    @FXML
    public void initialize() {
        searchService.searchQueryProperty().bindBidirectional(searchBar.textProperty());
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
            Parent customerView = stageManager.loadView("/ui/dashboard/HomeView.fxml");
            mainContent.getChildren().setAll(customerView);
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
            Parent customerView = stageManager.loadView("/ui/dashboard/LoanView.fxml");
            mainContent.getChildren().setAll(customerView);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void showPaymentsView(){
        try{
            Parent customerView = stageManager.loadView("/ui/dashboard/PaymentsView.fxml");
            mainContent.getChildren().setAll(customerView);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
