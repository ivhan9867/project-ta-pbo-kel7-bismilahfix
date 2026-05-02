package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** Pocong Listrik — shock + paralisis */
public class PocongListrik extends Enemy {
    public PocongListrik() {
        super("Pocong Listrik","Pocong yang tersambar petir — kini memancarkan listrik berbahaya.",
              EntityType.ENEMY, EnemyRace.SPIRIT, ThreatLevel.STANDARD, "LOOT_SPIRIT_LOW");
        initStats(); expReward=40; goldReward=18; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP,65); stats.setBase(StatType.MAX_MP,25);
        stats.setBase(StatType.CYBER_ATK,20); stats.setBase(StatType.ENERGY_ATK,12);
        stats.setBase(StatType.CYBER_DEF,10); stats.setBase(StatType.PHYSICAL_DEF,3);
        stats.setBase(StatType.SPEED,9); stats.setBase(StatType.ACCURACY,0.85);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getLowestHpTarget(enemies);
        return t==null ? CombatAction.pass() : CombatAction.basicAttack(List.of(t.getId()));
    }
    @Override protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getFastestTarget(enemies);
        return t==null ? CombatAction.pass() : CombatAction.useSkill("SHOCK_STRIKE", List.of(t.getId()));
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) {
        List<String> allIds = e.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return allIds.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("SHOCKWAVE", allIds);
    }
}
