package arclightcity.ui.view;
import arclightcity.entity.base.Entity;
import arclightcity.entity.stats.StatType;
import arclightcity.combat.*;
import arclightcity.engine.GameEngine;
import arclightcity.entity.mercenary.Mercenary;
import arclightcity.entity.player.Player;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import arclightcity.ui.ArclightApp;
import javafx.util.Duration;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;
import java.util.List;

/**
 * CombatView v0.6.5 — Layout RPG klasik:
 *
 * ┌─────────────────────────────────────────┐
 * │ KABUR | PERTEMPURAN Giliran X | ACTOR ▶ │  ← header bar
 * ├─────────────────────────────────────────┤
 * │                                         │
 * │  BG dungeon (full width, ~320px)        │  ← scene area
 * │                                         │
 * │  [Enemy1]  [Enemy2]  [Enemy3]           │  ← enemy di tengah-atas BG
 * │   HP████░   HP███░    HP████            │
 * │                                         │
 * │ [Ally1] [Ally2] [Ally3]  ← kiri bawah  │  ← ally kecil di bawah BG
 * ├─────────────────────────────────────────┤
 * │ [sk1][sk2][sk3][sk4]  MP ████░          │  ← skill slots
 * │ [SERANG] [JURUS] [ITEM] [BERTAHAN][KABUR│  ← action buttons
 * └─────────────────────────────────────────┘
 */
public class CombatView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private CombatManager     cm;

    private HBox     enemyRow;
    private HBox     allyRow;
    private VBox     actionPanel;
    private Label    turnLabel;
    private Label    actorLabel;
    private Timeline combatLoop;
    private Timeline floatLoop;
    private Canvas   floatCanvas;
    private int      speedMs = 1200;

    private record FloatingText(String text, String color,
                                double x, double y, long t0, int dur, double rise) {
        FloatingText(String t, String c, double x, double y, long t0) {
            this(t, c, x, y, t0, t.startsWith("\u26a1") ? 1800 : 1400,
                               t.startsWith("\u26a1") ? 80 : 55);
        }
    }
    private final List<FloatingText> floats = new java.util.ArrayList<>();

    public CombatView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
        this.cm     = engine.getActiveCombat();
    }

    // ════════════════════════════════════════════
    //  BUILD
    // ════════════════════════════════════════════
    public Parent build() {
        BorderPane root = UIFactory.screenRootBorder();
        root.setStyle("-fx-background-color:#0A0503;");

        root.setTop(buildHeader());
        root.setCenter(buildScene());

        actionPanel = new VBox(6);
        actionPanel.setPadding(new Insets(6, 10, 8, 10));
        actionPanel.setStyle("-fx-background-color:#0D0903;" +
            "-fx-border-color:#C8860A55; -fx-border-width:1 0 0 0;");
        root.setBottom(actionPanel);

        if (cm != null) {
            cm.addEventListener(ev -> Platform.runLater(() -> onEvent(ev)));
            cm.addResultListener(res -> Platform.runLater(() -> onEnd(res)));
        }
        refresh();
        refreshActions();
        startLoop();
        UIFactory.fadeIn(root, 300);
        return root;
    }

    // ── HEADER ──────────────────────────────────
    private HBox buildHeader() {
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(6, 12, 6, 12));
        h.setStyle("-fx-background-color:linear-gradient(to right,#1A0806,#0A0503);" +
                   "-fx-border-color:#CC330055; -fx-border-width:0 0 1 0;");

        Button kabur = btn("\u2190 KABUR", "#CC3300", "#CC330022");
        kabur.setOnAction(e -> doFlee());

        Label lbl = new Label("PERTEMPURAN");
        lbl.setStyle("-fx-text-fill:#CC3300; -fx-font-family:'Courier New';" +
            "-fx-font-size:13px; -fx-font-weight:bold;");

        turnLabel = new Label("Giliran 1");
        turnLabel.setStyle("-fx-text-fill:#5A3A10; -fx-font-family:'Courier New'; -fx-font-size:11px;");
        HBox.setHgrow(turnLabel, Priority.ALWAYS);

        actorLabel = new Label("ASUNA \u25ba");
        actorLabel.setStyle("-fx-text-fill:#FFB830; -fx-font-family:'Courier New';" +
            "-fx-font-size:12px; -fx-font-weight:bold;");

        Label sep = new Label("  SPD:");
        sep.setStyle("-fx-text-fill:#3A2810; -fx-font-size:10px;");
        ToggleGroup tg = new ToggleGroup();
        ToggleButton t1 = spd("1\u00d7", 1200, tg, true);
        ToggleButton t2 = spd("2\u00d7", 500,  tg, false);
        ToggleButton ts = spd("SKIP",    50,   tg, false);

        h.getChildren().addAll(kabur, lbl, turnLabel, actorLabel, sep, t1, t2, ts);
        return h;
    }

    private ToggleButton spd(String txt, int ms, ToggleGroup g, boolean sel) {
        ToggleButton b = new ToggleButton(txt);
        b.setToggleGroup(g); b.setSelected(sel);
        String s = "-fx-text-fill:#A09070; -fx-font-family:'Courier New';" +
                   "-fx-font-size:9px; -fx-padding:2 6; -fx-cursor:hand;";
        b.setStyle("-fx-background-color:" + (sel?"#3A2810":"#1A1008") + ";" + s);
        b.selectedProperty().addListener((o,ov,nv) -> {
            if (nv) { speedMs = ms; startLoop(); }
            b.setStyle("-fx-background-color:" + (nv?"#3A2810":"#1A1008") + ";" + s);
        });
        return b;
    }

    // ── SCENE ───────────────────────────────────
    private BorderPane buildScene() {
        BorderPane scene = new BorderPane();
        double sceneH = ArclightApp.SCREEN_HEIGHT * 0.60;
        scene.setPrefHeight(sceneH);
        scene.setMaxHeight(sceneH);

        // Background
        StackPane bgPane = new StackPane();
        bgPane.setStyle("-fx-background-color:#060402;");
        try {
            int fl = engine.getDungeonManager().getCurrentFloorNumber();
            var bg = arclightcity.ui.util.AssetManager.bgDungeon(fl);
            if (bg != null) {
                var iv = arclightcity.ui.util.AssetManager.makeIVFill(
                    bg, ArclightApp.GAME_WIDTH, sceneH);
                iv.setOpacity(0.78);
                iv.setMouseTransparent(true);
                bgPane.getChildren().add(iv);
            }
        } catch (Exception ignored) {}

        // Floating canvas
        floatCanvas = new Canvas(ArclightApp.GAME_WIDTH, sceneH);
        floatCanvas.setMouseTransparent(true);
        bgPane.getChildren().add(floatCanvas);
        scene.setCenter(bgPane);

        // Enemy row — CENTER-TOP of bg
        enemyRow = new HBox(20);
        enemyRow.setAlignment(Pos.BOTTOM_CENTER);
        enemyRow.setPadding(new Insets(20, 0, 0, 0));
        StackPane.setAlignment(enemyRow, Pos.TOP_CENTER);
        bgPane.getChildren().add(enemyRow);

        // Ally row — BOTTOM-LEFT of bg, smaller
        allyRow = new HBox(8);
        allyRow.setAlignment(Pos.BOTTOM_LEFT);
        allyRow.setPadding(new Insets(0, 0, 6, 8));
        StackPane.setAlignment(allyRow, Pos.BOTTOM_LEFT);
        bgPane.getChildren().add(allyRow);

        return scene;
    }

    // ── ENEMY CARD ──────────────────────────────
    private VBox buildEnemy(Entity e) {
        boolean isBoss = e instanceof arclightcity.entity.enemy.Boss;
        double sz = isBoss ? 140 : 110;

        boolean cur = cm.getCurrentActor() != null
            && cm.getCurrentActor().getId().equals(e.getId());

        VBox card = new VBox(3);
        card.setAlignment(Pos.BOTTOM_CENTER);
        card.setStyle(cur
            ? "-fx-effect:dropshadow(gaussian,#FF4422,20,0.6,0,0);" : "");

        // HP bar (slim, above sprite)
        double hp = e.getHpPercent();
        Region bg = new Region(); bg.setPrefSize(sz, 5);
        bg.setStyle("-fx-background-color:#1A0404; -fx-background-radius:3;");
        Region fill = new Region(); fill.setPrefSize(sz*hp, 5);
        fill.setStyle("-fx-background-color:" +
            (hp>.6?"#DD2211":hp>.3?"#FF5500":"#FF1100") + "; -fx-background-radius:3;");
        StackPane bar = new StackPane(); bar.setAlignment(Pos.CENTER_LEFT);
        bar.getChildren().addAll(bg, fill);

        Label hpLbl = new Label((int)e.getCurrentHp() + "/" + (int)e.getStats().get(StatType.MAX_HP));
        hpLbl.setStyle("-fx-text-fill:#FFAA88; -fx-font-family:'Courier New'; -fx-font-size:9px;" +
                      "-fx-effect:dropshadow(gaussian,#000,3,1,0,1);");

        // Sprite
        var sprite = isBoss
            ? arclightcity.ui.util.AssetManager.spriteBossByFloor(
                ((arclightcity.entity.enemy.Boss)e).getFloorLevel(), "idle")
            : arclightcity.ui.util.AssetManager.spriteEnemy(e.getName(), "idle");
        if (sprite == null && isBoss)
            sprite = arclightcity.ui.util.AssetManager.spriteBoss("theresa","idle");

        StackPane sp = new StackPane(); sp.setMinSize(sz,sz); sp.setMaxSize(sz,sz);
        if (sprite != null) {
            var iv = arclightcity.ui.util.AssetManager.makeIV(sprite, sz, sz);
            iv.setOpacity(e.isAlive() ? 1.0 : 0.2);
            sp.getChildren().add(iv);
        } else {
            Label ph = new Label(e.getName().substring(0,1));
            ph.setStyle("-fx-text-fill:#CC3300; -fx-font-size:50px; -fx-font-weight:bold;");
            sp.getChildren().add(ph);
        }

        Label name = new Label(e.getName());
        name.setStyle("-fx-text-fill:" + (cur?"#FF7755":"#DDAA88") +
            "; -fx-font-family:'Courier New'; -fx-font-size:10px; -fx-font-weight:bold;" +
            "-fx-effect:dropshadow(gaussian,#000,5,1,0,1);");

        card.getChildren().addAll(bar, hpLbl, sp, name);
        if (e.isAlive()) card.setOnMouseClicked(ev -> onTargetClick(e));
        return card;
    }

    // ── ALLY CARD ───────────────────────────────
    private VBox buildAlly(Entity ally) {
        boolean cur = cm.getCurrentActor() != null
            && cm.getCurrentActor().getId().equals(ally.getId());
        double sz = 70;

        VBox card = new VBox(2); card.setAlignment(Pos.BOTTOM_CENTER);
        card.setStyle("-fx-background-color:linear-gradient(to top,#1A120899,transparent);" +
            "-fx-border-color:" + (cur?"#FFB830":"transparent") + "; -fx-border-width:0 0 2 0;" +
            (cur?"-fx-effect:dropshadow(gaussian,#FFB830AA,12,0.4,0,0);":""));

        var sprite = ally instanceof Player
            ? arclightcity.ui.util.AssetManager.spriteAsuna("idle")
            : ally instanceof Mercenary m
                ? arclightcity.ui.util.AssetManager.spriteGuildmate(m.getName(),"idle")
                : null;

        StackPane sp = new StackPane(); sp.setMinSize(sz,sz); sp.setMaxSize(sz,sz);
        if (sprite != null) {
            var iv = arclightcity.ui.util.AssetManager.makeIV(sprite, sz, sz);
            iv.setOpacity(ally.isAlive() ? 1.0 : 0.15);
            sp.getChildren().add(iv);
        }

        double hp = ally.getHpPercent();
        Region hbg = new Region(); hbg.setPrefSize(sz,4);
        hbg.setStyle("-fx-background-color:#1A0808; -fx-background-radius:2;");
        Region hf = new Region(); hf.setPrefSize(sz*hp,4);
        hf.setStyle("-fx-background-color:" + (hp>.5?"#CC3300":hp>.25?"#FF6600":"#FF1100") +
            "; -fx-background-radius:2;");
        StackPane hb = new StackPane(); hb.setAlignment(Pos.CENTER_LEFT);
        hb.getChildren().addAll(hbg, hf);

        String dn = ally instanceof Mercenary m2
            ? m2.getMercenaryType().displayName : ally.getName();
        int lv = ally instanceof Mercenary m3 ? m3.getLoyaltyLevel()
               : ally instanceof Player p ? p.getLevel() : 1;
        Label nm = new Label(dn.toUpperCase() + " LV." + lv);
        nm.setStyle("-fx-text-fill:" + (cur?"#FFB830":"#AA9060") +
            "; -fx-font-family:'Courier New'; -fx-font-size:8px; -fx-font-weight:bold;" +
            "-fx-effect:dropshadow(gaussian,#000,3,1,0,1);");

        if (!ally.isAlive()) {
            Label d = new Label("\u2020");
            d.setStyle("-fx-text-fill:#660000; -fx-font-size:20px;");
            card.getChildren().addAll(sp, d);
        } else {
            card.getChildren().addAll(sp, hb, nm);
        }
        return card;
    }

    // ── REFRESH ──────────────────────────────────
    private void refresh() {
        if (cm == null) return;
        enemyRow.getChildren().clear();
        for (Entity e : cm.getAllEnemies())
            enemyRow.getChildren().add(buildEnemy(e));
        allyRow.getChildren().clear();
        for (Entity a : cm.getAllAllies())
            allyRow.getChildren().add(buildAlly(a));
        Entity actor = cm.getCurrentActor();
        if (turnLabel != null) turnLabel.setText("Giliran " + cm.getTotalTurns());
        if (actorLabel != null && actor != null)
            actorLabel.setText(actor.getName().toUpperCase() + " \u25ba");
    }

    // ── ACTION PANEL ────────────────────────────
    public void refreshActions() {
        if (actionPanel == null) return;
        actionPanel.getChildren().clear();
        if (cm == null || !cm.isCombatActive()) return;
        Entity actor = cm.getCurrentActor();
        if (actor == null) return;
        if (actor instanceof Player) {
            actionPanel.getChildren().add(buildSkills());
            actionPanel.getChildren().add(buildButtons());
        } else {
            Label ai = new Label("\u27f3  " + actor.getName() + " bertindak...");
            ai.setStyle("-fx-text-fill:#8A7060; -fx-font-family:'Courier New';" +
                       "-fx-font-size:12px; -fx-padding:10 0;");
            actionPanel.getChildren().add(ai);
        }
    }

    private HBox buildSkills() {
        HBox bar = new HBox(8); bar.setAlignment(Pos.CENTER); bar.setPadding(new Insets(2,8,2,8));
        Player pl = engine.getPlayer(); if (pl == null) return bar;
        List<String> eq = pl.getEquippedSkillIds();
        for (int i = 0; i < 4; i++) {
            String sid = i < eq.size() ? eq.get(i) : null;
            int cd = sid != null ? pl.getSkillCooldown(sid) : 0;
            double mc = sid != null ? arclightcity.combat.SkillExecutor.getMpCost(sid) : 0;
            boolean rdy = sid != null && cd == 0 && pl.getCurrentMp() >= mc;

            VBox slot = new VBox(2); slot.setAlignment(Pos.CENTER); slot.setPrefSize(106,56);
            slot.setStyle(rdy
                ? "-fx-background-color:#C8860A11; -fx-border-color:#C8860A66; -fx-border-width:1 1 2 1; -fx-cursor:hand;"
                : sid!=null ? "-fx-background-color:#3A281011; -fx-border-color:#3A2810; -fx-border-width:1;"
                            : "-fx-background-color:#150E08; -fx-border-color:#2A1808; -fx-border-width:1;");

            if (sid != null) {
                var icon = arclightcity.ui.util.AssetManager.iconSkill(sid);
                if (icon != null) {
                    var iv = arclightcity.ui.util.AssetManager.makeIV(icon,24,24);
                    iv.setOpacity(rdy?1.0:0.35); slot.getChildren().add(iv);
                }
            }
            Label nm = new Label(sid!=null ? skillName(sid) : "EMPTY");
            nm.setStyle("-fx-text-fill:" + (rdy?UIFactory.CYAN:sid!=null?"#6A5840":"#3A2810") +
                "; -fx-font-family:'Courier New'; -fx-font-size:9px; -fx-font-weight:bold;");
            Label cdl = new Label(cd>0?"CD:"+cd:sid!=null?"READY":"");
            cdl.setStyle("-fx-text-fill:" + (cd>0?UIFactory.ORANGE:"#2D7A4566") +
                "; -fx-font-family:'Courier New'; -fx-font-size:9px;");
            slot.getChildren().addAll(nm, cdl);
            if (rdy) { final String fs=sid; slot.setOnMouseClicked(e->useSkill(fs)); }
            bar.getChildren().add(slot);
        }
        // MP bar
        double mpPct = Math.min(1, pl.getCurrentMp() / Math.max(1, pl.getStats().get(StatType.MAX_MP)));
        Region mb = new Region(); mb.setPrefSize(80,4);
        mb.setStyle("-fx-background-color:#0A0816; -fx-background-radius:2;");
        Region mf = new Region(); mf.setPrefSize(80*mpPct,4);
        mf.setStyle("-fx-background-color:#4455CC; -fx-background-radius:2;");
        StackPane mp = new StackPane(); mp.setAlignment(Pos.CENTER_LEFT); mp.getChildren().addAll(mb,mf);
        Label ml = new Label("MP "+(int)pl.getCurrentMp()+"/"+(int)pl.getStats().get(StatType.MAX_MP));
        ml.setStyle("-fx-text-fill:#5566AA; -fx-font-family:'Courier New'; -fx-font-size:9px;");
        VBox mb2 = new VBox(2,mp,ml); mb2.setAlignment(Pos.CENTER);
        bar.getChildren().add(mb2);
        return bar;
    }

    private HBox buildButtons() {
        HBox r = new HBox(8); r.setAlignment(Pos.CENTER); r.setPadding(new Insets(4,8,2,8));
        Button serang = btn("\u2717  SERANG",   "#CC3300","#CC330022");
        Button jurus  = btn("\u2605  JURUS",    "#FFB830","#C8860A22");
        Button item   = btn("\u2261  ITEM",     "#2D7A45","#2D7A4522");
        Button tahan  = btn("\u21ba  BERTAHAN", "#7755BB","#7755BB22");
        Button kabur2 = btn("\u2190  KABUR",    "#5A3A10","transparent");
        serang.setOnAction(e -> attack());
        jurus.setOnAction(e  -> showSkillMenu());
        item.setOnAction(e   -> showItemMenu());
        tahan.setOnAction(e  -> defend());
        kabur2.setOnAction(e -> doFlee());
        r.getChildren().addAll(serang,jurus,item,tahan,kabur2);
        return r;
    }

    private Button btn(String txt, String tc, String bg) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:"+bg+"; -fx-border-color:"+tc+"55;" +
            "-fx-border-width:1 1 3 1; -fx-text-fill:"+tc+";" +
            "-fx-font-family:'Courier New'; -fx-font-size:12px; -fx-font-weight:bold;" +
            "-fx-padding:8 14; -fx-cursor:hand; -fx-min-width:100;" +
            "-fx-effect:dropshadow(gaussian,"+tc+",5,0.2,0,0);");
        b.setOnMouseEntered(e->b.setOpacity(0.8)); b.setOnMouseExited(e->b.setOpacity(1.0));
        return b;
    }

    // ── COMBAT LOOP ─────────────────────────────
    public void startCombatLoop() { startLoop(); }

    private void startLoop() {
        if (combatLoop != null) combatLoop.stop();
        combatLoop = new Timeline(new KeyFrame(Duration.millis(speedMs), e -> {
            if (cm != null && cm.isCombatActive() && !cm.isWaitingForPlayer()) {
                cm.processTurn();
                Platform.runLater(() -> { refresh(); refreshActions(); });
            }
        }));
        combatLoop.setCycleCount(Animation.INDEFINITE);
        combatLoop.play();
    }

    // ── PLAYER ACTIONS ──────────────────────────
    private void attack() {
        if (!ready()) return;
        Entity t = cm.getLivingEnemies().stream().findFirst().orElse(null);
        if (t == null) return;
        cm.submitPlayerAction(CombatAction.basicAttack(List.of(t.getId())));
        Platform.runLater(() -> { refresh(); refreshActions(); });
    }

    private void useSkill(String sid) {
        if (!ready()) return;
        Entity t = cm.getLivingEnemies().stream().findFirst().orElse(null);
        if (t == null) return;
        cm.submitPlayerAction(CombatAction.useSkill(sid, List.of(t.getId())));
        Platform.runLater(() -> { refresh(); refreshActions(); });
    }

    private void defend() {
        if (!ready()) return;
        cm.submitPlayerAction(CombatAction.defend());
        Platform.runLater(() -> { refresh(); refreshActions(); });
    }

    private void doFlee() { if (cm!=null) cm.attemptFlee(); }

    private boolean ready() { return cm != null && cm.isWaitingForPlayer(); }

    private void onTargetClick(Entity e) {
        // Klik enemy = basic attack ke target itu
        if (!ready()) return;
        cm.submitPlayerAction(CombatAction.basicAttack(List.of(e.getId())));
        Platform.runLater(() -> { refresh(); refreshActions(); });
    }

    // ── MENUS ───────────────────────────────────
    private void showSkillMenu() {
        actionPanel.getChildren().clear();
        Label title = new Label("\u2605 PILIH JURUS");
        title.setStyle("-fx-text-fill:#C8860A; -fx-font-family:'Courier New';" +
                      "-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:4 0;");
        VBox menu = new VBox(2); menu.setPadding(new Insets(2));
        Player pl = engine.getPlayer();
        if (pl != null) for (String sid : pl.getEquippedSkillIds()) {
            int cd=pl.getSkillCooldown(sid);
            double mc=arclightcity.combat.SkillExecutor.getMpCost(sid);
            boolean ok=cd==0 && pl.getCurrentMp()>=mc;
            HBox row=new HBox(10); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(5,12,5,12));
            row.setStyle(ok?"-fx-background-color:#1A1208; -fx-border-color:#3A2810; -fx-border-width:0 0 1 0; -fx-cursor:hand;"
                           :"-fx-background-color:#0F0A06; -fx-border-color:#2A1808; -fx-border-width:0 0 1 0; -fx-opacity:0.5;");
            var icon=arclightcity.ui.util.AssetManager.iconSkill(sid);
            if(icon!=null) row.getChildren().add(arclightcity.ui.util.AssetManager.makeIV(icon,20,20));
            Label nm=new Label("\u25ba "+skillName(sid));
            nm.setStyle("-fx-text-fill:"+(ok?"#EDE0C8":"#5A3A10")+"; -fx-font-family:'Courier New'; -fx-font-size:11px;");
            HBox.setHgrow(nm,Priority.ALWAYS);
            Label info=new Label("MP:"+(int)mc+(cd>0?"  CD:"+cd:""));
            info.setStyle("-fx-text-fill:"+(ok?"#4477AA":"#5A3A10")+"; -fx-font-family:'Courier New'; -fx-font-size:10px;");
            row.getChildren().addAll(nm,info);
            if(ok){final String fs=sid; row.setOnMouseClicked(e->{useSkill(fs); refreshActions();});}
            menu.getChildren().add(row);
        }
        Button cancel=UIFactory.btnGold("\u2717 CANCEL"); cancel.setOnAction(e->refreshActions());
        actionPanel.getChildren().addAll(title,menu,cancel);
    }

    private void showItemMenu() {
        actionPanel.getChildren().clear();
        Label title=new Label("\u2261 GUNAKAN ITEM");
        title.setStyle("-fx-text-fill:#2D7A45; -fx-font-family:'Courier New';" +
                      "-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:4 0;");
        VBox menu=new VBox(2); menu.setPadding(new Insets(2));
        for (var it : engine.getInventory().getAllBagItems()) {
            if (!(it instanceof arclightcity.item.Consumable c)) continue;
            HBox row=new HBox(10); row.setAlignment(Pos.CENTER_LEFT); row.setPadding(new Insets(5,12,5,12));
            row.setStyle("-fx-background-color:#1A1208; -fx-border-color:#3A2810; -fx-border-width:0 0 1 0; -fx-cursor:hand;");
            Label nm=new Label("\u25ba "+c.getName()+" \u00d7"+c.getStackCount());
            nm.setStyle("-fx-text-fill:#EDE0C8; -fx-font-family:'Courier New'; -fx-font-size:11px;");
            HBox.setHgrow(nm,Priority.ALWAYS);
            Label eff=new Label("+"+(int)c.getEffectValue()+" HP");
            eff.setStyle("-fx-text-fill:#44AA66; -fx-font-family:'Courier New'; -fx-font-size:10px;");
            row.getChildren().addAll(nm,eff);
            row.setOnMouseClicked(e->{
                if(!ready()) return;
                cm.submitPlayerAction(CombatAction.useItem(c.getId(),List.of(engine.getPlayer().getId())));
                refreshActions();
            });
            menu.getChildren().add(row);
        }
        if(menu.getChildren().isEmpty()) {
            Label none=new Label("Tidak ada item"); none.setStyle("-fx-text-fill:#5A3A10; -fx-font-family:'Courier New'; -fx-padding:6;");
            menu.getChildren().add(none);
        }
        Button cancel=UIFactory.btnGold("\u2717 CANCEL"); cancel.setOnAction(e->refreshActions());
        actionPanel.getChildren().addAll(title,menu,cancel);
    }

    // ── EVENTS ──────────────────────────────────
    private void onEvent(CombatEvent ev) {
        if (ev==null) return;
        spawnDamage(ev);
        refresh(); refreshActions();
    }

    private void onEnd(CombatResult res) {
        if (combatLoop!=null) combatLoop.stop();
        if (res.isVictory())     router.showVictory(res);
        else if (res.isDefeat()) router.showGameOver();
        else                     router.showDungeonMap();
    }

    // ── FLOATING DAMAGE ─────────────────────────
    private void spawnDamage(CombatEvent ev) {
        if (floatCanvas==null) return;
        String msg=ev.getMessage(); if(msg==null) return;
        double x=120+Math.random()*480, y=floatCanvas.getHeight()*0.55+Math.random()*30;
        String text=null, color=null;
        switch(ev.getType()) {
            case CRITICAL_HIT  -> {text="\u26a1 "+(int)num(msg)+"!"; color="#FFD700";}
            case DAMAGE_DEALT  -> {
                double d=num(msg); if(d<=0) return;
                text="-"+(int)d;
                color=msg.contains("Fisik")?"#FF6644":msg.contains("Cyber")?"#44AAFF":"#FF8844";
            }
            case DAMAGE_EVADED  -> {text="HINDAR"; color="#88CCFF";}
            case DAMAGE_BLOCKED -> {text="\ud83d\udee1 "+(int)num(msg); color="#6699CC";}
            case HEAL_RECEIVED  -> {text="+"+(int)num(msg)+" HP"; color="#44FF88"; y*=0.7;}
            case EFFECT_TICK    -> {text="\u2620 "+(int)num(msg); color="#AA4400";}
            case SKILL_USED     -> {text="\u2726 "+last(msg); color="#FFB830"; y*=0.5;}
            case ENTITY_DIED    -> {text="\ud83d\udc80"; color="#CC3300";}
            default -> {return;}
        }
        if(text!=null) {
            floats.add(new FloatingText(text,color,x,y,System.currentTimeMillis()));
            if(floatLoop==null||floatLoop.getStatus()!=Animation.Status.RUNNING) {
                floatLoop=new Timeline(new KeyFrame(Duration.millis(30),e->drawFloats()));
                floatLoop.setCycleCount(Animation.INDEFINITE); floatLoop.play();
            }
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
                boolean big=f.text().charAt(0)==0x26A1||f.text().charAt(0)==0x2726;
                gc.setFont(Font.font("Courier New",
                    big?javafx.scene.text.FontWeight.BOLD:javafx.scene.text.FontWeight.NORMAL,
                    big?22:16));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setFill(Color.web("#000000",alpha*0.7));
                gc.fillText(f.text(),f.x()+2,f.y()-el*f.rise()+2);
                gc.setFill(Color.web(f.color(),alpha));
                gc.fillText(f.text(),f.x(),f.y()-el*f.rise());
            } catch(Exception ignored){}
        }
        if(floats.isEmpty()&&floatLoop!=null){floatLoop.stop();floatLoop=null;}
    }

    // ── HELPERS ─────────────────────────────────
    private double num(String msg) {
        try {
            var m=java.util.regex.Pattern.compile("([0-9]+[.]?[0-9]*)").matcher(msg);
            if(m.find()) return Double.parseDouble(m.group(1));
        } catch(Exception e){}
        return 0;
    }
    private String last(String msg) {
        int i=msg==null?-1:msg.lastIndexOf(" ");
        return i>0?msg.substring(i+1):"Skill";
    }
    private String skillName(String sid) {
        if(sid==null) return "EMPTY";
        return switch(sid) {
            case "POWER_STRIKE"       -> "PUKULAN HARIMAU";
            case "EXECUTE"            -> "TEBASAN PAMUNGKAS";
            case "PHANTOM_SHOT"       -> "PANAH BAYANGAN";
            case "SHADOW_STEP"        -> "LANGKAH GAIB";
            case "DEEP_HACK"          -> "BIDANG BAYANGAN";
            case "NULL_FIELD"         -> "PROTOKOL NOL";
            case "SOVEREIGN_STRIKE"   -> "TEBASAN AGUNG";
            case "NULL_PROTOCOL"      -> "NULL PROTOCOL";
            case "DATA_FRAGMENTATION" -> "PECAH JIWA";
            case "ENERGY_DRAIN"       -> "SERAP TENAGA";
            case "IRON_SHIELD"        -> "TAMENG BAJA";
            case "SEISMIC_SLAM"       -> "GEMPA BUMI";
            case "SACRED_SEAL"        -> "RAJAH PELINDUNG";
            default -> sid.replace("_"," ");
        };
    }

    // Legacy alias
    public void refreshActionPanel() { refreshActions(); }
}
