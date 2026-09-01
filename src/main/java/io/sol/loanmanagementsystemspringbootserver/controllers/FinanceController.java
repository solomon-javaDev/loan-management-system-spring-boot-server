package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransaction;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransactionType;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Expense;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.ExpenseCategory;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.Customer;
import io.sol.loanmanagementsystemspringbootserver.services.*;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import io.sol.loanmanagementsystemspringbootserver.utilities.UiControlUtilities;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class FinanceController {
    private final ExpenseService expenseService;
    private final CustomerService customerService;
    private final UiControlUtilities uiControlUtilities;
    private final ExpenseCategoryService expenseCategoryService;
    private final CashTransactionService cashTransactionService;

    @FXML private TextField expenseDescription;
    @FXML private ComboBox<ExpenseCategory> expenseCategoryDropDown;
    @FXML private TextField expenseAmount;

    @FXML private ComboBox<String> savingsCustomer;
    @FXML private TextField savingsCustomerId;
    @FXML private ComboBox<String> savingsType;
    @FXML private TextField savingsAmount;
    @FXML private Label messageLabel;

    @FXML private TableView<Expense> expensesTable;
    @FXML private TableColumn<Expense, String> recordDateColumn;
    @FXML private TableColumn<Expense, String> recordDescriptionColumn;
    @FXML private TableColumn<Expense, String> recordCategoryColumn;
    @FXML private TableColumn<Expense, String> recordAmountColumn;

    @FXML private TableView<CashTransaction> customerSavingsTable;
    @FXML private TableColumn<CashTransaction, String> savingsDateColumn;
    @FXML private TableColumn<CashTransaction, String> savingsCustomerNameColumn;
    @FXML private TableColumn<CashTransaction, String> savingsMovementColumn;
    @FXML private TableColumn<CashTransaction, String> savingsAmountColumn;
    @FXML private TableColumn<CashTransaction, String> savingsTotalBalanceColumn;


    public FinanceController(ExpenseService expenseService, CustomerService customerService, 
                             UiControlUtilities uiControlUtilities, ExpenseCategoryService expenseCategoryService,
                             CashTransactionService cashTransactionService) {
        this.expenseService = expenseService;
        this.customerService = customerService;
        this.uiControlUtilities = uiControlUtilities;
        this.expenseCategoryService = expenseCategoryService;
        this.cashTransactionService = cashTransactionService;
    }

    @FXML
    public void initialize() {
        uiControlUtilities.configureDropDown(expenseCategoryDropDown, expenseCategoryService.getAllCategories().value(), e -> e.getDescription());
        
        customerService.getAllCustomers().value().forEach(customer ->
                savingsCustomer.getItems().add(customer.getId() + " - " + customer.getCustomerName()));
        
        savingsCustomer.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) savingsCustomerId.setText(newValue.split(" - ", 2)[0]);
        });

        savingsType.getItems().addAll("DEPOSIT", "WITHDRAWAL");

        configureTables();
        loadExpenses();
        loadSavings();
    }

    @FXML
    private void recordExpense() {
        Expense expense = new Expense();

        ExpenseCategory selectedCategory = expenseCategoryDropDown.getValue();
        if(selectedCategory == null){
            show("Select a category first");
            return;
        }

        BigDecimal amount = parseAmount(expenseAmount.getText());
        if(amount == null){
            show("Enter a valid expense amount");
            return;
        }

        expense.setDescription(expenseDescription.getText());
        expense.setCategory(selectedCategory);
        expense.setAmount(amount);

        Result<Expense> result = expenseService.recordExpense(expense);
        show(result.message());

        if (result.isSuccess()) {
            UIHelper.showInfo("SUCCESS", result.message());
            loadExpenses();
            expenseDescription.clear();
            expenseAmount.clear();
        }
    }

    @FXML
    private void recordSavings() {
        String custIdStr = savingsCustomerId.getText();
        String type = savingsType.getValue();
        BigDecimal amount = parseAmount(savingsAmount.getText());

        if (custIdStr.isEmpty() || type == null || amount == null) {
            show("Please fill in all savings fields");
            return;
        }

        CashTransaction transaction = new CashTransaction();
        transaction.setDate(LocalDateTime.now());
        transaction.setCustomerId(Integer.parseInt(custIdStr));
        transaction.setAmount(amount);
        transaction.setType(type.equals("DEPOSIT") ? CashTransactionType.SAVINGS_DEPOSIT : CashTransactionType.SAVINGS_WITHDRAWAL);
        transaction.setDescription("Savings " + type + " for customer " + custIdStr);

        Result<CashTransaction> result = cashTransactionService.recordTransaction(transaction);
        show(result.message());

        if (result.isSuccess()) {
            UIHelper.showInfo("SUCCESS", result.message());
            loadSavings();
            savingsAmount.clear();
        }
    }

    private void configureTables() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        recordDateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDate() == null ? "" : c.getValue().getDate().format(fmt)));
        recordDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        recordCategoryColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategory() != null ? c.getValue().getCategory().getDescription() : ""));
        recordAmountColumn.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getAmount())));

        savingsDateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDate() == null ? "" : c.getValue().getDate().format(fmt)));
        savingsCustomerNameColumn.setCellValueFactory(c -> {
            Integer id = c.getValue().getCustomerId();
            if (id == null) return new SimpleStringProperty("");
            Result<io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO> customerResult = customerService.getCustomerById(id);
            return new SimpleStringProperty(customerResult.isSuccess() ? customerResult.value().getCustomerName() : "ID: " + id);
        });
        savingsMovementColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().toString()));
        savingsAmountColumn.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getAmount())));
        savingsTotalBalanceColumn.setCellValueFactory(c -> {
             // For simplicity in MVP, we might just show the transaction amount or fetch customer balance
             Integer id = c.getValue().getCustomerId();
             if (id == null) return new SimpleStringProperty("");
             Result<CustomerDTO> customerResult = customerService.getCustomerById(id);
             return new SimpleStringProperty(customerResult.isSuccess() ? formatAmount(customerResult.value().getSavingsBalance()) : "UPDATE COMING");
        });
    }

    private void loadExpenses() {
        expensesTable.setItems(FXCollections.observableArrayList(
                expenseService.getAllExpenses().value().stream()
                        .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                        .collect(Collectors.toList())));
    }

    private void loadSavings() {
        customerSavingsTable.setItems(FXCollections.observableArrayList(
                cashTransactionService.getAllTransactions().stream()
                        .filter(t -> t.getType() == CashTransactionType.SAVINGS_DEPOSIT || t.getType() == CashTransactionType.SAVINGS_WITHDRAWAL)
                        .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                        .collect(Collectors.toList())));
    }

    private BigDecimal parseAmount(String value) {
        try { return new BigDecimal(value == null ? "" : value.trim()); }
        catch (NumberFormatException exception) { return null; }
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private void show(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ATTENTION");
        alert.setContentText(message);
        ButtonType type = new ButtonType("OK");
        alert.setResult(type);
        alert.showAndWait();

        messageLabel.setText(message);
    }
}
