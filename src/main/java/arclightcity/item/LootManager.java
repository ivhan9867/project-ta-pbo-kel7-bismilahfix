package arclightcity.item;
import arclightcity.entity.stats.StatType;

import java.util.*;

/**
 * LootManager — generate item drop dari loot table.
 *
 * Loot table pakai weighted random:
 *   - Setiap entry punya weight
 *   - Roll dilakukan beberapa kali (tergantung floor & room type)
 *   - Rarity dipengaruhi floor (makin dalam makin bagus)
 */
public class LootManager {

    private static final Random RNG = new Random();

    // ── Main Loot Generation ──────────────────────────────────

    /**
     * Generate loot dari loot table ID.
     * @param lootTableId   ID dari loot table
     * @param floorLevel    floor dungeon (mempengaruhi rarity)
     * @return list item yang di-drop
     */
    public static List<Item> generateLoot(String lootTableId, int floorLevel) {
        List<Item> drops = new ArrayList<>();

        int rollCount = getLootRollCount(lootTableId);
        Item.Rarity bias = getRarityBias(floorLevel);

        for (int i = 0; i < rollCount; i++) {
            Item item = rollLootTable(lootTableId, bias);
            if (item != null) drops.add(item);
        }

        return drops;
    }

    // ── Roll Count ────────────────────────────────────────────

    private static int getLootRollCount(String tableId) {
        if (tableId.contains("BOSS"))  return 4; // boss drop lebih banyak
        if (tableId.contains("HIGH"))  return 3;
        if (tableId.contains("FLOOR")) return 2;
        return 1 + (RNG.nextDouble() < 0.3 ? 1 : 0); // 30% chance drop 2
    }

    // ── Rarity Bias by Floor ──────────────────────────────────

    private static Item.Rarity getRarityBias(int floor) {
        double roll = RNG.nextDouble();
        if (floor >= 20) {
            if (roll < 0.05) return Item.Rarity.LEGENDARY;
            if (roll < 0.20) return Item.Rarity.EPIC;
            if (roll < 0.50) return Item.Rarity.RARE;
            if (roll < 0.80) return Item.Rarity.UNCOMMON;
            return Item.Rarity.COMMON;
        }
        if (floor >= 10) {
            if (roll < 0.02) return Item.Rarity.LEGENDARY;
            if (roll < 0.10) return Item.Rarity.EPIC;
            if (roll < 0.35) return Item.Rarity.RARE;
            if (roll < 0.70) return Item.Rarity.UNCOMMON;
            return Item.Rarity.COMMON;
        }
        // Floor 1-9
        if (roll < 0.01) return Item.Rarity.LEGENDARY;
        if (roll < 0.05) return Item.Rarity.EPIC;
        if (roll < 0.20) return Item.Rarity.RARE;
        if (roll < 0.55) return Item.Rarity.UNCOMMON;
        return Item.Rarity.COMMON;
    }

    // ── Item Roll ─────────────────────────────────────────────

    private static Item rollLootTable(String tableId, Item.Rarity rarity) {
        // Tentukan tipe item
        double typeRoll = RNG.nextDouble();

        if (tableId.contains("ANDROID") || tableId.contains("CYBER")) {
            // Lebih banyak cyber item & material
            if (typeRoll < 0.15) return generateMaterial(rarity);
            if (typeRoll < 0.35) return generateConsumable(rarity);
            return generateEquipment(rarity, "CYBER");
        }
        if (tableId.contains("MUTANT")) {
            if (typeRoll < 0.20) return generateMaterial(rarity);
            if (typeRoll < 0.40) return generateConsumable(rarity);
            return generateEquipment(rarity, "PHYSICAL");
        }
        if (tableId.contains("SPECTER")) {
            if (typeRoll < 0.10) return generateMaterial(rarity);
            if (typeRoll < 0.25) return generateConsumable(rarity);
            return generateEquipment(rarity, "ENERGY");
        }
        if (tableId.contains("BOSS")) {
            // Boss: guaranteed equipment + material
            if (typeRoll < 0.60) return generateEquipment(rarity, "MIXED");
            if (typeRoll < 0.85) return generateMaterial(rarity);
            return generateConsumable(rarity);
        }
        // Default
        if (typeRoll < 0.15) return generateMaterial(rarity);
        if (typeRoll < 0.30) return generateConsumable(rarity);
        return generateEquipment(rarity, "MIXED");
    }

    // ── Equipment Generator ───────────────────────────────────

    private static Equipment generateEquipment(Item.Rarity rarity, String bias) {
        int slotRoll = RNG.nextInt(3);
        return switch (slotRoll) {
            case 0 -> generateWeapon(rarity, bias);
            case 1 -> generateArmor(rarity);
            default -> generateAccessory(rarity, bias);
        };
    }

    private static Weapon generateWeapon(Item.Rarity rarity, String bias) {
        Map<StatType, Double> stats = new EnumMap<>(StatType.class);
        double mult = rarity.statMultiplier;

        switch (bias) {
            case "CYBER" -> {
                stats.put(StatType.CYBER_ATK,    20 + RNG.nextDouble() * 15 * mult);
                stats.put(StatType.ARMOR_PIERCE, 0.05 + RNG.nextDouble() * 0.10);
            }
            case "ENERGY" -> {
                stats.put(StatType.ENERGY_ATK,   18 + RNG.nextDouble() * 15 * mult);
                stats.put(StatType.SKILL_POWER,  0.05 + RNG.nextDouble() * 0.15);
            }
            default -> { // PHYSICAL / MIXED
                stats.put(StatType.PHYSICAL_ATK, 22 + RNG.nextDouble() * 15 * mult);
                stats.put(StatType.CRIT_CHANCE,  0.03 + RNG.nextDouble() * 0.07);
            }
        }

        // Bonus stat untuk rarity tinggi
        if (rarity.ordinal() >= Item.Rarity.RARE.ordinal()) {
            stats.put(StatType.CRIT_DAMAGE,  0.10 + RNG.nextDouble() * 0.20 * mult);
            stats.put(StatType.DAMAGE_MULT,  0.03 + RNG.nextDouble() * 0.07); // +3-10% damage
        }
        if (rarity.ordinal() >= Item.Rarity.EPIC.ordinal()) {
            stats.put(StatType.DAMAGE_MULT,  0.08 + RNG.nextDouble() * 0.12); // override: +8-20%
        }
        if (rarity == Item.Rarity.LEGENDARY) {
            stats.put(StatType.LIFESTEAL,    0.05 + RNG.nextDouble() * 0.10);
            stats.put(StatType.DAMAGE_MULT,  0.15 + RNG.nextDouble() * 0.15); // legendary: +15-30%
        }

        String[] names = getWeaponNames(bias);
        String name = names[RNG.nextInt(names.length)];

        Weapon.WeaponType wType = switch (bias) {
            case "CYBER"  -> Weapon.WeaponType.CYBER_TOOL;
            case "ENERGY" -> Weapon.WeaponType.ENERGY_EMITTER;
            default       -> RNG.nextBoolean() ? Weapon.WeaponType.BLADE : Weapon.WeaponType.GUN;
        };

        return new Weapon(name, "Generated weapon — " + rarity.displayName, rarity, wType, stats);
    }

    private static Armor generateArmor(Item.Rarity rarity) {
        Map<StatType, Double> stats = new EnumMap<>(StatType.class);
        double mult = rarity.statMultiplier;

        stats.put(StatType.MAX_HP,       30 + RNG.nextDouble() * 40 * mult);
        stats.put(StatType.MAX_SHIELD,   15 + RNG.nextDouble() * 25 * mult); // armor selalu punya shield
        stats.put(StatType.PHYSICAL_DEF, 10 + RNG.nextDouble() * 15 * mult);
        stats.put(StatType.CYBER_DEF,    6  + RNG.nextDouble() * 10 * mult);
        stats.put(StatType.ENERGY_DEF,   6  + RNG.nextDouble() * 10 * mult);

        if (rarity.ordinal() >= Item.Rarity.RARE.ordinal()) {
            stats.put(StatType.EVASION,     0.02 + RNG.nextDouble() * 0.08);
            stats.put(StatType.SHIELD_REGEN, 2   + RNG.nextDouble() * 5 * mult);
        }
        if (rarity.ordinal() >= Item.Rarity.EPIC.ordinal()) {
            stats.put(StatType.SHIELD_MULT, 0.05 + RNG.nextDouble() * 0.15); // +5-20% max shield
        }
        if (rarity == Item.Rarity.LEGENDARY) {
            stats.put(StatType.TENACITY,    0.10 + RNG.nextDouble() * 0.15);
            stats.put(StatType.HP_REGEN,    3    + RNG.nextDouble() * 5);
            stats.put(StatType.SHIELD_MULT, 0.20 + RNG.nextDouble() * 0.20); // legendary: shield besar
        }

        String[] names = {"Baju Zirah Majapahit", "Tameng Naga", "Kain Batik Pelindung",
                          "Rompi Rajah", "Zirah Gaib", "Perisai Garuda", "Baju Besi Empu"};
        return new Armor(names[RNG.nextInt(names.length)],
                "Perlengkapan pelindung Nusantara — " + rarity.displayName,
                rarity, Armor.ArmorType.MEDIUM, stats);
    }

    private static Accessory generateAccessory(Item.Rarity rarity, String bias) {
        Map<StatType, Double> stats = new EnumMap<>(StatType.class);
        double mult = rarity.statMultiplier;

        int roll = RNG.nextInt(5); // tambah 1 opsi baru
        switch (roll) {
            case 0 -> {
                stats.put(StatType.CRIT_CHANCE,  0.05 + RNG.nextDouble() * 0.10 * mult);
                stats.put(StatType.CRIT_DAMAGE,  0.15 + RNG.nextDouble() * 0.20 * mult);
                stats.put(StatType.DAMAGE_MULT,  0.05 + RNG.nextDouble() * 0.08);
            }
            case 1 -> {
                stats.put(StatType.SPEED,        5 + RNG.nextDouble() * 8 * mult);
                stats.put(StatType.EVASION,      0.03 + RNG.nextDouble() * 0.07);
            }
            case 2 -> {
                stats.put(StatType.SKILL_POWER,  0.10 + RNG.nextDouble() * 0.20 * mult);
                stats.put(StatType.MAX_MP,       15   + RNG.nextDouble() * 25 * mult);
                stats.put(StatType.DAMAGE_MULT,  0.05 + RNG.nextDouble() * 0.10);
            }
            case 3 -> {
                stats.put(StatType.LIFESTEAL,    0.04 + RNG.nextDouble() * 0.08);
                stats.put(StatType.ARMOR_PIERCE, 0.05 + RNG.nextDouble() * 0.10);
                stats.put(StatType.DAMAGE_MULT,  0.08 + RNG.nextDouble() * 0.12);
            }
            default -> {
                // Shield-focused accessory
                stats.put(StatType.MAX_SHIELD,   20 + RNG.nextDouble() * 30 * mult);
                stats.put(StatType.SHIELD_REGEN,  3 + RNG.nextDouble() * 5 * mult);
                stats.put(StatType.SHIELD_MULT,  0.05 + RNG.nextDouble() * 0.15);
            }
        }

        String[] names = {"Gelang Rajah", "Kalung Garuda", "Cincin Semar",
                          "Jimat Naga", "Amulet Kahyangan", "Gelang Kala", "Cincin Pamor", "Keris Mini"};
        return new Accessory(names[RNG.nextInt(names.length)],
                "Generated accessory — " + rarity.displayName, rarity, stats);
    }

    // ── Material Generator ────────────────────────────────────

    private static Material generateMaterial(Item.Rarity rarity) {
        Material.MaterialType type;
        int qty;

        if (rarity == Item.Rarity.LEGENDARY) {
            type = Material.MaterialType.LEGENDARY_SHARD;
            qty  = 1;
        } else if (rarity.ordinal() >= Item.Rarity.EPIC.ordinal()) {
            type = RNG.nextBoolean()
                    ? Material.MaterialType.NEON_CRYSTAL
                    : Material.MaterialType.CALIBRATION_KIT;
            qty = 1 + RNG.nextInt(2);
        } else if (rarity.ordinal() >= Item.Rarity.RARE.ordinal()) {
            type = RNG.nextBoolean()
                    ? Material.MaterialType.CYBER_CHIP
                    : Material.MaterialType.NEON_CRYSTAL;
            qty = 2 + RNG.nextInt(3);
        } else {
            type = RNG.nextBoolean()
                    ? Material.MaterialType.SCRAP_METAL
                    : Material.MaterialType.CYBER_CHIP;
            qty = 3 + RNG.nextInt(5);
        }

        Material mat = new Material(type.name().replace("_", " "),
                "Crafting material", rarity, type);
        mat.addQuantity(qty - 1);
        return mat;
    }

    // ── Consumable Generator ──────────────────────────────────

    private static Consumable generateConsumable(Item.Rarity rarity) {
        double mult = rarity.statMultiplier;
        int roll    = RNG.nextInt(4);

        return switch (roll) {
            case 0 -> new Consumable("Jamu Kunyit",
                    "Ramuan tradisional pemulih tenaga — menyembuhkan luka fisik.", rarity,
                    Consumable.ConsumableType.HEALTH_PACK, 50 + 20 * mult);
            case 1 -> new Consumable("Tirta Mahkota",
                    "Air suci dari sumber tersembunyi — memulihkan kekuatan batin.", rarity,
                    Consumable.ConsumableType.MP_PACK, 30 + 15 * mult);
            case 2 -> new Consumable("Daun Suruh Sakti",
                    "Daun suruh yang dimanterai — menetralisir racun dan kutukan.", rarity,
                    Consumable.ConsumableType.ANTIDOTE, 0);
            default -> new Consumable("Sesajen Kekuatan",
                    "Persembahan ritual — membangkitkan kekuatan gaib dalam serangan.", rarity,
                    Consumable.ConsumableType.BUFF_ITEM, mult);
        };
    }

    // ── Weapon Names ──────────────────────────────────────────

    private static String[] getWeaponNames(String bias) {
        return switch (bias) {
            case "CYBER"  -> new String[]{"Santet Kristal", "Rajah Perusak", "Ilmu Hitam Runcing",
                                          "Keris Cyber", "Tombak Roh Data"};
            case "ENERGY" -> new String[]{"Cakra Neon", "Panah Petir", "Trisula Energi",
                                          "Lembing Surya", "Cahaya Kahyangan"};
            default       -> new String[]{"Keris Pamor", "Golok Siluman", "Tombak Rajawali",
                                          "Kujang Sakti", "Mandau Dayak"};
        };
    }

    // ── MYTHIC WEAPON GENERATOR ─────────────────────────────
    // Mythic tidak bisa drop dari generateLoot biasa.
    // Hanya didapat via: boss kill fragment, floor 20 clear, atau Pasar Gaib trade.

    public static List<Item> generateMythicDrop() {
        // Pilih random 1 dari 10 Mythic weapon
        String[] mythicWeapons = {
            "Keris Naga Raja", "Cakra Wisnu", "Tombak Inti Bumi",
            "Kujang Bintang", "Golok Roh Purba", "Trisula Samudra",
            "Panah Angin Sakti", "Cemeti Kilat", "Mandau Dayak Agung", "Pedang Surya"
        };
        Weapon.WeaponType[] wTypes = {
            Weapon.WeaponType.BLADE,         // Keris Naga Raja
            Weapon.WeaponType.ENERGY_EMITTER,// Cakra Wisnu
            Weapon.WeaponType.HEAVY,         // Tombak Inti Bumi
            Weapon.WeaponType.CYBER_TOOL,    // Kujang Bintang
            Weapon.WeaponType.BLADE,         // Golok Roh Purba
            Weapon.WeaponType.ENERGY_EMITTER,// Trisula Samudra
            Weapon.WeaponType.GUN,           // Panah Angin Sakti
            Weapon.WeaponType.CYBER_TOOL,    // Cemeti Kilat
            Weapon.WeaponType.BLADE,         // Mandau Dayak Agung
            Weapon.WeaponType.ENERGY_EMITTER // Pedang Surya
        };

        int idx = RNG.nextInt(mythicWeapons.length);
        var stats = new java.util.EnumMap<StatType, Double>(StatType.class);

        // Stats Mythic jauh di atas Legendary
        switch (idx) {
            case 0 -> { // Keris Naga Raja — ATK + lifesteal per kill (simulated via high lifesteal)
                stats.put(StatType.PHYSICAL_ATK, 85.0); stats.put(StatType.CRIT_CHANCE, 0.25);
                stats.put(StatType.LIFESTEAL, 0.20); stats.put(StatType.DAMAGE_MULT, 0.30);
            }
            case 1 -> { // Cakra Wisnu — Energy chain
                stats.put(StatType.ENERGY_ATK, 90.0); stats.put(StatType.SKILL_POWER, 0.50);
                stats.put(StatType.ARMOR_PIERCE, 0.30); stats.put(StatType.DAMAGE_MULT, 0.25);
            }
            case 2 -> { // Tombak Inti Bumi — ignore armor, no crit
                stats.put(StatType.PHYSICAL_ATK, 100.0); stats.put(StatType.ARMOR_PIERCE, 1.0);
                stats.put(StatType.PHYSICAL_DEF, 20.0); stats.put(StatType.DAMAGE_MULT, 0.20);
            }
            case 3 -> { // Kujang Bintang — auto skill every 5 hits (simulated via CDR)
                stats.put(StatType.CYBER_ATK, 75.0); stats.put(StatType.COOLDOWN_REDUCE, 0.50);
                stats.put(StatType.SKILL_POWER, 0.40); stats.put(StatType.ACCURACY, 0.30);
            }
            case 4 -> { // Golok Roh Purba — lifesteal kill heal
                stats.put(StatType.PHYSICAL_ATK, 80.0); stats.put(StatType.LIFESTEAL, 0.35);
                stats.put(StatType.HP_REGEN, 15.0); stats.put(StatType.DAMAGE_MULT, 0.25);
            }
            case 5 -> { // Trisula Samudra — AoE passive (via skill power)
                stats.put(StatType.ENERGY_ATK, 70.0); stats.put(StatType.SKILL_POWER, 0.60);
                stats.put(StatType.MAX_MP, 50.0); stats.put(StatType.MP_REGEN, 10.0);
            }
            case 6 -> { // Panah Angin Sakti — first strike via speed
                stats.put(StatType.PHYSICAL_ATK, 78.0); stats.put(StatType.SPEED, 25.0);
                stats.put(StatType.INITIATIVE, 30.0); stats.put(StatType.CRIT_DAMAGE, 1.0);
            }
            case 7 -> { // Cemeti Kilat — stun (via cyber)
                stats.put(StatType.CYBER_ATK, 85.0); stats.put(StatType.ACCURACY, 0.50);
                stats.put(StatType.CRIT_CHANCE, 0.30); stats.put(StatType.DAMAGE_MULT, 0.20);
            }
            case 8 -> { // Mandau Dayak Agung — double attack (via crit + speed)
                stats.put(StatType.PHYSICAL_ATK, 72.0); stats.put(StatType.SPEED, 18.0);
                stats.put(StatType.CRIT_CHANCE, 0.40); stats.put(StatType.ARMOR_PIERCE, 0.25);
            }
            default -> { // Pedang Surya — burn permanent
                stats.put(StatType.ENERGY_ATK, 82.0); stats.put(StatType.DAMAGE_MULT, 0.35);
                stats.put(StatType.SKILL_POWER, 0.30); stats.put(StatType.CRIT_DAMAGE, 0.75);
            }
        }

        Weapon w = new Weapon(
            mythicWeapons[idx],
            "Senjata Mythic — hanya yang terpilih dapat menggunakannya.",
            Item.Rarity.MYTHIC,
            wTypes[idx],
            stats
        );
        return List.of(w);
    }

    /** Generate Mythic Fragment (material untuk craft Mythic) */
    public static Material generateMythicFragment() {
        return new Material(
            "✦ Pecahan Mitik",
            "Serpihan kekuatan dari boss yang dikalahkan. Kumpulkan 3 untuk hasilkan senjata Mythic.",
            Item.Rarity.MYTHIC,
            Material.MaterialType.MYTHIC_FRAGMENT
        );
    }
}
