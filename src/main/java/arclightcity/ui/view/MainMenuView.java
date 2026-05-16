package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.ui.ArclightApp;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.AssetManager;
import arclightcity.ui.util.UIFactory;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * MainMenuView — Full background, no title text, buttons at bottom.
 * Background: backgrounds/main_menu.png (1280×720, no preserve ratio)
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
        root.setMinSize(ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);
        root.setStyle("-fx-background-color: #050302;");

        // ── Background full-screen (bind ke parent lebar) ─
        var bgImg = AssetManager.bgMainMenu();
        if (bgImg != null) {
            ImageView bg = new ImageView(bgImg);
            bg.setPreserveRatio(false);
            // Bind ke StackPane size agar selalu penuh
            bg.fitWidthProperty().bind(root.widthProperty());
            bg.fitHeightProperty().bind(root.heightProperty());
            root.getChildren().add(bg);
        }

        // ── Overlay tipis di bawah — tempat tombol ────────
        // Gradient dari transparan ke hitam, mulai 55% dari atas
        Rectangle bottomGrad = new Rectangle();
        bottomGrad.widthProperty().bind(root.widthProperty());
        bottomGrad.heightProperty().bind(root.heightProperty());
        bottomGrad.setFill(javafx.scene.paint.Paint.valueOf(
            "linear-gradient(to bottom, transparent 40%, rgba(2,1,1,0.75) 70%, rgba(2,1,1,0.92) 100%)"
        ));
        root.getChildren().add(bottomGrad);

        // ── Konten — seluruh screen, tombol di bawah ──────
        BorderPane layout = new BorderPane();
        layout.setBackground(Background.EMPTY);
        layout.prefWidthProperty().bind(root.widthProperty());
        layout.prefHeightProperty().bind(root.heightProperty());

        // ── Tombol KANAN BAWAH (game logo area di gambar baru)
        VBox menuBox = new VBox(8);
        menuBox.setAlignment(Pos.BOTTOM_CENTER);
        menuBox.setPadding(new Insets(0, 0, 36, 0));

        // Divider ornamen kecil
        Label orn = new Label("── ✦ ──");
        orn.setStyle("-fx-text-fill: rgba(255,255,255,0.25); -fx-font-size: 11px;" +
                     "-fx-font-family: 'Courier New';");

        Button newGame = menuBtn("▶   MULAI PETUALANGAN", true);
        newGame.setOnAction(e -> router.showCharacterCreate());

        boolean hasSave = engine.hasSave();
        Button cont = menuBtn("◈   LANJUTKAN", false);
        cont.setDisable(!hasSave);
        if (!hasSave) cont.setOpacity(0.28);
        cont.setOnAction(e -> router.showSaveLoad(false));

        Button exit = menuBtnExit("✕   KELUAR");
        exit.setOnAction(e -> javafx.application.Platform.exit());

        Label ver = new Label("v0.8.5  ·  Tugas Akhir PBO 2026");
        ver.setStyle("-fx-text-fill: rgba(255,255,255,0.15); -fx-font-family: 'Courier New';" +
                     "-fx-font-size: 9px;");
        ver.setPadding(new Insets(4, 0, 0, 0));

        menuBox.getChildren().addAll(orn, newGame, cont, exit, ver);

        // ── Save info di atas tombol (jika ada save) ──────
        if (hasSave) {
            Label saveInfo = new Label("◈  " + engine.getSaveSummary());
            saveInfo.setStyle("-fx-text-fill: rgba(255,255,255,0.30); -fx-font-family: 'Courier New';" +
                              "-fx-font-size: 10px;");
            menuBox.getChildren().add(1, saveInfo);
        }

        layout.setBottom(menuBox);
        root.getChildren().add(layout);

        // ── Animasi particle shimmer di bawah ─────────────
        addParticles(root);

        UIFactory.fadeIn(root, 1000);
        return root;
    }

    // ── Shimmer particles untuk kesan hidup ───────────────
    private void addParticles(StackPane root) {
        javafx.scene.canvas.Canvas canvas =
            new javafx.scene.canvas.Canvas(ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);
        canvas.setMouseTransparent(true);
        var gc = canvas.getGraphicsContext2D();
        java.util.Random rng = new java.util.Random();

        // 30 titik shimmer di area bawah
        double[][] pts = new double[30][3]; // x,y,phase
        for (int i = 0; i < 30; i++) {
            pts[i][0] = rng.nextDouble() * ArclightApp.SCREEN_WIDTH;
            pts[i][1] = ArclightApp.SCREEN_HEIGHT * 0.45 + rng.nextDouble() * ArclightApp.SCREEN_HEIGHT * 0.55;
            pts[i][2] = rng.nextDouble() * Math.PI * 2;
        }

        Timeline anim = new Timeline(new javafx.animation.KeyFrame(Duration.millis(50), e -> {
            gc.clearRect(0, 0, ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);
            long t = System.currentTimeMillis();
            for (double[] pt : pts) {
                double alpha = 0.25 + 0.25 * Math.sin(t * 0.002 + pt[2]);
                double size  = 1.5 + Math.sin(t * 0.003 + pt[2]);
                gc.setFill(Color.rgb(255, 220, 160, alpha));
                gc.fillOval(pt[0] - size/2, pt[1] - size/2, size, size);
            }
        }));
        anim.setCycleCount(Animation.INDEFINITE);
        anim.play();
        root.getChildren().add(1, canvas); // di atas bg, di bawah overlay
    }

    // ── Button styles ──────────────────────────────────────
    private Button menuBtn(String text, boolean primary) {
        Button b = new Button(text);
        b.setPrefWidth(260);
        String col = primary ? "rgba(255,255,255,0.90)" : "rgba(255,255,255,0.55)";
        String bg  = primary ? "rgba(0,0,0,0.30)" : "rgba(0,0,0,0.15)";
        String bdr = primary ? "rgba(255,255,255,0.50)" : "rgba(255,255,255,0.18)";
        String base = "-fx-background-color: " + bg + ";" +
                      "-fx-border-color: " + bdr + "; -fx-border-width: 1;" +
                      "-fx-text-fill: " + col + ";" +
                      "-fx-font-family: 'Courier New'; -fx-font-size: 13px;" +
                      (primary ? "-fx-font-weight: bold;" : "") +
                      "-fx-padding: 10 24; -fx-cursor: hand;" +
                      "-fx-background-radius: 2; -fx-border-radius: 2;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(base +
            "-fx-background-color: rgba(255,255,255,0.20);" +
            "-fx-border-color: rgba(255,255,255,0.80);" +
            "-fx-text-fill: #FFFFFF;"));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    private Button menuBtnExit(String text) {
        Button b = new Button(text);
        b.setPrefWidth(260);
        String base = "-fx-background-color: transparent;" +
                      "-fx-border-color: rgba(200,50,30,0.30); -fx-border-width: 1;" +
                      "-fx-text-fill: rgba(200,80,60,0.50);" +
                      "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                      "-fx-padding: 7 24; -fx-cursor: hand;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(
            "-fx-background-color: rgba(180,40,20,0.15);" +
            "-fx-border-color: rgba(220,60,40,0.70); -fx-border-width: 1;" +
            "-fx-text-fill: rgba(220,80,60,0.90);" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
            "-fx-padding: 7 24; -fx-cursor: hand;"));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }
}
