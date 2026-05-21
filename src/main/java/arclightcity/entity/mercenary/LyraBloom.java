package arclightcity.entity.mercenary;
import arclightcity.combat.CombatManager;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * LYRA BLOOM — Neon Shaman
 * Role: SUPPORT / SPECIALIST
 *
 * Lore: Salah satu dari sedikit manusia yang tubuhnya menerima energi neon
 * alih-alih menolaknya. Energi yang membunuh orang lain justru menghidupkannya.
 * Dia menggunakannya untuk menyembuhkan, memberdayakan, dan kadang —
 * ketika terpaksa — untuk menghancurkan.
 *
 * Playstyle:
 *  - Heal berbasis Energy (bukan MP seperti Sera)
 *  - NEON BLOOM: AoE heal + Regen ke semua sekutu
 *  - RESONANCE: buff seluruh party dengan Empowered selama 2 turn
 *  - ENERGY DRAIN: serap energy musuh untuk heal diri sendiri
 *  - SOUL LINK: ketika sekutu mati, absorb sebagian HP-nya (controversial tapi powerful)
 *  - Lemah: Physical attack mentah, dan jika IRRADIATE kena dirinya (backfire)
 *
 * Synergy: Sera Mend → heal power +25% berdua (lihat SeraMend)
 *          Tank-RX9 → Lyra's Neon Bloom juga isi ulang sebagian HP RX9 tiap turn
 */
public class LyraBloom extends Mercenary {

    private int bloomCooldown     = 0;
    private int resonanceCooldown = 0;
    private int currentTurn       = 0;
    private double energyReserve  = 0; // resource khusus Lyra

    public LyraBloom() {
        super("Lyra Bloom",
              "Kulitnya berpendar lemah dengan cahaya neon biru-ungu. " +
              "Tangannya hangat saat menyentuh luka, dan luka itu menutup sendiri. " +
              "Dia tidak pernah menjelaskan caranya. Hasilnya cukup bicara.",
              MercenaryType.LYRA_BLOOM,
              Role.SUPPORT,
              950);

        initStats();
        healThreshold  = 0.55; // threshold heal paling tinggi — proaktif
        aggressiveness = 0.25;
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       2500);
        stats.setBase(StatType.MAX_SHIELD,   35);
        stats.setBase(StatType.DAMAGE_MULT,  0.08);
        stats.setBase(StatType.MAX_MP,       100);
        stats.setBase(StatType.MP_REGEN,     8);
        stats.setBase(StatType.HP_REGEN,     8);   // regen sendiri tinggi
        stats.setBase(StatType.PHYSICAL_ATK, 35);
        stats.setBase(StatType.CYBER_ATK,    16);
        stats.setBase(StatType.ENERGY_ATK, 80);  // offense via energy
        stats.setBase(StatType.PHYSICAL_DEF, 13);
        stats.setBase(StatType.CYBER_DEF,    15.6);
        stats.setBase(StatType.ENERGY_DEF,   39);  // sangat resist energy
        stats.setBase(StatType.LIFESTEAL,    0.15); // tiap hit pulihkan HP
        stats.setBase(StatType.SKILL_POWER,  1.40); // skill power tertinggi
        stats.setBase(StatType.SPEED,        14);
        stats.setBase(StatType.SYNC_RATE,    0.30); // buff ke player sangat efektif
    }

    @Override
    public void onTurnStart() {
        super.onTurnStart();
        currentTurn++;
        if (bloomCooldown > 0)     bloomCooldown--;
        if (resonanceCooldown > 0) resonanceCooldown--;

        // Tiap turn: akumulasi energy reserve dari lingkungan
        energyReserve += 10 + stats.get(StatType.ENERGY_ATK) * 0.1;
    }

    @Override
    protected boolean shouldHeal() { return true; }

    @Override
    protected CombatAction healAction(Entity target, List<Entity> allies, List<Entity> enemies) {
        List<Entity> aliveAllies = allies.stream().filter(Entity::isAlive).toList();

        // NEON BLOOM: AoE heal semua sekutu jika bloom ready
        if (bloomCooldown == 0 && aliveAllies.size() >= 2) {
            bloomCooldown = 3;
            double healPower = 25 + energyReserve * 0.3;
            energyReserve   = 0;

            aliveAllies.forEach(a -> {
                a.receiveHeal(healPower);
                a.applyEffect(new StatusEffect(StatusEffectType.REGEN, 2, 12.0, this.id));
            });

            List<String> allyIds = aliveAllies.stream().map(Entity::getId).toList();
            return CombatAction.useSkill("NEON_BLOOM", allyIds);
        }

        // Single target heal + Regen
        target.applyEffect(new StatusEffect(StatusEffectType.REGEN, 3, 18.0, this.id));
        return CombatAction.useSkill("BLOOM_MEND", List.of(target.getId()));
    }

    @Override
    protected CombatAction combatAction(List<Entity> allies, List<Entity> enemies) {
        List<Entity> aliveAllies = allies.stream().filter(Entity::isAlive).toList();

        // RESONANCE: buff seluruh party jika cooldown ok dan party butuh boost
        if (resonanceCooldown == 0 && aliveAllies.size() >= 2) {
            resonanceCooldown = 5;
            aliveAllies.forEach(a -> {
                a.applyEffect(new StatusEffect(StatusEffectType.EMPOWERED, 2, 0, this.id));
                a.applyEffect(new StatusEffect(StatusEffectType.FOCUS,     2, 0, this.id));
            });
            List<String> allyIds = aliveAllies.stream().map(Entity::getId).toList();
            return CombatAction.useSkill("RESONANCE", allyIds);
        }

        // ENERGY DRAIN: serap energy musuh → heal diri
        Entity target = getLowestHpEnemy(enemies);
        if (target == null) return CombatAction.pass();

        double drainHeal = stats.get(StatType.ENERGY_ATK) * 0.5;
        this.receiveHeal(drainHeal);
        target.applyEffect(new StatusEffect(StatusEffectType.IRRADIATE, 2, 12.0, this.id));

        return CombatAction.useSkill("ENERGY_DRAIN", List.of(target.getId()));
    }

    @Override
    protected void onDeath() {
        // SOUL LINK: saat mati, transfer sebagian remaining HP ke sekutu terdekat
        // (dihandle oleh CombatManager yang listen event onDeath)
        super.onDeath();
    }

    @Override
    protected void applySynergyWith(Mercenary other) {
        if (other.getMercenaryType() == MercenaryType.SERA_MEND) {
            stats.addBase(StatType.SKILL_POWER, 0.25);
        }
        if (other.getMercenaryType() == MercenaryType.TANK_RX9) {
            // Lyra's bloom juga trigger HP regen untuk RX9
            other.getStats().addBase(StatType.HP_REGEN, 5);
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
        stats.addBase(StatType.SKILL_POWER, 0.05);
        stats.addBase(StatType.ENERGY_ATK,  3);
        if (newLevel == 10) {
            // Soul Sync: NEON BLOOM tidak punya cooldown lagi, tapi butuh energy reserve penuh
            stats.addBase(StatType.SKILL_POWER, 0.40);
            stats.addBase(StatType.SYNC_RATE,   0.20);
            stats.addBase(StatType.LIFESTEAL,   0.10);
        }
    }

    public double getEnergyReserve() { return energyReserve; }
    @Override
    public CombatAction decideAction(java.util.List<Entity> allies, java.util.List<Entity> enemies) {
        Entity target = highestHpEnemy(enemies);
        Entity critical = lowestHpAlly(allies);

        // 1. HEAL KRITIS: siapapun yang HP < 25%
        if (critical != null && critical.getHpPercent() < 0.25 && hasMP(20)) {
            return skill("BLOOM_MEND", critical);
        }

        // 2. BUFF: REGEN ke semua jika belum aktif
        if (!teamHasBuff(allies, arclightcity.entity.status.StatusEffectType.REGEN)
                && hasMP(18)) {
            return skillAll("NEON_BLOOM", allies);
        }

        // 3. BARRIER ke ally paling kritis yang belum dapat barrier
        if (critical != null && !critical.hasEffect(arclightcity.entity.status.StatusEffectType.BARRIER)
                && hasMP(14)) {
            return skill("BLOOM_BARRIER", critical);
        }

        // 4. CC: WEAKEN musuh
        if (target != null && !target.hasEffect(arclightcity.entity.status.StatusEffectType.WEAKEN)
                && hasMP(12)) {
            return skill("WEAKEN_AURA", target);
        }

        // 5. Heal ally 50%-60% HP
        if (critical != null && critical.getHpPercent() < 0.60 && hasMP(16)) {
            return skillAll("NEON_BLOOM", allies);
        }
        return CombatAction.defend();
    }

}
