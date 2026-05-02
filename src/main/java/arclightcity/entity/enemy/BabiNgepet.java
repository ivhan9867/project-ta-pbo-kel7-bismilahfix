package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** Babi Ngepet — HP sangat tinggi, loot gold banyak */
public class BabiNgepet extends Enemy {
    public BabiNgepet() {
        super("Babi Ngepet","Babi gaib pembawa kekayaan — kalahkan untuk rampas hartanya.",
              EntityType.ENEMY, EnemyRace.BEAST, ThreatLevel.STANDARD, "LOOT_GOLD_HIGH");
        initStats(); expReward=50; goldReward=80; initVitals(); // goldReward tinggi!
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP,150); stats.setBase(StatType.MAX_MP,10);
        stats.setBase(StatType.PHYSICAL_ATK,16); stats.setBase(StatType.PHYSICAL_DEF,12);
        stats.setBase(StatType.ENERGY_DEF,5); stats.setBase(StatType.CYBER_DEF,5);
        stats.setBase(StatType.SPEED,7); stats.setBase(StatType.HP_REGEN,5);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getLowestHpTarget(enemies);
        return t==null ? CombatAction.pass() : CombatAction.basicAttack(List.of(t.getId()));
    }
    @Override protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getLowestHpTarget(enemies);
        return t==null ? CombatAction.pass() : CombatAction.useSkill("POWER_STRIKE", List.of(t.getId()));
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) { return normalAction(a,e); }
}
