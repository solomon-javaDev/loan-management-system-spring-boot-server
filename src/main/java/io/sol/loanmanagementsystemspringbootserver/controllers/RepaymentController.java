package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.config.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.entities.Repayment;
import io.sol.loanmanagementsystemspringbootserver.services.LoansService;
import io.sol.loanmanagementsystemspringbootserver.services.RepaymentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class RepaymentController {

    private final RepaymentService repaymentService;
    private final LoansService loansService;

    @FXML private ComboBox<Loan> loanSelector;
    @FXML private TextField installmentField;
    @FXML private TextField amountField;
    @FXML private TextField receivedByField;
    @FXML private Label messageLabel;
    @FXML private TextArea receiptArea;

    public RepaymentController(RepaymentService repaymentService, LoansService loansService) {
        this.repaymentService = repaymentService;
        this.loansService = loansService;
    }

    @FXML
    public void initialize() {
        Result<List<Loan>> result = loansService.getAllLoans();
        if (result.isSuccess()) {
            loanSelector.getItems().setAll(result.value());
        }
    }

    @FXML
    private void handleApplyPayment() {
        Loan loan = loanSelector.getValue();
        if (loan == null) {
            messageLabel.setText("Select a loan first.");
            return;
        }
        Integer installmentId = Integer.parseInt(installmentField.getText());
        BigDecimal amount = new BigDecimal(amountField.getText());
        Result<Repayment> result = repaymentService.applyPayment(loan.getId(), installmentId, amount, receivedByField.getText());
        messageLabel.setText(result.message());
        if (result.isSuccess()) {
            receiptArea.setText("Receipt\nLoan: " + loan.getId() + "\nAmount: " + amount + "\nReceived by: " + receivedByField.getText());
        }
    }
}
