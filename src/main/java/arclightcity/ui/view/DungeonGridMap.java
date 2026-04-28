package arclightcity.ui.view;

import arclightcity.dungeon.DungeonManager;
import arclightcity.dungeon.Floor;
import arclightcity.dungeon.Room;
import arclightcity.engine.GameEngine;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.*;

/**
 * DungeonGridMap — tampilan dungeon sebagai grid 2D.
 *
 * ✨ Fitur:
 *  - Grid NxM tile yang di-generate dari Floor rooms
 *  - Player icon bergerak antar tile dengan klik
 *  - Fog of war — tile belum dikunjungi tampil gelap
 *  - Icon per room type (♦=enemy, ☆=loot, ♥=rest, ?=event, $=shop, !=trap, ☠=boss)
 *  - Highlight tile yang bisa dituju (adjacent & accessible)
 *  - Animasi slide player bergerak ke tile baru
 *
 * Layout grid:
 *   Rooms di-arrange dalam grid dengan koneksi sebagai jalan antar tile.
 *   Room index 0 = pojok kiri atas, index terakhir = boss (pojok kanan bawah)
 */
public class DungeonGridMap extends StackPane {

    // ── Tile config ───────────────────────────────────────────
    private static final int TILE_SIZE   = 44;
    private static final int TILE_GAP    = 4;
    private static final int COLS        = 5;   // grid width

    // ── State ─────────────────────────────────────────────────
    private final GameEngine    engine;
    private final Runnable      onRoomSelected;  // callback saat tile diklik
    private       Floor         floor;
    private       int           playerCol, playerRow;
    private       double        playerVisualX, playerVisualY; // untuk animasi smooth

    // ── Grid data ─────────────────────────────────────────────
    private int[][] grid;       // grid[row][col] = room index, -1 = empty
    private int     gridRows;

    // ── Canvas ────────────────────────────────────────────────
    private final Canvas canvas;
    private final GraphicsContext gc;

    // ── Animation ─────────────────────────────────────────────
    private Timeline moveAnimation;
    private Set<Integer> reachableRoomIds = new HashSet<>();

    // ── Colors ────────────────────────────────────────────────
    private static final Color BG          = Color.web("#050810");
    private static final Color TILE_DARK   = Color.web("#0C1220");
    private static final Color TILE_FOG    = Color.web("#080D18");
    private static final Color BORDER_DIM  = Color.web("#1C2E44");
    private static final Color CYAN        = Color.web("#00E5FF");
    private static final Color YELLOW      = Color.web("#FFD600");
    private static final Color RED         = Color.web("#FF1744");
    private static final Color GREEN       = Color.web("#00E676");
    private static final Color PURPLE      = Color.web("#AA00FF");
    private static final Color ORANGE      = Color.web("#FF6B00");
    private static final Color PLAYER_CLR  = Color.web("#00E5FF");

    // ── Constructor ──────────────────────────────────────────

    public DungeonGridMap(GameEngine engine, Runnable onRoomSelected) {
        this.engine         = engine;
        this.onRoomSelected = onRoomSelected;

        // Ambil floor dari DungeonManager
        DungeonManager dm = engine.getDungeonManager();
        this.floor = dm != null ? dm.getCurrentFloor() : null;

        // Hitung ukuran canvas
        int canvasW = COLS * (TILE_SIZE + TILE_GAP) + TILE_GAP;
        int maxRows  = floor != null ? (floor.getTotalRooms() + COLS - 1) / COLS : 3;
        gridRows     = maxRows;
        int canvasH  = gridRows * (TILE_SIZE + TILE_GAP) + TILE_GAP;

        canvas = new Canvas(canvasW, canvasH);
        gc     = canvas.getGraphicsContext2D();

        getChildren().add(canvas);
        setStyle("-fx-background-color: #050810;");
        setAlignment(Pos.CENTER);

        // Build grid dari rooms
        buildGrid();

        // Set posisi awal player
        updatePlayerPosition();

        // Hitung reachable rooms
        updateReachable();

        // Draw
        draw();

        // Click handler
        canvas.setOnMouseClicked(this::handleClick);
    }

    // ── Grid Builder ──────────────────────────────────────────

    /**
     * Arrange rooms dalam grid COLS x N.
     * Room 0 = top-left, rooms berikutnya kiri ke kanan, atas ke bawah.
     * Boss room selalu di tengah baris terakhir.
     */
    private void buildGrid() {
        if (floor == null) return;
        List<Room> rooms = floor.getRooms();
        int total = rooms.size();
        gridRows = (total + COLS - 1) / COLS;

        grid = new int[gridRows][COLS];
        for (int[] row : grid) Arrays.fill(row, -1);

        for (int i = 0; i < total; i++) {
            int row = i / COLS;
            int col = i % COLS;
            grid[row][col] = i;
        }
    }

    /** Update posisi player berdasarkan currentRoomIndex di Floor */
    private void updatePlayerPosition() {
        if (floor == null) return;
        int currentIdx = floor.getCurrentRoomIndex();
        playerRow = currentIdx / COLS;
        playerCol = currentIdx % COLS;
        playerVisualX = tileX(playerCol);
        playerVisualY = tileY(playerRow);
    }

    /** Hitung room index yang bisa dijangkau dari posisi saat ini */
    private void updateReachable() {
        reachableRoomIds.clear();
        if (floor == null) return;
        Room current = floor.getCurrentRoom();
        if (current == null) return;
        // Tambahkan semua next rooms yang accessible
        reachableRoomIds.addAll(current.getNextRoomIndexes());
    }

    // ── Drawing ───────────────────────────────────────────────

    public void draw() {
        if (floor == null) {
            drawEmpty();
            return;
        }

        List<Room> rooms = floor.getRooms();
        int currentIdx = floor.getCurrentRoomIndex();

        // Clear
        gc.setFill(BG);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw connections dulu (di belakang tiles)
        drawConnections(rooms);

        // Draw tiles
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < COLS; col++) {
                int roomIdx = grid[row][col];
                if (roomIdx < 0 || roomIdx >= rooms.size()) continue;

                Room room = rooms.get(roomIdx);
                drawTile(room, roomIdx, col, row, currentIdx);
            }
        }

        // Draw player icon (di atas semua tiles)
        drawPlayer();
    }

    private void drawConnections(List<Room> rooms) {
        gc.setStroke(BORDER_DIM);
        gc.setLineWidth(1.5);

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            int fromRow = i / COLS;
            int fromCol = i % COLS;
            double fx = tileX(fromCol) + TILE_SIZE / 2.0;
            double fy = tileY(fromRow) + TILE_SIZE / 2.0;

            for (int nextIdx : room.getNextRoomIndexes()) {
                if (nextIdx >= rooms.size()) continue;
                int toRow = nextIdx / COLS;
                int toCol = nextIdx % COLS;
                double tx = tileX(toCol) + TILE_SIZE / 2.0;
                double ty = tileY(toRow) + TILE_SIZE / 2.0;

                // Warna connection berdasarkan apakah reachable
                boolean isReachablePath = reachableRoomIds.contains(nextIdx)
                        && i == floor.getCurrentRoomIndex();
                gc.setStroke(isReachablePath
                        ? Color.web("#00E5FF55")
                        : Color.web("#1C2E4488"));
                gc.setLineWidth(isReachablePath ? 2 : 1);
                gc.strokeLine(fx, fy, tx, ty);
            }
        }
    }

    private void drawTile(Room room, int roomIdx, int col, int row, int currentIdx) {
        double x = tileX(col);
        double y = tileY(row);

        boolean isCurrent   = (roomIdx == currentIdx);
        boolean isVisited   = room.isVisited();
        boolean isCleared   = room.isCleared();
        boolean isReachable = reachableRoomIds.contains(roomIdx);
        boolean isFog       = !isVisited && !isCurrent;

        // Background tile
        Color bgColor;
        if (isCurrent)        bgColor = Color.web("#00E5FF15");
        else if (isCleared)   bgColor = Color.web("#0C1220");
        else if (isReachable) bgColor = getRoomColor(room.getType()).deriveColor(0, 1, 1, 0.12);
        else if (isFog)       bgColor = TILE_FOG;
        else                  bgColor = TILE_DARK;

        gc.setFill(bgColor);
        gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, 4, 4);

        // Border
        Color borderColor;
        if (isCurrent)        borderColor = CYAN;
        else if (isReachable) borderColor = getRoomColor(room.getType()).deriveColor(0, 1, 1, 0.7);
        else if (isCleared)   borderColor = Color.web("#1C2E4455");
        else if (isFog)       borderColor = Color.web("#1C2E4422");
        else                  borderColor = BORDER_DIM;

        gc.setStroke(borderColor);
        gc.setLineWidth(isCurrent ? 2 : 1);
        gc.strokeRoundRect(x + 0.5, y + 0.5, TILE_SIZE - 1, TILE_SIZE - 1, 4, 4);

        // Fog: tidak tampilkan icon
        if (isFog) {
            gc.setFill(Color.web("#5A6A8044"));
            gc.setFont(Font.font("Courier New", 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("?", x + TILE_SIZE / 2.0, y + TILE_SIZE / 2.0 + 5);
            return;
        }

        // Room icon
        String icon = getRoomIcon(room.getType());
        Color iconColor = isCleared
                ? Color.web("#2A3A5088")
                : getRoomColor(room.getType());
        gc.setFill(iconColor);
        gc.setFont(Font.font("Courier New", 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(icon, x + TILE_SIZE / 2.0, y + TILE_SIZE / 2.0 + 6);

        // Room index (kecil, di pojok kiri atas)
        gc.setFill(Color.web("#5A6A8077"));
        gc.setFont(Font.font("Courier New", 8));
        gc.fillText(String.valueOf(roomIdx), x + 7, y + 10);

        // Cleared checkmark (pojok kanan atas)
        if (isCleared && !isCurrent) {
            gc.setFill(Color.web("#00E67666"));
            gc.setFont(Font.font("Courier New", 9));
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText("✓", x + TILE_SIZE - 3, y + 10);
        }

        // Reachable pulse indicator (bawah tile)
        if (isReachable) {
            gc.setFill(getRoomColor(room.getType()).deriveColor(0, 1, 1, 0.6));
            gc.fillOval(x + TILE_SIZE / 2.0 - 3, y + TILE_SIZE - 7, 6, 4);
        }

        gc.setTextAlign(TextAlignment.CENTER);
    }

    private void drawPlayer() {
        double px = playerVisualX + TILE_SIZE / 2.0;
        double py = playerVisualY + TILE_SIZE / 2.0;

        // Outer glow
        gc.setFill(Color.web("#00E5FF22"));
        gc.fillOval(px - 14, py - 14, 28, 28);

        // Inner circle
        gc.setFill(Color.web("#00E5FF44"));
        gc.fillOval(px - 9, py - 9, 18, 18);

        // Player icon
        gc.setFill(PLAYER_CLR);
        gc.setFont(Font.font("Courier New", 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("◈", px, py + 5);

        // Player outline
        gc.setStroke(CYAN);
        gc.setLineWidth(1.5);
        gc.strokeOval(px - 10, py - 10, 20, 20);
    }

    private void drawEmpty() {
        gc.setFill(BG);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.web("#5A6A80"));
        gc.setFont(Font.font("Courier New", 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("No dungeon active", canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    // ── Click Handler ─────────────────────────────────────────

    private void handleClick(MouseEvent event) {
        if (floor == null) return;

        double mouseX = event.getX();
        double mouseY = event.getY();

        // Hitung tile yang diklik
        int col = (int)((mouseX - TILE_GAP) / (TILE_SIZE + TILE_GAP));
        int row = (int)((mouseY - TILE_GAP) / (TILE_SIZE + TILE_GAP));

        if (row < 0 || row >= gridRows || col < 0 || col >= COLS) return;
        if (grid[row][col] < 0) return;

        int clickedRoomIdx = grid[row][col];

        // Hanya bisa pindah ke reachable room
        if (!reachableRoomIds.contains(clickedRoomIdx)) return;

        // Animasi player bergerak
        animateMoveTo(col, row, () -> {
            // Setelah animasi, pindah ke room
            boolean moved = engine.moveToRoom(clickedRoomIdx);
            if (moved) {
                playerCol = col;
                playerRow = row;
                updateReachable();
                draw();
                if (onRoomSelected != null) onRoomSelected.run();
            }
        });
    }

    private void animateMoveTo(int targetCol, int targetRow, Runnable onFinish) {
        if (moveAnimation != null) moveAnimation.stop();

        double startX = playerVisualX;
        double startY = playerVisualY;
        double endX   = tileX(targetCol);
        double endY   = tileY(targetRow);

        moveAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, e -> {
                playerVisualX = startX;
                playerVisualY = startY;
                draw();
            }),
            new KeyFrame(Duration.millis(250), e -> {
                playerVisualX = endX;
                playerVisualY = endY;
                draw();
            })
        );

        // Interpolasi smooth
        moveAnimation.currentTimeProperty().addListener((obs, old, now) -> {
            double t = now.toMillis() / 250.0;
            t = Math.min(1, t);
            // Ease in-out
            t = t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
            playerVisualX = startX + (endX - startX) * t;
            playerVisualY = startY + (endY - startY) * t;
            draw();
        });

        moveAnimation.setOnFinished(e -> {
            playerVisualX = endX;
            playerVisualY = endY;
            draw();
            if (onFinish != null) onFinish.run();
        });
        moveAnimation.play();
    }

    // ── Public Refresh ────────────────────────────────────────

    /** Dipanggil dari DungeonMapView saat floor berubah atau room di-clear */
    public void refresh() {
        DungeonManager dm = engine.getDungeonManager();
        this.floor = dm != null ? dm.getCurrentFloor() : null;
        buildGrid();
        updatePlayerPosition();
        updateReachable();
        draw();
    }

    // ── Helpers ───────────────────────────────────────────────

    private double tileX(int col) { return TILE_GAP + col * (TILE_SIZE + TILE_GAP); }
    private double tileY(int row) { return TILE_GAP + row * (TILE_SIZE + TILE_GAP); }

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

    private Color getRoomColor(Room.RoomType type) {
        return switch (type) {
            case EMPTY   -> Color.web("#5A6A80");
            case ENEMY   -> Color.web("#FF1744");
            case ELITE   -> Color.web("#AA00FF");
            case BOSS    -> Color.web("#FF0000");
            case LOOT    -> Color.web("#FFD600");
            case REST    -> Color.web("#00E676");
            case EVENT   -> Color.web("#00E5FF");
            case SHOP    -> Color.web("#FF6B00");
            case TRAP    -> Color.web("#FF6B00");
        };
    }

    public double getPreferredHeight() {
        return gridRows * (TILE_SIZE + TILE_GAP) + TILE_GAP;
    }
}
