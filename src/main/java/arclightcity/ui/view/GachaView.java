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

public class GachaView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private final FlowPane    resultGrid = new FlowPane(10, 10);
    private       Label       pityLabel;
    private       Label       goldLabel;
    private       Label       ticketLabel;

    public GachaView(GameEngine engine, SceneRouter router) {
        this.engine = engine; this.router = router;
    }

    public Parent build() {
        BorderPane root = UIFactory.screenRootBorder();
        root.setStyle("-fx-background-color: #060308;");
        root.setTop(buildHeader());
        root.setCenter(buildCenter());
        UIFactory.fadeIn(root, 500);
        return root;
    }

    private HBox buildHeader() {
        HBox h = new HBox(16);
        h.setPadding(new Insets(12, 20, 10, 20));
        h.setAlignment(Pos.CENTER_LEFT);
        h.setStyle("-fx-background-color:#09050E; -fx-border-color:#3A1A5A;" +
                   "-fx-border-width:0 0 1 0;");
        Button back = new Button("← KEMBALI");
        back.setStyle("-fx-background-color:transparent; -fx-border-color:#5A3A7A;" +
            "-fx-border-width:1; -fx-text-fill:#AA88CC; -fx-font-family:'Courier New';" +
            "-fx-font-size:11px; -fx-padding:6 14; -fx-cursor:hand;");
        back.setOnAction(e -> router.showHub());
        Label title = new Label("⬡  ALTAR ARTEFAK");
        title.setStyle("-fx-text-fill:#CC88FF; -fx-font-family:'Courier New';" +
                       "-fx-font-size:15px; -fx-font-weight:bold;");
        title.setEffect(new Glow(0.25));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        goldLabel   = new Label("⚙  " + engine.getPlayer().getGold() + " Gold");
        goldLabel.setStyle("-fx-text-fill:#FFB830; -fx-font-family:'Courier New'; -fx-font-size:12px;");
        ticketLabel = new Label("⬡  " + engine.getGachaTickets() + " Tiket");
        ticketLabel.setStyle("-fx-text-fill:#AA88CC; -fx-font-family:'Courier New'; -fx-font-size:12px;");
        h.getChildren().addAll(back, sp, title, goldLabel, ticketLabel);
        return h;
    }

    private ScrollPane buildCenter() {
        VBox content = new VBox(22);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(24, 40, 24, 40));
        content.getChildren().add(buildPortal());
        pityLabel = new Label(); updatePityLabel();
        pityLabel.setStyle("-fx-text-fill:rgba(204,136,255,0.72); -fx-font-family:'Courier New'; -fx-font-size:11px;");
        Label rates = new Label("MYTHIC 1%  ·  LEGENDARY 4%  ·  EPIC 12%  ·  RARE 18%  ·  UNCOMMON 25%  ·  COMMON 40%");
        rates.setStyle("-fx-text-fill:rgba(255,255,255,0.28); -fx-font-family:'Courier New'; -fx-font-size:9px;");
        Label tip = new Label("Tiket Gacha: drop dari boss (18%) dan event dungeon");
        tip.setStyle("-fx-text-fill:rgba(255,255,255,0.18); -fx-font-family:'Courier New'; -fx-font-size:9px;");
        VBox info = new VBox(5, pityLabel, rates, tip); info.setAlignment(Pos.CENTER);
        Separator sep = new Separator(); sep.setMaxWidth(600);
        sep.setStyle("-fx-background-color:#2A1A3A;");
        resultGrid.setAlignment(Pos.CENTER); resultGrid.setMaxWidth(900);
        content.getChildren().addAll(info, buildPullButtons(), sep, resultGrid);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent; -fx-background-color:transparent;");
        return scroll;
    }

    private StackPane buildPortal() {
        StackPane stack = new StackPane();
        stack.setPrefSize(200, 200); stack.setMaxSize(200, 200);
        Circle bg = new Circle(96, Color.web("#1A0830"));
        bg.setEffect(new DropShadow(35, Color.web("#8833CC44")));
        Circle r3 = new Circle(94, Color.TRANSPARENT);
        r3.setStroke(Color.web("#5A1A8A", 0.15)); r3.setStrokeWidth(1);
        Circle r2 = new Circle(76, Color.TRANSPARENT);
        r2.setStroke(Color.web("#8A33AA", 0.30)); r2.setStrokeWidth(1.5);
        Circle r1 = new Circle(58, Color.web("#100818"));
        r1.setStroke(Color.web("#CC88FF", 0.60)); r1.setStrokeWidth(2);
        r1.setEffect(new Glow(0.25));
        Label icon = new Label("⬡");
        icon.setStyle("-fx-text-fill:#CC88FF; -fx-font-size:48px;");
        icon.setEffect(new Glow(0.5));
        stack.getChildren().addAll(bg, r3, r2, r1, icon);
        RotateTransition rt1 = new RotateTransition(Duration.seconds(11), r3);
        rt1.setByAngle(360); rt1.setCycleCount(Animation.INDEFINITE); rt1.play();
        RotateTransition rt2 = new RotateTransition(Duration.seconds(7), r2);
        rt2.setByAngle(-360); rt2.setCycleCount(Animation.INDEFINITE); rt2.play();
        FadeTransition pulse = new FadeTransition(Duration.millis(1400), icon);
        pulse.setFromValue(0.65); pulse.setToValue(1.0);
        pulse.setCycleCount(Animation.INDEFINITE); pulse.setAutoReverse(true); pulse.play();
        return stack;
    }

    private HBox buildPullButtons() {
        HBox row = new HBox(20); row.setAlignment(Pos.CENTER);
        VBox s1 = pullCard("1×  PANGGIL", "800 Gold  /  1 Tiket", false);
        s1.setOnMouseClicked(e -> doPull(false));
        VBox s10 = pullCard("10×  PANGGIL", "7200 Gold  /  9 Tiket", true);
        s10.setOnMouseClicked(e -> doPull(true));
        row.getChildren().addAll(s1, s10);
        return row;
    }

    private VBox pullCard(String t, String sub, boolean big) {
        VBox card = new VBox(7); card.setAlignment(Pos.CENTER);
        card.setPrefWidth(big ? 240 : 220); card.setPrefHeight(80);
        card.setPadding(new Insets(14, 22, 14, 22));
        card.setCursor(javafx.scene.Cursor.HAND);
        String base = "-fx-background-color:#330066" + (big ? "66" : "44") +
            "; -fx-border-color:#9944DD; -fx-border-width:1.5;" +
            "-fx-background-radius:6; -fx-border-radius:6;";
        card.setStyle(base);
        Label tl = new Label(t);
        tl.setStyle("-fx-text-fill:#F0E0FF; -fx-font-family:'Courier New';" +
            "-fx-font-size:" + (big ? "14" : "13") + "px; -fx-font-weight:bold;");
        Label sl = new Label(sub);
        sl.setStyle("-fx-text-fill:rgba(200,170,230,0.55); -fx-font-family:'Courier New'; -fx-font-size:10px;");
        card.getChildren().addAll(tl, sl);
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color:#5500AA88; -fx-border-color:#CC88FF;" +
                "-fx-border-width:1.5; -fx-background-radius:6; -fx-border-radius:6;");
            card.setEffect(new Glow(0.30));
        });
        card.setOnMouseExited(e -> { card.setStyle(base); card.setEffect(null); });
        return card;
    }

    private void doPull(boolean isTen) {
        GachaSystem.PullResult result = isTen ? engine.pullTen() : engine.pullSingle();
        resultGrid.getChildren().clear();
        if (!result.success) {
            Label fail = new Label("✗  " + result.failReason);
            fail.setStyle("-fx-text-fill:#CC4444; -fx-font-family:'Courier New'; -fx-font-size:12px; -fx-padding:12;");
            resultGrid.getChildren().add(fail); return;
        }
        goldLabel.setText("⚙  " + engine.getPlayer().getGold() + " Gold");
        ticketLabel.setText("⬡  " + engine.getGachaTickets() + " Tiket");
        updatePityLabel();
        List<Artifact> sorted = new ArrayList<>(result.artifacts);
        sorted.sort((a, b) -> Integer.compare(b.getRarity().ordinal(), a.getRarity().ordinal()));
        for (int i = 0; i < sorted.size(); i++) {
            Artifact art = sorted.get(i);
            VBox card = buildResultCard(art);
            card.setOpacity(0);
            resultGrid.getChildren().add(card);
            final long ms = 90L * i;
            PauseTransition d = new PauseTransition(Duration.millis(ms));
            d.setOnFinished(ev -> {
                FadeTransition ft = new FadeTransition(Duration.millis(240), card);
                ft.setToValue(1); ft.play();
                if (art.hasGlowEffect()) {
                    ScaleTransition sc = new ScaleTransition(Duration.millis(240), card);
                    sc.setFromX(0.82); sc.setToX(1.0); sc.setFromY(0.82); sc.setToY(1.0); sc.play();
                }
            });
            d.play();
        }
    }

    private VBox buildResultCard(Artifact art) {
        VBox card = new VBox(5); card.setAlignment(Pos.CENTER);
        card.setPrefWidth(115); card.setPadding(new Insets(10, 8, 10, 8));
        card.setStyle("-fx-background-color:#0E0516; -fx-border-color:" + art.getBorderColor() + ";" +
                      "-fx-border-width:1.5; -fx-background-radius:6; -fx-border-radius:6;");
        if (art.hasGlowEffect())
            card.setEffect(new DropShadow(16, Color.web(art.getBorderColor() + "88")));
        javafx.scene.image.Image icon = AssetManager.artifactIcon(art.getArtifactType());
        if (icon != null) {
            ImageView iv = new ImageView(icon);
            iv.setFitWidth(70); iv.setFitHeight(70); iv.setPreserveRatio(true);
            card.getChildren().add(iv);
        } else {
            Label fb = new Label("⬡"); fb.setStyle("-fx-text-fill:" + art.getBorderColor() + "; -fx-font-size:24px;");
            card.getChildren().add(fb);
        }
        Label name = new Label(art.getArtifactType().displayName);
        name.setStyle("-fx-text-fill:" + art.getBorderColor() + "; -fx-font-family:'Courier New';" +
                      "-fx-font-size:9px; -fx-font-weight:bold;");
        name.setWrapText(true); name.setMaxWidth(108); name.setAlignment(Pos.CENTER);
        Label rar = new Label(art.getRarity().displayName.toUpperCase());
        rar.setStyle("-fx-text-fill:rgba(255,255,255,0.40); -fx-font-family:'Courier New'; -fx-font-size:7px;");
        Label bag = new Label("✓ DI BAG");
        bag.setStyle("-fx-text-fill:rgba(80,200,80,0.65); -fx-font-family:'Courier New'; -fx-font-size:7px;");
        card.getChildren().addAll(name, rar, bag);
        return card;
    }

    private void updatePityLabel() {
        int left = engine.getGachaSystem().getPullsToGuarantee();
        pityLabel.setText("Pity: " + left + " pull lagi → garansi LEGENDARY+");
    }
}
