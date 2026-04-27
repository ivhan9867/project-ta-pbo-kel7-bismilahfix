package arclightcity.entity.stats;

import java.util.EnumMap;
import java.util.Map;

/**
 * StatSheet menyimpan stat entity dalam 3 layer:
 *   base      = stat murni dari level/class
 *   equipment = bonus dari item yang dipakai
 *   buff      = bonus sementara dari skill/effect
 *
 * Final value = (base + equipment + buff) × multiplier
 * Ini memudahkan reset buff tanpa mengubah base stat.
 */
public class StatSheet {

    private final EnumMap<StatType, Double> base      = new EnumMap<>(StatType.class);
    private final EnumMap<StatType, Double> equipment = new EnumMap<>(StatType.class);
    private final EnumMap<StatType, Double> buff      = new EnumMap<>(StatType.class);

    // multiplier per stat (default 1.0) — bisa diubah oleh skill/debuff
    private final EnumMap<StatType, Double> multiplier = new EnumMap<>(StatType.class);

    // ── Constructor ─────────────────────────────────────────

    public StatSheet() {
        for (StatType t : StatType.values()) {
            base.put(t, 0.0);
            equipment.put(t, 0.0);
            buff.put(t, 0.0);
            multiplier.put(t, 1.0);
        }
        // default non-zero
        base.put(StatType.CRIT_DAMAGE,  1.5);
        base.put(StatType.ACCURACY,     0.95);
        base.put(StatType.CRIT_CHANCE,  0.05);
    }

    // ── Getter Final Value ───────────────────────────────────

    public double get(StatType type) {
        double raw = base.get(type) + equipment.get(type) + buff.get(type);
        return raw * multiplier.get(type);
    }

    public int getInt(StatType type) {
        return (int) Math.max(0, get(type));
    }

    // ── Setter Layer ─────────────────────────────────────────

    public void setBase(StatType type, double value) {
        base.put(type, value);
    }

    public void addBase(StatType type, double delta) {
        base.merge(type, delta, Double::sum);
    }

    public void setEquipment(StatType type, double value) {
        equipment.put(type, value);
    }

    public void addEquipmentBonus(StatType type, double delta) {
        equipment.merge(type, delta, Double::sum);
    }

    public void addBuff(StatType type, double delta) {
        buff.merge(type, delta, Double::sum);
    }

    public void removeBuff(StatType type, double delta) {
        buff.merge(type, -delta, Double::sum);
    }

    public void clearAllBuffs() {
        for (StatType t : StatType.values()) buff.put(t, 0.0);
    }

    public void clearEquipmentBonuses() {
        for (StatType t : StatType.values()) equipment.put(t, 0.0);
    }

    public void setMultiplier(StatType type, double value) {
        multiplier.put(type, value);
    }

    public void resetMultiplier(StatType type) {
        multiplier.put(type, 1.0);
    }

    // ── Utility ─────────────────────────────────────────────

    /**
     * Copy base stats dari sheet lain (untuk clone/template enemy)
     */
    public void copyBaseFrom(StatSheet other) {
        for (StatType t : StatType.values()) {
            this.base.put(t, other.base.get(t));
        }
    }

    /**
     * Scale semua base stat dengan faktor (untuk scaling difficulty dungeon)
     */
    public void scaleBase(double factor) {
        for (StatType t : StatType.values()) {
            base.put(t, base.get(t) * factor);
        }
    }

    /**
     * Print snapshot semua stat (untuk debug)
     */
    public String snapshot() {
        StringBuilder sb = new StringBuilder();
        for (StatType t : StatType.values()) {
            double val = get(t);
            if (val != 0) {
                sb.append(String.format("  %-20s: %.2f%n", t.displayName, val));
            }
        }
        return sb.toString();
    }

    // ── Raw access (untuk serialization / UI display) ─────────
    public Map<StatType, Double> getBaseMap()      { return base; }
    public Map<StatType, Double> getEquipmentMap() { return equipment; }
    public Map<StatType, Double> getBuffMap()      { return buff; }
}
