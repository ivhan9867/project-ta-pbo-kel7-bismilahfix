package arclightcity.ui.view;
import arclightcity.entity.stats.StatType;

import arclightcity.dungeon.*;
import arclightcity.engine.GameEngine;
import arclightcity.entity.enemy.Enemy;
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

    public DungeonMapView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
        wireEngineListeners();
    }

    private void wireEngineListeners() {
        engine.setOnDungeonEvent(event -> {
            javafx.application.Platform.runLater(() -> {
                switch (event.type) {
                    case COMBAT_STARTED -> router.showCombat();
                    case EVENT_ENCOUNTERED -> router.showEvent(event.dungeonEvent);
                    case SHOP_OPENED -> router.showShop();
                    case GAME_OVER -> router.showGameOver();
                    case READY_FOR_NEXT_FLOOR -> showDescentPrompt(event.intValue);
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

        // Map grid
        root.getChildren().add(buildMapGrid(floor));

        // Current room info
        root.getChildren().add(buildCurrentRoomPanel(dm));

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

    // ── Map grid ──────────────────────────────────────────

    private ScrollPane buildMapGrid(Floor floor) {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(220);
        scroll.setMaxHeight(220);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #050810; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        if (floor == null) {
            Label none = new Label("No dungeon active");
            none.setStyle("-fx-text-fill: #5A6A80; -fx-padding: 20;");
            scroll.setContent(none);
            return scroll;
        }

        // Build linear path display
        HBox mapRow = new HBox(4);
        mapRow.setPadding(new Insets(16));
        mapRow.setAlignment(Pos.CENTER_LEFT);

        List<Room> rooms = floor.getRooms();
        int currentIdx = floor.getCurrentRoomIndex();

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            boolean isCurrent = (i == currentIdx);
            boolean isAvailable = room.isVisited() || isCurrent;

            VBox roomNode = buildRoomNode(room, isCurrent, isAvailable, i);
            mapRow.getChildren().add(roomNode);

            // Connector line
            if (i < rooms.size() - 1) {
                Label connector = new Label("—");
                connector.setStyle("-fx-text-fill: #1C2E44; -fx-font-size: 12px;");
                mapRow.getChildren().add(connector);
            }
        }

        scroll.setContent(mapRow);
        return scroll;
    }

    private VBox buildRoomNode(Room room, boolean isCurrent, boolean isVisited, int index) {
        VBox node = new VBox(2);
        node.setAlignment(Pos.CENTER);
        node.setPrefSize(36, 44);

        String icon = getRoomIcon(room.getType());
        String color = getRoomColor(room.getType());

        Label iconLabel = new Label(icon);
        String iconStyle = "-fx-font-size: 16px; -fx-text-fill: ";
        if (!isVisited)      iconStyle += "#1C2E44;";
        else if (room.isCleared()) iconStyle += "#2A3A50;";
        else                 iconStyle += color + ";";

        iconLabel.setStyle(iconStyle);

        Label numLabel = new Label(String.valueOf(index));
        numLabel.setStyle("-fx-font-size: 8px; -fx-font-family: 'Courier New'; -fx-text-fill: #5A6A80;");

        String borderStyle;
        if (isCurrent) {
            borderStyle =
                "-fx-border-color: #00E5FF;" +
                "-fx-border-width: 2;" +
                "-fx-background-color: #00E5FF11;" +
                "-fx-effect: dropshadow(gaussian, #00E5FF, 8, 0.5, 0, 0);";
        } else if (room.isCleared()) {
            borderStyle = "-fx-border-color: #2A3A50; -fx-border-width: 1;";
        } else if (isVisited) {
            borderStyle = "-fx-border-color: " + color + "55; -fx-border-width: 1;";
        } else {
            borderStyle = "-fx-border-color: #1C2E44; -fx-border-width: 1;";
        }

        node.setStyle("-fx-padding: 4; " + borderStyle);
        node.getChildren().addAll(iconLabel, numLabel);

        Tooltip tip = new Tooltip(room.getType().name() + (room.isCleared() ? " (CLEARED)" : ""));
        tip.setStyle("-fx-background-color: #0C1220; -fx-text-fill: #E0E8F0; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        Tooltip.install(node, tip);

        return node;
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
        Label typeLabel = new Label(getRoomIcon(current.getType()) + "  " + current.getType().name());
        typeLabel.setStyle(
            "-fx-text-fill: " + color + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        Label desc = new Label(getRoomDescription(current));
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #8899AA; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        // Floor progress
        Floor floor = dm.getCurrentFloor();
        if (floor != null) {
            int cleared = floor.getClearedRooms();
            int total   = floor.getTotalRooms();
            Label progress = new Label("PROGRESS: " + cleared + "/" + total + " rooms cleared");
            progress.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
            box.getChildren().addAll(typeLabel, desc, progress);
        } else {
            box.getChildren().addAll(typeLabel, desc);
        }

        return box;
    }

    // ── Next rooms ────────────────────────────────────────

    private void refreshNextRooms(DungeonManager dm) {
        roomListContainer.getChildren().clear();

        Label title = UIFactory.sectionTitle("▼ CHOOSE NEXT ROOM");
        roomListContainer.getChildren().add(title);

        List<Room> available = engine.getDungeonManager().getAvailableNextRooms();

        if (available.isEmpty()) {
            Label none = new Label("No rooms available — floor complete!");
            none.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            roomListContainer.getChildren().add(none);

            Button descend = UIFactory.btnGold("▼ DESCEND TO NEXT FLOOR");
            descend.setOnAction(e -> engine.descend());
            roomListContainer.getChildren().add(descend);
            return;
        }

        for (Room room : available) {
            Button roomBtn = buildRoomButton(room);
            roomListContainer.getChildren().add(roomBtn);
        }
    }

    private Button buildRoomButton(Room room) {
        String color = getRoomColor(room.getType());
        String icon  = getRoomIcon(room.getType());

        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setText(icon + "  " + room.getType().name() + "  [" + room.getRoomIndex() + "]");
        btn.setStyle(
            "-fx-background-color: " + color + "11;" +
            "-fx-border-color: " + color + "55;" +
            "-fx-border-width: 1 1 1 3;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 10 12;" +
            "-fx-cursor: hand;" +
            "-fx-alignment: center-left;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle()
                .replace(color + "11", color + "22")
                .replace(color + "55", color)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle()
                .replace(color + "22", color + "11")
                .replace(color + ";-fx-border-width", color + "55;-fx-border-width")));

        btn.setOnAction(e -> engine.moveToRoom(room.getRoomIndex()));
        return btn;
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
        // Rebuild room info section — UI refresh
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
