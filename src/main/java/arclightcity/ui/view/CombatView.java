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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.view.MercenaryDialogue;
import arclightcity.ui.util.UIFactory;

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

    // UI refs untuk update
    private VBox    enemyContainer;
    private HBox    allyContainer;
    private VBox    logContainer;
    private ScrollPane logScroll;
    private VBox    actionPanel;
    private Label   turnLabel;
    private Label   currentActorLabel;
    private Timeline combatLoop;

    public CombatView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
        this.cm     = engine.getActiveCombat();
    }

    public Parent build() {
        VBox root = UIFactory.screenRoot();

        // ── Top bar ──────────────────────────────────────
        root.getChildren().add(buildTopBar());

        // ── Combat log ───────────────────────────────────
        root.getChildren().add(buildCombatLog());

        // ── Enemy area ───────────────────────────────────
        VBox enemySection = new VBox(6);
        enemySection.setPadding(new Insets(8, 12, 4, 12));
        enemySection.setStyle("-fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");
        Label enemySectionTitle = UIFactory.sectionTitle("◆ ENEMIES");
        enemySectionTitle.setPadding(new Insets(0, 0, 4, 0));
        enemyContainer = new VBox(6);
        enemySection.getChildren().addAll(enemySectionTitle, enemyContainer);
        root.getChildren().add(enemySection);

        // ── Ally area ────────────────────────────────────
        VBox allySection = new VBox(6);
        allySection.setPadding(new Insets(8, 12, 4, 12));
        allySection.setStyle("-fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");
        Label allySectionTitle = UIFactory.sectionTitle("◈ ALLIES");
        allySectionTitle.setPadding(new Insets(0, 0, 4, 0));
        allyContainer = new HBox(8);
        allySection.getChildren().addAll(allySectionTitle, allyContainer);
        root.getChildren().add(allySection);

        // ── Status effects ────────────────────────────────
        root.getChildren().add(buildStatusPanel());

        // Tidak ada spacer — biarkan action panel langsung di bawah status
        // spacer dihapus karena menyebabkan action panel terpotong di layar kecil

        // ── Action panel (selalu di bawah, tidak terpotong) ─
        actionPanel = new VBox(8);
        actionPanel.setPadding(new Insets(10, 12, 12, 12));
        actionPanel.setStyle("-fx-background-color: #0C1220; -fx-border-color: #1C2E44; -fx-border-width: 1 0 0 0;");
        root.getChildren().add(actionPanel);

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
        wrapper.setPrefHeight(130);
        wrapper.setMaxHeight(130);
        wrapper.setStyle("-fx-background-color: #050810; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        logContainer = new VBox(2);
        logContainer.setPadding(new Insets(6, 10, 6, 10));

        logScroll = new ScrollPane(logContainer);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(130);
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
        VBox card = new VBox(6);
        card.setPadding(new Insets(10, 12, 10, 12));
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
        VBox card = new VBox(6);
        card.setPadding(new Insets(10, 12, 10, 12));
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

        if (!cm.isWaitingForPlayer()) {
            Label waiting = new Label("Processing turn...");
            waiting.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            actionPanel.getChildren().add(waiting);
            return;
        }

        // Row 1: Attack + Skill + Item
        HBox row1 = new HBox(8);
        row1.setAlignment(Pos.CENTER);

        Button attackBtn = buildActionBtn("⚔ ATTACK", UIFactory.RED, () -> {
            Entity target = getFirstAliveEnemy();
            if (target != null) {
                engine.submitCombatAction(CombatAction.basicAttack(
                        java.util.List.of(target.getId())));
            }
        });

        Button skillBtn = buildActionBtn("★ SKILL", UIFactory.CYAN, () -> showSkillMenu());
        Button itemBtn  = buildActionBtn("⊞ ITEM",  UIFactory.GREEN, () -> showItemMenu());

        row1.getChildren().addAll(attackBtn, skillBtn, itemBtn);

        // Row 2: Defend + spacer + Flee
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

    private void showSkillMenu() {
        Player player = engine.getPlayer();
        List<String> equipped = player.getEquippedSkillIds();
        if (equipped.isEmpty()) {
            addLog("No skills equipped!", UIFactory.ORANGE);
            return;
        }
        // Simple: pakai skill pertama yang ready
        for (String skillId : equipped) {
            if (player.getSkillCooldown(skillId) == 0) {
                submitSkillAction(skillId);
                return;
            }
        }
        addLog("All skills on cooldown!", UIFactory.ORANGE);
    }

    private void submitSkillAction(String skillId) {
        Entity target = getFirstAliveEnemy();
        if (target != null) {
            engine.submitCombatAction(CombatAction.useSkill(
                    skillId, java.util.List.of(target.getId())));
        }
    }

    private void showItemMenu() {
        var consumables = engine.getInventory().getConsumables();
        if (consumables.isEmpty()) {
            addLog("No items available!", UIFactory.ORANGE);
            return;
        }
        // Pakai consumable pertama
        var first = consumables.get(0);
        engine.getInventory().useConsumable(first.getId(), engine.getPlayer());
        engine.submitCombatAction(CombatAction.useItem(first.getId(),
                java.util.List.of(engine.getPlayer().getId())));
        addLog("Used: " + first.getName(), UIFactory.GREEN);
    }

    // ════════════════════════════════════════════════════
    // EVENT HANDLERS
    // ════════════════════════════════════════════════════

    private boolean aiTurnPending = false; // guard agar Timeline tidak stack

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

        // ── Chat triggers ──────────────────────────────────────
        switch (event.getType()) {
            case ENTITY_DIED -> {
                // Cek apakah yang mati enemy atau ally
                if (event.getMessage() != null && !event.getMessage().contains(engine.getPlayer().getName())) {
                    router.emitChat(MercenaryDialogue.Trigger.COMBAT_ENEMY_DIES);
                }
            }
            case DAMAGE_DEALT -> {
                // Cek apakah player HP rendah (< 30%)
                double hpPct = engine.getPlayer().getCurrentHp() /
                               engine.getPlayer().getStats().get(arclightcity.entity.stats.StatType.MAX_HP);
                if (hpPct < 0.30 && Math.random() < 0.4) { // 40% chance agar tidak spam
                    router.emitChat(MercenaryDialogue.Trigger.COMBAT_PLAYER_LOW_HP);
                }
            }
            default -> { }
        }

        if (cm.isCombatActive() && !cm.isWaitingForPlayer() && !aiTurnPending) {
            aiTurnPending = true;
            Timeline delay = new Timeline(new KeyFrame(Duration.millis(500), e -> {
                aiTurnPending = false;
                if (cm.isCombatActive() && !cm.isWaitingForPlayer()) {
                    boolean ongoing = cm.processTurn();
                    refreshActionPanel();
                    if (!ongoing && cm.isCombatActive()) {
                        addLog("Combat concluded.", "#5A6A80");
                    }
                } else {
                    refreshActionPanel();
                }
            }));
            delay.play();
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
        // Simple: basic attack ke enemy yang diklik
        engine.submitCombatAction(CombatAction.basicAttack(
                java.util.List.of(enemy.getId())));
    }

    // ════════════════════════════════════════════════════
    // COMBAT LOOP
    // ════════════════════════════════════════════════════

    public void startCombatLoop() {
        if (cm == null) return;
        addLog("⚔ Combat begins!", UIFactory.CYAN);
        aiTurnPending = false;

        // Kick off first turn dengan sedikit delay agar UI ter-render dulu
        Timeline firstTurn = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            boolean ongoing = cm.processTurn();
            refreshCombatants();
            refreshActionPanel();
            if (!ongoing) {
                addLog("Combat ended immediately.", "#5A6A80");
            }
        }));
        firstTurn.play();
    }

    // ════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════

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
