package main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


import com.fasterxml.jackson.databind.ObjectMapper;
import model.Employee;
import main.AdminController;
import utils.AuthService;

public class LoginController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button btnLogin;

    AdminController adminController;

    private static final String BASE_URL = "http://localhost:8081/employee";

    /**
     * Vérifie les identifiants de l'utilisateur via l'API REST
     */
    @FXML
    private void handleLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        try {
            //authenticate : tente de récupérer un employé correspondant aux identifiants saisis.
            Employee employee = authenticate(login, password);
            if (employee != null) {
                AuthService.setLoggedUser(employee);
               Main.showMainView();
            } else {
                errorLabel.setText("Identifiants incorrects !");
            }
        } catch (Exception e) {
            errorLabel.setText("Erreur de connexion : " + e.getMessage());
            e.printStackTrace(); // Affiche l'erreur complète dans la console
        }

    }

    /**
     * Envoie le login et le mot de passe à l'API et récupère l'objet Employee
     */
    private Employee authenticate(String login, String password) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = String.format("{\"login\":\"%s\", \"password\":\"%s\"}", login, password);

        System.out.println("🔵 JSON Envoyé : " + jsonBody); // DEBUG

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/employee/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("🟡 Code HTTP : " + response.statusCode()); // DEBUG
        System.out.println("🟢 Réponse JSON : " + response.body()); // DEBUG

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), Employee.class);
        }

        return null;
    }

    /**
     * Ouvre la page principale après connexion
     */
    private void openHomePage() {
        try {
            Stage stage = (Stage) loginField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/home.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Home");
            stage.show();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
