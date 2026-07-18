package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.config.Result;
import io.sol.loanmanagementsystemspringbootserver.entities.Customer;
import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomerController {

    private final CustomerService customerService;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField otherNamesField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telephoneField;

    @FXML
    private Button customerSaveButton;

    @FXML
    private Button customerUpdateButton;

    @FXML
    private Button customerDeleteButton;

    @FXML
    private Button customerClearButton;

    @FXML
    private TableView<Customer> customersTable;

    @FXML
    private TableColumn<Customer, Integer> idColumn;

    @FXML
    private TableColumn<Customer, String> firstNameColumn;

    @FXML
    private TableColumn<Customer, String> lastNameColumn;

    @FXML
    private TableColumn<Customer, String> otherNamesColumn;

    @FXML
    private TableColumn<Customer, String> emailColumn;

    @FXML
    private TableColumn<Customer, String> telephoneColumn;

    @FXML
    private Label messageLabel;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @FXML
    public void initialize() {
        configureTable();
        loadCustomers();

        customersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedCustomer) -> {
            if (selectedCustomer != null) {
                populateForm(selectedCustomer);
            }
        });

        //Disable the save button if a row is selected or the form is populated
        customerSaveButton.disableProperty().bind(
                customersTable.getSelectionModel().selectedItemProperty().isNotNull()
        );

        //Disable the update button if no row has been selected
        customerUpdateButton.disableProperty().bind(
                customersTable.getSelectionModel().selectedItemProperty().isNull()
        );


    }

    @FXML
    private void handleSaveCustomer() {
        Customer customer = buildCustomerFromForm();
        Result<Customer> result = customerService.createCustomer(customer);
        messageLabel.setText(result.message());

        if (result.isSuccess()) {
            loadCustomers();
            clearForm();
        }
    }

    @FXML
    private void handleUpdateCustomer() {
        Customer selectedCustomer = customersTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            messageLabel.setText("Select a customer from the table first.");
            return;
        }

        Customer updatedCustomer = buildCustomerFromForm();
        updatedCustomer.setId(selectedCustomer.getId());
        Result<Customer> result = customerService.updateCustomer(updatedCustomer);
        messageLabel.setText(result.message());

        if (result.isSuccess()) {
            loadCustomers();
            clearForm();
        }
    }

    @FXML
    private void handleDeleteCustomer() {
        Customer selectedCustomer = customersTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            messageLabel.setText("Select a customer to delete.");
            return;
        }

        Result<Void> result = customerService.deleteCustomer(selectedCustomer);
        messageLabel.setText(result.message());

        if (result.isSuccess()) {
            loadCustomers();
            clearForm();
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void configureTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        otherNamesColumn.setCellValueFactory(new PropertyValueFactory<>("otherNames"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
    }

    private void loadCustomers() {
        Result<java.util.List<Customer>> result = customerService.getAllCustomers();
        customersTable.setItems(FXCollections.observableArrayList(result.value()));
    }

    private Customer buildCustomerFromForm() {
        Customer customer = new Customer();
        customer.setFirstName(firstNameField.getText() == null ? "" : firstNameField.getText().trim());
        customer.setLastName(lastNameField.getText() == null ? "" : lastNameField.getText().trim());
        customer.setOtherNames(otherNamesField.getText() == null ? "" : otherNamesField.getText().trim());
        customer.setEmail(emailField.getText() == null ? "" : emailField.getText().trim());
        customer.setTelephone(telephoneField.getText() == null ? "" : telephoneField.getText().trim());
        return customer;
    }

    private void populateForm(Customer customer) {
        firstNameField.setText(customer.getFirstName());
        lastNameField.setText(customer.getLastName());
        otherNamesField.setText(customer.getOtherNames());
        emailField.setText(customer.getEmail());
        telephoneField.setText(customer.getTelephone());
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        otherNamesField.clear();
        emailField.clear();
        telephoneField.clear();
        customersTable.getSelectionModel().clearSelection();
    }
}