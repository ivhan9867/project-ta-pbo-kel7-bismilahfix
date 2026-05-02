package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** Banaspati — kepala api terbang, burn DOT stack */
public class Banaspati extends Enemy {
    public Banaspati() {
        super("Banaspati","Kepala api yang melayang — setiap sentuhan membakar jiwa.",
              EntityType.ENEMY, EnemyRace.SPIRIT, ThreatLevel.STANDARD, "LOOT_SPIRIT_MED");
        initStats(); expReward=45; goldReward=22; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP,60); stats.setBase(StatType.MAX_MP,20);
        stats.setBase(StatType.ENERGY_ATK,22); stats.setBase(StatType.PHYSICAL_DEF,2);
        stats.setBase(StatType.ENERGY_DEF,15); stats.setBase(StatType.EVASION,0.12);
        stats.setBase(StatType.SPEED,14); stats.setBase(StatType.DAMAGE_MULT,0.10);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getLowestHpTarget(enemies);
        return t==null ? CombatAction.pass() : CombatAction.useSkill("NEON_VENOM", List.of(t.getId()));
    }
    @Override protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        List<String> allIds = enemies.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return allIds.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("BIO_IRRADIATE", allIds);
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) { return specialAction(a,e); }
}
