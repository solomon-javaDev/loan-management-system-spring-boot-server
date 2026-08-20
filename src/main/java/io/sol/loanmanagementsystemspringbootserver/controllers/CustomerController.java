package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerCreateDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerResponseDTO;
import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

/**
 * This class is a Spring-managed component that serves as the controller for managing customer-related
 * functionalities within a JavaFX application. It interacts with the UI elements defined in the FXML and
 * facilitates the CRUD operations for customers by interfacing with a `CustomerService`.
 *
 * Responsibilities include initializing the customer table, handling form events, enabling or disabling
 * appropriate buttons based on context, and updating the UI to reflect operations performed on customer data.
 */
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
    private TextField addressField;

    @FXML
    private TextField accountNumberField;

    @FXML
    private Button customerSaveButton;

    @FXML
    private Button customerUpdateButton;

    @FXML
    private Button customerDeleteButton;

    @FXML
    private Button customerClearButton;

    @FXML
    private TableView<CustomerDTO> customersTable;

    @FXML
    private TableColumn<CustomerDTO, Integer> idColumn;

    @FXML

    private TableColumn<CustomerDTO, String> firstNameColumn;

    @FXML
    private TableColumn<CustomerDTO, String> lastNameColumn;

    @FXML
    private TableColumn<CustomerDTO, String> otherNamesColumn;

    @FXML
    private TableColumn<CustomerDTO, String> emailColumn;

    @FXML
    private TableColumn<CustomerDTO, String> telephoneColumn;

    @FXML
    private TableColumn<CustomerDTO, String> accountNumberColumn;

    @FXML
    private TableColumn<CustomerDTO, Number> numberOfLoans;

    @FXML
    private TableColumn<CustomerDTO, String> addressColumn;

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
        CustomerDTO customer = buildCustomerFromForm();

        // Convert CustomerDTO to CustomerCreateDTO
        CustomerCreateDTO createDTO = new CustomerCreateDTO();
        createDTO.setFirstName(customer.getFirstName());
        createDTO.setLastName(customer.getLastName());
        createDTO.setOtherNames(customer.getOtherNames());
        createDTO.setEmail(customer.getEmail());
        createDTO.setTelephone(customer.getTelephone());
        createDTO.setAddress(customer.getAddress());

        Result<CustomerResponseDTO> result = customerService.createCustomer(createDTO);
        UIHelper.updateStatusLabel(messageLabel, result);

        if (result.isSuccess()) {
            loadCustomers();
            clearForm();
        }
    }

    @FXML
    private void handleUpdateCustomer() {
        CustomerDTO selectedCustomer = customersTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            messageLabel.setText("Select a customer from the table first.");
            return;
        }

        CustomerDTO updatedCustomer = buildCustomerFromForm();
        updatedCustomer.setId(selectedCustomer.getId());
        Result<CustomerResponseDTO> result = customerService.updateCustomer(updatedCustomer);
        UIHelper.updateStatusLabel(messageLabel, result);

        if (result.isSuccess()) {
            loadCustomers();
            clearForm();
        }
    }

    @FXML
    private void handleDeleteCustomer() {
        CustomerDTO selectedCustomer = customersTable.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            messageLabel.setText("Select a customer to delete.");
            return;
        }

        Result<Void> result = customerService.deleteCustomer(selectedCustomer);
        UIHelper.updateStatusLabel(messageLabel, result);

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
        accountNumberColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        otherNamesColumn.setCellValueFactory(new PropertyValueFactory<>("otherNames"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        numberOfLoans.setCellValueFactory(cellData -> {
            CustomerDTO customer = cellData.getValue();
            if (customer != null) {
                return new javafx.beans.property.SimpleIntegerProperty(customer.getLoanCount());
            }
            return new javafx.beans.property.SimpleIntegerProperty(0);
        });
    }

    private void loadCustomers() {
        Result<java.util.List<CustomerDTO>> result = customerService.getAllCustomers();
        customersTable.setItems(FXCollections.observableArrayList(result.value()));
    }

    private CustomerDTO buildCustomerFromForm() {
        CustomerDTO customer = new CustomerDTO();
        customer.setFirstName(firstNameField.getText() == null ? "" : firstNameField.getText().trim());
        customer.setLastName(lastNameField.getText() == null ? "" : lastNameField.getText().trim());
        customer.setOtherNames(otherNamesField.getText() == null ? "" : otherNamesField.getText().trim());
        customer.setEmail(emailField.getText() == null ? "" : emailField.getText().trim());
        customer.setTelephone(telephoneField.getText() == null ? "" : telephoneField.getText().trim());
        customer.setAddress(addressField.getText()== null ?  " " : addressField.getText().trim());
        return customer;
    }

    private void populateForm(CustomerDTO customer) {
        accountNumberField.setText(customer.getAccountNumber());
        firstNameField.setText(customer.getFirstName());
        lastNameField.setText(customer.getLastName());
        otherNamesField.setText(customer.getOtherNames());
        emailField.setText(customer.getEmail());
        telephoneField.setText(customer.getTelephone());
        addressField.setText(customer.getAddress());
    }

    private void clearForm() {
        accountNumberField.clear();
        firstNameField.clear();
        lastNameField.clear();
        otherNamesField.clear();
        emailField.clear();
        telephoneField.clear();
        addressField.clear();;
         customersTable.getSelectionModel().clearSelection();
    }
}