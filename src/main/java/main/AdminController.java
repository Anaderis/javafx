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

public class AdminController {

    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button btnConfirm;


    private Stage popupStage;
    private Employee employee;
    public Boolean adminButton;

    public Boolean getAdminButton() {
        return adminButton;
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
            adminButton = true;
            popupStage.close(); // Fermer la fenêtre après succès
        } else {
            errorLabel.setText("Mot de passe incorrect !");
        }
    }

    @FXML
    private void handleCancel() {
        popupStage.close(); // Fermer la fenêtre si on annule
    }


}
