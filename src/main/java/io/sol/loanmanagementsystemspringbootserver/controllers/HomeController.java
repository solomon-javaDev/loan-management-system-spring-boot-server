package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.entities.LoanStatus;
import io.sol.loanmanagementsystemspringbootserver.repositories.CustomerRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.LoansRepository;
import io.sol.loanmanagementsystemspringbootserver.repositories.UserRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

@Component
public class HomeController {

    private final LoansRepository loansRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public HomeController(LoansRepository loansRepository, CustomerRepository customerRepository, UserRepository userRepository) {
        this.loansRepository = loansRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @FXML
    private Label pending_loans;

    @FXML
    private Label total_employee_number;

    @FXML
    private Label dashboard_customer_number;

    @FXML
    private Label over_stayed_loans;

    @FXML
    private Label daily_payments;

    @FXML
    public void initialize() {
        showPendingLoans();
        showTotalEmployeeNumber();
        showCustomerNumber();
        showOverStayedLoans();
        showDailyPayments();
    }

    public void showPendingLoans() {
        long count = loansRepository.findByStatus(LoanStatus.PENDING).size();
        pending_loans.setText(String.valueOf(count));
    }

    public void showTotalEmployeeNumber() {
        long count = userRepository.count();
        total_employee_number.setText(String.valueOf(count));
    }

    public void showCustomerNumber() {
        long count = customerRepository.count();
        dashboard_customer_number.setText(String.valueOf(count));
    }

    public void showOverStayedLoans() {
        // Placeholder for now
        over_stayed_loans.setText("0");
    }

    public void showDailyPayments() {
        // Placeholder for now
        daily_payments.setText("0.00");
    }
}
