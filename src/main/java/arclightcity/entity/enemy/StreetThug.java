package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * STREET THUG — Enemy manusia paling dasar.
 * Agresif, serangan fisik, kadang bisa stun dengan pukulan keras.
 *
 * Floor: 1-5
 * Strategi: Hajar target HP terendah, sesekali pakai Power Strike.
 */
public class StreetThug extends Enemy {

    public StreetThug() {
        super("Street Thug",
              "Preman jalanan Arclight City yang desperate dan berbahaya.",
              EntityType.ENEMY,
              EnemyRace.HUMAN,
              ThreatLevel.STANDARD,
              "LOOT_HUMAN_LOW");

        initStats();
        expReward  = 30;
        goldReward = 15;
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       80);
        stats.setBase(StatType.MAX_MP,       20);
        stats.setBase(StatType.MAX_SHIELD,   0);   // no shield, full HP fighter
        stats.setBase(StatType.PHYSICAL_ATK, 18);
        stats.setBase(StatType.CYBER_ATK,    3);
        stats.setBase(StatType.PHYSICAL_DEF, 8);
        stats.setBase(StatType.CYBER_DEF,    3);
        stats.setBase(StatType.ENERGY_DEF,   3);
        stats.setBase(StatType.DAMAGE_MULT,  0.0);
        stats.setBase(StatType.SPEED,        10);
        stats.setBase(StatType.ACCURACY,     0.88);
        stats.setBase(StatType.CRIT_CHANCE,  0.08);
    }

    @Override
    protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        Entity target = getLowestHpTarget(enemies);
        if (target == null) return CombatAction.pass();
        return CombatAction.basicAttack(List.of(target.getId()));
    }

    @Override
    protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        // Power Strike: damage lebih tinggi + chance stun
        Entity target = getLowestHpTarget(enemies);
        if (target == null) return CombatAction.pass();
        // Stun chance 30%
        if (Math.random() < 0.30) {
            target.applyEffect(new StatusEffect(StatusEffectType.STUN, 1, 0, this.id));
        }
        return CombatAction.useSkill("POWER_STRIKE", List.of(target.getId()));
    }

    @Override
    protected int getSpecialCooldown() { return 3; }
}
