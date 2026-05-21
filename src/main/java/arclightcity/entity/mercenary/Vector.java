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
        stats.setBase(StatType.MAX_HP,       1800);
        stats.setBase(StatType.MAX_SHIELD,   0);   // glass cannon
        stats.setBase(StatType.DAMAGE_MULT,  0.15); // sangat rendah
        stats.setBase(StatType.MAX_MP,       90);
        stats.setBase(StatType.MP_REGEN,     8);
        stats.setBase(StatType.PHYSICAL_ATK, 90);
        stats.setBase(StatType.CYBER_ATK, 30); // cyber attack terkuat
        stats.setBase(StatType.PHYSICAL_DEF, 7.8);
        stats.setBase(StatType.CYBER_DEF,    18.2);
        stats.setBase(StatType.ENERGY_DEF,   10.4);
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
        // Universal stat buff per level (+10% HP, +12% ATK, +8% DEF)
        double hp  = getStats().get(arclightcity.entity.stats.StatType.MAX_HP) * 0.10;
        double atk = getStats().get(arclightcity.entity.stats.StatType.PHYSICAL_ATK) * 0.12;
        double def = getStats().get(arclightcity.entity.stats.StatType.PHYSICAL_DEF) * 0.08;
        getStats().addBase(arclightcity.entity.stats.StatType.MAX_HP, hp);
        getStats().addBase(arclightcity.entity.stats.StatType.PHYSICAL_ATK, atk);
        getStats().addBase(arclightcity.entity.stats.StatType.CYBER_ATK, atk * 0.6);
        getStats().addBase(arclightcity.entity.stats.StatType.ENERGY_ATK, atk * 0.6);
        getStats().addBase(arclightcity.entity.stats.StatType.PHYSICAL_DEF, def);
        restoreVitals(getStats().get(arclightcity.entity.stats.StatType.MAX_HP), getStats().get(arclightcity.entity.stats.StatType.MAX_SHIELD), getStats().get(arclightcity.entity.stats.StatType.MAX_MP)); // Refill HP/MP setelah level up
        System.out.println("[Merc] " + getName() + " level up! LV." + newLevel);
        stats.addBase(StatType.CYBER_ATK,   3);
        stats.addBase(StatType.CRIT_CHANCE, 0.02);
        if (newLevel == 10) {
            // Soul Sync: EXECUTE threshold naik ke 35%, HACK cooldown 2 turn
            stats.addBase(StatType.ARMOR_PIERCE, 0.20);
            stats.addBase(StatType.CRIT_DAMAGE,  0.40);
        }
    }

    public boolean isDetected() { return detected; }
    @Override
    public CombatAction decideAction(java.util.List<Entity> allies, java.util.List<Entity> enemies) {
        Entity target = highestHpEnemy(enemies);
        if (target == null) return CombatAction.defend();
        Entity lowest = enemies.stream().filter(Entity::isAlive)
            .min(java.util.Comparator.comparingDouble(Entity::getHpPercent)).orElse(target);

        // 1. BUFF: STEALTH (evasion) ke diri sendiri jika belum aktif
        if (!selfHasEffect(arclightcity.entity.status.StatusEffectType.STEALTH) && hasMP(14)) {
            return skill("STEALTH_STEP", this);
        }

        // 2. EXECUTE jika ada musuh HP < 30%
        if (lowest.getHpPercent() < 0.30 && hasMP(20)) {
            return skill("EXECUTE", lowest);
        }

        // 3. CC: BLEED musuh yang belum berdarah
        Entity unbled = enemyWithout(enemies, arclightcity.entity.status.StatusEffectType.BLEED);
        if (unbled != null && hasMP(12)) {
            return skill("BLEED_SLASH", unbled);
        }

        // 4. SHADOW_STEP attack
        if (hasMP(18)) return skill("SHADOW_STEP", target);
        return attack(target);
    }

}
