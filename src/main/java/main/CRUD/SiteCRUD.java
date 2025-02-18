package main.CRUD;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import main.HomeController;
import main.ServicesController;
import main.SiteController;
import model.Employee;
import model.Site;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SiteCRUD {



    /*--------------------------CHAMPS DU FORMULAIRE-----------------------------*/
    @FXML
    private TextField nameField, cityField, siretField, addressField, postcodeField, emailField, phoneField;
    @FXML
    Button btnCancel, btnSave, btnConfirm;

    private Stage popupStage;
    public void setPopupStage(Stage popupStage) {
        this.popupStage = popupStage;
    }

    /*---On récupère l'objet Site----*/
    Site site;
    HomeController homeController;



    public void initialize() {
    }


    //Je récupère toutes les infos dont j'ai besoin du site depuis le model site
    public void setSite(Site site) {
        this.site = site;
        if (site != null ) {
            System.out.println("Site reçu : " + site.getName());
            if (nameField != null)nameField.setText(site.getName());
            if (cityField != null)cityField.setText(site.getCity());
            if (siretField != null)siretField.setText(site.getSiret());
            if (addressField != null)addressField.setText(site.getAddress());
            if (postcodeField != null)postcodeField.setText(site.getPostcode());
            if (emailField != null)emailField.setText(site.getEmail());
            if (phoneField != null)phoneField.setText(site.getPhone());
        }
    }


    /*----------------------------------UPDATE--------------------------------------------*/
    @FXML

    public void handleUpdate() {

        if (this.site== null || this.site.getId() == null) {
            System.out.println("❌ Erreur : Aucun site enregistré ou ID manquant !");
            return;
        }

        System.out.println("🚀 Mise à jour du site ID: " + this.site.getId());

        try {
            Map<String, Object> updatedFields = new HashMap<>();

            /*---On récupère dans le field du formulaire la donnée, que l'on intègre dans le champ JSON associé--*/
            if (!nameField.getText().isEmpty()) updatedFields.put("name", nameField.getText());
            if (!cityField.getText().isEmpty()) updatedFields.put("city", cityField.getText());
            if (!siretField.getText().isEmpty()) updatedFields.put("siret", siretField.getText());
            if (!addressField.getText().isEmpty()) updatedFields.put("address", addressField.getText());
            if (!postcodeField.getText().isEmpty()) updatedFields.put("postcode", postcodeField.getText());
            if (!emailField.getText().isEmpty()) updatedFields.put("email", emailField.getText());
            if (!phoneField.getText().isEmpty()) updatedFields.put("phone", phoneField.getText());


            if (updatedFields.isEmpty()) {
                System.out.println("Aucune modification détectée.");
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(updatedFields);

            // 🔹 Vérification des employés associés au service
            String url = "http://localhost:8081/employee/readBySite/" + this.site.getId();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(json -> {
                        try {
                            return objectMapper.readValue(json, new TypeReference<List<Employee>>() {});
                        } catch (Exception e) {
                            e.printStackTrace();
                            return List.of(); // Retourne une liste vide en cas d'erreur
                        }
                    })
                    .thenAccept(employees -> {
                        if (employees.isEmpty()) {
                            // ✅ Aucun employé associé → Mise à jour possible
                            updateSite(requestBody);
                        } else {
                            // 🚫 Il y a des employés → Afficher un message d'erreur
                            System.out.println("❌ Impossible de modifier, employés présents !");
                            Platform.runLater(() ->
                                    showAlert("Erreur", "Impossible de modifier ce site, il contient encore des employés.", Alert.AlertType.ERROR)
                            );
                        }
                    })
                    .exceptionally(e -> {
                        e.printStackTrace();
                        System.out.println("❌ Erreur lors de la vérification des employés : " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la mise à jour du service : " + e.getMessage());
        }
    }

    public void updateSite(String requestBody) {
        try {
            // 🔹 Vérifie l'URL de l'API
            String apiUrl = "http://localhost:8081/site/update/" + this.site.getId();
            System.out.println("📡 URL API : " + apiUrl);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("✅ Mise à jour réussie !");
                Platform.runLater(() -> popupStage.close()); // Ferme la fenêtre après MAJ
            } else {
                System.out.println("❌ Erreur de mise à jour : " + response.body());
                Platform.runLater(() ->
                        showAlert("Erreur", "Échec de la mise à jour du site.", Alert.AlertType.ERROR)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la mise à jour du site : " + e.getMessage());
            Platform.runLater(() ->
                    showAlert("Erreur", "Une erreur est survenue lors de la mise à jour.", Alert.AlertType.ERROR)
            );
        }
    }


    /*-------------------------------------CREATE-------------------------------------*/
    @FXML
    public void handleCreate() {

        // 🔹 Vérifier que tous les champs sont remplis
        if (nameField.getText().isEmpty() ||
                cityField.getText().isEmpty() ||
                siretField.getText().isEmpty() ||
                addressField.getText().isEmpty() ||
                postcodeField.getText().isEmpty() ||
                emailField.getText().isEmpty() ||
                phoneField.getText().isEmpty()) {

            System.out.println("❌ Erreur : Tous les champs doivent être remplis !");
            showAlert("Erreur", "Tous les champs sont obligatoires.", Alert.AlertType.ERROR);
            return;
        }


        System.out.println("🚀 Création d'un nouveau site...");

        try {
            // Construction du JSON
            Map<String, Object> siteData = new HashMap<>();
            siteData.put("name", nameField.getText());
            siteData.put("city", cityField.getText());
            siteData.put("siret", siretField.getText());
            siteData.put("address", addressField.getText());
            siteData.put("postcode", postcodeField.getText());
            siteData.put("email", emailField.getText());
            siteData.put("phone", phoneField.getText());

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(siteData);

            // 🔹 URL de création (POST)
            String apiUrl = "http://localhost:8081/site/create";
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
                System.out.println("✅ Site créé avec succès !");
                showAlert("Succès", "Site créé avec succès.", Alert.AlertType.INFORMATION);
                popupStage.close();
            } else {
                System.out.println("❌ Erreur lors de la création du site !");
                System.out.println("📡 Code HTTP : " + response.statusCode());
                System.out.println("📡 Réponse du serveur : " + response.body());

                showAlert("Erreur", "Impossible de créer le site.\n ", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la création du site : " + e.getMessage());
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

    public void handleDelete() {
        if (this.site == null || this.site.getId() == null) {
            System.out.println("❌ Erreur : Aucun site enregistré ou ID manquant !");
            return;
        }

        System.out.println("🗑 Vérification des employés pour le site ID: " + this.site.getId());

        String url = "http://localhost:8081/employee/readBySite/" + this.site.getId();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(json -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        return objectMapper.readValue(json, new TypeReference<List<Employee>>() {});
                    } catch (Exception e) {
                        e.printStackTrace();
                        return List.of(); // Retourne une liste vide en cas d'erreur
                    }
                })
                .thenAccept(employees -> {
                    if (employees.isEmpty()) {
                        deleteSite();
                    } else {
                        System.out.println("❌ Impossible de supprimer, employés présents !");
                        Platform.runLater(() ->
                                showAlert("Erreur", "Impossible de supprimer ce service, il contient encore des employés.", Alert.AlertType.ERROR)
                        );
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    System.out.println("❌ Erreur lors de la vérification des employés : " + e.getMessage());
                    return null;
                });
    }


    // Méthode séparée pour la suppression
    private void deleteSite() {
        try {
            String apiUrl = "http://localhost:8081/site/delete/" + this.site.getId();
            System.out.println("📡 Suppression en cours via l'API : " + apiUrl);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("✅ Suppression réussie !");
                Platform.runLater(() -> {
                    popupStage.close();
                    SiteController.getInstance().loadSite(); // Recharge la liste des services
                });
            } else {
                System.out.println("❌ Erreur de suppression : " + response.body());
                Platform.runLater(() ->
                        showAlert("Erreur", "Impossible de supprimer ce service.", Alert.AlertType.ERROR)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la suppression du service : " + e.getMessage());
            Platform.runLater(() ->
                    showAlert("Erreur", "Une erreur est survenue lors de la suppression.", Alert.AlertType.ERROR)
            );
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
