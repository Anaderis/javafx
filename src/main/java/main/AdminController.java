package main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Employee;
import utils.AuthService;
import utils.SceneManager;

public class AdminController {
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button btnConfirm;


    private Stage popupStage;
    private Employee employee;


    private static AdminController instance;
    public Boolean adminButton = false;

    public static AdminController getInstance(){
        if(instance==null){
            instance = new AdminController();
        }
        return instance;
    }

    public Boolean getAdminButton() {
        return adminButton;
    }

    public void setAdminButton(Boolean adminButton){
        this.adminButton = adminButton;
    }


    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
    }

    @FXML
    public void initialize() {
        employee = AuthService.getLoggedUser();
    }

    @FXML
    private void handleLogin() {
        String inputPassword = passwordField.getText();

        if (inputPassword.isEmpty()) {
            errorLabel.setText("Veuillez entrer un mot de passe.");
            return;
        }

        if (employee != null && inputPassword.equals(employee.getAdminPassword())) {
            System.out.println("Authentification réussie !");
            AdminController.getInstance().setAdminButton(true);
            popupStage.close(); // Fermer la fenêtre après succès

        } else {
            errorLabel.setText("Mot de passe incorrect !");
        }
    }

    public void showAdminLoginPopup() {
        if(AuthService.getInstance().isAdmin()) {
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
    }

    @FXML
    private void handleCancel() {
        popupStage.close(); // Fermer la fenêtre si on annule
    }


}
