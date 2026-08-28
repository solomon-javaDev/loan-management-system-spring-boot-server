package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.entities.*;
import io.sol.loanmanagementsystemspringbootserver.services.*;
import io.sol.loanmanagementsystemspringbootserver.utilities.UiControlUtilities;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
    @FXML private TextField cashDescription;
    @FXML private TextField cashAmount;
    @FXML private ComboBox<CashTransactionType> cashType;
    @FXML private ComboBox<String> savingsCustomer;
    @FXML private TextField savingsCustomerId;
    @FXML private TextField savingsAmount;
    @FXML private ComboBox<SavingsTransactionType> savingsType;
    @FXML private Label messageLabel;

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

        uiControlUtilities.configureDropDown(expenseCategoryDropDown, expenseCategoryService.getAllCategories().value(),e-> e.getDescription());
        cashType.setItems(FXCollections.observableArrayList(CashTransactionType.values()));
        savingsType.setItems(FXCollections.observableArrayList(SavingsTransactionType.values()));
        customerService.getAllCustomers().value().forEach(customer ->
                savingsCustomer.getItems().add(customer.getId() + " - " + customer.getCustomerName()));
        savingsCustomer.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) savingsCustomerId.setText(newValue.split(" - ", 2)[0]);
        });
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

        show(expenseService.recordExpense(expense).message());
    }

    @FXML
    private void recordCash() {
        CashTransaction transaction = new CashTransaction();
        transaction.setDescription(cashDescription.getText());
        transaction.setAmount(parseAmount(cashAmount.getText()));
        transaction.setType(cashType.getValue());
        show(cashService.record(transaction).message());
    }

    @FXML
    private void recordSavings() {
        int id;
        try { id = Integer.parseInt(savingsCustomerId.getText()); }
        catch (NumberFormatException exception) { show("Select a customer first"); return; }
        show(savingsService.recordTransaction(id, savingsType.getValue(), parseAmount(savingsAmount.getText()), null).message());
    }

    private BigDecimal parseAmount(String value) {
        try { return new BigDecimal(value == null ? "" : value.trim()); }
        catch (NumberFormatException exception) { return null; }
    }

    private void show(String message) {
        messageLabel.setText(message);
    }
}
