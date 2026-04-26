package arclightcity.entity.enemy;
import arclightcity.combat.CombatManager;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * VOID SPECTER — Entitas digital yang bocor ke dunia fisik.
 * IMMUNE terhadap Physical damage (tubuhnya tidak solid).
 * Hanya bisa dilukai dengan Cyber atau Energy attack.
 *
 * Mechanic unik:
 *   - PHASE SHIFT: Tiap 3 turn, masuk Stealth 1 turn (immune damage)
 *   - SOUL DRAIN: Setiap hit mengambil sebagian MAX_HP target secara permanen
 *   - CORRUPT: Chance mengubah buff target menjadi debuff
 *
 * Floor: 12-25
 */
public class VoidSpecter extends Enemy {

    private int phaseShiftCounter = 0;
    private boolean isPhasing     = false;

    public VoidSpecter() {
        super("Void Specter",
              "Sisa kesadaran digital yang terperangkap antara dunia nyata dan jaringan data. " +
              "Bentuknya berubah-ubah seperti sinyal yang terdistorsi.",
              EntityType.ELITE,
              EnemyRace.SPECTER,
              ThreatLevel.ELITE,
              "LOOT_SPECTER_HIGH");

        initStats();
        expReward  = 200;
        goldReward = 60; // sulit dijual (entitas digital)
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       180);
        stats.setBase(StatType.MAX_SHIELD,   60);  // specter punya digital barrier
        stats.setBase(StatType.DAMAGE_MULT,  0.10);
        stats.setBase(StatType.MAX_MP,       120);
        stats.setBase(StatType.PHYSICAL_ATK, 5);   // lemah fisik
        stats.setBase(StatType.CYBER_ATK,    35);  // kuat cyber
        stats.setBase(StatType.ENERGY_ATK,   25);
        stats.setBase(StatType.PHYSICAL_DEF, 9999); // IMMUNE physical (handle di CombatManager)
        stats.setBase(StatType.CYBER_DEF,    10);
        stats.setBase(StatType.ENERGY_DEF,   10);
        stats.setBase(StatType.SPEED,        14);
        stats.setBase(StatType.EVASION,      0.15);
        stats.setBase(StatType.MP_REGEN,     10);
    }

    @Override
    public void onTurnStart() {
        super.onTurnStart();
        phaseShiftCounter++;

        if (phaseShiftCounter >= 3) {
            phaseShiftCounter = 0;
            isPhasing = true;
            this.applyEffect(new StatusEffect(StatusEffectType.STEALTH, 1, 0, this.id));
        } else {
            isPhasing = false;
            this.removeEffect(StatusEffectType.STEALTH);
        }
    }

    @Override
    protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        if (isPhasing) return CombatAction.pass(); // sedang phase shift

        Entity target = getRandomTarget(enemies);
        if (target == null) return CombatAction.pass();

        // Soul Drain: kurangi MAX_HP target secara permanen
        double drainAmount = 5 + (stats.get(StatType.CYBER_ATK) * 0.1);
        target.getStats().addBase(StatType.MAX_HP, -drainAmount);

        return CombatAction.basicAttack(List.of(target.getId()));
    }

    @Override
    protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        if (isPhasing) return CombatAction.pass();

        // CORRUPT: Balikkan semua buff target menjadi debuff
        Entity target = getLowestHpTarget(enemies);
        if (target == null) return CombatAction.pass();

        // Hapus buff, ganti dengan debuff
        if (target.hasEffect(StatusEffectType.FORTIFY)) {
            target.removeEffect(StatusEffectType.FORTIFY);
            target.applyEffect(new StatusEffect(StatusEffectType.SHRED, 2, 15.0, this.id));
        }
        if (target.hasEffect(StatusEffectType.REGEN)) {
            target.removeEffect(StatusEffectType.REGEN);
            target.applyEffect(new StatusEffect(StatusEffectType.BLEED, 2, 10.0, this.id));
        }
        if (target.hasEffect(StatusEffectType.EMPOWERED)) {
            target.removeEffect(StatusEffectType.EMPOWERED);
            target.applyEffect(new StatusEffect(StatusEffectType.WEAKEN, 2, 15.0, this.id));
        }

        // Tambah Silence
        target.applyEffect(new StatusEffect(StatusEffectType.SILENCE, 2, 0, this.id));
        return CombatAction.useSkill("CORRUPT", List.of(target.getId()));
    }

    @Override
    protected CombatAction desperateAction(List<Entity> allies, List<Entity> enemies) {
        // VOID RUPTURE: AoE Cyber besar + Irradiate semua target
        List<String> allTargetIds = enemies.stream()
                .filter(Entity::isAlive).map(Entity::getId).toList();

        enemies.stream().filter(Entity::isAlive).forEach(t ->
                t.applyEffect(new StatusEffect(StatusEffectType.IRRADIATE, 3, 20.0, this.id)));

        return CombatAction.useSkill("VOID_RUPTURE", allTargetIds);
    }

    @Override
    protected boolean shouldUseDesperateAction() { return Math.random() < 0.70; }
    @Override
    protected int getSpecialCooldown()           { return 3; }
    public boolean isPhasing()                   { return isPhasing; }
}
