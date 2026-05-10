package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.ui.ArclightApp;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * MainMenuView — Layar utama Mythic Item Obtained.
 * Tema: Nusantara Dark Gold — ornamen batik, keris, kahyangan.
 */
public class MainMenuView {

    private final GameEngine  engine;
    private final SceneRouter router;

    public MainMenuView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    public Parent build() {
        StackPane root = new StackPane();
        root.setPrefSize(ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);
        root.setStyle("-fx-background-color: #0A0604;");

        // ── Background image ───────────────────────────────
        javafx.scene.image.Image bgImg = arclightcity.ui.util.AssetManager.bgMainMenu();
        if (bgImg != null) {
            javafx.scene.image.ImageView bgView =
                arclightcity.ui.util.AssetManager.makeIVFill(bgImg,
                    ArclightApp.GAME_WIDTH, ArclightApp.SCREEN_HEIGHT);
            bgView.setOpacity(0.4);
            root.getChildren().add(bgView);
        }

        // ── Background dekoratif ───────────────────────────
        root.getChildren().add(buildBackground());

        // ── Konten utama ───────────────────────────────────
        VBox content = new VBox(0);
        content.setAlignment(Pos.CENTER);
        content.setPrefSize(ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);

        // Ornamen atas
        content.getChildren().add(buildTopOrnament());

        // Spacer
        Region spacer1 = new Region(); spacer1.setPrefHeight(30);
        content.getChildren().add(spacer1);

        // Title block
        content.getChildren().add(buildTitleBlock());

        Region spacer2 = new Region(); spacer2.setPrefHeight(40);
        content.getChildren().add(spacer2);

        // Menu buttons
        content.getChildren().add(buildMenuButtons());

        Region spacer3 = new Region(); spacer3.setPrefHeight(30);
        content.getChildren().add(spacer3);

        // Footer
        content.getChildren().add(buildFooter());

        root.getChildren().add(content);

        UIFactory.fadeIn(root, 800);
        return root;
    }

    // ── Background ────────────────────────────────────────

    private StackPane buildBackground() {
        StackPane bg = new StackPane();
        bg.setPrefSize(ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);

        // Garis ornamen vertikal kiri & kanan (batik style)
        VBox leftBar = new VBox();
        leftBar.setPrefWidth(3);
        leftBar.setPrefHeight(ArclightApp.SCREEN_HEIGHT);
        leftBar.setStyle("-fx-background-color: linear-gradient(to bottom, transparent, #C8860A55, #C8860A, #C8860A55, transparent);");
        StackPane.setAlignment(leftBar, Pos.CENTER_LEFT);
        StackPane.setMargin(leftBar, new Insets(0, 0, 0, 40));

        VBox rightBar = new VBox();
        rightBar.setPrefWidth(3);
        rightBar.setPrefHeight(ArclightApp.SCREEN_HEIGHT);
        rightBar.setStyle("-fx-background-color: linear-gradient(to bottom, transparent, #C8860A55, #C8860A, #C8860A55, transparent);");
        StackPane.setAlignment(rightBar, Pos.CENTER_RIGHT);
        StackPane.setMargin(rightBar, new Insets(0, 40, 0, 0));

        // Titik-titik ornamen di pojok (batik diamond)
        for (Pos pos : new Pos[]{Pos.TOP_LEFT, Pos.TOP_RIGHT, Pos.BOTTOM_LEFT, Pos.BOTTOM_RIGHT}) {
            Label diamond = new Label("◆");
            diamond.setStyle("-fx-text-fill: #3A2810; -fx-font-size: 24px;");
            StackPane.setAlignment(diamond, pos);
            StackPane.setMargin(diamond, new Insets(20));
            bg.getChildren().add(diamond);
        }

        bg.getChildren().addAll(leftBar, rightBar);
        return bg;
    }

    // ── Top ornament ──────────────────────────────────────

    private VBox buildTopOrnament() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);

        // Keris ornament divider
        HBox divider = new HBox(12);
        divider.setAlignment(Pos.CENTER);
        Label left = new Label("─────  ✦  ─────");
        left.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 13px;");
        divider.getChildren().add(left);

        // Sub-label
        Label sub = new Label("✦  MASUKI DUNIA GAIB NUSANTARA  ✦");
        sub.setStyle(
            "-fx-text-fill: #5A3A10;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 10px;" +
            "-fx-letter-spacing: 3;"
        );

        box.getChildren().addAll(divider, sub);
        return box;
    }

    // ── Title block ───────────────────────────────────────

    private VBox buildTitleBlock() {
        VBox block = new VBox(8);
        block.setAlignment(Pos.CENTER);

        // Main title - MYTHIC
        Label mythic = new Label("MYTHIC");
        mythic.setStyle(
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 52px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #FFB830;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 30, 0.8, 0, 0)" +
            "          , dropshadow(gaussian, #FF8800, 10, 0.5, 0, 0);"
        );

        // ITEM OBTAINED
        Label item = new Label("ITEM  OBTAINED");
        item.setStyle(
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #C8860A;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 12, 0.4, 0, 0);" +
            "-fx-letter-spacing: 8;"
        );

        // Ornamen bawah judul
        HBox ornamen = new HBox(8);
        ornamen.setAlignment(Pos.CENTER);
        Label orn = new Label("◈ ─── ROGUELITE  RPG  NUSANTARA ─── ◈");
        orn.setStyle(
            "-fx-text-fill: #5A3A10;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 11px;" +
            "-fx-letter-spacing: 2;"
        );
        ornamen.getChildren().add(orn);

        // Animasi pulse pada judul
        ScaleTransition pulse = new ScaleTransition(Duration.millis(2000), mythic);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.02);  pulse.setToY(1.02);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        block.getChildren().addAll(mythic, item, ornamen);
        return block;
    }

    // ── Menu buttons ──────────────────────────────────────

    private VBox buildMenuButtons() {
        VBox buttons = new VBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.setMaxWidth(320);

        // ── MULAI PETUALANGAN (New Game) ──────────────────
        Button newGame = createMenuButton("▶  MULAI PETUALANGAN", true);
        newGame.setOnAction(e -> router.showCharacterCreate());

        // ── LANJUTKAN (Continue) ──────────────────────────
        boolean hasSave = engine.hasSave();
        Button continueBtn = createMenuButton("◈  LANJUTKAN", false);
        continueBtn.setDisable(!hasSave);
        if (!hasSave) {
            continueBtn.setStyle(continueBtn.getStyle() +
                "-fx-opacity: 0.3; -fx-cursor: default;");
        }
        continueBtn.setOnAction(e -> router.showSaveLoad(false));

        // Save info
        Label saveInfo = new Label(hasSave
            ? "✦ " + engine.getSaveSummary()
            : "── Belum ada data simpanan ──");
        saveInfo.setStyle(
            "-fx-text-fill: " + (hasSave ? "#5A3A10" : "#2A1808") + ";" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 10px;"
        );

        // Divider
        Label div = new Label("─────────────────────────────");
        div.setStyle("-fx-text-fill: #2A1808; -fx-font-family: 'Courier New';");

        // ── KELUAR ────────────────────────────────────────
        Button exitBtn = createDangerButton("✕  KELUAR");
        exitBtn.setOnAction(e -> javafx.application.Platform.exit());

        buttons.getChildren().addAll(newGame, continueBtn, saveInfo, div, exitBtn);
        return buttons;
    }

    private Button createMenuButton(String text, boolean isPrimary) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefWidth(320);
        btn.setStyle(
            "-fx-background-color: " + (isPrimary ? "#C8860A22" : "transparent") + ";" +
            "-fx-border-color: " + (isPrimary ? "#FFB830" : "#5A3A10") + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + (isPrimary ? "#FFB830" : "#A09070") + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 24;" +
            "-fx-cursor: hand;" +
            (isPrimary ? "-fx-effect: dropshadow(gaussian, #C8860A, 8, 0.3, 0, 0);" : "")
        );
        // Hover effect
        String base = btn.getStyle();
        btn.setOnMouseEntered(ev -> btn.setStyle(base +
            "-fx-background-color: #C8860A33;" +
            "-fx-border-color: #FFB830;" +
            "-fx-text-fill: #FFB830;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 14, 0.5, 0, 0);"));
        btn.setOnMouseExited(ev -> btn.setStyle(base));
        return btn;
    }

    private Button createDangerButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefWidth(320);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #3A1808;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #5A2808;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 8 24;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(ev -> btn.setStyle(
            "-fx-background-color: #CC330011;" +
            "-fx-border-color: #CC3300;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #CC3300;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-padding: 8 24; -fx-cursor: hand;"));
        btn.setOnMouseExited(ev -> btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #3A1808; -fx-border-width: 1;" +
            "-fx-text-fill: #5A2808;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-padding: 8 24; -fx-cursor: hand;"));
        return btn;
    }

    // ── Footer ────────────────────────────────────────────

    private HBox buildFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(0, 40, 0, 40));

        Label version = new Label("v0.4.3  ✦  ALPHA  ✦  Tugas Akhir PBO 2026");
        version.setStyle(
            "-fx-text-fill: #2A1808;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 10px;"
        );

        footer.getChildren().add(version);
        return footer;
    }
}
