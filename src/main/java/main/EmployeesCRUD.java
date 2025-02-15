package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
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
    Button btnCancel, btnSave, btnConfirm;

    private Stage popupStage;
    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
    }

    Employee employee;
    HomeController homeController;

    //Je récupère toutes les infos dont j'ai besoin de l'employee, depuis Employee
    public void setEmployee(Employee employee) {
        this.employee = employee;
        if (employee != null ) {
            System.out.println("Employé reçu : " + employee.getName());
            if (nameField != null)nameField.setText(employee.getName());
            if (surnameField != null)surnameField.setText(employee.getSurname());
            if (emailField != null)emailField.setText(employee.getEmail());
            if(phoneField != null)phoneField.setText(employee.getPhone());
            if (cityField!= null)cityField.setText(employee.getCity());
            if (adminCheckBox != null)adminCheckBox.setSelected(employee.getAdmin());
        }
    }


    /*----------------------------------UPDATE--------------------------------------------*/
    @FXML

    public void handleUpdate() {
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
                popupStage.close();
            } else {
                System.out.println("❌ Erreur de mise à jour : " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la mise à jour de l'employé : " + e.getMessage());
        }
    }

    /*-------------------------------------CREATE-------------------------------------*/
    @FXML
    public void handleCreate() {
        // 🔹 Vérifier que tous les champs sont remplis
        if (nameField.getText().isEmpty() ||
                surnameField.getText().isEmpty() ||
                emailField.getText().isEmpty() ||
                addressField.getText().isEmpty() ||
                postcodeField.getText().isEmpty() ||
                cityField.getText().isEmpty() ||
                entryDatePicker.getValue() == null ||
                phoneField.getText().isEmpty() ||
                mobileField.getText().isEmpty() ||
                loginField.getText().isEmpty() ||
                passwordField.getText().isEmpty() ||
                adminPasswordField.getText().isEmpty() ||
                photoField.getText().isEmpty()) {

            System.out.println("❌ Erreur : Tous les champs doivent être remplis !");
            showAlert("Erreur", "Tous les champs sont obligatoires.", Alert.AlertType.ERROR);
            return;
        }

        System.out.println("🚀 Création d'un nouvel employé...");

        try {
            // 🔹 Construire le JSON à envoyer
            Map<String, Object> employeeData = new HashMap<>();
            employeeData.put("name", nameField.getText());
            employeeData.put("surname", surnameField.getText());
            employeeData.put("email", emailField.getText());
            employeeData.put("address", addressField.getText());
            employeeData.put("postcode", postcodeField.getText());
            employeeData.put("city", cityField.getText());
            employeeData.put("entrydate", entryDatePicker.getValue().toString());
            employeeData.put("phone", phoneField.getText());
            employeeData.put("mobile", mobileField.getText());
            employeeData.put("login", loginField.getText());
            employeeData.put("password", passwordField.getText());
            employeeData.put("adminPassword", adminPasswordField.getText());
            employeeData.put("photo", photoField.getText());
            employeeData.put("admin", adminCheckBox.isSelected());

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(employeeData);

            // 🔹 URL de création (POST)
            String apiUrl = "http://localhost:8081/employee/create";
            System.out.println("📡 URL API : " + apiUrl);
            System.out.println("📡 Données envoyées : " + requestBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) { // 201 = Created
                System.out.println("✅ Employé créé avec succès !");
                showAlert("Succès", "Employé créé avec succès.", Alert.AlertType.INFORMATION);
                popupStage.close();
            } else {
                System.out.println("❌ Erreur lors de la création : " + response.body());
                showAlert("Erreur", "Impossible de créer l'employé. Vérifiez les informations.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la création de l'employé : " + e.getMessage());
            showAlert("Erreur", "Une erreur est survenue lors de la création.", Alert.AlertType.ERROR);
        }
    }

    /*---------------------------ALERTE CHAMP VIDE CREATE -------------------------*/

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getButtonTypes().setAll(ButtonType.OK);
            alert.showAndWait();
        });
    }

    /*-----------------------------------DELETE---------------------------------*/

    @FXML
    public void handleDelete() {
        if (this.employee == null || this.employee.getId() == null) {
            System.out.println("❌ Erreur : Aucun employé enregistré ou ID manquant !");
            return;
        }

        System.out.println("🗑 Suppression de l'employé ID: " + this.employee.getId());

        try {
            // 🔹 Vérifie l'URL de l'API pour la suppression
            String apiUrl = "http://localhost:8081/employee/delete/" + this.employee.getId();
            System.out.println("📡 URL API : " + apiUrl);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .DELETE() // ✅ Utilisation de DELETE
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ Suppression réussie !");
                Platform.runLater(() -> {
                    popupStage.close();
                    EmployeesController.getInstance().loadEmployees(); // ✅ Recharge la liste après suppression
                });
            } else {
                System.out.println("❌ Erreur de suppression : " + response.body());
                showAlert("Erreur", "Impossible de supprimer cet employé.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la suppression de l'employé : " + e.getMessage());
            showAlert("Erreur", "Une erreur est survenue lors de la suppression.", Alert.AlertType.ERROR);
        }
    }


    public void handleCancel(){
        try {
            popupStage.close();
        } catch (Exception e) {
            e.printStackTrace();
    }
    }



}
