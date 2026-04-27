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

        String[] names = {"Neon Jacket", "Combat Vest", "Exoplate", "Shadow Coat",
                          "Titanium Shell", "Void Cloak", "Synth Armor"};
        return new Armor(names[RNG.nextInt(names.length)],
                "Generated armor — " + rarity.displayName,
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

        String[] names = {"Neon Ring", "Cyber Implant", "Signal Booster", "Ghost Chip",
                          "Void Lens", "Resonance Core", "Overclock Module", "Shield Cell"};
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
            case 0 -> new Consumable("Health Pack",
                    "Restores HP", rarity, Consumable.ConsumableType.HEALTH_PACK,
                    50 + 20 * mult);
            case 1 -> new Consumable("MP Injector",
                    "Restores MP", rarity, Consumable.ConsumableType.MP_PACK,
                    30 + 15 * mult);
            case 2 -> new Consumable("Antidote",
                    "Cleanses DOT effects", rarity, Consumable.ConsumableType.ANTIDOTE, 0);
            default -> new Consumable("Stim Pack",
                    "Buffs next attack", rarity, Consumable.ConsumableType.BUFF_ITEM, mult);
        };
    }

    // ── Weapon Names ──────────────────────────────────────────

    private static String[] getWeaponNames(String bias) {
        return switch (bias) {
            case "CYBER"  -> new String[]{"Virus Blade", "Hack Injector", "Data Lance",
                                          "Neural Spike", "Code Disruptor"};
            case "ENERGY" -> new String[]{"Neon Emitter", "Plasma Rod", "Void Pulse",
                                          "Arc Wand", "Resonance Gun"};
            default       -> new String[]{"Street Blade", "Chrome Pistol", "Heavy Revolver",
                                          "Shredder Knife", "Combat Shotgun"};
        };
    }
}
