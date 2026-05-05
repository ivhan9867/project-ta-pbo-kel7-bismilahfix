package arclightcity.entity.player;

import arclightcity.entity.stats.StatSheet;
import arclightcity.entity.stats.StatType;

/**
 * PlayerBackground — Identitas Asuna, protagonis Mythic Item Obtained.
 *
 * Satu nilai: ASUNA — gamer perempuan modern yang terjebak di Nusantara.
 * Enum dipertahankan untuk kompatibilitas save system.
 */
public enum PlayerBackground {

    ASUNA(
        "Asuna",
        "Seorang gamer perempuan dari dunia modern yang terjebak di Nusantara. " +
        "Ahli pedang slash — senjata andalannya katana. Ia harus mengumpulkan " +
        "5 Serpihan Red Essence untuk menempa Red Blossom Katana dan mengalahkan " +
        "Theresa, pemimpin para Demon Lord."
    );

    public final String name;
    public final String lore;

    PlayerBackground(String name, String lore) {
        this.name = name;
        this.lore = lore;
    }

    /** Stat awal Asuna — slash-type swordswoman, cepat dan kritis tinggi */
    public void applyBaseStats(StatSheet sheet) {
        sheet.addBase(StatType.MAX_HP,        120);
        sheet.addBase(StatType.MAX_SHIELD,     20);
        sheet.addBase(StatType.MAX_MP,         60);
        sheet.addBase(StatType.HP_REGEN,        2);
        sheet.addBase(StatType.MP_REGEN,        3);
        sheet.addBase(StatType.PHYSICAL_ATK,   22);
        sheet.addBase(StatType.CYBER_ATK,       5);
        sheet.addBase(StatType.ENERGY_ATK,      5);
        sheet.addBase(StatType.CRIT_CHANCE,  0.18);
        sheet.addBase(StatType.CRIT_DAMAGE,  0.50);
        sheet.addBase(StatType.ARMOR_PIERCE, 0.10);
        sheet.addBase(StatType.PHYSICAL_DEF,    8);
        sheet.addBase(StatType.CYBER_DEF,       4);
        sheet.addBase(StatType.ENERGY_DEF,      4);
        sheet.addBase(StatType.EVASION,      0.12);
        sheet.addBase(StatType.SPEED,          16);
        sheet.addBase(StatType.INITIATIVE,     14);
        sheet.addBase(StatType.ACCURACY,     0.90);
        sheet.addBase(StatType.LIFESTEAL,    0.05);
    }

    /** Starter skills Asuna — slash swordswoman */
    public String[] getStarterSkillIds() {
        return new String[]{"POWER_STRIKE", "PHANTOM_SHOT"};
    }
}
