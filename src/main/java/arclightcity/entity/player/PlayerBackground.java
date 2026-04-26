package arclightcity.entity.player;
import arclightcity.entity.stats.StatType;

import arclightcity.entity.stats.StatSheet;

/**
 * Background / asal usul player.
 * Tiap background memberi bonus stat unik + flavor lore.
 * Tidak mengunci skill apapun — classless tetap berlaku.
 */
public enum PlayerBackground {

    // ── STREET FIGHTER ───────────────────────────────────────
    STREET_BRAWLER(
        "Street Brawler",
        "Tumbuh di jalanan Arclight City, survive dengan tangan kosong. " +
        "Keras, cepat, dan tidak kenal ampun.",
        sheet -> {
            sheet.addBase(StatType.PHYSICAL_ATK, 12);
            sheet.addBase(StatType.MAX_HP,        30);
            sheet.addBase(StatType.PHYSICAL_DEF,   8);
            sheet.addBase(StatType.SPEED,           3);
            sheet.addBase(StatType.CRIT_CHANCE,  0.05);
            sheet.addBase(StatType.DAMAGE_MULT,  0.05); // +5% raw damage
        }
    ),

    NETRUNNER(
        "Netrunner",
        "Eks-karyawan MegaCorp yang kabur setelah tahu terlalu banyak. " +
        "Otak jenius, jari cepat, dan tahu cara merusak sistem.",
        sheet -> {
            sheet.addBase(StatType.CYBER_ATK,     15);
            sheet.addBase(StatType.MAX_MP,         25);
            sheet.addBase(StatType.MP_REGEN,        3);
            sheet.addBase(StatType.CYBER_DEF,       8);
            sheet.addBase(StatType.SKILL_POWER,   0.15);
            sheet.addBase(StatType.COOLDOWN_REDUCE, 0.10);
            sheet.addBase(StatType.DAMAGE_MULT,   0.08); // +8% semua damage
        }
    ),

    VETERAN_SOLDIER(
        "Veteran Soldier",
        "Mantan tentara bayaran yang berpengalaman di medan perang. " +
        "Terlatih, disiplin, dan tahu cara bertahan di kondisi terburuk.",
        sheet -> {
            sheet.addBase(StatType.PHYSICAL_ATK, 8);
            sheet.addBase(StatType.PHYSICAL_DEF, 15);
            sheet.addBase(StatType.ENERGY_DEF,    8);
            sheet.addBase(StatType.MAX_HP,        50);
            sheet.addBase(StatType.MAX_SHIELD,    60); // veteran punya shield tinggi
            sheet.addBase(StatType.SHIELD_REGEN,  3);
            sheet.addBase(StatType.HP_REGEN,      3);
            sheet.addBase(StatType.BLOCK_CHANCE, 0.08);
        }
    ),

    ENERGY_ADEPT(
        "Energy Adept",
        "Satu dari sedikit orang yang bisa memanipulasi energi neon langsung. " +
        "Asal-usulnya misterius — bahkan dia sendiri tidak sepenuhnya tahu.",
        sheet -> {
            sheet.addBase(StatType.ENERGY_ATK,   18);
            sheet.addBase(StatType.ENERGY_DEF,   12);
            sheet.addBase(StatType.MAX_MP,        20);
            sheet.addBase(StatType.SKILL_POWER,  0.20);
            sheet.addBase(StatType.LIFESTEAL,    0.05);
            sheet.addBase(StatType.DAMAGE_MULT,  0.10); // energy adept damage bonus
        }
    ),

    GHOST_OPERATIVE(
        "Ghost Operative",
        "Agen bayangan tanpa identitas resmi. Spesialis infiltrasi, " +
        "sabotase, dan menghilang sebelum musuh sadar apa yang terjadi.",
        sheet -> {
            sheet.addBase(StatType.EVASION,      0.10);
            sheet.addBase(StatType.SPEED,          6);
            sheet.addBase(StatType.CRIT_CHANCE,  0.10);
            sheet.addBase(StatType.CRIT_DAMAGE,  0.30);
            sheet.addBase(StatType.ARMOR_PIERCE, 0.15);
            sheet.addBase(StatType.INITIATIVE,    8);
            sheet.addBase(StatType.DAMAGE_MULT,  0.12); // ghost strike bonus
        }
    ),

    TECHWRIGHT(
        "Techwright",
        "Insinyur jalanan yang bisa merakit senjata dari sampah dan " +
        "menemukan celah di armor siapapun. Inovatif dan adaptif.",
        sheet -> {
            sheet.addBase(StatType.PHYSICAL_ATK,  6);
            sheet.addBase(StatType.CYBER_ATK,     10);
            sheet.addBase(StatType.ARMOR_PIERCE,  0.12);
            sheet.addBase(StatType.MAX_MP,        15);
            sheet.addBase(StatType.MAX_SHIELD,    30); // tech barrier
            sheet.addBase(StatType.SHIELD_REGEN,  5);  // shield regen dari gadget
            sheet.addBase(StatType.COMMAND_AURA,  10);
            sheet.addBase(StatType.SYNC_RATE,     0.15);
        }
    );

    // ─────────────────────────────────────────────────────────

    public final String name;
    public final String lore;
    private final BackgroundBonus bonus;

    PlayerBackground(String name, String lore, BackgroundBonus bonus) {
        this.name  = name;
        this.lore  = lore;
        this.bonus = bonus;
    }

    public void applyBonusTo(StatSheet sheet) {
        bonus.apply(sheet);
    }

    @FunctionalInterface
    interface BackgroundBonus {
        void apply(StatSheet sheet);
    }
}
