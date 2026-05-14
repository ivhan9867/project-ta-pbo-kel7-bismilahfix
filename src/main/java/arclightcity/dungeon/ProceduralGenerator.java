package arclightcity.dungeon;

import arclightcity.entity.EntityFactory;
import arclightcity.entity.enemy.Enemy;

import java.util.*;

/**
 * ProceduralGenerator — membuat floor dungeon secara acak setiap run.
 *
 * v0.2.3 — Grid Map Overhaul:
 *   - Floor direpresentasikan sebagai grid COLS x ROWS penuh
 *   - Semua tile berisi event (tidak ada empty kecuali start)
 *   - Koneksi cardinal (atas/bawah/kiri/kanan)
 *   - Player bebas bergerak ke arah manapun yang terhubung
 *   - Boss selalu di posisi tertentu (tengah baris terakhir)
 *   - Tile yang sudah dikunjungi bisa dikunjungi lagi (backtrack)
 *
 * Layout grid (COLS=5, contoh ROWS=4, total 20 tile):
 *   [START][  ?  ][  ?  ][  ?  ][  ?  ]
 *   [  ?  ][  ?  ][  ?  ][  ?  ][  ?  ]
 *   [  ?  ][  ?  ][  ?  ][  ?  ][  ?  ]
 *   [  ?  ][  ?  ][ BOSS][  ?  ][  ?  ]
 */
public class ProceduralGenerator {

    private static final Random RNG  = new Random();
    public  static final int    COLS = 12; // lebar grid — memenuhi area 940px (12*72+8=872px)

    // ── Floor Size Config ─────────────────────────────────────

    /** Jumlah baris grid berdasarkan floor */
    private static int getGridRows(int floor) {
        if (floor <= 5)  return 2;  // 24 tile (2×12)
        if (floor <= 15) return 3;  // 36 tile (3×12)
        return 4;                   // 48 tile (4×12, floor 16+)
    }

    // ── Main Generate ────────────────────────────────────────

    public static Floor generateFloor(int floorNumber) {
        int rows      = getGridRows(floorNumber);
        int totalTile = rows * COLS;

        // Step 1: Assign tipe ke tiap tile
        List<Room.RoomType> tileTypes = buildTileTypeGrid(floorNumber, rows);

        // Step 2: Buat koneksi cardinal (atas/bawah/kiri/kanan)
        Map<Integer, List<Integer>> connections = buildCardinalConnections(rows, COLS);

        // Step 3: Buat Room objects
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < totalTile; i++) {
            List<Integer> nexts = connections.getOrDefault(i, List.of());
            Room room = new Room(i, tileTypes.get(i), nexts);
            rooms.add(room);
        }

        // Step 4: Populate konten tiap room
        populateRooms(rooms, floorNumber);

        // Step 5: Room pertama (0,0) = EMPTY, langsung visited
        rooms.get(0).setVisited(true);

        // Step 6: Pilih tema floor
        Floor.FloorTheme theme = selectTheme(floorNumber);

        return new Floor(floorNumber, rooms, theme);
    }

    // ── Tile Type Grid ────────────────────────────────────────

    /**
     * Assign tipe tile untuk grid rows×COLS.
     * - Tile 0 = EMPTY (start)
     * - Tile terakhir baris tengah-bawah = BOSS
     * - Sisanya: random weighted
     */
    private static List<Room.RoomType> buildTileTypeGrid(int floor, int rows) {
        int total    = rows * COLS;
        int bossIdx  = getBossIndex(rows, COLS);

        // Hitung distribusi yang diinginkan
        List<Room.RoomType> types = new ArrayList<>(Collections.nCopies(total, null));

        // Fixed positions
        types.set(0, Room.RoomType.EMPTY);          // start
        types.set(bossIdx, Room.RoomType.BOSS);      // boss

        // Hitung slot yang tersisa
        int remaining = total - 2;

        // Build pool dengan distribusi seimbang
        List<Room.RoomType> pool = buildWeightedPool(floor, remaining);
        Collections.shuffle(pool, RNG);

        // Isi slot yang masih null
        int poolIdx = 0;
        for (int i = 0; i < total; i++) {
            if (types.get(i) == null) {
                types.set(i, pool.get(poolIdx++));
            }
        }

        return types;
    }

    /** Boss selalu ada di tengah baris terakhir (atau mendekati tengah) */
    static int getBossIndex(int rows, int cols) {
        int lastRowStart = (rows - 1) * cols;
        return lastRowStart + (cols / 2); // tengah baris terakhir
    }

    private static List<Room.RoomType> buildWeightedPool(int floor, int count) {
        List<Room.RoomType> pool = new ArrayList<>();

        // Pastikan ada minimal distribusi yang baik
        int guaranteedLoot  = Math.max(2, count / 6);
        int guaranteedRest  = Math.max(1, count / 8);
        int guaranteedEvent = Math.max(2, count / 6);
        int guaranteedShop  = 1;
        int guaranteedTrap  = Math.max(1, count / 8);
        int guaranteedElite = floor >= 5 ? Math.max(1, count / 8) : 0;

        for (int i = 0; i < guaranteedLoot;  i++) pool.add(Room.RoomType.LOOT);
        for (int i = 0; i < guaranteedRest;  i++) pool.add(Room.RoomType.REST);
        for (int i = 0; i < guaranteedEvent; i++) pool.add(Room.RoomType.EVENT);
        for (int i = 0; i < guaranteedShop;  i++) pool.add(Room.RoomType.SHOP);
        for (int i = 0; i < guaranteedTrap;  i++) pool.add(Room.RoomType.TRAP);
        for (int i = 0; i < guaranteedElite; i++) pool.add(Room.RoomType.ELITE);

        // Sisa diisi ENEMY
        int filled = pool.size();
        for (int i = filled; i < count; i++) pool.add(Room.RoomType.ENEMY);

        return pool;
    }

    // ── Cardinal Connection Builder ───────────────────────────

    /**
     * Buat koneksi cardinal: setiap tile terhubung ke
     * kanan, kiri, bawah, atas (jika masih dalam batas grid).
     *
     * Grid:
     *   0  1  2  3  4
     *   5  6  7  8  9
     *   10 11 12 13 14
     *
     * Tile 6 terhubung ke: 1 (atas), 11 (bawah), 5 (kiri), 7 (kanan)
     */
    static Map<Integer, List<Integer>> buildCardinalConnections(int rows, int cols) {
        Map<Integer, List<Integer>> conn = new HashMap<>();

        for (int i = 0; i < rows * cols; i++) {
            List<Integer> neighbors = new ArrayList<>();
            int row = i / cols;
            int col = i % cols;

            if (row > 0)        neighbors.add(i - cols); // atas
            if (row < rows - 1) neighbors.add(i + cols); // bawah
            if (col > 0)        neighbors.add(i - 1);    // kiri
            if (col < cols - 1) neighbors.add(i + 1);    // kanan

            conn.put(i, neighbors);
        }

        return conn;
    }

    // ── Room Population ──────────────────────────────────────

    private static void populateRooms(List<Room> rooms, int floor) {
        for (Room room : rooms) {
            switch (room.getType()) {
                case ENEMY -> {
                    int diff = getDifficulty(floor);
                    room.setEnemies(EntityFactory.generateEncounter(floor, diff));
                }
                case ELITE -> {
                    room.setEnemies(EntityFactory.generateEncounter(floor, 4));
                }
                case BOSS -> {
                    Enemy boss = EntityFactory.generateBoss(floor);
                    room.setEnemies(List.of(boss));
                }
                case LOOT -> {
                    room.setLootItemIds(List.of("LOOT_FLOOR_" + floor));
                }
                case EVENT -> {
                    room.setEventId(selectRandomEvent(floor));
                }
                case SHOP -> {
                    room.setShopId("SHOP_FLOOR_" + floor);
                }
                case TRAP -> {
                    room.setEventId("TRAP_" + selectTrapType());
                }
                case EMPTY -> { /* start room, no content */ }
                default    -> { }
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private static int getDifficulty(int floor) {
        if (floor <= 3)  return 1;
        if (floor <= 7)  return 1 + RNG.nextInt(2);
        if (floor <= 12) return 2 + RNG.nextInt(2);
        return                1 + RNG.nextInt(3);
    }

    private static String selectRandomEvent(int floor) {
        String[] events = {
            "EVENT_CALIBRATION",
            "EVENT_MERCHANT",
            "EVENT_MYSTERY_BOX",
            "EVENT_AMBUSH",
            "EVENT_DATA_CACHE",
            "EVENT_NEON_FOUNTAIN",
            "EVENT_CORRUPTED_LOOT",
        };
        return events[RNG.nextInt(events.length)];
    }

    private static String selectTrapType() {
        String[] traps = { "ELECTRIC", "CORRODE", "ALARM", "FREEZE", "NEON_BURN" };
        return traps[RNG.nextInt(traps.length)];
    }

    private static Floor.FloorTheme selectTheme(int floor) {
        Floor.FloorTheme[] themes = Floor.FloorTheme.values();
        int themeIndex = ((floor - 1) / 10) % themes.length;
        return themes[themeIndex];
    }

    /** Util: total tile untuk floor number */
    public static int getTotalTiles(int floorNumber) {
        return getGridRows(floorNumber) * COLS;
    }
}
