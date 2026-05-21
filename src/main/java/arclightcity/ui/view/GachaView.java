package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.item.*;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.AssetManager;
import arclightcity.ui.util.UIFactory;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.util.*;

/**
 * GachaView — Altar Artefak full-screen.
 * Pull result ditampilkan sebagai auto-slide reveal satu per satu,
 * lalu grid ringkasan semua hasil.
 */
public class GachaView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private       Label       pityLabel, goldLabel, ticketLabel;

    public GachaView(GameEngine engine, SceneRouter router) {
        this.engine = engine; this.router = router;
    }

    public Parent build() {
        BorderPane root = new BorderPane();
        root.setPrefSize(arclightcity.ui.ArclightApp.SCREEN_WIDTH,
                         arclightcity.ui.ArclightApp.SCREEN_HEIGHT);
        root.setStyle("-fx-background-color:#060308;");
        root.setTop(buildHeader());
        root.setCenter(buildMain());
        UIFactory.fadeIn(root, 500);
        return root;
    }

    // ── Header ──────────────────────────────────────────────────
    private HBox buildHeader() {
        HBox h = new HBox(16);
        h.setPadding(new Insets(12,20,10,20));
        h.setAlignment(Pos.CENTER_LEFT);
        h.setStyle("-fx-background-color:#09050E;-fx-border-color:#3A1A5A;-fx-border-width:0 0 1 0;");

        Button back = new Button("← KEMBALI");
        back.setStyle("-fx-background-color:transparent;-fx-border-color:#5A3A7A;-fx-border-width:1;" +
            "-fx-text-fill:#AA88CC;-fx-font-family:'Courier New';-fx-font-size:11px;-fx-padding:6 14;-fx-cursor:hand;");
        back.setOnAction(e -> router.showHub());

        Label title = new Label("⬡  ALTAR ARTEFAK");
        title.setStyle("-fx-text-fill:#CC88FF;-fx-font-family:'Courier New';-fx-font-size:15px;-fx-font-weight:bold;");
        title.setEffect(new Glow(0.25));

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        goldLabel   = new Label("⚙  " + engine.getPlayer().getGold() + " Gold");
        goldLabel.setStyle("-fx-text-fill:#FFB830;-fx-font-family:'Courier New';-fx-font-size:12px;");
        ticketLabel = new Label("⬡  " + engine.getGachaTickets() + " Tiket");
        ticketLabel.setStyle("-fx-text-fill:#AA88CC;-fx-font-family:'Courier New';-fx-font-size:12px;");
        h.getChildren().addAll(back, sp, title, goldLabel, ticketLabel);
        return h;
    }

    // ── Main area ───────────────────────────────────────────────
    private ScrollPane buildMain() {
        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(24,60,24,60));

        content.getChildren().add(buildPortal());

        pityLabel = new Label();
        updatePityLabel();
        pityLabel.setStyle("-fx-text-fill:rgba(204,136,255,0.75);-fx-font-family:'Courier New';-fx-font-size:11px;");
        Label rates = new Label(
            "MYTHIC 1%  ·  LEGENDARY 4%  ·  EPIC 12%  ·  RARE 18%  ·  UNCOMMON 25%  ·  COMMON 40%");
        rates.setStyle("-fx-text-fill:rgba(255,255,255,0.25);-fx-font-family:'Courier New';-fx-font-size:9px;");
        Label tip = new Label("Tiket Gacha: drop dari boss (18%) dan event dungeon");
        tip.setStyle("-fx-text-fill:rgba(255,255,255,0.18);-fx-font-family:'Courier New';-fx-font-size:9px;");
        VBox info = new VBox(5,pityLabel,rates,tip);
        info.setAlignment(Pos.CENTER);
        content.getChildren().addAll(info, buildPullButtons());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        return scroll;
    }

    // ── Portal ──────────────────────────────────────────────────
    private StackPane buildPortal() {
        StackPane stack = new StackPane();
        stack.setPrefSize(200,200); stack.setMaxSize(200,200);
        Circle bg = new Circle(96, Color.web("#1A0830"));
        bg.setEffect(new DropShadow(35, Color.web("#8833CC44")));
        Circle r3 = new Circle(94,Color.TRANSPARENT);
        r3.setStroke(Color.web("#5A1A8A",0.15)); r3.setStrokeWidth(1);
        Circle r2 = new Circle(76,Color.TRANSPARENT);
        r2.setStroke(Color.web("#8A33AA",0.30)); r2.setStrokeWidth(1.5);
        Circle r1 = new Circle(58,Color.web("#100818"));
        r1.setStroke(Color.web("#CC88FF",0.60)); r1.setStrokeWidth(2);
        r1.setEffect(new Glow(0.25));
        Label icon = new Label("⬡");
        icon.setStyle("-fx-text-fill:#CC88FF;-fx-font-size:48px;");
        icon.setEffect(new Glow(0.5));
        stack.getChildren().addAll(bg,r3,r2,r1,icon);
        RotateTransition rt1 = new RotateTransition(Duration.seconds(11),r3);
        rt1.setByAngle(360); rt1.setCycleCount(Animation.INDEFINITE); rt1.play();
        RotateTransition rt2 = new RotateTransition(Duration.seconds(7),r2);
        rt2.setByAngle(-360); rt2.setCycleCount(Animation.INDEFINITE); rt2.play();
        FadeTransition pulse = new FadeTransition(Duration.millis(1400),icon);
        pulse.setFromValue(0.65); pulse.setToValue(1.0);
        pulse.setCycleCount(Animation.INDEFINITE); pulse.setAutoReverse(true); pulse.play();
        return stack;
    }

    // ── Pull buttons ────────────────────────────────────────────
    private HBox buildPullButtons() {
        HBox row = new HBox(20); row.setAlignment(Pos.CENTER);
        VBox s1  = pullCard("1×  PANGGIL","800 Gold  /  1 Tiket",false);
        s1.setOnMouseClicked(e -> doPull(false));
        VBox s10 = pullCard("10×  PANGGIL","7200 Gold  /  9 Tiket",true);
        s10.setOnMouseClicked(e -> doPull(true));
        row.getChildren().addAll(s1,s10);
        return row;
    }

    private VBox pullCard(String t, String sub, boolean big) {
        VBox card = new VBox(7); card.setAlignment(Pos.CENTER);
        card.setPrefWidth(big?240:220); card.setPrefHeight(80);
        card.setPadding(new Insets(14,22,14,22)); card.setCursor(javafx.scene.Cursor.HAND);
        String base = "-fx-background-color:#330066"+(big?"66":"44")+
            ";-fx-border-color:#9944DD;-fx-border-width:1.5;-fx-background-radius:6;-fx-border-radius:6;";
        card.setStyle(base);
        Label tl = new Label(t);
        tl.setStyle("-fx-text-fill:#F0E0FF;-fx-font-family:'Courier New';-fx-font-size:"+(big?"14":"13")+"px;-fx-font-weight:bold;");
        Label sl = new Label(sub);
        sl.setStyle("-fx-text-fill:rgba(200,170,230,0.55);-fx-font-family:'Courier New';-fx-font-size:10px;");
        card.getChildren().addAll(tl,sl);
        card.setOnMouseEntered(e -> { card.setStyle("-fx-background-color:#5500AA88;-fx-border-color:#CC88FF;-fx-border-width:1.5;-fx-background-radius:6;-fx-border-radius:6;"); card.setEffect(new Glow(0.30)); });
        card.setOnMouseExited(e -> { card.setStyle(base); card.setEffect(null); });
        return card;
    }

    // ── Pull logic ── Full-screen slide reveal ──────────────────
    private void doPull(boolean isTen) {
        // Tampilkan animasi loading sebelum reveal
        showPullAnimation(isTen);
    }

    /**
     * Animasi gacha full: 3 fase
     *  Fase 1 (0-0.8s): Portal muncul + pulse building up
     *  Fase 2 (0.8-2.2s): Portal berputar cepat + flash white
     *  Fase 3 (2.2-2.6s): Fade out + reveal hasil
     */
    private void showPullAnimation(boolean isTen) {
        javafx.scene.Scene scene = router.getStage().getScene();
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color:rgba(4,1,10,0.0);");
        overlay.setPrefSize(arclightcity.ui.ArclightApp.SCREEN_WIDTH,
                            arclightcity.ui.ArclightApp.SCREEN_HEIGHT);
        overlay.setFocusTraversable(true);

        // Flash layer (putih saat peak)
        javafx.scene.shape.Rectangle flash = new javafx.scene.shape.Rectangle(
            arclightcity.ui.ArclightApp.SCREEN_WIDTH, arclightcity.ui.ArclightApp.SCREEN_HEIGHT,
            Color.web("#CC88FF"));
        flash.setOpacity(0);
        flash.setMouseTransparent(true);

        // Portal container — bisa di-animate scale
        StackPane portalWrap = buildAnimPortalFull();
        portalWrap.setOpacity(0);

        // Label teks
        Label calling = new Label(isTen ? "⬡  MEMANGGIL 10 ARTEFAK..." : "⬡  MEMANGGIL ARTEFAK...");
        calling.setStyle("-fx-text-fill:#CC88FF; -fx-font-family:'Courier New';" +
            "-fx-font-size:15px; -fx-font-weight:bold;");
        calling.setOpacity(0);

        Label subtext = new Label(isTen ? "「 10× GACHA 」" : "「 1× GACHA 」");
        subtext.setStyle("-fx-text-fill:rgba(200,150,255,0.50); -fx-font-family:'Courier New';" +
            "-fx-font-size:11px;");
        subtext.setOpacity(0);

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(16,
            portalWrap, calling, subtext);
        content.setAlignment(javafx.geometry.Pos.CENTER);

        overlay.getChildren().addAll(flash, content);

        if (scene.getRoot() instanceof StackPane sp) sp.getChildren().add(overlay);
        else { StackPane nr = new StackPane(scene.getRoot(), overlay); scene.setRoot(nr); }

        // ─ Fase 1: background gelap + portal muncul (0 – 800ms) ─
        FadeTransition bgFade = new FadeTransition(Duration.millis(400), overlay);
        bgFade.setToValue(1);
        bgFade.setOnFinished(e -> {
            overlay.setStyle("-fx-background-color:rgba(4,1,10,0.96);");
        });
        bgFade.play();

        FadeTransition portalFade = new FadeTransition(Duration.millis(600), portalWrap);
        portalFade.setToValue(1);
        ScaleTransition portalScale = new ScaleTransition(Duration.millis(600), portalWrap);
        portalScale.setFromX(0.5); portalScale.setToX(1.0);
        portalScale.setFromY(0.5); portalScale.setToY(1.0);
        new ParallelTransition(portalFade, portalScale).play();

        PauseTransition textDelay = new PauseTransition(Duration.millis(400));
        textDelay.setOnFinished(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(400), calling);
            ft.setToValue(1); ft.play();
            FadeTransition fs = new FadeTransition(Duration.millis(400), subtext);
            fs.setToValue(1); fs.play();
            // Blink teks
            FadeTransition blink = new FadeTransition(Duration.millis(450), calling);
            blink.setFromValue(0.5); blink.setToValue(1.0);
            blink.setCycleCount(Animation.INDEFINITE); blink.setAutoReverse(true); blink.play();
        });
        textDelay.play();

        // ─ Fase 2: portal makin besar + flash (800 – 2200ms) ─
        PauseTransition phase2 = new PauseTransition(Duration.millis(900));
        phase2.setOnFinished(e -> {
            // Perbesar portal dramatically
            ScaleTransition sc2 = new ScaleTransition(Duration.millis(800), portalWrap);
            sc2.setToX(1.6); sc2.setToY(1.6); sc2.play();

            // Flash purple di 1.6s
            PauseTransition flashDelay = new PauseTransition(Duration.millis(800));
            flashDelay.setOnFinished(ev -> {
                FadeTransition ft1 = new FadeTransition(Duration.millis(150), flash);
                ft1.setToValue(0.45);
                ft1.setOnFinished(ev2 -> {
                    FadeTransition ft2 = new FadeTransition(Duration.millis(350), flash);
                    ft2.setToValue(0); ft2.play();
                });
                ft1.play();
            });
            flashDelay.play();
        });
        phase2.play();

        // ─ Fase 3: fade out dan reveal (2200ms) ─
        PauseTransition phase3 = new PauseTransition(Duration.millis(2200));
        phase3.setOnFinished(e -> {
            FadeTransition fo = new FadeTransition(Duration.millis(350), overlay);
            fo.setToValue(0);
            fo.setOnFinished(ev -> {
                if (scene.getRoot() instanceof StackPane sp && sp.getChildren().contains(overlay))
                    sp.getChildren().remove(overlay);
                // Lakukan pull
                GachaSystem.PullResult result = isTen ? engine.pullTen() : engine.pullSingle();
                if (!result.success) { showFailAlert(result.failReason); return; }
                goldLabel.setText("⚙  " + engine.getPlayer().getGold() + " Gold");
                ticketLabel.setText("⬡  " + engine.getGachaTickets() + " Tiket");
                updatePityLabel();
                List<Artifact> sorted = new ArrayList<>(result.artifacts);
                sorted.sort((a,b) -> Integer.compare(b.getRarity().ordinal(), a.getRarity().ordinal()));
                showSlideReveal(sorted);
            });
            fo.play();
        });
        phase3.play();
    }

    private StackPane buildAnimPortalFull() {
        StackPane stack = new StackPane();
        stack.setPrefSize(200,200); stack.setMaxSize(200,200);

        // Rings — 4 cincin yang berputar berlawanan arah
        javafx.scene.shape.Circle bg = new javafx.scene.shape.Circle(96, Color.web("#120820"));
        bg.setEffect(new DropShadow(18, Color.web("#AA33FF88")));

        javafx.scene.shape.Circle r4 = new javafx.scene.shape.Circle(95, Color.TRANSPARENT);
        r4.setStroke(Color.web("#5A1A8A",0.20)); r4.setStrokeWidth(1);
        javafx.scene.shape.Circle r3 = new javafx.scene.shape.Circle(80, Color.TRANSPARENT);
        r3.setStroke(Color.web("#8833CC",0.40)); r3.setStrokeWidth(1.5);
        javafx.scene.shape.Circle r2 = new javafx.scene.shape.Circle(62, Color.TRANSPARENT);
        r2.setStroke(Color.web("#CC66FF",0.70)); r2.setStrokeWidth(2);
        javafx.scene.shape.Circle r1 = new javafx.scene.shape.Circle(45, Color.web("#0A0518"));
        r1.setStroke(Color.web("#EE99FF",0.90)); r1.setStrokeWidth(2.5);
        r1.setEffect(new Glow(0.5));

        // Center icon
        Label icon = new Label("⬡");
        icon.setStyle("-fx-text-fill:#EE99FF;-fx-font-size:48px;");
        icon.setEffect(new Glow(0.30));

        stack.getChildren().addAll(bg, r4, r3, r2, r1, icon);

        // Rotations berbeda kecepatan
        rot(r4, 1400, true);
        rot(r3, 900, false);
        rot(r2, 500, true);
        rot(r1, 300, false);

        // Pulse icon
        ScaleTransition sc = new ScaleTransition(Duration.millis(250), icon);
        sc.setFromX(0.85); sc.setToX(1.15); sc.setFromY(0.85); sc.setToY(1.15);
        sc.setCycleCount(Animation.INDEFINITE); sc.setAutoReverse(true); sc.play();

        return stack;
    }

    private void rot(javafx.scene.Node node, int ms, boolean cw) {
        RotateTransition rt = new RotateTransition(Duration.millis(ms), node);
        rt.setByAngle(cw ? 360 : -360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.play();
    }

    /**
     * Full-screen overlay — auto-slide tiap kartu.
     * Klik = skip ke berikutnya. Setelah semua = tampilkan grid summary.
     */
    private void showSlideReveal(List<Artifact> artifacts) {
        // Ambil root scene
        javafx.scene.Scene scene = router.getStage().getScene();
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color:rgba(4,1,10,0.96);");
        overlay.setPrefSize(arclightcity.ui.ArclightApp.SCREEN_WIDTH,
                            arclightcity.ui.ArclightApp.SCREEN_HEIGHT);

        // Counter label
        Label counter = new Label("1 / " + artifacts.size());
        counter.setStyle("-fx-text-fill:rgba(255,255,255,0.30);-fx-font-family:'Courier New';-fx-font-size:13px;");
        StackPane.setAlignment(counter, Pos.TOP_RIGHT);
        counter.setPadding(new Insets(16));

        // Skip hint
        Label hint = new Label("[ Klik atau tekan SPASI untuk lanjut ]");
        hint.setStyle("-fx-text-fill:rgba(255,255,255,0.22);-fx-font-family:'Courier New';-fx-font-size:10px;");
        StackPane.setAlignment(hint, Pos.BOTTOM_CENTER);
        hint.setPadding(new Insets(0,0,20,0));

        // Card container
        StackPane cardHolder = new StackPane();

        overlay.getChildren().addAll(cardHolder, counter, hint);

        // Tambahkan overlay ke scene root
        if (scene.getRoot() instanceof StackPane sp) {
            sp.getChildren().add(overlay);
        } else {
            StackPane newRoot = new StackPane(scene.getRoot(), overlay);
            scene.setRoot(newRoot);
        }

        // State
        int[] idx = {0};
        Timeline autoAdvance = new Timeline();

        Runnable showNext = new Runnable() {
            @Override public void run() {
                if (idx[0] >= artifacts.size()) {
                    // Semua selesai → tutup overlay + tampilkan summary
                    closeOverlay(overlay, scene);
                    showSummaryGrid(artifacts);
                    return;
                }
                Artifact art = artifacts.get(idx[0]);
                counter.setText((idx[0]+1) + " / " + artifacts.size());
                VBox card = buildFullCard(art);
                card.setOpacity(0);
                card.setScaleX(0.75); card.setScaleY(0.75);
                cardHolder.getChildren().setAll(card);

                FadeTransition ft = new FadeTransition(Duration.millis(350), card);
                ft.setToValue(1);
                ScaleTransition st = new ScaleTransition(Duration.millis(350), card);
                st.setToX(1); st.setToY(1);
                ParallelTransition reveal = new ParallelTransition(ft, st);
                reveal.play();
                idx[0]++;
            }
        };

        // Auto-advance timer
        autoAdvance.getKeyFrames().add(new KeyFrame(Duration.millis(1800), e -> showNext.run()));
        autoAdvance.setCycleCount(artifacts.size()+1);

        // Click / space skip
        overlay.setOnMouseClicked(e -> { autoAdvance.stop(); showNext.run(); autoAdvance.play(); });
        overlay.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.SPACE ||
                e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                autoAdvance.stop(); showNext.run(); autoAdvance.play();
            }
        });
        overlay.setFocusTraversable(true);
        overlay.requestFocus();

        showNext.run();
        autoAdvance.play();
    }

    private void closeOverlay(StackPane overlay, javafx.scene.Scene scene) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), overlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            if (scene.getRoot() instanceof StackPane sp && sp.getChildren().contains(overlay)) {
                sp.getChildren().remove(overlay);
            }
        });
        ft.play();
    }

    /** Kartu full-screen tunggal saat slide reveal */
    private VBox buildFullCard(Artifact art) {
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);

        // Rarity badge glow
        Label rarBadge = new Label(art.getRarity().displayName.toUpperCase());
        rarBadge.setStyle("-fx-text-fill:" + art.getBorderColor() + ";-fx-font-family:'Courier New';" +
            "-fx-font-size:13px;-fx-font-weight:bold;");
        if (art.hasGlowEffect()) rarBadge.setEffect(new Glow(0.5));

        // Big artifact icon (200x200)
        javafx.scene.image.Image icon = AssetManager.artifactIcon(art.getArtifactType());
        if (icon != null) {
            ImageView iv = new ImageView(icon);
            iv.setFitWidth(200); iv.setFitHeight(200); iv.setPreserveRatio(true);
            StackPane iconBox = new StackPane(iv);
            iconBox.setPrefSize(220,220);
            iconBox.setStyle("-fx-background-color:#0C0514;-fx-border-color:" + art.getBorderColor() +
                ";-fx-border-width:2;-fx-border-radius:8;-fx-background-radius:8;");
            if (art.hasGlowEffect())
                iconBox.setEffect(new DropShadow(40, Color.web(art.getBorderColor()+"99")));
            card.getChildren().add(iconBox);
        }

        // Name
        Label name = new Label(art.getArtifactType().displayName);
        name.setStyle("-fx-text-fill:" + art.getBorderColor() + ";-fx-font-family:'Courier New';" +
            "-fx-font-size:20px;-fx-font-weight:bold;");
        if (art.hasGlowEffect()) name.setEffect(new Glow(0.3));

        // Role + summary
        Label desc = new Label(art.getArtifactType().role.name() + " · " + art.getDisplaySummary());
        desc.setStyle("-fx-text-fill:rgba(240,230,255,0.70);-fx-font-family:'Courier New';-fx-font-size:12px;");
        desc.setWrapText(true); desc.setMaxWidth(380); desc.setAlignment(Pos.CENTER);

        // CD
        Label cd = new Label("CD: " + art.getScaledCooldown() + " giliran");
        cd.setStyle("-fx-text-fill:rgba(255,255,255,0.35);-fx-font-family:'Courier New';-fx-font-size:10px;");

        card.getChildren().addAll(rarBadge, name, desc, cd);
        return card;
    }

    /** Grid summary setelah semua kartu di-reveal */
    private void showSummaryGrid(List<Artifact> artifacts) {
        javafx.scene.Scene scene = router.getStage().getScene();
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color:rgba(4,1,10,0.95);");
        overlay.setPrefSize(arclightcity.ui.ArclightApp.SCREEN_WIDTH,
                            arclightcity.ui.ArclightApp.SCREEN_HEIGHT);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));

        Label title = new Label("HASIL PANGGILAN");
        title.setStyle("-fx-text-fill:#CC88FF;-fx-font-family:'Courier New';-fx-font-size:16px;-fx-font-weight:bold;");

        FlowPane grid = new FlowPane(12,12);
        grid.setAlignment(Pos.CENTER);
        grid.setMaxWidth(800);
        for (Artifact art : artifacts) grid.getChildren().add(buildSummaryCard(art));

        Button close = new Button("✕  TUTUP");
        close.setStyle("-fx-background-color:transparent;-fx-border-color:#5A3A7A;-fx-border-width:1;" +
            "-fx-text-fill:#AA88CC;-fx-font-family:'Courier New';-fx-font-size:12px;-fx-padding:8 24;-fx-cursor:hand;");
        close.setOnAction(e -> closeOverlay(overlay, scene));

        content.getChildren().addAll(title, grid, close);
        overlay.getChildren().add(content);

        if (scene.getRoot() instanceof StackPane sp) sp.getChildren().add(overlay);
        else { StackPane nr = new StackPane(scene.getRoot(), overlay); scene.setRoot(nr); }

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), overlay);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private VBox buildSummaryCard(Artifact art) {
        VBox card = new VBox(5); card.setAlignment(Pos.CENTER);
        card.setPrefWidth(110); card.setPadding(new Insets(8,6,8,6));
        card.setStyle("-fx-background-color:#0E0516;-fx-border-color:" + art.getBorderColor() +
            ";-fx-border-width:1.5;-fx-background-radius:6;-fx-border-radius:6;");
        if (art.hasGlowEffect())
            card.setEffect(new DropShadow(14, Color.web(art.getBorderColor()+"88")));
        javafx.scene.image.Image icon = AssetManager.artifactIcon(art.getArtifactType());
        if (icon != null) {
            ImageView iv = new ImageView(icon); iv.setFitWidth(64); iv.setFitHeight(64); iv.setPreserveRatio(true);
            card.getChildren().add(iv);
        }
        Label n = new Label(art.getArtifactType().displayName);
        n.setStyle("-fx-text-fill:" + art.getBorderColor() + ";-fx-font-family:'Courier New';" +
            "-fx-font-size:9px;-fx-font-weight:bold;");
        n.setWrapText(true); n.setMaxWidth(105); n.setAlignment(Pos.CENTER);
        Label r = new Label(art.getRarity().displayName.toUpperCase());
        r.setStyle("-fx-text-fill:rgba(255,255,255,0.38);-fx-font-family:'Courier New';-fx-font-size:7px;");
        Label b = new Label("✓ BAG");
        b.setStyle("-fx-text-fill:rgba(80,200,80,0.65);-fx-font-family:'Courier New';-fx-font-size:7px;");
        card.getChildren().addAll(n,r,b);
        return card;
    }

    private void showFailAlert(String reason) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.WARNING);
        a.setTitle("Altar Artefak"); a.setHeaderText(null);
        a.setContentText("✗  " + reason);
        a.initOwner(router.getStage());
        a.showAndWait();
    }

    private void updatePityLabel() {
        int left = engine.getGachaSystem().getPullsToGuarantee();
        pityLabel.setText("Pity: " + left + " pull lagi → garansi LEGENDARY+");
    }
}
