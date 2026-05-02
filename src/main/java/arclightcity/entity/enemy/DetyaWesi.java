package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** Detya Wesi — Elite, armor stacking per turn */
public class DetyaWesi extends Enemy {
    private int armorStacks = 0;
    public DetyaWesi() {
        super("Detya Wesi","Raksasa besi kuno — makin lama bertahan, makin tak tergoyahkan.",
              EntityType.ENEMY, EnemyRace.GIANT, ThreatLevel.ELITE, "LOOT_ELITE_HIGH");
        initStats(); expReward=170; goldReward=80; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP,280); stats.setBase(StatType.MAX_MP,30);
        stats.setBase(StatType.MAX_SHIELD,100);
        stats.setBase(StatType.PHYSICAL_ATK,50); stats.setBase(StatType.PHYSICAL_DEF,30);
        stats.setBase(StatType.CYBER_DEF,15); stats.setBase(StatType.ENERGY_DEF,15);
        stats.setBase(StatType.SPEED,6); stats.setBase(StatType.BLOCK_CHANCE,0.20);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        armorStacks = Math.min(armorStacks + 1, 5);
        stats.addBuff(StatType.PHYSICAL_DEF, 8.0); // stack armor setiap giliran
        Entity t = getLowestHpTarget(enemies);
        return t == null ? CombatAction.pass() : CombatAction.basicAttack(List.of(t.getId()));
    }
    @Override protected CombatAction specialAction(List<Entity> a, List<Entity> e) {
        List<String> all = e.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("SHOCKWAVE", all);
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) { return specialAction(a,e); }
}
