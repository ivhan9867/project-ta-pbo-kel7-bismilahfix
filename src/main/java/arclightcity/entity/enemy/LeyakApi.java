package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** Leyak Api — Elite, AoE burn setiap giliran */
public class LeyakApi extends Enemy {
    public LeyakApi() {
        super("Leyak Api","Leyak berselubung api — membakar semua yang dekat.",
              EntityType.ENEMY, EnemyRace.SPIRIT, ThreatLevel.ELITE, "LOOT_ELITE_MED");
        initStats(); expReward=140; goldReward=65; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP,130); stats.setBase(StatType.MAX_MP,70);
        stats.setBase(StatType.ENERGY_ATK,38); stats.setBase(StatType.PHYSICAL_DEF,8);
        stats.setBase(StatType.ENERGY_DEF,25); stats.setBase(StatType.EVASION,0.15);
        stats.setBase(StatType.SPEED,13); stats.setBase(StatType.SKILL_POWER,0.30);
        stats.setBase(StatType.DAMAGE_MULT,0.15);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        List<String> all = enemies.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("BIO_IRRADIATE", all);
    }
    @Override protected CombatAction specialAction(List<Entity> a, List<Entity> e) { return normalAction(a,e); }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) {
        List<String> all = e.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("EMP_BURST", all);
    }
}
