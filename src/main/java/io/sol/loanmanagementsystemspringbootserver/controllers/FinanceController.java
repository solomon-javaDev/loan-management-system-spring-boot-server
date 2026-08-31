package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.Expense;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.ExpenseCategory;
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
    private final CustomerService customerService;
    private final UiControlUtilities uiControlUtilities;
    private final ExpenseCategoryService expenseCategoryService;

    @FXML private TextField expenseDescription;
    @FXML private ComboBox<ExpenseCategory> expenseCategoryDropDown;
    @FXML private TextField expenseAmount;

    @FXML private ComboBox<String> savingsCustomer;
    @FXML private TextField savingsCustomerId;
    @FXML private TextField savingsAmount;
    @FXML private Label messageLabel;

    @FXML private TableView<Expense> expensesTable;
    @FXML private TableColumn<Expense, String> recordDateColumn;
    @FXML private TableColumn<Expense, String> recordDescriptionColumn;
    @FXML private TableColumn<Expense, String> recordCategoryColumn;
    @FXML private TableColumn<Expense, String> recordAmountColumn;


    public FinanceController(ExpenseService expenseService, CustomerService customerService, UiControlUtilities uiControlUtilities, ExpenseCategoryService expenseCategoryService) {
        this.expenseService = expenseService;
        this.customerService = customerService;
        this.uiControlUtilities = uiControlUtilities;
        this.expenseCategoryService = expenseCategoryService;
    }

    @FXML
    public void initialize() {

            uiControlUtilities.configureDropDown(expenseCategoryDropDown, expenseCategoryService.getAllCategories().value(), e -> e.getDescription());
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

    }

    private void configureTables() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        recordDateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDate() == null ? "" : c.getValue().getDate().format(fmt)));
        recordDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        recordCategoryColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCategory() != null ? c.getValue().getCategory().getDescription() : ""));
        recordAmountColumn.setCellValueFactory(c -> new SimpleStringProperty(formatAmount(c.getValue().getAmount())));


    }

    private void loadExpenses() {
        expensesTable.setItems(FXCollections.observableArrayList(
                expenseService.getAllExpenses().value().stream()
                        .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                        .toList()));
    }

    private void loadSavings() {

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
