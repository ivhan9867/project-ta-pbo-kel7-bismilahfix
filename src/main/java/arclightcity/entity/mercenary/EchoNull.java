package arclightcity.entity.mercenary;
import arclightcity.combat.CombatAction;
import arclightcity.entity.enemy.EnemyRace;
import arclightcity.entity.enemy.Enemy;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * ECHO NULL — Signal Jammer
 * Role: SPECIALIST (CC / Debuffer)
 *
 * Lore: Dulunya dia adalah sistem AI pengacak sinyal milik pemerintah.
 * Entah bagaimana dia berevolusi menjadi entitas mandiri dan memilih
 * wujud humanoid. Dia berbicara sedikit, tapi apa yang dia lakukan
 * cukup bicara sendiri.
 *
 * Playstyle:
 *  - Spesialis crowd control — hampir setiap skill apply CC
 *  - EMP BURST: Stun semua android/cyborg selama 2 turn
 *  - SIGNAL JAM: Silence + Blind semua musuh
 *  - FREQUENCY LOCK: prevent satu musuh dari bergerak selama 3 turn (Freeze-like)
 *  - Damage rendah tapi membuat musuh tidak bisa berbuat apapun
 *  - Lemah: dirinya sendiri lemah jika kena Hack (sesama signal entity)
 *
 * Synergy: Vector → Hack duration +1 (lihat Vector)
 *          Kira Voss → target yang di-CC oleh Echo = guaranteed crit dari Kira
 */
public class EchoNull extends Mercenary {

    private int empCooldown      = 0;
    private int frequencyLockCd  = 0;
    private int currentTurn      = 0;

    public EchoNull() {
        super("Echo Null",
              "Sinyal anomali yang entah dari mana mendapat kesadaran. " +
              "Kalau kamu dengar suara white noise tiba-tiba, kamu sudah dalam jangkauannya.",
              MercenaryType.ECHO_NULL,
              Role.SPECIALIST,
              750);

        initStats();
        aggressiveness = 0.60;
        healThreshold  = 0.30;
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       182);
        stats.setBase(StatType.MAX_SHIELD,   25);
        stats.setBase(StatType.DAMAGE_MULT,  0.0);
        stats.setBase(StatType.MAX_MP,       150); // MP tertinggi
        stats.setBase(StatType.MP_REGEN,     15);
        stats.setBase(StatType.PHYSICAL_ATK, 19.2); // damage sangat rendah
        stats.setBase(StatType.CYBER_ATK,    48); // tapi cyber damage lumayan
        stats.setBase(StatType.ENERGY_ATK,   24);
        stats.setBase(StatType.PHYSICAL_DEF, 13);
        stats.setBase(StatType.CYBER_DEF,    10.4);  // lemah ke Hack
        stats.setBase(StatType.ENERGY_DEF,   26);
        stats.setBase(StatType.TENACITY,     0.0); // tidak ada CC resistance sendiri
        stats.setBase(StatType.SPEED,        15);
        stats.setBase(StatType.EVASION,      0.10);
        stats.setBase(StatType.COOLDOWN_REDUCE, 0.20); // CDR tinggi
        stats.setBase(StatType.SKILL_POWER,  1.20);
    }

    @Override
    public void onTurnStart() {
        super.onTurnStart();
        currentTurn++;
        if (empCooldown > 0)     empCooldown--;
        if (frequencyLockCd > 0) frequencyLockCd--;
    }

    @Override
    protected CombatAction combatAction(List<Entity> allies, List<Entity> enemies) {
        List<Entity> aliveEnemies = enemies.stream().filter(Entity::isAlive).toList();
        if (aliveEnemies.isEmpty()) return CombatAction.pass();

        // EMP BURST: prioritas jika ada android/cyborg
        boolean hasAndroidOrCyborg = aliveEnemies.stream()
                .anyMatch(e -> e instanceof arclightcity.entity.enemy.Enemy enemy &&
                        (enemy.getRace() == EnemyRace.ANDROID ||
                         enemy.getRace() == EnemyRace.CYBORG));

        if (hasAndroidOrCyborg && empCooldown == 0) {
            empCooldown = 5;
            List<String> mechanicalIds = aliveEnemies.stream()
                    .filter(e -> e instanceof arclightcity.entity.enemy.Enemy en &&
                            (en.getRace() == EnemyRace.ANDROID ||
                             en.getRace() == EnemyRace.CYBORG))
                    .map(Entity::getId).toList();

            mechanicalIds.forEach(id -> {
                aliveEnemies.stream().filter(e -> e.getId().equals(id)).findFirst()
                        .ifPresent(t -> t.applyEffect(
                                new StatusEffect(StatusEffectType.STUN, 2, 0, this.id)));
            });
            return CombatAction.useSkill("EMP_BURST", mechanicalIds);
        }

        // FREQUENCY LOCK: isolasi satu target kuat
        Entity strongTarget = aliveEnemies.stream()
                .max(java.util.Comparator.comparingDouble(e -> e.getStats().get(StatType.PHYSICAL_ATK)))
                .orElse(null);

        if (strongTarget != null && frequencyLockCd == 0 &&
                !strongTarget.hasEffect(StatusEffectType.FREEZE)) {
            frequencyLockCd = 4;
            strongTarget.applyEffect(new StatusEffect(StatusEffectType.FREEZE,  3, 0, this.id));
            strongTarget.applyEffect(new StatusEffect(StatusEffectType.SILENCE, 3, 0, this.id));
            return CombatAction.useSkill("FREQUENCY_LOCK", List.of(strongTarget.getId()));
        }

        // SIGNAL JAM: AoE Silence + Blind jika banyak musuh belum di-CC
        long unccdCount = aliveEnemies.stream()
                .filter(e -> !e.isStunned() && !e.isFrozen() && !e.isSilenced())
                .count();

        if (unccdCount >= 2) {
            List<String> allIds = aliveEnemies.stream().map(Entity::getId).toList();
            aliveEnemies.forEach(e -> {
                if (!e.isStunned() && !e.isFrozen()) {
                    e.applyEffect(new StatusEffect(StatusEffectType.SILENCE, 2, 0, this.id));
                    e.applyEffect(new StatusEffect(StatusEffectType.BLIND,   2, 0, this.id));
                }
            });
            return CombatAction.useSkill("SIGNAL_JAM", allIds);
        }

        // Fallback: Virus ke target belum kena DOT
        Entity cleanTarget = aliveEnemies.stream()
                .filter(e -> !e.hasEffect(StatusEffectType.VIRUS))
                .findFirst().orElse(aliveEnemies.get(0));

        cleanTarget.applyEffect(new StatusEffect(StatusEffectType.VIRUS, 3, 10.0, this.id));
        return CombatAction.useSkill("SIGNAL_VIRUS", List.of(cleanTarget.getId()));
    }

    @Override
    protected void applySynergyWith(Mercenary other) {
        // Synergy dengan Kira: target yang di-CC = bonus crit Kira
        if (other.getMercenaryType() == MercenaryType.KIRA_VOSS) {
            stats.addBase(StatType.COOLDOWN_REDUCE, 0.05);
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
        stats.addBase(StatType.MAX_MP,          20);
        stats.addBase(StatType.COOLDOWN_REDUCE,  0.03);
        if (newLevel == 10) {
            // Soul Sync: EMP Burst juga affect Human targets (AoE sejati)
            stats.addBase(StatType.CYBER_ATK,       20);
            stats.addBase(StatType.COOLDOWN_REDUCE,  0.15);
        }
    }
    @Override
    public CombatAction decideAction(java.util.List<Entity> allies, java.util.List<Entity> enemies) {
        Entity target = highestHpEnemy(enemies);
        if (target == null) return CombatAction.defend();

        // 1. BUFF: SYNC (speed buff) ke team jika belum aktif
        if (!teamHasBuff(allies, arclightcity.entity.status.StatusEffectType.SYNC)
                && hasMP(14)) {
            return skillAll("SYNC_TEAM", allies);
        }

        // 2. CC: FREEZE musuh paling berbahaya (HP tertinggi yang belum freeze)
        Entity unfrozen = enemyWithout(enemies, arclightcity.entity.status.StatusEffectType.FREEZE);
        if (unfrozen != null && hasMP(22)) {
            return skill("FREQUENCY_LOCK", unfrozen);
        }

        // 3. CC: STUN jika ada musuh yang belum stun
        Entity unstunned = enemyWithout(enemies, arclightcity.entity.status.StatusEffectType.STUN);
        if (unstunned != null && hasMP(16)) {
            return skill("EMP_BURST", unstunned);
        }

        // 4. SIGNAL_JAM: slow semua musuh
        if (!target.hasEffect(arclightcity.entity.status.StatusEffectType.SLOW) && hasMP(18)) {
            return skillAll("SIGNAL_JAM", enemies.stream().filter(Entity::isAlive).toList());
        }

        return attack(target);
    }

}
