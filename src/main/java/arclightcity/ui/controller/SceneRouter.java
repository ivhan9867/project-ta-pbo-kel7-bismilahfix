package arclightcity.ui.controller;

import arclightcity.combat.CombatResult;
import arclightcity.dungeon.DungeonEvent;
import arclightcity.engine.GameEngine;
import arclightcity.ui.ArclightApp;
import arclightcity.ui.view.*;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * SceneRouter — navigasi antar screen + MercChatPanel global di kanan.
 */
public class SceneRouter {

    private final Stage       stage;
    private final GameEngine  engine;
    private final String      CSS_PATH = "/ui/style/arclight.css";

    public final MercChatPanel chatPanel;

    private MainMenuView      mainMenuView;
    private CharacterCreateView charCreateView;
    private HubView           hubView;
    private DungeonMapView    dungeonMapView;
    private CombatView        combatView;
    private InventoryView     inventoryView;
    private MercenaryView     mercenaryView;
    private ProfileView       profileView;
    private EventView         eventView;
    private ShopView          shopView;
    private VictoryView       victoryView;
    private GameOverView      gameOverView;

    public SceneRouter(Stage stage, GameEngine engine) {
        this.stage     = stage;
        this.engine    = engine;
        this.chatPanel = new MercChatPanel();
    }

    // ── Navigation ────────────────────────────────────────────

    public void showMainMenu() {
        if (mainMenuView == null) mainMenuView = new MainMenuView(engine, this);
        setSceneFullWidth(mainMenuView.build());
    }

    public void showCharacterCreate() {
        charCreateView = new CharacterCreateView(engine, this);
        setSceneFullWidth(charCreateView.build());
    }

    public void showHub() {
        hubView = new HubView(engine, this);
        setSceneWithChat(hubView.build());
    }

    public void showDungeonMap() {
        dungeonMapView = new DungeonMapView(engine, this);
        setSceneWithChat(dungeonMapView.build());
    }

    public void showCombat() {
        combatView = new CombatView(engine, this);
        setSceneWithChat(combatView.build());
        combatView.startCombatLoop();
    }

    public void showInventory() {
        inventoryView = new InventoryView(engine, this);
        setSceneWithChat(inventoryView.build());
    }

    public void showMercenary() {
        mercenaryView = new MercenaryView(engine, this);
        setSceneWithChat(mercenaryView.build());
    }

    public void showProfile() {
        profileView = new ProfileView(engine, this);
        setSceneWithChat(profileView.build());
    }

    public void showProfile(String tab) {
        profileView = new ProfileView(engine, this);
        profileView.setActiveTab(tab);
        setSceneWithChat(profileView.build());
    }

    public void showEvent(DungeonEvent event) {
        eventView = new EventView(engine, this, event);
        setSceneWithChat(eventView.build());
    }

    public void showShop() {
        shopView = new ShopView(engine, this);
        setSceneWithChat(shopView.build());
    }

    public void showVictory(CombatResult result) {
        victoryView = new VictoryView(engine, this, result);
        setSceneWithChat(victoryView.build());
    }

    public void showGameOver() {
        gameOverView = new GameOverView(engine, this);
        setSceneWithChat(gameOverView.build());
    }

    // ── Layout Builders ───────────────────────────────────────

    private void setSceneWithChat(javafx.scene.Parent gameRoot) {
        HBox layout = new HBox();
        layout.setStyle("-fx-background-color: #050810;");
        layout.getChildren().addAll(gameRoot, chatPanel);
        applyScene(layout, ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);
    }

    private void setSceneFullWidth(javafx.scene.Parent root) {
        applyScene(root, ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);
    }

    private void applyScene(javafx.scene.Parent root, double w, double h) {
        Scene scene = new Scene(root, w, h);
        try {
            String css = getClass().getResource(CSS_PATH) != null
                    ? getClass().getResource(CSS_PATH).toExternalForm() : null;
            if (css != null) scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("CSS not found: " + CSS_PATH);
        }
        stage.setScene(scene);
        stage.setWidth(w);
        stage.setHeight(h);
    }

    // ── Chat Helpers ──────────────────────────────────────────

    public void emitChat(MercenaryDialogue.Trigger trigger) {
        chatPanel.emitTrigger(engine.getActiveMercs(), trigger);
    }

    public void addSystemChat(String message) {
        chatPanel.addSystemMessage(message);
    }

    // ── Getters ───────────────────────────────────────────────

    public Stage      getStage()  { return stage; }
    public GameEngine getEngine() { return engine; }
}
