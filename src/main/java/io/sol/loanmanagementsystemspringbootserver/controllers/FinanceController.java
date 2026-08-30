package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.entities.*;
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
import java.time.format.DateTimeFormatter;

@Component
public class FinanceController {
    private final ExpenseService expenseService;
    private final CashTransactionService cashService;
    private final SavingsService savingsService;
    private final CustomerService customerService;
    private final UiControlUtilities uiControlUtilities;
    private final ExpenseCategoryService expenseCategoryService;

    @FXML private TextField expenseDescription;
    @FXML private ComboBox<ExpenseCategory> expenseCategoryDropDown;
    @FXML private TextField expenseAmount;

    @FXML private ComboBox<String> savingsCustomer;
    @FXML private TextField savingsCustomerId;
    @FXML private TextField savingsAmount;
    @FXML private ComboBox<SavingsTransactionType> savingsType;
    @FXML private Label messageLabel;

    @FXML private TableView<Expense> expensesTable;
    @FXML private TableColumn<Expense, String> recordDateColumn;
    @FXML private TableColumn<Expense, String> recordDescriptionColumn;
    @FXML private TableColumn<Expense, String> recordCategoryColumn;
    @FXML private TableColumn<Expense, String> recordAmountColumn;

    @FXML private TableView<SavingsTransaction> customerSavingsTable;
    @FXML private TableColumn<SavingsTransaction, String> savingsDateColumn;
    @FXML private TableColumn<SavingsTransaction, String> savingsCustomerNameColumn;
    @FXML private TableColumn<SavingsTransaction, String> savingsMovementColumn;
    @FXML private TableColumn<SavingsTransaction, String> savingsAmountColumn;
    @FXML private TableColumn<SavingsTransaction, String> savingsTotalBalanceColumn;

    public FinanceController(ExpenseService expenseService, CashTransactionService cashService,
                             SavingsService savingsService, CustomerService customerService, UiControlUtilities uiControlUtilities, ExpenseCategoryService expenseCategoryService) {
        this.expenseService = expenseService;
        this.cashService = cashService;
        this.savingsService = savingsService;
        this.customerService = customerService;
        this.uiControlUtilities = uiControlUtilities;
        this.expenseCategoryService = expenseCategoryService;
    }

    @FXML
    public void initialize() {

            uiControlUtilities.configureDropDown(expenseCategoryDropDown, expenseCategoryService.getAllCategories().value(), e -> e.getDescription());
            savingsType.setItems(FXCollections.observableArrayList(SavingsTransactionType.values()));
            customerService.getAllCustomers().value().forEach(customer ->
                    savingsCustomer.getItems().add(customer.getId() + " - " + customer.getCustomerName()));
            savingsCustomer.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) savingsCustomerId.setText(newValue.split(" - ", 2)[0]);
            });

            configureTables();
            loadExpenses();
            loadSavings();
    }


    @FXML
    private void recordExpense() {
        Expense expense = new Expense();

        ExpenseCategory selectedCategory = expenseCategoryDropDown.getValue();
        if(selectedCategory == null){
            show("Selecet a category first");
            return;
        }

        BigDecimal amount = parseAmount(expenseAmount.getText());
        if(amount == null){
            show("Enter a valid expense amount");
        }

        expense.setDescription(expenseDescription.getText());

        expense.setCategory(selectedCategory);

        expense.setAmount(amount);

        String message = expenseService.recordExpense(expense).message();
        show(message);

        UIHelper.showInfo("SUCCESS", message);
        loadExpenses();
    }

    @FXML
    private void recordSavings() {
        int id;
        try { id = Integer.parseInt(savingsCustomerId.getText()); }
        catch (NumberFormatException exception) { show("Select a customer first"); return; }

        Result<SavingsTransaction> savingsTransactionResult = savingsService.recordTransaction(id, savingsType.getValue(), parseAmount(savingsAmount.getText()), null);
        savingsCustomer.getSelectionModel().clearSelection();
        savingsType.getSelectionModel().clearSelection();
        savingsAmount.clear();
        String message = savingsTransactionResult.message();

        show(message);

        UIHelper.showInfo("SUCCESS", message);
        loadSavings();
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
        savingsCustomerNameColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCustomer() != null ? c.getValue().getCustomer().getCustomerName() : ""));
        savingsMovementColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getType() != null ? c.getValue().getType().name() : ""));
        savingsAmountColumn.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getAmount())));
        savingsTotalBalanceColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCustomer() != null && c.getValue().getCustomer().getSavingsBalance() != null
                        ? formatAmount(c.getValue().getCustomer().getSavingsBalance())
                        : "0.00"));
    }

    private void loadExpenses() {
        expensesTable.setItems(FXCollections.observableArrayList(
                expenseService.getAllExpenses().value().stream()
                        .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                        .toList()));
    }

    private void loadSavings() {
        customerSavingsTable.setItems(FXCollections.observableArrayList(
                savingsService.getAllTransactions().value().stream()
                        .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                        .toList()));
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

        messageLabel.setText(message);
    }
}
