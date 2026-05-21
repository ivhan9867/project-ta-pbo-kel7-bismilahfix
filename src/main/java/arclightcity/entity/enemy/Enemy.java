package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.base.Entity;


import java.util.List;

/**
 * Base class semua enemy di game.
 * Setiap enemy type memiliki:
 *  - Loot table (ID loot group)
 *  - AI behavior pattern
 *  - Threat level (untuk scaling dungeon)
 *  - EXP & Gold reward
 */
public abstract class Enemy extends Entity {

    protected final EnemyRace      race;
    protected final ThreatLevel    threatLevel;
    protected final String         lootTableId;
    protected       double         expReward;
    protected       long           goldReward;
    protected       int            floorLevel;   // floor dungeon di mana enemy ini spawn

    // AI State
    protected int    turnsSinceLastSpecial = 0;
    protected double aggression            = 1.0; // modifier AI behavior

    // ── Constructor ─────────────────────────────────────────

    protected Enemy(String name, String description, EntityType entityType,
                    EnemyRace race, ThreatLevel threatLevel, String lootTableId) {
        super(name, description, entityType);
        this.race        = race;
        this.threatLevel = threatLevel;
        this.lootTableId = lootTableId;
    }

    // ── Floor Scaling ────────────────────────────────────────

    /**
     * Scale semua stat enemy berdasarkan floor dungeon.
     * Dipanggil oleh DungeonGenerator saat spawn enemy.
     */
    public void scaleToFloor(int floor) {
        this.floorLevel = floor;

        if (this instanceof arclightcity.entity.enemy.Boss) {
            // BOSS: skala HP agresif, ATK moderat agar tidak one-shot
            // Pisahkan scaling HP vs ATK — bossScale lama menyebabkan ATK 89999
            double atkScale = 1.0 + (floor - 1) * 0.06;  // ATK +6%/floor (was 14%)
            double spdScale = 1.0 + (floor - 1) * 0.04;  // SPEED +4%/floor

            // Scale ATK + SPEED manual (bukan scaleBase keseluruhan)
            for (arclightcity.entity.stats.StatType at : new arclightcity.entity.stats.StatType[]{
                arclightcity.entity.stats.StatType.PHYSICAL_ATK,
                arclightcity.entity.stats.StatType.CYBER_ATK,
                arclightcity.entity.stats.StatType.ENERGY_ATK}) {
                double base = stats.get(at);
                if (base > 0) stats.setBase(at, base * atkScale);
            }
            stats.setBase(arclightcity.entity.stats.StatType.SPEED,
                stats.get(arclightcity.entity.stats.StatType.SPEED) * spdScale);

            // ZERO semua DEF & SHIELD — boss kuat dari HP, bukan DEF
            stats.setBase(arclightcity.entity.stats.StatType.PHYSICAL_DEF, 0);
            stats.setBase(arclightcity.entity.stats.StatType.CYBER_DEF, 0);
            stats.setBase(arclightcity.entity.stats.StatType.ENERGY_DEF, 0);
            stats.setBase(arclightcity.entity.stats.StatType.MAX_SHIELD, 0);
            stats.setBase(arclightcity.entity.stats.StatType.SHIELD_REGEN, 0);
            this.currentShield = 0;

            // HP sangat besar: minimal 10000, naik 600/floor
            double baseHp = stats.get(arclightcity.entity.stats.StatType.MAX_HP);
            double bossHp = Math.max(baseHp * 2.5, 10000.0 + floor * 600.0);
            stats.setBase(arclightcity.entity.stats.StatType.MAX_HP, bossHp);

            // Cap EVASION maks 20%
            if (stats.get(arclightcity.entity.stats.StatType.EVASION) > 0.20)
                stats.setBase(arclightcity.entity.stats.StatType.EVASION, 0.20);

            // Hard cap ATK agar tidak one-shot — maks 1200 per stat per floor
            double maxAtk = 400 + floor * 16.0; // floor 50 → 1200 maks ATK per stat
            for (arclightcity.entity.stats.StatType at : new arclightcity.entity.stats.StatType[]{
                arclightcity.entity.stats.StatType.PHYSICAL_ATK,
                arclightcity.entity.stats.StatType.CYBER_ATK,
                arclightcity.entity.stats.StatType.ENERGY_ATK}) {
                if (stats.get(at) > maxAtk) stats.setBase(at, maxAtk);
            }
        } else {
            // KROCO: HP naik +15%/floor, ATK +5%/floor, DEF = 0 (pure HP)
            // Scaling lebih agresif agar challenge meningkat seiring lantai
            double hpScale  = 1.0 + (floor - 1) * 0.15;
            double atkScale = 1.0 + (floor - 1) * 0.05;

            double baseHp = stats.get(arclightcity.entity.stats.StatType.MAX_HP);
            stats.setBase(arclightcity.entity.stats.StatType.MAX_HP, baseHp * hpScale);
            stats.setBase(arclightcity.entity.stats.StatType.PHYSICAL_ATK,
                stats.get(arclightcity.entity.stats.StatType.PHYSICAL_ATK) * atkScale);
            // DEF = 0: player damage selalu masuk penuh ke HP
            stats.setBase(arclightcity.entity.stats.StatType.PHYSICAL_DEF, 0);
            stats.setBase(arclightcity.entity.stats.StatType.CYBER_DEF, 0);
            stats.setBase(arclightcity.entity.stats.StatType.ENERGY_DEF, 0);
            // Hapus shield dan MP untuk kroco
            stats.setBase(arclightcity.entity.stats.StatType.MAX_SHIELD, 0);
            stats.setBase(arclightcity.entity.stats.StatType.MAX_MP, 0);
        }

        expReward  *= (1.0 + (floor - 1) * 0.08);
        goldReward  = (long)(goldReward * (1.0 + (floor - 1) * 0.08));
        initVitals();
    }

    // ── AI Logic ─────────────────────────────────────────────

    @Override
    public CombatAction decideAction(List<Entity> allies, List<Entity> enemies) {
        // Enemy hanya basic attack — tanpa skill/buff/debuff
        return normalAction(allies, enemies);
    }

    // ── Abstract AI Methods ──────────────────────────────────

    /** Aksi normal tiap turn */
    protected abstract CombatAction normalAction(List<Entity> allies, List<Entity> enemies);

    /** Aksi spesial (skill kuat, dipanggil tiap N turn) */
    /** Aksi spesial — di-override oleh subclass, default: basic attack */
    protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        return normalAction(allies, enemies);
    }

    /** Aksi saat HP sangat rendah */
    protected CombatAction desperateAction(List<Entity> allies, List<Entity> enemies) {
        // Default: basic attack ke target HP terendah
        return normalAction(allies, enemies);
    }

    protected boolean shouldUseDesperateAction() { return false; }
    protected boolean canUseSpecial()             { return true; }
    protected int     getSpecialCooldown()        { return 3; }

    // ── Helper untuk AI ──────────────────────────────────────

    /** Pilih enemy (target) dengan HP terendah */
    protected Entity getLowestHpTarget(List<Entity> targets) {
        return targets.stream()
                .filter(Entity::isAlive)
                .min(java.util.Comparator.comparingDouble(Entity::getCurrentHp))
                .orElse(null);
    }

    protected Entity getHighestAtkTarget(List<Entity> targets) {
        return targets.stream()
                .filter(Entity::isAlive)
                .max(java.util.Comparator.comparingDouble(
                    e -> e.getStats().get(arclightcity.entity.stats.StatType.PHYSICAL_ATK)))
                .orElse(null);
    }

    protected Entity getFastestTarget(List<Entity> targets) {
        return targets.stream()
                .filter(Entity::isAlive)
                .max(java.util.Comparator.comparingDouble(
                    e -> e.getStats().get(arclightcity.entity.stats.StatType.SPEED)))
                .orElse(null);
    }

    /** Pilih enemy (target) dengan HP tertinggi (tanky) */
    protected Entity getHighestHpTarget(List<Entity> targets) {
        return targets.stream()
                .filter(Entity::isAlive)
                .max(java.util.Comparator.comparingDouble(Entity::getCurrentHp))
                .orElse(null);
    }

    /** Pilih target random */
    protected Entity getRandomTarget(List<Entity> targets) {
        List<Entity> alive = targets.stream().filter(Entity::isAlive).toList();
        if (alive.isEmpty()) return null;
        return alive.get((int)(Math.random() * alive.size()));
    }

    // ── Getters ─────────────────────────────────────────────

    public EnemyRace   getRace()        { return race; }
    public ThreatLevel getThreatLevel() { return threatLevel; }
    public String      getLootTableId() { return lootTableId; }
    public double      getExpReward()   { return expReward; }
    public long        getGoldReward()  { return goldReward; }
    public int         getFloorLevel()  { return floorLevel; }
}
