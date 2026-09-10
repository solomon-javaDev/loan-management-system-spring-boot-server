package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransaction;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.CashTransactionType;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Expense;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.ExpenseCategory;
import io.sol.loanmanagementsystemspringbootserver.services.*;
import io.sol.loanmanagementsystemspringbootserver.utilities.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
    private final UserSession userSession;

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
    @FXML private TableColumn<Expense, BigDecimal> recordAmountColumn;

    @FXML private TableView<CashTransaction> customerSavingsTable;
    @FXML private TableColumn<CashTransaction, String> savingsDateColumn;
    @FXML private TableColumn<CashTransaction, String> savingsCustomerNameColumn;
    @FXML private TableColumn<CashTransaction, String> savingsMovementColumn;
    @FXML private TableColumn<CashTransaction, BigDecimal> savingsAmountColumn;
    @FXML private TableColumn<CashTransaction, BigDecimal> savingsTotalBalanceColumn;


    public FinanceController(ExpenseService expenseService, CustomerService customerService, 
                             UiControlUtilities uiControlUtilities, ExpenseCategoryService expenseCategoryService,
                             CashTransactionService cashTransactionService, UserSession userSession) {
        this.expenseService = expenseService;
        this.customerService = customerService;
        this.uiControlUtilities = uiControlUtilities;
        this.expenseCategoryService = expenseCategoryService;
        this.cashTransactionService = cashTransactionService;
        this.userSession = userSession;
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

        int customerId = Integer.parseInt(custIdStr);
        boolean outstandingLoan = cashTransactionService.hasOutstandingLoan(customerId);
        if (outstandingLoan && !userSession.isAdmin()) {
            show("This customer has an uncleared loan. Only an administrator may process savings for this customer.");
            return;
        }
        if (outstandingLoan && !confirmAdminSavingsTransaction()) {
            return;
        }

        CashTransaction transaction = new CashTransaction();
        transaction.setDate(LocalDateTime.now());
        transaction.setCustomerId(customerId);
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

    private boolean confirmAdminSavingsTransaction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Administrator confirmation required");
        alert.setHeaderText("Customer has an uncleared loan");
        alert.setContentText("Only an administrator may process this savings transaction. Continue?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void configureTables() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        recordDateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDate() == null ? "" : c.getValue().getDate().format(fmt)));
        recordDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        recordCategoryColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategory() != null ? c.getValue().getCategory().getDescription() : ""));
        recordAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        recordAmountColumn.setCellFactory(UiControlUtilities.formattedCell(MoneyHelper::formatMoney));

        savingsDateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDate() == null ? "" : c.getValue().getDate().format(fmt)));
        savingsCustomerNameColumn.setCellValueFactory(c -> {
            Integer id = c.getValue().getCustomerId();
            if (id == null) return new SimpleStringProperty("");
            Result<io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO> customerResult = customerService.getCustomerById(id);
            return new SimpleStringProperty(customerResult.isSuccess() ? customerResult.value().getCustomerName() : "ID: " + id);
        });
        savingsMovementColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().toString()));
        savingsAmountColumn.setCellValueFactory(c ->
            new ReadOnlyObjectWrapper<>(c.getValue().getAmount()));
        savingsAmountColumn.setCellFactory(UiControlUtilities.formattedCell(MoneyHelper::formatMoney));

        savingsTotalBalanceColumn.setCellValueFactory(c -> {
            Integer id = c.getValue().getCustomerId();
            if (id == null) {
            return new ReadOnlyObjectWrapper<>(BigDecimal.ZERO);
            }
            Result<CustomerDTO> customerResult = customerService.getCustomerById(id);
            BigDecimal balance = customerResult.isSuccess()
                ? customerResult.value().getSavingsBalance()
                : BigDecimal.ZERO;
            return new ReadOnlyObjectWrapper<>(
                balance == null ? BigDecimal.ZERO : balance);
        });
        savingsTotalBalanceColumn.setCellFactory(UiControlUtilities.formattedCell(MoneyHelper::formatMoney));

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

    @FXML
    private void deleteSelectedExpense() {
        Expense selected = expensesTable.getSelectionModel().getSelectedItem();
        if (!userSession.isAdmin()) {
            show("Only administrators can edit finance records.");
            return;
        }
        if (selected == null || !confirmDelete("expense")) return;
        Result<Void> result = expenseService.deleteExpense(selected.getId());
        show(result.message());
        if (result.isSuccess()) loadExpenses();
    }

    @FXML
    private void deleteSelectedSavings() {
        CashTransaction selected = customerSavingsTable.getSelectionModel().getSelectedItem();
        if (!userSession.isAdmin()) {
            show("Only administrators can edit finance records.");
            return;
        }
        if (selected == null || !confirmDelete("savings transaction")) return;
        Result<Void> result = cashTransactionService.deleteTransaction(selected.getId());
        show(result.message());
        if (result.isSuccess()) loadSavings();
    }

    private boolean confirmDelete(String recordType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm finance record edit");
        alert.setHeaderText("Delete selected " + recordType + "?");
        alert.setContentText("This action will be recorded in the audit log.");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private BigDecimal parseAmount(String value) {
        try { return new BigDecimal(value == null ? "" : value.trim()); }
        catch (NumberFormatException exception) { return null; }
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
