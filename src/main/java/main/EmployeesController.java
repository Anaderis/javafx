package main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import model.Employee;
import main.AdminController;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeesController {
    @FXML
    private Button btnUpdate, btnSave, btnCancel;
    @FXML
    private ListView<Employee> employeesListView;
    @FXML
    private TextField searchField, nameField, surnameField, emailField, addressField, postcodeField, cityField, phoneField, mobileField, loginField, photoField;; // 🔹 Search input field
    @FXML
    private DatePicker entryDatePicker;
    @FXML
    private PasswordField passwordField, adminPasswordField;
    @FXML
    private CheckBox adminCheckBox;

    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredEmployees;

    private static final String BASE_URL = "http://localhost:8081/employee/read";

    @FXML
    public void initialize(AdminController adminController) {
        btnUpdate.setVisible(adminController.getAdminButton());
        loadEmployees();
        handleSearch();
    }

    private void loadEmployees() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::populateList)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    public void loadEmployeesByService(Long serviceId) {
        String url = "http://localhost:8081/employee/readByService/" + serviceId;

        //instancie un client http pour envoyer des requêtes au serveur de l'api
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        System.out.println(request);
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::populateList)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    public void loadEmployeesBySite(Long siteId) {
        String url = "http://localhost:8081/employee/readBySite/" + siteId;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::populateList)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }


    private void populateList(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Employee> employees = mapper.readValue(responseBody, new TypeReference<List<Employee>>() {});
            employeeList.setAll(employees);

            // 🔹 Create a filtered list and bind it to ListView
            filteredEmployees = new FilteredList<>(employeeList, p -> true);
            employeesListView.setItems(filteredEmployees);
            employeesListView.setCellFactory(listView -> new EmployeeCell());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredEmployees.setPredicate(employee -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Show all employees if search is empty
                }
                String lowerCaseFilter = newValue.toLowerCase();

                return employee.getName().toLowerCase().contains(lowerCaseFilter) ||
                        employee.getSurname().toLowerCase().contains(lowerCaseFilter) ||
                        employee.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    @FXML
    private void handleBackToHome() {
        try {
            Stage stage = (Stage) employeesListView.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/home.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Custom Cell Factory for ListView (Styled Employee Card)
    static class EmployeeCell extends ListCell<Employee> {
        private final ImageView photoView = new ImageView();
        private final Label nameLabel = new Label();
        private final Label emailLabel = new Label();
        private final Label phoneLabel = new Label();
        private final VBox layout = new VBox(nameLabel, emailLabel, phoneLabel, photoView);

        public EmployeeCell() {
            layout.setSpacing(5);
            layout.setStyle("-fx-padding: 10px; -fx-border-color: lightgray; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");
            photoView.setFitWidth(50);
            photoView.setFitHeight(50);
        }

        @Override
        protected void updateItem(Employee employee, boolean empty) {
            super.updateItem(employee, empty);
            if (empty || employee == null) {
                setGraphic(null);
            } else {
                nameLabel.setText("👤 " + employee.getName() + " " + employee.getSurname());
                emailLabel.setText("📧 " + employee.getEmail());
                phoneLabel.setText("📞 " + employee.getPhone());

                if (employee.getPhoto() != null && !employee.getPhoto().isEmpty()) {
                    photoView.setImage(new Image(employee.getPhoto(), true));
                }

                setGraphic(layout);
            }
        }
    }

    @FXML
    private void setPopUpUpdateEmployee(Employee employee, AdminController adminController){
        if(adminController.getAdminButton()){
            try {
                // Charger le FXML du pop-up d'authentification admin
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/administrator/updateEmployeePopUp.fxml"));
                Parent root = loader.load();

                // Créer la fenêtre modale
                Stage popupStage = new Stage();
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.setTitle("Update Employee");

                // Afficher la fenêtre
                Scene scene = new Scene(root);
                popupStage.setScene(scene);
                popupStage.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erreur lors de l'ouverture de la pop-up admin : " + e.getMessage());
            }
        }


    }
    @FXML
    public void updateEmployee(Employee employee, Stage popupStage) {
        if (employee == null) {
            System.out.println("Erreur : Aucun employé sélectionné !");
            return;
        }

        try {
            // 🔹 Créer une Map pour stocker uniquement les champs non vides
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
            updatedFields.put("admin", adminCheckBox.isSelected()); // 🔹 Admin est un booléen

            // Vérifier qu'il y a des champs à mettre à jour
            if (updatedFields.isEmpty()) {
                System.out.println("Aucune modification détectée.");
                return;
            }

            // 🔹 Convertir en JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(updatedFields);

            // 🔹 Construire la requête HTTP
            String apiUrl = "http://localhost:3001/employee/update/" + employee.getId();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            // 🔹 Envoyer la requête et récupérer la réponse
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 🔹 Vérifier la réponse
            if (response.statusCode() == 200) {
                System.out.println("Employé mis à jour avec succès !");
                popupStage.close(); // Fermer la fenêtre après succès
            } else {
                System.out.println("Erreur de mise à jour : " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la mise à jour de l'employé : " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateEmployee(){
        return;
    }
    @FXML
    private void handleDeleteEmployee(){
        return;
    }
}
