package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.config.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Employee;
import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.services.EmployeeService;
import io.sol.loanmanagementsystemspringbootserver.services.LoansService;
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

    @FXML
    private TableView<Loan> loansTable;

    @FXML
    private TableColumn<Loan, Integer> idColumn;

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
    private TextField startDateField;

    @FXML
    private TextField maturityDateField;

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
    private ComboBox fieldOfficerField;

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

    public LoansController(LoansService loansService, EmployeeService employeeService) {
        this.loansService = loansService;
        this.employeeService = employeeService;
    }

    @FXML
    public void initialize() {
        System.out.println("Loaded the laons view");
        configureTable();
        initializeFieldOfficersDropDown();
        loadLoans();
        messageLabel.setText("Loans loaded successfully");


        loansTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedLoan) -> {
            if (selectedLoan != null) {
                populateForm(selectedLoan);
            }
        });
    }

    private void initializeFieldOfficersDropDown() {
        System.out.println("Initializing field officers");
        Result<List<Employee>> fieldOfficersResult = employeeService.getEmployeeByRole(Role.FIELD_OFFICER);

        if (fieldOfficersResult.isSuccess() && fieldOfficersResult.value() != null) {
            List<Employee> officersList = fieldOfficersResult.value();

            // Log the list size to the IDE console to verify the database is actually returning values
            System.out.println("DEBUG: Found " + officersList.size() + " field officers in database.");

            // Using a clean clear-and-set sequence to force JavaFX graphics layout refreshes
            fieldOfficerField.getItems().clear();
            fieldOfficerField.getItems().addAll(officersList);

            // Assigning the anonymous converter subclass cleanly
            fieldOfficerField.setConverter(new StringConverter<Employee>() {
                @Override
                public String toString(Employee employee) {
                    if (employee == null) {
                        return "";
                    }
                    // Handling optional null parameter cases safely to prevent UI string leaks
                    String first = employee.getFirstName() == null ? "" : employee.getFirstName();
                    String last = employee.getLastName() == null ? "" : employee.getLastName();
                    return (first + " " + last).trim();
                }

                @Override
                public Employee fromString(String s) {
                    return null; // ComboBox is read-only dropdown selection
                }
            });
        } else {
            String currentMessage = messageLabel.getText() == null ? "" : messageLabel.getText();
            String errorMessage = fieldOfficersResult != null ? fieldOfficersResult.message() : "Unknown Service Error";
            messageLabel.setText(currentMessage + "\n" + errorMessage);
            System.out.println(errorMessage);
        }
    }


    @FXML
    private void handleSaveLoan() {
        Loan loan = buildLoanFromForm();
        Result<Loan> result = loansService.createLoan(loan);
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
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        maturityDateColumn.setCellValueFactory(new PropertyValueFactory<>("maturityDate"));
        fullPaidDateColumn.setCellValueFactory(new PropertyValueFactory<>("fullPaidDate"));
        principalColumn.setCellValueFactory(new PropertyValueFactory<>("principal"));
        interestRateColumn.setCellValueFactory(new PropertyValueFactory<>("interestRate"));
        tenorColumn.setCellValueFactory(new PropertyValueFactory<>("tenor"));
        collateralColumn.setCellValueFactory(new PropertyValueFactory<>("collateral"));
        feesColumn.setCellValueFactory(new PropertyValueFactory<>("fees"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        fieldOfficerColumn.setCellValueFactory(new PropertyValueFactory<>("fieldOfficer"));
    }

    private void loadLoans() {
        Result<List<Loan>> result = loansService.getAllLoans();
        loansTable.setItems(FXCollections.observableArrayList(result.value()));
    }

    private Loan buildLoanFromForm() {
        Loan loan = new Loan();
        loan.setStartDate(parseDate(startDateField.getText()));
        loan.setMaturityDate(parseDate(maturityDateField.getText()));
        loan.setFullPaidDate(parseDate(startDateField.getText()));
        loan.setPrincipal(parseBigDecimal(principalField.getText()));
        loan.setInterestRate(parseBigDecimal(interestRateField.getText()));
        loan.setTenor(parseInt(tenorField.getText()));
        loan.setCollateral(collateralField.getText() == null ? "" : collateralField.getText().trim());
        loan.setFees(parseBigDecimal(feesField.getText()));
        loan.setStatus(LoanStatus.valueOf(statusField.getText() == null ? "" : statusField.getText().trim()));
        loan.setFieldOfficer((Employee) fieldOfficerField.getValue());
        return loan;
    }

    private void populateForm(Loan loan) {
        startDateField.setText(loan.getStartDate() == null ? "" : loan.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        maturityDateField.setText(loan.getMaturityDate() == null ? "" : loan.getMaturityDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        principalField.setText(loan.getPrincipal() == null ? "" : loan.getPrincipal().toPlainString());
        interestRateField.setText(loan.getInterestRate() == null ? "" : loan.getInterestRate().toPlainString());
        tenorField.setText(String.valueOf(loan.getTenor()));
        collateralField.setText(loan.getCollateral());
        feesField.setText(loan.getFees() == null ? "" : loan.getFees().toPlainString());
        statusField.setText(loan.getStatus().toString());
        if(loan.getFieldOfficer()!=null){
            fieldOfficerField.setValue(loan.getFieldOfficer());
        }else{
            fieldOfficerField.getSelectionModel().clearSelection();
        }
    }

    private void clearForm() {
        startDateField.clear();
        maturityDateField.clear();
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
