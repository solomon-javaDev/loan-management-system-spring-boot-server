package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.services.LoansService;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.entities.Loan;
import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;
import io.sol.loanmanagementsystemspringbootserver.utilities.UiControlUtilities;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentsController {

    private final CustomerService customerService;
    private final UiControlUtilities uiControlUtilities;
    private final LoansService loansService;

    @FXML
    private DatePicker date;

    @FXML
    private ComboBox<Customer> customerName;

    @FXML
    private TextField amountRecieved;

    @FXML
    private ComboBox<Loan> loanReference;


    private TableView paymentsTable;



    public PaymentsController(CustomerService customerService, UiControlUtilities uiControlUtilities, LoansService loansService) {
        this.customerService = customerService;
        this.uiControlUtilities = uiControlUtilities;
        this.loansService = loansService;
    }


    @FXML
    public void initialize(){
        //Load customers for selection
        uiControlUtilities.configureDropDown(customerName, customerService.getAllCustomers().value(),
                c -> c.getFirstName() + " " + c.getLastName()
                );

        uiControlUtilities.configureDropDown(loanReference, loansService.getAllLoans().value(),
                Loan::getReference
                );
    }

}
