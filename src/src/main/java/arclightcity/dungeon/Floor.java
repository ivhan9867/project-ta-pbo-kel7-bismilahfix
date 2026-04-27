package arclightcity.dungeon;

import java.util.List;

/**
 * Satu floor dalam dungeon.
 * Berisi daftar room yang sudah di-generate secara prosedural.
 */
public class Floor {

    private final int        floorNumber;
    private final List<Room> rooms;
    private       int        currentRoomIndex = 0;
    private       boolean    completed        = false;

    // Tema visual/lore floor (untuk UI)
    private final FloorTheme theme;

    // ── Constructor ─────────────────────────────────────────

    public Floor(int floorNumber, List<Room> rooms, FloorTheme theme) {
        this.floorNumber = floorNumber;
        this.rooms       = rooms;
        this.theme       = theme;
    }

    // ── Navigation ───────────────────────────────────────────

    public Room getCurrentRoom() {
        if (currentRoomIndex >= rooms.size()) return null;
        return rooms.get(currentRoomIndex);
    }

    public Room getRoom(int index) {
        if (index < 0 || index >= rooms.size()) return null;
        return rooms.get(index);
    }

    /**
     * Pindah ke room dengan index tertentu.
     * @return true jika berhasil, false jika invalid
     */
    public boolean moveToRoom(int roomIndex) {
        Room current = getCurrentRoom();
        if (current == null) return false;
        if (!current.getNextRoomIndexes().contains(roomIndex)) return false;

        currentRoomIndex = roomIndex;
        rooms.get(roomIndex).setVisited(true);
        return true;
    }

    /**
     * Cek apakah semua room sudah cleared (boss room = floor complete).
     */
    public boolean checkCompletion() {
        Room bossRoom = rooms.stream()
                .filter(r -> r.getType() == Room.RoomType.BOSS)
                .findFirst().orElse(null);

        if (bossRoom != null && bossRoom.isCleared()) {
            completed = true;
        }
        return completed;
    }

    // ── Stats ─────────────────────────────────────────────────

    public int getTotalRooms()   { return rooms.size(); }
    public int getClearedRooms() { return (int) rooms.stream().filter(Room::isCleared).count(); }
    public int getVisitedRooms() { return (int) rooms.stream().filter(Room::isVisited).count(); }

    // ── Getters ─────────────────────────────────────────────

    public int        getFloorNumber()       { return floorNumber; }
    public List<Room> getRooms()             { return rooms; }
    public int        getCurrentRoomIndex()  { return currentRoomIndex; }
    public boolean    isCompleted()          { return completed; }
    public FloorTheme getTheme()             { return theme; }

    // ── Floor Theme ──────────────────────────────────────────

    public enum FloorTheme {
        NEON_SLUM     ("Neon Slum",      "Jalanan bawah Arclight City, bau uap dan sinyal korup.",     "#FF00AA"),
        CORPORATE_HQ  ("Corporate HQ",   "Gedung korporat steril dengan keamanan berlapis-lapis.",     "#00AAFF"),
        DATA_VAULT    ("Data Vault",      "Server room bawah tanah, penuh entitas digital tersesat.",    "#AA00FF"),
        NEON_WASTES   ("Neon Wastes",    "Zona radiasi neon, berbahaya tapi kaya akan loot langka.",    "#FFAA00"),
        VOID_RIFT     ("Void Rift",      "Celah antara dunia nyata dan jaringan data — anomali murni.", "#FF0000");

        public final String displayName;
        public final String description;
        public final String accentColor; // untuk UI JavaFX

        FloorTheme(String displayName, String description, String accentColor) {
            this.displayName  = displayName;
            this.description  = description;
            this.accentColor  = accentColor;
        }
    }
}
