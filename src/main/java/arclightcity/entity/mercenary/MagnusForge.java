package arclightcity.entity.mercenary;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * MAGNUS FORGE — Heavy Gunner
 * Role: DPS (AoE Specialist)
 *
 * Lore: Mantan insinyur senjata MegaCorp yang dipecat karena desainnya
 * "terlalu destruktif bahkan untuk standar korporat". Dia menganggap itu
 * sebagai pujian tertinggi. Sekarang dia menguji sendiri semua ciptaannya.
 *
 * Playstyle:
 *  - AoE damage terluas di semua merc
 *  - INCENDIARY ROUND: Burn semua musuh sekaligus
 *  - SHRED CANNON: kurangi DEF semua musuh sebelum burst
 *  - OVERLOAD SHOT: charge 1 turn → damage masif next turn
 *  - Lambat tapi setiap pukulan terasa
 *  - Lemah: lambat, tidak bisa dodge, jadi mudah jadi target
 *
 * Synergy: Tank-RX9 → dapat 10% DEF dari RX9 (lihat TankRX9)
 */
public class MagnusForge extends Mercenary {

    private boolean chargingOverload = false;
    private int     chargeTarget     = -1;

    public MagnusForge() {
        super("Magnus Forge",
              "Senjatanya tidak punya nama resmi. Dia menyebutnya 'persuasinya'. " +
              "Ukurannya hampir sebesar dia. Tidak ada yang pernah komplain tentang hasilnya.",
              MercenaryType.MAGNUS_FORGE,
              Role.DPS,
              900);

        initStats();
        aggressiveness = 0.85;
        healThreshold  = 0.25;
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       3200);
        stats.setBase(StatType.MAX_SHIELD,   40);
        stats.setBase(StatType.DAMAGE_MULT,  0.10); // HP tinggi untuk DPS
        stats.setBase(StatType.MAX_MP,       60);
        stats.setBase(StatType.MP_REGEN,     4);
        stats.setBase(StatType.HP_REGEN,     3);
        stats.setBase(StatType.PHYSICAL_ATK, 70); // ATK tertinggi di semua merc
        stats.setBase(StatType.ENERGY_ATK,   32);
        stats.setBase(StatType.PHYSICAL_DEF, 28.6);
        stats.setBase(StatType.CYBER_DEF,    13);
        stats.setBase(StatType.ENERGY_DEF,   19.5);
        stats.setBase(StatType.EVASION,      0.02); // hampir tidak bisa dodge
        stats.setBase(StatType.BLOCK_CHANCE, 0.10);
        stats.setBase(StatType.CRIT_CHANCE,  0.12);
        stats.setBase(StatType.CRIT_DAMAGE,  1.80);
        stats.setBase(StatType.SPEED,        8);   // lambat
        stats.setBase(StatType.ARMOR_PIERCE, 0.15);
        stats.setBase(StatType.ACCURACY,     0.88);
    }

    @Override
    protected CombatAction combatAction(List<Entity> allies, List<Entity> enemies) {
        List<Entity> aliveEnemies = enemies.stream().filter(Entity::isAlive).toList();
        if (aliveEnemies.isEmpty()) return CombatAction.pass();

        // Jika sedang charge → lepaskan OVERLOAD SHOT
        if (chargingOverload) {
            chargingOverload = false;
            List<String> allIds = aliveEnemies.stream().map(Entity::getId).toList();
            // Damage masif ke semua + Burn
            aliveEnemies.forEach(e ->
                    e.applyEffect(new StatusEffect(StatusEffectType.BURN, 3, 18.0, this.id)));
            return CombatAction.useSkill("OVERLOAD_SHOT", allIds);
        }

        int enemyCount = aliveEnemies.size();

        // Jika ≥3 musuh → SHRED CANNON dulu untuk strip DEF
        if (enemyCount >= 3) {
            boolean anyNotShredded = aliveEnemies.stream()
                    .anyMatch(e -> !e.hasEffect(StatusEffectType.SHRED));
            if (anyNotShredded) {
                List<String> allIds = aliveEnemies.stream().map(Entity::getId).toList();
                aliveEnemies.forEach(e ->
                        e.applyEffect(new StatusEffect(StatusEffectType.SHRED, 2, 12.0, this.id)));
                return CombatAction.useSkill("SHRED_CANNON", allIds);
            }
        }

        // Charge OVERLOAD jika semua musuh sudah di-shred
        boolean allShredded = aliveEnemies.stream()
                .allMatch(e -> e.hasEffect(StatusEffectType.SHRED));
        if (allShredded && !chargingOverload) {
            chargingOverload = true;
            // Turn ini skip (charging), next turn overload
            return CombatAction.useSkill("OVERLOAD_CHARGE", List.of());
        }

        // INCENDIARY ROUND: Burn semua musuh
        if (enemyCount >= 2) {
            List<String> allIds = aliveEnemies.stream().map(Entity::getId).toList();
            aliveEnemies.forEach(e ->
                    e.applyEffect(new StatusEffect(StatusEffectType.BURN, 2, 12.0, this.id)));
            return CombatAction.useSkill("INCENDIARY_ROUND", allIds);
        }

        // Single target fallback
        Entity target = getLowestHpEnemy(enemies);
        return CombatAction.basicAttack(List.of(target.getId()));
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
        stats.addBase(StatType.PHYSICAL_ATK, 5);
        stats.addBase(StatType.MAX_HP,       15);
        if (newLevel == 10) {
            // Soul Sync: Overload tidak perlu charge turn lagi
            stats.addBase(StatType.PHYSICAL_ATK, 20);
            stats.addBase(StatType.ARMOR_PIERCE,  0.15);
        }
    }

    public boolean isChargingOverload() { return chargingOverload; }
    @Override
    public CombatAction decideAction(java.util.List<Entity> allies, java.util.List<Entity> enemies) {
        Entity target = highestHpEnemy(enemies);
        if (target == null) return CombatAction.defend();

        // 1. BUFF: EMPOWERED (atk buff) ke team jika belum aktif
        if (!teamHasBuff(allies, arclightcity.entity.status.StatusEffectType.EMPOWERED)
                && hasMP(16)) {
            return skillAll("EMPOWER_TEAM", allies);
        }

        // 2. CC: STUN musuh terkuat yang belum stun
        Entity unstunned = enemyWithout(enemies, arclightcity.entity.status.StatusEffectType.STUN);
        if (unstunned != null && hasMP(18)) {
            return skill("STUN_SLAM", unstunned);
        }

        // 3. AoE attack jika musuh banyak
        if (enemies.stream().filter(Entity::isAlive).count() >= 2 && hasMP(25)) {
            return skillAll("OVERLOAD_SHOT", enemies.stream().filter(Entity::isAlive).toList());
        }

        // 4. Basic attack
        return attack(target);
    }

}
