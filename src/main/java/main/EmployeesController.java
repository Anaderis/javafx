package main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
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
import main.CRUD.EmployeesCRUD;
import model.Employee;
import utils.SceneManager;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeesController {

    @FXML
    private ListView<Employee> employeesListView;
    @FXML
    private TextField searchField;
    @FXML
    private Button createEmployee;

    //Mapping pour récupérer le nom du service par employé
    public static final Map<Long, String> servicesMap = new HashMap<>();
    public static final Map<Long, String> siteMap = new HashMap<>();

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
        instance = this;
        loadEmployees();
        handleSearch();
        updateCreateEmployeesButtonVisibility();
    }

    public void updateCreateEmployeesButtonVisibility() {
        Platform.runLater(() -> {
            boolean isAdmin = AdminController.getInstance().getAdminButton();
            createEmployee.setVisible(isAdmin);  // Rend le bouton visible/invisible
            createEmployee.setManaged(isAdmin);  // Ajuste l'espace dans le layout
        });
    }

    /*----------------Connexion API envoi requête HTTP - GET----------------*/

    public void loadEmployees() {
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

    /*--------------------Recherche Employé PAR SERVICE ------------------------*/

    public void loadEmployeesByService(Long serviceId) {
        String url = "http://localhost:8081/employee/readByService/" + serviceId;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    Platform.runLater(() -> {
                        employeeList.clear(); // ✅ Vide la liste avant de la mettre à jour
                        populateList(responseBody);
                    });
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }


    /*--------------------Recherche Employé PAR SITE ------------------------*/


    public void loadEmployeesBySite(Long siteId) {
        String url = "http://localhost:8081/employee/readBySite/" + siteId;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    Platform.runLater(() -> {
                        employeeList.clear(); // ✅ Vide la liste avant de la mettre à jour
                        populateList(responseBody);
                    });
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }


    /*------------------ Affichage de la Liste des employés ---------------------------*/

    private void populateList(String responseBody) {
        Platform.runLater(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Employee> employees = mapper.readValue(responseBody, new TypeReference<List<Employee>>() {});

                employeeList.clear(); // ✅ Efface les anciens résultats avant d'ajouter les nouveaux

                if (employees.isEmpty()) {
                    System.out.println("⚠️ Aucun employé trouvé. La liste est vide.");
                    employeesListView.setItems(null); // Supprime la liste si vide
                    return;
                }

                employeeList.setAll(employees);

                // 🔹 Rafraîchit la liste filtrée et l'associe à la ListView
                filteredEmployees = new FilteredList<>(employeeList, p -> true);
                employeesListView.setItems(filteredEmployees);
                employeesListView.setCellFactory(listView -> new EmployeeCell());
                employeesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                employeesListView.getSelectionModel().clearSelection();
                employeesListView.setFocusTraversable(false);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /*---------------------Filtre employé - Recherche par lettre---------------------*/
    @FXML
    private void handleSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredEmployees.setPredicate(employee -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Show all employees if search is empty
                }
                String lowerCaseFilter = newValue.toLowerCase();

                /* --- Filtre à la touche sur : le contenu du mail, nom, et prénom --------*/
                return employee.getName().toLowerCase().contains(lowerCaseFilter) ||
                        employee.getSurname().toLowerCase().contains(lowerCaseFilter) ||
                        employee.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }


    /*--------------- Création des éléments de la liste : design, boutons etc-----------------*/

     class EmployeeCell extends ListCell<Employee> {
        private final ImageView photoView = new ImageView();
        private final Label nameLabel = new Label();
        private final Label emailLabel = new Label();
        private final Label phoneLabel = new Label();
        private final Label serviceLabel = new Label();
        private final Label siteLabel = new Label();
        private final VBox layout = new VBox(nameLabel, emailLabel, phoneLabel, photoView);
        private final Button updateButton = new Button("Mettre à jour");
        private final Button deleteButton = new Button("Supprimer");

        public EmployeeCell() {
            layout.setSpacing(2);
            layout.setStyle("-fx-padding: 10px; -fx-border-color: lightgray; -fx-border-radius: 5px; -fx-background-color: white; -fx-text-fill: black; -fx-font-family: 'Droid Sans Fallback'; -fx-font-size: 18px;");
            updateButton.setStyle("-fx-background-color: #db6da8; -fx-text-fill:  black;");
            deleteButton.setStyle("-fx-background-color: #4f5f70; -fx-text-fill: white;");

            updateButton.setCursor(Cursor.HAND);
            deleteButton.setCursor(Cursor.HAND);
            photoView.setFitWidth(50);
            photoView.setFitHeight(50);
            photoView.setX(100);
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
                serviceLabel.setText("Service : " + employee.getServiceName());
                siteLabel.setText("Site : " + employee.getSiteCity());


                System.out.println(employee.getServiceName());
                if (employee.getPhoto() != null && !employee.getPhoto().isEmpty()) {
                    photoView.setImage(new Image(employee.getPhoto(), true));
                }

                layout.getChildren().clear();
                // si je fais pas ça, il refuse d'afficher les new children. Obligé de faire 3 fois la vérif admin
                //distinctement, car il ne gère pas add All sur les deux boutons en même temps
                layout.getChildren().addAll(nameLabel, emailLabel, phoneLabel, photoView, serviceLabel, siteLabel);

                // ✅ Vérifier si l'admin est activé pour afficher le bouton "Update"
                if (AdminController.getInstance().getAdminButton()) {
                    updateCreateEmployeesButtonVisibility();
                    if (!layout.getChildren().contains(updateButton)) {
                        layout.getChildren().addAll(updateButton);
                    }
                } else {
                    layout.getChildren().remove(updateButton);
                }

                // ✅ Vérifier si l'admin est activé pour afficher le bouton "Create"
                if (AdminController.getInstance().getAdminButton()) {
                    updateCreateEmployeesButtonVisibility();
                    if (!layout.getChildren().contains(deleteButton)) {
                        layout.getChildren().addAll(deleteButton);
                    }
                } else {
                    layout.getChildren().remove(deleteButton);
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

                // ✅ Stocker l'employee correct pour ce bouton
                deleteButton.setOnAction(event -> {
                    EmployeesController controller = EmployeesController.getInstance();
                    if (controller != null) {
                        System.out.println("🟢 Employé cliqué : " + employee.getId() + " - " + employee.getName());
                        controller.handleEmployeesDelete(employee);
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
            System.out.println("❌ Erreur : Aucun employé sélectionné !");
            return;
        }

        System.out.println("🟢 handleEmployeesUpdate() - Employé reçu : " + selectedEmployee.getId() + " - " + selectedEmployee.getName());

        try {
            // Charger le FXML du pop-up d'authentification admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/administrator/EmployeeUpdate.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la pop-up
            EmployeesCRUD employeesCrudController = loader.getController();
            employeesCrudController.setEmployee(selectedEmployee);

            // Créer la fenêtre modale
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Mise à jour de l'employé");

            // Passer le stage à `AdminController` si besoin
            employeesCrudController.setPopupStage(popupStage);

            // Afficher la fenêtre
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.setOnHidden(event -> {
                System.out.println("🔄 Rafraîchissement de la liste des employés...");
                loadEmployees(); // ✅ Recharge la liste après fermeture de la popup
            });
            popupStage.showAndWait();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEmployeesCreate() {

        try {
            // Charger le FXML du pop-up d'authentification admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/administrator/EmployeeCreate.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la pop-up
            EmployeesCRUD employeesCrudController = loader.getController();

            // Créer la fenêtre modale
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Création d'un employé");

            // Passer le stage à `AdminController` si besoin
            employeesCrudController.setPopupStage(popupStage);

            // Afficher la fenêtre
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.setOnHidden(event -> {
                System.out.println("🔄 Rafraîchissement de la liste des employés...");
                loadEmployees(); // ✅ Recharge la liste après fermeture de la popup
            });
            popupStage.showAndWait();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEmployeesDelete(Employee selectedEmployee) {
        if (selectedEmployee == null) {
            System.out.println("❌ Erreur : Aucun employé sélectionné !");
            return;
        }

        System.out.println("🟢 handleEmployeesDelete() - Employé reçu : " + selectedEmployee.getId() + " - " + selectedEmployee.getName());

        try {
            // Charger le FXML du pop-up d'authentification admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/administrator/EmployeeDelete.fxml"));
            Parent root = loader.load();
            System.out.println("réception du fxml");

            // Récupérer le contrôleur de la pop-up
            EmployeesCRUD employeesCrudController = loader.getController();
            employeesCrudController.setEmployee(selectedEmployee);

            // Créer la fenêtre modale
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Suppression de l'employé");
            employeesCrudController.setPopupStage(popupStage);
            // Passer le stage à `AdminController` si besoin
            //employeesCrudController.setPopupStage(popupStage);

            // Afficher la fenêtre
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.showAndWait();
            //loadEmployees();


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


