package main;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
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
    private Button btnLogout;
    @FXML
    private Button btnEmployee;

    @FXML
    public void initialize() {
        // Récupérer l'utilisateur connecté
        employee = AuthService.getLoggedUser();

        if (employee != null && employee.getAdmin()) {
            System.out.println("Mode Administrateur disponible. Appuyez sur CTRL + ALT + A pour l'activer.");
        }
    }

    /**
     * Cette méthode est appelée lorsque la scène est affichée, ce qui permet d'ajouter l'écouteur de touches.
     */
    public void setStage(Stage stage) {
        stage.setOnShown(event -> {
            Scene scene = stage.getScene();
            if (scene != null) {
                scene.setOnKeyPressed(this::handleAdminKeyCombo);
            }
        });
    }

    /**
     * Vérifie la combinaison de touches pour activer le mode admin.
     */
    private void handleAdminKeyCombo(KeyEvent event) {
        if (event.isControlDown() && event.isAltDown() && event.getCode() == KeyCode.A) {
            System.out.println("Activation du mode admin demandée...");
            showAdminLoginPopup();
        }
    }

    /**
     * Affiche une pop-up de connexion pour vérifier le mot de passe administrateur.
     */
    private void showAdminLoginPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Authentification Admin");

        Label lblMessage = new Label("Entrez le mot de passe administrateur :");
        PasswordField passwordField = new PasswordField();
        Button btnConfirm = new Button("Valider");
        Label lblError = new Label();

        btnConfirm.setOnAction(event -> {
            String inputPassword = passwordField.getText();
            if (inputPassword.equals(employee.getAdminPassword())) {
                popupStage.close();
                openAdminPage();
            } else {
                lblError.setText("Mot de passe incorrect !");
            }
        });

        VBox vbox = new VBox(10, lblMessage, passwordField, btnConfirm, lblError);
        vbox.setStyle("-fx-padding: 20;");
        popupStage.setScene(new Scene(vbox));
        popupStage.showAndWait();
    }

    /**
     * Ouvre la page Admin si l'authentification réussit.
     */
    private void openAdminPage() {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("/main/admin.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Panel");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
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
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Company Sites");
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
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
