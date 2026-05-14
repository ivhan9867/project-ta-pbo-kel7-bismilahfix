package arclightcity.ui.view;

import arclightcity.combat.*;
import arclightcity.engine.GameEngine;
import arclightcity.entity.base.Entity;
import arclightcity.entity.enemy.Boss;
import arclightcity.entity.mercenary.Mercenary;
import arclightcity.entity.player.Player;
import arclightcity.entity.stats.StatType;
import arclightcity.ui.ArclightApp;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.AssetManager;
import arclightcity.ui.util.UIFactory;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;
import java.util.List;

/**
 * CombatView v0.7.0 — Layout final (Bravely Second style)
 *
 * ┌──────────────────────────────────────────────────────┐
 * │ KABUR  |  PERTEMPURAN  Giliran X  ACTOR ▶  SPD      │ ← header slim
 * ├──────────────────────────────────────────────────────┤
 * │                                                      │
 * │  SCENE 940 × ~440px (BG + sprites + overlay)        │
 * │                                                      │
 * │  [E1] [E2] [E3]          [A3] [A2] [A1]            │
 * │   (kiri, bigger)          (kanan, medium)            │
 * │                                                      │
 * │     [ACTION OVERLAY muncul di tengah saat klik]     │
 * │                                                      │
 * ├──────────────────────────────────────────────────────┤
 * │ PARTY BAR horizontal — info only, no buttons        │
 * │ ┌────────┬────────┬────────┬────────────────────┐   │
 * │ │[port.] │[port.] │[port.] │                    │   │
 * │ │ ASUNA  │ GATOT  │  ...   │     (kosong)       │   │
 * │ │ READY  │DEFAULT │        │                    │   │
 * │ │ HP▓▓░  │ HP▓▓▓  │        │                    │   │
 * │ │ MP▓░   │ MP▓▓░  │        │                    │   │
 * │ └────────┴────────┴────────┴────────────────────┘   │
 * └──────────────────────────────────────────────────────┘
 *
 * Action flow:
 *   Klik ally sprite  → overlay aksi muncul
 *   Pilih SERANG/JURUS → musuh di-highlight (targeting mode)
 *   Klik musuh         → eksekusi aksi
 *   Klik musuh dulu    → tampil daftar ally untuk memilih who acts
 */
public class CombatView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private CombatManager     cm;

    // Scene
    private javafx.scene.layout.Pane enemyPane;  // absolute positioning untuk enemy
    private HBox    enemyZone; // legacy, tidak dipakai
    private javafx.scene.layout.Pane allyPane; // formation absolut
    private VBox    actionOverlay;
    private Canvas  floatCanvas;
    private StackPane sceneStack;

    // Party bar (bottom, horizontal)
    private HBox    partyBar;
    // Entity float-x positions: diisi saat updateBattleScene
    private final java.util.Map<String,Double> entityFloatX = new java.util.HashMap<>();

    // Header
    private Label   turnLabel;
    private Label   actorLabel;

    // Loop
    private Timeline combatLoop;
    private Timeline floatLoop;
    private int      speedMs = 1200;

    // Action state
    private Entity   selectedAlly   = null;
    private String   pendingSkillId = null;
    private boolean  targetingMode  = false;  // tunggu klik enemy sebagai target

    // Sprite animation — pose state untuk setiap entity
    private final java.util.Map<String, javafx.scene.image.ImageView> spriteMap
        = new java.util.HashMap<>();
    private final java.util.Map<String, String> poseState
        = new java.util.HashMap<>();  // entityId → "idle"|"attack"|"hit"

    // Floating damage
    private record FT(String t, String c, double x, double y,
                      long t0, int dur, double rise) {
        FT(String t, String c, double x, double y, long t0) {
            this(t,c,x,y,t0,
                t.startsWith("\u26a1")||t.startsWith("\u2726")?1800:1400,
                t.startsWith("\u26a1")?85:60);
        }
    }
    private final List<FT> floats = new java.util.ArrayList<>();

    public CombatView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
        this.cm     = engine.getActiveCombat();
    }

    // ════════════════════════════════════════════════
    //  BUILD ROOT
    // ════════════════════════════════════════════════
    public Parent build() {
        BorderPane root = UIFactory.screenRootBorder();
        root.setStyle("-fx-background-color:#080402;");
        root.setTop(buildHeader());
        root.setCenter(buildScene());
        root.setBottom(buildPartyBar());

        if (cm != null) {
            cm.addEventListener(ev  -> Platform.runLater(() -> onEvent(ev)));
            cm.addResultListener(res -> Platform.runLater(() -> onEnd(res)));
        }
        refresh();
        startLoop();
        UIFactory.fadeIn(root, 250);
        return root;
    }

    // ════════════════════════════════════════════════
    //  HEADER — slim, minimal
    // ════════════════════════════════════════════════
    private HBox buildHeader() {
        HBox h = new HBox(12);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(5,12,5,12));
        h.setStyle("-fx-background-color:#0D0502;-fx-border-color:#CC330055;-fx-border-width:0 0 1 0;");

        Button kabur = hBtn("\u2190 KABUR","#CC3300","#CC330033");
        kabur.setOnAction(e -> doFlee());

        Label lbl = new Label("PERTEMPURAN");
        lbl.setStyle("-fx-text-fill:#CC3300;-fx-font-family:'Courier New';-fx-font-size:12px;-fx-font-weight:bold;");

        turnLabel = new Label("Giliran 1");
        turnLabel.setStyle("-fx-text-fill:#5A3A10;-fx-font-family:'Courier New';-fx-font-size:11px;");
        HBox.setHgrow(turnLabel, Priority.ALWAYS);

        actorLabel = new Label("ASUNA \u25ba");
        actorLabel.setStyle("-fx-text-fill:#FFB830;-fx-font-family:'Courier New';-fx-font-size:12px;-fx-font-weight:bold;");

        Label sLbl = new Label("  SPD:");
        sLbl.setStyle("-fx-text-fill:#3A2810;-fx-font-size:10px;");
        ToggleGroup tg = new ToggleGroup();
        h.getChildren().addAll(kabur, lbl, turnLabel, actorLabel, sLbl,
            spdBtn("1\u00d7",1200,tg,true), spdBtn("2\u00d7",500,tg,false), spdBtn("SKIP",50,tg,false));
        return h;
    }

    private ToggleButton spdBtn(String t, int ms, ToggleGroup g, boolean s) {
        ToggleButton b = new ToggleButton(t); b.setToggleGroup(g); b.setSelected(s);
        String st = "-fx-text-fill:#A09070;-fx-font-family:'Courier New';-fx-font-size:9px;-fx-padding:2 6;-fx-cursor:hand;";
        b.setStyle("-fx-background-color:"+(s?"#3A2810":"#1A1008")+";"+st);
        b.selectedProperty().addListener((o,ov,nv) -> {
            if(nv){speedMs=ms; startLoop();}
            b.setStyle("-fx-background-color:"+(nv?"#3A2810":"#1A1008")+";"+st);
        });
        return b;
    }

    // ════════════════════════════════════════════════
    //  SCENE — BG + enemy kiri + ally kanan + overlay
    // ════════════════════════════════════════════════
    private StackPane buildScene() {
        double W = ArclightApp.GAME_WIDTH;
        double H = ArclightApp.SCREEN_HEIGHT * 0.63;

        sceneStack = new StackPane();
        sceneStack.setPrefSize(W,H); sceneStack.setMaxSize(W,H);
        sceneStack.setStyle("-fx-background-color:#060402;");

        // Layer 0: BG
        try {
            int fl = engine.getDungeonManager().getCurrentFloorNumber();
            var bg = AssetManager.bgDungeon(fl);
            if (bg != null) {
                var iv = AssetManager.makeIVFill(bg,W,H);
                iv.setOpacity(0.82); iv.setMouseTransparent(true);
                sceneStack.getChildren().add(iv);
            }
        } catch(Exception ignored){}

        // Layer 1: gradient tepi gelap
        Region grad = new Region(); grad.setMouseTransparent(true);
        grad.setStyle("-fx-background-color:linear-gradient(to bottom," +
            "#06050399 0%,#06050333 15%,transparent 35%," +
            "transparent 70%,#06050355 88%,#060503BB 100%);");
        sceneStack.getChildren().add(grad);

        // Layer 2: ALLY kiri | spacer | ENEMY kanan (swap dari sebelumnya)
        HBox zones = new HBox(0);
        zones.setPrefSize(W,H); zones.setMaxSize(W,H);

        // ALLY zone — KIRI, Pane dengan absolute positioning (formation)
        double allyW = W * 0.30;
        allyPane = new javafx.scene.layout.Pane();
        allyPane.setPrefSize(allyW, H);
        allyPane.setMinWidth(allyW);
        allyPane.setMaxWidth(allyW);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // ENEMY zone — KANAN, Pane untuk absolute positioning
        double enemyW = W * 0.44;
        enemyPane = new javafx.scene.layout.Pane();
        enemyPane.setPrefSize(enemyW, H);
        enemyPane.setMinWidth(enemyW);
        enemyPane.setMaxWidth(enemyW);

        zones.getChildren().addAll(allyPane, spacer, enemyPane);
        sceneStack.getChildren().add(zones);

        // Layer 3: Action overlay (tengah scene)
        actionOverlay = new VBox(0);
        actionOverlay.setVisible(false);
        actionOverlay.setMaxWidth(240);
        actionOverlay.setStyle(
            "-fx-background-color:#0C0906F2;" +
            "-fx-border-color:#C8860A;-fx-border-width:0 0 0 3;" +
            "-fx-padding:0;" +
            "-fx-effect:dropshadow(gaussian,#000000,18,0.8,0,4);");
        StackPane.setAlignment(actionOverlay, Pos.CENTER);
        sceneStack.getChildren().add(actionOverlay);

        // Layer 4: Float canvas
        floatCanvas = new Canvas(W,H);
        floatCanvas.setMouseTransparent(true);
        sceneStack.getChildren().add(floatCanvas);

        // Klik area kosong scene = cancel
        sceneStack.setOnMouseClicked(e -> {
            if (actionOverlay.isVisible()) cancelAction();
        });

        return sceneStack;
    }

    // ════════════════════════════════════════════════
    //  ENEMY CARD — zona kiri, highlight saat targeting
    // ════════════════════════════════════════════════
    private VBox buildEnemyCard(Entity e, int totalEnemies) {
        boolean isBoss = e instanceof Boss;
        double sz = isBoss ? 130 : (totalEnemies<=1?110:totalEnemies<=2?95:totalEnemies<=4?78:62);
        boolean cur = cm.getCurrentActor()!=null && cm.getCurrentActor().getId().equals(e.getId());
        boolean alive = e.isAlive();

        VBox card = new VBox(3); card.setAlignment(Pos.BOTTOM_CENTER);

        // Targeting mode: musuh di-highlight saat player harus pilih target
        String cardStyle = "";
        if (targetingMode && alive) {
            cardStyle = "-fx-effect:dropshadow(gaussian,#FFAA00CC,22,0.7,0,0);" +
                        "-fx-cursor:hand;";
        } else if (cur) {
            cardStyle = "-fx-effect:dropshadow(gaussian,#FF442288,16,0.5,0,0);";
        }
        card.setStyle(cardStyle);

        // HP bar
        card.getChildren().addAll(buildHBar(sz, e.getHpPercent()), hpLabel(e));

        // Sprite
        String pose = poseState.getOrDefault(e.getId(), "idle");
        var sprite = isBoss
            ? AssetManager.spriteBossByFloor(((Boss)e).getFloorLevel(), pose)
            : AssetManager.spriteEnemy(e.getName(), pose);
        if (sprite==null && isBoss) sprite = AssetManager.spriteBoss("theresa", pose.equals("idle")?"idle":"hit");
        if (sprite==null) sprite = isBoss
            ? AssetManager.spriteBossByFloor(((Boss)e).getFloorLevel(), "idle")
            : AssetManager.spriteEnemy(e.getName(), "idle");

        StackPane sp = new StackPane(); sp.setMinSize(sz,sz); sp.setMaxSize(sz,sz);
        if (sprite!=null) {
            var iv = AssetManager.makeIV(sprite,sz,sz);
            iv.setOpacity(alive?1.0:0.18);
            spriteMap.put(e.getId(), iv);
            if (alive && !e.getActiveEffects().isEmpty()) addAura(sp,e);
            sp.getChildren().add(iv);
        } else {
            Label ph = new Label(e.getName().substring(0,1));
            ph.setStyle("-fx-text-fill:#CC3300;-fx-font-size:52px;-fx-font-weight:bold;");
            sp.getChildren().add(ph);
        }

        Label nm = new Label(e.getName());
        nm.setStyle("-fx-text-fill:"+(cur?"#FF8866":"#DDBB99")+
            ";-fx-font-family:'Courier New';-fx-font-size:10px;-fx-font-weight:bold;" +
            "-fx-effect:dropshadow(gaussian,#000,5,1,0,1);");

        card.getChildren().addAll(sp, nm);

        // Status effects
        HBox efx = new HBox(3); efx.setAlignment(Pos.CENTER);
        e.getActiveEffects().stream().limit(3).forEach(ef -> efx.getChildren().add(UIFactory.effectBadge(ef)));
        if (!efx.getChildren().isEmpty()) card.getChildren().add(efx);

        // Click handler
        if (alive) card.setOnMouseClicked(ev -> { ev.consume(); onEnemyClick(e); });
        return card;
    }

    // ════════════════════════════════════════════════
    //  ALLY SPRITE — zona kanan, clickable
    // ════════════════════════════════════════════════
    private StackPane buildAllySprite(Entity ally, int total, int szPx) {
        boolean cur = cm.getCurrentActor()!=null && cm.getCurrentActor().getId().equals(ally.getId());
        boolean sel = selectedAlly!=null && selectedAlly.getId().equals(ally.getId());
        double sz = szPx > 0 ? szPx : (total<=1?110:total==2?96:80);

        // Wrapper StackPane (tidak ada HP bar — sudah ada di party bar bawah)
        StackPane wrapper = new StackPane();
        wrapper.setMinSize(sz, sz); wrapper.setMaxSize(sz, sz);
        wrapper.setCursor(javafx.scene.Cursor.HAND);

        // Sprite
        String pose = poseState.getOrDefault(ally.getId(), "idle");
        var sprite = ally instanceof Player
            ? AssetManager.spriteAsuna(pose)
            : ally instanceof Mercenary m ? AssetManager.spriteGuildmate(m.getName(), pose) : null;
        if (sprite == null && !pose.equals("idle")) {
            sprite = ally instanceof Player
                ? AssetManager.spriteAsuna("idle")
                : ally instanceof Mercenary m2 ? AssetManager.spriteGuildmate(m2.getName(),"idle") : null;
        }

        StackPane sp = new StackPane(); sp.setMinSize(sz,sz); sp.setMaxSize(sz,sz);
        if (sprite!=null) {
            var iv = AssetManager.makeIV(sprite,sz,sz);
            iv.setOpacity(ally.isAlive()?1.0:0.15);
            spriteMap.put(ally.getId(), iv);
            if (ally.isAlive() && !ally.getActiveEffects().isEmpty()) addAura(sp,ally);
            sp.getChildren().add(iv);
        }

        // Glow saat dipilih / giliran
        String glow = sel
            ? "-fx-effect:dropshadow(gaussian,#FFB830DD,22,0.7,0,0);"
            : cur ? "-fx-effect:dropshadow(gaussian,#FFB830AA,14,0.4,0,0);"
                  : "-fx-effect:dropshadow(gaussian,#00000066,4,0.2,0,1);";
        wrapper.setStyle(glow);

        // Pulse saat giliran ini
        if (cur && ally.isAlive()) {
            FadeTransition pt = new FadeTransition(Duration.millis(650), wrapper);
            pt.setFromValue(1.0); pt.setToValue(0.72);
            pt.setCycleCount(Animation.INDEFINITE); pt.setAutoReverse(true); pt.play();
        }

        // Label nama singkat (kecil di bawah sprite)
        String shortName = ally instanceof Mercenary mm
            ? mm.getMercenaryType().displayName.split(" ")[0] : ally.getName();
        Label nameLbl = new Label(shortName);
        nameLbl.setStyle("-fx-text-fill:#FFB83099;-fx-font-family:'Courier New';" +
            "-fx-font-size:8px;-fx-effect:dropshadow(gaussian,#000,3,0.8,0,1);");
        StackPane.setAlignment(nameLbl, Pos.BOTTOM_CENTER);
        StackPane.setMargin(nameLbl, new Insets(0,0,2,0));

        wrapper.getChildren().addAll(sp, nameLbl);
        wrapper.setOnMouseClicked(ev -> { ev.consume(); onAllyClick(ally); });
        return wrapper;
    }


    private HBox buildPartyBar() {
        partyBar = new HBox(0);
        partyBar.setStyle("-fx-background-color:#0A0603;-fx-border-color:#C8860A44;-fx-border-width:1 0 0 0;");
        return partyBar;
    }

    private void refreshPartyBar() {
        if (partyBar==null||cm==null) return;
        partyBar.getChildren().clear();
        List<Entity> allies = cm.getAllAllies();
        int n = allies.size();

        // Height adaptif: 3+ member lebih compact
        double barH = n >= 4 ? 76 : n >= 3 ? 82 : 100;
        partyBar.setPrefHeight(barH); partyBar.setMaxHeight(barH);

        double slotW = (double) ArclightApp.GAME_WIDTH / Math.max(n, 4);
        for (Entity ally : allies)
            partyBar.getChildren().add(buildPartySlot(ally, slotW, n));
        // Slot kosong
        for (int i = n; i < 4; i++) {
            Region empty = new Region(); empty.setPrefWidth(slotW);
            empty.setStyle("-fx-border-color:#1A1008;-fx-border-width:0 1 0 0;");
            partyBar.getChildren().add(empty);
        }
        if (!partyBar.getChildren().isEmpty())
            HBox.setHgrow(partyBar.getChildren().get(partyBar.getChildren().size()-1), Priority.ALWAYS);
    }

    private VBox buildPartySlot(Entity ally, double slotW, int totalMembers) {
        boolean cur = cm.getCurrentActor()!=null && cm.getCurrentActor().getId().equals(ally.getId());
        boolean sel = selectedAlly!=null && selectedAlly.getId().equals(ally.getId());

        VBox slot = new VBox(4);
        slot.setPrefWidth(slotW); slot.setMaxWidth(slotW);
        slot.setPadding(new Insets(8,10,8,10));
        slot.setStyle(
            "-fx-background-color:" +
            (sel?"#1C1408":cur?"#140E05":"#0A0603")+
            ";-fx-border-color:"+(sel?"#FFB830":cur?"#C8860A44":"#2A1808")+
            ";-fx-border-width:0 1 0 0;-fx-cursor:hand;");

        // Portrait + nama baris
        HBox topRow = new HBox(8); topRow.setAlignment(Pos.CENTER_LEFT);

        // Portrait lingkaran
        StackPane circle = new StackPane();
        double circSz = totalMembers >= 3 ? 28 : 36;
        circle.setMinSize(circSz,circSz); circle.setMaxSize(circSz,circSz);
        double cr = circSz/2.0;
        circle.setStyle("-fx-background-color:"+(cur?"#C8860A22":"#1A1208")+
            ";-fx-background-radius:"+cr+";-fx-border-color:"+(cur?"#C8860A":"#3A2810")+
            ";-fx-border-width:2;-fx-border-radius:"+cr+";");
        javafx.scene.image.Image portrait = null;
        if (ally instanceof Player) portrait = AssetManager.portraitAsuna();
        else if (ally instanceof Mercenary m) portrait = AssetManager.portraitGuildmate(m.getName(),false);
        if (portrait!=null) {
            double pvSz = circSz-4;
            var iv = AssetManager.makeIV(portrait,(int)pvSz,(int)pvSz);
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(pvSz/2,pvSz/2,pvSz/2);
            iv.setClip(clip); circle.getChildren().add(iv);
        } else {
            Label init = new Label(ally.getName().substring(0,1).toUpperCase());
            init.setStyle("-fx-text-fill:#C8860A;-fx-font-weight:bold;-fx-font-size:14px;");
            circle.getChildren().add(init);
        }

        // Nama + level + status giliran
        String dn = ally instanceof Mercenary m ? m.getMercenaryType().displayName : ally.getName();
        int lv = ally instanceof Mercenary m2 ? m2.getLoyaltyLevel() : ally instanceof Player p ? p.getLevel() : 1;
        VBox nameCol = new VBox(1);
        Label nmLbl = new Label(dn.toUpperCase());
        String nmSize = totalMembers >= 3 ? "9px" : "10px";
        nmLbl.setStyle("-fx-text-fill:"+(cur?"#FFB830":"#AA9060")+
            ";-fx-font-family:'Courier New';-fx-font-size:"+nmSize+";-fx-font-weight:bold;");
        Label status = new Label(cur?"READY \u25c4":"LV."+lv);
        status.setStyle("-fx-text-fill:"+(cur?"#C8860A":"#5A3A10")+
            ";-fx-font-family:'Courier New';-fx-font-size:8px;");
        nameCol.getChildren().addAll(nmLbl, status);
        topRow.getChildren().addAll(circle, nameCol);
        slot.getChildren().add(topRow);

        // Bars
        if (!ally.isAlive()) {
            Label dead = new Label("\u2020  GUGUR");
            dead.setStyle("-fx-text-fill:#660000;-fx-font-family:'Courier New';-fx-font-size:9px;");
            slot.getChildren().add(dead);
        } else {
            double bw = slotW - (totalMembers >= 3 ? 110 : 20);
            double maxHp = ally.getStats().get(StatType.MAX_HP);
            double maxShd = ally.getStats().get(StatType.MAX_SHIELD);
            double maxMp  = ally.getStats().get(StatType.MAX_MP);
            slot.getChildren().add(namedBar("HP", ally.getCurrentHp(), maxHp, "#DD2211","#FF5500", bw));
            if (maxShd>0) slot.getChildren().add(namedBar("SHD",ally.getCurrentShield(),maxShd,"#2255AA","#4488FF",bw));
            if (maxMp>0)  slot.getChildren().add(namedBar("MP", ally.getCurrentMp(), maxMp, "#334488","#5566CC", bw));

            // Status effects max 5
            HBox efx = new HBox(3);
            ally.getActiveEffects().stream().limit(5).forEach(ef -> efx.getChildren().add(UIFactory.effectBadge(ef)));
            if (!efx.getChildren().isEmpty()) slot.getChildren().add(efx);
        }

        slot.setOnMouseClicked(ev -> { ev.consume(); onAllyClick(ally); });
        return slot;
    }

    private HBox namedBar(String lbl, double cur, double max, String hi, String lo, double w) {
        double pct = max>0 ? Math.min(1.0, Math.max(0.0, cur/max)) : 0;
        double fillW = w*pct, emptyW = w-fillW;
        String col = pct>.5?hi:lo;

        Region fr = new Region(); fr.setPrefSize(fillW,5); fr.setMinWidth(fillW); fr.setMaxWidth(fillW);
        fr.setStyle("-fx-background-color:"+col+";-fx-background-radius:3 0 0 3;");
        Region er = new Region(); er.setPrefSize(emptyW,5); er.setMinWidth(0); er.setMaxWidth(emptyW);
        er.setStyle("-fx-background-color:#1A0808;-fx-background-radius:0 3 3 0;");

        HBox bar = new HBox(0); bar.setPrefSize(w,5); bar.setMinWidth(w); bar.setMaxWidth(w);
        bar.getChildren().addAll(fr,er);

        Label l = new Label(lbl+" "+(int)cur+"/"+(int)max);
        l.setStyle("-fx-text-fill:#4A3820;-fx-font-family:'Courier New';-fx-font-size:8px;");
        l.setMinWidth(55); l.setMaxWidth(55);

        HBox row = new HBox(5,l,bar); row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ════════════════════════════════════════════════
    //  REFRESH
    // ════════════════════════════════════════════════
    private void refresh() {
        if (cm==null) return;
        spriteMap.clear();
        // poseState TIDAK di-clear — dipertahankan antar refresh untuk animasi

        List<Entity> enemies = cm.getAllEnemies();
        List<Entity> allies  = cm.getAllAllies();
        int ne = enemies.size();

        // Enemy zone — Pane dengan absolute positioning
        enemyPane.getChildren().clear();
        double epW = enemyPane.getPrefWidth();
        double epH = enemyPane.getPrefHeight();
        if (epW == 0) epW = ArclightApp.GAME_WIDTH * 0.46;
        if (epH == 0) epH = ArclightApp.SCREEN_HEIGHT * 0.63;

        // Slot posisi (% dari ukuran Pane) — depan-belakang-kiri-kanan
        // Format: {xPct, yPct} dari pusat masing-masing slot
        double[][] slots = {
            {0.50, 0.52},  // slot 0: tengah-depan
            {0.72, 0.40},  // slot 1: kanan-belakang
            {0.28, 0.38},  // slot 2: kiri-belakang
            {0.78, 0.60},  // slot 3: kanan-depan
            {0.22, 0.62},  // slot 4: kiri-depan
            {0.50, 0.28},  // slot 5: tengah-belakang jauh
        };

        for (int i=0; i<ne; i++) {
            Entity en = enemies.get(i);
            var card = buildEnemyCard(en, ne);

            // Pilih slot berdasarkan index dan hash ID untuk variasi posisi
            int slotIdx = i < slots.length ? i : i % slots.length;
            // Untuk 1 musuh: gunakan hash untuk pilih slot dari semua slot
            if (ne == 1) {
                int h = Math.abs(en.getId().hashCode());
                slotIdx = h % slots.length;
            }

            double[] slot = slots[slotIdx];
            boolean isBoss = en instanceof Boss;
            double sz = isBoss ? 145 : (ne<=1?125:ne<=2?108:ne<=4?88:70);

            // Posisi: center sprite di slot
            double xPos = epW * slot[0] - sz/2;
            double yPos = epH * slot[1] - sz - 30; // -30 untuk HP bar di atas

            double ex = Math.max(0, Math.min(epW-sz-10, xPos));
            card.setLayoutX(ex);
            card.setLayoutY(Math.max(10, Math.min(epH-sz-40, yPos)));
            enemyPane.getChildren().add(card);
            // Simpan floatX: offset ke floatCanvas (enemyPane start ~30% dari lebar)
            if (en.getId() != null) entityFloatX.put(en.getId(), epW*0.30 + ex + sz/2);
        }

        // Ally zone — formation absolut
        allyPane.getChildren().clear();
        double aW = allyPane.getPrefWidth();
        double aH = allyPane.getPrefHeight();
        if (aW == 0) aW = ArclightApp.GAME_WIDTH * 0.30;
        if (aH == 0) aH = ArclightApp.SCREEN_HEIGHT * 0.63;
        int na = allies.size();
        // Slot formation [xPct, yPct] per jumlah ally
        // Urutan: [player/0, ally1/1, ally2/2, ally3/3]
        // Formation slots: [xPct, yPct] dari Pane (allyW × aH)
        // Asuna/player selalu depan-tengah, guildmate di belakang
        //
        //  1 ally:    [ A1 ]  tengah
        //  2 ally:  [A2]  [A1]  depan-kanan, belakang-kiri
        //  3 ally: [A2][A3]     dua belakang
        //              [A1]     satu depan
        //  4 ally: [A3][A4]     dua belakang atas
        //          [A2][A1]     dua depan bawah
        double[][] slots1 = {{0.40, 0.52}};
        double[][] slots2 = {{0.58, 0.60}, {0.18, 0.35}};
        double[][] slots3 = {{0.50, 0.70}, {0.12, 0.28}, {0.65, 0.25}};
        double[][] slots4 = {{0.58, 0.68}, {0.15, 0.65}, {0.58, 0.20}, {0.12, 0.22}};
        double[][] slotMap = na<=1?slots1:na==2?slots2:na==3?slots3:slots4;
        // Urutan ally: player pertama, lalu guildmate urut
        java.util.List<Entity> orderedAllies = new java.util.ArrayList<>();
        for (Entity a : allies) if (a instanceof arclightcity.entity.player.Player) orderedAllies.add(0, a);
        for (Entity a : allies) if (!(a instanceof arclightcity.entity.player.Player)) orderedAllies.add(a);
        for (int i=0; i<orderedAllies.size(); i++) {
            Entity a = orderedAllies.get(i);
            double[] slot = i<slotMap.length ? slotMap[i] : slotMap[slotMap.length-1];
            int szPx = na<=1?105:na==2?92:na==3?80:70;
            var card = buildAllySprite(a, na, szPx);
            double cx = aW * slot[0] - szPx/2.0;
            double cy = aH * slot[1] - szPx/2.0;
            double ax = Math.max(0, Math.min(aW-szPx-4, cx));
            card.setLayoutX(ax);
            card.setLayoutY(Math.max(0, Math.min(aH-szPx-20, cy)));
            allyPane.getChildren().add(card);
            // Simpan floatX: ally pane di sisi kiri canvas
            if (a.getId() != null) entityFloatX.put(a.getId(), ax + szPx/2.0);
        }

        // Header
        Entity actor = cm.getCurrentActor();
        if (turnLabel!=null) turnLabel.setText("Giliran "+cm.getTotalTurns());
        if (actorLabel!=null && actor!=null)
            actorLabel.setText(actor.getName().toUpperCase()+" \u25ba");

        refreshPartyBar();
    }

    // ════════════════════════════════════════════════
    //  ACTION OVERLAY — muncul di tengah scene
    // ════════════════════════════════════════════════
    private void onAllyClick(Entity ally) {
        if (!ally.isAlive()) return;
        if (targetingMode) { cancelAction(); return; }
        selectedAlly = ally;
        refresh();
        showMainMenu(ally);
    }

    private void onEnemyClick(Entity enemy) {
        if (!enemy.isAlive()) return;
        if (targetingMode && selectedAlly!=null && cm.isWaitingForPlayer()) {
            // Konfirmasi target → eksekusi aksi
            if (pendingSkillId == null) {
                cm.submitPlayerAction(CombatAction.basicAttack(List.of(enemy.getId())));
            } else {
                cm.submitPlayerAction(CombatAction.useSkill(pendingSkillId, List.of(enemy.getId())));
            }
            cancelAction();
            Platform.runLater(this::refresh);
        } else {
            // Klik enemy tanpa targeting mode = auto select ally pertama yang giliran
            if (!targetingMode && cm.isWaitingForPlayer()) {
                Entity actor = cm.getCurrentActor();
                if (actor instanceof Player || actor instanceof Mercenary) {
                    selectedAlly = actor;
                    showMainMenu(actor);
                }
            }
        }
    }

    private void showMainMenu(Entity actor) {
        actionOverlay.getChildren().clear();
        actionOverlay.setVisible(true);
        targetingMode = false;

        // Title header dengan warna accent
        HBox titleRow = new HBox();
        titleRow.setStyle("-fx-background-color:#C8860A;-fx-padding:8 14;");
        String dn = actor instanceof Mercenary m ? m.getMercenaryType().displayName : actor.getName();
        Label title = new Label("\u25c6  " + dn.toUpperCase());
        title.setStyle("-fx-text-fill:#0A0603;-fx-font-family:'Courier New';-fx-font-size:12px;-fx-font-weight:bold;");
        titleRow.getChildren().add(title);

        actionOverlay.getChildren().add(titleRow);

        // Menu items
        oRow("\u2717  SERANG",    "#FF6644", e -> startTargeting(actor, null));
        oRow("\u2605  JURUS",     "#FFB830", e -> showJurusMenu(actor));
        oRow("\u2261  ITEM",      "#44AA66", e -> showItemMenu(actor));
        oRow("\u21ba  BERTAHAN",  "#8866CC", e -> { cancelAction(); doDefend(); });
        oRow("\u2190  KABUR",     "#886644", e -> doFlee());
    }

    private void showJurusMenu(Entity actor) {
        actionOverlay.getChildren().clear();
        HBox titleRow = new HBox();
        titleRow.setStyle("-fx-background-color:#C8860A88;-fx-padding:7 14;");
        Label title = new Label("\u2605  PILIH JURUS");
        title.setStyle("-fx-text-fill:#FFD080;-fx-font-family:'Courier New';-fx-font-size:11px;-fx-font-weight:bold;");
        titleRow.getChildren().add(title);
        actionOverlay.getChildren().add(titleRow);

        for (String[] sk : getSkills(actor)) {
            String sid=sk[0], sname=sk[1], sdesc=sk[2];
            double mp = arclightcity.combat.SkillExecutor.getMpCost(sid);
            int cd = actor instanceof Player p ? p.getSkillCooldown(sid) : 0;
            boolean ok = actor.getCurrentMp()>=mp && cd==0;

            VBox entry = new VBox(2);
            entry.setPadding(new Insets(7,14,7,14));
            entry.setStyle(ok
                ? "-fx-background-color:#1A1208;-fx-border-color:#3A2810;-fx-border-width:0 0 1 0;-fx-cursor:hand;"
                : "-fx-background-color:#0E0B07;-fx-border-color:#2A1808;-fx-border-width:0 0 1 0;-fx-opacity:0.5;");

            HBox nameRow = new HBox(8); nameRow.setAlignment(Pos.CENTER_LEFT);
            var icon = AssetManager.iconSkill(sid);
            if (icon!=null) nameRow.getChildren().add(AssetManager.makeIV(icon,18,18));

            Label nm = new Label("\u25ba "+sname);
            nm.setStyle("-fx-text-fill:"+(ok?"#EDE0C8":"#5A3A10")+
                ";-fx-font-family:'Courier New';-fx-font-size:11px;");
            HBox.setHgrow(nm, Priority.ALWAYS);
            Label cost = new Label("MP:"+(int)mp+(cd>0?" CD:"+cd:""));
            cost.setStyle("-fx-text-fill:"+(ok?"#5566AA":"#3A2810")+";-fx-font-family:'Courier New';-fx-font-size:9px;");
            nameRow.getChildren().addAll(nm,cost);

            Label desc = new Label(sdesc);
            desc.setStyle("-fx-text-fill:#4A3820;-fx-font-family:'Courier New';-fx-font-size:8px;");
            desc.setWrapText(true); desc.setMaxWidth(210);
            entry.getChildren().addAll(nameRow,desc);

            if (ok) {
                final String fs=sid;
                entry.setOnMouseClicked(ev -> { ev.consume(); startTargeting(actor, fs); });
                entry.setOnMouseEntered(ev -> entry.setStyle(entry.getStyle().replace("#1A1208","#231B0D")));
                entry.setOnMouseExited(ev  -> entry.setStyle(entry.getStyle().replace("#231B0D","#1A1208")));
            }
            actionOverlay.getChildren().add(entry);
        }

        oRow("\u2190  KEMBALI", "#886644", e -> showMainMenu(actor));
    }

    private void showItemMenu(Entity actor) {
        actionOverlay.getChildren().clear();
        HBox titleRow = new HBox();
        titleRow.setStyle("-fx-background-color:#2D7A4588;-fx-padding:7 14;");
        Label title = new Label("\u2261  PILIH ITEM");
        title.setStyle("-fx-text-fill:#88DDAA;-fx-font-family:'Courier New';-fx-font-size:11px;-fx-font-weight:bold;");
        titleRow.getChildren().add(title);
        actionOverlay.getChildren().add(titleRow);

        var items = engine.getInventory().getAllBagItems().stream()
            .filter(i -> i instanceof arclightcity.item.Consumable).toList();

        for (var it : items) {
            var c = (arclightcity.item.Consumable)it;
            VBox entry = new VBox(1); entry.setPadding(new Insets(7,14,7,14));
            entry.setStyle("-fx-background-color:#1A1208;-fx-border-color:#3A2810;-fx-border-width:0 0 1 0;-fx-cursor:hand;");

            Label nm = new Label("\u25ba "+c.getName()+" \u00d7"+c.getStackCount());
            nm.setStyle("-fx-text-fill:#EDE0C8;-fx-font-family:'Courier New';-fx-font-size:11px;");
            Label eff = new Label("+"+(int)c.getEffectValue()+" HP");
            eff.setStyle("-fx-text-fill:#44AA66;-fx-font-family:'Courier New';-fx-font-size:9px;");
            entry.getChildren().addAll(nm,eff);
            entry.setOnMouseClicked(ev -> {
                ev.consume(); cancelAction();
                if (cm.isWaitingForPlayer()) {
                    // Kurangi stack dulu di UI
                    c.useOne();
                    if (c.getStackCount() <= 0) engine.getInventory().removeItem(c.getId());
                    // Encode: "itemId|healAmt|itemName" agar CombatManager bisa heal
                    String enc = c.getId() + "|" + (int)c.getEffectValue() + "|" + c.getName();
                    cm.submitPlayerAction(CombatAction.useItem(enc, List.of(engine.getPlayer().getId())));
                }
                Platform.runLater(this::refresh);
            });
            entry.setOnMouseEntered(ev -> entry.setStyle(entry.getStyle().replace("#1A1208","#231B0D")));
            entry.setOnMouseExited(ev  -> entry.setStyle(entry.getStyle().replace("#231B0D","#1A1208")));
            actionOverlay.getChildren().add(entry);
        }
        if (items.isEmpty()) {
            Label none = new Label("   Tidak ada item");
            none.setStyle("-fx-text-fill:#5A3A10;-fx-font-family:'Courier New';-fx-padding:8;");
            actionOverlay.getChildren().add(none);
        }
        oRow("\u2190  KEMBALI", "#886644", e -> showMainMenu(actor));
    }

    /** Masuk targeting mode — enemy akan di-highlight, click = eksekusi */
    private void startTargeting(Entity actor, String skillId) {
        selectedAlly   = actor;
        pendingSkillId = skillId;
        targetingMode  = true;
        actionOverlay.setVisible(false);

        // Tampil instruksi kecil di overlay
        actionOverlay.getChildren().clear();
        HBox hint = new HBox(8); hint.setAlignment(Pos.CENTER_LEFT);
        hint.setStyle("-fx-background-color:#1A1208CC;-fx-padding:8 14;-fx-border-color:#C8860A55;-fx-border-width:0 0 1 0;");
        Label hintLbl = new Label("\u25cf Pilih target musuh...");
        hintLbl.setStyle("-fx-text-fill:#FFB830;-fx-font-family:'Courier New';-fx-font-size:11px;");
        Button cancel = hBtn("\u2715","#CC3300","transparent");
        cancel.setOnAction(e -> cancelAction());
        HBox.setHgrow(hintLbl, Priority.ALWAYS);
        hint.getChildren().addAll(hintLbl, cancel);
        actionOverlay.getChildren().add(hint);
        actionOverlay.setVisible(true);

        refresh(); // rebuild enemy cards dengan highlight
    }

    private void cancelAction() {
        targetingMode  = false;
        selectedAlly   = null;
        pendingSkillId = null;
        actionOverlay.setVisible(false);
        refresh();
    }

    private void oRow(String txt, String color, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color:transparent;-fx-border-color:"+color+"33;-fx-border-width:0 0 1 0;-fx-padding:10 14;-fx-cursor:hand;");
        Label lbl = new Label(txt);
        lbl.setStyle("-fx-text-fill:"+color+";-fx-font-family:'Courier New';-fx-font-size:14px;-fx-font-weight:bold;");
        row.getChildren().add(lbl);
        row.setOnMouseClicked(ev -> { ev.consume(); handler.handle(null); });
        row.setOnMouseEntered(ev -> row.setStyle("-fx-background-color:"+color+"18;-fx-border-color:"+color+"55;-fx-border-width:0 0 1 0;-fx-padding:10 14;-fx-cursor:hand;"));
        row.setOnMouseExited(ev  -> row.setStyle("-fx-background-color:transparent;-fx-border-color:"+color+"33;-fx-border-width:0 0 1 0;-fx-padding:10 14;-fx-cursor:hand;"));
        actionOverlay.getChildren().add(row);
    }

    // ════════════════════════════════════════════════
    //  PLAYER ACTIONS
    // ════════════════════════════════════════════════
    private void doDefend() {
        if (cm!=null && cm.isWaitingForPlayer()) {
            cm.submitPlayerAction(CombatAction.defend());
            Platform.runLater(this::refresh);
        }
    }

    private void doFlee() {
        if (cm==null) return;
        cancelAction();
        boolean ok = cm.attemptFlee();
        if (ok) Platform.runLater(() -> cm.processTurn());
        else    Platform.runLater(this::refresh);
    }

    // ════════════════════════════════════════════════
    //  COMBAT LOOP
    // ════════════════════════════════════════════════
    public void startCombatLoop() { startLoop(); }

    private void startLoop() {
        if (combatLoop!=null) combatLoop.stop();
        combatLoop = new Timeline(new KeyFrame(Duration.millis(speedMs), e -> {
            if (cm!=null && cm.isCombatActive() && !cm.isWaitingForPlayer()) {
                cm.processTurn();
                Platform.runLater(this::refresh);
            }
        }));
        combatLoop.setCycleCount(Animation.INDEFINITE);
        combatLoop.play();
    }

    // ════════════════════════════════════════════════
    //  EVENTS + SPRITE ANIMATION
    // ════════════════════════════════════════════════
    private void onEvent(CombatEvent ev) {
        if (ev==null) return;
        spawnFloat(ev);

        // Sprite pose animation — delay refresh agar animasi terlihat
        String actorId  = ev.getActorId();
        String targetId = ev.getTargetId();
        Entity actor    = findEntity(actorId);
        Entity target   = findEntity(targetId);
        int animDur     = 500;

        switch (ev.getType()) {
            case SKILL_USED         -> { animSprite(actorId,actor,"attack",600); animDur=650; }
            case DAMAGE_DEALT,
                 CRITICAL_HIT       -> {
                animSprite(actorId,actor,"attack",420);
                animSprite(targetId,target,"hit",380);
                animDur=450;
            }
            case DAMAGE_EVADED      -> { animSprite(targetId,target,"hit",250); animDur=300; }
            case ENTITY_DIED        -> {
                // Hit pose permanen
                var iv = spriteMap.get(targetId);
                if (iv!=null && target!=null) {
                    var hitImg = getPose(target,"hit");
                    if (hitImg!=null) Platform.runLater(()->{ iv.setImage(hitImg); iv.setOpacity(0.2); });
                }
            }
            case MERCENARY_LOYALTY_UP -> {
                Entity loyaltyEntity = findEntity(actorId);
                if (loyaltyEntity instanceof Mercenary lm) {
                    String lvUpMsg = "⬆ " + lm.getMercenaryType().displayName +
                        " naik ke LV." + lm.getLoyaltyLevel() + "!";
                    Platform.runLater(() -> router.addSystemChat(lvUpMsg));
                }
                animDur = 0;
            }
            default -> {}
        }

        // animSprite sudah handle refresh() via poseState
        // Untuk event yang tidak ada animasi, refresh langsung
        if (animDur == 500) Platform.runLater(this::refresh);
    }

    private void onEnd(CombatResult res) {
        if (combatLoop!=null) combatLoop.stop();
        if (res.isVictory())     router.showVictory(res);
        else if (res.isDefeat()) router.showGameOver();
        else                     router.showDungeonMap();
    }

    // ════════════════════════════════════════════════
    //  SPRITE POSE ANIMATION
    // ════════════════════════════════════════════════
    private void animSprite(String id, Entity entity, String pose, int durMs) {
        if (id==null||entity==null) return;
        // Update poseState lalu trigger refresh — lebih reliable dari ImageView manipulation
        poseState.put(id, pose);
        Platform.runLater(this::refresh);
        // Kembali ke idle setelah durasi
        new Timeline(new KeyFrame(Duration.millis(durMs), e2 -> {
            poseState.put(id, "idle");
            Platform.runLater(this::refresh);
        })).play();
    }

    private javafx.scene.image.Image getPose(Entity e, String pose) {
        if (e==null) return null;
        if (e instanceof Boss b) return AssetManager.spriteBossByFloor(b.getFloorLevel(),pose);
        if (e instanceof Player)  return AssetManager.spriteAsuna(pose.equals("attack")?"attack":pose.equals("hit")?"hit":"idle");
        if (e instanceof Mercenary m) return AssetManager.spriteGuildmate(m.getName(),pose);
        return AssetManager.spriteEnemy(e.getName(),pose);
    }

    private Entity findEntity(String id) {
        if (id==null||cm==null) return null;
        for (Entity e : cm.getAllAllies())  if (e.getId().equals(id)) return e;
        for (Entity e : cm.getAllEnemies()) if (e.getId().equals(id)) return e;
        return null;
    }

    // ════════════════════════════════════════════════
    //  FLOATING DAMAGE
    // ════════════════════════════════════════════════
    private void spawnFloat(CombatEvent ev) {
        if (floatCanvas==null) return;
        String msg=ev.getMessage(); if(msg==null) return;
        double H=floatCanvas.getHeight();
        // Posisi X berdasarkan entity yang terkena/melakukan aksi
        String targetId = ev.getTargetId() != null ? ev.getTargetId() : ev.getActorId();
        double x;
        if (targetId != null && entityFloatX.containsKey(targetId)) {
            x = entityFloatX.get(targetId) + (Math.random()-0.5)*40;
        } else {
            // Fallback: sisi kiri untuk ally-related, kanan untuk enemy
            x = 60 + Math.random() * 560;
        }
        double y=H*0.45+Math.random()*50;
        String txt=null, col=null;
        switch(ev.getType()) {
            case CRITICAL_HIT   ->{txt="\u26a1 "+(int)num(msg)+"!!"; col="#FFD700";}
            case DAMAGE_DEALT   ->{double d=num(msg);if(d<=0)return;txt="-"+(int)d;
                col=msg.contains("Fisik")?"#FF6644":msg.contains("Cyber")?"#44AAFF":"#FF8844";}
            case DAMAGE_EVADED  ->{txt="HINDAR";  col="#88CCFF";}
            case DAMAGE_BLOCKED ->{txt="\ud83d\udee1 "+(int)num(msg); col="#6699CC";}
            case HEAL_RECEIVED  ->{txt="+"+(int)num(msg)+" HP"; col="#44FF88"; y*=0.6;}
            case EFFECT_TICK    ->{txt="\u2620 "+(int)num(msg); col="#AA4400";}
            case SKILL_USED     ->{txt="\u2726 "+last(msg); col="#FFB830"; y*=0.35;}
            case ENTITY_DIED    ->{txt="\u2620 GUGUR"; col="#CC3300";}
            case MERCENARY_LOYALTY_UP->{txt="\u2b06 LEVEL UP!"; col="#FFD700"; y*=0.2;}
            case SKILL_FAILED   ->{txt="MP KURANG"; col="#884422";}
            default->{return;}
        }
        if (txt==null) return;
        floats.add(new FT(txt,col,x,y,System.currentTimeMillis()));
        if (floatLoop==null||floatLoop.getStatus()!=Animation.Status.RUNNING) {
            floatLoop=new Timeline(new KeyFrame(Duration.millis(30), e->drawFloats()));
            floatLoop.setCycleCount(Animation.INDEFINITE); floatLoop.play();
        }
    }

    private void drawFloats() {
        if(floatCanvas==null) return;
        var gc=floatCanvas.getGraphicsContext2D();
        gc.clearRect(0,0,floatCanvas.getWidth(),floatCanvas.getHeight());
        long now=System.currentTimeMillis();
        floats.removeIf(f->(now-f.t0())>f.dur());
        for(var f:floats) {
            double el=(now-f.t0())/(double)f.dur();
            double alpha=el<0.3?1.0:Math.max(0,(1.0-el)*1.4);
            try {
                boolean big=!f.t().isEmpty()&&(f.t().charAt(0)==0x26A1||f.t().charAt(0)==0x2726);
                gc.setFont(Font.font("Courier New",big?FontWeight.BOLD:FontWeight.NORMAL,big?24:17));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setFill(Color.web("#000",alpha*0.8)); gc.fillText(f.t(),f.x()+2,f.y()-el*f.rise()+2);
                gc.setFill(Color.web(f.c(),alpha));     gc.fillText(f.t(),f.x(),f.y()-el*f.rise());
            } catch(Exception ignored){}
        }
        if(floats.isEmpty()&&floatLoop!=null){floatLoop.stop();floatLoop=null;}
    }

    // ════════════════════════════════════════════════
    //  SKILL DATA + HELPERS
    // ════════════════════════════════════════════════
    private List<String[]> getSkills(Entity actor) {
        if (actor instanceof Player pl)
            return pl.getEquippedSkillIds().stream()
                .map(sid->new String[]{sid,sname(sid),sdesc(sid)}).toList();
        if (actor instanceof Mercenary m) return switch(m.getMercenaryType()) {
            case TANK_RX9     -> List.of(new String[]{"IRON_SHIELD","Tameng Baja","Barrier ke sekutu kritis"},
                                          new String[]{"TAUNT",      "Provokasi",  "Paksa semua musuh serang RX9"});
            case KIRA_VOSS    -> List.of(new String[]{"PHANTOM_SHOT","Panah Bayang","Crit dijamin + expose"},
                                          new String[]{"SHADOW_STEP", "Langkah Gaib","Abaikan evasion musuh"});
            case SERA_MEND    -> List.of(new String[]{"TRIAGE_HEAL","Penyembuhan","HP + Regen 3 giliran"},
                                          new String[]{"CLEANSE_PROTOCOL","Hapus Kutuk","Bersihkan semua debuff"});
            case MAGNUS_FORGE -> List.of(new String[]{"OVERLOAD_SHOT","Tembak Lebur","AoE Cyber + BURN"},
                                          new String[]{"SHRED_CANNON","Meriam Hancur","Strip DEF musuh"});
            case VECTOR       -> List.of(new String[]{"SHADOW_STEP","Langkah Bayang","Serang blind spot"},
                                          new String[]{"EXECUTE",    "Eksekusi",     "Damage x2 HP rendah"});
            case ECHO_NULL    -> List.of(new String[]{"EMP_BURST","Ledakan EMP","STUN semua mekanikal"},
                                          new String[]{"FREQUENCY_LOCK","Kunci Frekuensi","FREEZE musuh terkuat"},
                                          new String[]{"SIGNAL_JAM","Macet Sinyal","-30% SPD semua musuh"});
            case LYRA_BLOOM   -> List.of(new String[]{"NEON_BLOOM","Mekar Neon","Regen HP semua sekutu"},
                                          new String[]{"BLOOM_MEND","Pemulih Mekar","Regen kuat 1 sekutu"});
        };
        return List.of();
    }

    private void addAura(StackPane box, Entity e) {
        boolean buff = e.getActiveEffects().stream().anyMatch(ef->ef.getType().isPositive());
        boolean debuff= e.getActiveEffects().stream().anyMatch(ef->ef.getType().isNegative());
        if (!buff&&!debuff) return;
        String c = buff&&debuff?"#AA55AA":buff?"#44AA88":"#AA4422";
        Region aura = new Region(); aura.setMouseTransparent(true);
        aura.setStyle("-fx-background-color:transparent;-fx-border-color:"+c+";-fx-border-width:2;-fx-border-radius:4;" +
            "-fx-effect:dropshadow(gaussian,"+c+",8,0.5,0,0);");
        box.getChildren().add(0,aura);
    }

    private HBox buildHBar(double w, double pct) {
        double fill=Math.max(0,w*Math.min(1,pct)), empty=Math.max(0,w-fill);
        String col=pct>.6?"#CC2211":pct>.3?"#FF5500":"#FF1100";
        Region fr=new Region(); fr.setPrefSize(fill,6); fr.setMinWidth(fill); fr.setMaxWidth(fill);
        fr.setStyle("-fx-background-color:"+col+";-fx-background-radius:3 0 0 3;");
        Region er=new Region(); er.setPrefSize(empty,6); er.setMinWidth(0); er.setMaxWidth(empty);
        er.setStyle("-fx-background-color:#1A0404;-fx-background-radius:0 3 3 0;");
        HBox bar=new HBox(0); bar.setPrefSize(w,6); bar.setMinWidth(w); bar.setMaxWidth(w);
        bar.getChildren().addAll(fr,er); return bar;
    }

    private Label hpLabel(Entity e) {
        Label l=new Label((int)e.getCurrentHp()+"/"+(int)e.getStats().get(StatType.MAX_HP));
        l.setStyle("-fx-text-fill:#FFAA88;-fx-font-family:'Courier New';-fx-font-size:9px;" +
                  "-fx-effect:dropshadow(gaussian,#000,3,1,0,1);");
        return l;
    }

    private Button hBtn(String t, String tc, String bg) {
        Button b=new Button(t);
        b.setStyle("-fx-background-color:"+bg+";-fx-border-color:"+tc+"55;-fx-border-width:1;" +
            "-fx-text-fill:"+tc+";-fx-font-family:'Courier New';-fx-font-size:10px;-fx-padding:4 8;-fx-cursor:hand;");
        return b;
    }

    private double num(String msg) {
        try{var m=java.util.regex.Pattern.compile("([0-9]+[.]?[0-9]*)").matcher(msg);
            if(m.find())return Double.parseDouble(m.group(1));}catch(Exception e){}return 0;
    }
    private String last(String msg){int i=msg==null?-1:msg.lastIndexOf(" ");return i>0?msg.substring(i+1):"Skill";}
    private String sname(String sid){return switch(sid){
        case "POWER_STRIKE"->"Pukulan Harimau";case "EXECUTE"->"Tebasan Pamungkas";
        case "PHANTOM_SHOT"->"Panah Bayangan";case "SHADOW_STEP"->"Langkah Gaib";
        case "DEEP_HACK"->"Bidang Bayangan";case "NULL_FIELD"->"Protokol Nol";
        case "SOVEREIGN_STRIKE"->"Tebasan Agung";case "NULL_PROTOCOL"->"Null Protocol";
        case "DATA_FRAGMENTATION"->"Pecah Jiwa";case "ENERGY_DRAIN"->"Serap Tenaga";
        case "IRON_SHIELD"->"Tameng Baja";case "SEISMIC_SLAM"->"Gempa Bumi";
        case "SACRED_SEAL"->"Rajah Pelindung";default->sid.replace("_"," ");
    };}
    private String sdesc(String sid){return switch(sid){
        case "POWER_STRIKE"->"Serangan Fisik kuat ke 1 target";
        case "EXECUTE"->"Damage x2 saat target HP < 30%";
        case "PHANTOM_SHOT"->"Tembakan crit dijamin";
        case "SHADOW_STEP"->"Serang dari blind spot, abaikan evasion";
        case "DEEP_HACK"->"Serangan Cyber + SHRED armor";
        case "IRON_SHIELD"->"Pasang barrier pelindung";
        case "SEISMIC_SLAM"->"Hantam tanah, gempa menyebar";
        case "SACRED_SEAL"->"Segel keramat, buff DEF party";
        case "ENERGY_DRAIN"->"Serap MP musuh, isi MP sendiri";
        default->"";
    };}

    // Legacy alias
    public void refreshActionPanel() { refresh(); }
    /** Hentikan semua timer — dipanggil SceneRouter saat navigasi keluar dari combat */
    public void stopAll() {
        if (combatLoop != null) { combatLoop.stop(); combatLoop = null; }
        if (floatLoop  != null) { floatLoop.stop();  floatLoop  = null; }
    }


}
