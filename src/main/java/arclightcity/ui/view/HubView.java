package arclightcity.ui.view;
import arclightcity.item.Item;
import arclightcity.entity.stats.StatType;

import arclightcity.engine.GameEngine;
import arclightcity.entity.player.Player;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.view.MercenaryDialogue;
import arclightcity.ui.util.UIFactory;

/**
 * HubView — hub utama setelah masuk game.
 * Mirip dengan main district screen di Arclight City asli.
 *
 * Layout:
 *   ┌─────────────────────────┐
 *   │  LV.X [NAME]    ⚙ GOLD │
 *   │  ████████░░░░░ 141K/146K│  ← EXP bar
 *   ├─────────────────────────┤
 *   │  HP ████████░░ 120/120  │
 *   │  SHD ██████░░  40/ 40   │
 *   │  MP  ████░░░░  60/ 60   │
 *   ├─────────────────────────┤
 *   │  ARCLIGHT CITY          │  ← district banner
 *   │  Floor depth: 0         │
 *   ├─────────────────────────┤
 *   │  [ ENTER DUNGEON ]      │
 *   │  [ MERCENARY ]          │
 *   │  [ INVENTORY ]          │
 *   │  [ PROFILE ]            │
 *   ├─────────────────────────┤
 *   │  [REWARDS][SHOP][CRAFT] │  ← bottom nav
 *   └─────────────────────────┘
 */
public class HubView {

    private final GameEngine  engine;
    private final SceneRouter router;

    public HubView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    public Parent build() {
        Player player = engine.getPlayer();
        VBox root = UIFactory.screenRoot();

        // ── Top: Player info bar ─────────────────────────
        root.getChildren().add(buildPlayerBar(player));

        // ── Vitals ────────────────────────────────────────
        root.getChildren().add(buildVitalsPanel(player));

        // ── District banner ───────────────────────────────
        root.getChildren().add(buildDistrictBanner(player));

        // ── Main navigation buttons ───────────────────────
        VBox navButtons = new VBox(10);
        navButtons.setPadding(new Insets(16));
        VBox.setVgrow(navButtons, Priority.ALWAYS);

        Button enterDungeon = UIFactory.btnGold("▶  ENTER DUNGEON");
        enterDungeon.setOnAction(e -> {
            engine.startDungeonRun();
            router.emitChat(MercenaryDialogue.Trigger.HUB_ENTER_DUNGEON);
            router.showDungeonMap();
        });

        Button mercenary = UIFactory.btnPrimary("◈  MERCENARY");
        mercenary.setOnAction(e -> router.showMercenary());

        Button inventory = UIFactory.btnPrimary("⊞  INVENTORY");
        inventory.setOnAction(e -> router.showInventory());

        Button profile = UIFactory.btnPrimary("☰  PROFILE");
        profile.setOnAction(e -> router.showProfile());

        navButtons.getChildren().addAll(enterDungeon, mercenary, inventory, profile);
        root.getChildren().add(navButtons);

        root.getChildren().add(UIFactory.spacer());

        // ── Bottom nav bar ────────────────────────────────
        root.getChildren().add(buildBottomNav());

        UIFactory.fadeIn(root, 400);

        // Emit mercenary idle chat saat masuk hub
        javafx.animation.Timeline hubIdle = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(800),
                e -> router.emitChat(MercenaryDialogue.Trigger.HUB_IDLE))
        );
        hubIdle.play();

        return root;
    }

    // ── Player info bar ──────────────────────────────────

    private VBox buildPlayerBar(Player player) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(12, 16, 8, 16));
        box.setStyle("-fx-background-color: #0C1220; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        // Name + level row
        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label levelBadge = new Label("LV." + player.getLevel());
        levelBadge.setStyle(
            "-fx-background-color: #00E5FF22;" +
            "-fx-border-color: #00E5FF;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #00E5FF;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 2 6;"
        );

        Label nameLabel = new Label(player.getName().toUpperCase());
        nameLabel.setStyle(
            "-fx-text-fill: #E0E8F0;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;"
        );

        Label gold = new Label("⚙ " + UIFactory.formatNumber(player.getGold()));
        gold.setStyle("-fx-text-fill: #FFD600; -fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        nameRow.getChildren().addAll(levelBadge, nameLabel, gold);

        // EXP bar
        HBox expRow = new HBox(8);
        expRow.setAlignment(Pos.CENTER_LEFT);

        Label expLabel = new Label("EXP");
        expLabel.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        ProgressBar expBar = new ProgressBar(player.getExpPercent());
        expBar.setPrefWidth(Double.MAX_VALUE);
        expBar.setPrefHeight(5);
        expBar.setStyle(
            "-fx-accent: #FFD600;" +
            "-fx-background-color: #1C1400;" +
            "-fx-min-height: 5px; -fx-max-height: 5px;"
        );
        HBox.setHgrow(expBar, Priority.ALWAYS);

        String expText = UIFactory.formatNumber((long)player.getCurrentExp()) + " / " +
                         UIFactory.formatNumber((long)player.getExpToNextLevel());
        Label expVal = new Label(expText);
        expVal.setStyle("-fx-text-fill: #FFD600; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        expRow.getChildren().addAll(expLabel, expBar, expVal);

        box.getChildren().addAll(nameRow, expRow);
        return box;
    }

    // ── Vitals panel ─────────────────────────────────────

    private VBox buildVitalsPanel(Player player) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10, 16, 10, 16));
        box.setStyle("-fx-background-color: #080D18; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        box.getChildren().add(UIFactory.vitalBar("HEALTH",
                UIFactory.RED,
                player.getCurrentHp(),
                player.getStats().get(StatType.MAX_HP)));

        double maxShield = player.getStats().get(StatType.MAX_SHIELD);
        if (maxShield > 0) {
            box.getChildren().add(UIFactory.vitalBar("SHIELD",
                    UIFactory.PURPLE,
                    player.getCurrentShield(), maxShield));
        }

        box.getChildren().add(UIFactory.vitalBar("ENERGY",
                "#2979FF",
                player.getCurrentMp(),
                player.getStats().get(StatType.MAX_MP)));

        return box;
    }

    // ── District banner ───────────────────────────────────

    private VBox buildDistrictBanner(Player player) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.setStyle(
            "-fx-background-color: #0C1220;" +
            "-fx-border-color: #1C2E44;" +
            "-fx-border-width: 0 0 1 0;"
        );

        Label subtitle = new Label("ARCLIGHT CITY — SECTOR 7");
        subtitle.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");

        Label district = new Label("NEON SLUM DISTRICT");
        district.setStyle(
            "-fx-text-fill: #00E5FF;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, #00E5FF, 8, 0.3, 0, 0);"
        );

        int depth = player.getDungeonDepth();
        Label depthLabel = new Label("DEEPEST FLOOR: " + (depth > 0 ? depth : "NOT EXPLORED"));
        depthLabel.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        HBox mercInfo = new HBox(8);
        mercInfo.setAlignment(Pos.CENTER_LEFT);
        Label mercLabel = new Label("CREW: " + engine.getActiveMercs().size() + "/2 active");
        mercLabel.setStyle("-fx-text-fill: #8899AA; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        mercInfo.getChildren().add(mercLabel);

        box.getChildren().addAll(subtitle, district, depthLabel, mercInfo);
        return box;
    }

    // ── Bottom nav ────────────────────────────────────────

    private HBox buildBottomNav() {
        HBox nav = new HBox(0);
        nav.setAlignment(Pos.CENTER);
        nav.setStyle(
            "-fx-background-color: #0C1220;" +
            "-fx-border-color: #1C2E44;" +
            "-fx-border-width: 1 0 0 0;" +
            "-fx-padding: 8 0;"
        );

        String[][] items = {
            {"⚙", "CRAFT"},
            {"⊞", "INV"},
            {"▷", "DUNGEON"},
            {"◈", "CREW"},
            {"☰", "PROFILE"}
        };

        Runnable[] actions = {
            () -> router.addSystemChat("CRAFT SYSTEM — Coming Soon"),  // Craft
            () -> router.showInventory(),
            () -> { engine.startDungeonRun(); router.showDungeonMap(); },
            () -> router.showMercenary(),
            () -> router.showProfile()
        };

        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            VBox navItem = new VBox(2);
            navItem.setAlignment(Pos.CENTER);
            navItem.setPrefWidth(84);
            navItem.setPadding(new Insets(4));
            navItem.setCursor(javafx.scene.Cursor.HAND);

            Label icon = new Label(items[i][0]);
            icon.setStyle("-fx-text-fill: #5A6A80; -fx-font-size: 16px;");
            Label label = new Label(items[i][1]);
            label.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 8px;");

            navItem.getChildren().addAll(icon, label);
            navItem.setOnMouseClicked(e -> actions[idx].run());
            navItem.setOnMouseEntered(e -> {
                icon.setStyle("-fx-text-fill: #00E5FF; -fx-font-size: 16px;");
                label.setStyle("-fx-text-fill: #00E5FF; -fx-font-family: 'Courier New'; -fx-font-size: 8px;");
            });
            navItem.setOnMouseExited(e -> {
                icon.setStyle("-fx-text-fill: #5A6A80; -fx-font-size: 16px;");
                label.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 8px;");
            });

            nav.getChildren().add(navItem);
        }

        return nav;
    }
}
