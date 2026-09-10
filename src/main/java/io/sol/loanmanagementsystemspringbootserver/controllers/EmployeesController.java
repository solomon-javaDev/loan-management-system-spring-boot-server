package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.EmployeeDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.entities.custom.User;
import io.sol.loanmanagementsystemspringbootserver.services.EmployeeService;
import io.sol.loanmanagementsystemspringbootserver.services.UserService;
import io.sol.loanmanagementsystemspringbootserver.utilities.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class EmployeesController {
    private final EmployeeService employeeService;
    private final UiControlUtilities uiControlUtilities;
    private final UserSession userSession;
    private final UserService userService;
    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField username;
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

    @FXML private TableColumn<EmployeeDTO, Number> salaryColumn;
    @FXML private TableColumn<EmployeeDTO, String> roleColumn;
    @FXML private TableColumn<EmployeeDTO, String> userNameColumn;
    @FXML private TableColumn<EmployeeDTO, Boolean> activeColumn;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userUsernameColumn;
    @FXML private TableColumn<User, String> userEmailColumn;
    @FXML private TableColumn<User, Role> userRoleColumn;
    @FXML private Button createEmployeeButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;


    public EmployeesController(EmployeeService employeeService, UiControlUtilities uiControlUtilities,
                               UserSession userSession, UserService userService) {
        this.employeeService = employeeService;
        this.uiControlUtilities = uiControlUtilities;
        this.userSession = userSession;
        this.userService = userService;
    }

    @FXML
    public void initialize() {
        configureTable();
        uiControlUtilities.configureDropDown(role, List.of(Role.values()), Enum::toString);
        loadEmployees();
        loadUsers();
        boolean cashier = !userSession.isAdmin();
        createEmployeeButton.setDisable(cashier);
        updateButton.setDisable(cashier);
        deleteButton.setDisable(cashier);
        // populate form on selection
        employeesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> populateForm(newV));
    }

    private void populateForm(EmployeeDTO dto) {
        if (dto == null) return;
        firstName.setText(dto.getFirstName());
        lastName.setText(dto.getLastName());
        username.setText(dto.getUsername());
        email.setText(dto.getEmail());
        telephone.setText(dto.getTelephone());
        salary.setText(String.valueOf(dto.getSalary()));
        role.setValue(dto.getRole());
        password.setText(dto.getPassword());
    }

    private void loadEmployees() {
        var result = employeeService.getAllEmployees();
        if (result.isSuccess()) {
            employeesTable.getItems().setAll(result.value());
        }
    }

    private void loadUsers() {
        var result = userService.getAllUsers();
        if (result.isSuccess()) usersTable.getItems().setAll(result.value());
    }

    private void configureTable(){
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        salaryColumn.setCellFactory(UiControlUtilities.formattedCell(
            salaryValue -> MoneyHelper.formatMoney(
                BigDecimal.valueOf(salaryValue.longValue()))));

        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        userUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    @FXML
    private void createEmployee() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setFirstName(firstName.getText());
        employee.setLastName(lastName.getText());
        employee.setUsername(username.getText());
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
        }else {
            UIHelper.showError("EMPLOYEE NOT CREATED", "There was an error creating the employee, chack you input the proper details");

        }
    }

    @FXML
    private void clearForm() {
        firstName.clear();
        lastName.clear();
        username.clear();
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
        toUpdate.setUsername(username.getText());
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
