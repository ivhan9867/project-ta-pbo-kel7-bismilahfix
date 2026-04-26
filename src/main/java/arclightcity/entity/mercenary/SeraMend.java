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
        stats.setBase(StatType.MAX_HP,       140);
        stats.setBase(StatType.MAX_SHIELD,   30);
        stats.setBase(StatType.DAMAGE_MULT,  0.0);
        stats.setBase(StatType.MAX_MP,       120); // MP tinggi untuk banyak heal
        stats.setBase(StatType.MP_REGEN,     12);
        stats.setBase(StatType.HP_REGEN,     5);
        stats.setBase(StatType.PHYSICAL_ATK, 10);
        stats.setBase(StatType.CYBER_ATK,    8);
        stats.setBase(StatType.ENERGY_ATK,   20); // offense via energy
        stats.setBase(StatType.PHYSICAL_DEF, 12);
        stats.setBase(StatType.CYBER_DEF,    15);
        stats.setBase(StatType.ENERGY_DEF,   18);
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
}
