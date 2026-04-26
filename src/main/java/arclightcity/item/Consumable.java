package arclightcity.item;

import arclightcity.entity.stats.StatType;
import java.util.Map;

public class Consumable extends Item {

    public enum ConsumableType { HEALTH_PACK, MP_PACK, BUFF_ITEM, ANTIDOTE, GRENADE }

    private final ConsumableType consumableType;
    private final double         effectValue;
    private       int            stackCount = 1;

    public Consumable(String name, String description, Rarity rarity,
                      ConsumableType type, double effectValue) {
        super(name, description, ItemType.CONSUMABLE, rarity);
        this.consumableType = type;
        this.effectValue    = effectValue;
    }

    @Override
    public Map<StatType, Double> getStatBonuses() { return Map.of(); }

    @Override
    public String getDisplaySummary() {
        return "[" + rarity.displayName + "] " + name + " x" + stackCount
               + "\n  Effect: " + consumableType.name() + " (" + (int)effectValue + ")\n";
    }

    public ConsumableType getConsumableType() { return consumableType; }
    public double         getEffectValue()    { return effectValue; }
    public int            getStackCount()     { return stackCount; }
    public void           addStack()          { stackCount++; }
    public boolean        useOne()            {
        if (stackCount <= 0) return false;
        stackCount--;
        return true;
    }
}
