package arclightcity.item;

import arclightcity.entity.stats.StatType;
import java.util.Map;

public class Weapon extends Equipment {

    /**
     * WeaponType — semua jenis pedang/katana untuk mendukung lore Asuna.
     * Tipe serangan tetap ada (Physical/Cyber/Energy) tapi bentuknya selalu pedang.
     */
    public enum WeaponType {
        KATANA,          // Katana klasik — balanced Physical
        ODACHI,          // Pedang panjang Jepang — Physical berat, slow
        WAKIZASHI,       // Katana pendek — cepat, dual wield
        KERIS_SWORD,     // Keris berbentuk pedang — Physical + Energy
        GOLOK_RUNE,      // Golok rune — Physical + Cyber debuff
        KUJANG_BLADE,    // Kujang sebagai pedang — Energy slash
        FLAME_KATANA,    // Katana berapi — Physical + burn DoT
        SHADOW_BLADE,    // Pedang bayangan — crit + evasion
        DIVINE_SWORD     // Pedang kahyangan — semua tipe damage
    }

    private final WeaponType weaponType;

    public Weapon(String name, String description, Rarity rarity,
                  WeaponType weaponType, Map<StatType, Double> stats) {
        super(name, description, ItemType.WEAPON, rarity);
        this.weaponType = weaponType;
        this.baseStats.putAll(stats);
    }

    public WeaponType getWeaponType() { return weaponType; }
}
