package arclightcity.dungeon;

import arclightcity.entity.enemy.Enemy;

import java.util.List;

/**
 * Satu room dalam sebuah floor dungeon.
 */
public class Room {

    public enum RoomType {
        EMPTY, ENEMY, ELITE, BOSS, LOOT, TRAP, SHOP, EVENT, REST
    }

    private final int      roomIndex;
    private final RoomType type;
    private       boolean  cleared   = false;
    private       boolean  visited   = false;
    private       int      restUseCount = 0;  // jumlah kali REST room dipakai

    // Konten room (diisi saat generate)
    private List<Enemy>  enemies    = null;  // ENEMY / ELITE / BOSS
    private String       eventId    = null;  // EVENT
    private String       shopId     = null;  // SHOP
    private List<String> lootItemIds = null; // LOOT

    // Koneksi ke room lain (untuk branching path)
    private final List<Integer> nextRoomIndexes; // index room yang bisa diakses setelah ini

    // ── Constructor ─────────────────────────────────────────

    public Room(int roomIndex, RoomType type, List<Integer> nextRoomIndexes) {
        this.roomIndex       = roomIndex;
        this.type            = type;
        this.nextRoomIndexes = nextRoomIndexes;
    }

    // ── Setters ──────────────────────────────────────────────

    public void setEnemies(List<Enemy> enemies)         { this.enemies     = enemies; }
    public void setEventId(String eventId)              { this.eventId     = eventId; }
    public void setShopId(String shopId)                { this.shopId      = shopId; }
    public void setLootItemIds(List<String> lootItemIds){ this.lootItemIds = lootItemIds; }
    public void setCleared(boolean cleared)             { this.cleared     = cleared; }
    public void setVisited(boolean visited)             { this.visited     = visited; }

    // ── Getters ─────────────────────────────────────────────

    public int           getRoomIndex()      { return roomIndex; }
    public RoomType      getType()           { return type; }
    public boolean       isCleared()         { return cleared; }
    public boolean       isVisited()         { return visited; }
    public List<Enemy>   getEnemies()        { return enemies; }
    public String        getEventId()        { return eventId; }
    public String        getShopId()         { return shopId; }
    public List<String>  getLootItemIds()    { return lootItemIds; }
    public List<Integer> getNextRoomIndexes(){ return nextRoomIndexes; }

    public int  getRestUseCount()  { return restUseCount; }
    public void incrementRestUse() { restUseCount++; }

    public boolean hasEnemies() {
        return enemies != null && enemies.stream().anyMatch(e -> e != null && e.isAlive());
    }

    @Override
    public String toString() {
        return String.format("Room[%d | %s | cleared:%s]", roomIndex, type, cleared);
    }
}
