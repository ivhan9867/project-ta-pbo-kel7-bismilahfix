package arclightcity.ui.controller;

import arclightcity.combat.CombatResult;
import arclightcity.dungeon.DungeonEvent;
import arclightcity.engine.GameEngine;
import arclightcity.item.Inventory;
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

    /** Toast notification — compact pojok kiri atas, hilang otomatis */
    public void showToast(String title, String body, String color) {
        javafx.application.Platform.runLater(() -> {
            // Toast — satu baris kompak, tidak wrap
            javafx.scene.layout.HBox toast = new javafx.scene.layout.HBox(10);
            toast.setPadding(new javafx.geometry.Insets(8, 14, 8, 14));
            toast.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            toast.setStyle(
                "-fx-background-color: #0D0804F0;" +
                "-fx-border-color: " + color + ";" +
                "-fx-border-width: 0 0 0 3;" +
                "-fx-effect: dropshadow(gaussian, " + color + ", 8, 0.4, 0, 1);"
            );
            toast.setMouseTransparent(true);

            javafx.scene.control.Label iconLbl = new javafx.scene.control.Label("●");
            iconLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 8px;");

            javafx.scene.control.Label titleLbl = new javafx.scene.control.Label(title);
            titleLbl.setStyle(
                "-fx-text-fill: " + color + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
                "-fx-font-weight: bold;");

            javafx.scene.control.Label sepLbl = new javafx.scene.control.Label("│");
            sepLbl.setStyle("-fx-text-fill: #3A2810; -fx-font-size: 12px;");

            javafx.scene.control.Label bodyLbl = new javafx.scene.control.Label(body);
            bodyLbl.setStyle(
                "-fx-text-fill: #8A7860;" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 11px;");

            toast.getChildren().addAll(iconLbl, titleLbl, sepLbl, bodyLbl);

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
                toast, new javafx.geometry.Insets(10, 0, 0, 10));

            gameArea.getChildren().add(overlay);

            toast.setTranslateX(-800);
            javafx.animation.TranslateTransition slideIn =
                new javafx.animation.TranslateTransition(
                    javafx.util.Duration.millis(180), toast);
            slideIn.setToX(0);
            slideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

            javafx.animation.FadeTransition fadeOut =
                new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(350), overlay);
            fadeOut.setToValue(0);
            fadeOut.setDelay(javafx.util.Duration.millis(2400));
            fadeOut.setOnFinished(e -> gameArea.getChildren().remove(overlay));

            slideIn.play();
            fadeOut.play();
        });
    }

    /** Tampilkan item picker untuk kalibrasi — dari dungeon event */
    public void showCalibrationPicker() {
        javafx.application.Platform.runLater(() -> {
            Inventory inv = engine.getInventory();
            java.util.List<arclightcity.item.Equipment> equips = new java.util.ArrayList<>();
            if (inv.getEquippedWeapon() != null)     equips.add(inv.getEquippedWeapon());
            if (inv.getEquippedArmor() != null)      equips.add(inv.getEquippedArmor());
            if (inv.getEquippedHelmet() != null)     equips.add(inv.getEquippedHelmet());
            if (inv.getEquippedBoots() != null)      equips.add(inv.getEquippedBoots());
            equips.addAll(inv.getEquipmentInBag());

            javafx.stage.Stage picker = new javafx.stage.Stage();
            picker.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            picker.setTitle("Kalibrasi Item — Pilih Item");

            javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(0);
            root.setStyle("-fx-background-color: #0A0604;");
            root.setPrefWidth(400);

            javafx.scene.control.Label title = new javafx.scene.control.Label("◈  PILIH ITEM UNTUK DIKALIBRASI");
            title.setStyle("-fx-text-fill: #7755BB; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 13px; -fx-font-weight: bold;" +
                          "-fx-padding: 12 16 8 16;" +
                          "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");
            root.getChildren().add(title);

            javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane();
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;");

            javafx.scene.layout.VBox list = new javafx.scene.layout.VBox(0);
            list.setStyle("-fx-background-color: #0A0604;");

            if (equips.isEmpty()) {
                javafx.scene.control.Label none = new javafx.scene.control.Label("Tidak ada item yang bisa dikalibrasi.");
                none.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-padding: 16;");
                list.getChildren().add(none);
            }

            for (arclightcity.item.Equipment eq : equips) {
                String rc = arclightcity.ui.util.UIFactory.rarityColor(eq.getRarity());
                javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
                row.setPadding(new javafx.geometry.Insets(10, 14, 10, 14));
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #1A1008; -fx-border-color: #2A1808;" +
                            "-fx-border-width: 0 0 1 0; -fx-cursor: hand;");

                javafx.scene.layout.VBox info = new javafx.scene.layout.VBox(2);
                javafx.scene.layout.HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

                javafx.scene.control.Label nameLbl = new javafx.scene.control.Label(eq.getFullName());
                nameLbl.setStyle("-fx-text-fill: " + rc + "; -fx-font-family: 'Courier New';" +
                               "-fx-font-size: 12px; -fx-font-weight: bold;");
                javafx.scene.control.Label calLbl = new javafx.scene.control.Label(
                    "Kalibrasi: " + eq.getCalibrationLevel() + "/10  |  +" + eq.getUpgradeLevel());
                calLbl.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
                info.getChildren().addAll(nameLbl, calLbl);

                javafx.scene.control.Button calBtn = new javafx.scene.control.Button("KALIBRASI");
                calBtn.setStyle("-fx-background-color: #7755BB22; -fx-border-color: #7755BB;" +
                               "-fx-border-width: 1; -fx-text-fill: #9977DD;" +
                               "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                               "-fx-padding: 5 10; -fx-cursor: hand;");
                calBtn.setOnAction(e -> {
                    eq.calibrate(new java.util.Random());
                    addSystemChat("◈ " + eq.getName() + " berhasil dikalibrasi!");
                    picker.close();
                    showDungeonMap();
                });

                row.getChildren().addAll(info, calBtn);
                list.getChildren().add(row);
            }

            scroll.setContent(list);
            scroll.setPrefHeight(350);
            root.getChildren().add(scroll);

            javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("LEWATI");
            closeBtn.setMaxWidth(Double.MAX_VALUE);
            closeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                             "-fx-border-width: 1 0 0 0; -fx-text-fill: #5A3A10;" +
                             "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                             "-fx-padding: 10; -fx-cursor: hand;");
            closeBtn.setOnAction(e -> { picker.close(); showDungeonMap(); });
            root.getChildren().add(closeBtn);

            picker.setScene(new javafx.scene.Scene(root));
            picker.show();
        });
    }

    public void showSaveLoad(boolean isSaveMode) {
        var view = new SaveLoadView(engine, this, isSaveMode);
        showWithChat(view.build());
    }

    public void showHub() {
        hubView = new HubView(engine, this);
        showWithChat(hubView.build());
        emitChatDelayed(MercenaryDialogue.Trigger.HUB_IDLE, 700);
    }

    public void showDungeonMap() {
        // Stop animasi view lama sebelum diganti (cegah INDEFINITE timer leak)
        if (dungeonMapView != null) dungeonMapView.stopAnimations();
        // Buat DungeonMapView baru untuk fresh UI
        dungeonMapView = new DungeonMapView(engine, this);
        // WAJIB re-wire listener karena instance baru dibuat
        dungeonMapView.wireEngineListeners();
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
        if (combatView != null) combatView.stopAll();
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
        Platform.runLater(() -> {
            List<arclightcity.entity.mercenary.Mercenary> mercs = engine.getActiveMercs();
            // Fallback ke ownedMercs jika activeMercs kosong
            if (mercs == null || mercs.isEmpty()) mercs = engine.getOwnedMercs();
            if (mercs == null || mercs.isEmpty()) return;
            final var finalMercs = mercs;
            new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(400),
                    e -> Platform.runLater(() ->
                        chatPanel.emitTrigger(finalMercs, trigger)
                    )
                )
            ).play();
        });
    }

    public void emitChatDelayed(MercenaryDialogue.Trigger trigger, int delayMs) {
        Platform.runLater(() -> {
            List<arclightcity.entity.mercenary.Mercenary> mercs = engine.getActiveMercs();
            if (mercs == null || mercs.isEmpty()) mercs = engine.getOwnedMercs();
            if (mercs == null || mercs.isEmpty()) return;
            final var finalMercs = mercs;
            new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(delayMs),
                    e -> Platform.runLater(() ->
                        chatPanel.emitTrigger(finalMercs, trigger)
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
