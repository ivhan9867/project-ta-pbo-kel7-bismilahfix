package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** Tuyul Pencuri — mencuri gold saat menyerang, sangat cepat */
public class TuyulPencuri extends Enemy {
    public TuyulPencuri() {
        super("Tuyul Pencuri","Makhluk kecil licik yang mengintai di sudut gelap pasar malam.",
              EntityType.ENEMY, EnemyRace.SPIRIT, ThreatLevel.STANDARD, "LOOT_SPIRIT_LOW");
        initStats(); expReward=25; goldReward=30; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP,50); stats.setBase(StatType.MAX_MP,15);
        stats.setBase(StatType.PHYSICAL_ATK,12); stats.setBase(StatType.CYBER_ATK,8);
        stats.setBase(StatType.PHYSICAL_DEF,3); stats.setBase(StatType.EVASION,0.20);
        stats.setBase(StatType.SPEED,18); stats.setBase(StatType.CRIT_CHANCE,0.15);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getFastestTarget(enemies);
        return t==null ? CombatAction.pass() : CombatAction.basicAttack(List.of(t.getId()));
    }
    @Override protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getLowestHpTarget(enemies);
        return t==null ? CombatAction.pass() : CombatAction.useSkill("STEAL_GOLD", List.of(t.getId()));
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) { return normalAction(a,e); }
}
