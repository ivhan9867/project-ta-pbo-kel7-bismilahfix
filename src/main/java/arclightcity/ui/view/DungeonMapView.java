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
import javafx.scene.layout.*;
import javafx.util.Duration;
import arclightcity.ui.controller.SceneRouter;
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
        wireEngineListeners();
    }

    private void wireEngineListeners() {
        engine.setOnDungeonEvent(event -> {
            javafx.application.Platform.runLater(() -> {
                switch (event.type) {
                    case COMBAT_STARTED       -> router.showCombat();
                    case EVENT_ENCOUNTERED    -> router.showEvent(event.dungeonEvent);
                    case SHOP_OPENED          -> router.showShop();
                    case GAME_OVER            -> router.showGameOver();
                    case READY_FOR_NEXT_FLOOR -> showDescentPrompt(event.intValue);
                    case LOOT_FOUND           -> showLootPopup(event);
                    case REST                 -> showRestNotification(event.message);
                    case ROOM_ALREADY_CLEARED -> {
                        // Backtrack ke tile yang sudah cleared — hanya refresh UI
                        refreshMapGrid();
                        refreshCurrentRoomInfo();
                        refreshNextRoomsPanel();
                    }
                    case ROOM_CLEARED, ROOM_ENTERED, FLOOR_ENTERED -> {
                        refreshMapGrid();
                        refreshCurrentRoomInfo();
                        refreshNextRoomsPanel();
                    }
                    default -> refreshCurrentRoomInfo();
                }
            });
        });
    }

    public Parent build() {
        DungeonManager dm = engine.getDungeonManager();
        Floor floor = dm.getCurrentFloor();

        VBox root = UIFactory.screenRoot();

        // Header
        String floorTitle = "FLOOR " + dm.getCurrentFloorNumber() + ": " +
                (floor != null ? floor.getTheme().displayName.toUpperCase() : "—");
        root.getChildren().add(UIFactory.headerWithResources(
                floorTitle,
                () -> router.showHub(),
                engine.getPlayer().getGold(),
                dm.getCurrentFloorNumber()));

        // Player vitals
        root.getChildren().add(buildVitalsBar());

        UIFactory.divider();

        // ── Grid Map Interaktif ───────────────────────────────
        dungeonGridMap = new DungeonGridMap(engine, () -> {
            // Callback saat player klik tile — refresh info panel
            javafx.application.Platform.runLater(() -> {
                refreshCurrentRoomInfo();
                refreshNextRoomsPanel();
            });
        });

        // Wrap dalam ScrollPane agar grid bisa discroll kalau panjang
        ScrollPane gridScroll = new ScrollPane(dungeonGridMap);
        gridScroll.setFitToWidth(true);
        gridScroll.setPrefHeight(230);
        gridScroll.setMaxHeight(230);
        gridScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        gridScroll.setStyle(
            "-fx-background-color: #050810;" +
            "-fx-background: #050810;" +
            "-fx-border-color: #1C2E44;" +
            "-fx-border-width: 0 0 1 0;"
        );
        gridMapContainer = new VBox(gridScroll);
        root.getChildren().add(gridMapContainer);

        // Current room info
        currentRoomPanel = buildCurrentRoomPanel(dm);
        root.getChildren().add(currentRoomPanel);

        // Next rooms
        roomListContainer = new VBox(8);
        roomListContainer.setPadding(new Insets(8, 16, 12, 16));
        VBox.setVgrow(roomListContainer, Priority.ALWAYS);
        refreshNextRooms(dm);
        root.getChildren().add(roomListContainer);

        UIFactory.fadeIn(root, 300);
        return root;
    }

    // ── Vitals bar ────────────────────────────────────────

    private HBox buildVitalsBar() {
        var player = engine.getPlayer();
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setStyle("-fx-background-color: #080D18; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");
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
        lbl.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");

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
            {"◆", "ENEMY",  "#FF1744"},
            {"☆", "LOOT",   "#FFD600"},
            {"♥", "REST",   "#00E676"},
            {"?", "EVENT",  "#00E5FF"},
            {"$", "SHOP",   "#FF6B00"},
            {"!", "TRAP",   "#FF6B00"},
            {"☠", "BOSS",   "#FF0000"},
        };
        for (Object[] it : items) {
            Label l = new Label(it[0] + " " + it[1]);
            l.setStyle(
                "-fx-text-fill: " + it[2] + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 9px;"
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
        box.setStyle("-fx-background-color: #0C1220; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

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
            "-fx-text-fill: " + (isCleared ? "#5A6A80" : color) + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );
        typeRow.getChildren().add(typeLabel);

        if (isCleared) {
            Label clearedBadge = new Label("✓ CLEARED");
            clearedBadge.setStyle(
                "-fx-text-fill: #00E676;" +
                "-fx-font-family: 'Courier New';" +
                "-fx-font-size: 9px;" +
                "-fx-background-color: #00E67622;" +
                "-fx-border-color: #00E67655;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 2 6;"
            );
            typeRow.getChildren().add(clearedBadge);
        }

        // Description
        String descText = isCleared
                ? "You've been here before. Nothing left to find."
                : getRoomDescription(current);
        Label desc = new Label(descText);
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: " + (isCleared ? "#3A4A60" : "#8899AA") +
                "; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        // Floor progress bar
        Floor floor = dm.getCurrentFloor();
        if (floor != null) {
            int cleared = floor.getClearedRooms();
            int total   = floor.getTotalRooms();
            boolean bossDown = floor.isBossDefeated();

            HBox progressRow = new HBox(8);
            progressRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label progressLabel = new Label("EXPLORED: " + cleared + "/" + total);
            progressLabel.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

            javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(
                    total > 0 ? (double) cleared / total : 0);
            progressBar.setPrefWidth(100);
            progressBar.setStyle("-fx-accent: #00E5FF55; -fx-min-height: 4px; -fx-max-height: 4px;" +
                    "-fx-background-color: #1C2E4444;");

            progressRow.getChildren().addAll(progressLabel, progressBar);

            if (bossDown) {
                Label bossLabel = new Label("☠ BOSS DEFEATED");
                bossLabel.setStyle("-fx-text-fill: #FF174488; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
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
            done.setStyle("-fx-text-fill: #00E676; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            roomListContainer.getChildren().add(done);

            Button descend = UIFactory.btnGold("▼  DESCEND TO NEXT FLOOR");
            descend.setOnAction(e -> engine.descend());
            roomListContainer.getChildren().add(descend);
            return;
        }

        // Hint navigasi via grid
        Label hint = new Label("▲ Click adjacent tile on the map to explore");
        hint.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        // Boss belum dikalahkan — tampilkan reminder
        Label bossHint = new Label("☠ Defeat the BOSS to unlock next floor");
        bossHint.setStyle("-fx-text-fill: #FF174488; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        roomListContainer.getChildren().addAll(hint, bossHint);

        // Legend icon
        HBox legend = buildLegend();
        roomListContainer.getChildren().add(legend);
    }

    // ── Descent prompt ─────────────────────────────────────

    private void showDescentPrompt(int nextFloor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("FLOOR CLEARED");
        alert.setHeaderText("Floor cleared! Ready to descend.");
        alert.setContentText("Proceed to Floor " + nextFloor + "?\n\nHP partially restored.");
        alert.getDialogPane().setStyle(
            "-fx-background-color: #0C1220;" +
            "-fx-border-color: #00E5FF;" +
            "-fx-border-width: 1;" +
            "-fx-font-family: 'Courier New';"
        );
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) engine.descend();
        });
    }

    private void refreshCurrentRoomInfo() {
        if (currentRoomPanel == null) return;
        DungeonManager dm = engine.getDungeonManager();
        VBox newPanel = buildCurrentRoomPanel(dm);
        // Cari parent dan replace
        if (currentRoomPanel.getParent() instanceof VBox parent) {
            int idx = parent.getChildren().indexOf(currentRoomPanel);
            if (idx >= 0) {
                parent.getChildren().set(idx, newPanel);
                currentRoomPanel = newPanel;
            }
        }
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
            showInfoAlert("📦 LOOT ROOM", "The cache is empty. Someone got here first.");
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
                "-fx-background-color: #0C1220;" +
                "-fx-font-family: 'Courier New', monospace;" +
                "-fx-font-size: 11px;");
        // Style button OK
        alert.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK)
             .setStyle("-fx-background-color: #00E5FF22; -fx-text-fill: #00E5FF;" +
                       "-fx-border-color: #00E5FF; -fx-cursor: hand;");
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
