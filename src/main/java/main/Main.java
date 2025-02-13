package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.SceneManager;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialisation du SceneManager
        SceneManager.getInstance().setPrimaryStage(primaryStage);

        // Charger la première scène (login.fxml)
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("login.fxml")));

        // Définir un listener pour capturer les changements de scène
        SceneManager.getInstance().setOnSceneChange(SceneManager.getInstance()::setupGlobalKeyListener);

        // Définir la scène avec SceneManager
        SceneManager.getInstance().changeScene(scene);

        primaryStage.setTitle("Connexion");
        primaryStage.show();
    }

    public static void showMainView() {
        try {
            Scene scene = new Scene(FXMLLoader.load(Main.class.getResource("home.fxml")));
            SceneManager.getInstance().changeScene(scene); // 🔹 Utilisation de SceneManager pour détecter le changement
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
