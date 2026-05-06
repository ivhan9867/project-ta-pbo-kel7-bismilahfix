package arclightcity.item;

import arclightcity.entity.stats.StatType;
import java.util.Map;

public class Armor extends Equipment {

    public enum ArmorType { LIGHT, MEDIUM, HEAVY, EXOSUIT, HELMET, BOOTS, RING }

    private final ArmorType armorType;

    public Armor(String name, String description, Rarity rarity,
                 ArmorType armorType, Map<StatType, Double> stats) {
        super(name, description, ItemType.ARMOR, rarity);
        this.armorType = armorType;
        this.baseStats.putAll(stats);
    }

    public ArmorType getArmorType() { return armorType; }
}
