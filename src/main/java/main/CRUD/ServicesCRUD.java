package main.CRUD;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import main.HomeController;
import main.ServicesController;
import model.Services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class ServicesCRUD {



    /*--------------------------CHAMPS DU FORMULAIRE-----------------------------*/
    @FXML
    private TextField nameField, headcountField,  descriptionField;
    @FXML
    Button btnCancel, btnSave, btnConfirm;

    private Stage popupStage;
    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
    }

    Services services;
    HomeController homeController;



    public void initialize() {
    }


    //Je récupère toutes les infos dont j'ai besoin du Service depuis Services
    public void setServices(Services services) {
        this.services = services;
        if (services != null ) {
            System.out.println("Service reçu : " + services.getName());
            if (nameField != null)nameField.setText(services.getName());
            if (headcountField != null)headcountField.setText(services.getHeadcount());
            if (descriptionField != null)descriptionField.setText(services.getDescription());
        }
    }


    /*----------------------------------UPDATE--------------------------------------------*/
    @FXML

    public void handleUpdate() {

        if (this.services== null || this.services.getId() == null) {
            System.out.println("❌ Erreur : Aucun service enregistré ou ID manquant !");
            return;
        }

        System.out.println("🚀 Mise à jour du service ID: " + this.services.getId());

        try {
            Map<String, Object> updatedFields = new HashMap<>();
            /*---On récupère dans le field du formulaire la donnée, que l'on intègre dans le champ JSON associé--*/
            if (!nameField.getText().isEmpty()) updatedFields.put("name", nameField.getText());
            if (!headcountField.getText().isEmpty()) updatedFields.put("headcount", headcountField.getText());
            if (!descriptionField.getText().isEmpty()) updatedFields.put("description", descriptionField.getText());


            if (updatedFields.isEmpty()) {
                System.out.println("Aucune modification détectée.");
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(updatedFields);

            // 🔹 Vérifie l'URL de l'API
            String apiUrl = "http://localhost:8081/services/update/" + this.services.getId();
            System.out.println("📡 URL API : " + apiUrl);
            System.out.println("📡 Données envoyées : " + requestBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("✅ Mise à jour réussie !");
                popupStage.close();
            } else {
                System.out.println("❌ Erreur de mise à jour : " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la mise à jour du service : " + e.getMessage());
        }
    }

    /*-------------------------------------CREATE-------------------------------------*/
    @FXML
    public void handleCreate() {

        // 🔹 Vérifier que tous les champs sont remplis
        if (nameField.getText().isEmpty() ||
                headcountField.getText().isEmpty() ||
                descriptionField.getText().isEmpty()) {

            System.out.println("❌ Erreur : Tous les champs doivent être remplis !");
            showAlert("Erreur", "Tous les champs sont obligatoires.", Alert.AlertType.ERROR);
            return;
        }


        System.out.println("🚀 Création d'un nouveau service...");

        try {
            // Construction du JSON
            Map<String, Object> servicesData = new HashMap<>();
            servicesData.put("name", nameField.getText());
            servicesData.put("headcount", headcountField.getText());
            servicesData.put("description", descriptionField.getText());

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(servicesData);

            // 🔹 URL de création (POST)
            String apiUrl = "http://localhost:8081/services/create";
            System.out.println("📡 URL API : " + apiUrl);
            System.out.println("📡 Données envoyées : " + requestBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("📡 Réponse reçue : " + response.statusCode() + " - " + response.body());


            if (response.statusCode() == 200 || response.statusCode() == 201) { // 201 = Created
                System.out.println("✅ Service créé avec succès !");
                showAlert("Succès", "Service créé avec succès.", Alert.AlertType.INFORMATION);
                popupStage.close();
            } else {
                System.out.println("❌ Erreur lors de la création du service !");
                System.out.println("📡 Code HTTP : " + response.statusCode());
                System.out.println("📡 Réponse du serveur : " + response.body());

                showAlert("Erreur", "Impossible de créer le service.\nDétails : " + response.body(), Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la création du service : " + e.getMessage());
            showAlert("Erreur", "Une erreur est survenue lors de la création.", Alert.AlertType.ERROR);
        }
    }

    /*---------------------------ALERTE CHAMP VIDE CREATE -------------------------*/

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getButtonTypes().setAll(ButtonType.OK);
            alert.showAndWait();
        });
    }

    /*-----------------------------------DELETE---------------------------------*/

    @FXML
    public void handleDelete() {
        if (this.services == null || this.services.getId() == null) {
            System.out.println("❌ Erreur : Aucun service enregistré ou ID manquant !");
            return;
        }

        System.out.println("🗑 Suppression du service ID: " + this.services.getId());

        try {
            // 🔹 Vérifie l'URL de l'API pour la suppression
            String apiUrl = "http://localhost:8081/services/delete/" + this.services.getId();
            System.out.println("📡 URL API : " + apiUrl);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .DELETE() // ✅ Utilisation de DELETE
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("✅ Suppression réussie !");
                Platform.runLater(() -> {
                    popupStage.close();
                    ServicesController.getInstance().loadServices(); // ✅ Recharge la liste après suppression
                });
            } else {
                System.out.println("❌ Erreur de suppression : " + response.body());
                showAlert("Erreur", "Impossible de supprimer ce service.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la suppression du service : " + e.getMessage());
            showAlert("Erreur", "Une erreur est survenue lors de la suppression.", Alert.AlertType.ERROR);
        }
    }


    public void handleCancel(){
        try {
            popupStage.close();
        } catch (Exception e) {
            e.printStackTrace();
    }
    }



}
