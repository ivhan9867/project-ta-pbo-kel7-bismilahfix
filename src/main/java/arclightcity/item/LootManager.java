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
        return rollLootTable(tableId, rarity, 1);
    }
    private static Item rollLootTable(String tableId, Item.Rarity rarity, int floorTier) {
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
        return generateEquipment(rarity, bias, 1);
    }
    private static Equipment generateEquipment(Item.Rarity rarity, String bias, int floorTier) {
        int slotRoll = RNG.nextInt(6);
        Equipment eq = switch (slotRoll) {
            case 0 -> generateWeapon(rarity, bias);
            case 1 -> generateArmor(rarity, Armor.ArmorType.MEDIUM);
            case 2 -> generateArmor(rarity, Armor.ArmorType.HELMET);
            case 3 -> generateArmor(rarity, Armor.ArmorType.BOOTS);
            case 4 -> generateArmor(rarity, Armor.ArmorType.RING);
            default -> generateAccessory(rarity, bias);
        };
        eq.setItemTier(floorTier);
        return eq;
    }

    private static Weapon generateWeapon(Item.Rarity rarity, String bias) {
        Map<StatType, Double> stats = new EnumMap<>(StatType.class);
        double mult = rarity.statMultiplier;

        switch (bias) {
            case "CYBER" -> {
                stats.put(StatType.CYBER_ATK,    (12 + rarity.ordinal()*8) + RNG.nextDouble() * 15 * mult);
                stats.put(StatType.ARMOR_PIERCE, 0.05 + RNG.nextDouble() * 0.10);
            }
            case "ENERGY" -> {
                stats.put(StatType.ENERGY_ATK,   (12 + rarity.ordinal()*8) + RNG.nextDouble() * 15 * mult);
                stats.put(StatType.SKILL_POWER,  0.05 + RNG.nextDouble() * 0.15);
            }
            default -> { // PHYSICAL / MIXED
                stats.put(StatType.PHYSICAL_ATK, (14 + rarity.ordinal()*8) + RNG.nextDouble() * 15 * mult);
                stats.put(StatType.CRIT_CHANCE,  0.03 + RNG.nextDouble() * 0.07);
            }
        }

        // Bonus stat untuk rarity tinggi
        if (rarity.ordinal() >= Item.Rarity.RARE.ordinal()) {
            stats.put(StatType.CRIT_DAMAGE,  0.10 + RNG.nextDouble() * 0.20 * mult);
            stats.put(StatType.DAMAGE_MULT,  0.05 + RNG.nextDouble() * 0.10);
        }
        // On-hit effects berdasarkan rarity (semakin tinggi semakin banyak)
        if (rarity == Item.Rarity.UNCOMMON) {
            // 1 effect: bleed chance 15-25%
            stats.put(StatType.BLEED_ON_HIT, 0.15 + RNG.nextDouble() * 0.10);
        }
        if (rarity == Item.Rarity.RARE) {
            stats.put(StatType.BLEED_ON_HIT, 0.20 + RNG.nextDouble() * 0.15);
            if (RNG.nextBoolean()) stats.put(StatType.BURN_ON_HIT, 0.10 + RNG.nextDouble() * 0.10);
            stats.put(StatType.LIFESTEAL, 0.03 + RNG.nextDouble() * 0.05);
        }
        if (rarity.ordinal() >= Item.Rarity.EPIC.ordinal()) {
            stats.put(StatType.DAMAGE_MULT,  0.10 + RNG.nextDouble() * 0.12);
            stats.put(StatType.BLEED_ON_HIT, 0.25 + RNG.nextDouble() * 0.20);
            stats.put(StatType.BURN_ON_HIT,  0.15 + RNG.nextDouble() * 0.15);
            stats.put(StatType.POISON_ON_HIT,0.10 + RNG.nextDouble() * 0.15);
            stats.put(StatType.LIFESTEAL,    0.06 + RNG.nextDouble() * 0.08);
        }
        if (rarity == Item.Rarity.LEGENDARY) {
            stats.put(StatType.DAMAGE_MULT,  0.20 + RNG.nextDouble() * 0.15);
            stats.put(StatType.BLEED_ON_HIT, 0.35 + RNG.nextDouble() * 0.20);
            stats.put(StatType.BURN_ON_HIT,  0.25 + RNG.nextDouble() * 0.15);
            stats.put(StatType.POISON_ON_HIT,0.20 + RNG.nextDouble() * 0.15);
            stats.put(StatType.LIFESTEAL,    0.10 + RNG.nextDouble() * 0.10);
        }

        String[] names = getWeaponNames(bias);
        String name = names[RNG.nextInt(names.length)];

        Weapon.WeaponType wType = switch (bias) {
            case "CYBER"  -> Weapon.WeaponType.GOLOK_RUNE;
            case "ENERGY" -> Weapon.WeaponType.KUJANG_BLADE;
            default       -> RNG.nextBoolean() ? Weapon.WeaponType.KATANA : Weapon.WeaponType.SHADOW_BLADE;
        };

        // Ensure minimum stat values (tidak boleh < 1 untuk stat integer)
        stats.replaceAll((stat, val) -> {
            if (stat == StatType.PHYSICAL_ATK || stat == StatType.CYBER_ATK ||
                stat == StatType.ENERGY_ATK || stat == StatType.MAX_HP) {
                return Math.max(1.0, val);
            }
            return val;
        });
        Weapon w = new Weapon(name, "Generated weapon — " + rarity.displayName, rarity, wType, stats);
        return w;
    }

    private static Armor generateArmor(Item.Rarity rarity, Armor.ArmorType armorType) {
        Map<StatType, Double> stats = new EnumMap<>(StatType.class);
        double mult = rarity.statMultiplier;
        int ord = rarity.ordinal();

        switch (armorType) {
            case HELMET -> {
                // Selalu: HP + DEF
                stats.put(StatType.MAX_HP,       (20 + ord*12) + RNG.nextDouble() * 35 * mult);
                stats.put(StatType.PHYSICAL_DEF, 8  + RNG.nextDouble() * 12 * mult);
                // Variasi random
                int helmRoll = RNG.nextInt(4);
                if (helmRoll == 0 && ord >= 1)
                    stats.put(StatType.CYBER_DEF,     5 + RNG.nextDouble() * 10 * mult);
                if (helmRoll == 1 && ord >= 2)
                    stats.put(StatType.CRIT_CHANCE,   0.03 + RNG.nextDouble() * 0.07);
                if (helmRoll == 2 && ord >= 2)
                    stats.put(StatType.EVASION,       0.03 + RNG.nextDouble() * 0.06);
                if (helmRoll == 3 && ord >= 3)
                    stats.put(StatType.SKILL_POWER,   0.05 + RNG.nextDouble() * 0.10 * mult);
                if (ord >= 3) // RARE+: bonus HP Regen atau Max HP extra
                    stats.put(StatType.HP_REGEN, 2 + RNG.nextDouble() * 4 * mult);
                if (ord >= 4) // EPIC+: HP multiplier
                    stats.put(StatType.MAX_HP, stats.getOrDefault(StatType.MAX_HP, 0.0)
                              + RNG.nextDouble() * 20 * mult);
                if (ord >= 5) // LEGENDARY
                    stats.put(StatType.DAMAGE_MULT, 0.05 + RNG.nextDouble() * 0.08 * mult);
            }
            case BOOTS -> {
                // Selalu: Speed + Evasion
                stats.put(StatType.SPEED,       3 + RNG.nextDouble() * 5 * mult);
                stats.put(StatType.EVASION,     0.04 + RNG.nextDouble() * 0.08);
                // Variasi random
                int bootRoll = RNG.nextInt(4);
                if (bootRoll == 0 && ord >= 1)
                    stats.put(StatType.DAMAGE_MULT,   0.04 + RNG.nextDouble() * 0.08 * mult);
                if (bootRoll == 1 && ord >= 1)
                    stats.put(StatType.INITIATIVE,    2 + RNG.nextDouble() * 4 * mult);
                if (bootRoll == 2 && ord >= 2)
                    stats.put(StatType.ACCURACY,      0.05 + RNG.nextDouble() * 0.10);
                if (bootRoll == 3 && ord >= 2)
                    stats.put(StatType.MAX_HP,        100 + RNG.nextDouble() * 200 * mult);
                if (ord >= 3)
                    stats.put(StatType.COOLDOWN_REDUCE, 0.03 + RNG.nextDouble() * 0.06);
                if (ord >= 5) // LEGENDARY
                    stats.put(StatType.DAMAGE_MULT, stats.getOrDefault(StatType.DAMAGE_MULT,0.0)
                              + 0.05 + RNG.nextDouble() * 0.08);
            }
            case RING -> {
                // Ring: 5 build berbeda — lebih banyak variasi
                int ringRoll = RNG.nextInt(5);
                switch (ringRoll) {
                    case 0 -> { // Crit-focused
                        stats.put(StatType.CRIT_CHANCE,  0.06 + RNG.nextDouble() * 0.10);
                        stats.put(StatType.CRIT_DAMAGE,  0.18 + RNG.nextDouble() * 0.22 * mult);
                        if (ord >= 3) stats.put(StatType.DAMAGE_MULT, 0.05 + RNG.nextDouble() * 0.10 * mult);
                    }
                    case 1 -> { // HP/Shield-focused
                        stats.put(StatType.MAX_HP,     250 + RNG.nextDouble() * 400 * mult);
                        stats.put(StatType.MAX_SHIELD, 80 + RNG.nextDouble() * 150 * mult);
                        if (ord >= 3) stats.put(StatType.SHIELD_REGEN, 3 + RNG.nextDouble() * 5 * mult);
                    }
                    case 2 -> { // Lifesteal + On-Hit
                        stats.put(StatType.LIFESTEAL,  0.04 + RNG.nextDouble() * 0.08);
                        stats.put(StatType.MAX_HP,     180 + RNG.nextDouble() * 280 * mult);
                        if (ord >= 4) stats.put(StatType.BLEED_ON_HIT, 0.12 + RNG.nextDouble() * 0.18);
                    }
                    case 3 -> { // Damage-focused
                        stats.put(StatType.DAMAGE_MULT,  0.07 + RNG.nextDouble() * 0.12 * mult);
                        stats.put(StatType.SKILL_POWER,  0.06 + RNG.nextDouble() * 0.10 * mult);
                        if (ord >= 3) stats.put(StatType.CRIT_CHANCE, 0.04 + RNG.nextDouble() * 0.07);
                    }
                    case 4 -> { // Utility/Support
                        stats.put(StatType.COOLDOWN_REDUCE, 0.05 + RNG.nextDouble() * 0.10);
                        stats.put(StatType.MAX_MP,          20 + RNG.nextDouble() * 30 * mult);
                        if (ord >= 3) stats.put(StatType.ACCURACY, 0.05 + RNG.nextDouble() * 0.10);
                        if (ord >= 4) stats.put(StatType.EVASION,  0.04 + RNG.nextDouble() * 0.08);
                    }
                }
                if (ord >= 5) // LEGENDARY bonus
                    stats.put(StatType.DAMAGE_MULT, stats.getOrDefault(StatType.DAMAGE_MULT, 0.0)
                              + 0.06 + RNG.nextDouble() * 0.08);
            }
            default -> { // MEDIUM/HEAVY armor — body
                stats.put(StatType.MAX_HP,       (25 + ord*15) + RNG.nextDouble() * 45 * mult);
                stats.put(StatType.MAX_SHIELD,   100 + RNG.nextDouble() * 200 * mult);
                stats.put(StatType.PHYSICAL_DEF, 12 + RNG.nextDouble() * 18 * mult);
                stats.put(StatType.CYBER_DEF,    8  + RNG.nextDouble() * 12 * mult);
                stats.put(StatType.ENERGY_DEF,   8  + RNG.nextDouble() * 12 * mult);
                if (ord >= 2)
                    stats.put(StatType.EVASION, 0.02 + RNG.nextDouble() * 0.07);
                if (ord >= 3)
                    stats.put(StatType.SHIELD_REGEN, 2 + RNG.nextDouble() * 4 * mult);
                if (ord >= 4) {
                    stats.put(StatType.THORN, 0.08 + RNG.nextDouble() * 0.12);
                    stats.put(StatType.HP_REGEN, 2 + RNG.nextDouble() * 4 * mult);
                }
                if (ord >= 5) {
                    stats.put(StatType.TENACITY, 0.10 + RNG.nextDouble() * 0.15);
                    stats.put(StatType.THORN, stats.getOrDefault(StatType.THORN, 0.0)
                              + 0.08 + RNG.nextDouble() * 0.10);
                    stats.put(StatType.DAMAGE_MULT, 0.04 + RNG.nextDouble() * 0.07 * mult);
                }
            }
        }

        String name = switch (armorType) {
            case HELMET -> new String[]{"Helm Rajawali", "Mahkota Empu", "Topi Batik Gaib",
                "Helm Naga", "Pelindung Kepala Sakti"}[RNG.nextInt(5)];
            case BOOTS  -> new String[]{"Sandal Angin", "Sepatu Cakar Harimau", "Alas Kaki Gaib",
                "Sepatu Rajah", "Pelindung Kaki Sakti"}[RNG.nextInt(5)];
            case RING   -> new String[]{"Cincin Pamor", "Gelang Naga", "Cincin Semar",
                "Cincin Garuda", "Mahkota Jari"}[RNG.nextInt(5)];
            default     -> new String[]{"Baju Zirah Majapahit", "Tameng Naga", "Kain Batik Pelindung",
                "Rompi Rajah", "Zirah Gaib", "Perisai Garuda"}[RNG.nextInt(6)];
        };

        return new Armor(name, "Perlengkapan " + armorType.name().toLowerCase() +
                " — " + rarity.displayName, rarity, armorType, stats);
    }

    private static Armor generateArmor(Item.Rarity rarity) {
        return generateArmor(rarity, Armor.ArmorType.MEDIUM);
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
                    Consumable.ConsumableType.HEALTH_PACK, 600 + 150 * mult);
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
        // Semua senjata adalah jenis pedang — mendukung lore Asuna slash-type
        return switch (bias) {
            case "CYBER"  -> new String[]{
                "Golok Santet", "Pedang Rune Hitam", "Golok Data",
                "Pedang Byte", "Kujang Cyber"};
            case "ENERGY" -> new String[]{
                "Kujang Cahaya", "Pedang Surya Mini", "Keris Energi",
                "Pedang Api Langit", "Wakizashi Petir"};
            default -> new String[]{
                "Katana Pamor", "Golok Silat", "Pedang Gaib",
                "Wakizashi Bayangan", "Odachi Kala"};
        };
    }

    // ── RED BLOSSOM KATANA SYSTEM ────────────────────────────
    //
    // Satu-satunya item Mythic dalam game.
    // Cara mendapatkan:
    //   1. Kalahkan 5 boss berbeda (F10, F20, F30, F40, F50)
    //   2. Setiap boss drop 1 Red Essence Shard
    //   3. Kumpulkan 5 Shard → tempa Red Blossom Katana
    //
    // Red Blossom Katana adalah senjata Asuna untuk mengalahkan Theresa (F51).

    /** Generate Red Blossom Katana — senjata pamungkas Asuna */
    public static List<Item> generateMythicDrop() {
        var stats = new java.util.EnumMap<StatType, Double>(StatType.class);

        // Stat terkuat dalam game — setara level Asuna vs Theresa
        stats.put(StatType.PHYSICAL_ATK,  120.0);
        stats.put(StatType.CRIT_CHANCE,     0.40);
        stats.put(StatType.CRIT_DAMAGE,     1.20); // 220% crit
        stats.put(StatType.ARMOR_PIERCE,    0.50); // tembus 50% armor
        stats.put(StatType.SPEED,           10.0);
        stats.put(StatType.LIFESTEAL,       0.20); // 20% lifesteal
        stats.put(StatType.DAMAGE_MULT,     0.35); // +35% semua damage
        stats.put(StatType.EVASION,         0.10); // aura pedang

        Weapon katana = new Weapon(
            "✦ Red Blossom Katana",
            "Katana pamungkas yang ditempa dari 5 Serpihan Red Essence milik Garuda. " +
            "Satu-satunya senjata yang mampu melukai Theresa, pemimpin Demon Lord. " +
            "Ketika dihunuskan, kelopak sakura merah berguguran di sekitar pemegangnya.",
            Item.Rarity.MYTHIC,
            Weapon.WeaponType.KATANA,
            stats
        );

        return List.of(katana);
    }

    /** Generate weapon untuk dijual di Toko Senjata kota */
    public static Weapon generateCityWeapon(int playerLevel, java.util.Random rng) {
        // Rarity berdasarkan level player
        Item.Rarity rarity = playerLevel < 5  ? Item.Rarity.COMMON :
                             playerLevel < 10 ? Item.Rarity.UNCOMMON :
                             playerLevel < 18 ? Item.Rarity.RARE :
                             playerLevel < 28 ? Item.Rarity.EPIC :
                                                Item.Rarity.LEGENDARY;

        Weapon.WeaponType[] types = Weapon.WeaponType.values();
        Weapon.WeaponType wType = types[rng.nextInt(types.length)];

        String[] names = {
            "Katana Pamor", "Odachi Kala", "Wakizashi Bayangan",
            "Keris Bintang", "Golok Rune Merah", "Kujang Cahaya",
            "Katana Api", "Pedang Gelap", "Pedang Surya Kecil"
        };
        String name = names[rng.nextInt(names.length)];

        double mult = rarity.statMultiplier;
        var stats = new java.util.EnumMap<StatType, Double>(StatType.class);
        stats.put(StatType.PHYSICAL_ATK, 10.0 * mult + rng.nextInt(5));
        if (rng.nextBoolean()) stats.put(StatType.CRIT_CHANCE, 0.05 * mult);
        if (rarity.ordinal() >= 2) stats.put(StatType.SPEED, 2.0 * mult);
        if (rarity.ordinal() >= 3) stats.put(StatType.ARMOR_PIERCE, 0.05 * mult);

        return new Weapon(name, "Dijual oleh Pak Empu.", rarity, wType, stats);
    }

    /** Generate Red Essence Shard — didapat dari membunuh boss */
    public static arclightcity.item.Material generateMythicFragment() {
        return new arclightcity.item.Material(
            "✦ Serpihan Red Essence",
            "Pecahan kristal merah yang mengalir dari tubuh boss yang dikalahkan. " +
            "Kumpulkan 5 Serpihan dari 5 boss berbeda untuk menempa Red Blossom Katana.",
            Item.Rarity.MYTHIC,
            arclightcity.item.Material.MaterialType.MYTHIC_FRAGMENT
        );
    }
    /** Drop gacha ticket dari boss/elite (public helper dipanggil DungeonManager) */
    public static void tryDropGachaTicket(arclightcity.engine.GameEngine engine, boolean isBoss) {
        double chance = isBoss ? 0.18 : 0.05;
        if (RNG.nextDouble() < chance) {
            engine.addGachaTickets(1);
        }
    }


}
