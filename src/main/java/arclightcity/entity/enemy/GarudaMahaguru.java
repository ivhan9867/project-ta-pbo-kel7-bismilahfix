package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;

/** GARUDA MAHAGURU — Boss 4, Floor 16. Garuda setengah dewa. 4 fase. */
public class GarudaMahaguru extends Boss {
    public GarudaMahaguru() {
        super("Garuda Mahaguru",
              "Garuda setengah dewa — gerakannya bagai angin, pukulannya bagai petir.",
              EnemyRace.DIVINE, ThreatLevel.BOSS,
              "LOOT_BOSS_LEGENDARY", 4, new double[]{0.70, 0.40, 0.15});
        initStats(); expReward = 1400; goldReward = 600; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP, 1600);  stats.setBase(StatType.MAX_MP, 250);
        stats.setBase(StatType.MAX_SHIELD, 100);
        stats.setBase(StatType.PHYSICAL_ATK, 90); stats.setBase(StatType.ENERGY_ATK, 60);
        stats.setBase(StatType.PHYSICAL_DEF, 35); stats.setBase(StatType.ENERGY_DEF, 50);
        stats.setBase(StatType.CYBER_DEF, 25);    stats.setBase(StatType.SPEED, 20);
        stats.setBase(StatType.EVASION, 0.30);    stats.setBase(StatType.CRIT_CHANCE, 0.25);
        stats.setBase(StatType.CRIT_DAMAGE, 0.75); stats.setBase(StatType.ARMOR_PIERCE, 0.20);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        Entity t = getFastestTarget(enemies);
        return t == null ? CombatAction.pass() : CombatAction.useSkill("SHADOW_STEP", List.of(t.getId()));
    }
    @Override protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        List<String> all = enemies.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("SHOCKWAVE", all);
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) {
        List<String> all = e.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("SOVEREIGN_STRIKE", all);
    }
    @Override protected void onPhaseTransition(int from, int to) {}
}
