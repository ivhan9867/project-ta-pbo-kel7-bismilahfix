package arclightcity.ui.view;
import arclightcity.entity.stats.StatType;

import arclightcity.dungeon.*;
import arclightcity.item.Item;
import arclightcity.item.LootManager;
import arclightcity.engine.GameEngine;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.layout.*;
import javafx.util.Duration;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.ArclightApp;
import arclightcity.ui.view.MercenaryDialogue;
import arclightcity.ui.util.UIFactory;

import java.util.List;

/**
 * DungeonMapView — tampilan peta dungeon dan navigasi room.
 *
 * Layout:
 *   ┌──────────────────────────┐
 *   │ ← BACK  FLOOR 1: SLUM   │
 *   │ HP ███ SHIELD ██ MP ██   │
 *   ├──────────────────────────┤
 *   │  [ROOM MAP GRID]         │  ← ASCII-style room grid
 *   │  ○──□──◆──☐──☆           │
 *   │  Legend: ○=empty ◆=enemy │
 *   ├──────────────────────────┤
 *   │  CURRENT ROOM: ENEMY     │
 *   │  desc...                 │
 *   ├──────────────────────────┤
 *   │  NEXT ROOMS:             │
 *   │  [→ LOOT ROOM]           │
 *   │  [→ ENEMY ROOM]          │
 *   └──────────────────────────┘
 */
public class DungeonMapView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private VBox              roomListContainer;
    private VBox              currentRoomPanel;
    private DungeonGridMap    dungeonGridMap;  // Grid map interaktif
    private VBox              gridMapContainer;

    public DungeonMapView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
        // wireEngineListeners() dipanggil di build() setelah semua field siap
    }

    public void wireEngineListeners() {
        engine.setOnDungeonEvent(event -> {
            javafx.application.Platform.runLater(() -> {
                switch (event.type) {
                    case COMBAT_STARTED       -> {
                        router.emitChat(MercenaryDialogue.Trigger.COMBAT_START);
                        router.showCombat();
                    }
                    case EVENT_ENCOUNTERED    -> router.showEvent(event.dungeonEvent);
                    case SHOP_OPENED          -> {
                        router.addSystemChat("Pedagang ditemukan");
                        router.showShop();
                    }
                    case GAME_OVER            -> router.showGameOver();
                    case LEVEL_UP -> showLevelUpNotification(event);
                    case BOSS_DEFEATED -> {
                        router.showToast(
                            "💀  BOSS DIKALAHKAN!",
                            event.message,
                            "#FF6B00"
                        );
                        router.addSystemChat("✦ " + event.message);
                        router.emitChat(MercenaryDialogue.Trigger.COMBAT_VICTORY);
                        refreshMapGrid();
                        refreshCurrentRoomInfo();
                        refreshNextRoomsPanel();
                    }
                    case MYTHIC_CRAFT -> {
                        router.showToast(
                            "✦  SENJATA MYTHIC DITEMPA!",
                            event.message,
                            "#FF6B00"
                        );
                        router.addSystemChat("✦✦✦ " + event.message);
                    }
                    case READY_FOR_NEXT_FLOOR -> showDescentPrompt(event.intValue);
                    case LOOT_FOUND           -> {
                        showLootPopup(event);
                        router.emitChat(MercenaryDialogue.Trigger.DUNGEON_ENTER_LOOT);
                    }
                    case REST                 -> {
                        showRestNotification(event.message);
                        router.emitChat(MercenaryDialogue.Trigger.DUNGEON_ENTER_REST);
                    }
                    case ROOM_ALREADY_CLEARED -> {
                        refreshMapGrid();
                        refreshCurrentRoomInfo();
                        refreshNextRoomsPanel();
                    }
                    case ROOM_CLEARED, ROOM_ENTERED -> {
                        refreshMapGrid();
                        refreshCurrentRoomInfo();
                        refreshNextRoomsPanel();
                    }
                    case FLOOR_ENTERED -> {
                        router.emitChat(MercenaryDialogue.Trigger.DUNGEON_ENTER_FLOOR);
                        // Rebuild dungeon map view setelah sedikit delay
                        // agar descend() selesai dulu sebelum view dibuat ulang
                        new javafx.animation.Timeline(
                            new javafx.animation.KeyFrame(
                                javafx.util.Duration.millis(300),
                                ev -> router.showDungeonMap()
                            )
                        ).play();
                    }
                    default -> refreshCurrentRoomInfo();
                }
            });
        });
    }

    private javafx.scene.layout.BorderPane bpRoot;

    public Parent build() {
        DungeonManager dm = engine.getDungeonManager();
        Floor floor = dm.getCurrentFloor();

        bpRoot = UIFactory.screenRootBorder();
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #0A0604;");

        // ── TOP: header + vitals ──────────────────────────
        VBox topFixed = new VBox(0);

        // Header bar
        HBox header = new HBox(10);
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #0F0A06;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Button backBtn = new Button("← MARKAS");
        backBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                         "-fx-border-width: 1; -fx-text-fill: #5A3A10;" +
                         "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                         "-fx-padding: 3 8; -fx-cursor: hand;");
        backBtn.setOnAction(e -> router.showHub());

        String floorName = floor != null ? floor.getTheme().displayName.toUpperCase() : "—";
        Label floorLbl = new Label("⚔  LANTAI " + dm.getCurrentFloorNumber() + ": " + floorName);
        floorLbl.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 13px; -fx-font-weight: bold;" +
                          "-fx-effect: dropshadow(gaussian, #C8860A, 6, 0.3, 0, 0);");
        HBox.setHgrow(floorLbl, Priority.ALWAYS);

        Label goldLbl = new Label("⚙ " + UIFactory.formatNumber(engine.getPlayer().getGold()));
        goldLbl.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 12px; -fx-font-weight: bold;");

        header.getChildren().addAll(backBtn, floorLbl, goldLbl);
        topFixed.getChildren().add(header);
        topFixed.getChildren().add(buildVitalsBar());
        bpRoot.setTop(topFixed);

        // ── CENTER: map + room info ───────────────────────
        VBox centerContent = new VBox(0);

        dungeonGridMap = new DungeonGridMap(engine, () -> {
            javafx.application.Platform.runLater(() -> {
                refreshCurrentRoomInfo();
                refreshNextRoomsPanel();
            });
        });

        ScrollPane gridScroll = new ScrollPane(dungeonGridMap);
        gridScroll.setFitToWidth(true);
        gridScroll.setPrefHeight(290);
        gridScroll.setMaxHeight(310);
        gridScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        gridScroll.setStyle(
            "-fx-background-color: #0A0604;" +
            "-fx-background: #0A0604;" +
            "-fx-border-color: #3A2810;" +
            "-fx-border-width: 0 0 1 0;"
        );
        gridMapContainer = new VBox(gridScroll);
        centerContent.getChildren().add(gridMapContainer);

        currentRoomPanel = buildCurrentRoomPanel(dm);
        centerContent.getChildren().add(currentRoomPanel);

        ScrollPane centerScroll = new ScrollPane(centerContent);
        centerScroll.setFitToWidth(true);
        centerScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        centerScroll.setStyle(
            "-fx-background-color: #0A0604;" +
            "-fx-background: #0A0604;" +
            "-fx-border-color: transparent;"
        );
        bpRoot.setCenter(centerScroll);

        // ── BOTTOM: room actions / DESCEND ────────────────
        roomListContainer = new VBox(8);
        roomListContainer.setPadding(new Insets(10, 16, 12, 16));
        roomListContainer.setStyle(
            "-fx-background-color: #0F0A06;" +
            "-fx-border-color: #3A2810;" +
            "-fx-border-width: 1 0 0 0;"
        );
        refreshNextRooms(dm);
        bpRoot.setBottom(roomListContainer);

        UIFactory.fadeIn(bpRoot, 300);
        return bpRoot;
    }

    // ── Vitals bar ────────────────────────────────────────

    private HBox buildVitalsBar() {
        var player = engine.getPlayer();
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setStyle("-fx-background-color: #080D18; -fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");
        bar.setAlignment(Pos.CENTER_LEFT);

        // HP mini
        VBox hp = miniBar("HP", UIFactory.RED,
                player.getCurrentHp(),
                player.getStats().get(StatType.MAX_HP));
        HBox.setHgrow(hp, Priority.ALWAYS);

        // Shield mini
        double maxShield = player.getStats().get(StatType.MAX_SHIELD);
        VBox shield = miniBar("SHD", UIFactory.PURPLE,
                player.getCurrentShield(), maxShield);
        HBox.setHgrow(shield, Priority.ALWAYS);

        // MP mini
        VBox mp = miniBar("MP", "#2979FF",
                player.getCurrentMp(),
                player.getStats().get(StatType.MAX_MP));
        HBox.setHgrow(mp, Priority.ALWAYS);

        bar.getChildren().addAll(hp, shield, mp);
        return bar;
    }

    private VBox miniBar(String label, String color, double current, double max) {
        VBox box = new VBox(2);
        Label lbl = new Label(label + " " + (int)current + "/" + (int)max);
        lbl.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        ProgressBar bar = new ProgressBar(max > 0 ? current / max : 0);
        bar.setPrefWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: " + color + "; -fx-min-height: 5px; -fx-max-height: 5px; -fx-background-color: " + color + "22;");
        box.getChildren().addAll(lbl, bar);
        return box;
    }

    /** Build legend bar untuk icon room type */
    private HBox buildLegend() {
        HBox legend = new HBox(6);
        legend.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        legend.setPadding(new Insets(4, 0, 0, 0));
        legend.setStyle("-fx-border-color: #1C2E4444; -fx-border-width: 1 0 0 0;");

        Object[][] items = {
            {"◆", "ENEMY",  "#CC3300"},
            {"☆", "LOOT",   "#FFB830"},
            {"♥", "REST",   "#2D7A45"},
            {"?", "EVENT",  "#C8860A"},
            {"$", "SHOP",   "#FF6B00"},
            {"!", "TRAP",   "#FF6B00"},
            {"☠", "BOSS",   "#FF0000"},
        };
        for (Object[] it : items) {
            Label l = new Label(it[0] + " " + it[1]);
            l.setStyle(
                "-fx-text-fill: " + it[2] + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 11px;"
            );
            legend.getChildren().add(l);
        }
        return legend;
    }

    // ── Current room info ─────────────────────────────────

    private VBox buildCurrentRoomPanel(DungeonManager dm) {
        Room current = dm.getCurrentRoom();
        VBox box = new VBox(6);
        box.setPadding(new Insets(12, 16, 8, 16));
        box.setStyle("-fx-background-color: #0F0A06; -fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        if (current == null) {
            box.getChildren().add(new Label("No current room"));
            return box;
        }

        String color = getRoomColor(current.getType());
        boolean isCleared = current.isCleared();

        // Room type label + cleared badge
        HBox typeRow = new HBox(8);
        typeRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label typeLabel = new Label(getRoomIcon(current.getType()) + "  " + current.getType().name());
        typeLabel.setStyle(
            "-fx-text-fill: " + (isCleared ? "#6A5840" : color) + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );
        typeRow.getChildren().add(typeLabel);

        if (isCleared) {
            Label clearedBadge = new Label("✓ SELESAI");
            clearedBadge.setStyle(
                "-fx-text-fill: #2D7A45;" +
                "-fx-font-family: 'Courier New';" +
                "-fx-font-size: 11px;" +
                "-fx-background-color: #2D7A4522;" +
                "-fx-border-color: #2D7A4555;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 2 6;"
            );
            typeRow.getChildren().add(clearedBadge);
        }

        // Description
        String descText = isCleared
                ? "Sudah pernah dijelajahi. Tidak ada lagi yang tersisa."
                : getRoomDescription(current);
        Label desc = new Label(descText);
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: " + (isCleared ? "#3A4A60" : "#A09070") +
                "; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        // Floor progress bar
        Floor floor = dm.getCurrentFloor();
        if (floor != null) {
            int cleared = floor.getClearedRooms();
            int total   = floor.getTotalRooms();
            boolean bossDown = floor.isBossDefeated();

            HBox progressRow = new HBox(8);
            progressRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label progressLabel = new Label("DIJELAJAHI: " + cleared + "/" + total);
            progressLabel.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

            javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(
                    total > 0 ? (double) cleared / total : 0);
            progressBar.setPrefWidth(100);
            progressBar.setStyle("-fx-accent: #C8860A55; -fx-min-height: 4px; -fx-max-height: 4px;" +
                    "-fx-background-color: #1C2E4444;");

            progressRow.getChildren().addAll(progressLabel, progressBar);

            if (bossDown) {
                Label bossLabel = new Label("☠ BOSS DIKALAHKAN");
                bossLabel.setStyle("-fx-text-fill: #CC330088; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
                box.getChildren().addAll(typeRow, desc, progressRow, bossLabel);
            } else {
                box.getChildren().addAll(typeRow, desc, progressRow);
            }
        } else {
            box.getChildren().addAll(typeRow, desc);
        }

        return box;
    }

    // ── Next rooms ────────────────────────────────────────

    private void refreshNextRooms(DungeonManager dm) {
        roomListContainer.getChildren().clear();

        // Cek apakah boss sudah dikalahkan
        boolean bossCleared = dm.getCurrentFloor() != null
                && dm.getCurrentFloor().isBossDefeated();

        if (bossCleared) {
            // Boss sudah mati — tampilkan DESCEND
            Label done = new Label("✓ Boss defeated — floor cleared!");
            done.setStyle("-fx-text-fill: #2D7A45; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            roomListContainer.getChildren().add(done);

            Button descend = UIFactory.btnGold("▼  TURUN KE LANTAI BERIKUTNYA");
            descend.setOnAction(e -> engine.descend());
            roomListContainer.getChildren().add(descend);
            return;
        }

        // Hint navigasi via grid
        Label hint = new Label("▲ Klik ruangan yang berdekatan di peta untuk menjelajah");
        hint.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        // Boss belum dikalahkan — tampilkan reminder
        Label bossHint = new Label("☠ Kalahkan BOSS untuk membuka lantai berikutnya");
        bossHint.setStyle("-fx-text-fill: #CC330088; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        roomListContainer.getChildren().addAll(hint, bossHint);

        // Legend icon
        HBox legend = buildLegend();
        roomListContainer.getChildren().add(legend);
    }

    // ── Descent prompt ─────────────────────────────────────

    private void showLevelUpNotification(DungeonStateEvent event) {
        int newLevel = event.intValue;
        int skillPts = engine.getPlayer().getSkillPoints();

        // Chat notification — selalu reliable
        router.addSystemChat("⬆ LEVEL UP → LV." + newLevel +
            "  |  ✦ +" + skillPts + " Poin Jurus");

        // Toast via SceneRouter yang punya akses scene yang benar
        router.showToast(
            "⬆  LEVEL UP  LV." + newLevel,
            "Asuna naik level! +" + skillPts + " Poin Jurus tersedia.",
            "#FFB830"
        );
    }

    private void showDescentPrompt(int nextFloor) {
        // Tampilkan panel pilihan di bawah dungeon map
        // Player bisa pilih: TURUN atau KEMBALI KE MARKAS

        javafx.application.Platform.runLater(() -> {
            // Buat panel pilihan
            javafx.scene.layout.VBox panel = new javafx.scene.layout.VBox(8);
            panel.setPadding(new javafx.geometry.Insets(12, 16, 14, 16));
            panel.setStyle(
                "-fx-background-color: #0F0A06;" +
                "-fx-border-color: #C8860A; -fx-border-width: 2 0 0 0;" +
                "-fx-effect: dropshadow(gaussian, #C8860A, 12, 0.3, 0, -2);"
            );

            javafx.scene.layout.HBox titleRow = new javafx.scene.layout.HBox(8);
            titleRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            javafx.scene.control.Label icon = new javafx.scene.control.Label("▼");
            icon.setStyle("-fx-text-fill: #C8860A; -fx-font-size: 16px;");

            javafx.scene.control.Label titleLbl =
                new javafx.scene.control.Label("LANTAI " + (nextFloor-1) + " SELESAI!");
            titleLbl.setStyle(
                "-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, #C8860A, 6, 0.3, 0, 0);");
            javafx.scene.layout.HBox.setHgrow(titleLbl, javafx.scene.layout.Priority.ALWAYS);

            javafx.scene.control.Label subLbl =
                new javafx.scene.control.Label("Menuju lantai " + nextFloor + "...");
            subLbl.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

            titleRow.getChildren().addAll(icon, titleLbl);

            // Tombol pilihan
            javafx.scene.layout.HBox btnRow = new javafx.scene.layout.HBox(8);

            javafx.scene.control.Button descBtn = new javafx.scene.control.Button(
                "▼  TURUN KE LANTAI " + nextFloor);
            descBtn.setMaxWidth(Double.MAX_VALUE);
            javafx.scene.layout.HBox.setHgrow(descBtn, javafx.scene.layout.Priority.ALWAYS);
            descBtn.setStyle(
                "-fx-background-color: #C8860A22; -fx-border-color: #FFB830; -fx-border-width: 1;" +
                "-fx-text-fill: #FFB830; -fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-padding: 10 16; -fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, #C8860A, 6, 0.3, 0, 0);");
            descBtn.setOnAction(e -> {
                // Hapus panel pilihan dari root
                if (panel.getParent() instanceof javafx.scene.layout.BorderPane bp) {
                    bp.setBottom(null);
                }
                playFloorTransition(nextFloor, () -> engine.descend());
            });

            javafx.scene.control.Button hubBtn = new javafx.scene.control.Button(
                "◈  KEMBALI KE MARKAS");
            hubBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #3A2810; -fx-border-width: 1;" +
                "-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                "-fx-padding: 10 16; -fx-cursor: hand;");
            hubBtn.setOnAction(e -> router.showHub());

            btnRow.getChildren().addAll(descBtn, hubBtn);
            panel.getChildren().addAll(titleRow, subLbl, btnRow);

            // Tampilkan di bawah dungeon map view
            if (bpRoot != null) {
                bpRoot.setBottom(panel);
            }
        });

        // Toast kecil di pojok
        router.showToast(
            "▼  LANTAI " + (nextFloor-1) + " SELESAI",
            "Pilih: turun atau kembali ke markas",
            "#C8860A"
        );
    }

    /**
     * Floor transition animation:
     * 1. Fade out screen ke hitam (400ms)
     * 2. Tampilkan teks "DESCENDING TO FLOOR X" (600ms)
     * 3. Fade in screen baru (400ms)
     */
    private void playFloorTransition(int nextFloor,  Runnable onDescend) {
        // Ambil root scene pane
        javafx.scene.Parent sceneRoot = router.getStage().getScene().getRoot();
        if (!(sceneRoot instanceof javafx.scene.layout.Pane rootPane)) {
            onDescend.run();
            return;
        }

        // Overlay gelap
        javafx.scene.layout.StackPane overlay = new javafx.scene.layout.StackPane();
        overlay.setStyle("-fx-background-color: #000000;");
        overlay.setOpacity(0);
        overlay.setPrefSize(ArclightApp.SCREEN_WIDTH, ArclightApp.SCREEN_HEIGHT);

        // Teks transisi
        javafx.scene.layout.VBox textBox = new javafx.scene.layout.VBox(12);
        textBox.setAlignment(javafx.geometry.Pos.CENTER);

        javafx.scene.control.Label descLabel = new javafx.scene.control.Label(
            "▼ TURUN");
        descLabel.setStyle("-fx-text-fill: #C8860A; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 24px; -fx-font-weight: bold;");

        javafx.scene.control.Label floorLabel = new javafx.scene.control.Label(
            "LANTAI " + nextFloor);
        floorLabel.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                            "-fx-font-size: 36px; -fx-font-weight: bold;" +
                            "-fx-effect: dropshadow(gaussian, #FFB830, 20, 0.5, 0, 0);");

        javafx.scene.control.Label scanLabel = new javafx.scene.control.Label(
            "[ SCANNING SECTOR... ]");
        scanLabel.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 12px;");

        textBox.getChildren().addAll(descLabel, floorLabel, scanLabel);
        overlay.getChildren().add(textBox);

        rootPane.getChildren().add(overlay);

        // Timeline: fade in overlay → descend → fade out
        Timeline timeline = new Timeline(
            // 0ms: mulai fade in
            new KeyFrame(Duration.ZERO,
                new KeyValue(overlay.opacityProperty(), 0)),
            // 400ms: fully black
            new KeyFrame(Duration.millis(400),
                new KeyValue(overlay.opacityProperty(), 1.0)),
            // 600ms: jalankan descend (screen sudah hitam)
            new KeyFrame(Duration.millis(600), e -> {
                onDescend.run();
                // Update floor label
                floorLabel.setText("LANTAI " + engine.getDungeonManager().getCurrentFloorNumber());
                scanLabel.setText("[ SECTOR LOADED ]");
                scanLabel.setStyle(scanLabel.getStyle().replace("#3A2810", "#2D7A45"));
            }),
            // 1200ms: mulai fade out
            new KeyFrame(Duration.millis(1200),
                new KeyValue(overlay.opacityProperty(), 1.0)),
            // 1600ms: fully visible again
            new KeyFrame(Duration.millis(1600),
                new KeyValue(overlay.opacityProperty(), 0))
        );
        timeline.setOnFinished(e -> rootPane.getChildren().remove(overlay));
        timeline.play();
    }

    private void refreshCurrentRoomInfo() {
        if (currentRoomPanel == null) return;
        DungeonManager dm = engine.getDungeonManager();
        VBox newPanel = buildCurrentRoomPanel(dm);

        javafx.scene.Parent parent = currentRoomPanel.getParent();
        if (parent instanceof VBox vb) {
            int idx = vb.getChildren().indexOf(currentRoomPanel);
            if (idx >= 0) {
                vb.getChildren().set(idx, newPanel);
                currentRoomPanel = newPanel;
            }
        }
        // Refresh bottom room actions juga
        refreshNextRoomsPanel();
    }

    private void refreshMapGrid() {
        if (dungeonGridMap != null) {
            dungeonGridMap.refresh();
        }
    }

    private void refreshNextRoomsPanel() {
        if (roomListContainer == null) return;
        refreshNextRooms(engine.getDungeonManager());
    }

    // ── Loot Popup ────────────────────────────────────────

    /**
     * Tampilkan popup sederhana berisi item yang didapat dari loot room.
     * Item langsung masuk inventory, popup hanya untuk feedback visual.
     *
     * 💡 Ide: Popup ini bisa dikembangkan jadi full "Loot Screen" di v0.3
     *    dengan animasi item muncul satu per satu dan pilihan auto-equip.
     */
    private void showLootPopup(DungeonStateEvent event) {
        // Generate actual loot dan masukkan ke inventory
        java.util.List<Item> drops = LootManager.generateLoot(
                "LOOT_FLOOR_" + engine.getDungeonManager().getCurrentFloorNumber(),
                engine.getDungeonManager().getCurrentFloorNumber());
        drops.forEach(engine.getInventory()::addItem);

        if (drops.isEmpty()) {
            showInfoAlert("📦 RUANG HARTA", "Peti sudah kosong. Ada yang lebih dulu sampai di sini.");
            return;
        }

        // Build item list untuk popup
        StringBuilder sb = new StringBuilder();
        sb.append("Items added to inventory:\n\n");
        for (Item it : drops) {
            String rarityTag = "[" + it.getRarity().displayName + "]";
            sb.append(rarityTag).append(" ").append(it.getFullName()).append("\n");
        }

        showInfoAlert("📦 LOOT FOUND — " + drops.size() + " item(s)!", sb.toString());
        refreshCurrentRoomInfo();
        refreshNextRoomsPanel();
    }

    private void showRestNotification(String message) {
        showInfoAlert("🛌 SAFE ZONE", message != null ? message :
                "HP and MP partially restored. The calm before the storm.");
        refreshCurrentRoomInfo();
        refreshNextRoomsPanel();
    }

    private void showInfoAlert(String title, String content) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle(
                "-fx-background-color: #0F0A06;" +
                "-fx-font-family: 'Courier New', monospace;" +
                "-fx-font-size: 11px;");
        // Style button OK
        alert.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK)
             .setStyle("-fx-background-color: #C8860A22; -fx-text-fill: #C8860A;" +
                       "-fx-border-color: #C8860A; -fx-cursor: hand;");
        alert.showAndWait();
    }

    // ── Icon / Color helpers ──────────────────────────────

    private String getRoomIcon(Room.RoomType type) {
        return switch (type) {
            case EMPTY   -> "○";
            case ENEMY   -> "◆";
            case ELITE   -> "◈";
            case BOSS    -> "☠";
            case LOOT    -> "☆";
            case REST    -> "♥";
            case EVENT   -> "?";
            case SHOP    -> "$";
            case TRAP    -> "!";
        };
    }

    private String getRoomColor(Room.RoomType type) {
        return switch (type) {
            case EMPTY   -> UIFactory.DIM;
            case ENEMY   -> UIFactory.RED;
            case ELITE   -> UIFactory.PURPLE;
            case BOSS    -> "#FF0000";
            case LOOT    -> UIFactory.YELLOW;
            case REST    -> UIFactory.GREEN;
            case EVENT   -> UIFactory.CYAN;
            case SHOP    -> UIFactory.ORANGE;
            case TRAP    -> "#FF6B00";
        };
    }

    private String getRoomDescription(Room room) {
        return switch (room.getType()) {
            case EMPTY   -> "An empty corridor. Nothing to see here.";
            case ENEMY   -> "Enemies detected in this area. Prepare for combat.";
            case ELITE   -> "Elite enemies here. Stronger mechanics — proceed with caution.";
            case BOSS    -> "⚠ BOSS ROOM — The floor guardian awaits.";
            case LOOT    -> "A cache of supplies and equipment was found here.";
            case REST    -> "A safe spot to rest. Recover HP and MP.";
            case EVENT   -> "Something unusual here. Unknown outcome.";
            case SHOP    -> "A wandering merchant is set up here.";
            case TRAP    -> "This room triggered a hazard.";
        };
    }
}
