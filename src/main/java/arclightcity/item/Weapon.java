package arclightcity.item;

import arclightcity.entity.stats.StatType;
import java.util.Map;

public class Weapon extends Equipment {

    public enum WeaponType { BLADE, GUN, CYBER_TOOL, ENERGY_EMITTER, HEAVY }

    private final WeaponType weaponType;

    public Weapon(String name, String description, Rarity rarity,
                  WeaponType weaponType, Map<StatType, Double> stats) {
        super(name, description, ItemType.WEAPON, rarity);
        this.weaponType = weaponType;
        this.baseStats.putAll(stats);
    }

    public WeaponType getWeaponType() { return weaponType; }
}
