package arclightcity.item;

import arclightcity.entity.stats.StatType;
import java.util.*;

/**
 * Item — base class semua item di game.
 */
public abstract class Item {

    public enum Rarity {
        COMMON    ("Common",    "#AAAAAA", 1.0,  3),
        UNCOMMON  ("Uncommon",  "#44FF44", 1.3,  5),
        RARE      ("Rare",      "#4488FF", 1.7,  7),
        EPIC      ("Epic",      "#AA44FF", 2.2, 10),
        LEGENDARY ("Legendary", "#FFAA00", 3.0, 15),
        MYTHIC    ("✦ Mythic",  "#FF6B00", 5.0, 25); // Eksklusif — tidak bisa drop biasa

        public final String displayName;
        public final String hexColor;
        public final double statMultiplier;
        public final int    maxUpgradeLevel;

        Rarity(String n, String c, double m, int u) {
            displayName = n; hexColor = c; statMultiplier = m; maxUpgradeLevel = u;
        }
    }

    public enum ItemType { WEAPON, ARMOR, ACCESSORY, CONSUMABLE, MATERIAL }

    // ── Fields ───────────────────────────────────────────────
    protected final String   id;
    protected       String   name;
    protected       String   description;
    protected final ItemType itemType;
    protected final Rarity   rarity;
    protected       int      upgradeLevel  = 0;
    protected       int      calibrationCount = 0;

    // ── Constructor ─────────────────────────────────────────
    protected Item(String name, String description, ItemType itemType, Rarity rarity) {
        this.id          = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.name        = name;
        this.description = description;
        this.itemType    = itemType;
        this.rarity      = rarity;
    }

    // ── Abstract ─────────────────────────────────────────────
    public abstract Map<StatType, Double> getStatBonuses();
    public abstract String getDisplaySummary();

    // ── Getters ─────────────────────────────────────────────
    public String   getId()              { return id; }
    public String   getName()            { return name; }
    public String   getDescription()     { return description; }
    public ItemType getItemType()        { return itemType; }
    public Rarity   getRarity()          { return rarity; }
    public int      getUpgradeLevel()    { return upgradeLevel; }
    public int      getCalibrationCount(){ return calibrationCount; }
    public boolean  canUpgrade()         { return upgradeLevel < rarity.maxUpgradeLevel; }

    public String getFullName() {
        return upgradeLevel > 0
                ? name + " +" + upgradeLevel
                : name;
    }
}
