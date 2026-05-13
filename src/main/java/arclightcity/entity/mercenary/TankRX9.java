package arclightcity.entity.mercenary;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.stats.DamageType;
import arclightcity.entity.stats.StatType;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import java.util.List;



/**
 * TANK-RX9 — Combat Android
 * Role: TANK
 *
 * Lore: Unit militer generasi ke-9 yang dipensiunkan paksa karena dianggap
 * "terlalu mandiri". Dia sendiri tidak setuju dengan keputusan itu.
 * Sekarang menawarkan layanan proteksi ke siapapun yang menghargai
 * kesetiaan lebih dari kepatuhan buta.
 *
 * Playstyle:
 *  - HP dan DEF tertinggi di semua merc
 *  - TAUNT: memaksa semua musuh menyerang RX9
 *  - BARRIER: pasang shield ke sekutu yang HP kritis
 *  - COUNTER: tiap kena serangan fisik, ada chance counter attack
 *  - Lemah: lambat, Cyber damage, HACK (android = rentan)
 *
 * Synergy: Magnus Forge (Heavy Gunner) → berbagi 10% DEF ke Magnus
 */
public class TankRX9 extends Mercenary {

    private int    counterCount   = 0;
    private double damageAbsorbed = 0; // tracking untuk passive counter

    public TankRX9() {
        super("Tank-RX9",
              "Unit combat android yang didesain untuk menyerap damage. " +
              "Motto internalnya: 'Mereka tidak akan menyentuh yang lain selama aku berdiri.'",
              MercenaryType.TANK_RX9,
              Role.TANK,
              600);

        initStats();
        healThreshold  = 0.35;
        aggressiveness = 0.50;
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       448);
        stats.setBase(StatType.MAX_MP,       40);
        stats.setBase(StatType.MAX_SHIELD,   150); // Tank punya shield paling besar
        stats.setBase(StatType.SHIELD_REGEN, 12);  // regen shield kuat
        stats.setBase(StatType.HP_REGEN,     8);
        stats.setBase(StatType.PHYSICAL_ATK, 55);
        stats.setBase(StatType.CYBER_ATK, 15);
        stats.setBase(StatType.DAMAGE_MULT,  0.0);
        stats.setBase(StatType.PHYSICAL_DEF, 58.5);
        stats.setBase(StatType.CYBER_DEF,    6.5);
        stats.setBase(StatType.ENERGY_DEF,   32.5);
        stats.setBase(StatType.BLOCK_CHANCE, 0.25);
        stats.setBase(StatType.TENACITY,     0.50);
        stats.setBase(StatType.SPEED,        6);
        stats.setBase(StatType.ACCURACY,     0.82);
        stats.setBase(StatType.COMMAND_AURA, 15);
    }

    @Override
    protected CombatAction combatAction(List<Entity> allies, List<Entity> enemies) {
        // Prioritas 1: Pasang Taunt jika tidak aktif
        if (!this.hasEffect(StatusEffectType.TAUNT)) {
            this.applyEffect(new StatusEffect(StatusEffectType.TAUNT, 2, 0, this.id));
            this.applyEffect(new StatusEffect(StatusEffectType.FORTIFY, 2, 0, this.id));
        }

        // Prioritas 2: Pasang Barrier ke sekutu HP kritis
        Entity critAlly = findCriticalAlly(allies);
        if (critAlly != null && critAlly != this) {
            double barrierPower = stats.get(StatType.PHYSICAL_DEF) * 0.5;
            critAlly.applyEffect(new StatusEffect(StatusEffectType.BARRIER, 2, barrierPower, this.id));
            return CombatAction.useSkill("IRON_SHIELD", List.of(critAlly.getId()));
        }

        // Prioritas 3: Serang target yang menyerang sekutu (aggro control)
        Entity target = getRandomEnemy(enemies);
        if (target == null) return CombatAction.pass();
        return CombatAction.basicAttack(List.of(target.getId()));
    }

    @Override
    public Entity.DamageResult receiveDamage(double rawDamage,
                                             DamageType type,
                                             boolean ignoreArmor) {
        Entity.DamageResult result = super.receiveDamage(rawDamage, type, ignoreArmor);
        damageAbsorbed += result.damage;

        // COUNTER PROTOCOL: tiap 50 damage yang diterima → counter attack ready
        if (damageAbsorbed >= 50) {
            damageAbsorbed -= 50;
            counterCount++;
            this.applyEffect(new StatusEffect(StatusEffectType.EMPOWERED, 1, 0, this.id));
        }

        return result;
    }

    @Override
    protected boolean shouldHeal()  { return true; }
    @Override
    protected boolean canSelfHeal() { return true; }

    @Override
    protected CombatAction selfHealAction(List<Entity> allies, List<Entity> enemies) {
        // Emergency Repair: pulihkan 30% max HP
        return CombatAction.useSkill("EMERGENCY_REPAIR", List.of(this.id));
    }

    @Override
    protected void applySynergyWith(Mercenary other) {
        // Synergy dengan Magnus: share 10% DEF ke Magnus
        if (other.getMercenaryType() == MercenaryType.MAGNUS_FORGE) {
            double sharedDef = stats.get(StatType.PHYSICAL_DEF) * 0.10;
            other.getStats().addBase(StatType.PHYSICAL_DEF, sharedDef);
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
        stats.addBase(StatType.MAX_HP,       20);
        stats.addBase(StatType.PHYSICAL_DEF, 3);
        if (newLevel == 10) {
            // Soul Sync: Counter Attack damage meningkat drastis + jadi AOE
            stats.addBase(StatType.PHYSICAL_ATK, 25);
            stats.addBase(StatType.BLOCK_CHANCE,  0.15);
        }
    }

    public int getCounterCount() { return counterCount; }
    @Override
    public CombatAction decideAction(java.util.List<Entity> allies, java.util.List<Entity> enemies) {
        Entity target = highestHpEnemy(enemies);
        if (target == null) return CombatAction.defend();

        // 1. BUFF PRIORITY: FORTIFY ke team jika belum aktif
        if (!teamHasBuff(allies, arclightcity.entity.status.StatusEffectType.FORTIFY)
                && hasMP(15)) {
            return skillAll("FORTIFY_TEAM", allies);
        }

        // 2. TAUNT jika banyak musuh dan musuh tidak sedang fokus ke kita
        if (enemies.stream().filter(Entity::isAlive).count() >= 2
                && !selfHasEffect(arclightcity.entity.status.StatusEffectType.TAUNT)
                && hasMP(12)) {
            return skillAll("TAUNT", enemies);
        }

        // 3. Shield diri sendiri jika HP rendah
        if (getHpPercent() < 0.45 && hasMP(10)) {
            return skill("IRON_SHIELD", this);
        }

        // 4. Basic attack
        return attack(target);
    }

}
