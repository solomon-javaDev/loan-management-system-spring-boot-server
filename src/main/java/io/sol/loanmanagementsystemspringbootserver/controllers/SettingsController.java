package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.ExpenseCategory;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.SystemFinancialState;
import io.sol.loanmanagementsystemspringbootserver.events.FinancialStateUpdatedEvent;
import io.sol.loanmanagementsystemspringbootserver.services.ExpenseCategoryService;
import io.sol.loanmanagementsystemspringbootserver.services.FinancialStateService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class SettingsController {

    private final ExpenseCategoryService expenseCategoryService;
    private final FinancialStateService financialStateService;

    @FXML
    private TextField adminEmailsField;

    @FXML
    private Label totalCapitalLabel;

    @FXML
    private Label totalCashOutLabel;

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
    private TextField cashOnHandField;
    @FXML
    private TextField bankBalanceField;
    @FXML
    private TextField ownersCapitalField;

    public SettingsController(ExpenseCategoryService expenseCategoryService, FinancialStateService financialStateService) {
        this.expenseCategoryService = expenseCategoryService;
        this.financialStateService = financialStateService;
    }

    @FXML
    public void initialize() {
        agingFreq.getItems().addAll("Daily", "Every 3 days", "Weekly", "Monthly");
        refreshUI();
    }

    private void refreshUI() {
        SystemFinancialState state = financialStateService.getCurrentState();
        if (state != null && totalCapitalLabel != null) {
            totalCapitalLabel.setText(String.valueOf(state.getOwnerCapital()));
            if (totalCashOutLabel != null) totalCashOutLabel.setText(String.valueOf(state.getTotalExpenses()));
            if (bankBalanceField != null) bankBalanceField.setText(state.getBankBalance().toPlainString());
            if (ownersCapitalField != null) ownersCapitalField.setText(state.getOwnerCapital().toPlainString());
            if (cashOnHandField != null) cashOnHandField.setText(state.getCashOnHand().toPlainString());
            if (adminEmailsField != null) adminEmailsField.setText(state.getAdminEmails());
        }
    }

    @EventListener
    public void onFinancialStateUpdated(FinancialStateUpdatedEvent event) {
        Platform.runLater(this::refreshUI);
    }

    @FXML
    private void handleSave() {
        financialSave();
    }

    private void financialSave() {

        String bankBalance = bankBalanceField.getText() == null ? "" : bankBalanceField.getText().trim();
        String ownersCapital = ownersCapitalField.getText() == null ? "" : ownersCapitalField.getText().trim();
        String cashOnHand = cashOnHandField.getText() == null ? "" : cashOnHandField.getText().trim();

        if(bankBalance.isBlank() || ownersCapital.isBlank() || cashOnHand.isBlank()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Financial Save Error");
            alert.setContentText("Please fill in the fields for Bank balance, Liquidity, and Cash On Hand");
            ButtonType buttonTypeSave = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            alert.setResult(buttonTypeSave);
            alert.showAndWait();
        }else{
            try {
                BigDecimal bank = new BigDecimal(bankBalance);
                BigDecimal capital = new BigDecimal(ownersCapital);
                BigDecimal cash = new BigDecimal(cashOnHand);
                String adminEmails = adminEmailsField.getText() == null ? "" : adminEmailsField.getText().trim();
                financialStateService.makeFinancialSetting(bank, capital, cash, adminEmails);
                messageLabel.setText("Financial settings saved successfully.");
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid Input");
                alert.setContentText("Please enter valid numeric values for financial fields.");
                alert.showAndWait();
            }
        }
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
