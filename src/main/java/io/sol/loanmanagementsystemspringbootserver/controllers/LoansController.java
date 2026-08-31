package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.EmployeeDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.LoanDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;
import io.sol.loanmanagementsystemspringbootserver.services.EmployeeService;
import io.sol.loanmanagementsystemspringbootserver.services.LoansService;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import io.sol.loanmanagementsystemspringbootserver.utilities.UiControlUtilities;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller class for managing loan-related operations in a UI context.
 * Responsible for handling the loans' view, initializing components,
 * and managing user interactions such as creating, updating, deleting,
 * and filtering loans.
 *
 * This class manages form components, data binding for the table view,
 * and delegation of business logic to the associated services.
 *
 * Primary functionalities include:
 * - Loading all loans into the table.
 * - Managing and clearing the loan form.
 * - Issuing a new loan.
 * - Updating existing loans.
 * - Deleting loans.
 */

@Component
public class LoansController {

    private final LoansService loansService;
    private final EmployeeService employeeService;
    private final CustomerService customerService;
    private final UiControlUtilities uiControlUtilities;
    private final io.sol.loanmanagementsystemspringbootserver.services.FeeBucketService feeBucketService;
    private final io.sol.loanmanagementsystemspringbootserver.utilities.UserSession userSession;

    @FXML
    private TableView<LoanDTO> loansTable;

    @FXML
    private TableColumn<LoanDTO, Integer> idColumn;

    @FXML
    private TableColumn<LoanDTO, String> customerNameColumn;

    @FXML
    private TableColumn<LoanDTO, LocalDate> startDateColumn;

    @FXML
    private TableColumn<LoanDTO, LocalDate> maturityDateColumn;

    @FXML
    private TableColumn<LoanDTO, LocalDate> fullPaidDateColumn;

    @FXML
    private TableColumn<LoanDTO, BigDecimal> principalColumn;

    @FXML
    private TableColumn<LoanDTO, BigDecimal> interestRateColumn;

    @FXML
    private TableColumn<LoanDTO, Integer> tenorColumn;

    @FXML
    private TableColumn<LoanDTO, String> collateralColumn;

    @FXML
    private TableColumn<LoanDTO, BigDecimal> feesColumn;

    @FXML
    private TableColumn<LoanDTO, String> statusColumn;

    @FXML
    private TableColumn<LoanDTO, String> fieldOfficerColumn;

    @FXML
    private TableColumn<LoanDTO, BigDecimal> fullPayment;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker maturityDatePicker;

    @FXML
    private TextField principalField;

    @FXML
    private TextField interestRateField;

    @FXML
    private TextField tenorField;

    @FXML
    private TextField collateralField;

    @FXML
    private TextField feesField;

    @FXML
    private ComboBox<LoanStatus> statusField;

    @FXML
    private ComboBox<EmployeeDTO> fieldOfficerField;

    @FXML
    private ComboBox<CustomerDTO> customerList;

    @FXML
    private Button saveButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button clearButton;

    @FXML
    private ComboBox<CustomerDTO> guarantorDropDown;

    @FXML
    private Label messageLabel;

    public LoansController(LoansService loansService, EmployeeService employeeService, CustomerService customerService, UiControlUtilities uiControlUtilities, io.sol.loanmanagementsystemspringbootserver.services.FeeBucketService feeBucketService, io.sol.loanmanagementsystemspringbootserver.utilities.UserSession userSession) {
        this.loansService = loansService;
        this.employeeService = employeeService;
        this.customerService = customerService;
        this.uiControlUtilities = uiControlUtilities;
        this.feeBucketService = feeBucketService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        System.out.println("Loaded the loans view");
        configureTable();

        uiControlUtilities.configureDropDown(customerList, customerService.getAllCustomers().value(),
                c -> c.getFirstName() + " " + c.getLastName()
                );

        uiControlUtilities.configureDropDown(fieldOfficerField, employeeService.getEmployeeByRole(Role.FIELD_OFFICER).value(),
                e -> e.getFirstName() + " " + e.getLastName()
                );

        statusField.setItems(FXCollections.observableArrayList(LoanStatus.values()));
        statusField.setValue(LoanStatus.PENDING);

        maturityDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> computeTenorFromDates());
        startDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> computeTenorFromDates());
        principalField.textProperty().addListener((obs, oldValue, newValue) -> {
            refreshGuarantorOptions();
            if (newValue != null && !newValue.isEmpty()) {
                try {
                    BigDecimal principal = new BigDecimal(newValue);
                    BigDecimal fee = feeBucketService.calculateFee(principal);
                    feesField.setText(fee.toPlainString());
                } catch (NumberFormatException e) {
                    // Ignore invalid numbers
                }
            }
        });

        saveButton.disableProperty().bind(loansTable.getSelectionModel().selectedItemProperty().isNotNull());
        updateButton.disableProperty().bind(loansTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(loansTable.getSelectionModel().selectedItemProperty().isNull());

        loadLoans();
        messageLabel.setText("Loans loaded successfully");


        loansTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedLoan) -> {
            if (selectedLoan != null) {
                populateForm(selectedLoan);
            }
        });
    }

    
    private void loadLoans() {
        Result<List<LoanDTO>> result = loansService.getAllLoans();
        loansTable.setItems(FXCollections.observableArrayList(result.value()));
    }


        private void refreshGuarantorOptions() {
        BigDecimal principal = parseBigDecimal(principalField.getText());

    }

    private LoanDTO buildLoanFromForm() {
        LoanDTO loan = new LoanDTO();
        loan.setStartDate(startDatePicker.getValue());
        loan.setMaturityDate(maturityDatePicker.getValue());
        loan.setPrincipal(parseBigDecimal(principalField.getText()));
        loan.setInterestRate(parseBigDecimal(interestRateField.getText()));
        loan.setTenor(parseInt(tenorField.getText()));
        loan.setCollateral(collateralField.getText() == null ? "" : collateralField.getText().trim());
        loan.setFees(parseBigDecimal(feesField.getText()));
        loan.setStatus(statusField.getValue() != null ? statusField.getValue() : LoanStatus.PENDING);
        if (fieldOfficerField.getValue() != null) {
            loan.setFieldOfficerId(fieldOfficerField.getValue().getId());
            loan.setFieldOfficerName(fieldOfficerField.getValue().toString());
        }
        if (customerList.getValue() != null) {
            loan.setCustomerId(customerList.getValue().getId());
            loan.setCustomerName(customerList.getValue().getCustomerName());
        }
        if (guarantorDropDown.getValue() != null) {
            loan.setGuarantorId((long) guarantorDropDown.getValue().getId());
            loan.setGuarantorName(guarantorDropDown.getValue().getCustomerName());
        }
        return loan;
    }

    private void populateForm(LoanDTO loan) {
        startDatePicker.setValue(loan.getStartDate());
        maturityDatePicker.setValue(loan.getMaturityDate());
        principalField.setText(loan.getPrincipal() == null ? "" : loan.getPrincipal().toPlainString());
        interestRateField.setText(loan.getInterestRate() == null ? "" : loan.getInterestRate().toPlainString());
        tenorField.setText(String.valueOf(loan.getTenor()));
        collateralField.setText(loan.getCollateral());
        feesField.setText(loan.getFees() == null ? "" : loan.getFees().toPlainString());
        statusField.setValue(loan.getStatus() != null ? loan.getStatus() : LoanStatus.PENDING);

        if (loan.getFieldOfficerId() != null) {
             // Find in combo box
             fieldOfficerField.getItems().stream()
                .filter(e -> e.getId().equals(loan.getFieldOfficerId()))
                .findFirst()
                .ifPresent(e -> fieldOfficerField.setValue(e));
        } else {
            fieldOfficerField.getSelectionModel().clearSelection();
        }
        
        if (loan.getCustomerId() != null) {
            customerList.getItems().stream()
                .filter(c -> c.getId() == loan.getCustomerId())
                .findFirst()
                .ifPresent(c -> customerList.setValue(c));
        } else {
            customerList.getSelectionModel().clearSelection();
        }

        if (loan.getGuarantorId() != null) {
            guarantorDropDown.getItems().stream()
                .filter(c -> c.getId() == loan.getGuarantorId().intValue())
                .findFirst()
                .ifPresent(c -> guarantorDropDown.setValue(c));
        } else {
            guarantorDropDown.getSelectionModel().clearSelection();
        }
    }


    @FXML
    private void handleSaveLoan() {
        LoanDTO loan = buildLoanFromForm();

        if (loan.getCustomerId() == null) {
            UIHelper.showError("Error", "Select a customer first.");
            return;
        }
        
        if (loan.getStartDate() != null && !loan.getStartDate().equals(LocalDate.now())) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Loan Date Warning");
            alert.setHeaderText("Non-Standard Loan Date");
            alert.setContentText("The loan date is not today. This requires admin approval. Do you wish to proceed?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        Result<LoanDTO> result = loansService.issueLoan(loan.getCustomerId(), loan);
        UIHelper.updateStatusLabel(messageLabel, result);

        if (result.isSuccess()) {
            loadLoans();
            clearForm();
        }

        clearForm();
    }

    @FXML
    private void handleUpdateLoan() {
        LoanDTO selectedLoan = loansTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null) {
            UIHelper.showError("Error", "Select a loan from the table first.");
            return;
        }

        LoanDTO loan = buildLoanFromForm();
        loan.setId(selectedLoan.getId());
        
        boolean sensitiveChange = !loan.getPrincipal().equals(selectedLoan.getPrincipal()) ||
                                  !loan.getInterestRate().equals(selectedLoan.getInterestRate()) ||
                                  !loan.getFees().equals(selectedLoan.getFees()) ||
                                  !loan.getStartDate().equals(selectedLoan.getStartDate());
        
        if (sensitiveChange) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sensitive Update Warning");
            alert.setHeaderText("Critical Parameter Change Detected");
            alert.setContentText("You are attempting to change critical loan parameters (Principal, Interest Rate, Fees, or Date). " +
                                "This action will be permanently recorded and an alert will be sent to the administrator. " +
                                "Do you want to proceed?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        Result<LoanDTO> result = loansService.updateLoan(loan);
        UIHelper.updateStatusLabel(messageLabel, result);

        if (result.isSuccess()) {
            loadLoans();
            clearForm();
        }
    }

    @FXML
    private void handleDeleteLoan() {
        if (!userSession.isAdmin()) {
            messageLabel.setText("Only Admin can delete loans.");
            return;
        }

        LoanDTO selectedLoan = loansTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null) {
            messageLabel.setText("Select a loan to delete.");
            return;
        }

        Result<Void> result = loansService.deleteLoan(selectedLoan.getId());
        messageLabel.setText(result.message());

        if (result.isSuccess()) {
            loadLoans();
            clearForm();
        }
    }

    @FXML
    private void handleClearLoanForm() {
        clearForm();
    }

    private void configureTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getCustomerName();
            return new SimpleStringProperty(name != null ? name : "");
        });
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        maturityDateColumn.setCellValueFactory(new PropertyValueFactory<>("maturityDate"));
        fullPaidDateColumn.setCellValueFactory(new PropertyValueFactory<>("fullPaidDate"));
        principalColumn.setCellValueFactory(new PropertyValueFactory<>("principal"));
        interestRateColumn.setCellValueFactory(new PropertyValueFactory<>("interestRate"));
        tenorColumn.setCellValueFactory(new PropertyValueFactory<>("tenor"));
        collateralColumn.setCellValueFactory(new PropertyValueFactory<>("collateral"));
        feesColumn.setCellValueFactory(new PropertyValueFactory<>("fees"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        fullPayment.setCellValueFactory(new PropertyValueFactory<>("fullPayment"));

        fieldOfficerColumn.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getFieldOfficerName();
            return new SimpleStringProperty(name != null ? name : "");
        });
    }

    private void clearForm() {
        startDatePicker.setValue(null);
        maturityDatePicker.setValue(null);
        principalField.clear();
        interestRateField.clear();
        tenorField.clear();
        collateralField.clear();
        feesField.clear();
        statusField.setValue(LoanStatus.PENDING);
        fieldOfficerField.getSelectionModel().clearSelection();
        customerList.getSelectionModel().clearSelection();
        guarantorDropDown.getSelectionModel().clearSelection();
        loansTable.getSelectionModel().clearSelection();
    }

    private void computeTenorFromDates() {
        LocalDate start = startDatePicker.getValue();
        LocalDate maturity = maturityDatePicker.getValue();

        if (start == null || maturity == null) {
            tenorField.clear();
            return;
        }

        if (maturity.isBefore(start)) {
            tenorField.clear();
            messageLabel.setText("Maturity date cannot be earlier than the start date.");
            return;
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(start, maturity);

        tenorField.setText(String.valueOf(Math.max(days, 0)));
        messageLabel.setText("");
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value);
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value);
    }


}
