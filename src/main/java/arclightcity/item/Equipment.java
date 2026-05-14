package arclightcity.item;

import arclightcity.entity.stats.StatType;
import java.util.*;

public class Equipment extends Item {

    protected final Map<StatType, Double> baseStats  = new EnumMap<>(StatType.class);
    protected final Map<StatType, Double> bonusStats = new EnumMap<>(StatType.class);
    private int itemTier = 1; // Tier lantai: 1=fl1-10, 2=fl11-20, dst. Cap upgrade = tier*10

    protected Equipment(String name, String description, ItemType type, Rarity rarity) {
        super(name, description, type, rarity);
    }

    @Override
    public Map<StatType, Double> getStatBonuses() {
        Map<StatType, Double> total = new EnumMap<>(StatType.class);
        baseStats.forEach((k, v) -> total.merge(k, v, Double::sum));
        bonusStats.forEach((k, v) -> total.merge(k, v, Double::sum));
        return total;
    }

    @Override
    public String getDisplaySummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(rarity.displayName).append("] ").append(getFullName()).append("\n");
        getStatBonuses().forEach((k, v) ->
                sb.append("  ").append(k.displayName).append(": +").append(String.format("%.1f", v)).append("\n"));
        if (calibrationCount > 0) sb.append("  Calibrated: ").append(calibrationCount).append("x\n");
        return sb.toString();
    }

    /** Max upgrade level berdasarkan tier lantai saat item drop */
    public int getMaxUpgradeFromTier() { return itemTier * 10; }

    public boolean canUpgrade() {
        int cap = Math.min(rarity.maxUpgradeLevel, getMaxUpgradeFromTier());
        return upgradeLevel < cap;
    }

    public void setItemTier(int tier) { this.itemTier = Math.max(1, tier); }
    public int  getItemTier()         { return itemTier; }

    public void applyUpgrade() {
        if (!canUpgrade()) return;
        upgradeLevel++;
        double mult = 0.08 + (rarity.ordinal() * 0.015);
        baseStats.replaceAll((k, v) -> v * (1 + mult));
    }

    /** True jika stat ini disimpan dalam skala 0.0-1.0 (persentase), bukan nilai absolut */
    private static boolean isPercentStat(arclightcity.entity.stats.StatType st) {
        return switch (st) {
            case CRIT_CHANCE, CRIT_DAMAGE, LIFESTEAL, THORN, ARMOR_PIERCE,
                 BLEED_ON_HIT, BURN_ON_HIT, POISON_ON_HIT,
                 DAMAGE_MULT, SHIELD_MULT, SKILL_POWER, COOLDOWN_REDUCE -> true;
            default -> false;
        };
    }

    private double rollCalibValue(Random rng, arclightcity.entity.stats.StatType st) {
        if (isPercentStat(st)) {
            // Range 3%-25% berdasarkan rarity (LEGENDARY max 25%)
            double minPct = 0.02 + rarity.ordinal() * 0.01; // 0.02–0.06
            double maxPct = 0.05 + rarity.ordinal() * 0.04; // 0.05–0.21
            return minPct + rng.nextDouble() * (maxPct - minPct);
        } else {
            // Nilai absolut (ATK, HP, DEF, dll)
            return (3 + rarity.ordinal()*3) + rng.nextDouble() * 15 * rarity.statMultiplier;
        }
    }

    public StatType calibrate(Random rng) {
        calibrationCount++;
        if (bonusStats.isEmpty()) {
            StatType newStat = getRandomOffensiveStat(rng);
            bonusStats.put(newStat, rollCalibValue(rng, newStat));
            return newStat;
        }
        List<StatType> keys = new ArrayList<>(bonusStats.keySet());
        StatType rerolled   = keys.get(rng.nextInt(keys.size()));
        bonusStats.put(rerolled, rollCalibValue(rng, rerolled));
        return rerolled;
    }

    private StatType getRandomOffensiveStat(Random rng) {
        StatType[] options = {
            StatType.PHYSICAL_ATK, StatType.CYBER_ATK, StatType.ENERGY_ATK,
            StatType.CRIT_CHANCE,  StatType.CRIT_DAMAGE, StatType.ARMOR_PIERCE,
            StatType.LIFESTEAL,    StatType.SPEED,        StatType.SKILL_POWER
        };
        return options[rng.nextInt(options.length)];
    }

    public Map<StatType, Double> getBaseStats()  { return Collections.unmodifiableMap(baseStats); }
    public Map<StatType, Double> getBonusStats() { return Collections.unmodifiableMap(bonusStats); }

    /** Dipakai saat load save — replace seluruh bonusStats dengan data yang disimpan */
    public void restoreBonusStats(Map<StatType, Double> saved) {
        bonusStats.clear();
        if (saved != null) bonusStats.putAll(saved);
    }

    /** Set upgrade level langsung tanpa trigger random stat bonus */
    public void setUpgradeLevelDirect(int level) { this.upgradeLevel = level; }

    /** Semua stat gabungan (base + bonus dari upgrade/kalibrasi) */
    public Map<StatType, Double> getEffectiveStats() {
        Map<StatType, Double> all = new java.util.LinkedHashMap<>(baseStats);
        bonusStats.forEach((k, v) -> all.merge(k, v, Double::sum));
        return all;
    }

    /** Jumlah kalibrasi yang sudah dilakukan */
    public int getCalibrationLevel() { return calibrationCount; }
}
