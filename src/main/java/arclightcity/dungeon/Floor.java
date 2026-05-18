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
        if (roomIndex < 0 || roomIndex >= rooms.size()) return false;
        Room current = getCurrentRoom();
        if (current == null) return false;

        // Izinkan gerakan ke room manapun yang ada di nextRoomIndexes current room
        // ATAU yang nextRoomIndexes-nya berisi current room (backtrack symmetric)
        boolean isNextRoom   = current.getNextRoomIndexes().contains(roomIndex);
        boolean isBacktrack  = rooms.get(roomIndex).getNextRoomIndexes().contains(currentRoomIndex);

        if (!isNextRoom && !isBacktrack) return false;

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

    /** Set posisi langsung tanpa cek konektivitas — untuk inisialisasi awal */
    public void setCurrentRoom(int idx) {
        if (idx >= 0 && idx < rooms.size()) currentRoomIndex = idx;
    }
    public boolean    isCompleted()          { return completed; }
    public FloorTheme getTheme()             { return theme; }

    /** Cek apakah boss room sudah di-clear (prerequisite untuk DESCEND) */
    public boolean isBossDefeated() {
        return rooms.stream()
                .filter(r -> r.getType() == Room.RoomType.BOSS)
                .anyMatch(Room::isCleared);
    }

    // ── Floor Theme ──────────────────────────────────────────

    public enum FloorTheme {
        NEON_SLUM     ("Pasar Malam Gaib",  "Pasar mistis bawah tanah — bau kemenyan dan daging bakar bercampur.", "#CC8800"),
        CORPORATE_HQ  ("Candi Terlarang",   "Candi kuno tersegel, penuh jebakan dan kutukan leluhur.",             "#AA4400"),
        DATA_VAULT    ("Hutan Angker",      "Hutan belantara Kalimantan, dijaga roh-roh purba yang gelisah.",      "#336622"),
        NEON_WASTES   ("Goa Naga",          "Gua bawah gunung berapi tempat naga purba bersemayam.",               "#884400"),
        VOID_RIFT     ("Kahyangan Rusak",   "Langit para dewa yang runtuh — berbahaya tapi penuh artefak sakti.", "#882200");

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
