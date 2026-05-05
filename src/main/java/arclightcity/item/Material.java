package arclightcity.item;

import arclightcity.entity.stats.StatType;
import java.util.Map;

public class Material extends Item {

    public enum MaterialType {
        SCRAP_METAL, CYBER_CHIP, NEON_CRYSTAL, VOID_FRAGMENT,
        MYTHIC_FRAGMENT,    // Serpihan Red Essence dari boss
        CALIBRATION_KIT,    // Kalibrasi biasa
        CALIBRATOR,         // Kalibrator premium (beli di Bengkel Empu kota)
        UPGRADE_CORE,       // Upgrade biasa
        ULTRA_ENHANCE_CORE, // Ultra Enhance +9/+10 (rare, bisa gagal)
        LEGENDARY_SHARD
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
