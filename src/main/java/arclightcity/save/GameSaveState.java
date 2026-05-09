package arclightcity.save;

import arclightcity.entity.player.PlayerBackground;
import arclightcity.entity.mercenary.MercenaryType;
import arclightcity.entity.stats.StatType;
import arclightcity.item.Item;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * GameSaveState — snapshot lengkap state game yang bisa di-serialize.
 *
 * Desain: daripada serialize seluruh GameEngine (yang punya listener, JavaFX refs,
 * dan object non-serializable), kita extract data penting ke record/class sederhana.
 *
 * Structure:
 *   GameSaveState
 *   ├── PlayerData        (nama, level, exp, gold, HP, skills, dll)
 *   ├── List<MercData>    (merc yang dimiliki)
 *   ├── List<ItemData>    (inventory)
 *   └── ProgressData      (floor terdalam, total combat, dll)
 */
public class GameSaveState implements Serializable {

    private static final long serialVersionUID = 20260506L; // v0.5.9 - updated save format

    // ── Meta ────────────────────────────────────────────────
    public String    saveId;         // "MANUAL" atau "AUTO"
    public String    savedAt;        // timestamp human-readable
    public String    gameVersion;    // untuk compatibility check
    public boolean   isAutoSave;

    // ── Player ──────────────────────────────────────────────
    public PlayerData player;

    // ── Inventory ───────────────────────────────────────────
    public List<ItemData> inventoryItems = new ArrayList<>();

    // ── Mercenaries ─────────────────────────────────────────
    public List<MercData> ownedMercs  = new ArrayList<>();
    public List<String>   activeMercTypes = new ArrayList<>(); // MercenaryType names

    // ── Progress ────────────────────────────────────────────
    public ProgressData progress;

    // ════════════════════════════════════════════════════════
    // INNER DATA CLASSES
    // ════════════════════════════════════════════════════════

    public static class PlayerData implements Serializable {
        public String           name;
        public String           background;    // PlayerBackground.name()
        public int              level;
        public double           currentExp;
        public double           expToNext;
        public long             gold;
        public double           currentHp;
        public double           currentMp;
        public double           currentShield;
        public int              skillPoints;
        public List<String>     unlockedSkillIds  = new ArrayList<>();
        public List<String>     equippedSkillIds  = new ArrayList<>();
        public int              dungeonDepth;
        public Map<String, Double> baseStatsOverride = new HashMap<>(); // extra stats dari progression
    }

    public static class MercData implements Serializable {
        public String  mercType;      // MercenaryType.name()
        public int     loyaltyLevel;
        public double  currentHp;
        public double  currentMp;
        public double  currentShield;
        public boolean isActive;
    }

    public static class ItemData implements Serializable {
        public String  itemId;
        public String  itemClass;     // "Weapon", "Armor", "Accessory", "Consumable", "Material"
        public String  name;
        public String  rarity;        // Item.Rarity.name()
        public int     upgradeLevel;
        public int     calibrationCount;
        public int     quantity;       // untuk Consumable stack / Material qty
        public String  slot;          // "WEAPON", "ARMOR", "ACC1", "ACC2", null=bag
        // Stat bonuses disimpan sebagai Map<String, Double>
        public Map<String, Double> baseStats  = new HashMap<>();
        public Map<String, Double> bonusStats = new HashMap<>();
        // Sub-type info
        public String  subType;       // WeaponType, ArmorType, ConsumableType, MaterialType
        public double  effectValue;   // untuk Consumable
    }

    public static class ProgressData implements Serializable {
        public int  deepestFloorReached;
        public int  totalCombatsWon;
        public int  totalCombatsLost;
        public int  totalGoldEarned;
        public int  totalEnemiesDefeated;
        public long totalPlaytimeMs;  // playtime dalam milliseconds
        public long lastSaveMs;       // System.currentTimeMillis() saat save
    }

    // ════════════════════════════════════════════════════════
    // FACTORY
    // ════════════════════════════════════════════════════════

    /** Buat GameSaveState kosong dengan timestamp sekarang */
    public static GameSaveState create(boolean isAuto) {
        GameSaveState s  = new GameSaveState();
        s.saveId         = isAuto ? "AUTO" : "MANUAL";
        s.isAutoSave     = isAuto;
        s.savedAt        = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        s.gameVersion    = "0.3.3";
        s.player         = new PlayerData();
        s.progress       = new ProgressData();
        return s;
    }

    /** Summary singkat untuk ditampilkan di main menu */
    public String getSummary() {
        if (player == null) return "EMPTY";
        long playMin = progress.totalPlaytimeMs / 60000;
        String timeStr = savedAt != null ? savedAt : "—";
        return player.name + "  LV." + player.level + "\n" +
               "Lantai " + player.dungeonDepth + "  |  " +
               String.format("%02d:%02d", playMin/60, playMin%60) + "\n" +
               timeStr;
    }

    @Override
    public String toString() {
        return "SaveState[" + saveId + " | " + getSummary() + "]";
    }
}
