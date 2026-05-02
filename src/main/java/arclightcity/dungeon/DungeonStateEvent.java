package arclightcity.dungeon;

import arclightcity.entity.enemy.Enemy;

import java.util.List;

/**
 * DungeonStateEvent — event yang dikirim DungeonManager ke UI.
 * UI subscribe via setStateListener() dan update tampilan berdasarkan ini.
 */
public class DungeonStateEvent {

    public enum Type {
        DUNGEON_STARTED,
        FLOOR_ENTERED,
        FLOOR_COMPLETED,
        READY_FOR_NEXT_FLOOR,
        ROOM_ENTERED,
        ROOM_CLEARED,
        ROOM_ALREADY_CLEARED,
        COMBAT_STARTED,
        BOSS_ENCOUNTERED,
        EVENT_ENCOUNTERED,
        SHOP_OPENED,
        LOOT_FOUND,
        REST,
        HEALED,
        DAMAGED,
        EXP_GAINED,
        GOLD_GAINED,
        DEBUFF_APPLIED,
        SPAWN_ENEMY,
        MAP_REVEALED,
        NOTHING,
        GAME_OVER,
        LEVEL_UP,
        ERROR
    }

    // ── Fields ───────────────────────────────────────────────
    public final Type         type;
    public final String       message;
    public final Room         room;
    public final Floor.FloorTheme theme;
    public final List<Enemy>  enemies;
    public final List<String> lootIds;
    public final int          intValue;
    public final long         longValue;
    public final String       stringValue;
    public final DungeonEvent dungeonEvent;

    // ── Private Constructor (pakai factory) ──────────────────
    private DungeonStateEvent(Builder b) {
        this.type         = b.type;
        this.message      = b.message;
        this.room         = b.room;
        this.theme        = b.theme;
        this.enemies      = b.enemies;
        this.lootIds      = b.lootIds;
        this.intValue     = b.intValue;
        this.longValue    = b.longValue;
        this.stringValue  = b.stringValue;
        this.dungeonEvent = b.dungeonEvent;
    }

    // ── Factory Methods ──────────────────────────────────────

    public static DungeonStateEvent dungeonStarted(String playerName) {
        return new Builder(Type.DUNGEON_STARTED)
                .message("🏙️ " + playerName + " enters Arclight City dungeon...").build();
    }

    public static DungeonStateEvent floorEntered(int floor, Floor.FloorTheme theme) {
        return new Builder(Type.FLOOR_ENTERED)
                .message("📍 Floor " + floor + ": " + theme.displayName)
                .intValue(floor).theme(theme).build();
    }

    public static DungeonStateEvent floorCompleted(int floor) {
        return new Builder(Type.FLOOR_COMPLETED)
                .message("✅ Floor " + floor + " cleared!").intValue(floor).build();
    }

    public static DungeonStateEvent readyForNextFloor(int nextFloor) {
        return new Builder(Type.READY_FOR_NEXT_FLOOR)
                .message("🚪 Descend to Floor " + nextFloor + "?").intValue(nextFloor).build();
    }

    public static DungeonStateEvent roomEntered(Room room) {
        return new Builder(Type.ROOM_ENTERED)
                .message("→ Entering: " + room.getType().name() + " Room").room(room).build();
    }

    public static DungeonStateEvent roomCleared(Room room, String detail) {
        return new Builder(Type.ROOM_CLEARED).message(detail).room(room).build();
    }

    public static DungeonStateEvent roomAlreadyCleared(Room room) {
        return new Builder(Type.ROOM_ALREADY_CLEARED)
                .message("Room already cleared.").room(room).build();
    }

    public static DungeonStateEvent combatStarted(List<Enemy> enemies, boolean elite) {
        String label = elite ? "⚠️ Elite encounter!" : "⚔️ Enemy encountered!";
        return new Builder(Type.COMBAT_STARTED).message(label).enemies(enemies).build();
    }

    public static DungeonStateEvent bossEncountered(String bossName) {
        return new Builder(Type.BOSS_ENCOUNTERED)
                .message("💀 BOSS: " + bossName + " — prepare for the fight of your life.").build();
    }

    public static DungeonStateEvent eventEncountered(DungeonEvent event) {
        return new Builder(Type.EVENT_ENCOUNTERED)
                .message(event.getTitle()).dungeonEvent(event).build();
    }

    public static DungeonStateEvent shopOpened(String shopId) {
        return new Builder(Type.SHOP_OPENED)
                .message("🛒 Shop open.").stringValue(shopId).build();
    }

    public static DungeonStateEvent lootFound(List<String> lootIds, int floor) {
        return new Builder(Type.LOOT_FOUND)
                .message("📦 Loot found!").lootIds(lootIds).intValue(floor).build();
    }

    public static DungeonStateEvent rest(double hpRestored, double mpRestored) {
        return rest(hpRestored, mpRestored, null);
    }

    public static DungeonStateEvent rest(double hpRestored, double mpRestored, String customMsg) {
        String msg = customMsg != null
                ? customMsg + (hpRestored > 0
                    ? String.format(" +%.0f HP, +%.0f MP", hpRestored, mpRestored)
                    : "")
                : String.format("🛌 Resting... +%.0f HP, +%.0f MP", hpRestored, mpRestored);
        return new Builder(Type.REST).message(msg).build();
    }

    public static DungeonStateEvent healed(int amount) {
        return new Builder(Type.HEALED).message("💚 Healed " + amount + " HP").intValue(amount).build();
    }

    public static DungeonStateEvent damaged(int amount) {
        return new Builder(Type.DAMAGED).message("💢 Took " + amount + " damage!").intValue(amount).build();
    }

    public static DungeonStateEvent expGained(int amount) {
        return new Builder(Type.EXP_GAINED).message("✨ +" + amount + " EXP").intValue(amount).build();
    }

    public static DungeonStateEvent goldGained(long amount) {
        return new Builder(Type.GOLD_GAINED).message("💰 +" + amount + " Gold").longValue(amount).build();
    }

    public static DungeonStateEvent debuffApplied(String effectName) {
        return new Builder(Type.DEBUFF_APPLIED)
                .message("☠️ " + effectName + " applied!").stringValue(effectName).build();
    }

    public static DungeonStateEvent mapRevealed() {
        return new Builder(Type.MAP_REVEALED).message("🗺️ Floor map revealed.").build();
    }

    public static DungeonStateEvent nothing() {
        return new Builder(Type.NOTHING).message("Nothing happens.").build();
    }

    public static DungeonStateEvent gameOver(int floorReached, double totalDmg) {
        return new Builder(Type.GAME_OVER)
                .message(String.format("💀 GAME OVER — Reached Floor %d | Damage dealt: %.0f",
                        floorReached, totalDmg))
                .intValue(floorReached).build();
    }

    public static DungeonStateEvent error(String msg) {
        return new Builder(Type.ERROR).message("⚠️ " + msg).build();
    }

    // ── Builder ──────────────────────────────────────────────

    private static class Builder {
        Type type; String message = ""; Room room = null;
        Floor.FloorTheme theme = null; List<Enemy> enemies = null;
        List<String> lootIds = null; int intValue = 0; long longValue = 0;
        String stringValue = null; DungeonEvent dungeonEvent = null;

        Builder(Type type) { this.type = type; }
        Builder message(String m)         { message = m; return this; }
        Builder room(Room r)              { room = r; return this; }
        Builder theme(Floor.FloorTheme t) { theme = t; return this; }
        Builder enemies(List<Enemy> e)    { enemies = e; return this; }
        Builder lootIds(List<String> l)   { lootIds = l; return this; }
        Builder intValue(int v)           { intValue = v; return this; }
        Builder longValue(long v)         { longValue = v; return this; }
        Builder stringValue(String v)     { stringValue = v; return this; }
        Builder dungeonEvent(DungeonEvent e) { dungeonEvent = e; return this; }
        DungeonStateEvent build()         { return new DungeonStateEvent(this); }
    }

    /** Event saat player naik level setelah combat */
    public static DungeonStateEvent levelUp(int newLevel, int skillPoints) {
        return new Builder(Type.LEVEL_UP)
                .message("⬆ LEVEL UP! Now Level " + newLevel +
                         " — +" + skillPoints + " Skill Point(s) available!")
                .intValue(newLevel)
                .build();
    }

    /** Event saat 3 Mythic Fragment berhasil di-craft jadi Mythic weapon */
    public static DungeonStateEvent mythicCraft(String weaponName) {
        return new Builder(Type.LEVEL_UP) // reuse LEVEL_UP type for notification
                .message("✦ MYTHIC CRAFTED! " + weaponName)
                .intValue(0)
                .build();
    }
}
