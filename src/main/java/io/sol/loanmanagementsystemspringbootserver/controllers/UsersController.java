package io.sol.loanmanagementsystemspringbootserver.controllers;

import io.sol.loanmanagementsystemspringbootserver.dtos.EmployeeDTO;
import io.sol.loanmanagementsystemspringbootserver.entities.Role;
import io.sol.loanmanagementsystemspringbootserver.services.EmployeeService;
import io.sol.loanmanagementsystemspringbootserver.utilities.UIHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component
public class UsersController {
    private final EmployeeService employeeService;
    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private TextField telephoneOrEmail;
    @FXML private ComboBox<Role> role;
    @FXML private Label messageLabel;

    public UsersController(EmployeeService employeeService) { this.employeeService = employeeService; }

    @FXML
    public void initialize() {
        role.getItems().addAll(Role.FIELD_OFFICER, Role.CASHIER, Role.MANAGER);
        role.setValue(Role.FIELD_OFFICER);
    }

    @FXML
    private void createUser() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setFirstName(firstName.getText());
        employee.setLastName(lastName.getText());
        employee.setUsername(username.getText());
        employee.setEmail(telephoneOrEmail.getText());
        employee.setRole(role.getValue());
        employee.setPassword(password.getText());
        UIHelper.updateStatusLabel(messageLabel, employeeService.createEmployee(employee));
    }
}
