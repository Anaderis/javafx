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
import utils.SceneManager;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.List;

public class EmployeesController {
    @FXML
    private Button btnUpdate, btnSave, btnCancel;
    @FXML
    private ListView<Employee> employeesListView;
    @FXML
    private TextField searchField;

    EmployeesCRUD employeesCRUD;

    private ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private FilteredList<Employee> filteredEmployees;

    private static final String BASE_URL = "http://localhost:8081/employee/read";

    /*------------SINGLETON--------------*/
    private static EmployeesController instance;

    public static EmployeesController getInstance(){
        if(instance==null){
            instance = new EmployeesController();
        }
        return instance;
    }

    @FXML
    public void initialize() {
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


    // 🔹 Custom Cell Factory for ListView (Styled Employee Card)
     class EmployeeCell extends ListCell<Employee> {
        private final ImageView photoView = new ImageView();
        private final Label nameLabel = new Label();
        private final Label emailLabel = new Label();
        private final Label phoneLabel = new Label();
        private final VBox layout = new VBox(nameLabel, emailLabel, phoneLabel, photoView);
        private final Button updateButton = new Button("Update"); // ✅ Déclare le bouton une seule fois

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

                // ✅ Stocker l'employee correct pour ce bouton
                updateButton.setOnAction(event -> {
                    EmployeesController controller = EmployeesController.getInstance();
                    if (controller != null) {
                        System.out.println("🟢 Employé cliqué : " + employee.getId() + " - " + employee.getName());
                        controller.handleEmployeesUpdate(employee);
                    } else {
                        System.out.println("🔴 Erreur : Impossible de récupérer EmployeesController.");
                    }
                });

                setGraphic(layout);
            }
        }

    }


    @FXML
    public void handleEmployeesUpdate(Employee selectedEmployee) {
        if (selectedEmployee == null) {
            System.out.println("Erreur : Aucun employé sélectionné !");
            return;
        }
        System.out.println("🟢 handleEmployeesUpdate() - Employé reçu : " + selectedEmployee.getId() + " - " + selectedEmployee.getName());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("administrator/EmployeeUpdate.fxml"));
            Parent root = loader.load();

            // 🔹 Récupérer le contrôleur de la fenêtre de modification
            EmployeesCRUD employeesCRUDController = loader.getController();
            employeesCRUDController.setEmployee(selectedEmployee); // ✅ Passer l'employé

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Mise à jour employé");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleBackToHome() {
        try {
            Stage stage = (Stage) employeesListView.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/home.fxml"));
            Scene scene = new Scene(loader.load());
            SceneManager.getInstance().setOnSceneChange(SceneManager.getInstance()::setupGlobalKeyListener);
            // Définir la scène avec SceneManager
            SceneManager.getInstance().changeScene(scene);
            stage.setScene(scene);
            stage.setTitle("Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





}


