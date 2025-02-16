package main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.CRUD.SiteCRUD;
import model.Site;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class SiteController {

    @FXML
    private ListView<Site> siteListView;
    @FXML
    private Button createSite;

    private static final String BASE_URL = "http://localhost:8081/site/read"; // API Endpoint

    /*------------SINGLETON--------------*/
    private static SiteController instance;

    public static SiteController getInstance(){
        if(instance==null){
            instance = new SiteController();
        }
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        loadSite();
        updateCreateSiteButtonVisibility();
    }

    public void updateCreateSiteButtonVisibility() {
        Platform.runLater(() -> {
            boolean isAdmin = AdminController.getInstance().getAdminButton();
            createSite.setVisible(isAdmin);  // Rend le bouton visible/invisible
            createSite.setManaged(isAdmin);  // Ajuste l'espace dans le layout
        });
    }

    /*----------------Connexion API envoi requête HTTP - GET----------------*/

    public void loadSite() {
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

    /*------------------ Affichage de la Liste des sites ---------------------------*/

    public void populateList(String responseBody) {
        Platform.runLater(() -> {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Site> site = mapper.readValue(responseBody, new TypeReference<List<Site>>() {});
            ObservableList<Site> siteList = FXCollections.observableArrayList(site);

            siteListView.setItems(siteList);
            siteListView.setCellFactory(listView -> new SiteCell());

            /*------------Empêche le focus sur la case de l'employé et la disparition du texte-----*/
            siteListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); // Permet la sélection unique
            siteListView.getSelectionModel().clearSelection(); // Empêche de garder la sélection après un clic
            siteListView.setFocusTraversable(false); // Désactive le focus sur la liste

            // Ouvre la page Employée pour afficher la liste des employés avec l'id du Site sélectionné
            siteListView.setOnMouseClicked(event -> {
                Site selectedSite = siteListView.getSelectionModel().getSelectedItem();
                if (selectedSite != null) {
                    openEmployeesPage(selectedSite.getId());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        });
    }
    /*-------------------LISTE spécifique à la création d'un client ( CREATE EMPLOYEE), sans filtre-----------------*/
    public static ObservableList<Site> getSiteList() {
        ObservableList<Site> siteList = FXCollections.observableArrayList();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/site/read"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            List<Site> sites = mapper.readValue(response, new TypeReference<List<Site>>() {});
                            siteList.addAll(sites);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                });

        return siteList;
    }


    /*--------------- Redirection vers la page Employée avec l'id du site sélectionné-----------------*/

    private void openEmployeesPage(Long siteId) {
        try {
            Stage stage = (Stage) siteListView.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/employee.fxml"));
            Scene scene = new Scene(loader.load());

            EmployeesController controller = loader.getController();
            controller.loadEmployeesBySite(siteId); // Load filtered employees

            stage.setScene(scene);
            stage.setTitle("Employees in Selected Site");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*--------------- Création des éléments de la liste : design, boutons etc-----------------*/
            /*--- Récupère la fonction updateItem de Employee et la modifie----*/

    class SiteCell extends ListCell<Site> {

        private final Label nameLabel = new Label();
        private final Label cityLabel = new Label();
        private final VBox layout = new VBox(nameLabel, cityLabel);
        private final Button updateButton = new Button("Mettre à jour");
        private final Button deleteButton = new Button("Supprimer");

        public SiteCell() {
            layout.setSpacing(5);
            layout.setStyle("-fx-padding: 10px; -fx-border-color: lightgray; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");
        }

        @Override
        protected void updateItem(Site site, boolean empty) {
            super.updateItem(site, empty);

            if (empty || site == null) {
                setGraphic(null);
            } else {
                nameLabel.setText("🏢 " + site.getName());
                cityLabel.setText("👥 Ville : " + site.getCity());

                layout.getChildren().clear();
                layout.getChildren().addAll(nameLabel, cityLabel);

                // ✅ Vérifier si l'admin est activé pour afficher le bouton "Update"
                if (AdminController.getInstance().getAdminButton()) {
                    updateCreateSiteButtonVisibility();
                    if (!layout.getChildren().contains(updateButton)) {
                        layout.getChildren().addAll(updateButton);
                    }
                } else {
                    layout.getChildren().remove(updateButton);
                }

                // ✅ Vérifier si l'admin est activé pour afficher le bouton "Delete"
                if (AdminController.getInstance().getAdminButton()) {
                    updateCreateSiteButtonVisibility();
                    if (!layout.getChildren().contains(deleteButton)) {
                        layout.getChildren().addAll(deleteButton);
                    }
                } else {
                    layout.getChildren().remove(deleteButton);
                }

                // ✅ Stocker le site correct pour le bouton Update
                updateButton.setOnAction(event -> {
                    SiteController controller = SiteController.getInstance();
                    if (controller != null) {
                        System.out.println("🟢 Site cliqué : " + site.getId() + " - " + site.getName());
                        controller.handleSiteUpdate(site);
                    } else {
                        System.out.println("🔴 Erreur : Impossible de récupérer SiteController.");
                    }
                });

                // ✅ Stocker le site correct pour le bouton Delete
                deleteButton.setOnAction(event -> {
                    SiteController controller = SiteController.getInstance();
                    if (controller != null) {
                        System.out.println("🟢 Site cliqué : " + site.getId() + " - " + site.getName());
                        controller.handleSiteDelete(site);
                    } else {
                        System.out.println("🔴 Erreur : Impossible de récupérer SiteController.");
                    }
                });

                setGraphic(layout);
            }
        }
    }

    /*----------------Redirection page Site : UPDATE-------------------*/
    @FXML
    public void handleSiteUpdate(Site selectedSite) {
        if (selectedSite == null) {
            System.out.println("❌ Erreur : Aucun site sélectionné !");
            return;
        }

        System.out.println("🟢 handleSiteUpdate() - Site reçu : " + selectedSite.getId() + " - " + selectedSite.getName());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/administrator/SiteUpdate.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la pop-up
            SiteCRUD siteCrudController = loader.getController();
            siteCrudController.setSite(selectedSite);

            // Créer la fenêtre modale
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Mise à jour du Site");

            // Passer le stage à `siteController` si besoin
            siteCrudController.setPopupStage(popupStage);

            // Afficher la fenêtre
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.setOnHidden(event -> {
                System.out.println("🔄 Rafraîchissement de la liste des sites...");
                loadSite(); // ✅ Recharge la liste après fermeture de la popup
            });
            popupStage.showAndWait();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*----------------Redirection page Site : CREATE-------------------*/

    @FXML
    public void handleSiteCreate() {

        try {
            // Charger le FXML du pop-up d'authentification admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/administrator/SiteCreate.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la pop-up
            SiteCRUD siteCrudController = loader.getController();

            // Créer la fenêtre modale
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Création d'un site");

            // Passer le stage à `AdminController` si besoin
            siteCrudController.setPopupStage(popupStage);

            // Afficher la fenêtre
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.setOnHidden(event -> {
                System.out.println("🔄 Rafraîchissement de la liste des sites...");
                loadSite(); // ✅ Recharge la liste après fermeture de la popup
            });
            popupStage.showAndWait();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*----------------Redirection page Site : DELETE-------------------*/

    @FXML
    public void handleSiteDelete(Site selectedSite) {
        if (selectedSite == null) {
            System.out.println("❌ Erreur : Aucun site sélectionné !");
            return;
        }

        System.out.println("🟢 handleSiteDelete() - Site reçu : " + selectedSite.getId() + " - " + selectedSite.getName());

        try {
            // Charger le FXML du pop-up d'authentification admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/administrator/SiteDelete.fxml"));
            Parent root = loader.load();
            System.out.println("réception du fxml");

            // Récupérer le contrôleur de la pop-up
            SiteCRUD siteCrudController = loader.getController();
            siteCrudController.setSite(selectedSite);

            // Créer la fenêtre modale
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Suppression du site");
            siteCrudController.setPopupStage(popupStage);
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
            Stage stage = (Stage) siteListView.getScene().getWindow();
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
