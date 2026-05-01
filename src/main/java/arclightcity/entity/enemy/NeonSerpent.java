package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * NEON SERPENT — Mutant berbentuk ular raksasa bermandikan cahaya neon.
 * Sangat cepat, serangan meracuni target, bisa dodge banyak.
 *
 * Floor: 3-10
 * Strategi: Stack Bleed + Poison, hindari serangan dengan Evasion tinggi.
 * Desperate: Coil Strike — serangan mematikan saat HP kritis.
 */
public class NeonSerpent extends Enemy {

    private int bleedStacks = 0;

    public NeonSerpent() {
        super("Naga Basuki",
              "Ular mutant raksasa yang tubuhnya bersinar cahaya neon ungu. " +
              "Gerakan hipnotis, gigitan mematikan.",
              EntityType.ENEMY,
              EnemyRace.MUTANT,
              ThreatLevel.STANDARD,
              "LOOT_MUTANT_MID");

        initStats();
        expReward  = 55;
        goldReward = 20;
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       65);
        stats.setBase(StatType.MAX_SHIELD,   0);   // serpent no shield
        stats.setBase(StatType.DAMAGE_MULT,  0.05);
        stats.setBase(StatType.MAX_MP,       30);
        stats.setBase(StatType.PHYSICAL_ATK, 14);
        stats.setBase(StatType.ENERGY_ATK,   12);
        stats.setBase(StatType.PHYSICAL_DEF, 5);
        stats.setBase(StatType.ENERGY_DEF,   15); // resist energy (warna neon)
        stats.setBase(StatType.EVASION,      0.20); // sangat lincah
        stats.setBase(StatType.SPEED,        18);  // tercepat di tier awal
        stats.setBase(StatType.CRIT_CHANCE,  0.15);
        stats.setBase(StatType.LIFESTEAL,    0.08);
        stats.setBase(StatType.ACCURACY,     0.90);
    }

    @Override
    protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        Entity target = getLowestHpTarget(enemies);
        if (target == null) return CombatAction.pass();

        // Venomous Bite: selalu coba stack Bleed
        target.applyEffect(new StatusEffect(StatusEffectType.BLEED, 3, 8.0, this.id));
        bleedStacks++;

        return CombatAction.basicAttack(List.of(target.getId()));
    }

    @Override
    protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        // Neon Venom: Bleed kuat + Slow target
        Entity target = getLowestHpTarget(enemies);
        if (target == null) return CombatAction.pass();

        target.applyEffect(new StatusEffect(StatusEffectType.BLEED, 4, 15.0, this.id));
        target.applyEffect(new StatusEffect(StatusEffectType.SLOW, 2, 0.5, this.id));

        return CombatAction.useSkill("NEON_VENOM", List.of(target.getId()));
    }

    @Override
    protected CombatAction desperateAction(List<Entity> allies, List<Entity> enemies) {
        // Coil Strike: semua enemy terkena Bleed + damage besar saat HP < 25%
        List<String> allTargetIds = enemies.stream()
                .filter(Entity::isAlive)
                .map(Entity::getId)
                .toList();
        enemies.stream().filter(Entity::isAlive).forEach(t ->
                t.applyEffect(new StatusEffect(StatusEffectType.BLEED, 3, 12.0, this.id)));

        return CombatAction.useSkill("COIL_STRIKE", allTargetIds);
    }

    @Override
    protected boolean shouldUseDesperateAction() { return Math.random() < 0.60; }
    @Override
    protected int getSpecialCooldown()           { return 2; }
}
