package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.*;
import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;
import io.sol.loanmanagementsystemspringbootserver.services.EmployeeService;
import io.sol.loanmanagementsystemspringbootserver.services.LoansService;
import io.sol.loanmanagementsystemspringbootserver.utilities.UiControlUtilities;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class LoansController {

    private final LoansService loansService;
    private final EmployeeService employeeService;
    private final CustomerService customerService;
    private final UiControlUtilities uiControlUtilities;

    @FXML
    private TableView<Loan> loansTable;

    @FXML
    private TableColumn<Loan, Integer> idColumn;

    @FXML
    private TableColumn<Loan, String> customerNameColumn;

    @FXML
    private TableColumn<Loan, LocalDate> startDateColumn;

    @FXML
    private TableColumn<Loan, LocalDate> maturityDateColumn;

    @FXML
    private TableColumn<Loan, LocalDate> fullPaidDateColumn;

    @FXML
    private TableColumn<Loan, BigDecimal> principalColumn;

    @FXML
    private TableColumn<Loan, BigDecimal> interestRateColumn;

    @FXML
    private TableColumn<Loan, Integer> tenorColumn;

    @FXML
    private TableColumn<Loan, String> collateralColumn;

    @FXML
    private TableColumn<Loan, BigDecimal> feesColumn;

    @FXML
    private TableColumn<Loan, String> statusColumn;

    @FXML
    private TableColumn<Loan, String> fieldOfficerColumn;

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
    private TextField statusField;

    @FXML
    private ComboBox<Employee> fieldOfficerField;

    @FXML
    private ComboBox<Customer> customerList;

    @FXML
    private Button saveButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button clearButton;

    @FXML
    private Label messageLabel;

    public LoansController(LoansService loansService, EmployeeService employeeService, CustomerService customerService, UiControlUtilities uiControlUtilities) {
        this.loansService = loansService;
        this.employeeService = employeeService;
        this.customerService = customerService;
        this.uiControlUtilities = uiControlUtilities;
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

        loadLoans();
        messageLabel.setText("Loans loaded successfully");


        loansTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedLoan) -> {
            if (selectedLoan != null) {
                populateForm(selectedLoan);
            }
        });
    }

    
    private void loadLoans() {
        Result<List<Loan>> result = loansService.getAllLoans();
        loansTable.setItems(FXCollections.observableArrayList(result.value()));
    }


        private Loan buildLoanFromForm() {
        Loan loan = new Loan();
        loan.setStartDate(startDatePicker.getValue());
        loan.setMaturityDate(maturityDatePicker.getValue());
        loan.setFullPaidDate(startDatePicker.getValue());
        loan.setPrincipal(parseBigDecimal(principalField.getText()));
        loan.setInterestRate(parseBigDecimal(interestRateField.getText()));
        loan.setTenor(parseInt(tenorField.getText()));
        loan.setCollateral(collateralField.getText() == null ? "" : collateralField.getText().trim());
        loan.setFees(parseBigDecimal(feesField.getText()));
        loan.setStatus(LoanStatus.valueOf(statusField.getText() == null ? "" : statusField.getText().trim()));
        loan.setFieldOfficer(fieldOfficerField.getValue());
        loan.setCustomer(customerList.getValue());
        return loan;
    }

    private void populateForm(Loan loan) {
        startDatePicker.setValue(loan.getStartDate());
        maturityDatePicker.setValue(loan.getMaturityDate());
        principalField.setText(loan.getPrincipal() == null ? "" : loan.getPrincipal().toPlainString());
        interestRateField.setText(loan.getInterestRate() == null ? "" : loan.getInterestRate().toPlainString());
        tenorField.setText(String.valueOf(loan.getTenor()));
        collateralField.setText(loan.getCollateral());
        feesField.setText(loan.getFees() == null ? "" : loan.getFees().toPlainString());
        statusField.setText(loan.getStatus().toString());
        if (loan.getFieldOfficer() != null) {
            fieldOfficerField.setValue(loan.getFieldOfficer());
        } else {
            fieldOfficerField.getSelectionModel().clearSelection();
        }
        if (loan.getCustomer() != null) {
            customerList.setValue(loan.getCustomer());
        } else {
            customerList.getSelectionModel().clearSelection();
        }
    }


    @FXML
    private void handleSaveLoan() {
        Loan loan = buildLoanFromForm();
        int id = loan.getCustomer().getId();
        Result<Loan> result = loansService.issueLoan(id, loan);
        messageLabel.setText(result.message());


        if (result.isSuccess()) {
            loadLoans();
            clearForm();
        }
    }

    @FXML
    private void handleUpdateLoan() {
        Loan selectedLoan = loansTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null) {
            messageLabel.setText("Select a loan from the table first.");
            return;
        }

        Loan loan = buildLoanFromForm();
        loan.setId(selectedLoan.getId());
        Result<Loan> result = loansService.updateLoan(loan);
        messageLabel.setText(result.message());

        if (result.isSuccess()) {
            loadLoans();
            clearForm();
        }
    }

    @FXML
    private void handleDeleteLoan() {
        Loan selectedLoan = loansTable.getSelectionModel().getSelectedItem();
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
            Customer customer = cellData.getValue().getCustomer();
            if (customer != null) {
                return new SimpleStringProperty(customer.getFirstName() + " " + customer.getLastName());
            }
            return new SimpleStringProperty("");
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

        // Custom cell value factory to display employee name instead of object
        fieldOfficerColumn.setCellValueFactory(cellData -> {
            Employee employee = cellData.getValue().getFieldOfficer();
            if (employee != null) {
                return new SimpleStringProperty(
                    employee.getFirstName() + " " + employee.getLastName()
                );
            }
            return new SimpleStringProperty("");
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
        statusField.clear();
        fieldOfficerField.getSelectionModel().clearSelection(); // Reset dropdown state
        loansTable.getSelectionModel().clearSelection();
        loansTable.getSelectionModel().clearSelection();
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
