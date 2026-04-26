package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * GLITCH DRONE — Android kecil terbang buatan MegaCorp, terinfeksi malware.
 * Spesialis Cyber attack: Virus, Hack, Drain MP.
 * Datang berpasangan atau kelompok kecil.
 *
 * Floor: 5-15
 * Strategi: Drain MP player agar tidak bisa pakai skill,
 *           kemudian Virus untuk DOT cyber tiap turn.
 * Lemah: Physical attack, EMP-type skills.
 */
public class GlitchDrone extends Enemy {

    private boolean hasSelfDestructed = false;

    public GlitchDrone() {
        super("Glitch Drone",
              "Drone surveillance MegaCorp yang sistemnya korup. " +
              "Matanya berkedip merah dan bergerak tidak menentu seperti glitch.",
              EntityType.ENEMY,
              EnemyRace.ANDROID,
              ThreatLevel.MINION,
              "LOOT_ANDROID_LOW");

        initStats();
        expReward  = 40;
        goldReward = 25; // android → lebih banyak scrap/gold
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       50);
        stats.setBase(StatType.MAX_SHIELD,   20);  // drone punya energy shield
        stats.setBase(StatType.DAMAGE_MULT,  0.08);
        stats.setBase(StatType.MAX_MP,       60); // MP tinggi untuk banyak skill cyber
        stats.setBase(StatType.PHYSICAL_ATK, 8);
        stats.setBase(StatType.CYBER_ATK,    22); // spesialis cyber
        stats.setBase(StatType.PHYSICAL_DEF, 3);  // mudah dihancurkan secara fisik
        stats.setBase(StatType.CYBER_DEF,    18); // resist cyber (sesama android)
        stats.setBase(StatType.ENERGY_DEF,   5);
        stats.setBase(StatType.SPEED,        16);
        stats.setBase(StatType.EVASION,      0.12); // drone kecil, susah dipukul
        stats.setBase(StatType.ACCURACY,     0.93);
    }

    @Override
    protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        // Prioritas: target dengan MP tertinggi → drain
        Entity mpTarget = enemies.stream()
                .filter(Entity::isAlive)
                .max(java.util.Comparator.comparingDouble(Entity::getCurrentMp))
                .orElse(null);

        if (mpTarget != null) {
            mpTarget.applyEffect(new StatusEffect(StatusEffectType.DRAIN, 2, 8.0, this.id));
            return CombatAction.basicAttack(List.of(mpTarget.getId()));
        }
        return CombatAction.pass();
    }

    @Override
    protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        // Virus Upload: infeksi Virus ke target + chance Hack
        Entity target = getRandomTarget(enemies);
        if (target == null) return CombatAction.pass();

        target.applyEffect(new StatusEffect(StatusEffectType.VIRUS, 3, 12.0, this.id));
        if (Math.random() < 0.25) {
            target.applyEffect(new StatusEffect(StatusEffectType.HACK, 1, 0, this.id));
        }

        return CombatAction.useSkill("VIRUS_UPLOAD", List.of(target.getId()));
    }

    @Override
    protected CombatAction desperateAction(List<Entity> allies, List<Entity> enemies) {
        if (!hasSelfDestructed) {
            hasSelfDestructed = true;
            // Self Destruct: damage AoE ke semua musuh, drone mati
            List<String> allTargetIds = enemies.stream()
                    .filter(Entity::isAlive)
                    .map(Entity::getId)
                    .toList();
            // Bunuh diri sendiri
            this.currentHp = 0;
            this.alive = false;
            return CombatAction.useSkill("SELF_DESTRUCT", allTargetIds);
        }
        return CombatAction.pass();
    }

    @Override
    protected boolean shouldUseDesperateAction() { return !hasSelfDestructed && Math.random() < 0.45; }
    @Override
    protected int getSpecialCooldown()           { return 2; }
}
