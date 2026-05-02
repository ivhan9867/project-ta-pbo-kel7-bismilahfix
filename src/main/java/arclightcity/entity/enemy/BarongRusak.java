package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** Barong Rusak — Elite, toggle ATK/DEF mode tiap turn */
public class BarongRusak extends Enemy {
    private boolean atkMode = true;
    public BarongRusak() {
        super("Barong Rusak","Barong pelindung yang korup — bergantian menyerang dan bertahan.",
              EntityType.ENEMY, EnemyRace.BEAST, ThreatLevel.ELITE, "LOOT_ELITE_MED");
        initStats(); expReward=130; goldReward=60; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP,200); stats.setBase(StatType.MAX_MP,50);
        stats.setBase(StatType.MAX_SHIELD,80);
        stats.setBase(StatType.PHYSICAL_ATK,40); stats.setBase(StatType.ENERGY_ATK,20);
        stats.setBase(StatType.PHYSICAL_DEF,20); stats.setBase(StatType.ENERGY_DEF,20);
        stats.setBase(StatType.SPEED,10); stats.setBase(StatType.HP_REGEN,8);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        atkMode = !atkMode;
        if (atkMode) {
            Entity t = getLowestHpTarget(enemies);
            return t==null ? CombatAction.pass() : CombatAction.basicAttack(List.of(t.getId()));
        } else {
            return CombatAction.useSkill("IRON_SHIELD", List.of(getId()));
        }
    }
    @Override protected CombatAction specialAction(List<Entity> a, List<Entity> e) { return normalAction(a,e); }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) {
        List<String> all = e.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("SHOCKWAVE", all);
    }
}
