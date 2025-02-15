package main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.Site;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class SiteController {

    @FXML
    private ListView<Site> siteListView;

    private static final String BASE_URL = "http://localhost:8081/site/read"; // API Endpoint

    @FXML
    public void initialize() {
        loadSite();
    }

    private void loadSite() {
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

    public void populateList(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Site> site = mapper.readValue(responseBody, new TypeReference<List<Site>>() {});
            ObservableList<Site> siteList = FXCollections.observableArrayList(site);

            siteListView.setItems(siteList);
            siteListView.setCellFactory(listView -> new SiteCell());

            // Handle clicks to filter employees
            siteListView.setOnMouseClicked(event -> {
                Site selectedSite = siteListView.getSelectionModel().getSelectedItem();
                if (selectedSite != null) {
                    openEmployeesPage(selectedSite.getId());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*-------------------LISTE spécifique à la création d'un client, sans filtre-----------------*/
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

    // Custom Cell Factory for Services List
    static class SiteCell extends ListCell<Site> {
        @Override
        protected void updateItem(Site site, boolean empty) {
            super.updateItem(site, empty);
            if (empty || site == null) {
                setGraphic(null);
            } else {
                setText(site.getName() + " (" + site.getCity() + " .)");
            }
        }
    }
}
