package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** NYI RORO KIDUL — Boss 2, Floor 8. Ratu Laut Selatan. 3 fase. */
public class NyiRoroKidul extends Boss {
    public NyiRoroKidul() {
        super("Nyi Roro Kidul",
              "Ratu penguasa Samudra Hindia yang marahnya menghancurkan segalanya.",
              EnemyRace.DIVINE, ThreatLevel.BOSS,
              "LOOT_BOSS_HIGH", 3, new double[]{0.60, 0.25});
        initStats(); expReward = 600; goldReward = 200; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP, 800);   stats.setBase(StatType.MAX_MP, 200);
        stats.setBase(StatType.MAX_SHIELD, 150);
        stats.setBase(StatType.ENERGY_ATK, 55); stats.setBase(StatType.CYBER_ATK, 25);
        stats.setBase(StatType.PHYSICAL_DEF, 20); stats.setBase(StatType.ENERGY_DEF, 40);
        stats.setBase(StatType.CYBER_DEF, 30);  stats.setBase(StatType.SPEED, 12);
        stats.setBase(StatType.SKILL_POWER, 0.35);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        List<String> all = enemies.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("VOID_RUPTURE", all);
    }
    @Override protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getLowestHpTarget(enemies);
        return t == null ? CombatAction.pass() : CombatAction.useSkill("CORRUPT", List.of(t.getId()));
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) {
        List<String> all = e.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("DATA_FRAGMENTATION", all);
    }
    @Override protected void onPhaseTransition(int from, int to) {}
}
