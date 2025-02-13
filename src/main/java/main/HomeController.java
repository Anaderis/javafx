package main;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import utils.AuthService;
import model.Employee;

public class HomeController {

    Employee employee;


    @FXML
    private VBox mainContainer; // Ajoute cette ligne pour récupérer le VBox principal
    @FXML
    private Button btnLogout;
    @FXML
    private Button btnEmployee;

    @FXML
    public void initialize() {
        employee = AuthService.getLoggedUser();
        if (employee != null && employee.getAdmin()) {
            System.out.println("Mode Administrateur disponible. Appuyez sur CTRL + ALT + A pour l'activer.");

        // Attendre que la scène soit prête en écoutant la propriété `sceneProperty` du VBox
        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.isControlDown() && event.isAltDown() && event.getCode() == KeyCode.A) {
                        showAdminLoginPopup();
                        System.out.println("Key pressed: Activation mode admin");
                    }
                });
            }
        });

        }
    }

    private void showAdminLoginPopup() {
        try {
            // Charger le FXML du pop-up d'authentification admin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/administrator/adminPopUp.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la pop-up
            AdminController adminController = loader.getController();

            // Créer la fenêtre modale
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Authentification Admin");

            // Passer le stage à `AdminController` si besoin
            adminController.setPopupStage(popupStage);

            // Afficher la fenêtre
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'ouverture de la pop-up admin : " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        AuthService.logout(); // Clears session data
        loadLoginPage();
    }

    @FXML
    private void handleViewEmployees() {
        try {
            Stage stage = (Stage) btnEmployee.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/employee.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Employees List");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*-----------On navigue vers page SERVICE------------------*/
    @FXML
    private void handleViewServices() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/service.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Company Services");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*-----------On navigue vers page SERVICE------------------*/
    @FXML
    private void handleViewSites() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/site.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Company sites");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleSettings() {
        System.out.println("Navigate to Settings Page"); // Replace with actual navigation
    }

    private void loadLoginPage() {
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/login.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
