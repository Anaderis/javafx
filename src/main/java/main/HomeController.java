package main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import utils.AuthService;

public class HomeController {

    @FXML
    private Button btnLogout;
    @FXML
    private Button btnEmployee;

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
