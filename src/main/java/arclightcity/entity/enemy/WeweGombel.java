package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** Wewe Gombel — mencuri buff dari player, medium threat */
public class WeweGombel extends Enemy {
    public WeweGombel() {
        super("Wewe Gombel","Hantu perempuan tua yang memangsa buff dan kekuatan orang lain.",
              EntityType.ENEMY, EnemyRace.SPIRIT, ThreatLevel.STANDARD, "LOOT_SPIRIT_LOW");
        initStats(); expReward=35; goldReward=20; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP,70); stats.setBase(StatType.MAX_MP,30);
        stats.setBase(StatType.MAX_SHIELD,20);
        stats.setBase(StatType.PHYSICAL_ATK,14); stats.setBase(StatType.ENERGY_ATK,10);
        stats.setBase(StatType.PHYSICAL_DEF,5); stats.setBase(StatType.ENERGY_DEF,8);
        stats.setBase(StatType.SPEED,12); stats.setBase(StatType.EVASION,0.10);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getLowestHpTarget(enemies);
        return t==null ? CombatAction.pass() : CombatAction.basicAttack(List.of(t.getId()));
    }
    @Override protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getHighestAtkTarget(enemies);
        return t==null ? CombatAction.pass() : CombatAction.useSkill("STEAL_BUFF", List.of(t.getId()));
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) { return specialAction(a,e); }
}
