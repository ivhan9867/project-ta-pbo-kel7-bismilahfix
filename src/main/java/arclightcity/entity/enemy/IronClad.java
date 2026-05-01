package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * IRON CLAD — Cyborg Elite berat berlapis armor industrial.
 * ELITE enemy dengan mechanic "Armor Phase":
 *   Phase 1 (HP 100-50%): Bertahan, counter attack
 *   Phase 2 (HP 50-25%): Armor retak, lemah tapi lebih agresif
 *   Phase 3 (HP <25%):   Berserker mode, buang semua DEF untuk ATK
 *
 * Floor: 8-20
 * Strategi: Tank damage, reflect serangan fisik, counter dengan Shockwave.
 */
public class IronClad extends Enemy {

    private int currentPhase = 1;
    private boolean phaseTransitionTriggered2 = false;
    private boolean phaseTransitionTriggered3 = false;

    public IronClad() {
        super("Raksasa Kala",
              "Mantan prajurit yang 80% tubuhnya digantikan besi dan baja industri. " +
              "Tidak lagi merasa sakit. Mungkin tidak lagi manusia.",
              EntityType.ELITE,
              EnemyRace.CYBORG,
              ThreatLevel.ELITE,
              "LOOT_CYBORG_HIGH");

        initStats();
        expReward  = 150;
        goldReward = 80;
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       250);
        stats.setBase(StatType.MAX_MP,       40);
        stats.setBase(StatType.MAX_SHIELD,   80);  // IronClad punya shield dari armor plating
        stats.setBase(StatType.SHIELD_REGEN, 8);   // shield regen per turn
        stats.setBase(StatType.PHYSICAL_ATK, 28);
        stats.setBase(StatType.CYBER_ATK,    10);
        stats.setBase(StatType.DAMAGE_MULT,  0.05);
        stats.setBase(StatType.PHYSICAL_DEF, 35);
        stats.setBase(StatType.CYBER_DEF,    5);
        stats.setBase(StatType.ENERGY_DEF,   20);
        stats.setBase(StatType.BLOCK_CHANCE, 0.20);
        stats.setBase(StatType.SPEED,        7);
        stats.setBase(StatType.HP_REGEN,     5);
        stats.setBase(StatType.ACCURACY,     0.85);
        stats.setBase(StatType.TENACITY,     0.30);
    }

    @Override
    protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        checkPhaseTransition();

        Entity target = getHighestHpTarget(enemies); // serang yang paling tanky
        if (target == null) return CombatAction.pass();

        return switch (currentPhase) {
            case 1 -> {
                // Phase 1: Fortify diri, lalu serang
                this.applyEffect(new StatusEffect(StatusEffectType.FORTIFY, 1, 0, this.id));
                yield CombatAction.basicAttack(List.of(target.getId()));
            }
            case 2 -> {
                // Phase 2: Armor retak, lebih agresif — tambahkan Shred ke target
                target.applyEffect(new StatusEffect(StatusEffectType.SHRED, 2, 10.0, this.id));
                yield CombatAction.basicAttack(List.of(target.getId()));
            }
            case 3 -> {
                // Phase 3: Berserker — serang target terendah HP
                Entity weakest = getLowestHpTarget(enemies);
                yield CombatAction.basicAttack(List.of(weakest != null ? weakest.getId() : target.getId()));
            }
            default -> CombatAction.basicAttack(List.of(target.getId()));
        };
    }

    @Override
    protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        // SHOCKWAVE: AoE fisik + chance Stun semua target
        List<String> allTargetIds = enemies.stream()
                .filter(Entity::isAlive).map(Entity::getId).toList();

        enemies.stream().filter(Entity::isAlive).forEach(t -> {
            if (Math.random() < 0.35) {
                t.applyEffect(new StatusEffect(StatusEffectType.STUN, 1, 0, this.id));
            }
        });

        return CombatAction.useSkill("SHOCKWAVE", allTargetIds);
    }

    private void checkPhaseTransition() {
        double hpPct = getHpPercent();

        if (hpPct < 0.50 && !phaseTransitionTriggered2) {
            phaseTransitionTriggered2 = true;
            currentPhase = 2;
            // Armor retak: DEF turun, ATK naik
            stats.addBase(StatType.PHYSICAL_DEF, -15);
            stats.addBase(StatType.PHYSICAL_ATK,  12);
            stats.addBase(StatType.SPEED,          3);
        }

        if (hpPct < 0.25 && !phaseTransitionTriggered3) {
            phaseTransitionTriggered3 = true;
            currentPhase = 3;
            // Buang semua DEF untuk ATK — berserker
            stats.addBase(StatType.PHYSICAL_DEF, -20);
            stats.addBase(StatType.CYBER_DEF,    -5);
            stats.addBase(StatType.PHYSICAL_ATK,  20);
            stats.addBase(StatType.SPEED,          5);
            stats.addBase(StatType.CRIT_CHANCE,  0.20);
            this.applyEffect(new StatusEffect(StatusEffectType.OVERCLOCK, 999, 0, this.id));
        }
    }

    @Override
    protected int getSpecialCooldown() { return currentPhase == 3 ? 2 : 4; }
    public int getCurrentPhase()       { return currentPhase; }
}
