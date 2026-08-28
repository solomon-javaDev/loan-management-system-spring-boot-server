package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.entities.ExpenseCategory;
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

    public SettingsController(SystemSettingService settingService, ExpenseCategoryService expenseCategoryService) {
        this.settingService = settingService;
        this.expenseCategoryService = expenseCategoryService;
    }

    @FXML
    public void initialize() {
        agingFreq.getItems().addAll("Daily","Every 3 days","Weekly","Monthly");
        
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
}
