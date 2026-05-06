package arclightcity.ui.controller;

import arclightcity.combat.CombatResult;
import arclightcity.dungeon.DungeonEvent;
import arclightcity.engine.GameEngine;
import arclightcity.ui.ArclightApp;
import arclightcity.ui.view.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.List;

/**
 * SceneRouter — navigasi antar screen + MercChatPanel global di kanan.
 *
 * FIX ROOT CAUSE chat tidak pernah muncul:
 * Sebelumnya: setiap navigasi buat new Scene() → chatPanel (JavaFX Node)
 * dipindah ke HBox baru → JavaFX otomatis detach dari parent lama →
 * chatPanel kehilangan messageContainer-nya → pesan tidak pernah tampil.
 *
 * Fix: Gunakan SATU Scene permanen dengan layout tetap.
 * - persistentLayout (HBox) = root tunggal, tidak pernah diganti
 * - gameArea (StackPane) = hanya bagian ini yang diganti saat navigasi
 * - chatPanel = tetap di kanan, tidak pernah dipindahkan
 */
public class SceneRouter {

    private final Stage       stage;
    private final GameEngine  engine;
    private final String      CSS_PATH = "/ui/style/arclight.css";

    public final MercChatPanel chatPanel;

    // ── Persistent layout (TIDAK pernah diganti) ─────────────
    private final HBox       persistentLayout;   // root HBox
    private final StackPane  gameArea;           // kiri — diganti setiap navigasi

    private MainMenuView        mainMenuView;
    private CharacterCreateView charCreateView;
    private HubView             hubView;
    private DungeonMapView      dungeonMapView;
    private CombatView          combatView;
    private InventoryView       inventoryView;
    private MercenaryView       mercenaryView;
    private ProfileView         profileView;
    private EventView           eventView;
    private ShopView            shopView;
    private VictoryView         victoryView;
    private GameOverView        gameOverView;

    public SceneRouter(Stage stage, GameEngine engine) {
        this.stage  = stage;
        this.engine = engine;

        // Buat chatPanel SEKALI — tidak pernah dipindahkan
        this.chatPanel = new MercChatPanel();

        // gameArea = container kiri yang isinya diganti tiap navigate
        this.gameArea = new StackPane();
        this.gameArea.setPrefSize(ArclightApp.GAME_WIDTH, ArclightApp.SCREEN_HEIGHT);
        this.gameArea.setMaxWidth(ArclightApp.GAME_WIDTH);
        this.gameArea.setMinWidth(ArclightApp.GAME_WIDTH);

        // persistentLayout = HBox root PERMANEN (gameArea + chatPanel)
        this.persistentLayout = new HBox();
        this.persistentLayout.setStyle("-fx-background-color: #0A0604;");
        this.persistentLayout.getChildren().addAll(gameArea, chatPanel);

        // Buat Scene SEKALI dengan persistent layout
        Scene scene = new Scene(
            persistentLayout,
            ArclightApp.SCREEN_WIDTH,
            ArclightApp.SCREEN_HEIGHT
        );
        try {
            String css = getClass().getResource(CSS_PATH) != null
                    ? getClass().getResource(CSS_PATH).toExternalForm() : null;
            if (css != null) scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("CSS not found: " + CSS_PATH);
        }

        stage.setScene(scene);
        stage.setWidth(ArclightApp.SCREEN_WIDTH);
        stage.setHeight(ArclightApp.SCREEN_HEIGHT);
    }

    // ── Navigation ────────────────────────────────────────────

    public void showMainMenu() {
        mainMenuView = new MainMenuView(engine, this);
        // Main menu full-width: sembunyikan chat panel
        showFullWidth(mainMenuView.build());
    }

    public void showCharacterCreate() {
        charCreateView = new CharacterCreateView(engine, this);
        showFullWidth(charCreateView.build());
    }

    public void showCharacterCreateWith(arclightcity.entity.player.PlayerBackground bg, String name) {
        charCreateView = new CharacterCreateView(engine, this);
        charCreateView.setSelectedBg(bg);
        javafx.application.Platform.runLater(() -> {
            showFullWidth(charCreateView.build());
            charCreateView.setNameText(name);
        });
    }

    public void showCity() {
        var view = new arclightcity.ui.view.CityView(engine, this);
        showWithChat(view.build());
    }

    public void showCityArea(String area) {
        var view = new arclightcity.ui.view.CityView(engine, this);
        view.setArea(area);
        showWithChat(view.build());
    }

    /** Toast notification — compact, pojok kiri atas gameArea, hilang otomatis */
    public void showToast(String title, String body, String color) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.layout.VBox toast = new javafx.scene.layout.VBox(2);
            toast.setPadding(new javafx.geometry.Insets(8, 14, 8, 14));
            toast.setMaxWidth(260);
            toast.setStyle(
                "-fx-background-color: #0F0A06DD;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-width: 0 0 0 3;" +
                "-fx-effect: dropshadow(gaussian, " + color + ", 10, 0.4, 0, 1);"
            );
            toast.setMouseTransparent(true);

            javafx.scene.control.Label titleLbl = new javafx.scene.control.Label(title);
            titleLbl.setStyle(
                "-fx-text-fill: " + color + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
                "-fx-font-weight: bold;");

            javafx.scene.control.Label bodyLbl = new javafx.scene.control.Label(body);
            bodyLbl.setStyle(
                "-fx-text-fill: #8A7860;" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 9px;");
            bodyLbl.setWrapText(true);
            bodyLbl.setMaxWidth(240);

            toast.getChildren().addAll(titleLbl, bodyLbl);

            // Overlay di pojok kiri atas gameArea
            javafx.scene.layout.StackPane overlay =
                new javafx.scene.layout.StackPane(toast);
            overlay.setMouseTransparent(true);
            overlay.setMaxWidth(ArclightApp.GAME_WIDTH);
            overlay.setPrefWidth(ArclightApp.GAME_WIDTH);
            overlay.setMaxHeight(ArclightApp.SCREEN_HEIGHT);
            overlay.setStyle("-fx-background-color: transparent;");
            javafx.scene.layout.StackPane.setAlignment(
                toast, javafx.geometry.Pos.TOP_LEFT);
            javafx.scene.layout.StackPane.setMargin(
                toast, new javafx.geometry.Insets(12, 0, 0, 12));

            gameArea.getChildren().add(overlay);

            // Slide in dari kiri
            toast.setTranslateX(-260);
            javafx.animation.TranslateTransition slideIn =
                new javafx.animation.TranslateTransition(
                    javafx.util.Duration.millis(200), toast);
            slideIn.setToX(0);

            javafx.animation.FadeTransition fadeOut =
                new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), overlay);
            fadeOut.setToValue(0);
            fadeOut.setDelay(javafx.util.Duration.millis(2500));
            fadeOut.setOnFinished(e -> gameArea.getChildren().remove(overlay));

            slideIn.play();
            fadeOut.play();
        });
    }

    public void showHub() {
        hubView = new HubView(engine, this);
        showWithChat(hubView.build());
        emitChatDelayed(MercenaryDialogue.Trigger.HUB_IDLE, 700);
    }

    public void showDungeonMap() {
        // Selalu buat DungeonMapView baru untuk fresh UI
        // tapi wireEngineListeners dipanggil di initDungeonListeners() — hanya sekali
        dungeonMapView = new DungeonMapView(engine, this);
        showWithChat(dungeonMapView.build());
        emitChatDelayed(MercenaryDialogue.Trigger.DUNGEON_ENTER_FLOOR, 600);
    }

    /** Dipanggil SEKALI saat SceneRouter dibuat — pasang semua engine listener */
    public void initEngineListeners() {
        if (dungeonMapView == null) {
            dungeonMapView = new DungeonMapView(engine, this);
        }
        dungeonMapView.wireEngineListeners();
    }

    public void showCombat() {
        combatView = new CombatView(engine, this);
        showWithChat(combatView.build());
        combatView.startCombatLoop();
        emitChatDelayed(MercenaryDialogue.Trigger.COMBAT_START, 1000);
    }

    public void showInventory() {
        inventoryView = new InventoryView(engine, this);
        showWithChat(inventoryView.build());
    }

    public void showMercenary() {
        mercenaryView = new MercenaryView(engine, this);
        showWithChat(mercenaryView.build());
    }

    public void showSkillTree() {
        var view = new arclightcity.ui.view.SkillTreeView(engine, this);
        showWithChat(view.build());
    }

    public void showProfile() {
        profileView = new ProfileView(engine, this);
        showWithChat(profileView.build());
    }

    public void showProfile(String tab) {
        profileView = new ProfileView(engine, this);
        profileView.setActiveTab(tab);
        showWithChat(profileView.build());
    }

    public void showEvent(DungeonEvent event) {
        eventView = new EventView(engine, this, event);
        showWithChat(eventView.build());
    }

    public void showShop() {
        shopView = new ShopView(engine, this);
        showWithChat(shopView.build());
    }

    public void showVictory(CombatResult result) {
        victoryView = new VictoryView(engine, this, result);
        showWithChat(victoryView.build());
    }

    public void showGameOver() {
        gameOverView = new GameOverView(engine, this);
        showWithChat(gameOverView.build());
    }

    // ── Layout Switchers ──────────────────────────────────────

    /**
     * Ganti konten gameArea — chatPanel TIDAK disentuh.
     * Ini fix utama: chatPanel tetap di parent yang sama (persistentLayout).
     */
    private void showWithChat(javafx.scene.Parent content) {
        Platform.runLater(() -> {
            // Reset gameArea ke ukuran normal (jika sebelumnya full-width)
            gameArea.setPrefSize(ArclightApp.GAME_WIDTH, ArclightApp.SCREEN_HEIGHT);
            gameArea.setMaxWidth(ArclightApp.GAME_WIDTH);
            gameArea.setMinWidth(ArclightApp.GAME_WIDTH);
            gameArea.getChildren().setAll(content);
            chatPanel.setVisible(true);
            chatPanel.setManaged(true);
        });
    }

    /**
     * Full width — sembunyikan chat panel (untuk main menu & char create).
     */
    private void showFullWidth(javafx.scene.Parent content) {
        Platform.runLater(() -> {
            // Untuk full width: tampilkan konten di gameArea, sembunyikan chat
            gameArea.getChildren().setAll(content);
            gameArea.setPrefWidth(ArclightApp.SCREEN_WIDTH);
            gameArea.setMaxWidth(ArclightApp.SCREEN_WIDTH);
            chatPanel.setVisible(false);
            chatPanel.setManaged(false);
        });
    }

    // ── Chat Helpers ──────────────────────────────────────────

    public void emitChat(MercenaryDialogue.Trigger trigger) {
        // Double runLater + delay memastikan scene sudah fully rendered
        // sebelum pesan ditambahkan ke messageContainer
        Platform.runLater(() -> {
            List<arclightcity.entity.mercenary.Mercenary> mercs = engine.getActiveMercs();
            // Delay 400ms agar layout selesai dulu
            new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(400),
                    e -> Platform.runLater(() ->
                        chatPanel.emitTrigger(mercs, trigger)
                    )
                )
            ).play();
        });
    }

    public void emitChatDelayed(MercenaryDialogue.Trigger trigger, int delayMs) {
        Platform.runLater(() -> {
            List<arclightcity.entity.mercenary.Mercenary> mercs = engine.getActiveMercs();
            new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(delayMs),
                    e -> Platform.runLater(() ->
                        chatPanel.emitTrigger(mercs, trigger)
                    )
                )
            ).play();
        });
    }

    public void addSystemChat(String message) {
        Platform.runLater(() -> chatPanel.addSystemMessage(message));
    }

    // ── Getters ───────────────────────────────────────────────

    public Stage      getStage()  { return stage; }
    public GameEngine getEngine() { return engine; }
}
