package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** Garuda Korup — Elite, evasion tinggi, dive AoE */
public class GarudaKorup extends Enemy {
    public GarudaKorup() {
        super("Garuda Korup","Garuda suci yang terkorupsi — kecepatan dan kekuatan tak tertandingi.",
              EntityType.ENEMY, EnemyRace.BEAST, ThreatLevel.ELITE, "LOOT_ELITE_HIGH");
        initStats(); expReward=160; goldReward=75; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP,180); stats.setBase(StatType.MAX_MP,60);
        stats.setBase(StatType.PHYSICAL_ATK,45); stats.setBase(StatType.ENERGY_ATK,25);
        stats.setBase(StatType.PHYSICAL_DEF,15); stats.setBase(StatType.ENERGY_DEF,20);
        stats.setBase(StatType.EVASION,0.25); stats.setBase(StatType.SPEED,16);
        stats.setBase(StatType.CRIT_CHANCE,0.20); stats.setBase(StatType.CRIT_DAMAGE,0.50);
        stats.setBase(StatType.ARMOR_PIERCE,0.15);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getFastestTarget(enemies);
        return t==null ? CombatAction.pass() : CombatAction.useSkill("PHANTOM_SHOT", List.of(t.getId()));
    }
    @Override protected CombatAction specialAction(List<Entity> a, List<Entity> e) {
        List<String> all = e.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("SHOCKWAVE", all);
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) {
        Entity t = getLowestHpTarget(e);
        return t==null ? CombatAction.pass() : CombatAction.useSkill("SHADOW_STEP", List.of(t.getId()));
    }
}
