package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.item.*;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.util.*;

/**
 * GachaView — layar gacha artefak.
 * Akses dari hub. Menampilkan:
 *  - Info pity counter
 *  - Tombol 1x dan 10x pull
 *  - Hasil pull dengan animasi
 *  - Artefak yang diperoleh (bisa langsung equip)
 */
public class GachaView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private final VBox        resultsBox = new VBox(8);

    public GachaView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    public Parent build() {
        BorderPane root = UIFactory.screenRootBorder();
        root.setStyle("-fx-background-color: #060308;");

        // ── Header ─────────────────────────────────────────────
        HBox header = buildHeader();
        root.setTop(header);

        // ── Center — info + tombol pull ─────────────────────────
        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(30, 40, 20, 40));

        // Portal visual
        StackPane portal = buildPortalVisual();

        // Info gacha
        VBox infoBox = buildInfoBox();

        // Tombol pull
        HBox pullBtns = buildPullButtons();

        // Results area (muncul setelah pull)
        resultsBox.setAlignment(Pos.CENTER);
        resultsBox.setPadding(new Insets(10, 20, 10, 20));

        center.getChildren().addAll(portal, infoBox, pullBtns, resultsBox);

        ScrollPane scroll = new ScrollPane(center);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.setCenter(scroll);

        UIFactory.fadeIn(root, 500);
        return root;
    }

    // ── Header ──────────────────────────────────────────────────
    private HBox buildHeader() {
        HBox h = new HBox();
        h.setPadding(new Insets(14, 20, 10, 20));
        h.setAlignment(Pos.CENTER_LEFT);
        h.setStyle("-fx-background-color: #0A0510; -fx-border-color: #3A1A5A;" +
                   "-fx-border-width: 0 0 1 0;");

        Button back = UIFactory.btnPrimary("← KEMBALI");
        back.setOnAction(e -> router.showHub());

        Label title = new Label("⬡  ALTAR ARTEFAK");
        title.setStyle("-fx-text-fill: #CC88FF; -fx-font-family:'Courier New';" +
                       "-fx-font-size:16px; -fx-font-weight:bold;");

        Label goldLbl = new Label("⚙  " + engine.getPlayer().getGold());
        goldLbl.setStyle("-fx-text-fill:#FFB830; -fx-font-family:'Courier New'; -fx-font-size:12px;");

        Label ticketLbl = new Label("⬡  " + engine.getGachaTickets() + " TIKET");
        ticketLbl.setStyle("-fx-text-fill:#CC88FF; -fx-font-family:'Courier New'; -fx-font-size:12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        h.getChildren().addAll(back, spacer, title, new Region() {{ setPrefWidth(40); }},
                               goldLbl, new Region() {{ setPrefWidth(16); }}, ticketLbl);
        return h;
    }

    // ── Portal visual ───────────────────────────────────────────
    private StackPane buildPortalVisual() {
        StackPane stack = new StackPane();
        stack.setPrefSize(200, 200);
        stack.setMaxSize(200, 200);

        // Rotating rings
        Circle ring1 = new Circle(90, Color.TRANSPARENT);
        ring1.setStroke(Color.web("#CC88FF", 0.25)); ring1.setStrokeWidth(2);
        Circle ring2 = new Circle(70, Color.TRANSPARENT);
        ring2.setStroke(Color.web("#9944CC", 0.40)); ring2.setStrokeWidth(1.5);
        Circle core  = new Circle(45, Color.web("#1A0830"));
        core.setStroke(Color.web("#CC88FF", 0.60)); core.setStrokeWidth(2);
        core.setEffect(new Glow(0.6));

        Label icon = new Label("⬡");
        icon.setStyle("-fx-text-fill:#CC88FF; -fx-font-size:48px;");
        icon.setEffect(new Glow(0.5));

        stack.getChildren().addAll(ring1, ring2, core, icon);

        // Rotate animation
        RotateTransition rt1 = new RotateTransition(Duration.seconds(8), ring1);
        rt1.setByAngle(360); rt1.setCycleCount(Animation.INDEFINITE); rt1.play();
        RotateTransition rt2 = new RotateTransition(Duration.seconds(5), ring2);
        rt2.setByAngle(-360); rt2.setCycleCount(Animation.INDEFINITE); rt2.play();

        FadeTransition pulse = new FadeTransition(Duration.millis(1200), icon);
        pulse.setFromValue(0.6); pulse.setToValue(1.0);
        pulse.setCycleCount(Animation.INDEFINITE); pulse.setAutoReverse(true); pulse.play();

        return stack;
    }

    // ── Info box ────────────────────────────────────────────────
    private VBox buildInfoBox() {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);

        int pity = engine.getGachaSystem().getPullsToGuarantee();
        Label pitylbl = new Label("Pity: " + pity + " pull lagi garansi LEGENDARY");
        pitylbl.setStyle("-fx-text-fill:rgba(204,136,255,0.65); -fx-font-family:'Courier New';" +
                         "-fx-font-size:11px;");

        Label rates = new Label("MYTHIC 1% · LEGENDARY 4% · EPIC 12% · RARE 18%");
        rates.setStyle("-fx-text-fill:rgba(255,255,255,0.35); -fx-font-family:'Courier New';" +
                       "-fx-font-size:10px;");

        Label tip = new Label("Tiket Gacha bisa didapat dari loot boss dan event dungeon");
        tip.setStyle("-fx-text-fill:rgba(255,255,255,0.25); -fx-font-family:'Courier New';" +
                     "-fx-font-size:9px;");

        box.getChildren().addAll(pitylbl, rates, tip);
        return box;
    }

    // ── Pull buttons ────────────────────────────────────────────
    private HBox buildPullButtons() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER);

        Button single = pullBtn("1x PANGGIL",
            "800 Gold  /  1 Tiket", "#9944CC", "#CC88FF");
        single.setOnAction(e -> doPull(false));

        Button ten = pullBtn("10x PANGGIL",
            "7200 Gold  /  9 Tiket", "#660099", "#AA66FF");
        ten.setOnAction(e -> doPull(true));

        row.getChildren().addAll(single, ten);
        return row;
    }

    private Button pullBtn(String title, String sub, String bg, String border) {
        VBox content = new VBox(2);
        content.setAlignment(Pos.CENTER);
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#F0E0FF; -fx-font-family:'Courier New';" +
                   "-fx-font-size:13px; -fx-font-weight:bold;");
        Label s = new Label(sub);
        s.setStyle("-fx-text-fill:rgba(255,255,255,0.50); -fx-font-family:'Courier New';" +
                   "-fx-font-size:10px;");
        content.getChildren().addAll(t, s);

        Button b = new Button();
        b.setGraphic(content);
        b.setPrefWidth(200); b.setPrefHeight(64);
        String base = "-fx-background-color:" + bg + "44; -fx-border-color:" + border + ";" +
                      "-fx-border-width:1; -fx-cursor:hand; -fx-background-radius:4; -fx-border-radius:4;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> {
            b.setStyle(base + "-fx-background-color:" + bg + "88;");
            b.setEffect(new Glow(0.3));
        });
        b.setOnMouseExited(e -> { b.setStyle(base); b.setEffect(null); });
        return b;
    }

    // ── Pull logic ──────────────────────────────────────────────
    private void doPull(boolean isTen) {
        GachaSystem.PullResult result = isTen ? engine.pullTen() : engine.pullSingle();

        resultsBox.getChildren().clear();

        if (!result.success) {
            Label fail = new Label("✗  " + result.failReason);
            fail.setStyle("-fx-text-fill:#CC4444; -fx-font-family:'Courier New';" +
                          "-fx-font-size:12px; -fx-padding:8;");
            resultsBox.getChildren().add(fail);
            return;
        }

        // Tambahkan artefak ke inventory bag (PERSISTENT)
        int added = 0;
        for (Artifact art : result.artifacts) {
            if (engine.getInventory() != null && engine.getInventory().addItem(art)) added++;
        }
        router.showToast("⬡  Gacha!", added + " artefak masuk ke Perbendaharaan!", "#CC88FF");

        // Show results
        Label header = new Label("— Hasil Pull —");
        header.setStyle("-fx-text-fill:rgba(204,136,255,0.60); -fx-font-family:'Courier New';" +
                        "-fx-font-size:11px;");
        resultsBox.getChildren().add(header);

        // Sort: terbaik dulu
        List<Artifact> sorted = new ArrayList<>(result.artifacts);
        sorted.sort((a, b) -> Integer.compare(b.getRarity().ordinal(), a.getRarity().ordinal()));

        for (int i = 0; i < sorted.size(); i++) {
            final Artifact art = sorted.get(i);
            final int      idx = i;
            HBox card = buildResultCard(art);
            card.setOpacity(0);
            resultsBox.getChildren().add(card);

            // Staggered fade-in
            PauseTransition delay = new PauseTransition(Duration.millis(80L * idx));
            delay.setOnFinished(e -> {
                FadeTransition ft = new FadeTransition(Duration.millis(300), card);
                ft.setToValue(1.0); ft.play();
                if (art.hasGlowEffect()) {
                    ScaleTransition sc = new ScaleTransition(Duration.millis(300), card);
                    sc.setFromX(0.85); sc.setToX(1.0);
                    sc.setFromY(0.85); sc.setToY(1.0);
                    sc.play();
                }
            });
            delay.play();
        }
    }

    private HBox buildResultCard(Artifact art) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 16, 10, 12));
        card.setMaxWidth(500);
        card.setStyle("-fx-background-color: #0E0518; -fx-border-color:" +
                      art.getBorderColor() + "; -fx-border-width: 1;" +
                      "-fx-background-radius:4; -fx-border-radius:4;");

        if (art.hasGlowEffect()) {
            card.setEffect(new Glow(0.2));
        }

        // Rarity icon
        Label rar = new Label(rarIcon(art.getRarity()));
        rar.setStyle("-fx-text-fill:" + art.getBorderColor() + ";" +
                     "-fx-font-size:18px;");

        // Info
        VBox info = new VBox(2);
        Label name = new Label(art.getArtifactType().displayName);
        name.setStyle("-fx-text-fill:" + art.getBorderColor() + ";" +
                      "-fx-font-family:'Courier New'; -fx-font-size:13px; -fx-font-weight:bold;");

        String effectDesc = buildEffectDesc(art);
        Label  eff  = new Label(effectDesc);
        eff.setStyle("-fx-text-fill:rgba(240,235,225,0.65); -fx-font-family:'Courier New';" +
                     "-fx-font-size:10px;");

        Label  rarLbl = new Label("[" + art.getRarity().displayName + "] · CD " +
                                  art.getScaledCooldown() + " turn");
        rarLbl.setStyle("-fx-text-fill:rgba(255,255,255,0.35); -fx-font-family:'Courier New';" +
                        "-fx-font-size:9px;");

        info.getChildren().addAll(name, eff, rarLbl);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label inBag = new Label("✓ DI BAG");
        inBag.setStyle("-fx-text-fill:rgba(100,200,100,0.60); -fx-font-family:'Courier New';" +
                       "-fx-font-size:9px; -fx-padding:4 10;");
        card.getChildren().addAll(rar, info, inBag);
        return card;
    }

    private String buildEffectDesc(Artifact art) {
        ArtifactType type = art.getArtifactType();
        double val = art.getScaledValue();
        int    dur = art.getScaledDuration();
        return switch (type.mode) {
            case HEAL_SELF      -> String.format("Pulihkan %.0f%% HP", val * 100);
            case STAT_SELF      -> type.statType != null
                ? String.format("+%.0f%% %s selama %d turn", val * 100,
                    type.statType.displayName, dur) : type.description;
            case STATUS_SELF    -> type.statusType != null
                ? type.statusType.name() + " +" + dur + " turn" : type.description;
            case STATUS_ENEMY   -> type.statusType != null
                ? "Apply " + type.statusType.name() + " ke musuh (" + dur + " turn)"
                : type.description;
            case STATUS_PARTY   -> "Party " + (type.statusType != null
                ? type.statusType.name() : "") + " +" + dur + " turn";
            case CLEANSE_PARTY  -> "Bersihkan semua debuff party";
        };
    }

    private String rarIcon(Item.Rarity r) {
        return switch (r) {
            case COMMON    -> "◌";
            case UNCOMMON  -> "◎";
            case RARE      -> "⬡";
            case EPIC      -> "✦";
            case LEGENDARY -> "★";
            case MYTHIC    -> "⚝";
        };
    }
}
