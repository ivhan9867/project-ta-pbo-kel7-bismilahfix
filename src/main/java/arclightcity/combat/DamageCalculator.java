package arclightcity.combat;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.DamageType;
import arclightcity.entity.stats.StatType;

import arclightcity.entity.stats.StatSheet;

import java.util.Random;

/**
 * DamageCalculator — semua formula kalkulasi damage di satu tempat.
 *
 * Formula utama:
 *   rawDamage   = ATK stat + skill multiplier
 *   finalDamage = rawDamage × (100 / (100 + DEF)) × modifiers
 *
 * Modifiers:
 *   - Critical hit
 *   - Armor pierce
 *   - Status effects (Weaken, Shred, Empowered, dll)
 *   - Damage type resistance
 */
public class DamageCalculator {

    private static final Random RNG = new Random();

    // ── Main Damage Calculation ───────────────────────────────

    /**
     * Hitung damage dari attacker ke target.
     *
     * @param attacker    entity yang menyerang
     * @param target      entity yang diserang
     * @param baseDamage  damage sebelum DEF (dari ATK stat atau skill multiplier)
     * @param dmgType     tipe damage
     * @param armorPierce tambahan armor pierce dari skill (0.0 - 1.0)
     * @return            DamageCalcResult berisi detail lengkap
     */
    public static DamageCalcResult calculate(Entity attacker, Entity target,
                                              double baseDamage, DamageType dmgType,
                                              double armorPierce) {
        StatSheet atkStats = attacker.getStats();
        StatSheet defStats = target.getStats();

        // ── Step 1: Accuracy check ───────────────────────────
        double accuracy = atkStats.get(StatType.ACCURACY);
        double evasion  = defStats.get(StatType.EVASION);

        // Blind mengurangi accuracy drastis
        if (attacker.hasEffect(StatusEffectType.BLIND)) accuracy *= 0.40;
        // Focus menambah accuracy
        if (attacker.hasEffect(StatusEffectType.FOCUS)) accuracy = Math.min(1.0, accuracy + 0.20);

        double hitChance = Math.max(0.15, accuracy - evasion); // min 15% hit chance (was 5%)
        if (RNG.nextDouble() > hitChance) {
            return DamageCalcResult.missed();
        }

        // ── Step 2: Crit check ───────────────────────────────
        double critChance = atkStats.get(StatType.CRIT_CHANCE);
        double critDamage = atkStats.get(StatType.CRIT_DAMAGE);

        // Empowered: guaranteed crit + bonus crit damage
        boolean guaranteedCrit = attacker.hasEffect(StatusEffectType.EMPOWERED);
        // Expose target: attacker dapat +crit chance
        if (target.hasEffect(StatusEffectType.EXPOSE)) critChance += 0.30;
        // Focus: +crit chance
        if (attacker.hasEffect(StatusEffectType.FOCUS)) critChance += 0.15;

        boolean isCrit = guaranteedCrit || RNG.nextDouble() < critChance;
        double critMult = isCrit ? critDamage : 1.0;

        // ── Step 3: Base damage modifiers ────────────────────
        double dmg = baseDamage;

        // Weaken: reduce attacker's physical + cyber ATK
        if (attacker.hasEffect(StatusEffectType.WEAKEN)) dmg *= 0.70;

        // DAMAGE_MULT: multiplier additive dari equipment/background (misal 0.15 = +15%)
        double damageMult = 1.0 + atkStats.get(StatType.DAMAGE_MULT);
        dmg *= damageMult;

        // Skill power multiplier (khusus untuk skill, bukan basic attack)
        dmg *= atkStats.get(StatType.SKILL_POWER);

        // ── Step 4: DEF reduction ────────────────────────────
        double finalDmg = dmg;
        if (dmgType != DamageType.TRUE && dmgType != DamageType.HEAL) {
            double defense = defStats.get(dmgType.resistanceStat);

            // Shred: reduce target DEF
            if (target.hasEffect(StatusEffectType.SHRED)) {
                defense *= 0.80; // SHRED: -20% DEF (cukup terasa tapi tidak OP)
            }

            // Armor pierce: bypass sebagian DEF
            double totalPierce = atkStats.get(StatType.ARMOR_PIERCE) + armorPierce;
            totalPierce = Math.min(totalPierce, 0.90); // max 90% pierce
            defense *= (1.0 - totalPierce);

            // Formula: damage = raw × (100 / (100 + DEF))
            // DEF reduction cap: max 80% reduction (enemy tidak bisa blocking >80% damage)
            double defReduction = defense / (defense + 100.0);
            defReduction = Math.min(0.80, defReduction); // max 80% reduction
            finalDmg = dmg * (1.0 - defReduction);
        }

        // ── Step 5: Crit multiplier ──────────────────────────
        finalDmg *= critMult;

        // ── Step 6: Block check ──────────────────────────────
        double blockChance = defStats.get(StatType.BLOCK_CHANCE);
        boolean blocked = false;
        if (RNG.nextDouble() < blockChance) {
            finalDmg *= 0.50;
            blocked = true;
        }

        // ── Step 7: Overload check ───────────────────────────
        // Jika target punya Overload dan terkena Cyber attack → explode bonus damage
        double overloadBonus = 0;
        if (target.hasEffect(StatusEffectType.OVERLOAD) && dmgType == DamageType.CYBER) {
            overloadBonus = finalDmg * 0.40; // +40% bonus damage
            target.removeEffect(StatusEffectType.OVERLOAD);
        }

        finalDmg += overloadBonus;
        // Minimum damage: 15% dari base ATK dominan, min 5
        double dominantAtk = Math.max(
            atkStats.get(StatType.PHYSICAL_ATK),
            Math.max(atkStats.get(StatType.ENERGY_ATK), atkStats.get(StatType.CYBER_ATK))
        );
        double minDmg = Math.max(5, dominantAtk * 0.15);
        finalDmg = Math.max(minDmg, Math.round(finalDmg));

        return new DamageCalcResult(finalDmg, isCrit, blocked, false, overloadBonus > 0, dmgType);
    }

    // ── Basic Attack Damage ──────────────────────────────────

    /**
     * Hitung base damage untuk basic attack berdasarkan damage type.
     */
    public static double getBasicAttackDamage(Entity attacker, DamageType type) {
        StatSheet stats = attacker.getStats();
        // Multiplier 1.5x untuk basic attack agar terasa impactful
        double mult = 1.5;
        return switch (type) {
            case PHYSICAL -> stats.get(StatType.PHYSICAL_ATK) * mult;
            case CYBER    -> stats.get(StatType.CYBER_ATK)    * mult;
            case ENERGY   -> stats.get(StatType.ENERGY_ATK)   * mult;
            default       -> stats.get(StatType.PHYSICAL_ATK) * mult;
        };
    }

    /**
     * Tentukan damage type dominan untuk basic attack entity.
     * Pilih stat ATK tertinggi.
     */
    public static DamageType getDominantDamageType(Entity attacker) {
        StatSheet stats = attacker.getStats();
        double phys  = stats.get(StatType.PHYSICAL_ATK);
        double cyber = stats.get(StatType.CYBER_ATK);
        double energy= stats.get(StatType.ENERGY_ATK);

        if (phys >= cyber && phys >= energy)   return DamageType.PHYSICAL;
        if (cyber >= phys && cyber >= energy)  return DamageType.CYBER;
        return DamageType.ENERGY;
    }

    // ── Lifesteal ────────────────────────────────────────────

    /**
     * Hitung dan apply lifesteal setelah damage didealt.
     * @return jumlah HP yang dipulihkan
     */
    public static double applyLifesteal(Entity attacker, double damageDealt) {
        double lifesteal = attacker.getStats().get(StatType.LIFESTEAL);
        if (lifesteal <= 0) return 0;
        double healed = damageDealt * lifesteal;
        return attacker.receiveHeal(healed);
    }

    // ── Result Inner Class ────────────────────────────────────

    public static class DamageCalcResult {
        public final double     finalDamage;
        public final boolean    isCritical;
        public final boolean    isBlocked;
        public final boolean    isMissed;
        public final boolean    triggeredOverload;
        public final DamageType damageType;

        public DamageCalcResult(double finalDamage, boolean isCritical, boolean isBlocked,
                                boolean isMissed, boolean triggeredOverload, DamageType damageType) {
            this.finalDamage       = finalDamage;
            this.isCritical        = isCritical;
            this.isBlocked         = isBlocked;
            this.isMissed          = isMissed;
            this.triggeredOverload = triggeredOverload;
            this.damageType        = damageType;
        }

        public static DamageCalcResult missed() {
            return new DamageCalcResult(0, false, false, true, false, DamageType.PHYSICAL);
        }
    }
}
