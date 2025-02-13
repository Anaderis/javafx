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
import utils.SceneManager;

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
            SceneManager.getInstance().setOnSceneChange(SceneManager.getInstance()::setupGlobalKeyListener);
            SceneManager.getInstance().changeScene(scene);
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
            SceneManager.getInstance().setOnSceneChange(SceneManager.getInstance()::setupGlobalKeyListener);
            SceneManager.getInstance().changeScene(scene);
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
            SceneManager.getInstance().setOnSceneChange(SceneManager.getInstance()::setupGlobalKeyListener);
            SceneManager.getInstance().changeScene(scene);
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
            SceneManager.getInstance().setOnSceneChange(SceneManager.getInstance()::setupGlobalKeyListener);
            SceneManager.getInstance().changeScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
