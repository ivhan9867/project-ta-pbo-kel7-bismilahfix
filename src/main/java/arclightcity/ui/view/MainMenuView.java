package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.ui.ArclightApp;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.AssetManager;
import arclightcity.ui.util.UIFactory;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

/**
 * MainMenuView — Layout sesuai desain user:
 *
 *  ┌──────────────────────────────────────────────────┐
 *  │            [Logo Mythic Item Obtained]           │
 *  │                  (background image)              │
 *  │                                                  │
 *  │  ╔══════════════════╗  ╔══════════════════╗     │
 *  │  ║ MULAI PETUALANGAN║  ║    LANJUTKAN     ║     │
 *  │  ╚══════════════════╝  ╚══════════════════╝     │
 *  │           ╔══════════════════╗                  │
 *  │           ║     KELUAR       ║                  │
 *  │           ╚══════════════════╝                  │
 *  └──────────────────────────────────────────────────┘
 *
 * Menggunakan BorderPane.setBottom() untuk GARANTEE posisi bawah.
 */
public class MainMenuView {

    private final GameEngine  engine;
    private final SceneRouter router;

    public MainMenuView(GameEngine engine, SceneRouter router) {
        this.engine = engine; this.router = router;
    }

    public Parent build() {
        // ── Root: background image mengisi seluruh layar ────
        StackPane root = new StackPane();
        root.setPrefSize(ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);
        root.setStyle("-fx-background-color: #050302;");

        var bgImg = AssetManager.bgMainMenu();
        if (bgImg != null) {
            ImageView bg = new ImageView(bgImg);
            bg.setPreserveRatio(false);
            bg.fitWidthProperty().bind(root.widthProperty());
            bg.fitHeightProperty().bind(root.heightProperty());
            root.getChildren().add(bg);
        }

        // ── BorderPane overlay untuk posisi tombol di BAWAH ─
        BorderPane overlay = new BorderPane();
        overlay.setBackground(Background.EMPTY);
        overlay.setPrefSize(ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);

        // ── Panel tombol di BOTTOM ──────────────────────────
        boolean hasSave = engine.hasSave();

        // Baris 1: MULAI PETUALANGAN   LANJUTKAN
        Button newGame = menuBtn("▶  MULAI PETUALANGAN", true);
        newGame.setPrefWidth(290);
        newGame.setOnAction(e -> router.showCharacterCreate());

        Button cont = menuBtn("◈  LANJUTKAN", false);
        cont.setPrefWidth(200);
        cont.setDisable(!hasSave);
        if (!hasSave) cont.setOpacity(0.30);
        cont.setOnAction(e -> router.showSaveLoad(false));

        HBox row1 = new HBox(16, newGame, cont);
        row1.setAlignment(Pos.CENTER);

        // Save info (kecil, di antara row1 dan row2)
        Label saveInfo = null;
        if (hasSave) {
            saveInfo = new Label(engine.getSaveSummary());
            saveInfo.setStyle("-fx-text-fill:rgba(255,255,255,0.30); -fx-font-family:'Courier New';" +
                              "-fx-font-size:10px;");
        }

        // Baris 2: KELUAR (centered)
        Button exit = menuBtnExit("✕  KELUAR");
        exit.setPrefWidth(160);
        exit.setOnAction(e -> javafx.application.Platform.exit());

        HBox row2 = new HBox(exit);
        row2.setAlignment(Pos.CENTER);

        // Version
        Label ver = new Label("v0.9.1  ·  Tugas Akhir PBO 2026");
        ver.setStyle("-fx-text-fill:rgba(255,255,255,0.15); -fx-font-family:'Courier New';" +
                     "-fx-font-size:9px;");

        // Susun VBox tombol
        VBox btnBox = new VBox(12);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(20, 60, 32, 60));
        btnBox.setStyle(
            "-fx-background-color: linear-gradient(to top, rgba(2,1,1,0.88) 60%, rgba(2,1,1,0.0) 100%);"
        );

        btnBox.getChildren().add(row1);
        if (saveInfo != null) btnBox.getChildren().add(saveInfo);
        btnBox.getChildren().addAll(row2, ver);

        overlay.setBottom(btnBox);
        root.getChildren().add(overlay);

        UIFactory.fadeIn(root, 700);
        return root;
    }

    private Button menuBtn(String txt, boolean primary) {
        Button b = new Button(txt);
        String bg  = primary ? "rgba(200,134,10,0.18)" : "rgba(255,255,255,0.06)";
        String bdr = primary ? "rgba(200,134,10,0.80)" : "rgba(255,255,255,0.30)";
        String tc  = primary ? "#FFFFFF" : "rgba(255,255,255,0.70)";
        String base = "-fx-background-color:" + bg + "; -fx-border-color:" + bdr + ";" +
            "-fx-border-width:1; -fx-text-fill:" + tc + "; -fx-font-family:'Courier New';" +
            "-fx-font-size:13px;" + (primary ? "-fx-font-weight:bold;" : "") +
            "-fx-padding:12 24; -fx-cursor:hand; -fx-background-radius:3; -fx-border-radius:3;";
        b.setStyle(base);

        ScaleTransition sc = new ScaleTransition(Duration.millis(100), b);
        b.setOnMouseEntered(e -> {
            b.setStyle("-fx-background-color:rgba(200,134,10,0.35); -fx-border-color:rgba(255,200,60,1.0);" +
                "-fx-border-width:1; -fx-text-fill:#FFFFFF; -fx-font-family:'Courier New';" +
                "-fx-font-size:13px;" + (primary ? "-fx-font-weight:bold;" : "") +
                "-fx-padding:12 24; -fx-cursor:hand; -fx-background-radius:3; -fx-border-radius:3;");
            b.setEffect(new Glow(0.25));
            sc.setToX(1.03); sc.setToY(1.03); sc.play();
        });
        b.setOnMouseExited(e -> {
            b.setStyle(base); b.setEffect(null);
            sc.setToX(1.0); sc.setToY(1.0); sc.play();
        });
        return b;
    }

    private Button menuBtnExit(String txt) {
        Button b = new Button(txt);
        String base = "-fx-background-color:transparent; -fx-border-color:rgba(180,50,30,0.35);" +
            "-fx-border-width:1; -fx-text-fill:rgba(200,70,50,0.60); -fx-font-family:'Courier New';" +
            "-fx-font-size:11px; -fx-padding:8 24; -fx-cursor:hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(
            "-fx-background-color:rgba(180,40,20,0.15); -fx-border-color:rgba(220,70,50,0.80);" +
            "-fx-border-width:1; -fx-text-fill:rgba(220,80,60,0.95); -fx-font-family:'Courier New';" +
            "-fx-font-size:11px; -fx-padding:8 24; -fx-cursor:hand;"));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }
}
