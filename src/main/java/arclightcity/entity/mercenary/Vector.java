package arclightcity.entity.mercenary;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.stats.DamageType;
import arclightcity.entity.stats.StatType;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import java.util.List;



/**
 * VECTOR — Cyber Assassin
 * Role: DPS / SPECIALIST
 *
 * Lore: Identitasnya adalah misteri. Bahkan namanya adalah alias.
 * Yang diketahui: dia dulu bekerja untuk tiga korporasi berbeda secara bersamaan,
 * dan ketiganya sekarang sudah tidak ada. Coincidence? Tidak mungkin.
 *
 * Playstyle:
 *  - Spesialis single-target burst damage
 *  - HACK: ambil kendali musuh untuk menyerang rekannya sendiri
 *  - EXECUTE: jika target HP < 20%, instant kill dengan satu serangan
 *  - SHADOW STEP: teleport ke belakang target, serang dari blind spot (ignore evasion)
 *  - Lemah: sangat lemah jika sudah dideteksi (no stealth → damage turun 40%)
 *
 * Synergy: Kira Voss → +15% Crit Damage berdua
 */
public class Vector extends Mercenary {

    private boolean detected        = false; // jika terkena CC/damage → detected
    private int     hackedLastTurn  = -99;
    private int     currentTurn     = 0;

    public Vector() {
        super("Vector",
              "Dia ada di sana sebelum kamu masuk, dan sudah pergi sebelum kamu sadar apa yang terjadi. " +
              "Tanda-tandanya: satu musuh mati, dan musuh lainnya saling menusuk.",
              MercenaryType.VECTOR,
              Role.DPS,
              1200); // mahal karena sangat powerful

        initStats();
        aggressiveness = 1.0;
        healThreshold  = 0.15;
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       100);
        stats.setBase(StatType.MAX_SHIELD,   0);   // glass cannon
        stats.setBase(StatType.DAMAGE_MULT,  0.15); // sangat rendah
        stats.setBase(StatType.MAX_MP,       90);
        stats.setBase(StatType.MP_REGEN,     8);
        stats.setBase(StatType.PHYSICAL_ATK, 28);
        stats.setBase(StatType.CYBER_ATK,    38); // cyber attack terkuat
        stats.setBase(StatType.PHYSICAL_DEF, 6);
        stats.setBase(StatType.CYBER_DEF,    14);
        stats.setBase(StatType.ENERGY_DEF,   8);
        stats.setBase(StatType.CRIT_CHANCE,  0.25);
        stats.setBase(StatType.CRIT_DAMAGE,  2.00);
        stats.setBase(StatType.ARMOR_PIERCE, 0.30); // armor pierce tinggi
        stats.setBase(StatType.EVASION,      0.22);
        stats.setBase(StatType.SPEED,        19);  // tercepat di semua merc
        stats.setBase(StatType.ACCURACY,     0.96);
        stats.setBase(StatType.COOLDOWN_REDUCE, 0.15);
    }

    @Override
    public void onTurnStart() {
        super.onTurnStart();
        currentTurn++;
        detected = false; // reset detection tiap turn
        // Masuk stealth di awal turn jika tidak ada CC
        if (canAct()) {
            this.applyEffect(new StatusEffect(StatusEffectType.STEALTH, 1, 0, this.id));
        }
    }

    @Override
    public Entity.DamageResult receiveDamage(double rawDamage,
                                             DamageType type,
                                             boolean ignoreArmor) {
        detected = true; // kena damage = deteksi
        this.removeEffect(StatusEffectType.STEALTH);
        return super.receiveDamage(rawDamage, type, ignoreArmor);
    }

    @Override
    protected CombatAction combatAction(List<Entity> allies, List<Entity> enemies) {
        // EXECUTE: jika ada musuh HP < 20%, prioritas execute
        Entity executeTarget = enemies.stream()
                .filter(e -> e.isAlive() && e.getHpPercent() < 0.20)
                .findFirst().orElse(null);

        if (executeTarget != null) {
            this.applyEffect(new StatusEffect(StatusEffectType.EMPOWERED, 1, 0, this.id));
            return CombatAction.useSkill("EXECUTE", List.of(executeTarget.getId()));
        }

        // HACK: ambil kendali musuh terkuat (jika cooldown ok)
        if (currentTurn - hackedLastTurn >= 4) {
            Entity strongestEnemy = enemies.stream()
                    .filter(Entity::isAlive)
                    .max(java.util.Comparator.comparingDouble(e -> e.getStats().get(StatType.PHYSICAL_ATK)))
                    .orElse(null);

            if (strongestEnemy != null && !strongestEnemy.hasEffect(StatusEffectType.HACK)) {
                strongestEnemy.applyEffect(new StatusEffect(StatusEffectType.HACK, 1, 0, this.id));
                hackedLastTurn = currentTurn;
                return CombatAction.useSkill("DEEP_HACK", List.of(strongestEnemy.getId()));
            }
        }

        // SHADOW STEP: serangan dari blind spot (ignore evasion)
        Entity target = getLowestHpEnemy(enemies);
        if (target == null) return CombatAction.pass();

        if (this.isInStealth()) {
            // Dari stealth: guaranteed hit + expose + bonus damage
            this.removeEffect(StatusEffectType.STEALTH);
            target.applyEffect(new StatusEffect(StatusEffectType.EXPOSE, 2, 0, this.id));
            return CombatAction.useSkill("SHADOW_STEP", List.of(target.getId()));
        }

        // Normal attack
        return CombatAction.basicAttack(List.of(target.getId()));
    }

    @Override
    protected void applySynergyWith(Mercenary other) {
        if (other.getMercenaryType() == MercenaryType.KIRA_VOSS) {
            stats.addBase(StatType.CRIT_DAMAGE, 0.15);
        }
        // Synergy dengan Echo Null: Hack duration +1 turn
        if (other.getMercenaryType() == MercenaryType.ECHO_NULL) {
            stats.addBase(StatType.COOLDOWN_REDUCE, 0.10);
        }
    }

    @Override
    protected void onLoyaltyLevelUp(int newLevel) {
        stats.addBase(StatType.CYBER_ATK,   3);
        stats.addBase(StatType.CRIT_CHANCE, 0.02);
        if (newLevel == 10) {
            // Soul Sync: EXECUTE threshold naik ke 35%, HACK cooldown 2 turn
            stats.addBase(StatType.ARMOR_PIERCE, 0.20);
            stats.addBase(StatType.CRIT_DAMAGE,  0.40);
        }
    }

    public boolean isDetected() { return detected; }
}
