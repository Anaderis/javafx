package main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.List;

import main.CRUD.ServicesCRUD;
import model.Services;

public class ServicesController {

    @FXML
    private ListView<Services> servicesListView;
    @FXML
    private Button createServices;


    private static final String BASE_URL = "http://localhost:8081/services/read"; // API Endpoint

    /*------------SINGLETON--------------*/
    private static ServicesController instance;

    public static ServicesController getInstance(){
        if(instance==null){
            instance = new ServicesController();
        }
        return instance;
    }
    @FXML
    public void initialize() {
        instance = this;
        loadServices();

        // Masquer ou afficher le bouton selon le statut admin
        updateCreateServicesButtonVisibility();
    }

    public void updateCreateServicesButtonVisibility() {
        Platform.runLater(() -> {
            boolean isAdmin = AdminController.getInstance().getAdminButton();
            createServices.setVisible(isAdmin);  // Rend le bouton visible/invisible
            createServices.setManaged(isAdmin);  // Ajuste l'espace dans le layout
        });
    }


    /*----------------Connexion API envoi requête HTTP - GET----------------*/
    public void loadServices() {
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

    /*------------------ Affichage de la Liste des services ---------------------------*/

    private void populateList(String responseBody) {
        Platform.runLater(() -> {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Services> services = mapper.readValue(responseBody, new TypeReference<List<Services>>() {});
            ObservableList<Services> serviceList = FXCollections.observableArrayList(services);

            servicesListView.setItems(serviceList);
            servicesListView.setCellFactory(listView -> new ServiceCell());

            /*------------Empêche le focus sur la case de l'employé et la disparition du texte-----*/
            servicesListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); // Permet la sélection unique
            servicesListView.getSelectionModel().clearSelection(); // Empêche de garder la sélection après un clic
            servicesListView.setFocusTraversable(false);

            // Ouvre la page Employée pour afficher la liste des employés avec l'id du Service sélectionné
            servicesListView.setOnMouseClicked(event -> {
                Services selectedService = servicesListView.getSelectionModel().getSelectedItem();
                System.out.println(selectedService.getId());
                openEmployeesPage(selectedService.getId());

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        });
    }

    /*-------------------LISTE spécifique à la création d'un client, sans filtre-----------------*/
    public static ObservableList<Services> getServicesList() {
        ObservableList<Services> servicesList = FXCollections.observableArrayList();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/services/read"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            List<Services> services = mapper.readValue(response, new TypeReference<List<Services>>() {});
                            servicesList.addAll(services);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                });

        return servicesList;
    }


    /*--------------- Redirection vers la page Employée avec l'id du service sélectionné-----------------*/

    private void openEmployeesPage(Long serviceId) {
        try {
            System.out.println("OpenEmployeePage"+ serviceId);
            Stage stage = (Stage) servicesListView.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/employee.fxml"));
            Scene scene = new Scene(loader.load());

            EmployeesController controller = loader.getController();
            //récupère une instance de employee controller pour accéder à la function loadEmployeebyService
            controller.loadEmployeesByService(serviceId); // Load filtered employees
            System.out.println(serviceId);
            stage.setScene(scene);
            stage.setTitle("Employees in Selected Service");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*--------------- Création des éléments de la liste : design, boutons etc-----------------*/
    /*--- Récupère la fonction updateItem de Employee et la modifie----*/

    class ServiceCell extends ListCell<Services> {

        private final Label nameLabel = new Label();
        private final Label employeeCountLabel = new Label();
        private final VBox layout = new VBox(nameLabel, employeeCountLabel);
        private final Button updateButton = new Button("Mettre à jour");
        private final Button deleteButton = new Button("Supprimer");

        public ServiceCell() {
            layout.setSpacing(5);
            layout.setStyle("-fx-padding: 10px; -fx-border-color: lightgray; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");
        }

        @Override
        protected void updateItem(Services services, boolean empty) {
            super.updateItem(services, empty);

            if (empty || services == null) {
                setGraphic(null);
            } else {
                nameLabel.setText("🏢 " + services.getName());
                employeeCountLabel.setText("👥 Nombre d'employés : " + services.getHeadcount());

                layout.getChildren().clear();
                layout.getChildren().addAll(nameLabel, employeeCountLabel);

                // ✅ Vérifier si l'admin est activé pour afficher le bouton "Update"
                if (AdminController.getInstance().getAdminButton()) {
                    updateCreateServicesButtonVisibility();
                    if (!layout.getChildren().contains(updateButton)) {
                        layout.getChildren().addAll(updateButton);
                    }
                } else {
                    layout.getChildren().remove(updateButton);
                }

                // ✅ Vérifier si l'admin est activé pour afficher le bouton "Delete"
                if (AdminController.getInstance().getAdminButton()) {
                    updateCreateServicesButtonVisibility();
                    if (!layout.getChildren().contains(deleteButton)) {
                        layout.getChildren().addAll(deleteButton);
                    }
                } else {
                    layout.getChildren().remove(deleteButton);
                }

                // ✅ Stocker le service correct pour le bouton Update
                updateButton.setOnAction(event -> {
                    ServicesController servicesController = ServicesController.getInstance();
                    if (servicesController != null) {
                        System.out.println("🟢 Site cliqué : " + services.getId() + " - " + services.getName());
                        servicesController.handleServicesUpdate(services);
                    } else {
                        System.out.println("🔴 Erreur : Impossible de récupérer ServicesController.");
                    }
                });

                // ✅ Stocker le service correct pour le bouton Delete
                deleteButton.setOnAction(event -> {
                    ServicesController servicesController = ServicesController.getInstance();
                    if (servicesController != null) {
                        System.out.println("🟢 Service cliqué : " + services.getId() + " - " + services.getName());
                        servicesController.handleServicesDelete(services);
                    } else {
                        System.out.println("🔴 Erreur : Impossible de récupérer EmployeesController.");
                    }
                });

                setGraphic(layout);
            }
        }
    }


    /*----------------Redirection page Services : UPDATE-------------------*/
    @FXML
    public void handleServicesUpdate(Services selectedServices) {
        if (selectedServices == null) {
            System.out.println("❌ Erreur : Aucun service sélectionné !");
            return;
        }

        System.out.println("🟢 handleServicesUpdate() - Service reçu : " + selectedServices.getId() + " - " + selectedServices.getName());

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/administrator/ServicesUpdate.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la pop-up
            ServicesCRUD servicesCrudController = loader.getController();
            servicesCrudController.setServices(selectedServices);

            // Créer la fenêtre modale
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Mise à jour du Service");

            // Passer le stage à `siteController` si besoin
            servicesCrudController.setPopupStage(popupStage);

            // Afficher la fenêtre
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.setOnHidden(event -> {
                System.out.println("🔄 Rafraîchissement de la liste des services...");
                loadServices(); // ✅ Recharge la liste après fermeture de la popup
            });
            popupStage.showAndWait();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*----------------Redirection page Services : CREATE-------------------*/

    @FXML
    public void handleServicesCreate() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/administrator/ServicesCreate.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la pop-up
            ServicesCRUD servicesCrudController = loader.getController();

            // Créer la fenêtre modale
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Création d'un service");

            // Passer le stage à `AdminController` si besoin
            servicesCrudController.setPopupStage(popupStage);

            // Afficher la fenêtre
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.setOnHidden(event -> {
                System.out.println("🔄 Rafraîchissement de la liste des services...");
                loadServices(); // ✅ Recharge la liste après fermeture de la popup
            });
            popupStage.showAndWait();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*----------------Redirection page Site : DELETE-------------------*/

    @FXML
    public void handleServicesDelete(Services selectedServices) {
        if (selectedServices == null) {
            System.out.println("❌ Erreur : Aucun service sélectionné !");
            return;
        }

        System.out.println("🟢 handleServicesDelete() - Service reçu : " + selectedServices.getId() + " - " + selectedServices.getName());

        try {
            // Charger le FXML du pop-up d'authentification admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/administrator/ServicesDelete.fxml"));
            Parent root = loader.load();
            System.out.println("réception du fxml");

            // Récupérer le contrôleur de la pop-up
            ServicesCRUD servicesCrudController = loader.getController();
            servicesCrudController.setServices(selectedServices);

            // Créer la fenêtre modale
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Suppression du services");
            servicesCrudController.setPopupStage(popupStage);
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

    /*-------------------Retour page d'accueil-------------------*/

    @FXML
    private void handleBackToHome() {
        try {
            Stage stage = (Stage) servicesListView.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/home.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
