package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.EmployeeDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.services.EmployeeService;
import io.sol.loanmanagementsystemspringbootserver.utilities.Result;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import io.sol.loanmanagementsystemspringbootserver.utilities.UiControlUtilities;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeesController {
    private final EmployeeService employeeService;
    private final UiControlUtilities uiControlUtilities;
    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private PasswordField password;
    @FXML private TextField email;
    @FXML private TextField telephone;
    @FXML private ComboBox<Role> role;
    @FXML private TextField salary;
    @FXML private Label messageLabel;

    @FXML private TableView<EmployeeDTO> employeesTable;
    @FXML private TableColumn<EmployeeDTO, String> firstNameColumn;
    @FXML private TableColumn<EmployeeDTO, String> lastNameColumn;
    @FXML private TableColumn<EmployeeDTO, String> emailColumn;
    @FXML private TableColumn<EmployeeDTO, String> telephoneColumn;
    @FXML private TableColumn<EmployeeDTO, String> passwordColumn;

    @FXML private TableColumn<EmployeeDTO, String> salaryColumn;
    @FXML private TableColumn<EmployeeDTO, String> roleColumn;
    @FXML private TableColumn<EmployeeDTO, String> userNameColumn;
    @FXML private TableColumn<EmployeeDTO, Boolean> activeColumn;


    public EmployeesController(EmployeeService employeeService, UiControlUtilities uiControlUtilities) {
        this.employeeService = employeeService;
        this.uiControlUtilities = uiControlUtilities;
    }

    @FXML
    public void initialize() {
        configureTable();
        uiControlUtilities.configureDropDown(role, List.of(Role.values()), Enum::toString);
        loadEmployees();
        // populate form on selection
        employeesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> populateForm(newV));
    }

    private void populateForm(EmployeeDTO dto) {
        if (dto == null) return;
        firstName.setText(dto.getFirstName());
        lastName.setText(dto.getLastName());
        email.setText(dto.getEmail());
        telephone.setText(dto.getTelephone());
        salary.setText(String.valueOf(dto.getSalary()));
        role.setValue(dto.getRole());
        // do not populate password for security reasons
        password.clear();
    }

    private void loadEmployees() {
        var result = employeeService.getAllEmployees();
        if (result.isSuccess()) {
            employeesTable.getItems().setAll(result.value());
        }
    }

    private void configureTable(){
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
    }

    @FXML
    private void createEmployee() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setFirstName(firstName.getText());
        employee.setLastName(lastName.getText());
        employee.setUsername(firstName.getText() + " "+lastName.getText());
        employee.setRole(role.getValue());
        employee.setPassword(password.getText());
        employee.setEmail(email.getText());
        employee.setTelephone(telephone.getText());
        try {
            employee.setSalary(salary.getText() == null || salary.getText().isBlank() ? 0 : Integer.parseInt(salary.getText().trim()));
        } catch (NumberFormatException ex) {
            UIHelper.showInfo("Invalid Salary", "Please enter a valid number for salary.");
            return;
        }

        var result = employeeService.createEmployee(employee);
        UIHelper.updateStatusLabel(messageLabel, result);
        if (result.isSuccess()) {
            loadEmployees();
            clearForm();
        }
    }

    @FXML
    private void clearForm() {
        firstName.clear();
        lastName.clear();
        email.clear();
        telephone.clear();
        salary.clear();
        role.getSelectionModel().clearSelection();
        password.clear();
    }

    @FXML
    private void handleUpdateEmployee(){
        EmployeeDTO selectedEmployee = employeesTable.getSelectionModel().getSelectedItem();
        if(selectedEmployee == null){
            UIHelper.showInfo("EMPTY SELECTION", "There's no employee selected yet");
            return;
        }
        EmployeeDTO toUpdate = new EmployeeDTO();
        toUpdate.setId(selectedEmployee.getId());
        toUpdate.setFirstName(firstName.getText());
        toUpdate.setLastName(lastName.getText());
        toUpdate.setUsername(firstName.getText() + " "+ lastName.getText());
        toUpdate.setRole(role.getValue());
        toUpdate.setEmail(email.getText());
        toUpdate.setTelephone(telephone.getText());
        try {
            toUpdate.setSalary(salary.getText() == null || salary.getText().isBlank() ? 0 : Integer.parseInt(salary.getText().trim()));
        } catch (NumberFormatException ex) {
            UIHelper.showInfo("Invalid Salary", "Please enter a valid number for salary.");
            return;
        }
        // If password provided, send it; service will decide encoding/keeping
        toUpdate.setPassword(password.getText());

        Result<EmployeeDTO> result = employeeService.updateEmployee(toUpdate);
        UIHelper.updateStatusLabel(messageLabel, result);
        if (result.isSuccess()) {
            loadEmployees();
            clearForm();
        }
    }

    @FXML
    private void handleDeleteEmployee() {
        EmployeeDTO selected = employeesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UIHelper.showInfo("EMPTY SELECTION", "There's no employee selected yet");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Employee");
        alert.setHeaderText("Are you sure you want to delete this employee?");
        alert.setContentText(selected.getFirstName() + " " + selected.getLastName());
        var res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            var del = employeeService.deleteEmployeeById(selected.getId());
            UIHelper.updateStatusLabel(messageLabel, del);
            if (del.isSuccess()) {
                loadEmployees();
                clearForm();
            }
        }
    }

    @FXML
    private void handleActivate() {
        updateStatus(true);
    }

    @FXML
    private void handleDeactivate() {
        updateStatus(false);
    }

    private void updateStatus(boolean active) {
        EmployeeDTO selected = employeesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            var result = employeeService.updateEmployeeStatus(selected.getId(), active);
            UIHelper.updateStatusLabel(messageLabel, result);
            if (result.isSuccess()) {
                loadEmployees();
            }
        } else {
            messageLabel.setText("Please select an employee");
        }
    }
}
