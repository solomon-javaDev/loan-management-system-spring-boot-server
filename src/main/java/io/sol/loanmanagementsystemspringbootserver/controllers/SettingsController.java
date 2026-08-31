package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.ExpenseCategory;
import io.sol.loanmanagementsystemspringbootserver.services.ExpenseCategoryService;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SettingsController {

    private final ExpenseCategoryService expenseCategoryService;

    @FXML
    private TextField adminEmailsField;

    @FXML
    private CheckBox dailyReportCheckbox;

    @FXML
    private TextField dailyTimeField;

    @FXML
    private ComboBox<String> agingFreq;

    @FXML
    private TextField surchargeRateField;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField expenseCategory;

    @FXML
    private Label totalCapitalLabel;

    @FXML
    private Label totalCashOutLabel;

    @FXML
    private TextField transactionAmount;


    public SettingsController(ExpenseCategoryService expenseCategoryService) {
        this.expenseCategoryService = expenseCategoryService;
    }

    @FXML
    public void initialize() {
        agingFreq.getItems().addAll("Daily", "Every 3 days", "Weekly", "Monthly");

    }

    @FXML
    private void handleSave() {

    }

    @FXML
    private void addCategory() {
        String description = expenseCategory.getText();
        if (description.isBlank()) {
            messageLabel.setText("Fill in a category");
            return;
        }
        if (expenseCategoryService.getCategoryByDescription(description).isSuccess()) {
            messageLabel.setText("Category already exists");
            return;
        }
        if (!description.isBlank()) {
            ExpenseCategory expenseCategory1 = new ExpenseCategory(description);
            expenseCategoryService.saveCategory(expenseCategory1);
            messageLabel.setText("A new expense category has been saved: " + description);
            expenseCategory.clear();
        }
    }

}



