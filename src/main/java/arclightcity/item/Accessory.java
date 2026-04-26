package arclightcity.item;

import arclightcity.entity.stats.StatType;
import java.util.Map;

public class Accessory extends Equipment {

    public Accessory(String name, String description, Rarity rarity,
                     Map<StatType, Double> stats) {
        super(name, description, ItemType.ACCESSORY, rarity);
        this.baseStats.putAll(stats);
    }
}
