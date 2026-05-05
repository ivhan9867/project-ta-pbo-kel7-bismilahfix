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

    public void showHub() {
        hubView = new HubView(engine, this);
        showWithChat(hubView.build());
        Platform.runLater(() ->
            emitChat(MercenaryDialogue.Trigger.HUB_IDLE));
    }

    public void showDungeonMap() {
        dungeonMapView = new DungeonMapView(engine, this);
        showWithChat(dungeonMapView.build());
    }

    public void showCombat() {
        combatView = new CombatView(engine, this);
        showWithChat(combatView.build());
        combatView.startCombatLoop();
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
        // Platform.runLater memastikan chat ditambah setelah scene selesai render
        Platform.runLater(() ->
            chatPanel.emitTrigger(engine.getActiveMercs(), trigger));
    }

    public void addSystemChat(String message) {
        Platform.runLater(() -> chatPanel.addSystemMessage(message));
    }

    // ── Getters ───────────────────────────────────────────────

    public Stage      getStage()  { return stage; }
    public GameEngine getEngine() { return engine; }
}
