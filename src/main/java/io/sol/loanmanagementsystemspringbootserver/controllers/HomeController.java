package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.*;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailDetails;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailsService;
import io.sol.loanmanagementsystemspringbootserver.services.*;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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
    private final EmailsService emailsService;
    private final DashboardController dashboardController;

    @FXML
    private Label totalCustomersLabel;
    @FXML
    private Label activeCustomersLabel;
    @FXML
    private Label customersPaidTodayLabel;
    @FXML
    private Label collectionRateLabel; //a percentage of the days collections that has been done
    @FXML
    private Label newCustomersTodayLabel;//customers registered today
    @FXML
    private Label totalCollectionsLabel;//money collected as loan repayments
    @FXML
    private Label loansDisbursedLabel; //number of loans given out today
    @FXML
    private Label totalAmountDisbursedLabel;//money issued out as loans
    @FXML
    private Label principalBalanceLabel; //money in the system
    @FXML
    private Label totalLoanPortfolioLabel; //the combined balance of all outstanding loans issued and //TODO their interests
    @FXML
    private Label openingCashLabel; //amount present at hand
    @FXML
    private Label principalCollectedLabel; //describes the amount of principal amounts collected, i.e, without looking at the interest yet
    @FXML
    private Label interestCollectedLabel;//describes the amount collected as interest todya
    @FXML
    private Label processingFeesLabel; //describes the amount of money taken from the principal as a fee
    @FXML
    private Label bankDepositsLabel; //expense showing how much has been deposited into the bank
    @FXML
    private Label totalExpensesLabel; //total expenses in a day
    @FXML
    private Label loanDisbursementsLabel;
    @FXML
    private Label checkoutCashLabel;

    @FXML
    private TableView<CustomerDTO> dueCustomersTable;
    @FXML
    private TableColumn<CustomerDTO, String> customerAccountNumber;

    @FXML
    private TableColumn<CustomerDTO, String> customerName;
    @FXML
    private TableColumn<CustomerDTO, String> customerTelephone;
    @FXML
    private TableColumn<CustomerDTO, String> guarantorName;
    @FXML
    private TableColumn<CustomerDTO, String> agingDays;

    private Map<String, String> statsMap = new LinkedHashMap<>();



    public HomeController(LoansService loansService, CustomerService customerService, 
                          PaymentsService paymentsService,
                          EmailsService emailsService,
                          @Lazy DashboardController dashboardController) {
        this.loansService = loansService;
        this.customerService = customerService;
        this.paymentsService = paymentsService;
        this.emailsService = emailsService;
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {

        fillDueCustomersTable();
        calculateDailyStats();
    }


    private void calculateDailyStats() {
        LocalDate today = LocalDate.now();
        List<CustomerDTO> allCustomers = customerService.getAllCustomers().value();
        List<LoanDTO> allLoans = loansService.getAllLoans().value();
        List<PaymentDTO> allPayments = paymentsService.getAllPayments().value();
        List<PaymentDTO> todayPayments = paymentsService.getPaymentsByDate(today).value();
        
        List<LoanDTO> todayLoans = allLoans.stream()
                .filter(l -> today.equals(l.getStartDate()))
                .toList();

        statsMap.clear();

        updateStat("Total Customers", String.valueOf(allCustomers.size()), totalCustomersLabel);

        long activeCustomers = allLoans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .map(LoanDTO::getCustomerId)
                .distinct()
                .count();
        updateStat("Active Customers", String.valueOf(activeCustomers), activeCustomersLabel);

        long paidToday = todayPayments.stream()
                .map(PaymentDTO::getCustomerId)
                .distinct()
                .count();
        updateStat("Customers Paid Today", String.valueOf(paidToday), customersPaidTodayLabel);

        double rate = activeCustomers > 0 ? (double) paidToday / activeCustomers * 100 : 0;
        updateStat("Collection Rate", String.format("%.2f%%", rate), collectionRateLabel);

        updateStat("New Customers Today", "0", newCustomersTodayLabel);

        BigDecimal totalColl = todayPayments.stream()
                .map(PaymentDTO::getAmountReceived)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        updateStat("Total Collections (Today)", String.format("%.2f", totalColl), totalCollectionsLabel);

        updateStat("Loans Disbursed (Today)", String.valueOf(todayLoans.size()), loansDisbursedLabel);

        BigDecimal totalDisbursedToday = todayLoans.stream()
                .map(LoanDTO::getPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        updateStat("Total Amount Disbursed (Today)", String.format("%.2f", totalDisbursedToday), totalAmountDisbursedLabel);

        BigDecimal principalBalance = allLoans.stream()
                .map(LoanDTO::getOutstandingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        updateStat("Principal Balance", String.format("%.2f", principalBalance), principalBalanceLabel);

        BigDecimal totalPortfolio = allLoans.stream()
                .map(LoanDTO::getPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        updateStat("Total Loan Portfolio", String.format("%.2f", totalPortfolio), totalLoanPortfolioLabel);

        updateStat("Opening Cash", "0.00", openingCashLabel);
        updateStat("Principal Collected", String.format("%.2f", totalColl.multiply(new BigDecimal("0.8"))), principalCollectedLabel);
        updateStat("Interest Collected", String.format("%.2f", totalColl.multiply(new BigDecimal("0.2"))), interestCollectedLabel);
        updateStat("Processing Fees", "0.00", processingFeesLabel);
        updateStat("Bank Deposits", "0.00", bankDepositsLabel);
        updateStat("Total Expenses", "0.00", totalExpensesLabel);
        updateStat("Loan Disbursements", String.format("%.2f", totalDisbursedToday), loanDisbursementsLabel);
        updateStat("Checkout Cash", String.format("%.2f", totalColl.subtract(totalDisbursedToday)), checkoutCashLabel);
    }

    private void fillDueCustomersTable(){


    }
    private void updateStat(String key, String value, Label label) {
        statsMap.put(key, value);
        if (label != null) label.setText(value);
    }

    @FXML
    private void handlePrintPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.setInitialFileName("DailyReport_" + LocalDate.now() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (PdfWriter writer = new PdfWriter(new FileOutputStream(file));
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                document.add(new Paragraph("Daily Summary Report - " + LocalDate.now()).setBold().setFontSize(18));
                document.add(new Paragraph(" "));

                Table table = new Table(2);
                for (Map.Entry<String, String> entry : statsMap.entrySet()) {
                    table.addCell(entry.getKey());
                    table.addCell(entry.getValue());
                }
                document.add(table);
                UIHelper.showInfo("Success", "PDF report generated successfully.");
            } catch (Exception e) {
                UIHelper.showError("Error", "Failed to generate PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel Report");
        fileChooser.setInitialFileName("DailyReport_" + LocalDate.now() + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook();
                 FileOutputStream fileOut = new FileOutputStream(file)) {
                Sheet sheet = workbook.createSheet("Daily Summary");
                int rowNum = 0;
                for (Map.Entry<String, String> entry : statsMap.entrySet()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue());
                }
                workbook.write(fileOut);
                UIHelper.showInfo("Success", "Excel report exported successfully.");
            } catch (IOException e) {
                UIHelper.showError("Error", "Failed to export Excel: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSendEmail() {
        Dialog<EmailOptions> dialog = new Dialog<>();
        dialog.setTitle("Send Email Report");
        dialog.setHeaderText("Select email recipient and details to include");

        ButtonType sendButtonType = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        TextField emailField = new TextField();
        emailField.setPromptText("Recipient Email");
        
        Label optionsLabel = new Label("Select stats to include:");
        VBox optionsBox = new VBox(5);
        Map<CheckBox, String> checkBoxes = new LinkedHashMap<>();
        for (String key : statsMap.keySet()) {
            CheckBox cb = new CheckBox(key);
            cb.setSelected(true);
            checkBoxes.put(cb, key);
            optionsBox.getChildren().add(cb);
        }
        
        ScrollPane scrollPane = new ScrollPane(optionsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);

        content.getChildren().addAll(new Label("To:"), emailField, optionsLabel, scrollPane);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == sendButtonType) {
                List<String> selectedKeys = new ArrayList<>();
                for (Map.Entry<CheckBox, String> entry : checkBoxes.entrySet()) {
                    if (entry.getKey().isSelected()) {
                        selectedKeys.add(entry.getValue());
                    }
                }
                return new EmailOptions(emailField.getText(), selectedKeys);
            }
            return null;
        });

        Optional<EmailOptions> result = dialog.showAndWait();
        result.ifPresent(options -> {
            if (options.recipient == null || options.recipient.isBlank()) {
                UIHelper.showError("Error", "Recipient email is required.");
                return;
            }
            
            StringBuilder body = new StringBuilder("Daily Summary Report - " + LocalDate.now() + "\n\n");
            for (String key : options.selectedKeys) {
                body.append(key).append(": ").append(statsMap.get(key)).append("\n");
            }

            EmailDetails details = new EmailDetails();
            details.setRecipient(options.recipient);
            details.setSubject("Daily Summary Report - " + LocalDate.now());
            details.setBody(body.toString());

            String status = String.valueOf(emailsService.sendSimpleMail(details));
           if(status.contains("error")){
               UIHelper.showError("ERROR", "An error ocurred while sending this email");
           }
        });
    }

    private static class EmailOptions {
        String recipient;
        List<String> selectedKeys;
        EmailOptions(String recipient, List<String> selectedKeys) {
            this.recipient = recipient;
            this.selectedKeys = selectedKeys;
        }
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
