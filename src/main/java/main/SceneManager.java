package utils;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import main.AdminController;
import model.Employee;

import java.util.function.Consumer;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;
    private Consumer<Scene> sceneChangeListener;

    private SceneManager() { }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void setOnSceneChange(Consumer<Scene> listener) {
        this.sceneChangeListener = listener;
    }

    public void changeScene(Scene scene) {
        if (primaryStage != null) {
            primaryStage.setScene(scene);
            System.out.println("✅ Scene changée : " + scene); // 🔹 Vérifie si la scène change bien

            // Exécute le listener si défini
            if (sceneChangeListener != null) {
                sceneChangeListener.accept(scene);
            }

            // Appliquer l'écouteur global au changement de scène
            setupGlobalKeyListener(scene);
        } else {
            System.out.println("❌ SceneManager : primaryStage est NULL !");
        }
    }

    public void setupGlobalKeyListener(Scene scene) {
        if (scene == null) {
            System.out.println("❌ SceneManager : Impossible d'ajouter un listener à une scène NULL !");
            return;
        }

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.isAltDown() && event.getCode() == KeyCode.A) {
                System.out.println("🔑 Combinaison Ctrl + Alt + A détectée !");
                System.out.println(Employee.getInstance().getAdmin());
                AdminController.getInstance().showAdminLoginPopup(); // Ouvre la popup admin
                event.consume();
            }
        });

        System.out.println("🎯 Listener ajouté à la scène : " + scene);
    }
}
