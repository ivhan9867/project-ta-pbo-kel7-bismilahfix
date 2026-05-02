package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;
import java.util.Random;

/** SEMAR PAMUNGKAS — Final Boss, Floor 20. Wild card. 5 fase. */
public class SemarPamungkas extends Boss {
    private static final Random RNG = new Random();
    private static final String[] WILD_SKILLS = {
        "NULL_FIELD", "SOVEREIGN_STRIKE", "DATA_FRAGMENTATION",
        "NULL_PROTOCOL", "SHOCKWAVE", "VOID_RUPTURE"
    };

    public SemarPamungkas() {
        super("Semar Pamungkas",
              "Sang Hyang Ismaya — lucu di luar, tapi kekuatannya melampaui para dewa.",
              EnemyRace.DIVINE, ThreatLevel.BOSS,
              "LOOT_BOSS_MYTHIC", 5, new double[]{0.80, 0.60, 0.40, 0.15});
        initStats(); expReward = 3000; goldReward = 1000; initVitals();
    }
    @Override protected void initStats() {
        stats.setBase(StatType.MAX_HP, 3000);  stats.setBase(StatType.MAX_MP, 500);
        stats.setBase(StatType.MAX_SHIELD, 500);
        stats.setBase(StatType.PHYSICAL_ATK, 80); stats.setBase(StatType.CYBER_ATK, 80);
        stats.setBase(StatType.ENERGY_ATK, 80);
        stats.setBase(StatType.PHYSICAL_DEF, 40); stats.setBase(StatType.CYBER_DEF, 40);
        stats.setBase(StatType.ENERGY_DEF, 40);   stats.setBase(StatType.SPEED, 15);
        stats.setBase(StatType.SKILL_POWER, 0.50); stats.setBase(StatType.DAMAGE_MULT, 0.30);
        stats.setBase(StatType.HP_REGEN, 8);       stats.setBase(StatType.SHIELD_REGEN, 5);
    }
    @Override protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        String skill = WILD_SKILLS[RNG.nextInt(WILD_SKILLS.length)];
        boolean isTargeted = skill.equals("SOVEREIGN_STRIKE") || skill.equals("NULL_FIELD");
        if (isTargeted) {
            Entity t = getHighestAtkTarget(enemies);
            return t == null ? CombatAction.pass() : CombatAction.useSkill(skill, List.of(t.getId()));
        }
        List<String> all = enemies.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill(skill, all);
    }
    @Override protected CombatAction specialAction(List<Entity> a, List<Entity> e) {
        return normalAction(a, e);
    }
    @Override protected CombatAction desperateAction(List<Entity> a, List<Entity> e) {
        List<String> all = e.stream().filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass() : CombatAction.useSkill("NULL_PROTOCOL", all);
    }
    @Override protected void onPhaseTransition(int from, int to) {}
}
