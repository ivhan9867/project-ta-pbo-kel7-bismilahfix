package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.ArclightApp;
import arclightcity.ui.util.UIFactory;

/**
 * MainMenuView — screen awal game.
 *
 * Layout:
 *   ┌─────────────────────────┐
 *   │   [SCAN LINES bg]       │
 *   │                         │
 *   │   MYTHIC ITEM OBTAINED         │  ← glowing title
 *   │   ─── CYBERPUNK RPG ─── │
 *   │                         │
 *   │   [ENTER ARCLIGHT]      │  ← NEW GAME
 *   │   [CONTINUE]            │  ← future save
 *   │   [SETTINGS]            │
 *   │                         │
 *   │   v0.1 ALPHA            │
 *   └─────────────────────────┘
 */
public class MainMenuView {

    private final GameEngine  engine;
    private final SceneRouter router;

    public MainMenuView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    public Parent build() {
        // ── Root ─────────────────────────────────────────
        StackPane root = new StackPane();
        root.setPrefSize(ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);
        root.setStyle("-fx-background-color: #050810;");

        // ── Background grid lines ─────────────────────────
        Pane gridBg = buildGridBackground();
        root.getChildren().add(gridBg);

        // ── Scan line overlay ─────────────────────────────
        Pane scanlines = buildScanlines();
        root.getChildren().add(scanlines);

        // ── Content ───────────────────────────────────────
        VBox content = new VBox(0);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(60, 40, 60, 40));

        // City skyline ASCII (decorative)
        Label skyline = new Label(
            "█▀▀▄▄▀▀████▄▄▀▀▀▀██████▀▀▄▄\n" +
            "█  ██  ████  ██  ██████  ██\n" +
            "▀  ▀▀  ▀▀▀▀  ▀▀  ▀▀▀▀▀  ▀▀"
        );
        skyline.setStyle(
            "-fx-text-fill: #1C2E44;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 9px;"
        );
        skyline.setAlignment(Pos.CENTER);

        // ── Title ─────────────────────────────────────────
        VBox titleBlock = new VBox(4);
        titleBlock.setAlignment(Pos.CENTER);

        Label arc = new Label("ARCLIGHT");
        arc.setStyle(
            "-fx-text-fill: #00E5FF;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 44px;" +
            "-fx-font-weight: bold;" +
            // Reduced glow: radius 8 spread 0.4 — readable but still glows
            "-fx-effect: dropshadow(gaussian, #00E5FF, 8, 0.4, 0, 0);"
        );

        Label city = new Label("C I T Y");
        city.setStyle(
            "-fx-text-fill: #E0E8F0;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-letter-spacing: 8px;"
        );

        Label separator = new Label("──────── CYBERPUNK RPG ────────");
        separator.setStyle(
            "-fx-text-fill: #1C2E44;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 10px;"
        );

        titleBlock.getChildren().addAll(arc, city, separator);

        // ── Blinking cursor ───────────────────────────────
        Label cursor = new Label("█");
        cursor.setStyle("-fx-text-fill: #00E5FF; -fx-font-family: 'Courier New'; -fx-font-size: 16px;");
        Timeline blink = new Timeline(
            new KeyFrame(Duration.millis(500),  e -> cursor.setVisible(true)),
            new KeyFrame(Duration.millis(1000), e -> cursor.setVisible(false))
        );
        blink.setCycleCount(Timeline.INDEFINITE);
        blink.play();

        // ── Buttons ───────────────────────────────────────
        VBox buttons = new VBox(12);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(40, 0, 20, 0));

        Button newGame = UIFactory.btnPrimary("[ ENTER MYTHIC ITEM OBTAINED ]");
        newGame.setOnAction(e -> router.showCharacterCreate());

        // CONTINUE — aktif jika ada save
        boolean hasSave = engine.hasSave();
        String saveInfo = hasSave ? engine.getSaveSummary() : "No save data";

        Button continueBtn = new Button("[ CONTINUE ]");
        continueBtn.setMaxWidth(Double.MAX_VALUE);
        continueBtn.setDisable(!hasSave);
        continueBtn.setStyle(
            "-fx-background-color: " + (hasSave ? "#00E5FF11" : "transparent") + ";" +
            "-fx-border-color: " + (hasSave ? "#00E5FF" : "#5A6A80") + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + (hasSave ? "#00E5FF" : "#5A6A80") + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: " + (hasSave ? "hand" : "default") + ";"
        );
        continueBtn.setOnAction(e -> {
            if (engine.loadGame()) router.showHub();
        });

        // Save info label
        Label saveLabel = new Label(saveInfo);
        saveLabel.setStyle(
            "-fx-text-fill: " + (hasSave ? "#5A6A80" : "#2A3A50") + ";" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 10px;"
        );

        buttons.getChildren().addAll(newGame, continueBtn, saveLabel);

        // ── Version info ──────────────────────────────────
        Label version = new Label("v0.1 ALPHA  ·  TUGAS AKHIR OOP");
        version.setStyle(
            "-fx-text-fill: #2A3A50;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 10px;"
        );

        content.getChildren().addAll(
            skyline,
            UIFactory.spacer(),
            titleBlock,
            cursor,
            buttons,
            UIFactory.spacer(),
            version
        );

        root.getChildren().add(content);

        // ── Animate in ────────────────────────────────────
        UIFactory.fadeIn(titleBlock, 800);
        UIFactory.fadeIn(buttons, 1200);

        // Glow pulse di title
        UIFactory.glowPulse(arc, "#00E5FF");

        return root;
    }

    // ── Background grid (cyberpunk city grid aesthetic) ───

    private Pane buildGridBackground() {
        Pane pane = new Pane();
        pane.setPrefSize(ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);

        // Horizontal grid lines (setiap 40px)
        for (int y = 0; y < (int)ArclightApp.SCREEN_HEIGHT; y += 40) {
            Rectangle line = new Rectangle(0, y, (int)ArclightApp.SCREEN_WIDTH, 1);
            line.setFill(Color.web("#1C2E44", 0.15));
            pane.getChildren().add(line);
        }
        // Vertical grid lines
        for (int x = 0; x < (int)ArclightApp.SCREEN_WIDTH; x += 40) {
            Rectangle line = new Rectangle(x, 0, 1, (int)ArclightApp.SCREEN_HEIGHT);
            line.setFill(Color.web("#1C2E44", 0.15));
            pane.getChildren().add(line);
        }

        // Perspective lines dari bawah (cyberpunk city look)
        int cx = (int)(ArclightApp.SCREEN_WIDTH / 2);
        for (int x = 0; x <= (int)ArclightApp.SCREEN_WIDTH; x += 60) {
            javafx.scene.shape.Line perspLine = new javafx.scene.shape.Line(cx, (int)ArclightApp.SCREEN_HEIGHT, x, 300);
            perspLine.setStroke(Color.web("#00E5FF", 0.04));
            perspLine.setStrokeWidth(1);
            pane.getChildren().add(perspLine);
        }

        return pane;
    }

    private Pane buildScanlines() {
        Pane pane = new Pane();
        pane.setPrefSize(ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);
        pane.setStyle("-fx-background-color: transparent;");
        // Scanlines horizontal tipis (setiap 2px)
        for (int y = 0; y < (int)ArclightApp.SCREEN_HEIGHT; y += 3) {
            Rectangle line = new Rectangle(0, y, (int)ArclightApp.SCREEN_WIDTH, 1);
            line.setFill(Color.web("#000000", 0.08));
            pane.getChildren().add(line);
        }
        pane.setMouseTransparent(true);
        return pane;
    }
}
