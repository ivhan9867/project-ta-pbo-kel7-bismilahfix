package arclightcity.item;

import arclightcity.entity.stats.StatType;
import java.util.Map;

public class Material extends Item {

    public enum MaterialType {
        SCRAP_METAL, CYBER_CHIP, NEON_CRYSTAL, VOID_FRAGMENT,
        MYTHIC_FRAGMENT,  // Didapat dari boss kill — kumpulkan 3 untuk craft Mythic weapon
        CALIBRATION_KIT, UPGRADE_CORE, LEGENDARY_SHARD
    }

    private final MaterialType materialType;
    private       int          quantity = 1;

    public Material(String name, String description, Rarity rarity,
                    MaterialType materialType) {
        super(name, description, ItemType.MATERIAL, rarity);
        this.materialType = materialType;
    }

    @Override
    public Map<StatType, Double> getStatBonuses() { return Map.of(); }

    @Override
    public String getDisplaySummary() {
        return "[Material] " + name + " x" + quantity + "\n  " + description + "\n";
    }

    public MaterialType getMaterialType() { return materialType; }
    public int          getQuantity()     { return quantity; }
    public void         addQuantity(int n){ quantity += n; }
    public boolean      consume(int n)    {
        if (quantity < n) return false;
        quantity -= n;
        return true;
    }
}
