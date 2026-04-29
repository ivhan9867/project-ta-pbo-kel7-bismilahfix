package arclightcity.ui.view;

import arclightcity.dungeon.Floor;
import arclightcity.dungeon.ProceduralGenerator;
import arclightcity.dungeon.Room;
import arclightcity.engine.GameEngine;
import javafx.animation.*;
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
 * DungeonGridMap v2 — full grid dungeon map dengan fog of war.
 *
 * Perubahan dari v1:
 * - Grid penuh COLS×ROWS (semua tile berisi event)
 * - Fog of War 3 state: HIDDEN / VISIBLE / VISITED
 * - Koneksi cardinal H/V (garis horizontal dan vertikal saja)
 * - Player bebas backtrack ke tile yang sudah VISITED
 * - Boss tile = objective untuk DESCEND
 */
public class DungeonGridMap extends StackPane {

    private static final int TILE_SIZE = 42;
    private static final int TILE_GAP  = 6;
    private static final int COLS      = ProceduralGenerator.COLS;

    private static final Color BG     = Color.web("#050810");
    private static final Color CYAN   = Color.web("#00E5FF");
    private static final Color YELLOW = Color.web("#FFD600");
    private static final Color RED    = Color.web("#FF1744");
    private static final Color GREEN  = Color.web("#00E676");
    private static final Color PURPLE = Color.web("#AA00FF");
    private static final Color ORANGE = Color.web("#FF6B00");

    private final GameEngine engine;
    private final Runnable   onRoomSelected;
    private       Floor      floor;
    private       int        rows;

    private double  playerVisualX, playerVisualY;
    private int     playerTileIdx;

    private final Set<Integer> visitedTiles = new HashSet<>();
    private final Set<Integer> visibleTiles = new HashSet<>();

    private final Canvas          canvas;
    private final GraphicsContext gc;
    private       Timeline        moveAnim;

    public DungeonGridMap(GameEngine engine, Runnable onRoomSelected) {
        this.engine         = engine;
        this.onRoomSelected = onRoomSelected;
        this.floor = engine.getDungeonManager() != null
                ? engine.getDungeonManager().getCurrentFloor() : null;
        this.rows = floor != null ? (floor.getTotalRooms() + COLS - 1) / COLS : 3;

        int w = COLS * (TILE_SIZE + TILE_GAP) + TILE_GAP;
        int h = rows * (TILE_SIZE + TILE_GAP) + TILE_GAP;
        canvas = new Canvas(w, h);
        gc     = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
        setStyle("-fx-background-color: #050810;");

        initFog();
        syncPlayer();
        draw();
        canvas.setOnMouseClicked(this::handleClick);
    }

    // ── Init ─────────────────────────────────────────────────

    private void initFog() {
        visitedTiles.clear();
        visibleTiles.clear();
        if (floor == null) return;
        for (Room r : floor.getRooms()) {
            if (r.isVisited()) {
                visitedTiles.add(r.getRoomIndex());
                revealAround(r.getRoomIndex());
            }
        }
        visibleTiles.add(0); // start selalu visible
    }

    private void revealAround(int idx) {
        int r = idx / COLS, c = idx % COLS;
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < COLS) {
                int ni = nr * COLS + nc;
                if (ni < floor.getTotalRooms() && !visitedTiles.contains(ni))
                    visibleTiles.add(ni);
            }
        }
    }

    private void syncPlayer() {
        if (floor == null) return;
        playerTileIdx = floor.getCurrentRoomIndex();
        playerVisualX = tileX(playerTileIdx % COLS);
        playerVisualY = tileY(playerTileIdx / COLS);
    }

    // ── Draw ─────────────────────────────────────────────────

    public void draw() {
        gc.setFill(BG);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (floor == null) return;

        drawConnections();
        List<Room> rooms = floor.getRooms();
        for (int i = 0; i < rooms.size(); i++) drawTile(rooms.get(i), i);
        drawPlayer();
    }

    private void drawConnections() {
        List<Room> rooms = floor.getRooms();
        gc.setLineDashes(0);

        for (int i = 0; i < rooms.size(); i++) {
            int r = i / COLS, c = i % COLS;
            // Gambar ke kanan
            if (c + 1 < COLS && i + 1 < rooms.size()) drawLine(i, i + 1, r, c, r, c + 1);
            // Gambar ke bawah
            if (r + 1 < rows && i + COLS < rooms.size()) drawLine(i, i + COLS, r, c, r + 1, c);
        }
    }

    private void drawLine(int a, int b, int ar, int ac, int br, int bc) {
        boolean aVis = visitedTiles.contains(a) || visibleTiles.contains(a);
        boolean bVis = visitedTiles.contains(b) || visibleTiles.contains(b);
        if (!aVis && !bVis) return;

        boolean aVisit = visitedTiles.contains(a);
        boolean bVisit = visitedTiles.contains(b);
        boolean curAdj = (a == playerTileIdx || b == playerTileIdx);

        // Titik pusat tile, lalu potong ke tepi
        double ax = tileX(ac) + TILE_SIZE / 2.0;
        double ay = tileY(ar) + TILE_SIZE / 2.0;
        double bx = tileX(bc) + TILE_SIZE / 2.0;
        double by = tileY(br) + TILE_SIZE / 2.0;
        double half = TILE_SIZE / 2.0 + 2;

        if (ac == bc) { ay += half; by -= half; } // vertikal
        else           { ax += half; bx -= half; } // horizontal

        Color col;
        double lw;
        if (curAdj && aVis && bVis) {
            col = Color.web("#00E5FF66"); lw = 2;
            gc.setLineDashes(0);
        } else if (aVisit && bVisit) {
            col = Color.web("#1C2E4455"); lw = 1;
            gc.setLineDashes(0);
        } else {
            col = Color.web("#1C2E4488"); lw = 1;
            gc.setLineDashes(3, 4);
        }

        gc.setStroke(col);
        gc.setLineWidth(lw);
        gc.strokeLine(ax, ay, bx, by);
        gc.setLineDashes(0);
    }

    private void drawTile(Room room, int idx) {
        int col = idx % COLS, row = idx / COLS;
        double x = tileX(col), y = tileY(row);

        boolean isCur     = (idx == playerTileIdx);
        boolean isVisited = visitedTiles.contains(idx);
        boolean isVisible = visibleTiles.contains(idx);
        boolean isHidden  = !isCur && !isVisited && !isVisible;
        boolean isBoss    = room.getType() == Room.RoomType.BOSS;
        boolean isAdjReach= isAdjacent(idx) && !isVisited;

        // Hidden tile — hanya kotak gelap + dot kecil
        if (isHidden) {
            gc.setFill(Color.web("#0C1220"));
            gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, 4, 4);
            gc.setFill(Color.web("#1C2E4444"));
            gc.fillOval(x + TILE_SIZE/2.0 - 2, y + TILE_SIZE/2.0 - 2, 4, 4);
            return;
        }

        // Background
        Color bg = isCur      ? Color.web(isBoss ? "#FF000022" : "#00E5FF15")
                 : isVisited  ? Color.web(isBoss ? "#FF000018" : "#080D18")
                 : isBoss     ? Color.web("#FF174412")
                              : roomColor(room.getType()).deriveColor(0,1,1,0.08);
        gc.setFill(bg);
        gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, 4, 4);

        // Border
        Color border = isCur    ? CYAN
                     : isBoss   ? Color.web("#FF1744AA")
                     : isVisited? Color.web("#1C2E4466")
                                : roomColor(room.getType()).deriveColor(0,1,1,0.5);
        gc.setStroke(border);
        gc.setLineWidth(isCur ? 2 : 1);
        gc.strokeRoundRect(x+.5, y+.5, TILE_SIZE-1, TILE_SIZE-1, 4, 4);

        // Boss outer glow
        if (isBoss && !isVisited) {
            gc.setStroke(Color.web("#FF174433"));
            gc.setLineWidth(4);
            gc.strokeRoundRect(x-2, y-2, TILE_SIZE+4, TILE_SIZE+4, 6, 6);
        }

        // Icon
        Color icColor = isCur                          ? roomColor(room.getType())
                      : isVisited && room.isCleared()  ? Color.web("#2A3A5066")
                      : isVisited                      ? roomColor(room.getType()).deriveColor(0,.5,.8,.7)
                                                       : roomColor(room.getType()).deriveColor(0,.8,.9,.65);
        gc.setFill(icColor);
        gc.setFont(Font.font("Courier New", 15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(roomIcon(room.getType()), x + TILE_SIZE/2.0, y + TILE_SIZE/2.0 + 6);

        // Cleared mark
        if (room.isCleared() && !isCur) {
            gc.setFill(Color.web("#00E67288"));
            gc.setFont(Font.font("Courier New", 8));
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText("✓", x + TILE_SIZE - 3, y + 10);
        }

        // Index label
        gc.setFill(Color.web("#5A6A8055"));
        gc.setFont(Font.font("Courier New", 7));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(String.valueOf(idx), x + 3, y + 9);

        // BOSS label
        if (isBoss) {
            gc.setFill(Color.web("#FF174488"));
            gc.setFont(Font.font("Courier New", 7));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("BOSS", x + TILE_SIZE/2.0, y + TILE_SIZE - 5);
        }

        // Reachable dot
        if (isAdjReach) {
            gc.setFill(roomColor(room.getType()).deriveColor(0,1,1,0.8));
            gc.fillOval(x + TILE_SIZE/2.0 - 3, y + TILE_SIZE - 9, 6, 5);
        }

        gc.setTextAlign(TextAlignment.CENTER);
    }

    private void drawPlayer() {
        double px = playerVisualX + TILE_SIZE / 2.0;
        double py = playerVisualY + TILE_SIZE / 2.0;
        gc.setFill(Color.web("#00E5FF18")); gc.fillOval(px-15, py-15, 30, 30);
        gc.setFill(Color.web("#00E5FF33")); gc.fillOval(px-9,  py-9,  18, 18);
        gc.setFill(CYAN);
        gc.setFont(Font.font("Courier New", 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("◈", px, py + 5);
        gc.setStroke(CYAN); gc.setLineWidth(1.5);
        gc.strokeOval(px-10, py-10, 20, 20);
    }

    // ── Click ─────────────────────────────────────────────────

    private void handleClick(MouseEvent e) {
        if (floor == null) return;
        int col = (int)((e.getX() - TILE_GAP) / (TILE_SIZE + TILE_GAP));
        int row = (int)((e.getY() - TILE_GAP) / (TILE_SIZE + TILE_GAP));
        if (row < 0 || row >= rows || col < 0 || col >= COLS) return;
        int idx = row * COLS + col;
        if (idx >= floor.getTotalRooms()) return;
        if (!isAdjacent(idx)) return;
        if (!visibleTiles.contains(idx) && !visitedTiles.contains(idx)) return;

        double sx = playerVisualX, sy = playerVisualY;
        double ex = tileX(col),    ey = tileY(row);
        if (moveAnim != null) moveAnim.stop();

        moveAnim = new Timeline(
            new KeyFrame(Duration.ZERO),
            new KeyFrame(Duration.millis(240))
        );
        moveAnim.currentTimeProperty().addListener((obs, o, n) -> {
            double t = Math.min(1.0, n.toMillis() / 240.0);
            t = t < .5 ? 4*t*t*t : 1 - Math.pow(-2*t+2, 3)/2;
            playerVisualX = sx + (ex-sx)*t;
            playerVisualY = sy + (ey-sy)*t;
            draw();
        });
        moveAnim.setOnFinished(ev -> {
            playerVisualX = ex; playerVisualY = ey;
            playerTileIdx = idx;
            visitedTiles.add(idx);
            visibleTiles.remove(idx);
            revealAround(idx);
            draw();
            engine.moveToRoom(idx);
            if (onRoomSelected != null) onRoomSelected.run();
        });
        moveAnim.play();
    }

    private boolean isAdjacent(int idx) {
        int pr = playerTileIdx / COLS, pc = playerTileIdx % COLS;
        int tr = idx / COLS,           tc = idx % COLS;
        return (Math.abs(pr-tr) == 1 && pc == tc) || (pr == tr && Math.abs(pc-tc) == 1);
    }

    // ── Refresh ───────────────────────────────────────────────

    public void refresh() {
        floor = engine.getDungeonManager() != null
                ? engine.getDungeonManager().getCurrentFloor() : null;
        if (floor == null) { draw(); return; }
        rows = (floor.getTotalRooms() + COLS - 1) / COLS;
        for (Room r : floor.getRooms()) {
            if (r.isVisited()) {
                visitedTiles.add(r.getRoomIndex());
                visibleTiles.remove(r.getRoomIndex());
                revealAround(r.getRoomIndex());
            }
        }
        syncPlayer();
        draw();
    }

    // ── Helpers ───────────────────────────────────────────────

    private double tileX(int col) { return TILE_GAP + col * (TILE_SIZE + TILE_GAP); }
    private double tileY(int row) { return TILE_GAP + row * (TILE_SIZE + TILE_GAP); }

    public double getPreferredHeight() { return rows * (TILE_SIZE + TILE_GAP) + TILE_GAP; }

    private String roomIcon(Room.RoomType t) {
        return switch (t) {
            case EMPTY -> "○"; case ENEMY -> "◆"; case ELITE -> "◈";
            case BOSS  -> "☠"; case LOOT  -> "☆"; case REST  -> "♥";
            case EVENT -> "?"; case SHOP  -> "$"; case TRAP  -> "!";
        };
    }

    private Color roomColor(Room.RoomType t) {
        return switch (t) {
            case EMPTY -> Color.web("#5A6A80"); case ENEMY -> RED;
            case ELITE -> PURPLE;               case BOSS  -> Color.web("#FF0000");
            case LOOT  -> YELLOW;               case REST  -> GREEN;
            case EVENT -> CYAN;                 case SHOP  -> ORANGE;
            case TRAP  -> ORANGE;
        };
    }
}
