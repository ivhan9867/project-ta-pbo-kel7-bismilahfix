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
        double scaleFactor = 1.0 + (floor - 1) * 0.08; // +8% per floor (turun dari 15%)
        stats.scaleBase(scaleFactor);

        // Reward scale
        expReward  *= scaleFactor;
        goldReward  = (long)(goldReward * scaleFactor);

        initVitals(); // refresh HP/MP setelah scaling
    }

    // ── AI Logic ─────────────────────────────────────────────

    @Override
    public CombatAction decideAction(List<Entity> allies, List<Entity> enemies) {
        turnsSinceLastSpecial++;

        // Cek kondisi: HP rendah → prioritas survive
        if (getHpPercent() < 0.25 && shouldUseDesperateAction()) {
            return desperateAction(allies, enemies);
        }

        // Cek cooldown skill khusus
        if (turnsSinceLastSpecial >= getSpecialCooldown() && canUseSpecial()) {
            turnsSinceLastSpecial = 0;
            return specialAction(allies, enemies);
        }

        return normalAction(allies, enemies);
    }

    // ── Abstract AI Methods ──────────────────────────────────

    /** Aksi normal tiap turn */
    protected abstract CombatAction normalAction(List<Entity> allies, List<Entity> enemies);

    /** Aksi spesial (skill kuat, dipanggil tiap N turn) */
    protected abstract CombatAction specialAction(List<Entity> allies, List<Entity> enemies);

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
