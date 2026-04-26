package arclightcity.dungeon;

import arclightcity.entity.EntityFactory;
import arclightcity.entity.enemy.Enemy;

import java.util.*;

/**
 * ProceduralGenerator — membuat floor dungeon secara acak setiap run.
 *
 * Algoritma:
 *   1. Tentukan jumlah room berdasarkan floor (makin dalam makin banyak)
 *   2. Assign tipe ke tiap room (ENEMY, LOOT, EVENT, dll)
 *   3. Buat koneksi antar room (branching path)
 *   4. Populate konten tiap room (enemy, event, loot)
 *
 * Constraint:
 *   - Room pertama selalu EMPTY (aman untuk orientasi)
 *   - Room terakhir selalu BOSS
 *   - Ada minimal 1 LOOT room dan 1 REST room per floor
 *   - ELITE room mulai muncul dari floor 5+
 */
public class ProceduralGenerator {

    private static final Random RNG = new Random();

    // ── Floor Size Config ─────────────────────────────────────

    private static int getRoomCount(int floor) {
        // Floor 1-5: 6-8 rooms | Floor 6-15: 8-12 rooms | Floor 16+: 12-16 rooms
        if (floor <= 5)  return 6  + RNG.nextInt(3);
        if (floor <= 15) return 8  + RNG.nextInt(5);
        return              12 + RNG.nextInt(5);
    }

    // ── Main Generate ────────────────────────────────────────

    /**
     * Generate satu floor dungeon secara prosedural.
     * @param floorNumber nomor floor (mulai 1)
     * @return Floor yang siap dipakai
     */
    public static Floor generateFloor(int floorNumber) {
        int totalRooms = getRoomCount(floorNumber);

        // Step 1: Tentukan tipe tiap room
        List<Room.RoomType> roomTypes = buildRoomTypeList(floorNumber, totalRooms);

        // Step 2: Buat koneksi antar room (branching path graph)
        Map<Integer, List<Integer>> connections = buildConnections(totalRooms);

        // Step 3: Buat Room objects
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < totalRooms; i++) {
            List<Integer> nextRooms = connections.getOrDefault(i, List.of());
            Room room = new Room(i, roomTypes.get(i), nextRooms);
            rooms.add(room);
        }

        // Step 4: Populate konten room
        populateRooms(rooms, floorNumber);

        // Step 5: Mark room pertama sebagai visited
        rooms.get(0).setVisited(true);

        // Step 6: Pilih tema floor
        Floor.FloorTheme theme = selectTheme(floorNumber);

        return new Floor(floorNumber, rooms, theme);
    }

    // ── Room Type Assignment ──────────────────────────────────

    private static List<Room.RoomType> buildRoomTypeList(int floor, int total) {
        List<Room.RoomType> types = new ArrayList<>();

        // Room 0: selalu EMPTY
        types.add(Room.RoomType.EMPTY);

        // Room terakhir: selalu BOSS
        // Sisa rooms: diisi dengan tipe acak berdasarkan weight

        List<Room.RoomType> pool = buildWeightedPool(floor, total - 2);
        Collections.shuffle(pool, RNG);
        types.addAll(pool);

        // Room terakhir: BOSS
        types.add(Room.RoomType.BOSS);

        return types;
    }

    /**
     * Buat pool tipe room dengan distribusi yang seimbang.
     * Pool berisi (total-2) tipe untuk room 1 s.d. total-2.
     */
    private static List<Room.RoomType> buildWeightedPool(int floor, int count) {
        List<Room.RoomType> pool = new ArrayList<>();

        // Baseline weights
        int enemyWeight  = 40;
        int lootWeight   = 15;
        int eventWeight  = 12;
        int shopWeight   = 10;
        int trapWeight   = 10;
        int restWeight   = 8;
        int eliteWeight  = floor >= 5 ? 5 : 0; // mulai floor 5

        int totalWeight = enemyWeight + lootWeight + eventWeight + shopWeight
                        + trapWeight + restWeight + eliteWeight;

        // Pastikan ada minimal 1 LOOT dan 1 REST
        pool.add(Room.RoomType.LOOT);
        pool.add(Room.RoomType.REST);
        int remaining = count - 2;

        for (int i = 0; i < remaining; i++) {
            int roll = RNG.nextInt(totalWeight);
            if      (roll < enemyWeight)                               pool.add(Room.RoomType.ENEMY);
            else if (roll < enemyWeight + eliteWeight)                 pool.add(Room.RoomType.ELITE);
            else if (roll < enemyWeight + eliteWeight + lootWeight)    pool.add(Room.RoomType.LOOT);
            else if (roll < enemyWeight + eliteWeight + lootWeight + eventWeight) pool.add(Room.RoomType.EVENT);
            else if (roll < enemyWeight + eliteWeight + lootWeight + eventWeight + shopWeight) pool.add(Room.RoomType.SHOP);
            else if (roll < enemyWeight + eliteWeight + lootWeight + eventWeight + shopWeight + trapWeight) pool.add(Room.RoomType.TRAP);
            else                                                        pool.add(Room.RoomType.REST);
        }

        return pool;
    }

    // ── Connection / Path Builder ─────────────────────────────

    /**
     * Buat graf koneksi antar room.
     * Struktur: linear dengan percabangan.
     * Setiap room punya 1-2 "next room" pilihan.
     *
     * Contoh (8 rooms):
     *   0 → [1, 2]
     *   1 → [3]
     *   2 → [3]
     *   3 → [4, 5]
     *   4 → [6]
     *   5 → [6]
     *   6 → [7]
     *   7 → [] (boss, end)
     */
    private static Map<Integer, List<Integer>> buildConnections(int total) {
        Map<Integer, List<Integer>> conn = new HashMap<>();
        int last = total - 1;

        // Bangun jalur dengan beberapa percabangan
        List<Integer> mainPath = new ArrayList<>();
        for (int i = 0; i < total; i++) mainPath.add(i);

        for (int i = 0; i < last; i++) {
            List<Integer> nexts = new ArrayList<>();
            nexts.add(i + 1); // selalu ada jalur lurus

            // Percabangan: 35% chance ada pilihan alternatif (jika masih ada room)
            if (i + 2 < last && RNG.nextDouble() < 0.35) {
                nexts.add(i + 2); // skip satu room (bypass)
            }

            conn.put(i, nexts);
        }

        // Boss room tidak punya next
        conn.put(last, List.of());

        return conn;
    }

    // ── Room Population ──────────────────────────────────────

    private static void populateRooms(List<Room> rooms, int floor) {
        for (Room room : rooms) {
            switch (room.getType()) {

                case ENEMY -> {
                    int difficulty = getDifficulty(floor);
                    List<Enemy> enemies = EntityFactory.generateEncounter(floor, difficulty);
                    room.setEnemies(enemies);
                }

                case ELITE -> {
                    List<Enemy> elites = EntityFactory.generateEncounter(floor, 4); // hardest
                    room.setEnemies(elites);
                }

                case BOSS -> {
                    Enemy boss = EntityFactory.generateBoss(floor);
                    room.setEnemies(List.of(boss));
                }

                case LOOT -> {
                    // Loot items di-resolve oleh LootManager saat room dibuka
                    room.setLootItemIds(List.of("LOOT_FLOOR_" + floor));
                }

                case EVENT -> {
                    String eventId = selectRandomEvent(floor);
                    room.setEventId(eventId);
                }

                case SHOP -> {
                    room.setShopId("SHOP_FLOOR_" + floor);
                }

                case TRAP -> {
                    // Trap damage ditangani oleh DungeonManager
                    room.setEventId("TRAP_" + selectTrapType());
                }

                case REST, EMPTY -> {
                    // Tidak butuh konten khusus
                }
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private static int getDifficulty(int floor) {
        // Floor 1-3: easy | 4-7: normal | 8-12: hard | 13+: random hard/normal
        if (floor <= 3)  return 1;
        if (floor <= 7)  return 1 + RNG.nextInt(2); // 1-2
        if (floor <= 12) return 2 + RNG.nextInt(2); // 2-3
        return                1 + RNG.nextInt(3);   // 1-3 random
    }

    private static String selectRandomEvent(int floor) {
        String[] events = {
            "EVENT_CALIBRATION",    // kalibrasi gratis
            "EVENT_MERCHANT",       // merchant muncul
            "EVENT_MYSTERY_BOX",    // kotak misterius
            "EVENT_AMBUSH",         // musuh menyerang tiba-tiba
            "EVENT_DATA_CACHE",     // temukan data cache = EXP bonus
            "EVENT_NEON_FOUNTAIN",  // fountain yang menyembuhkan
            "EVENT_CORRUPTED_LOOT", // loot tapi ada risiko
        };
        return events[RNG.nextInt(events.length)];
    }

    private static String selectTrapType() {
        String[] traps = { "ELECTRIC", "CORRODE", "ALARM", "FREEZE", "NEON_BURN" };
        return traps[RNG.nextInt(traps.length)];
    }

    private static Floor.FloorTheme selectTheme(int floor) {
        Floor.FloorTheme[] themes = Floor.FloorTheme.values();
        // Tema berubah tiap 5 floor, dengan sedikit variasi
        int themeIndex = ((floor - 1) / 5) % themes.length;
        if (RNG.nextDouble() < 0.20) { // 20% chance tema acak
            themeIndex = RNG.nextInt(themes.length);
        }
        return themes[themeIndex];
    }
}
