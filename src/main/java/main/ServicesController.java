package main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.List;
import model.Services;

public class ServicesController {

    @FXML
    private ListView<Services> servicesListView;

    private static final String BASE_URL = "http://localhost:8081/services/read"; // API Endpoint

    @FXML
    public void initialize() {
        loadServices();
    }

    private void loadServices() {
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

    private void populateList(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Services> services = mapper.readValue(responseBody, new TypeReference<List<Services>>() {});
            ObservableList<Services> serviceList = FXCollections.observableArrayList(services);

            servicesListView.setItems(serviceList);
            servicesListView.setCellFactory(listView -> new ServiceCell());

            // Handle clicks to filter employees
            servicesListView.setOnMouseClicked(event -> {
                Services selectedService = servicesListView.getSelectionModel().getSelectedItem();
                if (selectedService != null) {
                    openEmployeesPage(selectedService.getId());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToHome() {
        try {
            Stage stage = (Stage) servicesListView.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/home.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Home");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEmployeesPage(Long serviceId) {
        try {
            Stage stage = (Stage) servicesListView.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/employee.fxml"));
            Scene scene = new Scene(loader.load());

            EmployeesController controller = loader.getController();
            controller.loadEmployeesByService(serviceId); // Load filtered employees

            stage.setScene(scene);
            stage.setTitle("Employees in Selected Service");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Custom Cell Factory for Services List
    static class ServiceCell extends ListCell<Services> {
        @Override
        protected void updateItem(Services service, boolean empty) {
            super.updateItem(service, empty);
            if (empty || service == null) {
                setGraphic(null);
            } else {
                setText(service.getName() + " (" + service.getHeadcount() + " employees)");
            }
        }
    }
}
