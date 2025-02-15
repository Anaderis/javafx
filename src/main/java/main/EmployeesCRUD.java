package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Employee;
import utils.AuthService;
import utils.SceneManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EmployeesCRUD {

    /*--------------------------CHAMPS DU FORMULAIRE UPDATE-----------------------------*/
    @FXML
    private TextField searchField, nameField, surnameField, emailField, addressField, postcodeField,
            cityField, phoneField, mobileField, loginField, photoField;; // 🔹 Search input field
    @FXML
    private CheckBox adminCheckBox;
    @FXML
    private PasswordField passwordField, adminPasswordField;
    @FXML
    private DatePicker entryDatePicker;
    @FXML
    Button btnCancel, btnSave;

    Employee employee;
    HomeController homeController;

    public void setEmployee(Employee employee) {
        this.employee = employee;
        if (employee != null) {
            System.out.println("Employé reçu : " + employee.getName());
            nameField.setText(employee.getName());
            surnameField.setText(employee.getSurname());
            emailField.setText(employee.getEmail());
            phoneField.setText(employee.getPhone());
            cityField.setText(employee.getCity());
            adminCheckBox.setSelected(employee.getAdmin());
        }
    }


    /*----------------------------------UPDATE--------------------------------------------*/
    @FXML

    public void handleSave() {
        if (this.employee == null || this.employee.getId() == null) {
            System.out.println("❌ Erreur : Aucun employé enregistré ou ID manquant !");
            return;
        }

        System.out.println("🚀 Mise à jour de l'employé ID: " + this.employee.getId());

        try {
            Map<String, Object> updatedFields = new HashMap<>();

            if (!nameField.getText().isEmpty()) updatedFields.put("name", nameField.getText());
            if (!surnameField.getText().isEmpty()) updatedFields.put("surname", surnameField.getText());
            if (!emailField.getText().isEmpty()) updatedFields.put("email", emailField.getText());
            if (!addressField.getText().isEmpty()) updatedFields.put("address", addressField.getText());
            if (!postcodeField.getText().isEmpty()) updatedFields.put("postcode", postcodeField.getText());
            if (!cityField.getText().isEmpty()) updatedFields.put("city", cityField.getText());
            if (entryDatePicker.getValue() != null) updatedFields.put("entrydate", entryDatePicker.getValue().toString());
            if (!phoneField.getText().isEmpty()) updatedFields.put("phone", phoneField.getText());
            if (!mobileField.getText().isEmpty()) updatedFields.put("mobile", mobileField.getText());
            if (!loginField.getText().isEmpty()) updatedFields.put("login", loginField.getText());
            if (!passwordField.getText().isEmpty()) updatedFields.put("password", passwordField.getText());
            if (!adminPasswordField.getText().isEmpty()) updatedFields.put("adminPassword", adminPasswordField.getText());
            if (!photoField.getText().isEmpty()) updatedFields.put("photo", photoField.getText());
            updatedFields.put("admin", adminCheckBox.isSelected());

            if (updatedFields.isEmpty()) {
                System.out.println("Aucune modification détectée.");
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(updatedFields);

            // 🔹 Vérifie l'URL de l'API
            String apiUrl = "http://localhost:8081/employee/update/" + this.employee.getId();
            System.out.println("📡 URL API : " + apiUrl);
            System.out.println("📡 Données envoyées : " + requestBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Mise à jour réussie !");
                backEmployeePage();
            } else {
                System.out.println("❌ Erreur de mise à jour : " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la mise à jour de l'employé : " + e.getMessage());
        }
    }



    public void handleCancel(){
        try {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/employee.fxml"));
        Scene scene = new Scene(loader.load());
        SceneManager.getInstance().setOnSceneChange(SceneManager.getInstance()::setupGlobalKeyListener);
        SceneManager.getInstance().changeScene(scene);
        stage.setScene(scene);
        stage.setTitle("Employees List");
        stage.show();
        } catch (Exception e) {
            e.printStackTrace();
    }
    }

    public void backEmployeePage(){
        try {
            Stage stage = (Stage) btnSave.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/employee.fxml"));
            Scene scene = new Scene(loader.load());
            SceneManager.getInstance().setOnSceneChange(SceneManager.getInstance()::setupGlobalKeyListener);
            SceneManager.getInstance().changeScene(scene);
            stage.setScene(scene);
            stage.setTitle("Employees List");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
