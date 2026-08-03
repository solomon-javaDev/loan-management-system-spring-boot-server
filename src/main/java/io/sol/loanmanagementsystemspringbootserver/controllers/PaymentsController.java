package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.PaymentDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;
import io.sol.loanmanagementsystemspringbootserver.services.LoansService;
import io.sol.loanmanagementsystemspringbootserver.services.PaymentsService;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UiControlUtilities;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

        customerName.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterLoansByCustomer(newVal);
            } else {
                loanReference.setItems(FXCollections.emptyObservableList());
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

        Result<java.util.List<PaymentDTO>> payments = paymentsService.getAllPayments();
        paymentsTable.setItems(FXCollections.observableArrayList(payments.value()));
    }

    private void filterLoansByCustomer(CustomerDTO customer) {
        if (customer == null) return;
        
        Result<List<LoanDTO>> result = loansService.getAllLoans();
        java.util.List<LoanDTO> filteredLoans = result.value().stream()
                .filter(loan -> loan.getCustomerId() != null && loan.getCustomerId() == customer.getId())
                .filter(loan -> loan.getStatus() == LoanStatus.PENDING || loan.getStatus() == LoanStatus.ACTIVE)
                .toList();
        
        uiControlUtilities.configureDropDown(loanReference, filteredLoans, LoanDTO::getReference);
    }

    @FXML
    private void handleFilterDay() {
        Result<java.util.List<PaymentDTO>> result = paymentsService.getPaymentsByDate(LocalDate.now());
        paymentsTable.setItems(FXCollections.observableArrayList(result.value()));
    }

    @FXML
    private void handleFilterWeek() {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        Result<java.util.List<PaymentDTO>> result = paymentsService.getPaymentsBetween(startOfWeek, now);
        paymentsTable.setItems(FXCollections.observableArrayList(result.value()));
    }

    @FXML
    private void handleFilterMonth() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        Result<java.util.List<PaymentDTO>> result = paymentsService.getPaymentsBetween(startOfMonth, now);
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
        messageLabel.setText(result.message());
        if (result.isSuccess()) {
            loadData();
            handleClearForm();
        }
    }

    @FXML
    private void handleUpdatePayment() {
        PaymentDTO selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        BigDecimal amount = parseBigDecimal(amountRecieved.getText());
        Result<PaymentDTO> result = paymentsService.updatePayment(selected.getId(), date.getValue(), amount);
        messageLabel.setText(result.message());
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
        messageLabel.setText(result.message());
        if (result.isSuccess()) {
            loadData();
            handleClearForm();
        }
    }

    @FXML
    private void handleClearForm() {
        date.setValue(null);
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
