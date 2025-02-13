package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Connexion");
        primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("login.fxml"))));
        primaryStage.show();
    }

    public static void showMainView() {
        try {
            Stage stage = new Stage();
            stage.setTitle("Annuaire Entreprise");
            stage.setScene(new Scene(FXMLLoader.load(Main.class.getResource("home.fxml"))));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
