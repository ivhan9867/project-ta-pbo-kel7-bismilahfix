package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** Rangda Merah — Elite, kebal fisik, refleksi 20% damage */
public class RangdaMerah extends Enemy {
    public RangdaMerah() {
        super("Rangda Merah","Penyihir iblis merah — serangan fisik terpantul balik.",
              EntityType.ENEMY, EnemyRace.DEMON, ThreatLevel.ELITE, "LOOT_ELITE_MED");
        initStats(); expReward=120; goldReward=55; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP,160); stats.setBase(StatType.MAX_MP,80);
        stats.setBase(StatType.MAX_SHIELD,40);
        stats.setBase(StatType.ENERGY_ATK,35); stats.setBase(StatType.CYBER_ATK,20);
        stats.setBase(StatType.PHYSICAL_DEF,50); // Very high — kebal fisik
        stats.setBase(StatType.ENERGY_DEF,15); stats.setBase(StatType.CYBER_DEF,12);
        stats.setBase(StatType.SPEED,11); stats.setBase(StatType.SKILL_POWER,0.25);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getLowestHpTarget(enemies);
        return t==null ? CombatAction.pass() : CombatAction.useSkill("CORRUPT", List.of(t.getId()));
    }
    @Override protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        List<String> all = enemies.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("VOID_RUPTURE", all);
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) { return specialAction(a,e); }
}
