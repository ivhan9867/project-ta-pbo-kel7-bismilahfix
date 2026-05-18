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
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.*;

/**
 * DungeonGridMap v3 — Comprehensive Visual Overhaul
 *
 * Perubahan dari v2:
 *  - Tile lebih besar (52×52px) memanfaatkan game area 560px
 *  - Background grid dots subtle di canvas (scanline cyberpunk feel)
 *  - Tile dengan gradient background per room type
 *  - Hover effect: tile reachable glow + scale visual cue
 *  - Player icon dengan breathing pulse animation (idle)
 *  - Reachable tile: animated dashed border (march effect via Timeline)
 *  - Floor header: nama floor prominent dengan warna tema
 *  - Connection lines lebih tebal dan elegan
 *  - Hidden tiles: subtle cross-hatch pattern (tidak flat hitam)
 *  - Boss tile: double-border merah + pulsing outer glow
 *  - Cleared tiles: distinct desaturated look (bukan sekadar redup)
 *  - Hover: tunjukkan nama room type di tooltip bawah map
 */
public class DungeonGridMap extends StackPane {

    // ── Tile config ───────────────────────────────────────────
    private static final int  TILE_SIZE  = 64;   // 1280px resolution — memanfaatkan 940px game area
    private static final int  TILE_GAP   = 8;    // gap lebih lega
    private static final int  TILE_R     = 6;    // corner radius
    private static final int  COLS       = ProceduralGenerator.COLS;

    // ── Colors ────────────────────────────────────────────────
    private static final Color BG        = Color.web("#050810");
    private static final Color GRID_DOT  = Color.web("#0C1420");
    private static final Color CYAN      = Color.web("#00E5FF");
    private static final Color YELLOW    = Color.web("#FFD600");
    private static final Color RED       = Color.web("#FF1744");
    private static final Color GREEN     = Color.web("#00E676");
    private static final Color PURPLE    = Color.web("#AA00FF");
    private static final Color ORANGE    = Color.web("#FF6B00");
    private static final Color DIM       = Color.web("#1C2E44");

    // ── State ─────────────────────────────────────────────────
    private final GameEngine engine;
    private final Runnable   onRoomSelected;
    private       Floor      floor;
    private       int        rows;

    private double playerVisualX, playerVisualY;
    private int    playerTileIdx;
    private int    hoveredTileIdx = -1;   // tile yang di-hover mouse

    private final Set<Integer> visitedTiles = new HashSet<>();
    private final Set<Integer> visibleTiles = new HashSet<>();

    // ── Canvas ────────────────────────────────────────────────
    private final Canvas          canvas;
    private final GraphicsContext gc;

    // ── Animations ────────────────────────────────────────────
    private Timeline moveAnim;
    private Timeline pulseAnim;        // player idle breathing
    private Timeline marchAnim;        // reachable tile dashed border march
    private double   pulseAlpha  = 0.6;
    private double   marchOffset = 0;  // 0–8 untuk march effect

    // ── Constructor ──────────────────────────────────────────

    public DungeonGridMap(GameEngine engine, Runnable onRoomSelected) {
        this.engine         = engine;
        this.onRoomSelected = onRoomSelected;
        this.floor = engine.getDungeonManager() != null
                ? engine.getDungeonManager().getCurrentFloor() : null;
        this.rows  = floor != null ? (floor.getTotalRooms() + COLS - 1) / COLS : 3;

        int canvasW = COLS * (TILE_SIZE + TILE_GAP) + TILE_GAP;
        int canvasH = rows * (TILE_SIZE + TILE_GAP) + TILE_GAP;

        canvas = new Canvas(canvasW, canvasH);
        gc     = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
        setStyle("-fx-background-color: #050810;");

        initFog();
        // syncPlayer dipanggil nanti setelah player diposisikan
        startAnimations();
        draw();

        canvas.setOnMouseClicked(this::handleClick);
        canvas.setOnMouseMoved(this::handleHover);
        canvas.setOnMouseExited(e -> { hoveredTileIdx = -1; draw(); });
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
        // getCurrentRoomIndex() mungkin belum benar saat initFog dipanggil pertama kali
        // (player belum diposisikan). syncPlayer() akan memanggil revealFromCurrentRoom()
        // setelah player benar-benar diposisikan.
    }

    /** Reveal dari posisi player saat ini — dipanggil setelah floor + player siap */
    public void revealFromCurrentRoom() {
        if (floor == null) return;
        int idx = floor.getCurrentRoomIndex();
        visibleTiles.add(idx);
        revealAround(idx);
        draw();
    }

    /** Reveal semua tiles di floor saat ini (MAP_REVEALED event) */
    public void revealAll() {
        if (floor == null) return;
        for (int i = 0; i < floor.getTotalRooms(); i++) {
            visibleTiles.add(i);
        }
        draw();
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
        int newIdx = floor.getCurrentRoomIndex();
        // Hanya reveal tile current jika belum pernah terlihat
        // Ini mencegah reveal prematur room 0 saat setFloor() dipanggil
        if (newIdx >= 0 && !visibleTiles.contains(newIdx)) {
            visibleTiles.add(newIdx);
            revealAround(newIdx);
        }
        playerTileIdx  = newIdx;
        playerVisualX  = tileX(playerTileIdx % COLS);
        playerVisualY  = tileY(playerTileIdx / COLS);
    }

    // ── Animations ────────────────────────────────────────────

    private void startAnimations() {
        // Player breathing pulse — 0.4 → 1.0, cycle 2.4 detik
        pulseAnim = new Timeline(
            new KeyFrame(Duration.ZERO,         e -> pulseAlpha = 0.4),
            new KeyFrame(Duration.millis(1200), e -> pulseAlpha = 1.0),
            new KeyFrame(Duration.millis(2400), e -> pulseAlpha = 0.4)
        );
        pulseAnim.currentTimeProperty().addListener((obs, o, n) -> {
            double t = (n.toMillis() % 2400) / 2400.0;
            // Sinusoidal untuk smooth
            pulseAlpha = 0.4 + 0.6 * (0.5 + 0.5 * Math.sin(t * 2 * Math.PI - Math.PI/2));
            draw();
        });
        pulseAnim.setCycleCount(Timeline.INDEFINITE);
        pulseAnim.play();

        // March animation untuk reachable tile border — 8 step, 60ms per step
        marchAnim = new Timeline(
            new KeyFrame(Duration.millis(80), e -> {
                marchOffset = (marchOffset + 1) % 10;
                draw();
            })
        );
        marchAnim.setCycleCount(Timeline.INDEFINITE);
        marchAnim.play();
    }

    // ── Draw ─────────────────────────────────────────────────

    public void draw() {
        // Clear
        gc.setFill(BG);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Layer 0: Subtle grid dots background
        drawBackgroundGrid();

        if (floor == null) {
            gc.setFill(Color.web("#5A6A80"));
            gc.setFont(Font.font("Courier New", 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No dungeon active", canvas.getWidth()/2, canvas.getHeight()/2);
            return;
        }

        // Layer 1: Connection lines
        drawConnections();

        // Layer 2: Tiles
        List<Room> rooms = floor.getRooms();
        for (int i = 0; i < rooms.size(); i++) drawTile(rooms.get(i), i);

        // Layer 3: Player (di atas semua tile)
        drawPlayer();

        // Layer 4: Hover tooltip
        if (hoveredTileIdx >= 0 && hoveredTileIdx < rooms.size()) {
            drawHoverTooltip(rooms.get(hoveredTileIdx), hoveredTileIdx);
        }
    }

    // ── Background grid dots ──────────────────────────────────

    /**
     * Subtle dot grid di background canvas — memberi feel hologram/cyberpunk
     * tanpa mengganggu readability tile.
     */
    private void drawBackgroundGrid() {
        int spacing = 16;
        gc.setFill(GRID_DOT);
        for (int x = spacing/2; x < canvas.getWidth(); x += spacing) {
            for (int y = spacing/2; y < canvas.getHeight(); y += spacing) {
                gc.fillOval(x - 1, y - 1, 2, 2);
            }
        }
    }

    // ── Connection lines ──────────────────────────────────────

    private void drawConnections() {
        List<Room> rooms = floor.getRooms();
        gc.setLineDashes(0);

        for (int i = 0; i < rooms.size(); i++) {
            int r = i / COLS, c = i % COLS;
            if (c + 1 < COLS && i + 1 < rooms.size())
                drawLine(i, i + 1, r, c, r, c + 1, rooms);
            if (r + 1 < rows && i + COLS < rooms.size())
                drawLine(i, i + COLS, r, c, r + 1, c, rooms);
        }
    }

    private void drawLine(int a, int b, int ar, int ac, int br, int bc,
                          List<Room> rooms) {
        boolean aVis = visitedTiles.contains(a) || visibleTiles.contains(a);
        boolean bVis = visitedTiles.contains(b) || visibleTiles.contains(b);
        if (!aVis && !bVis) return;

        boolean aVisit = visitedTiles.contains(a);
        boolean bVisit = visitedTiles.contains(b);
        boolean curAdj = (a == playerTileIdx || b == playerTileIdx);

        double ax = tileX(ac) + TILE_SIZE / 2.0;
        double ay = tileY(ar) + TILE_SIZE / 2.0;
        double bx = tileX(bc) + TILE_SIZE / 2.0;
        double by = tileY(br) + TILE_SIZE / 2.0;
        double half = TILE_SIZE / 2.0 + 3;

        if (ac == bc) { ay += half; by -= half; }  // vertikal
        else          { ax += half; bx -= half; }  // horizontal

        Color lineColor;
        double lineWidth;
        gc.setLineDashes(0);

        if (curAdj) {
            // Garis dari current player — cyan solid tebal
            lineColor = Color.web("#00E5FF88");
            lineWidth = 2.5;
        } else if (aVisit && bVisit) {
            // Keduanya visited — solid redup
            lineColor = Color.web("#1C2E4466");
            lineWidth = 1.5;
        } else if (aVis && bVis) {
            // Visible tapi belum dikunjungi — dashed
            lineColor = Color.web("#1C2E44AA");
            lineWidth = 1;
            gc.setLineDashes(4, 5);
        } else {
            lineColor = Color.web("#1C2E4444");
            lineWidth = 1;
            gc.setLineDashes(2, 6);
        }

        gc.setStroke(lineColor);
        gc.setLineWidth(lineWidth);
        gc.strokeLine(ax, ay, bx, by);
        gc.setLineDashes(0);
    }

    // ── Tile Drawing ──────────────────────────────────────────

    private void drawTile(Room room, int idx) {
        int col = idx % COLS, row = idx / COLS;
        double x = tileX(col), y = tileY(row);

        boolean isCur      = (idx == playerTileIdx);
        boolean isVisited  = visitedTiles.contains(idx);
        boolean isVisible  = visibleTiles.contains(idx);
        boolean isHidden   = !isCur && !isVisited && !isVisible;
        boolean isBoss     = room.getType() == Room.RoomType.BOSS;
        boolean isReach    = isAdjacent(idx) && !isVisited;
        boolean isHovered  = (idx == hoveredTileIdx);

        if (isHidden) {
            drawHiddenTile(x, y);
            return;
        }

        // ── Background dengan gradient ─────────────────────────
        drawTileBackground(x, y, room, isCur, isVisited, isBoss, isReach, isHovered);

        // ── Border ────────────────────────────────────────────
        drawTileBorder(x, y, room, isCur, isVisited, isBoss, isReach);

        // ── Boss special outer ring ────────────────────────────
        if (isBoss) drawBossRing(x, y);

        // ── Icon ──────────────────────────────────────────────
        drawTileIcon(x, y, room, isCur, isVisited);

        // ── Cleared overlay ───────────────────────────────────
        if (room.isCleared() && !isCur) drawClearedOverlay(x, y);

        // ── Boss label ────────────────────────────────────────
        if (isBoss) drawBossLabel(x, y, isVisited);

        // ── Reachable indicator (marching ants + dot) ─────────
        if (isReach && !isBoss) drawReachableIndicator(x, y, room);

        // ── Hover highlight ───────────────────────────────────
        if (isHovered && isReach) drawHoverHighlight(x, y, room);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setLineDashes(0);
    }

    private void drawHiddenTile(double x, double y) {
        // Background sangat gelap dengan subtle cross pattern
        gc.setFill(Color.web("#08101A"));
        gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, TILE_R, TILE_R);

        // Cross-hatch pattern subtle
        gc.setStroke(Color.web("#0C1825"));
        gc.setLineWidth(0.5);
        for (int i = 4; i < TILE_SIZE; i += 8) {
            gc.strokeLine(x + i, y, x, y + i);
            gc.strokeLine(x + TILE_SIZE, y + i, x + i, y + TILE_SIZE);
        }

        // Tiny dot di tengah
        gc.setFill(Color.web("#1C2E4466"));
        gc.fillOval(x + TILE_SIZE/2.0 - 1.5, y + TILE_SIZE/2.0 - 1.5, 3, 3);
    }

    private void drawTileBackground(double x, double y, Room room,
                                     boolean isCur, boolean isVisited,
                                     boolean isBoss, boolean isReach, boolean isHovered) {
        Color roomClr = roomColor(room.getType());

        if (isCur) {
            // Current tile: gradient dari room color ke gelap
            gc.setFill(Color.web(isBoss ? "#FF000030" : "#00E5FF20"));
            gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, TILE_R, TILE_R);

            // Inner lighter spot
            gc.setFill(Color.web(isBoss ? "#FF000018" : "#00E5FF0A"));
            gc.fillRoundRect(x+4, y+4, TILE_SIZE-8, TILE_SIZE-8, 3, 3);

        } else if (isVisited && room.isCleared()) {
            // Cleared visited — sangat gelap, desaturated
            gc.setFill(Color.web("#070C14"));
            gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, TILE_R, TILE_R);

        } else if (isVisited) {
            // Visited belum cleared — gelap dengan sedikit warna
            gc.setFill(Color.web("#0A1220"));
            gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, TILE_R, TILE_R);
            gc.setFill(roomClr.deriveColor(0, 1, 1, 0.05));
            gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, TILE_R, TILE_R);

        } else if (isHovered && isReach) {
            // Hover + reachable: lebih terang
            gc.setFill(roomClr.deriveColor(0, 1, 1, 0.20));
            gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, TILE_R, TILE_R);
        } else if (isReach) {
            // Reachable — subtle glow background
            gc.setFill(roomClr.deriveColor(0, 1, 1, 0.10));
            gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, TILE_R, TILE_R);
        } else {
            // Visible — sangat subtle
            gc.setFill(roomClr.deriveColor(0, 1, 1, 0.06));
            gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, TILE_R, TILE_R);
        }
    }

    private void drawTileBorder(double x, double y, Room room,
                                 boolean isCur, boolean isVisited,
                                 boolean isBoss, boolean isReach) {
        Color roomClr = roomColor(room.getType());

        if (isCur) {
            // Solid bright border
            gc.setStroke(isBoss ? RED : CYAN);
            gc.setLineWidth(2);
            gc.strokeRoundRect(x+1, y+1, TILE_SIZE-2, TILE_SIZE-2, TILE_R, TILE_R);
            // Inner second border subtle
            gc.setStroke((isBoss ? RED : CYAN).deriveColor(0, 1, 1, 0.3));
            gc.setLineWidth(1);
            gc.strokeRoundRect(x+3, y+3, TILE_SIZE-6, TILE_SIZE-6, 3, 3);

        } else if (isReach) {
            // Marching ants — dashed border yang bergerak
            gc.setStroke(roomClr.deriveColor(0, 1, 1, 0.8));
            gc.setLineWidth(1.5);
            double dash = 5, gap = 4;
            double offset = marchOffset % (dash + gap);
            gc.setLineDashes(dash, gap);
            gc.setLineDashOffset(offset);
            gc.strokeRoundRect(x+1, y+1, TILE_SIZE-2, TILE_SIZE-2, TILE_R, TILE_R);
            gc.setLineDashes(0);
            gc.setLineDashOffset(0);

        } else if (isBoss && !isVisited) {
            gc.setStroke(Color.web("#FF1744CC"));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(x+1, y+1, TILE_SIZE-2, TILE_SIZE-2, TILE_R, TILE_R);

        } else if (isVisited && room.isCleared()) {
            gc.setStroke(Color.web("#1C2E4433"));
            gc.setLineWidth(1);
            gc.strokeRoundRect(x+.5, y+.5, TILE_SIZE-1, TILE_SIZE-1, TILE_R, TILE_R);

        } else {
            gc.setStroke(roomClr.deriveColor(0, 1, 1, 0.35));
            gc.setLineWidth(1);
            gc.strokeRoundRect(x+.5, y+.5, TILE_SIZE-1, TILE_SIZE-1, TILE_R, TILE_R);
        }
    }

    private void drawBossRing(double x, double y) {
        // Outer double ring untuk boss tile
        gc.setStroke(Color.web("#FF174444"));
        gc.setLineWidth(5);
        gc.strokeRoundRect(x-3, y-3, TILE_SIZE+6, TILE_SIZE+6, TILE_R+2, TILE_R+2);

        gc.setStroke(Color.web("#FF174422"));
        gc.setLineWidth(9);
        gc.strokeRoundRect(x-6, y-6, TILE_SIZE+12, TILE_SIZE+12, TILE_R+4, TILE_R+4);
    }

    private void drawTileIcon(double x, double y, Room room,
                               boolean isCur, boolean isVisited) {
        Color roomClr = roomColor(room.getType());

        // Icon size lebih besar di v3
        Color iconColor;
        double fontSize;
        if (isCur) {
            iconColor = roomClr;
            fontSize  = 20;
        } else if (isVisited && room.isCleared()) {
            iconColor = roomClr.deriveColor(0, 0.2, 0.5, 0.4); // sangat desaturated
            fontSize  = 18;
        } else if (isVisited) {
            iconColor = roomClr.deriveColor(0, 0.5, 0.8, 0.6);
            fontSize  = 18;
        } else {
            // Visible tapi belum dikunjungi — agak redup
            iconColor = roomClr.deriveColor(0, 0.8, 0.9, 0.55);
            fontSize  = 18;
        }

        gc.setFill(iconColor);
        gc.setFont(Font.font("Courier New", fontSize));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(roomIcon(room.getType()),
                x + TILE_SIZE / 2.0, y + TILE_SIZE / 2.0 + 7);
    }

    private void drawClearedOverlay(double x, double y) {
        // Diagonal stroke overlay untuk cleared tiles — efek "dicoret"
        gc.setStroke(Color.web("#00E67222"));
        gc.setLineWidth(1);
        // Hanya satu diagonal tipis
        gc.strokeLine(x + 4, y + TILE_SIZE - 4, x + TILE_SIZE - 4, y + 4);

        // Checkmark di pojok kanan atas — lebih besar dari sebelumnya
        gc.setFill(Color.web("#00E67299"));
        gc.setFont(Font.font("Courier New", 10));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("✓", x + TILE_SIZE - 3, y + 12);
    }

    private void drawBossLabel(double x, double y, boolean isVisited) {
        String label = isVisited ? (floor != null && floor.isBossDefeated() ? "SLAIN" : "BOSS") : "BOSS";
        Color labelColor = (floor != null && floor.isBossDefeated())
                ? Color.web("#00E67266") : Color.web("#FF174499");

        gc.setFill(labelColor);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 8));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, x + TILE_SIZE / 2.0, y + TILE_SIZE - 5);
    }

    private void drawReachableIndicator(double x, double y, Room room) {
        // Pulsing dot di bawah tile
        Color roomClr = roomColor(room.getType());
        double alpha  = 0.4 + 0.5 * pulseAlpha; // ikut breathe

        gc.setFill(roomClr.deriveColor(0, 1, 1, alpha));
        gc.fillOval(x + TILE_SIZE/2.0 - 4, y + TILE_SIZE - 10, 8, 5);

        // Outer ring
        gc.setFill(roomClr.deriveColor(0, 1, 1, alpha * 0.4));
        gc.fillOval(x + TILE_SIZE/2.0 - 6, y + TILE_SIZE - 11, 12, 7);
    }

    private void drawHoverHighlight(double x, double y, Room room) {
        // Full tile subtle highlight saat hover
        gc.setFill(roomColor(room.getType()).deriveColor(0, 1, 1, 0.12));
        gc.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, TILE_R, TILE_R);

        // Arrow indicator di tengah tile — arah gerakan
        gc.setFill(Color.web("#FFFFFF22"));
        gc.setFont(Font.font("Courier New", 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("→", x + TILE_SIZE/2.0, y + TILE_SIZE/2.0 - 8);
    }

    private void drawHoverTooltip(Room room, int idx) {
        if (!isAdjacent(idx)) return;

        Color clr     = roomColor(room.getType());
        String line1  = roomName(room.getType()) + (room.isCleared() ? " ✓" : "");

        // Enemy room: tampilkan preview enemy
        String line2 = null;
        if (!room.isCleared() && room.getEnemies() != null && !room.getEnemies().isEmpty()) {
            long alive = room.getEnemies().stream()
                    .filter(e -> e != null && e.isAlive()).count();
            String names = room.getEnemies().stream()
                    .filter(e -> e != null && e.isAlive())
                    .limit(2)
                    .map(e -> e.getName())
                    .reduce("", (a, b) -> a.isEmpty() ? b : a + ", ");
            line2 = alive + " enemy: " + names;
        } else if (room.getType() == Room.RoomType.REST) {
            if (room.getRestUseCount() == 0)      line2 = "Recover HP & MP (+35%/+50%)";
            else if (room.getRestUseCount() == 1) line2 = "Partial recovery (+20%/+30%)";
            else if (room.getRestUseCount() == 2) line2 = "Minimal recovery (+10%/+15%)";
            else                                   line2 = "Zone exhausted — no effect";
        }

        // Hitung lebar tooltip
        double tooltipW = line2 != null ? 220 : 160;
        double tooltipH = line2 != null ? 36 : 22;
        double col = idx % COLS;
        double tx  = tileX((int)col) + TILE_SIZE / 2.0 - tooltipW / 2.0;
        double ty  = tileY(idx / COLS) - tooltipH - 4;

        // Clamp ke canvas
        tx = Math.max(4, Math.min(canvas.getWidth() - tooltipW - 4, tx));
        if (ty < 4) ty = tileY(idx / COLS) + TILE_SIZE + 4;

        gc.setFill(Color.web("#0C1220EE"));
        gc.fillRoundRect(tx, ty, tooltipW, tooltipH, 4, 4);
        gc.setStroke(clr.deriveColor(0, 1, 1, 0.5));
        gc.setLineWidth(1);
        gc.strokeRoundRect(tx, ty, tooltipW, tooltipH, 4, 4);

        gc.setFill(clr);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(line1, tx + tooltipW / 2, ty + 14);

        if (line2 != null) {
            gc.setFill(Color.web("#8899AA"));
            gc.setFont(Font.font("Courier New", 9));
            gc.fillText(line2, tx + tooltipW / 2, ty + 28);
        }
    }

    // ── Player ────────────────────────────────────────────────

    private void drawPlayer() {
        double px = playerVisualX + TILE_SIZE / 2.0;
        double py = playerVisualY + TILE_SIZE / 2.0;

        // Outer breathing ring — alpha ikut pulseAlpha
        double outerAlpha = pulseAlpha * 0.25;
        gc.setFill(Color.web(String.format("#00E5FF%02X",
                (int)(outerAlpha * 255))));
        gc.fillOval(px - 18, py - 18, 36, 36);

        // Mid ring
        gc.setFill(Color.web("#00E5FF40"));
        gc.fillOval(px - 11, py - 11, 22, 22);

        // Core fill
        gc.setFill(Color.web("#00E5FF88"));
        gc.fillOval(px - 7, py - 7, 14, 14);

        // Player icon ◈
        gc.setFill(CYAN);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("◈", px, py + 6);

        // Outer ring stroke
        gc.setStroke(CYAN);
        gc.setLineWidth(1.5);
        gc.strokeOval(px - 12, py - 12, 24, 24);

        // Inner ring stroke (second ring)
        gc.setStroke(Color.web("#00E5FF55"));
        gc.setLineWidth(1);
        gc.strokeOval(px - 17, py - 17, 34, 34);
    }

    // ── Mouse Handlers ────────────────────────────────────────

    private void handleHover(MouseEvent e) {
        int col = (int)((e.getX() - TILE_GAP) / (TILE_SIZE + TILE_GAP));
        int row = (int)((e.getY() - TILE_GAP) / (TILE_SIZE + TILE_GAP));
        int idx = (row >= 0 && row < rows && col >= 0 && col < COLS)
                ? row * COLS + col : -1;

        if (floor != null && idx >= 0 && idx < floor.getTotalRooms()) {
            if (idx != hoveredTileIdx) {
                hoveredTileIdx = idx;
                draw();
            }
        } else if (hoveredTileIdx != -1) {
            hoveredTileIdx = -1;
            draw();
        }
    }

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

        // Pause pulse saat bergerak
        pulseAnim.pause();
        marchAnim.pause();

        moveAnim = new Timeline(
            new KeyFrame(Duration.ZERO),
            new KeyFrame(Duration.millis(220))
        );
        moveAnim.currentTimeProperty().addListener((obs, o, n) -> {
            double t = Math.min(1.0, n.toMillis() / 220.0);
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
            hoveredTileIdx = -1;
            pulseAnim.play();  // resume animasi
            marchAnim.play();
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

    /** Hentikan semua animasi saat view tidak aktif (cleanup) */
    public void stopAnimations() {
        if (pulseAnim != null) pulseAnim.stop();
        if (marchAnim != null) marchAnim.stop();
        if (moveAnim  != null) moveAnim.stop();
    }

    // ── Coordinate & Mapping Helpers ──────────────────────────

    private double tileX(int col) { return TILE_GAP + col * (TILE_SIZE + TILE_GAP); }
    private double tileY(int row) { return TILE_GAP + row * (TILE_SIZE + TILE_GAP); }

    public double getPreferredHeight() {
        return rows * (TILE_SIZE + TILE_GAP) + TILE_GAP;
    }

    private String roomIcon(Room.RoomType t) {
        return switch (t) {
            case EMPTY -> "○"; case ENEMY -> "◆"; case ELITE -> "◈";
            case BOSS  -> "☠"; case LOOT  -> "☆"; case REST  -> "♥";
            case EVENT -> "?"; case SHOP  -> "$"; case TRAP  -> "!";
        };
    }

    private String roomName(Room.RoomType t) {
        return switch (t) {
            case EMPTY -> "Safe Corridor"; case ENEMY -> "Enemy Encounter";
            case ELITE -> "Elite Enemy";   case BOSS  -> "BOSS ROOM";
            case LOOT  -> "Supply Cache";  case REST  -> "Safe Zone";
            case EVENT -> "Event";         case SHOP  -> "Merchant";
            case TRAP  -> "Danger Zone";
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
