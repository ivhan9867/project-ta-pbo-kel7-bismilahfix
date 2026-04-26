package arclightcity.item;

import arclightcity.entity.stats.StatType;
import java.util.*;

public class Equipment extends Item {

    protected final Map<StatType, Double> baseStats  = new EnumMap<>(StatType.class);
    protected final Map<StatType, Double> bonusStats = new EnumMap<>(StatType.class);

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

    public void applyUpgrade() {
        if (!canUpgrade()) return;
        upgradeLevel++;
        double mult = 0.08 + (rarity.ordinal() * 0.015);
        baseStats.replaceAll((k, v) -> v * (1 + mult));
    }

    public StatType calibrate(Random rng) {
        calibrationCount++;
        if (bonusStats.isEmpty()) {
            StatType newStat = getRandomOffensiveStat(rng);
            double   val     = 5 + rng.nextDouble() * 15 * rarity.statMultiplier;
            bonusStats.put(newStat, val);
            return newStat;
        }
        List<StatType> keys = new ArrayList<>(bonusStats.keySet());
        StatType rerolled   = keys.get(rng.nextInt(keys.size()));
        double   newVal     = 5 + rng.nextDouble() * 15 * rarity.statMultiplier;
        bonusStats.put(rerolled, newVal);
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
}
