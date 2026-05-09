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

    // Total window size
    // ── Dimensi window 1280×720 (landscape, untuk cutscene video) ──
    public static final double SCREEN_WIDTH  = 1280;
    public static final double SCREEN_HEIGHT = 720;
    public static final double GAME_WIDTH    = 940;  // area game (kiri)
    public static final double CHAT_WIDTH    = 340;  // chat panel (kanan)
    private static GameEngine   engine;
    private static SceneRouter  router;
    private static Stage        primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        engine       = new GameEngine();
        router       = new SceneRouter(stage, engine);

        // Pasang semua engine listener SEKALI di sini
        // Tidak boleh dipanggil lagi dari mana pun
        router.initEngineListeners();

        // Setup stage
        stage.setTitle("MYTHIC ITEM OBTAINED  v0.6.0");
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
