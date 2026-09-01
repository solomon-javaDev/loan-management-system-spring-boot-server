package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.PaymentDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;
import io.sol.loanmanagementsystemspringbootserver.services.LoansService;
import io.sol.loanmanagementsystemspringbootserver.services.PaymentsService;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import io.sol.loanmanagementsystemspringbootserver.utilities.UiControlUtilities;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The PaymentsController class is responsible for managing the user interface interactions
 * related to payment operations in the application. It is a part of the UI layer and interacts
 * with various services to perform payment-related actions such as saving, updating, deleting,
 * and filtering payment records.
 * Responsibilities of this controller include:
 * - Configuring and initializing UI components such as tables, dropdowns, and other controls.
 * - Handling user actions and binding data to UI components.
 * - Communicating with services to fetch, update, and save data.
 * - Filtering payment records based on specific criteria (e.g., date, week, month).
 *
 * The PaymentsController integrates with the following services:
 * - CustomerService: For fetching and managing customer-related data.
 * - UiControlUtilities: For handling common UI configurations and utilities.
 * - LoansService: For retrieving and managing loan-related data.
 * - PaymentsService: For performing operations related to payments, such as fetching, saving,
 *   updating, and deleting payment records.
 * * The class uses FXML annotations to map UI components, ensuring seamless interaction with the
 * JavaFX framework.
 * * Key Features:
 * - TableView for displaying a list of payments with columns for date, customer, amount, and loan reference.
 * - Dropdowns for selecting customers and loans.
 * - Basic CRUD operations for payments.
 * - Input validation and UI feedback for user actions.
 * - Filtering of data based on specific time periods (day, week, month).
 *
 * Methods:
 * 1. initialize: Sets up the initial configuration for the UI components and binds listeners.
 * 2. configureTable: Configures the table columns for displaying payment information.
 * 3. loadData: Loads the initial data for customers, loans, and payments.
 * 4. filterLoansByCustomer: Filters loans based on a selected customer.
 * 5. handleFilterDay, handleFilterWeek, handleFilterMonth: Filters payments by specific timeframes.
 * 6. handleSavePayment: Saves a new payment based on user-provided data.
 * 7. handleUpdatePayment: Updates an existing payment based on changes made by the user.
 * 8. handleDeletePayment: Deletes a selected payment.
 * 9. handleClearForm: Clears all form fields and resets the UI state.
 * 10. populateForm: Populates form inputs when a payment is selected from the table.
 *
 * Dependencies:
 * - JavaFX components such as TableView, ComboBox, DatePicker, TextField, and Buttons.
 * - External services (CustomerService, UiControlUtilities, LoansService, PaymentsService).
 *
 * This class ensures a user-friendly interface for managing payments while maintaining robust
 * communication with backend services for data consistency.
 */

@Component
public class PaymentsController {

    private final CustomerService customerService;
    private final UiControlUtilities uiControlUtilities;
    private final LoansService loansService;
    private final PaymentsService paymentsService;

    @FXML
    private DatePicker date;

    @FXML
    private ComboBox<CustomerDTO> customerName;

    @FXML
    private TextField amountRecieved;

    @FXML
    private ComboBox<LoanDTO> loanReference;

    @FXML
    private TableView<PaymentDTO> paymentsTable;

    @FXML
    private TableColumn<PaymentDTO, LocalDate> dateColumn;

    @FXML
    private TableColumn<PaymentDTO, String> customerColumn;

    @FXML
    private TableColumn<PaymentDTO, BigDecimal> amountColumn;

    @FXML
    private TableColumn<PaymentDTO, BigDecimal> remainingColumn;

    @FXML
    private TableColumn<PaymentDTO, String> loanRefColumn;

    @FXML
    private Button paymentSaveButton;

    @FXML
    private Button paymentUpdateButton;

    @FXML
    private Button paymentDeleteButton;

    @FXML
    private Button paymentClearButton;

    @FXML
    private Label messageLabel;

    public PaymentsController(CustomerService customerService, UiControlUtilities uiControlUtilities, LoansService loansService, PaymentsService paymentsService) {
        this.customerService = customerService;
        this.uiControlUtilities = uiControlUtilities;
        this.loansService = loansService;
        this.paymentsService = paymentsService;
    }

    @FXML
    public void initialize(){
        configureTable();
        loadData();

        date.setValue(LocalDate.now());

        customerName.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterLoansByCustomer(newVal);
            } else {
                loanReference.setItems(FXCollections.emptyObservableList()); //there is no customer selected, so no loans to choose a payment for
            }
        });

        paymentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedPayment) -> {
            if (selectedPayment != null) {
                populateForm(selectedPayment);
            }
        });

        paymentSaveButton.disableProperty().bind(paymentsTable.getSelectionModel().selectedItemProperty().isNotNull());
        paymentUpdateButton.disableProperty().bind(paymentsTable.getSelectionModel().selectedItemProperty().isNull());
        paymentDeleteButton.disableProperty().bind(paymentsTable.getSelectionModel().selectedItemProperty().isNull());
    }

    private void configureTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amountReceived"));
        remainingColumn.setCellValueFactory(new PropertyValueFactory<>("remainingBalance"));

        customerColumn.setCellValueFactory(cellData -> {
            PaymentDTO payment = cellData.getValue();
            String name = payment.getCustomerName();
            return new SimpleStringProperty(name != null ? name : "");
        });

        loanRefColumn.setCellValueFactory(cellData -> {
            PaymentDTO payment = cellData.getValue();
            String ref = payment.getLoanReference();
            return new SimpleStringProperty(ref != null ? ref : "");
        });
    }

    @FXML
    public void loadData() {
        uiControlUtilities.configureDropDown(customerName, customerService.getAllCustomers().value(),
                c -> c.getFirstName() + " " + c.getLastName());
        
        uiControlUtilities.configureDropDown(loanReference, loansService.getAllLoans().value(),
                LoanDTO::getReference);

        Result<List<PaymentDTO>> payments = paymentsService.getAllPayments();
        paymentsTable.setItems(FXCollections.observableArrayList(payments.value()));
    }

    private void filterLoansByCustomer(CustomerDTO customer) {
        if (customer == null) return;
        
        Result<List<LoanDTO>> result = loansService.getAllLoans();
        List<LoanDTO> filteredLoans = result.value().stream()
                .filter(loan -> loan.getCustomerId() != null && loan.getCustomerId() == customer.getId())
                .filter(loan -> loan.getStatus() == LoanStatus.PENDING || loan.getStatus() == LoanStatus.ACTIVE)
                .toList();
        
        uiControlUtilities.configureDropDown(loanReference, filteredLoans, LoanDTO::getReference);
    }

    @FXML
    private void handleFilterDay() {
        Result<List<PaymentDTO>> result = paymentsService.getPaymentsByDate(LocalDate.now());
        paymentsTable.setItems(FXCollections.observableArrayList(result.value()));
    }

    @FXML
    private void handleFilterWeek() {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        Result<List<PaymentDTO>> result = paymentsService.getPaymentsBetween(startOfWeek, now);
        paymentsTable.setItems(FXCollections.observableArrayList(result.value()));
    }

    @FXML
    private void handleFilterMonth() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        Result<List<PaymentDTO>> result = paymentsService.getPaymentsBetween(startOfMonth, now);
        paymentsTable.setItems(FXCollections.observableArrayList(result.value()));
    }

    @FXML
    private void handleSavePayment() {
        LoanDTO selectedLoan = loanReference.getValue();
        if (selectedLoan == null) {
            messageLabel.setText("Please select a loan");
            return;
        }

        BigDecimal amount = parseBigDecimal(amountRecieved.getText());
        Result<PaymentDTO> result = paymentsService.savePayment(date.getValue(), amount, selectedLoan.getId());

        Map<String, String> paymentMap = new LinkedHashMap<>();
        paymentMap.put("Date - ", String.valueOf(result.value().getDate()));
        paymentMap.put("Amount paid - ", String.valueOf(result.value().getAmountReceived()));
        paymentMap.put("Loan balance - ", String.valueOf(result.value().getRemainingBalance()));
        paymentMap.put("Days Skipped  - ", null); //TODO I still need to identify a way of getting aging days into the different DTOS, including the customerDTO
        paymentMap.put("Signature of receiver - ", null);

        UIHelper.exportToPdf("Payment receipt -", "PAYMENT FOR LOAN", paymentMap, (Stage) paymentsTable.getScene().getWindow());
        UIHelper.updateStatusLabel(messageLabel, result);

        if (result.isSuccess()) {
            loadData();
            handleClearForm();

            // Show alert for loan closure or excess payment
            if (result.message().contains("loan fully paid and cleared") || result.message().contains("added to savings")) {
                UIHelper.showInfo("Loan Status Update", result.message());
            }
        }
        
    }

    @FXML
    private void handleUpdatePayment() {
        PaymentDTO selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        UIHelper.showWarning("WARNING: ", "Avoid updating payments!");

        BigDecimal amount = parseBigDecimal(amountRecieved.getText());
        Result<PaymentDTO> result = paymentsService.updatePayment(selected.getId(), date.getValue(), amount);
        UIHelper.updateStatusLabel(messageLabel, result);
        if (result.isSuccess()) {
            loadData();
            handleClearForm();
        }
    }

    @FXML
    private void handleDeletePayment() {
        PaymentDTO selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Result<Void> result = paymentsService.deletePayment(selected.getId());
        UIHelper.updateStatusLabel(messageLabel, result);
        if (result.isSuccess()) {
            loadData();
            handleClearForm();
        }
    }

    @FXML
    private void handleClearForm() {
        date.setValue(LocalDate.now());
        customerName.setValue(null);
        amountRecieved.clear();
        loanReference.setValue(null);
        paymentsTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    private void populateForm(PaymentDTO payment) {
        date.setValue(payment.getDate());
        amountRecieved.setText(payment.getAmountReceived().toString());
        
        if (payment.getCustomerId() != null) {
            customerName.getItems().stream()
                .filter(c -> c.getId() == payment.getCustomerId())
                .findFirst()
                .ifPresent(c -> customerName.setValue(c));
        }

        if (payment.getLoanId() != null) {
            loanReference.getItems().stream()
                .filter(l -> l.getId() == payment.getLoanId())
                .findFirst()
                .ifPresent(l -> loanReference.setValue(l));
        }
    }
    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    }
