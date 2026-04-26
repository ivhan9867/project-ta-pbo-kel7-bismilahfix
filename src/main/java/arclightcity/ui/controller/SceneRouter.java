package arclightcity.ui.controller;
import arclightcity.dungeon.DungeonEvent;
import arclightcity.combat.CombatResult;

import arclightcity.engine.GameEngine;
import arclightcity.ui.ArclightApp;
import javafx.scene.Scene;
import javafx.stage.Stage;
import arclightcity.ui.view.*;

/**
 * SceneRouter — satu titik navigasi antar screen.
 * Setiap screen dibuat saat pertama kali dibutuhkan (lazy init).
 */
public class SceneRouter {

    private final Stage       stage;
    private final GameEngine  engine;
    private final String      CSS_PATH = "/ui/style/arclight.css";

    // ── View instances ───────────────────────────────────
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

    // ── Constructor ──────────────────────────────────────

    public SceneRouter(Stage stage, GameEngine engine) {
        this.stage  = stage;
        this.engine = engine;
    }

    // ── Navigation Methods ────────────────────────────────

    public void showMainMenu() {
        if (mainMenuView == null) mainMenuView = new MainMenuView(engine, this);
        setScene(mainMenuView.build());
    }

    public void showCharacterCreate() {
        charCreateView = new CharacterCreateView(engine, this);
        setScene(charCreateView.build());
    }

    public void showHub() {
        hubView = new HubView(engine, this);
        setScene(hubView.build());
    }

    public void showDungeonMap() {
        dungeonMapView = new DungeonMapView(engine, this);
        setScene(dungeonMapView.build());
    }

    public void showCombat() {
        combatView = new CombatView(engine, this);
        setScene(combatView.build());
        combatView.startCombatLoop();
    }

    public void showInventory() {
        inventoryView = new InventoryView(engine, this);
        setScene(inventoryView.build());
    }

    public void showMercenary() {
        mercenaryView = new MercenaryView(engine, this);
        setScene(mercenaryView.build());
    }

    public void showProfile() {
        profileView = new ProfileView(engine, this);
        setScene(profileView.build());
    }

    public void showEvent(DungeonEvent event) {
        eventView = new EventView(engine, this, event);
        setScene(eventView.build());
    }

    public void showShop() {
        shopView = new ShopView(engine, this);
        setScene(shopView.build());
    }

    public void showVictory(CombatResult result) {
        victoryView = new VictoryView(engine, this, result);
        setScene(victoryView.build());
    }

    public void showGameOver() {
        gameOverView = new GameOverView(engine, this);
        setScene(gameOverView.build());
    }

    // ── Helper ───────────────────────────────────────────

    private void setScene(javafx.scene.Parent root) {
        Scene scene = new Scene(root,
                ArclightApp.SCREEN_WIDTH,
                ArclightApp.SCREEN_HEIGHT);
        // Load CSS
        try {
            String css = getClass().getResource(CSS_PATH) != null
                    ? getClass().getResource(CSS_PATH).toExternalForm()
                    : null;
            if (css != null) scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("CSS not found: " + CSS_PATH);
        }
        stage.setScene(scene);
    }

    public Stage      getStage()  { return stage; }
    public GameEngine getEngine() { return engine; }
}
