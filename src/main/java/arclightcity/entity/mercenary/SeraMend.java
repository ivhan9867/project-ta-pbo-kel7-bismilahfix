package arclightcity.entity.mercenary;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * SERA MEND — Field Medic
 * Role: SUPPORT
 *
 * Lore: Dokter jalanan yang dulunya bekerja di klinik bawah tanah gratis
 * sebelum MegaCorp menghancurkannya. Dia menyembuhkan siapapun yang butuh,
 * tapi musuh yang menyakiti pasiennya akan menyesal.
 *
 * Playstyle:
 *  - Satu-satunya merc dengan full heal kit
 *  - Bisa cleanse status effect negatif dari sekutu
 *  - Bisa apply Regen + Barrier sekaligus
 *  - TRIAGE: prioritas heal otomatis ke yang HP paling rendah
 *  - Offense lemah tapi punya IRRADIATE sebagai counter
 *  - Lemah: HP rendah, tidak bisa melarikan diri jika jadi target
 *
 * Synergy: Lyra Bloom (Neon Shaman) → heal power +25% berdua
 */
public class SeraMend extends Mercenary {

    private int healsThisBattle = 0;

    public SeraMend() {
        super("Sera Mend",
              "Orang yang paling ingin kamu lihat saat darah membasahi lantai dungeon. " +
              "Tangannya tidak pernah gemetar, bahkan di tengah chaos.",
              MercenaryType.SERA_MEND,
              Role.SUPPORT,
              700);

        initStats();
        healThreshold  = 0.50; // mulai heal lebih awal dari merc lain
        aggressiveness = 0.20; // jarang menyerang
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       196);
        stats.setBase(StatType.MAX_SHIELD,   30);
        stats.setBase(StatType.DAMAGE_MULT,  0.0);
        stats.setBase(StatType.MAX_MP,       120); // MP tinggi untuk banyak heal
        stats.setBase(StatType.MP_REGEN,     12);
        stats.setBase(StatType.HP_REGEN,     5);
        stats.setBase(StatType.PHYSICAL_ATK, 16);
        stats.setBase(StatType.CYBER_ATK,    12.8);
        stats.setBase(StatType.ENERGY_ATK,   32); // offense via energy
        stats.setBase(StatType.PHYSICAL_DEF, 15.6);
        stats.setBase(StatType.CYBER_DEF,    19.5);
        stats.setBase(StatType.ENERGY_DEF,   23.4);
        stats.setBase(StatType.SKILL_POWER,  1.30); // skill power tinggi (heal lebih besar)
        stats.setBase(StatType.SPEED,        13);
        stats.setBase(StatType.SYNC_RATE,    0.20); // buff ke player lebih efektif
    }

    @Override
    protected boolean shouldHeal() { return true; } // selalu prioritas heal

    @Override
    protected CombatAction healAction(Entity target, List<Entity> allies, List<Entity> enemies) {
        // Cleanse debuff dulu jika ada
        boolean hasBadEffect = target.getActiveEffects().stream()
                .anyMatch(e -> e.getType().isNegative());

        if (hasBadEffect) {
            // CLEANSE: hapus semua DOT dan debuff
            for (StatusEffectType bad : List.of(
                    StatusEffectType.BURN, StatusEffectType.BLEED, StatusEffectType.VIRUS,
                    StatusEffectType.IRRADIATE, StatusEffectType.CORRODE,
                    StatusEffectType.WEAKEN, StatusEffectType.SLOW, StatusEffectType.SHRED)) {
                target.removeEffect(bad);
            }
            return CombatAction.useSkill("CLEANSE_PROTOCOL", List.of(target.getId()));
        }

        // TRIAGE HEAL: heal + Regen
        target.applyEffect(new StatusEffect(StatusEffectType.REGEN, 3, 15.0, this.id));
        healsThisBattle++;
        return CombatAction.useSkill("TRIAGE_HEAL", List.of(target.getId()));
    }

    @Override
    protected CombatAction selfHealAction(List<Entity> allies, List<Entity> enemies) {
        this.applyEffect(new StatusEffect(StatusEffectType.REGEN, 2, 20.0, this.id));
        return CombatAction.useSkill("SELF_MEND", List.of(this.id));
    }

    @Override
    protected boolean canSelfHeal() { return currentMp >= 20; }

    @Override
    protected CombatAction combatAction(List<Entity> allies, List<Entity> enemies) {
        // Sera tidak suka menyerang, tapi punya Irradiate sebagai deterrent
        Entity target = getRandomEnemy(enemies);
        if (target == null) return CombatAction.pass();

        // Jika ada ally yang butuh barrier, pasang dulu
        Entity fragileAlly = allies.stream()
                .filter(a -> a.isAlive() && a.getHpPercent() < 0.60 && !a.hasEffect(StatusEffectType.BARRIER))
                .findFirst().orElse(null);

        if (fragileAlly != null) {
            double barrierPower = 30 + stats.get(StatType.SKILL_POWER) * 10;
            fragileAlly.applyEffect(new StatusEffect(StatusEffectType.BARRIER, 2, barrierPower, this.id));
            return CombatAction.useSkill("FIELD_BARRIER", List.of(fragileAlly.getId()));
        }

        // Fallback: Irradiate ke musuh terdekat
        target.applyEffect(new StatusEffect(StatusEffectType.IRRADIATE, 2, 10.0, this.id));
        return CombatAction.useSkill("BIO_IRRADIATE", List.of(target.getId()));
    }

    @Override
    protected void applySynergyWith(Mercenary other) {
        // Synergy dengan Lyra Bloom: heal power +25%
        if (other.getMercenaryType() == MercenaryType.LYRA_BLOOM) {
            stats.addBase(StatType.SKILL_POWER, 0.25);
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
        stats.addBase(StatType.MAX_MP,    15);
        stats.addBase(StatType.MP_REGEN,   3);
        stats.addBase(StatType.SKILL_POWER, 0.05);
        if (newLevel == 10) {
            // Soul Sync: Triage Heal juga pasang Barrier + Regen sekaligus
            stats.addBase(StatType.SKILL_POWER, 0.30);
            stats.addBase(StatType.SYNC_RATE,   0.20);
        }
    }

    public int getHealsThisBattle() { return healsThisBattle; }
    @Override
    public CombatAction decideAction(java.util.List<Entity> allies, java.util.List<Entity> enemies) {
        Entity target = highestHpEnemy(enemies);
        Entity critical = lowestHpAlly(allies);

        // 1. HEAL KRITIS: jika ada ally HP < 30%
        if (critical != null && critical.getHpPercent() < 0.30 && hasMP(18)) {
            return skill("TRIAGE_HEAL", critical);
        }

        // 2. BUFF: REGEN + BARRIER ke team jika belum ada REGEN
        if (!teamHasBuff(allies, arclightcity.entity.status.StatusEffectType.REGEN)
                && hasMP(16)) {
            return skillAll("REGEN_TEAM", allies);
        }

        // 3. BARRIER ke ally yang HP paling rendah
        if (critical != null && !critical.hasEffect(arclightcity.entity.status.StatusEffectType.BARRIER)
                && hasMP(14)) {
            return skill("BARRIER_SHIELD", critical);
        }

        // 4. CC: SLOW musuh
        if (target != null && !target.hasEffect(arclightcity.entity.status.StatusEffectType.SLOW)
                && hasMP(10)) {
            return skill("SLOW_CURSE", target);
        }

        // 5. Heal ally paling lemah jika masih ada MP
        if (critical != null && critical.getHpPercent() < 0.60 && hasMP(12)) {
            return skill("TRIAGE_HEAL", critical);
        }
        return CombatAction.defend(); // Support tidak perlu basic attack
    }

}
