package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.ExpenseCategory;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.SystemFinancialState;
import io.sol.loanmanagementsystemspringbootserver.entities.Finance.FeeBucket;
import io.sol.loanmanagementsystemspringbootserver.events.FinancialStateUpdatedEvent;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailDetails;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailsService;
import io.sol.loanmanagementsystemspringbootserver.services.ExpenseCategoryService;
import io.sol.loanmanagementsystemspringbootserver.services.FeeBucketService;
import io.sol.loanmanagementsystemspringbootserver.services.FinancialStateService;
import io.sol.loanmanagementsystemspringbootserver.utilities.MoneyHelper;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UiControlUtilities;
import io.sol.loanmanagementsystemspringbootserver.utilities.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SettingsController {

    private final ExpenseCategoryService expenseCategoryService;
    private final FinancialStateService financialStateService;
    private final FeeBucketService feeBucketService;
    private final EmailsService emailsService;
    private final UserSession userSession;

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

    @FXML private TextField feeMinAmountField;
    @FXML private TextField feeMaxAmountField;
    @FXML private TextField feeAmountField;
    @FXML private TableView<FeeBucket> feeBucketsTable;
    @FXML private TableColumn<FeeBucket, BigDecimal> feeMinColumn;
    @FXML private TableColumn<FeeBucket, BigDecimal> feeMaxColumn;
    @FXML private TableColumn<FeeBucket, BigDecimal> feeAmountColumn;

    public SettingsController(ExpenseCategoryService expenseCategoryService, FinancialStateService financialStateService,
                              FeeBucketService feeBucketService, EmailsService emailsService, UserSession userSession) {
        this.expenseCategoryService = expenseCategoryService;
        this.financialStateService = financialStateService;
        this.feeBucketService = feeBucketService;
        this.emailsService = emailsService;
        this.userSession = userSession;
    }

    @FXML
    public void initialize() {
        agingFreq.getItems().addAll("Daily", "Every 3 days", "Weekly", "Monthly");
        feeMinColumn.setCellValueFactory(new PropertyValueFactory<>("minAmount"));
        feeMinColumn.setCellFactory(UiControlUtilities.formattedCell(MoneyHelper::formatMoney));

        feeMaxColumn.setCellValueFactory(new PropertyValueFactory<>("maxAmount"));
        feeMaxColumn.setCellFactory(UiControlUtilities.formattedCell(MoneyHelper::formatMoney));

        feeAmountColumn.setCellValueFactory(new PropertyValueFactory<>("feeAmount"));
        feeAmountColumn.setCellFactory(UiControlUtilities.formattedCell(MoneyHelper::formatMoney));

        loadFeeBuckets();
        refreshUI();
    }

    private void refreshUI() {
        SystemFinancialState state = financialStateService.getCurrentState();
        if (state != null && totalCapitalLabel != null) {
            totalCapitalLabel.setText(MoneyHelper.formatMoney(state.getOwnerCapital()));
            if (totalCashOutLabel != null) totalCashOutLabel.setText(MoneyHelper.formatMoney(state.getTotalExpenses()));
            if (bankBalanceField != null) bankBalanceField.setText(MoneyHelper.formatMoney(state.getBankBalance()));
            if (ownersCapitalField != null) ownersCapitalField.setText(MoneyHelper.formatMoney(state.getOwnerCapital()));
            if (cashOnHandField != null) cashOnHandField.setText(MoneyHelper.formatMoney(state.getCashOnHand()));
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

        if (!userSession.isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Authorization required", "Only an administrator can change capital and cash settings.");
            return;
        }

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
                SystemFinancialState previous = financialStateService.getCurrentState();
                if (!confirmFinancialChange(previous, bank, capital, cash)) return;
                Result<Void> result = financialStateService.makeFinancialSetting(bank, capital, cash, adminEmails);
                if (result.isFailure()) {
                    showAlert(Alert.AlertType.ERROR, "Settings not saved", result.message());
                    return;
                }
                sendFinancialSettingsSnapshot(previous, bank, capital, cash, adminEmails);
                messageLabel.setText(result.message());
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
    private void saveFeeBucket() {
        if (!userSession.isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Authorization required", "Only an administrator can edit fee buckets.");
            return;
        }
        try {
            FeeBucket selected = feeBucketsTable.getSelectionModel().getSelectedItem();
            FeeBucket bucket = selected == null ? new FeeBucket() : selected;
            bucket.setMinAmount(new BigDecimal(feeMinAmountField.getText().trim()));
            bucket.setMaxAmount(new BigDecimal(feeMaxAmountField.getText().trim()));
            bucket.setFeeAmount(new BigDecimal(feeAmountField.getText().trim()));
            Result<FeeBucket> result = feeBucketService.saveBucket(bucket);
            messageLabel.setText(result.message());
            if (result.isSuccess()) {
                loadFeeBuckets();
                clearFeeBucketFields();
            }
        } catch (NumberFormatException exception) {
            showAlert(Alert.AlertType.ERROR, "Invalid fee bucket", "Enter valid numeric values for all fee bucket fields.");
        }
    }

    @FXML
    private void selectFeeBucket() {
        FeeBucket bucket = feeBucketsTable.getSelectionModel().getSelectedItem();
        if (bucket == null) return;
        feeMinAmountField.setText(bucket.getMinAmount().toPlainString());
        feeMaxAmountField.setText(bucket.getMaxAmount().toPlainString());
        feeAmountField.setText(bucket.getFeeAmount().toPlainString());
    }

    private void loadFeeBuckets() {
        feeBucketsTable.setItems(FXCollections.observableArrayList(feeBucketService.getAllBuckets()));
    }

    @FXML
    private void clearFeeBucketFields() {
        feeMinAmountField.clear();
        feeMaxAmountField.clear();
        feeAmountField.clear();
        feeBucketsTable.getSelectionModel().clearSelection();
    }

    private boolean confirmFinancialChange(SystemFinancialState previous, BigDecimal bank, BigDecimal capital, BigDecimal cash) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm financial settings");
        alert.setHeaderText("Capital and cash settings will be changed");
        alert.setContentText("A snapshot of the previous settings will be recorded and emailed to the configured administrators. Continue?");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void sendFinancialSettingsSnapshot(SystemFinancialState previous, BigDecimal bank, BigDecimal capital,
                                               BigDecimal cash, String adminEmails) {
        if (adminEmails == null || adminEmails.isBlank()) return;
        String body = "Capital and cash settings have been updated by an administrator.\n\n"
                + "Previous settings (snapshot):\n" + formatFinancialSettings(previous.getBankBalance(), previous.getOwnerCapital(), previous.getCashOnHand())
                + "\nNew settings:\n" + formatFinancialSettings(bank, capital, cash);
        for (String email : adminEmails.split(",")) {
            String recipient = email.trim();
            if (!recipient.isBlank()) {
                emailsService.sendSimpleMail(new EmailDetails(recipient, "Capital and cash settings updated", body));
            }
        }
    }

    private String formatFinancialSettings(BigDecimal bank, BigDecimal capital, BigDecimal cash) {
        return "Bank balance: " + bank + "\nOwner capital: " + capital + "\nCash on hand: " + cash + "\n";
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("Settings");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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

    public void updateFeeBucket() {
        if(feeBucketsTable.getSelectionModel().getSelectedItem() == null){
            showAlert(Alert.AlertType.WARNING, "No selected Item", "There has been no selection made");
            return;
        }

        FeeBucket feeBucket = feeBucketsTable.getSelectionModel().getSelectedItem();
        feeBucketService.updateBucket(feeBucket);
    }

    public void deleteFeeBucket() {
        if(feeBucketsTable.getSelectionModel().getSelectedItem() == null){
            showAlert(Alert.AlertType.WARNING, "No selected Item", "There has been no selection made");
            return;
        }

        FeeBucket feeBucket = feeBucketsTable.getSelectionModel().getSelectedItem();
        feeBucketService.deleteBucket(feeBucket.getId());
    }
}
