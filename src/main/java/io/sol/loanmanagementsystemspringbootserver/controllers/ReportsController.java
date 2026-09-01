package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.entities.Finance.*;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailDetails;
import io.sol.loanmanagementsystemspringbootserver.mailing.EmailsService;
import io.sol.loanmanagementsystemspringbootserver.repositories.*;
import io.sol.loanmanagementsystemspringbootserver.services.FinancialStateService;
import io.sol.loanmanagementsystemspringbootserver.services.ReportService;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportsController {

    private final ReportService reportService;
    private final LoansRepository loansRepository;
    private final ExpenseRepository expenseRepository;
    private final CashTransactionRepository cashTransactionRepository;
    private final EmailsService emailsService;
    private final FinancialStateService financialStateService;
    private final EmployeeRepository employeeRepository;

    @FXML private DatePicker reportDatePicker;
    @FXML private Label messageLabel;

    // Daily Loans
    @FXML private TableView<Loan> dailyLoansTable;
    @FXML private Label totalDisbursedLabel;
    @FXML private Label totalExpectedInterestLabel;

    // Daily Expenses
    @FXML private TableView<Expense> dailyExpensesTable;
    @FXML private Label totalExpensesLabel;

    // Daily Savings
    @FXML private TableView<CashTransaction> dailySavingsTable;
    @FXML private Label totalSavingsLabel;

    // Loan Statements
    @FXML private ComboBox<Loan> loanComboBox;
    @FXML private TableView<StatementRow> loanStatementTable;
    @FXML private Label totalRepaymentsLabel;
    @FXML private Label remainingBalanceLabel;

    // Aging Analysis
    @FXML private TableView<AgingRow> agingAnalysisTable;
    @FXML private TextField emailField;

    @FXML
    public void initialize() {
        reportDatePicker.setValue(LocalDate.now());
        setupTables();
        loadAllData();

        loanComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) loadLoanStatement(newVal);
        });
    }

    private void setupTables() {
        // Daily Loans Table
        TableColumn<Loan, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomer().getCustomerName()));
        
        TableColumn<Loan, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomer().getTelephone()));
        
        TableColumn<Loan, BigDecimal> disbursedCol = new TableColumn<>("Disbursed");
        disbursedCol.setCellValueFactory(new PropertyValueFactory<>("disbursedAmount"));
        
        TableColumn<Loan, String> rateCol = new TableColumn<>("Rate");
        rateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getInterestRate().multiply(BigDecimal.valueOf(100)) + "%"));
        
        TableColumn<Loan, BigDecimal> expectedCol = new TableColumn<>("Expected");
        expectedCol.setCellValueFactory(new PropertyValueFactory<>("fullPayment"));
        
        TableColumn<Loan, LocalDate> maturityCol = new TableColumn<>("Maturity");
        maturityCol.setCellValueFactory(new PropertyValueFactory<>("maturityDate"));
        
        TableColumn<Loan, String> officerCol = new TableColumn<>("Officer");
        officerCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFieldOfficer() != null ? d.getValue().getFieldOfficer().getUsername() : "N/A"));
        
        dailyLoansTable.getColumns().setAll(customerCol, contactCol, disbursedCol, rateCol, expectedCol, maturityCol, officerCol);

        // Daily Expenses Table
        TableColumn<Expense, String> expDescCol = new TableColumn<>("Description");
        expDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<Expense, String> expCatCol = new TableColumn<>("Category");
        expCatCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory().getDescription()));
        TableColumn<Expense, BigDecimal> expAmtCol = new TableColumn<>("Amount");
        expAmtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<Expense, String> expRefCol = new TableColumn<>("Reference");
        expRefCol.setCellValueFactory(new PropertyValueFactory<>("reference"));
        dailyExpensesTable.getColumns().setAll(expDescCol, expCatCol, expAmtCol, expRefCol);

        // Daily Savings Table
        TableColumn<CashTransaction, Integer> savCustCol = new TableColumn<>("Cust ID");
        savCustCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        TableColumn<CashTransaction, String> savTypeCol = new TableColumn<>("Type");
        savTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<CashTransaction, BigDecimal> savAmtCol = new TableColumn<>("Amount");
        savAmtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<CashTransaction, String> savDescCol = new TableColumn<>("Description");
        savDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        dailySavingsTable.getColumns().setAll(savCustCol, savTypeCol, savAmtCol, savDescCol);

        // Loan Statement Table
        TableColumn<StatementRow, String> stDateCol = new TableColumn<>("Date");
        stDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<StatementRow, BigDecimal> stPaidCol = new TableColumn<>("Amount Paid");
        stPaidCol.setCellValueFactory(new PropertyValueFactory<>("amountPaid"));
        TableColumn<StatementRow, BigDecimal> stBalCol = new TableColumn<>("Balance");
        stBalCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        loanStatementTable.getColumns().setAll(stDateCol, stPaidCol, stBalCol);

        // Aging Analysis Table
        TableColumn<AgingRow, String> agCustCol = new TableColumn<>("Customer");
        agCustCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        TableColumn<AgingRow, String> agContCol = new TableColumn<>("Contact");
        agContCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        TableColumn<AgingRow, String> agGuarCol = new TableColumn<>("Guarantor");
        agGuarCol.setCellValueFactory(new PropertyValueFactory<>("guarantorName"));
        TableColumn<AgingRow, LocalDate> agDisbCol = new TableColumn<>("Disbursed");
        agDisbCol.setCellValueFactory(new PropertyValueFactory<>("disbursementDate"));
        TableColumn<AgingRow, LocalDate> agDueCol = new TableColumn<>("Due Date");
        agDueCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        TableColumn<AgingRow, BigDecimal> agOutCol = new TableColumn<>("Outstanding");
        agOutCol.setCellValueFactory(new PropertyValueFactory<>("outstanding"));
        TableColumn<AgingRow, BigDecimal> agArrCol = new TableColumn<>("Arrears");
        agArrCol.setCellValueFactory(new PropertyValueFactory<>("arrears"));
        TableColumn<AgingRow, Long> agDaysCol = new TableColumn<>("Aging Days");
        agDaysCol.setCellValueFactory(new PropertyValueFactory<>("agingDays"));
        
        agingAnalysisTable.getColumns().setAll(agCustCol, agContCol, agGuarCol, agDisbCol, agDueCol, agOutCol, agArrCol, agDaysCol);
    }

    private void loadAllData() {
        LocalDate date = reportDatePicker.getValue();
        if (date == null) return;

        // Daily Loans
        List<Loan> dailyLoans = loansRepository.findByStartDate(date);
        dailyLoansTable.setItems(FXCollections.observableArrayList(dailyLoans));
        BigDecimal totalD = dailyLoans.stream().map(Loan::getDisbursedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalI = dailyLoans.stream().map(l -> l.getFullPayment().subtract(l.getPrincipal())).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalDisbursedLabel.setText("Total Disbursed: " + totalD.setScale(2, RoundingMode.HALF_UP));
        totalExpectedInterestLabel.setText("Expected Interest: " + totalI.setScale(2, RoundingMode.HALF_UP));

        // Daily Expenses
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<Expense> expenses = expenseRepository.findByDateBetween(start, end);
        dailyExpensesTable.setItems(FXCollections.observableArrayList(expenses));
        BigDecimal totalE = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        totalExpensesLabel.setText("Total Expenses: " + totalE.setScale(2, RoundingMode.HALF_UP));

        // Daily Savings
        List<CashTransaction> savings = cashTransactionRepository.findByTypeInAndDateBetween(
                List.of(CashTransactionType.SAVINGS_DEPOSIT, CashTransactionType.SAVINGS_WITHDRAWAL), start, end);
        dailySavingsTable.setItems(FXCollections.observableArrayList(savings));
        BigDecimal netS = savings.stream()
                .map(t -> t.getType() == CashTransactionType.SAVINGS_DEPOSIT ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalSavingsLabel.setText("Total Savings Change: " + netS.setScale(2, RoundingMode.HALF_UP));

        // Loan ComboBox
        List<Loan> allActiveLoans = loansRepository.findByStatusIn(List.of(LoanStatus.ACTIVE, LoanStatus.PENDING, LoanStatus.DEFAULTED));
        loanComboBox.setItems(FXCollections.observableArrayList(allActiveLoans));
        loanComboBox.setCellFactory(lv -> new ListCell<Loan>() {
            @Override protected void updateItem(Loan item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getCustomer().getCustomerName() + " (" + item.getReference() + ")");
            }
        });
        loanComboBox.setButtonCell(loanComboBox.getCellFactory().call(null));

        // Aging Analysis
        loadAgingAnalysis();
    }

    private void loadLoanStatement(Loan loan) {
        List<StatementRow> rows = new ArrayList<>();
        BigDecimal balance = loan.getFullPayment();
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (Payment p : loan.getPayments()) {
            totalPaid = totalPaid.add(p.getAmountReceived());
            balance = balance.subtract(p.getAmountReceived());
            rows.add(new StatementRow(p.getDate().toString(), p.getAmountReceived(), balance));
        }
        loanStatementTable.setItems(FXCollections.observableArrayList(rows));
        totalRepaymentsLabel.setText("Total Repayments: " + totalPaid.setScale(2, RoundingMode.HALF_UP));
        remainingBalanceLabel.setText("Remaining Balance: " + loan.getOutstandingBalance().setScale(2, RoundingMode.HALF_UP));
    }

    private void loadAgingAnalysis() {
        List<Loan> loans = loansRepository.findAll().stream()
                .filter(l -> l.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0)
                .toList();
        List<AgingRow> rows = loans.stream().map(l -> {
            BigDecimal ir = l.getInterestRate() == null ? BigDecimal.ZERO : l.getInterestRate();
            BigDecimal scheduledTotal = l.getPrincipal().add(l.getPrincipal().multiply(ir));
            BigDecimal dailyInstallment = scheduledTotal.divide(BigDecimal.valueOf(l.getTenor()), 10, RoundingMode.HALF_UP);
            long elapsed = ChronoUnit.DAYS.between(l.getStartDate(), LocalDate.now()) + 1;
            BigDecimal expectedSoFar = dailyInstallment.multiply(BigDecimal.valueOf(Math.min(elapsed, (long)l.getTenor())));
            BigDecimal arrears = expectedSoFar.subtract(l.getTotalPaid()).max(BigDecimal.ZERO);
            
            return new AgingRow(
                l.getCustomer().getCustomerName(),
                l.getCustomer().getTelephone(),
                l.getGuarantor().getCustomerName(),
                l.getStartDate(),
                l.getMaturityDate(),
                l.getOutstandingBalance(),
                arrears.setScale(2, RoundingMode.HALF_UP),
                l.getAgingDays(LocalDate.now())
            );
        }).toList();
        agingAnalysisTable.setItems(FXCollections.observableArrayList(rows));
    }

    @FXML private void handleRefresh() { loadAllData(); messageLabel.setText("Data refreshed"); }

    @FXML private void handlePrintDailyLoans() { saveAndOpenPDF(reportService.generateDailyLoanReport(reportDatePicker.getValue()), "DailyLoans"); }
    @FXML private void handlePrintDailyExpenses() { saveAndOpenPDF(reportService.generateDailyExpenseReport(reportDatePicker.getValue()), "DailyExpenses"); }
    @FXML private void handlePrintDailySavings() { saveAndOpenPDF(reportService.generateDailySavingsReport(reportDatePicker.getValue()), "DailySavings"); }
    
    @FXML private void handlePrintLoanStatement() {
        Loan selected = loanComboBox.getValue();
        if (selected == null) { UIHelper.showError("Missing Data", "Please select a loan first"); return; }
        saveAndOpenPDF(reportService.generateLoanStatement(selected.getId()), "LoanStatement_" + selected.getId());
    }

    @FXML private void handlePrintAgingAnalysis() { saveAndOpenPDF(reportService.generateAgingAnalysis(), "AgingAnalysis"); }

    private void saveAndOpenPDF(byte[] pdf, String name) {
        try {
            File dir = new File("reports");
            if (!dir.exists()) dir.mkdir();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File file = new File(dir, name + "_" + timestamp + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdf);
            }
            messageLabel.setText("PDF saved: " + file.getAbsolutePath());
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
        } catch (Exception e) {
            UIHelper.showError("Error", "Failed to save PDF: " + e.getMessage());
        }
    }

    @FXML private void handleEmailDailyLoans() { emailReport(reportService.generateDailyLoanReport(reportDatePicker.getValue()), "Daily Loans Report"); }
    @FXML private void handleEmailDailyExpenses() { emailReport(reportService.generateDailyExpenseReport(reportDatePicker.getValue()), "Daily Expenses Report"); }
    @FXML private void handleEmailDailySavings() { emailReport(reportService.generateDailySavingsReport(reportDatePicker.getValue()), "Daily Savings Report"); }

    @FXML private void handleEmailLoanStatement() {
        Loan selected = loanComboBox.getValue();
        if (selected == null) { UIHelper.showError("Missing Data", "Please select a loan first"); return; }
        emailReport(reportService.generateLoanStatement(selected.getId()), "Loan Statement - " + selected.getCustomer().getCustomerName());
    }

    @FXML private void handleEmailAgingAnalysis() {
        String email = emailField.getText();
        if (email == null || email.isBlank()) { UIHelper.showError("Missing Data", "Please enter an email address"); return; }
        emailReport(reportService.generateAgingAnalysis(), "Aging Analysis Report", email);
    }

    @FXML private void handleEmailAllStatements() {
        List<Loan> loans = loansRepository.findByStatus(LoanStatus.ACTIVE);
        int sent = 0;
        for (Loan l : loans) {
            if (l.getFieldOfficer() != null && l.getFieldOfficer().getEmail() != null) {
                byte[] pdf = reportService.generateLoanStatement(l.getId());
                emailReport(pdf, "Loan Statement - " + l.getCustomer().getCustomerName(), l.getFieldOfficer().getEmail());
                sent++;
            }
        }
        UIHelper.showInfo("Batch Email", "Sent " + sent + " statements to field officers.");
    }

    private void emailReport(byte[] pdf, String subject) {
        SystemFinancialState state = financialStateService.getCurrentState();
        String emails = state.getAdminEmails();
        if (emails == null || emails.isBlank()) {
            UIHelper.showError("Config Error", "Admin emails not configured in financial settings.");
            return;
        }
        for (String email : emails.split(",")) {
            emailReport(pdf, subject, email.trim());
        }
    }

    private void emailReport(byte[] pdf, String subject, String recipient) {
        EmailDetails details = new EmailDetails();
        details.setRecipient(recipient);
        details.setSubject(subject);
        details.setBody("Please find attached the " + subject);
        details.setAttachment(pdf);
        details.setAttachmentName(subject.replace(" ", "_") + ".pdf");
        
        emailsService.sendMailWithAttachment(details);
        messageLabel.setText("Email sent to " + recipient);
    }

    // Helper classes for TableView
    public static class StatementRow {
        private final SimpleStringProperty date;
        private final BigDecimal amountPaid;
        private final BigDecimal balance;
        public StatementRow(String date, BigDecimal amountPaid, BigDecimal balance) {
            this.date = new SimpleStringProperty(date);
            this.amountPaid = amountPaid;
            this.balance = balance;
        }
        public String getDate() { return date.get(); }
        public BigDecimal getAmountPaid() { return amountPaid; }
        public BigDecimal getBalance() { return balance; }
    }

    public static class AgingRow {
        private final String customerName;
        private final String contact;
        private final String guarantorName;
        private final LocalDate disbursementDate;
        private final LocalDate dueDate;
        private final BigDecimal outstanding;
        private final BigDecimal arrears;
        private final long agingDays;

        public AgingRow(String customerName, String contact, String guarantorName, LocalDate disbursementDate, 
                        LocalDate dueDate, BigDecimal outstanding, BigDecimal arrears, long agingDays) {
            this.customerName = customerName;
            this.contact = contact;
            this.guarantorName = guarantorName;
            this.disbursementDate = disbursementDate;
            this.dueDate = dueDate;
            this.outstanding = outstanding;
            this.arrears = arrears;
            this.agingDays = agingDays;
        }
        public String getCustomerName() { return customerName; }
        public String getContact() { return contact; }
        public String getGuarantorName() { return guarantorName; }
        public LocalDate getDisbursementDate() { return disbursementDate; }
        public LocalDate getDueDate() { return dueDate; }
        public BigDecimal getOutstanding() { return outstanding; }
        public BigDecimal getArrears() { return arrears; }
        public long getAgingDays() { return agingDays; }
    }
}
