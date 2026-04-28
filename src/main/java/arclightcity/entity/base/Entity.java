package arclightcity.entity.base;
import arclightcity.combat.CombatAction;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.DamageType;
import arclightcity.entity.stats.StatType;
import arclightcity.entity.stats.StatSheet;

import java.util.*;

/**
 * Base class untuk semua entity yang bisa bertarung di game:
 * Player, Enemy, Boss, Mercenary.
 *
 * Menyimpan: HP/MP saat ini, StatSheet, StatusEffect aktif,
 * dan logika dasar combat (take damage, heal, apply effect).
 */
public abstract class Entity {

    // ── Identity ─────────────────────────────────────────────
    protected final String id;       // UUID unik
    protected String name;
    protected String description;
    protected EntityType entityType;

    // ── Vital Resources ──────────────────────────────────────
    protected double currentHp;
    protected double currentMp;
    protected double currentShield; // Shield diserap SEBELUM HP
    protected boolean alive = true;

    // ── Stats ────────────────────────────────────────────────
    protected final StatSheet stats;

    // ── Status Effects ───────────────────────────────────────
    protected final List<StatusEffect> activeEffects = new ArrayList<>();

    // ── Combat Tracking ──────────────────────────────────────
    protected double totalDamageDealt   = 0;
    protected double totalDamageReceived = 0;
    protected double totalHealingDone   = 0;
    protected int    turnCount          = 0;

    // ── Constructor ─────────────────────────────────────────

    protected Entity(String name, String description, EntityType entityType) {
        this.id          = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.name        = name;
        this.description = description;
        this.entityType  = entityType;
        this.stats       = new StatSheet();
    }

    // ── Abstract Methods ─────────────────────────────────────

    /** Inisialisasi stat dasar entity — dipanggil setelah konstruktor */
    protected abstract void initStats();

    /** Logika aksi di turn entity ini (attack, skill, dll) */
    public abstract CombatAction decideAction(List<Entity> allies, List<Entity> enemies);

    // ── HP & MP Management ───────────────────────────────────

    public void initVitals() {
        this.currentHp     = stats.get(StatType.MAX_HP);
        this.currentMp     = stats.get(StatType.MAX_MP);
        this.currentShield = stats.get(StatType.MAX_SHIELD);
    }

    /**
     * Terima damage. Urutan penyerapan:
     *   1. Evasion check  → miss total
     *   2. DEF reduction  → kurangi raw damage
     *   3. Block check    → kurangi 50%
     *   4. Barrier (buff) → absorb sejumlah power
     *   5. SHIELD         → serap sisa damage sebelum kena HP
     *   6. HP             → damage akhir
     *
     * @return DamageResult berisi info detail damage
     */
    public DamageResult receiveDamage(double rawDamage, DamageType type, boolean ignoreArmor) {
        // ── 1. Evasion check ─────────────────────────────────
        double evasion = stats.get(StatType.EVASION);
        if (Math.random() < evasion) {
            return new DamageResult(0, 0, true, false, type);
        }

        double finalDamage = rawDamage;

        // ── 2. DEF reduction (kecuali TRUE damage) ───────────
        if (type != DamageType.TRUE && type != DamageType.HEAL && !ignoreArmor) {
            double defense = stats.get(type.resistanceStat);
            finalDamage = rawDamage * (100.0 / (100.0 + Math.max(0, defense)));
        }

        // ── 3. Block check ───────────────────────────────────
        boolean blocked = false;
        if (Math.random() < stats.get(StatType.BLOCK_CHANCE)) {
            finalDamage *= 0.50;
            blocked = true;
        }

        // ── 4. Barrier (status effect) absorb ────────────────
        StatusEffect barrier = findEffect(StatusEffectType.BARRIER);
        if (barrier != null) {
            double absorbed = Math.min(barrier.getPower(), finalDamage);
            finalDamage -= absorbed;
            // Kurangi power barrier
            if (absorbed >= barrier.getPower()) {
                removeEffect(StatusEffectType.BARRIER);
            }
        }

        finalDamage = Math.max(1, Math.round(finalDamage));

        // ── 5. Shield absorb (sebelum HP) ────────────────────
        double shieldDamage = 0;
        double hpDamage     = finalDamage;

        if (currentShield > 0 && type != DamageType.TRUE) {
            shieldDamage    = Math.min(currentShield, finalDamage);
            hpDamage        = finalDamage - shieldDamage;
            currentShield   = Math.max(0, currentShield - shieldDamage);
        }

        // ── 6. HP damage ──────────────────────────────────────
        currentHp = Math.max(0, currentHp - hpDamage);
        totalDamageReceived += finalDamage;

        if (currentHp <= 0) {
            alive = false;
            onDeath();
        }

        return new DamageResult(finalDamage, shieldDamage, false, blocked, type);
    }

    /**
     * Pulihkan HP.
     * @return jumlah HP yang benar-benar dipulihkan
     */
    public double receiveHeal(double amount) {
        double maxHp  = stats.get(StatType.MAX_HP);
        double before = currentHp;
        currentHp     = Math.min(maxHp, currentHp + amount);
        double healed = currentHp - before;
        totalHealingDone += healed;
        return healed;
    }

    public boolean spendMp(double amount) {
        if (currentMp < amount) return false;
        currentMp -= amount;
        return true;
    }

    public void restoreMp(double amount) {
        currentMp = Math.min(stats.get(StatType.MAX_MP), currentMp + amount);
    }

    /** Pulihkan Shield (tidak bisa melebihi MAX_SHIELD) */
    public double restoreShield(double amount) {
        double maxShield = stats.get(StatType.MAX_SHIELD);
        if (maxShield <= 0) return 0;
        double before = currentShield;
        currentShield = Math.min(maxShield, currentShield + amount);
        return currentShield - before;
    }

    /** Set shield langsung (untuk equipment re-apply) */
    public void setShield(double amount) {
        currentShield = Math.max(0, Math.min(stats.get(StatType.MAX_SHIELD), amount));
    }

    // ── Status Effect Management ─────────────────────────────

    public void applyEffect(StatusEffect effect) {
        // Cek apakah sudah ada effect tipe sama
        StatusEffect existing = findEffect(effect.getType());
        if (existing != null) {
            int maxStacks = getMaxStacks(effect.getType());
            existing.refresh(effect.getRemainingTurns(), effect.getPower(), maxStacks);
            return;
        }
        activeEffects.add(effect);
        onEffectApplied(effect);
    }

    public void removeEffect(StatusEffectType type) {
        activeEffects.removeIf(e -> e.getType() == type);
        onEffectRemoved(type);
    }

    public boolean hasEffect(StatusEffectType type) {
        return findEffect(type) != null;
    }

    public StatusEffect findEffect(StatusEffectType type) {
        return activeEffects.stream()
                .filter(e -> e.getType() == type)
                .findFirst().orElse(null);
    }

    /**
     * Proses semua DOT dan tick durasi semua effect.
     * Dipanggil di awal/akhir turn entity.
     * @return list damage events dari DOT
     */
    public List<DotTickResult> tickEffects() {
        List<DotTickResult> results = new ArrayList<>();
        List<StatusEffect> toRemove = new ArrayList<>();

        // Iterate over COPY untuk cegah ConcurrentModificationException
        // receiveDamage() bisa memodifikasi activeEffects (via onDeath/applyEffect)
        List<StatusEffect> snapshot = new ArrayList<>(activeEffects);

        for (StatusEffect effect : snapshot) {
            // Skip jika effect sudah dihapus di tengah iterasi
            if (!activeEffects.contains(effect)) continue;

            // Process DOT
            if (effect.getType().category == StatusEffectType.Category.DOT) {
                double dot = effect.calculateDotDamage();
                DamageType dmgType = getDotDamageType(effect.getType());
                DamageResult result = receiveDamage(dot, dmgType, true);
                results.add(new DotTickResult(effect.getType(), result.damage));
            }

            // Tick durasi — hanya jika entity masih hidup
            if (alive && !effect.tick()) {
                toRemove.add(effect);
            }

            // Stop jika entity mati kena DOT
            if (!alive) break;
        }

        // Remove expired effects (safe karena tidak dalam loop activeEffects)
        toRemove.forEach(e -> {
            activeEffects.remove(e);
            onEffectRemoved(e.getType());
        });

        return results;
    }

    // ── Turn Lifecycle ───────────────────────────────────────

    /**
     * Dipanggil di awal turn entity ini.
     */
    public void onTurnStart() {
        turnCount++;
        // HP Regen
        double hpRegen = stats.get(StatType.HP_REGEN);
        if (hpRegen > 0) receiveHeal(hpRegen);
        // MP Regen
        double mpRegen = stats.get(StatType.MP_REGEN);
        if (mpRegen > 0) restoreMp(mpRegen);
        // Shield Regen
        double shieldRegen = stats.get(StatType.SHIELD_REGEN);
        if (shieldRegen > 0) restoreShield(shieldRegen);
    }

    /**
     * Dipanggil di akhir turn entity ini.
     */
    public void onTurnEnd() {
        tickEffects();
    }

    // ── Hook Methods (override di subclass) ──────────────────
    protected void onDeath()                          { }
    protected void onEffectApplied(StatusEffect e)    { }
    protected void onEffectRemoved(StatusEffectType t){ }

    // ── Helper ───────────────────────────────────────────────

    private DamageType getDotDamageType(StatusEffectType type) {
        return switch (type) {
            case BURN, BLEED, CORRODE -> DamageType.PHYSICAL;
            case VIRUS, DRAIN         -> DamageType.CYBER;
            case IRRADIATE            -> DamageType.ENERGY;
            default                   -> DamageType.TRUE;
        };
    }

    private int getMaxStacks(StatusEffectType type) {
        return switch (type) {
            case BURN      -> 3;
            case BLEED     -> 5;
            case VIRUS     -> 2;
            default        -> 1;
        };
    }

    // ── Status Checks ─────────────────────────────────────────

    public boolean isStunned()   { return hasEffect(StatusEffectType.STUN); }
    public boolean isFrozen()    { return hasEffect(StatusEffectType.FREEZE); }
    public boolean isSilenced()  { return hasEffect(StatusEffectType.SILENCE); }
    public boolean isAsleep()    { return hasEffect(StatusEffectType.SLEEP); }
    public boolean isInStealth() { return hasEffect(StatusEffectType.STEALTH); }
    public boolean canAct()      { return alive && !isStunned() && !isFrozen() && !isAsleep(); }

    // ── Getters ──────────────────────────────────────────────

    public String       getId()           { return id; }
    public String       getName()         { return name; }
    public String       getDescription()  { return description; }
    public EntityType   getEntityType()   { return entityType; }
    public double       getCurrentHp()    { return currentHp; }
    public double       getCurrentMp()    { return currentMp; }
    public double       getCurrentShield(){ return currentShield; }
    public boolean      isAlive()         { return alive; }
    public StatSheet    getStats()        { return stats; }
    public List<StatusEffect> getActiveEffects() { return Collections.unmodifiableList(activeEffects); }

    public double getHpPercent() {
        double max = stats.get(StatType.MAX_HP);
        return max <= 0 ? 0 : currentHp / max;
    }

    public double getMpPercent() {
        double max = stats.get(StatType.MAX_MP);
        return max <= 0 ? 0 : currentMp / max;
    }

    public double getShieldPercent() {
        double max = stats.get(StatType.MAX_SHIELD);
        return max <= 0 ? 0 : currentShield / max;
    }

    public boolean hasShield() { return currentShield > 0; }

    public double getTotalDamageDealt()    { return totalDamageDealt; }
    public double getTotalDamageReceived() { return totalDamageReceived; }
    public void   addDamageDealt(double amount) { totalDamageDealt += amount; }
    public int    getTurnCount()           { return turnCount; }

    @Override
    public String toString() {
        return String.format("[%s] %s — HP: %.0f/%.0f | Shield: %.0f/%.0f | MP: %.0f/%.0f",
                id, name,
                currentHp,     stats.get(StatType.MAX_HP),
                currentShield, stats.get(StatType.MAX_SHIELD),
                currentMp,     stats.get(StatType.MAX_MP));
    }

    // ── Inner Result Classes ─────────────────────────────────

    public static class DamageResult {
        public final double     damage;       // total damage (shield + hp)
        public final double     shieldDamage; // porsi yang diserap shield
        public final double     hpDamage;     // porsi yang kena HP langsung
        public final boolean    evaded;
        public final boolean    blocked;
        public final DamageType type;

        public DamageResult(double damage, double shieldDamage,
                            boolean evaded, boolean blocked, DamageType type) {
            this.damage       = damage;
            this.shieldDamage = shieldDamage;
            this.hpDamage     = damage - shieldDamage;
            this.evaded       = evaded;
            this.blocked      = blocked;
            this.type         = type;
        }
    }

    public static class DotTickResult {
        public final StatusEffectType effectType;
        public final double           damage;

        public DotTickResult(StatusEffectType effectType, double damage) {
            this.effectType = effectType;
            this.damage     = damage;
        }
    }
}
