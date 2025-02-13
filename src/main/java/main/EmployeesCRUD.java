package main;

import javafx.scene.control.TextField;
import model.Employee;

import java.util.Date;

public class EmployeesCRUD {

    private TextField nameField;
    private TextField surnameField;
    private TextField emailField;
    private TextField addressField;
    private TextField postcodeField;
    private TextField entrydateField;
    private TextField cityField;
    private Boolean adminField;
    private TextField mobileField;
    private TextField phoneField;
    private TextField loginField;
    private TextField passwordField;
    private TextField photoField;
    private TextField adminPasswordField;

    private static final String BASE_URL = "http://localhost:8081/employee";

    public void updateEmployee(){
        Employee.getInstance().setName(nameField.getText());
        Employee.getInstance().setSurname(surnameField.getText());
        Employee.getInstance().setEmail(emailField.getText());
        Employee.getInstance().setAddress(addressField.getText());
        Employee.getInstance().setPostcode(postcodeField.getText());
        Employee.getInstance().setEntrydate(entrydateField.getText());
        Employee.getInstance().setCity(cityField.getText());
    }
}
