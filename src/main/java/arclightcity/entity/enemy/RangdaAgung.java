package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** RANGDA AGUNG — Boss 3, Floor 12. Ratu iblis Bali. 3 fase. */
public class RangdaAgung extends Boss {
    public RangdaAgung() {
        super("Rangda Agung",
              "Ratu para iblis — menyentuhnya berarti menyentuh kutukan abadi.",
              EnemyRace.DEMON, ThreatLevel.BOSS,
              "LOOT_BOSS_EPIC", 3, new double[]{0.55, 0.20});
        initStats(); expReward = 900; goldReward = 350; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP, 1100);  stats.setBase(StatType.MAX_MP, 300);
        stats.setBase(StatType.MAX_SHIELD, 200);
        stats.setBase(StatType.ENERGY_ATK, 70); stats.setBase(StatType.PHYSICAL_DEF, 80);
        stats.setBase(StatType.ENERGY_DEF, 35); stats.setBase(StatType.CYBER_DEF, 35);
        stats.setBase(StatType.SPEED, 10);      stats.setBase(StatType.SKILL_POWER, 0.40);
        stats.setBase(StatType.EVASION, 0.15);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        List<String> all = enemies.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("CORRUPT", all);
    }
    @Override protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getHighestAtkTarget(enemies);
        return t == null ? CombatAction.pass() : CombatAction.useSkill("NULL_FIELD", List.of(t.getId()));
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) {
        List<String> all = e.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("NULL_PROTOCOL", all);
    }
    @Override protected void onPhaseTransition(int from, int to) {}
}
