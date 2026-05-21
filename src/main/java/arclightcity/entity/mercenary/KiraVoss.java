package arclightcity.entity.mercenary;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * KIRA VOSS — Ghost Sniper
 * Role: DPS
 *
 * Lore: Eks-agen korporat yang membelot setelah bosnya menjualnya ke musuh.
 * Sekarang dia tidak bekerja untuk siapapun kecuali dirinya sendiri — dan siapapun
 * yang bisa bayar cukup. Dingin, presisi, efisien.
 *
 * Playstyle:
 *  - Crit damage tertinggi di antara semua merc
 *  - Masuk Stealth setiap awal babak, keluar saat menyerang
 *  - Serangan dari Stealth: GUARANTEED CRIT + Expose target
 *  - Lemah: HP rendah, tidak bisa tank sama sekali
 *
 * Synergy: Vector (Cyber Assassin) → +15% Crit Damage berdua
 */
public class KiraVoss extends Mercenary {

    private boolean readyForStealth = true;

    public KiraVoss() {
        super("Kira Voss",
              "Ghost Sniper yang tidak pernah meleset. Kalau kamu bisa melihatnya, " +
              "kamu sudah terlambat. Kalau kamu tidak bisa melihatnya, kamu sudah mati.",
              MercenaryType.KIRA_VOSS,
              Role.DPS,
              800);

        initStats();
        aggressiveness = 0.90;
        healThreshold  = 0.20;
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       2200);
        stats.setBase(StatType.MAX_SHIELD,   20);
        stats.setBase(StatType.DAMAGE_MULT,  0.12);
        stats.setBase(StatType.MAX_MP,       70);
        stats.setBase(StatType.PHYSICAL_ATK, 80);
        stats.setBase(StatType.CYBER_ATK, 45);
        stats.setBase(StatType.PHYSICAL_DEF, 10.4);
        stats.setBase(StatType.CYBER_DEF,    13);
        stats.setBase(StatType.ENERGY_DEF,   10.4);
        stats.setBase(StatType.CRIT_CHANCE,  0.30); // crit tinggi
        stats.setBase(StatType.CRIT_DAMAGE,  2.20); // crit damage tinggi
        stats.setBase(StatType.ARMOR_PIERCE, 0.20);
        stats.setBase(StatType.EVASION,      0.18);
        stats.setBase(StatType.SPEED,        17);
        stats.setBase(StatType.ACCURACY,     0.97);
        stats.setBase(StatType.MP_REGEN,     5);
    }

    @Override
    public void onTurnStart() {
        super.onTurnStart();
        // Masuk Stealth tiap awal turn jika tidak sedang kena CC
        if (readyForStealth && canAct()) {
            this.applyEffect(new StatusEffect(StatusEffectType.STEALTH, 1, 0, this.id));
        }
    }

    @Override
    protected CombatAction combatAction(List<Entity> allies, List<Entity> enemies) {
        Entity target = getLowestHpEnemy(enemies);
        if (target == null) return CombatAction.pass();

        boolean inStealth = this.isInStealth();

        if (inStealth) {
            // PHANTOM SHOT: keluar stealth, guaranteed crit + Expose target
            this.removeEffect(StatusEffectType.STEALTH);
            this.applyEffect(new StatusEffect(StatusEffectType.EMPOWERED, 1, 0, this.id));
            target.applyEffect(new StatusEffect(StatusEffectType.EXPOSE, 2, 0, this.id));
            readyForStealth = true;
            return CombatAction.useSkill("PHANTOM_SHOT", List.of(target.getId()));
        }

        // Normal: basic attack ke target terendah HP
        readyForStealth = true;
        return CombatAction.basicAttack(List.of(target.getId()));
    }

    @Override
    protected void applySynergyWith(Mercenary other) {
        // Synergy dengan Vector: +15% Crit Damage
        if (other.getMercenaryType() == MercenaryType.VECTOR) {
            stats.addBase(StatType.CRIT_DAMAGE, 0.15);
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
        // Tiap loyalty level: +crit chance kecil
        stats.addBase(StatType.CRIT_CHANCE, 0.02);
        if (newLevel == 10) {
            // Soul Sync: Phantom Shot tidak butuh stealth lagi untuk guaranteed crit
            stats.addBase(StatType.CRIT_CHANCE, 0.10);
            stats.addBase(StatType.CRIT_DAMAGE, 0.30);
        }
    }
    @Override
    public CombatAction decideAction(java.util.List<Entity> allies, java.util.List<Entity> enemies) {
        Entity target = highestHpEnemy(enemies);
        if (target == null) return CombatAction.defend();

        // 1. BUFF: FOCUS (crit buff) ke team jika belum aktif
        if (!teamHasBuff(allies, arclightcity.entity.status.StatusEffectType.FOCUS)
                && hasMP(14)) {
            return skillAll("FOCUS_TEAM", allies);
        }

        // 2. CC: EXPOSE musuh paling kuat yang belum di-expose
        Entity unexposed = enemyWithout(enemies, arclightcity.entity.status.StatusEffectType.EXPOSE);
        if (unexposed != null && hasMP(12)) {
            return skill("EXPOSE_SHOT", unexposed);
        }

        // 3. Attack: PHANTOM_SHOT ke musuh HP tertinggi
        if (hasMP(20)) return skill("PHANTOM_SHOT", target);
        return attack(target);
    }

}
