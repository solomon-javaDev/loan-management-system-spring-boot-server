package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.entities.CashTransaction;
import io.sol.loanmanagementsystemspringbootserver.entities.CashTransactionType;
import io.sol.loanmanagementsystemspringbootserver.entities.ExpenseCategory;
import io.sol.loanmanagementsystemspringbootserver.services.CashTransactionService;
import io.sol.loanmanagementsystemspringbootserver.services.ExpenseCategoryService;
import io.sol.loanmanagementsystemspringbootserver.services.SystemSettingService;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class SettingsController {

    private final SystemSettingService settingService;
    private final ExpenseCategoryService expenseCategoryService;
    private final CashTransactionService cashTransactionService;

    @FXML
    private TextField adminEmailsField;

    @FXML
    private CheckBox dailyReportCheckbox;

    @FXML
    private TextField dailyTimeField;

    @FXML
    private ComboBox<String> agingFreq;

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

    @FXML
    private ComboBox<CashTransactionType> transactionType;

    public SettingsController(SystemSettingService settingService, ExpenseCategoryService expenseCategoryService, CashTransactionService cashTransactionService) {
        this.settingService = settingService;
        this.expenseCategoryService = expenseCategoryService;
        this.cashTransactionService = cashTransactionService;
    }

    @FXML
    public void initialize() {
        agingFreq.getItems().addAll("Daily","Every 3 days","Weekly","Monthly");
        transactionType.getItems().addAll(CashTransactionType.CAPITAL_IN, CashTransactionType.CASH_OUT);
        
        refreshCapitalInfo();
        
        String savedEmails = settingService.getSetting("report.emails", "");
        adminEmailsField.setText(savedEmails);
        
        String reportEnabled = settingService.getSetting("report.enabled", "true");
        dailyReportCheckbox.setSelected(Boolean.parseBoolean(reportEnabled));
        
        String reportTime = settingService.getSetting("report.time", "07:00");
        dailyTimeField.setText(reportTime);
        
        String agingF = settingService.getSetting("report.aging.freq", "Daily");
        agingFreq.setValue(agingF);
    }

    @FXML
    private void handleSave() {
        try {
            settingService.saveSetting("report.emails", adminEmailsField.getText());
            settingService.saveSetting("report.enabled", String.valueOf(dailyReportCheckbox.isSelected()));
            settingService.saveSetting("report.time", dailyTimeField.getText());
            settingService.saveSetting("report.aging.freq", agingFreq.getValue());
            
            UIHelper.updateStatusLabel(messageLabel, Result.success("Settings saved successfully.", null));
        } catch (Exception e) {
            UIHelper.updateStatusLabel(messageLabel, Result.invalid("Error saving settings: " + e.getMessage(), null));
        }
    }

    @FXML
    private void addCategory(){
        String description = expenseCategory.getText();
        if(description.isBlank()){
            messageLabel.setText("Fill in a category");
            return;
        }
        if(expenseCategoryService.getCategoryByDescription(description).isSuccess()){
            messageLabel.setText("Category already exists");
            return;
        }
        if(!description.isBlank()){
            ExpenseCategory expenseCategory1 = new ExpenseCategory(description);
            expenseCategoryService.saveCategory(expenseCategory1);
            messageLabel.setText("A new expense category has been saved: " + description);
            expenseCategory.clear();
        }
    }

    private void refreshCapitalInfo() {
        totalCapitalLabel.setText(String.format("%.2f", cashTransactionService.getTotalCapital()));
        totalCashOutLabel.setText(String.format("%.2f", cashTransactionService.getTotalCashOut()));
    }

    @FXML
    private void handleRecordTransaction() {
        try {
            java.math.BigDecimal amount = new java.math.BigDecimal(transactionAmount.getText());
            CashTransactionType type = transactionType.getValue();
            if (type == null) {
                messageLabel.setText("Select transaction type");
                return;
            }
            CashTransaction tx = new CashTransaction();
            tx.setAmount(amount);
            tx.setType(type);
            tx.setDescription("Capital management transaction: " + type);
            
            var result = cashTransactionService.record(tx);
            if (result.isSuccess()) {
                messageLabel.setText("Transaction recorded successfully");
                transactionAmount.clear();
                refreshCapitalInfo();
            } else {
                messageLabel.setText(result.message());
            }
        } catch (Exception e) {
            messageLabel.setText("Invalid amount: " + e.getMessage());
        }
    }
}
