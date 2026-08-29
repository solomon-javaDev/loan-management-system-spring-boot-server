package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerCreateDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.CustomerResponseDTO;
import io.sol.loanmanagementsystemspringbootserver.dtos.EmployeeDTO;
import io.sol.loanmanagementsystemspringbootserver.services.CustomerService;
import io.sol.loanmanagementsystemspringbootserver.services.EmployeeService;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
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
    private final EmployeeService employeeService;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField otherNamesField;

    @FXML
    private TextField ninField;

    @FXML
    private TextField guarantorNameField;

    @FXML
    private TextField guarantorPhoneField;

    @FXML
    private TextField guarantorNinField;

    @FXML
    private TextField customerSearchField;

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
    private TableColumn<CustomerDTO, String> ninColumn;

    @FXML
    private TableColumn<CustomerDTO, String> guarantorColumn;

    @FXML
    private TableColumn<CustomerDTO, String> telephoneColumn;

    @FXML
    private TableColumn<CustomerDTO, String> accountNumberColumn;

    @FXML
    private TableColumn<CustomerDTO, Number> numberOfLoans;

    @FXML
    private TableColumn<CustomerDTO, String> addressColumn;

    @FXML
    private TableColumn<CustomerDTO, Boolean> activeColumn;

    @FXML
    private TableColumn<CustomerDTO, String> fieldOfficerColumn;

    @FXML
    private ComboBox<EmployeeDTO> fieldOfficerCombo;

    @FXML
    private CheckBox activeCheckbox;

    @FXML
    private Label messageLabel;

    private FilteredList<CustomerDTO> filteredCustomers;

    public CustomerController(CustomerService customerService, EmployeeService employeeService) {
        this.customerService = customerService;
        this.employeeService = employeeService;
    }

    @FXML
    public void initialize() {
        configureTable();
        loadCustomers();
        loadFieldOfficers();
        customerSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyCustomerFilter(newValue));

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
        createDTO.setNin(customer.getNin());
        createDTO.setGuarantorName(customer.getGuarantorName());
        createDTO.setGuarantorPhone(customer.getGuarantorPhone());
        createDTO.setGuarantorNin(customer.getGuarantorNin());
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
        ninColumn.setCellValueFactory(new PropertyValueFactory<>("nin"));
        guarantorColumn.setCellValueFactory(new PropertyValueFactory<>("guarantorName"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        fieldOfficerColumn.setCellValueFactory(cellData -> {
            EmployeeDTO officer = cellData.getValue().getFieldOfficer();
            return new javafx.beans.property.SimpleStringProperty(officer != null ? officer.toString() : "");
        });
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
        filteredCustomers = new FilteredList<>(FXCollections.observableArrayList(result.value()), customer -> true);
        customersTable.setItems(filteredCustomers);
    }

    private void loadFieldOfficers() {
        var result = employeeService.getAllEmployees();
        if (result.isSuccess()) {
            fieldOfficerCombo.setItems(FXCollections.observableArrayList(result.value()));
        }
    }

    private void applyCustomerFilter(String searchText) {
        if (filteredCustomers == null) return;
        String query = searchText == null ? "" : searchText.trim().toLowerCase();
        filteredCustomers.setPredicate(customer -> query.isEmpty()
                || contains(customer.getFirstName(), query)
                || contains(customer.getLastName(), query)
                || contains(customer.getOtherNames(), query)
                || contains(customer.getAccountNumber(), query)
                || contains(customer.getTelephone(), query)
                || contains(customer.getNin(), query)
                || contains(customer.getGuarantorName(), query)
                || contains(customer.getGuarantorPhone(), query)
                || contains(customer.getGuarantorNin(), query));
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private CustomerDTO buildCustomerFromForm() {
        CustomerDTO customer = new CustomerDTO();
        customer.setFirstName(firstNameField.getText() == null ? "" : firstNameField.getText().trim());
        customer.setLastName(lastNameField.getText() == null ? "" : lastNameField.getText().trim());
        customer.setOtherNames(otherNamesField.getText() == null ? "" : otherNamesField.getText().trim());
        customer.setNin(ninField.getText() == null ? "" : ninField.getText().trim());
        customer.setGuarantorName(guarantorNameField.getText().trim());
        customer.setGuarantorPhone(guarantorPhoneField.getText().trim());
        customer.setGuarantorNin(guarantorNinField.getText().trim());
        customer.setTelephone(telephoneField.getText() == null ? "" : telephoneField.getText().trim());
        customer.setAddress(addressField.getText()== null ?  " " : addressField.getText().trim());
        customer.setActive(activeCheckbox.isSelected());
        customer.setFieldOfficer(fieldOfficerCombo.getValue());
        return customer;
    }

    private void populateForm(CustomerDTO customer) {
        accountNumberField.setText(customer.getAccountNumber());
        firstNameField.setText(customer.getFirstName());
        lastNameField.setText(customer.getLastName());
        otherNamesField.setText(customer.getOtherNames());
        ninField.setText(customer.getNin());
        guarantorNameField.setText(customer.getGuarantorName());
        guarantorPhoneField.setText(customer.getGuarantorPhone());
        guarantorNinField.setText(customer.getGuarantorNin());
        telephoneField.setText(customer.getTelephone());
        addressField.setText(customer.getAddress());
        activeCheckbox.setSelected(customer.isActive());
        fieldOfficerCombo.setValue(customer.getFieldOfficer());
    }

    private void clearForm() {
        accountNumberField.clear();
        firstNameField.clear();
        lastNameField.clear();
        otherNamesField.clear();
        ninField.clear();
        guarantorNameField.clear();
        guarantorPhoneField.clear();
        guarantorNinField.clear();
        telephoneField.clear();
        addressField.clear();
        activeCheckbox.setSelected(true);
        fieldOfficerCombo.getSelectionModel().clearSelection();
         customersTable.getSelectionModel().clearSelection();
    }
}