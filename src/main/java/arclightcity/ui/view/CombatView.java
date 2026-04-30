package arclightcity.ui.view;
import arclightcity.entity.base.Entity;
import arclightcity.entity.stats.StatType;
import arclightcity.combat.*;
import arclightcity.engine.GameEngine;
import arclightcity.entity.enemy.Enemy;
import arclightcity.entity.mercenary.Mercenary;
import arclightcity.entity.player.Player;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.view.MercenaryDialogue;
import arclightcity.ui.util.UIFactory;
import java.util.List;
import java.util.ArrayList;

import java.util.List;

/**
 * CombatView — layar combat utama.
 *
 * Layout:
 *   ┌──────────────────────────┐
 *   │ ESCAPE      [TURN X]     │
 *   │ [combat log scroll]      │
 *   ├──────────────────────────┤
 *   │ ENEMIES                  │
 *   │ [enemy cards: name+bars] │
 *   ├──────────────────────────┤
 *   │ ALLIES                   │
 *   │ [player] [merc1] [merc2] │
 *   ├──────────────────────────┤
 *   │ STATUS EFFECTS           │
 *   ├──────────────────────────┤
 *   │ [ATTACK] [SKILL] [ITEM]  │
 *   │ [DEFEND]         [FLEE]  │
 *   └──────────────────────────┘
 */
public class CombatView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private CombatManager     cm;

    // UI refs
    private VBox       enemyContainer;
    private HBox       allyContainer;
    private VBox       logContainer;
    private ScrollPane logScroll;
    private VBox       actionPanel;
    private Label      turnLabel;
    private Label      currentActorLabel;
    private HBox       turnOrderBar;     // v0.3: antrian giliran
    private Timeline   combatLoop;

    // v0.3: AI turn guard
    private boolean aiTurnPending = false;

    // v0.3: Target Selection mode
    private boolean          targetSelectMode = false;
    private String           pendingSkillId   = null;
    private java.util.function.Consumer<Entity> onTargetSelected = null;

    // v0.3: Combat speed — default 1200ms agar player sempat baca log
    // 1× = 1200ms (comfortable), 2× = 500ms (faster), SKIP = 50ms
    private int combatSpeedMs = 1200;

    // v0.3: Floating damage texts
    private record FloatingText(String text, String color,
                                double x, double y, long startMs) {}
    private final java.util.List<FloatingText> floatingTexts
            = new java.util.ArrayList<>();

    public CombatView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
        this.cm     = engine.getActiveCombat();
    }

    public Parent build() {
        // BorderPane: top=fixed headers, center=scrollable battle area, bottom=action panel
        BorderPane root = UIFactory.screenRootBorder();

        // ── TOP: TopBar + TurnOrderBar (fixed) ───────────
        VBox topSection = new VBox(0);
        topSection.getChildren().add(buildTopBar());

        turnOrderBar = new HBox(6);
        turnOrderBar.setPadding(new Insets(5, 12, 5, 12));
        turnOrderBar.setMinHeight(36);
        turnOrderBar.setMaxHeight(36);
        turnOrderBar.setAlignment(Pos.CENTER_LEFT);
        turnOrderBar.setStyle(
            "-fx-background-color: #080D18;" +
            "-fx-border-color: #1C2E44;" +
            "-fx-border-width: 0 0 1 0;"
        );
        topSection.getChildren().add(turnOrderBar);
        root.setTop(topSection);

        // ── CENTER: scrollable battle area ───────────────
        VBox battleArea = new VBox(0);

        // Combat log
        battleArea.getChildren().add(buildCombatLog());

        // Enemy area
        VBox enemySection = new VBox(6);
        enemySection.setPadding(new Insets(6, 12, 4, 12));
        enemySection.setStyle("-fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");
        Label enemySectionTitle = UIFactory.sectionTitle("◆ ENEMIES");
        enemySectionTitle.setPadding(new Insets(0, 0, 4, 0));
        enemyContainer = new VBox(6);
        enemySection.getChildren().addAll(enemySectionTitle, enemyContainer);
        battleArea.getChildren().add(enemySection);

        // Ally area
        VBox allySection = new VBox(6);
        allySection.setPadding(new Insets(6, 12, 4, 12));
        allySection.setStyle("-fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");
        Label allySectionTitle = UIFactory.sectionTitle("◈ ALLIES");
        allySectionTitle.setPadding(new Insets(0, 0, 4, 0));
        allyContainer = new HBox(8);
        allySection.getChildren().addAll(allySectionTitle, allyContainer);
        battleArea.getChildren().add(allySection);

        // Status effects
        battleArea.getChildren().add(buildStatusPanel());

        // Wrap dalam ScrollPane agar battle area bisa discroll jika konten panjang
        ScrollPane battleScroll = new ScrollPane(battleArea);
        battleScroll.setFitToWidth(true);
        battleScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        battleScroll.setStyle(
            "-fx-background-color: #050810;" +
            "-fx-background: #050810;" +
            "-fx-border-color: transparent;"
        );
        root.setCenter(battleScroll);

        // ── BOTTOM: action panel (selalu terlihat) ────────
        actionPanel = new VBox(8);
        actionPanel.setPadding(new Insets(8, 12, 10, 12));
        actionPanel.setStyle(
            "-fx-background-color: #0C1220;" +
            "-fx-border-color: #1C2E44;" +
            "-fx-border-width: 1 0 0 0;"
        );
        root.setBottom(actionPanel);

        // Initial render
        refreshCombatants();
        refreshActionPanel();

        // Subscribe event listener
        if (cm != null) {
            cm.addEventListener(event -> Platform.runLater(() -> handleCombatEvent(event)));
            cm.addResultListener(result -> Platform.runLater(() -> handleCombatEnd(result)));
        }

        UIFactory.fadeIn(root, 300);
        return root;
    }

    // ════════════════════════════════════════════════════
    // TOP BAR
    // ════════════════════════════════════════════════════

    private HBox buildTopBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 12, 8, 12));
        bar.setStyle("-fx-background-color: #0C1220; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        Button escapeBtn = new Button("ESCAPE");
        escapeBtn.setStyle(
            "-fx-background-color: transparent; -fx-border-color: #FF1744; -fx-border-width: 1;" +
            "-fx-text-fill: #FF1744; -fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
            "-fx-padding: 4 10; -fx-cursor: hand;"
        );
        escapeBtn.setOnAction(e -> {
            boolean fled = engine.attemptFlee();
            if (!fled) addLog("❌ Flee failed!", "#FF6B6B");
        });

        turnLabel = new Label("TURN 0");
        turnLabel.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        HBox.setHgrow(turnLabel, Priority.ALWAYS);
        turnLabel.setAlignment(Pos.CENTER);

        currentActorLabel = new Label("—");
        currentActorLabel.setStyle(
            "-fx-text-fill: #00E5FF; -fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;"
        );

        bar.getChildren().addAll(escapeBtn, turnLabel, currentActorLabel);
        return bar;
    }

    // ════════════════════════════════════════════════════
    // COMBAT LOG
    // ════════════════════════════════════════════════════

    private VBox buildCombatLog() {
        VBox wrapper = new VBox();
        wrapper.setPrefHeight(150);  // naik dari 130 → memanfaatkan +100px
        wrapper.setMaxHeight(150);
        wrapper.setStyle("-fx-background-color: #050810; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        logContainer = new VBox(2);
        logContainer.setPadding(new Insets(6, 10, 6, 10));

        logScroll = new ScrollPane(logContainer);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(150);
        logScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        logScroll.setStyle("-fx-background-color: #050810; -fx-background: #050810; -fx-border-color: transparent;");

        wrapper.getChildren().add(logScroll);
        return wrapper;
    }

    public void addLog(String message, String color) {
        Label entry = new Label("> " + message);
        entry.setWrapText(true);
        entry.setStyle(
            "-fx-text-fill: " + color + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 12px;"   // naik dari 10px
        );
        logContainer.getChildren().add(entry);

        // Keep only last 50 entries
        if (logContainer.getChildren().size() > 50) {
            logContainer.getChildren().remove(0);
        }

        // Auto-scroll to bottom
        Platform.runLater(() -> logScroll.setVvalue(1.0));
    }

    // ════════════════════════════════════════════════════
    // STATUS PANEL
    // ════════════════════════════════════════════════════

    private HBox buildStatusPanel() {
        HBox panel = new HBox(4);
        panel.setPadding(new Insets(6, 12, 6, 12));
        panel.setStyle("-fx-background-color: #080D18; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");
        panel.setAlignment(Pos.CENTER_LEFT);

        Player player = engine.getPlayer();
        for (var effect : player.getActiveEffects()) {
            panel.getChildren().add(UIFactory.effectBadge(effect));
        }

        if (panel.getChildren().isEmpty()) {
            Label none = new Label("No active effects");
            none.setStyle("-fx-text-fill: #2A3A50; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
            panel.getChildren().add(none);
        }

        return panel;
    }

    // ════════════════════════════════════════════════════
    // ENTITY CARDS
    // ════════════════════════════════════════════════════

    private void refreshCombatants() {
        if (cm == null) return;

        // Enemies
        enemyContainer.getChildren().clear();
        for (Entity enemy : cm.getAllEnemies()) {
            enemyContainer.getChildren().add(buildEnemyCard(enemy));
        }

        // Allies
        allyContainer.getChildren().clear();
        for (Entity ally : cm.getAllAllies()) {
            allyContainer.getChildren().add(buildAllyCard(ally));
        }

        // Update turn info
        if (turnLabel != null) {
            turnLabel.setText("TURN " + cm.getTotalTurns());
        }
        Entity actor = cm.getCurrentActor();
        if (currentActorLabel != null && actor != null) {
            currentActorLabel.setText(actor.getName().toUpperCase() + " ▶");
        }
    }

    private VBox buildEnemyCard(Entity enemy) {
        VBox card = new VBox(4);  // spacing 6→4
        card.setPadding(new Insets(8, 12, 8, 12));  // 10→8 atas/bawah
        card.setCursor(javafx.scene.Cursor.HAND);

        boolean isCurrentActor = cm.getCurrentActor() != null
                && cm.getCurrentActor().getId().equals(enemy.getId());

        String borderColor = isCurrentActor ? UIFactory.RED : "#2A3A50";
        card.setStyle(
            "-fx-background-color: " + (isCurrentActor ? "#1C0808" : "#100808") + ";" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: " + (isCurrentActor ? "2" : "1") + " " +
                                   (isCurrentActor ? "2" : "1") + " " +
                                   (isCurrentActor ? "2" : "1") + " 3;" +
            (isCurrentActor ? "-fx-effect: dropshadow(gaussian, #FF1744, 10, 0.4, 0, 0);" : "")
        );

        // Name + badges row
        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        String nameColor = enemy.isAlive() ? UIFactory.RED : "#5A6A80";
        Label nameLabel = new Label(enemy.getName());
        nameLabel.setStyle("-fx-text-fill: " + nameColor +
                "; -fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-font-weight: bold;");

        if (enemy instanceof Enemy e) {
            Label levelBadge = new Label("Lv." + e.getFloorLevel());
            levelBadge.setStyle("-fx-text-fill: #8899AA; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            Label typeBadge = new Label(e.getRace().displayName);
            typeBadge.setStyle(
                "-fx-background-color: #1C2E4488; -fx-text-fill: #8899AA;" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-padding: 2 6;"
            );
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            nameRow.getChildren().addAll(nameLabel, levelBadge, typeBadge);
        } else {
            nameRow.getChildren().add(nameLabel);
        }

        // Target select mode: highlight style + konten tetap ditampilkan
        if (targetSelectMode && enemy.isAlive()) {
            card.setStyle(
                "-fx-background-color: #FFD60011;" +
                "-fx-border-color: #FFD600;" +
                "-fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, #FFD600, 12, 0.4, 0, 0);" +
                "-fx-cursor: hand;"
            );
            card.setOnMouseClicked(e -> handleTargetSelect(enemy));
            card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #FFD60022;" +
                "-fx-border-color: #FFD600;" +
                "-fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, #FFD600, 18, 0.6, 0, 0);" +
                "-fx-cursor: hand;"
            ));
            card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: #FFD60011;" +
                "-fx-border-color: #FFD600;" +
                "-fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, #FFD600, 12, 0.4, 0, 0);" +
                "-fx-cursor: hand;"
            ));

            // Tambah konten: nama + bars tetap tampil
            VBox bars = UIFactory.compactVitalBars(
                    enemy.getCurrentHp(),     enemy.getStats().get(StatType.MAX_HP),
                    enemy.getCurrentShield(), enemy.getStats().get(StatType.MAX_SHIELD),
                    enemy.getCurrentMp(),     enemy.getStats().get(StatType.MAX_MP));

            // Label TARGET di atas nama
            Label targetHint = new Label("▶ SELECT TARGET");
            targetHint.setStyle(
                "-fx-text-fill: #FFD600; -fx-font-family: 'Courier New';" +
                "-fx-font-size: 9px; -fx-font-weight: bold;"
            );
            card.getChildren().addAll(targetHint, nameRow, bars);
            return card;
        }

        // Defeated overlay
        if (!enemy.isAlive()) {
            card.setOpacity(0.3);
            Label deadLabel = new Label("✕ DEFEATED");
            deadLabel.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            card.getChildren().addAll(nameRow, deadLabel);
            return card;
        }

        // Vital bars — lebih tebal
        VBox bars = UIFactory.compactVitalBars(
                enemy.getCurrentHp(),    enemy.getStats().get(StatType.MAX_HP),
                enemy.getCurrentShield(),enemy.getStats().get(StatType.MAX_SHIELD),
                enemy.getCurrentMp(),    enemy.getStats().get(StatType.MAX_MP));

        // Status effects
        HBox effects = new HBox(4);
        enemy.getActiveEffects().forEach(ef -> effects.getChildren().add(UIFactory.effectBadge(ef)));

        card.getChildren().addAll(nameRow, bars, effects);
        card.setOnMouseClicked(e -> handleTargetSelect(enemy));
        return card;
    }

    private VBox buildAllyCard(Entity ally) {
        VBox card = new VBox(4);  // 6→4
        card.setPadding(new Insets(8, 12, 8, 12));  // 10→8
        card.setPrefWidth(150);
        card.setMaxWidth(180);

        boolean isCurrentActor = cm.getCurrentActor() != null
                && cm.getCurrentActor().getId().equals(ally.getId());
        boolean isPlayer = ally instanceof Player;

        String borderColor = isCurrentActor ? UIFactory.CYAN
                : isPlayer ? "#00E5FF55" : "#1C2E44";
        card.setStyle(
            "-fx-background-color: #080D18;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: " + (isCurrentActor ? "2" : "1") + " " +
                                   (isCurrentActor ? "2" : "1") + " " +
                                   (isCurrentActor ? "2" : "1") + " 3;" +
            (isCurrentActor ? "-fx-effect: dropshadow(gaussian, #00E5FF, 10, 0.4, 0, 0);" : "")
        );

        // Name
        String nameStr = isPlayer
                ? ally.getName()
                : (ally instanceof Mercenary m ? m.getMercenaryType().displayName : ally.getName());
        Label nameLabel = new Label(nameStr.toUpperCase());
        String nameColor = !ally.isAlive() ? "#5A6A80" : isPlayer ? UIFactory.CYAN : UIFactory.TEXT;
        nameLabel.setStyle("-fx-text-fill: " + nameColor +
                "; -fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);

        // Role badge
        if (!isPlayer && ally instanceof Mercenary m) {
            Label roleBadge = new Label("[" + m.getRole().name() + "]");
            roleBadge.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

            // Vital bars
            VBox bars = UIFactory.compactVitalBars(
                    ally.getCurrentHp(),     ally.getStats().get(StatType.MAX_HP),
                    ally.getCurrentShield(), ally.getStats().get(StatType.MAX_SHIELD),
                    ally.getCurrentMp(),     ally.getStats().get(StatType.MAX_MP));

            HBox effects = new HBox(3);
            ally.getActiveEffects().stream().limit(3)
                    .forEach(ef -> effects.getChildren().add(UIFactory.effectBadge(ef)));

            if (!ally.isAlive()) {
                card.setOpacity(0.3);
                card.getChildren().addAll(nameLabel, new Label("✕"));
                return card;
            }
            card.getChildren().addAll(nameLabel, roleBadge, bars, effects);
        } else {
            // Vital bars
            VBox bars = UIFactory.compactVitalBars(
                    ally.getCurrentHp(),     ally.getStats().get(StatType.MAX_HP),
                    ally.getCurrentShield(), ally.getStats().get(StatType.MAX_SHIELD),
                    ally.getCurrentMp(),     ally.getStats().get(StatType.MAX_MP));

            HBox effects = new HBox(3);
            ally.getActiveEffects().stream().limit(3)
                    .forEach(ef -> effects.getChildren().add(UIFactory.effectBadge(ef)));

            if (!ally.isAlive()) {
                card.setOpacity(0.3);
                card.getChildren().addAll(nameLabel, new Label("✕"));
                return card;
            }
            card.getChildren().addAll(nameLabel, bars, effects);
        }

        return card;
    }

    // ════════════════════════════════════════════════════
    // ACTION PANEL
    // ════════════════════════════════════════════════════

    private void refreshActionPanel() {
        actionPanel.getChildren().clear();

        if (cm == null || !cm.isCombatActive()) return;

        // Target select mode indicator
        if (targetSelectMode) {
            HBox hint = new HBox(8);
            hint.setAlignment(Pos.CENTER);
            Label hintLabel = new Label("▶ Click an enemy to target  ");
            hintLabel.setStyle("-fx-text-fill: #FFD600; -fx-font-family: 'Courier New';" +
                               "-fx-font-size: 12px; -fx-font-weight: bold;");
            Button cancelTarget = new Button("✕ CANCEL");
            cancelTarget.setStyle("-fx-background-color: transparent; -fx-border-color: #5A6A80;" +
                                  "-fx-border-width: 1; -fx-text-fill: #5A6A80;" +
                                  "-fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-cursor: hand;");
            cancelTarget.setOnAction(e -> {
                targetSelectMode = false;
                pendingSkillId   = null;
                refreshCombatants();
                refreshActionPanel();
            });
            hint.getChildren().addAll(hintLabel, cancelTarget);
            actionPanel.getChildren().add(hint);
            return;
        }

        if (!cm.isWaitingForPlayer()) {
            HBox processingRow = new HBox(8);
            processingRow.setAlignment(Pos.CENTER);
            Label waiting = new Label("⟳ Processing...");
            waiting.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            processingRow.getChildren().add(waiting);

            // Speed control tetap tampil saat AI turn
            processingRow.getChildren().add(buildSpeedControl());
            actionPanel.getChildren().add(processingRow);
            return;
        }

        // Speed control di atas action buttons
        HBox speedRow = new HBox();
        speedRow.setAlignment(Pos.CENTER_RIGHT);
        speedRow.getChildren().add(buildSpeedControl());
        actionPanel.getChildren().add(speedRow);

        // Row 1: Attack + Skill + Item
        HBox row1 = new HBox(8);
        row1.setAlignment(Pos.CENTER);

        // ATTACK — enter target select mode
        Button attackBtn = buildActionBtn("⚔ ATTACK", UIFactory.RED, () -> {
            addLog("▶ Click an enemy to attack", UIFactory.YELLOW);
            targetSelectMode = true;
            pendingSkillId   = null;
            refreshCombatants();
            refreshActionPanel();
        });

        Button skillBtn = buildActionBtn("★ SKILL", UIFactory.CYAN, () -> showSkillMenu());
        Button itemBtn  = buildActionBtn("⊞ ITEM",  UIFactory.GREEN, () -> showItemMenu());

        row1.getChildren().addAll(attackBtn, skillBtn, itemBtn);

        // Row 2: Defend + Flee
        HBox row2 = new HBox(8);
        row2.setAlignment(Pos.CENTER);

        Button defendBtn = buildActionBtn("🛡 DEFEND", UIFactory.PURPLE, () -> {
            engine.submitCombatAction(CombatAction.defend());
        });

        Button fleeBtn = buildActionBtn("↩ FLEE", "#5A6A80", () -> {
            boolean fled = engine.attemptFlee();
            if (!fled) addLog("Cannot escape!", "#FF6B6B");
        });

        HBox.setHgrow(defendBtn, Priority.ALWAYS);
        HBox.setHgrow(fleeBtn, Priority.ALWAYS);
        row2.getChildren().addAll(defendBtn, fleeBtn);

        // Equipped skills quick bar
        HBox skillBar = buildSkillQuickBar();

        actionPanel.getChildren().addAll(skillBar, row1, row2);
    }

    private HBox buildSkillQuickBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 0, 6, 0));

        Player player = engine.getPlayer();
        List<String> equipped = player.getEquippedSkillIds();

        for (int i = 0; i < 4; i++) {
            String skillId = i < equipped.size() ? equipped.get(i) : null;
            int cd = skillId != null ? player.getSkillCooldown(skillId) : 0;
            boolean ready = skillId != null && cd == 0;

            VBox slot = new VBox(3);
            slot.setAlignment(Pos.CENTER);
            slot.setPrefSize(110, 52);  // lebih besar dari sebelumnya (72×44)

            String slotStyle;
            if (ready) {
                slotStyle = "-fx-background-color: #00E5FF11; -fx-border-color: #00E5FF55;" +
                            " -fx-border-width: 1 1 1 2; -fx-cursor: hand;";
            } else if (skillId != null) {
                slotStyle = "-fx-background-color: #1C2E4411; -fx-border-color: #1C2E44;" +
                            " -fx-border-width: 1;";
            } else {
                slotStyle = "-fx-background-color: #0C1220; -fx-border-color: #1C2E4433;" +
                            " -fx-border-width: 1;";
            }
            slot.setStyle(slotStyle);

            // Skill name — lebih besar
            String displayName = skillId != null ? getSkillDisplayName(skillId) : "—";
            Label nameLabel = new Label(displayName);
            nameLabel.setStyle(
                "-fx-text-fill: " + (ready ? UIFactory.CYAN : skillId != null ? "#5A6A80" : "#2A3A50") + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-alignment: center; -fx-text-alignment: center;"
            );
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(100);

            // CD / Status
            Label cdLabel = new Label(cd > 0 ? "CD: " + cd + "t" : skillId != null ? "READY" : "EMPTY");
            cdLabel.setStyle(
                "-fx-text-fill: " + (cd > 0 ? UIFactory.ORANGE : skillId != null ? "#00E67688" : "#2A3A50") + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 10px;"
            );

            slot.getChildren().addAll(nameLabel, cdLabel);

            if (ready) {
                final String sid = skillId;
                slot.setOnMouseClicked(e -> submitSkillAction(sid));
                // Hover effect
                slot.setOnMouseEntered(e -> slot.setStyle(
                    "-fx-background-color: #00E5FF22; -fx-border-color: #00E5FF;" +
                    " -fx-border-width: 1 1 1 2; -fx-cursor: hand;"));
                slot.setOnMouseExited(e -> slot.setStyle(slotStyle));
            }

            bar.getChildren().add(slot);
        }

        return bar;
    }

    private Button buildActionBtn(String text, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(130);
        btn.setStyle(
            "-fx-background-color: " + color + "18;" +
            "-fx-border-color: " + color + "77;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 8;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + color + "30;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 8;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, " + color + ", 6, 0.3, 0, 0);"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + "18;" +
            "-fx-border-color: " + color + "77;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 8;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    // ════════════════════════════════════════════════════
    // SKILL / ITEM MENUS
    // ════════════════════════════════════════════════════

    // ════════════════════════════════════════════════════
    // v0.3: SKILL SELECTION POPUP
    // ════════════════════════════════════════════════════

    private void showSkillMenu() {
        Player player = engine.getPlayer();
        List<String> equipped = player.getEquippedSkillIds();
        if (equipped.isEmpty()) {
            addLog("No skills equipped!", UIFactory.ORANGE);
            return;
        }

        // Hapus popup lama jika ada
        actionPanel.getChildren().removeIf(n -> "skill-popup".equals(n.getId()));

        VBox popup = new VBox(6);
        popup.setId("skill-popup");
        popup.setPadding(new Insets(12));
        popup.setStyle(
            "-fx-background-color: #0C1220EE;" +
            "-fx-border-color: #00E5FF55;" +
            "-fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, #00E5FF, 12, 0.2, 0, 0);"
        );

        Label title = new Label("◈ SELECT SKILL");
        title.setStyle("-fx-text-fill: #00E5FF; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 12px; -fx-font-weight: bold;");
        popup.getChildren().add(title);

        for (String skillId : equipped) {
            int cd = player.getSkillCooldown(skillId);
            boolean ready = cd == 0;
            SkillInfo info = getSkillInfo(skillId);

            Button btn = new Button(
                (ready ? "▶ " : "✕ ") + info.name() +
                "   [MP:" + info.mpCost() + "]" +
                (cd > 0 ? "  [CD:" + cd + "t]" : "  READY") +
                (info.isAoe() ? "  [AoE]" : "")
            );
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setDisable(!ready);

            String c = ready ? "#00E5FF" : "#5A6A80";
            btn.setStyle(
                "-fx-background-color: " + (ready ? "#00E5FF11" : "transparent") + ";" +
                "-fx-border-color: " + c + "55;" +
                "-fx-border-width: 1 1 1 3;" +
                "-fx-text-fill: " + c + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                "-fx-padding: 8 10; -fx-cursor: " + (ready ? "hand" : "default") + ";"
            );

            // Tooltip deskripsi
            Tooltip tip = new Tooltip(info.description());
            tip.setStyle("-fx-background-color: #0C1220; -fx-text-fill: #8899AA;" +
                         "-fx-font-family: 'Courier New'; -fx-font-size: 10px;");
            btn.setTooltip(tip);

            if (ready) {
                final String sid = skillId;
                btn.setOnAction(e -> {
                    actionPanel.getChildren().removeIf(n -> "skill-popup".equals(n.getId()));
                    submitSkillAction(sid);
                });
            }
            popup.getChildren().add(btn);
        }

        Button cancel = new Button("✕  CANCEL");
        cancel.setStyle("-fx-background-color: transparent; -fx-border-color: #5A6A8066;" +
                        "-fx-border-width: 1; -fx-text-fill: #5A6A80;" +
                        "-fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-cursor: hand;");
        cancel.setOnAction(e -> actionPanel.getChildren()
                .removeIf(n -> "skill-popup".equals(n.getId())));
        popup.getChildren().add(cancel);

        actionPanel.getChildren().add(0, popup);
    }

    private void submitSkillAction(String skillId) {
        SkillInfo info = getSkillInfo(skillId);
        if (info.isAoe()) {
            List<String> allEnemyIds = cm.getEnemies().stream()
                    .filter(Entity::isAlive).map(Entity::getId).toList();
            engine.submitCombatAction(CombatAction.useSkill(skillId, allEnemyIds));
        } else {
            // Enter target select mode
            addLog("▶ Click an enemy to target", UIFactory.YELLOW);
            pendingSkillId   = skillId;
            targetSelectMode = true;
            refreshCombatants(); // re-render enemy cards dengan highlight
        }
    }

    // ════════════════════════════════════════════════════
    // v0.3: ITEM MENU
    // ════════════════════════════════════════════════════

    private void showItemMenu() {
        var consumables = engine.getInventory().getConsumables();
        if (consumables.isEmpty()) {
            addLog("No items available!", UIFactory.ORANGE);
            return;
        }

        actionPanel.getChildren().removeIf(n -> "item-popup".equals(n.getId()));

        VBox popup = new VBox(6);
        popup.setId("item-popup");
        popup.setPadding(new Insets(12));
        popup.setStyle("-fx-background-color: #0C1220EE; -fx-border-color: #00E67655;" +
                       "-fx-border-width: 1;");

        Label title = new Label("⊞ USE ITEM");
        title.setStyle("-fx-text-fill: #00E676; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 12px; -fx-font-weight: bold;");
        popup.getChildren().add(title);

        for (var cons : consumables) {
            Button btn = new Button("▶  " + cons.getName() +
                    " ×" + cons.getStackCount());
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: #00E67611; -fx-border-color: #00E67655;" +
                         "-fx-border-width: 1 1 1 3; -fx-text-fill: #00E676;" +
                         "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                         "-fx-padding: 8 10; -fx-cursor: hand;");
            btn.setOnAction(e -> {
                boolean used = engine.getInventory().useConsumable(
                        cons.getId(), engine.getPlayer());
                if (used) addLog("Used: " + cons.getName(), UIFactory.GREEN);
                actionPanel.getChildren().removeIf(n -> "item-popup".equals(n.getId()));
                refreshCombatants();
            });
            popup.getChildren().add(btn);
        }

        Button cancel = new Button("✕  CANCEL");
        cancel.setStyle("-fx-background-color: transparent; -fx-border-color: #5A6A8066;" +
                        "-fx-border-width: 1; -fx-text-fill: #5A6A80;" +
                        "-fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-cursor: hand;");
        cancel.setOnAction(e -> actionPanel.getChildren()
                .removeIf(n -> "item-popup".equals(n.getId())));
        popup.getChildren().add(cancel);

        actionPanel.getChildren().add(0, popup);
    }

    // ════════════════════════════════════════════════════
    // v0.3: SKILL INFO DATABASE
    // ════════════════════════════════════════════════════

    private record SkillInfo(String name, String description, int mpCost, boolean isAoe) {}

    private SkillInfo getSkillInfo(String skillId) {
        return switch (skillId) {
            case "POWER_STRIKE"  -> new SkillInfo("Power Strike",
                    "Heavy blow: 1.8× ATK damage.", 15, false);
            case "EXECUTE"       -> new SkillInfo("Execute",
                    "Instant kill if HP < 25%. Else 1.5× damage.", 25, false);
            case "DEEP_HACK"     -> new SkillInfo("Deep Hack",
                    "Cyber + HACK debuff (-30% ATK) for 3 turns.", 20, false);
            case "VIRUS_UPLOAD"  -> new SkillInfo("Virus Upload",
                    "DOT: Cyber damage each turn for 4 turns.", 18, false);
            case "PHANTOM_SHOT"  -> new SkillInfo("Phantom Shot",
                    "High crit chance attack. Enters stealth.", 20, false);
            case "SHADOW_STEP"   -> new SkillInfo("Shadow Step",
                    "2× crit damage. Ignore all armor.", 22, false);
            case "IRON_SHIELD"   -> new SkillInfo("Iron Shield",
                    "BLOCK buff for 2 turns. Reduce damage greatly.", 18, false);
            case "SHOCKWAVE"     -> new SkillInfo("Shockwave",
                    "AoE physical hit to all enemies.", 24, true);
            case "ENERGY_DRAIN"  -> new SkillInfo("Energy Drain",
                    "Drain MP from target, restore own MP.", 10, false);
            case "BIO_IRRADIATE" -> new SkillInfo("Bio Irradiate",
                    "AoE Energy DOT + CORRODE debuff.", 28, true);
            case "EMP_BURST"     -> new SkillInfo("EMP Burst",
                    "AoE Cyber + STUN Android enemies 1 turn.", 30, true);
            case "FIELD_BARRIER" -> new SkillInfo("Field Barrier",
                    "Shield barrier on all allies.", 25, true);
            default              -> new SkillInfo(skillId.replace("_", " "),
                    "Special skill.", 15, false);
        };
    }

    // ════════════════════════════════════════════════════
    // EVENT HANDLERS
    // ════════════════════════════════════════════════════

    private void handleCombatEvent(CombatEvent event) {
        String color = switch (event.getType()) {
            case CRITICAL_HIT      -> UIFactory.YELLOW;
            case DAMAGE_DEALT      -> "#FF6B6B";
            case HEAL_RECEIVED     -> UIFactory.GREEN;
            case EFFECT_APPLIED    -> UIFactory.ORANGE;
            case EFFECT_TICK       -> UIFactory.ORANGE;
            case ENTITY_DIED       -> UIFactory.RED;
            case BOSS_PHASE_CHANGE -> UIFactory.PURPLE;
            case BOSS_ENRAGE       -> UIFactory.RED;
            case TURN_START        -> "#2A3A50";
            default                -> "#5A6A80";
        };
        addLog(event.getMessage(), color);
        refreshCombatants();
        refreshTurnOrderBar();

        // Chat triggers
        switch (event.getType()) {
            case ENTITY_DIED -> {
                if (event.getMessage() != null &&
                    !event.getMessage().contains(engine.getPlayer().getName())) {
                    router.emitChat(MercenaryDialogue.Trigger.COMBAT_ENEMY_DIES);
                }
            }
            case DAMAGE_DEALT -> {
                double hpPct = engine.getPlayer().getCurrentHp() /
                               engine.getPlayer().getStats().get(StatType.MAX_HP);
                if (hpPct < 0.30 && Math.random() < 0.4) {
                    router.emitChat(MercenaryDialogue.Trigger.COMBAT_PLAYER_LOW_HP);
                }
            }
            default -> { }
        }

        // AI turn dengan combatSpeedMs delay
        int delay = Math.max(50, combatSpeedMs); // minimum 50ms
        if (cm.isCombatActive() && !cm.isWaitingForPlayer() && !aiTurnPending) {
            aiTurnPending = true;
            Timeline t = new Timeline(new KeyFrame(Duration.millis(delay), e -> {
                aiTurnPending = false;
                if (cm.isCombatActive() && !cm.isWaitingForPlayer()) {
                    cm.processTurn();
                    refreshActionPanel();
                } else {
                    refreshActionPanel();
                }
            }));
            t.play();
        } else if (!aiTurnPending) {
            refreshActionPanel();
        }
    }

    private void handleCombatEnd(CombatResult result) {
        addLog(result.isVictory() ? "🏆 VICTORY!" : "💀 DEFEATED", UIFactory.YELLOW);
        combatLoop = null;

        // Chat trigger untuk victory/defeat
        router.emitChat(result.isVictory()
                ? MercenaryDialogue.Trigger.COMBAT_VICTORY
                : MercenaryDialogue.Trigger.COMBAT_DEFEAT);

        Timeline delay = new Timeline(new KeyFrame(Duration.millis(1500), e -> {
            if (result.isVictory()) router.showVictory(result);
            else if (result.isDefeat()) router.showGameOver();
            else router.showDungeonMap();
        }));
        delay.play();
    }

    private void handleTargetSelect(Entity enemy) {
        if (!cm.isWaitingForPlayer() || !enemy.isAlive()) return;

        if (targetSelectMode && pendingSkillId != null) {
            // Target select for skill
            targetSelectMode = false;
            String sid = pendingSkillId;
            pendingSkillId = null;
            engine.submitCombatAction(CombatAction.useSkill(sid, List.of(enemy.getId())));
        } else {
            // Normal attack
            engine.submitCombatAction(CombatAction.basicAttack(List.of(enemy.getId())));
        }
    }

    // ════════════════════════════════════════════════════
    // COMBAT LOOP
    // ════════════════════════════════════════════════════

    public void startCombatLoop() {
        if (cm == null) return;
        addLog("⚔ Combat begins!", UIFactory.CYAN);
        aiTurnPending    = false;
        targetSelectMode = false;
        combatSpeedMs    = 1200; // default: comfortable reading speed

        Timeline firstTurn = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            boolean ongoing = cm.processTurn();
            refreshCombatants();
            refreshActionPanel();
            refreshTurnOrderBar();
            if (!ongoing) addLog("Combat ended immediately.", "#5A6A80");
        }));
        firstTurn.play();
    }

    // ════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════

    // ════════════════════════════════════════════════════
    // v0.3: TURN ORDER BAR + SPEED CONTROL
    // ════════════════════════════════════════════════════

    private void refreshTurnOrderBar() {
        if (turnOrderBar == null || cm == null) return;
        turnOrderBar.getChildren().clear();

        Label lbl = new Label("NEXT ›");
        lbl.setStyle("-fx-text-fill: #2A3A50; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        turnOrderBar.getChildren().add(lbl);

        List<Entity> upcoming = cm.getTurnQueue().getUpcomingTurns(6);
        Entity current = cm.getCurrentActor();

        for (int i = 0; i < upcoming.size(); i++) {
            Entity e   = upcoming.get(i);
            boolean isCur = current != null && e.getId().equals(current.getId()) && i == 0;
            boolean isP   = e instanceof Player;
            boolean isM   = e instanceof Mercenary;
            String  clr   = isP ? UIFactory.CYAN : isM ? UIFactory.GREEN : UIFactory.RED;

            VBox slot = new VBox(2);
            slot.setAlignment(Pos.CENTER);
            slot.setPadding(new Insets(2, 8, 2, 8));
            slot.setStyle(isCur
                ? "-fx-background-color: " + clr + "18; -fx-border-color: " + clr + "66; -fx-border-width: 1;"
                : "-fx-background-color: transparent;");

            String name = e.getName().length() > 7 ? e.getName().substring(0, 6) + "." : e.getName();
            Label nameL = new Label(isCur ? "▶ " + name : name);
            nameL.setStyle(
                "-fx-text-fill: " + (isCur ? clr : clr + "77") + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: " + (isCur ? "10" : "9") + "px;" +
                (isCur ? "-fx-font-weight: bold;" : "")
            );

            double hpPct = e.getCurrentHp() / e.getStats().get(StatType.MAX_HP);
            ProgressBar hp = new ProgressBar(hpPct);
            hp.setPrefWidth(48); hp.setPrefHeight(3);
            hp.setStyle("-fx-accent: " + clr + "; -fx-background-color: " + clr + "22;" +
                        "-fx-min-height: 3px; -fx-max-height: 3px;");

            slot.getChildren().addAll(nameL, hp);
            turnOrderBar.getChildren().add(slot);

            if (i < upcoming.size() - 1) {
                Label sep = new Label("›");
                sep.setStyle("-fx-text-fill: #1C2E44; -fx-font-size: 9px;");
                turnOrderBar.getChildren().add(sep);
            }
        }
    }

    private HBox buildSpeedControl() {
        HBox bar = new HBox(4);
        bar.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(bar, Priority.ALWAYS);

        Label lbl = new Label("SPD:");
        lbl.setStyle("-fx-text-fill: #2A3A50; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
        bar.getChildren().add(lbl);

        for (int[] spec : new int[][]{{1, 1200}, {2, 500}, {0, 50}}) {
            int label = spec[0], ms = spec[1];
            String text = ms == 50 ? "SKIP" : label + "×";
            boolean active = combatSpeedMs == ms;

            Button b = new Button(text);
            b.setStyle(
                "-fx-background-color: " + (active ? "#00E5FF22" : "transparent") + ";" +
                "-fx-border-color: " + (active ? "#00E5FF" : "#1C2E44") + ";" +
                "-fx-border-width: 1; -fx-text-fill: " + (active ? "#00E5FF" : "#5A6A80") + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 9px; -fx-padding: 2 5; -fx-cursor: hand;"
            );
            final int speedMs = ms;
            b.setOnAction(e -> { combatSpeedMs = speedMs; refreshActionPanel(); });
            bar.getChildren().add(b);
        }
        return bar;
    }

    private Entity getFirstAliveEnemy() {
        if (cm == null) return null;
        return cm.getLivingEnemies().stream().findFirst().orElse(null);
    }

    private String getSkillDisplayName(String skillId) {
        return switch (skillId) {
            case "POWER_STRIKE"  -> "POWER\nSTRIKE";
            case "PHANTOM_SHOT"  -> "PHANTOM\nSHOT";
            case "SHADOW_STEP"   -> "SHADOW\nSTEP";
            case "DEEP_HACK"     -> "DEEP\nHACK";
            case "EXECUTE"       -> "EXECUTE";
            default              -> skillId.replace("_", "\n").substring(0, Math.min(skillId.length(), 10));
        };
    }
}
