package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.PaymentDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;
import io.sol.loanmanagementsystemspringbootserver.services.LoansService;
import io.sol.loanmanagementsystemspringbootserver.services.PaymentsService;
import io.sol.loanmanagementsystemspringbootserver.services.ReportService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * The HomeController class is a Spring component that serves as the controller
 * for managing and displaying statistical and financial data on the application's home/dashboard.
 * It communicates with various service layers to fetch data and updates the UI labels accordingly.
 *
 * Responsibilities of the HomeController include:
 * - Computing and displaying daily statistics such as total customers, active customers,
 *   loans disbursed, collection rates, etc.
 * - Handling user interactions and navigation to other views within the application.
 * - Providing actions to export or print the summarized data.
 */
@Component
public class HomeController {

    private final LoansService loansService;
    private final CustomerService customerService;
    private final PaymentsService paymentsService;
    private final ReportService reportService;
    private final DashboardController dashboardController;

    @FXML
    private Label totalCustomersLabel;
    @FXML
    private Label activeCustomersLabel;
    @FXML
    private Label customersPaidTodayLabel;
    @FXML
    private Label collectionRateLabel;
    @FXML
    private Label newCustomersTodayLabel;
    @FXML
    private Label totalCollectionsLabel;
    @FXML
    private Label loansDisbursedLabel;
    @FXML
    private Label totalAmountDisbursedLabel;
    @FXML
    private Label principalBalanceLabel;
    @FXML
    private Label totalLoanPortfolioLabel;
    @FXML
    private Label openingCashLabel;
    @FXML
    private Label principalCollectedLabel;
    @FXML
    private Label interestCollectedLabel;
    @FXML
    private Label processingFeesLabel;
    @FXML
    private Label bankDepositsLabel;
    @FXML
    private Label totalExpensesLabel;
    @FXML
    private Label loanDisbursementsLabel;
    @FXML
    private Label checkoutCashLabel;

    public HomeController(LoansService loansService, CustomerService customerService, 
                          PaymentsService paymentsService,
                          ReportService reportService,
                          @Lazy DashboardController dashboardController) {
        this.loansService = loansService;
        this.customerService = customerService;
        this.paymentsService = paymentsService;
        this.reportService = reportService;
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        calculateDailyStats();
    }

    private void calculateDailyStats() {
        LocalDate today = LocalDate.now();
        List<CustomerDTO> allCustomers = customerService.getAllCustomers().value();
        List<LoanDTO> allLoans = loansService.getAllLoans().value();
        List<PaymentDTO> allPayments = paymentsService.getAllPayments().value();
        List<PaymentDTO> todayPayments = paymentsService.getPaymentsByDate(today).value();
        
        // loansService doesn't have getLoansByDate yet, let's filter manually
        List<LoanDTO> todayLoans = allLoans.stream()
                .filter(l -> today.equals(l.getStartDate()))
                .toList();

        // 1. Total number of customers
        totalCustomersLabel.setText(String.valueOf(allCustomers.size()));

        // 2. Number of active customers (having active loans)
        long activeCustomers = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .map(LoanDTO::getCustomerId)
                .distinct()
                .count();
        activeCustomersLabel.setText(String.valueOf(activeCustomers));

        // 3. Number of customers who have paid today
        long paidToday = todayPayments.stream()
                .map(PaymentDTO::getCustomerId)
                .distinct()
                .count();
        customersPaidTodayLabel.setText(String.valueOf(paidToday));

        // 4. Collection Rate
        double rate = activeCustomers > 0 ? (double) paidToday / activeCustomers * 100 : 0;
        collectionRateLabel.setText(String.format("%.2f%%", rate));

        // 5. New customers today
        newCustomersTodayLabel.setText("0");

        // 6. Total collections (today)
        BigDecimal totalColl = todayPayments.stream()
                .map(PaymentDTO::getAmountReceived)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalCollectionsLabel.setText(String.format("%.2f", totalColl));

        // 7. Number of loans disbursed (today)
        loansDisbursedLabel.setText(String.valueOf(todayLoans.size()));

        // 8. Total amount disbursed (today)
        BigDecimal totalDisbursedToday = todayLoans.stream()
                .map(LoanDTO::getPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalAmountDisbursedLabel.setText(String.format("%.2f", totalDisbursedToday));

        // 9. Principal Balance (Total outstanding principal)
        BigDecimal principalBalance = allLoans.stream()
                .map(LoanDTO::getOutstandingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        principalBalanceLabel.setText(String.format("%.2f", principalBalance));

        // 10. Total loan portfolio (Total principal ever disbursed)
        BigDecimal totalPortfolio = allLoans.stream()
                .map(LoanDTO::getPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLoanPortfolioLabel.setText(String.format("%.2f", totalPortfolio));

        // 11-18 Placeholders for more complex accounting fields
        openingCashLabel.setText("0.00");
        principalCollectedLabel.setText(String.format("%.2f", totalColl.multiply(new BigDecimal("0.8")))); // Approx
        interestCollectedLabel.setText(String.format("%.2f", totalColl.multiply(new BigDecimal("0.2")))); // Approx
        processingFeesLabel.setText("0.00");
        bankDepositsLabel.setText("0.00");
        totalExpensesLabel.setText("0.00");
        loanDisbursementsLabel.setText(String.format("%.2f", totalDisbursedToday));
        checkoutCashLabel.setText(String.format("%.2f", totalColl.subtract(totalDisbursedToday)));
    }

    @FXML
    private void handlePrintPdf() {
        System.out.println("Printing PDF summary...");
    }

    @FXML
    private void handleSendEmail() {
        reportService.sendDailyReport(LocalDate.now());
    }

    @FXML
    private void handleExportExcel() {
        System.out.println("Exporting Excel summary...");
    }

    @FXML
    private void handleNavCustomers() {
        dashboardController.showCustomerView();
    }

    @FXML
    private void handleNavLoans() {
        dashboardController.showLoanView();
    }

    @FXML
    private void handleNavPayments() {
        dashboardController.showPaymentsView();
    }
}
