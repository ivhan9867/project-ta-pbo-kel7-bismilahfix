package arclightcity.ui;

import arclightcity.engine.GameEngine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import arclightcity.ui.controller.SceneRouter;

/**
 * ArclightApp — entry point JavaFX.
 * Inisialisasi GameEngine dan SceneRouter, lalu tampilkan Main Menu.
 */
public class ArclightApp extends Application {

    public static final double SCREEN_WIDTH  = 420;
    public static final double SCREEN_HEIGHT = 820;

    private static GameEngine   engine;
    private static SceneRouter  router;
    private static Stage        primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        engine       = new GameEngine();
        router       = new SceneRouter(stage, engine);

        // Setup stage
        stage.setTitle("ARCLIGHT CITY");
        stage.setWidth(SCREEN_WIDTH);
        stage.setHeight(SCREEN_HEIGHT);
        stage.setResizable(false);

        // Coba load icon (opsional)
        try {
            stage.getIcons().add(new Image(
                    ArclightApp.class.getResourceAsStream("/assets/icon.png")));
        } catch (Exception ignored) { }

        // Mulai dari Main Menu
        router.showMainMenu();
        stage.show();
    }

    public static GameEngine  getEngine()      { return engine; }
    public static SceneRouter getRouter()       { return router; }
    public static Stage       getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}
