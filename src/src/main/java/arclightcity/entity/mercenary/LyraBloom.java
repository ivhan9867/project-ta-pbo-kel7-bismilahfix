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
        stats.setBase(StatType.MAX_HP,       160);
        stats.setBase(StatType.MAX_SHIELD,   35);
        stats.setBase(StatType.DAMAGE_MULT,  0.08);
        stats.setBase(StatType.MAX_MP,       100);
        stats.setBase(StatType.MP_REGEN,     8);
        stats.setBase(StatType.HP_REGEN,     8);   // regen sendiri tinggi
        stats.setBase(StatType.PHYSICAL_ATK, 8);
        stats.setBase(StatType.CYBER_ATK,    10);
        stats.setBase(StatType.ENERGY_ATK,   35);  // offense via energy
        stats.setBase(StatType.PHYSICAL_DEF, 10);
        stats.setBase(StatType.CYBER_DEF,    12);
        stats.setBase(StatType.ENERGY_DEF,   30);  // sangat resist energy
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
}
