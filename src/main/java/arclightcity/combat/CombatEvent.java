package arclightcity.combat;
import arclightcity.entity.base.Entity;
import arclightcity.entity.stats.DamageType;
import arclightcity.entity.status.StatusEffectType;


/**
 * Satu event yang terjadi dalam satu turn combat.
 * Dikumpulkan oleh CombatManager dan dikirim ke UI untuk ditampilkan.
 *
 * Tiap event punya: jenis, siapa melakukan, ke siapa, nilai, dan pesan teks.
 */
public class CombatEvent {

    public enum EventType {
        // Damage
        DAMAGE_DEALT,
        DAMAGE_EVADED,
        DAMAGE_BLOCKED,
        CRITICAL_HIT,

        // Healing
        HEAL_RECEIVED,

        // Status
        EFFECT_APPLIED,
        EFFECT_EXPIRED,
        EFFECT_TICK,        // DOT damage tiap turn

        // Turn
        TURN_START,
        TURN_END,

        // Entity state
        ENTITY_DIED,
        ENTITY_FLED,

        // Skill
        SKILL_USED,
        SKILL_FAILED,       // tidak cukup MP, dll

        // Phase (untuk boss)
        BOSS_PHASE_CHANGE,
        BOSS_ENRAGE,

        // Combat flow
        COMBAT_START,
        COMBAT_END,

        // Mercenary
        MERCENARY_SYNERGY,
        MERCENARY_LOYALTY_UP,

        // CC
        ACTION_PREVENTED,   // stun/freeze/sleep mencegah aksi

        // Item / Artifact
        ITEM_USED,          // player pakai item (bonus action, tidak end turn)
        ARTIFACT_ACTIVATED, // artifact diaktifkan
    }

    // ── Fields ───────────────────────────────────────────────

    private final EventType        type;
    private final String           actorId;       // siapa melakukan
    private final String           actorName;
    private final String           targetId;      // ke siapa (bisa null)
    private final String           targetName;
    private final double           value;         // damage/heal amount
    private final DamageType       damageType;    // null jika bukan damage event
    private final StatusEffectType effectType;    // null jika bukan effect event
    private final String           skillId;       // null jika bukan skill event
    private final String           message;       // pesan teks untuk log

    // ── Builder pattern ──────────────────────────────────────

    private CombatEvent(Builder b) {
        this.type       = b.type;
        this.actorId    = b.actorId;
        this.actorName  = b.actorName;
        this.targetId   = b.targetId;
        this.targetName = b.targetName;
        this.value      = b.value;
        this.damageType = b.damageType;
        this.effectType = b.effectType;
        this.skillId    = b.skillId;
        this.message    = b.message;
    }

    public static class Builder {
        private EventType        type;
        private String           actorId    = "";
        private String           actorName  = "";
        private String           targetId   = "";
        private String           targetName = "";
        private double           value      = 0;
        private DamageType       damageType = null;
        private StatusEffectType effectType = null;
        private String           skillId    = null;
        private String           message    = "";

        public Builder(EventType type) { this.type = type; }

        public Builder actor(String id, String name)   { actorId = id; actorName = name; return this; }
        public Builder target(String id, String name)  { targetId = id; targetName = name; return this; }
        public Builder value(double v)                 { value = v; return this; }
        public Builder damageType(DamageType dt)       { damageType = dt; return this; }
        public Builder effectType(StatusEffectType et) { effectType = et; return this; }
        public Builder skillId(String sid)             { skillId = sid; return this; }
        public Builder message(String msg)             { message = msg; return this; }

        public CombatEvent build() { return new CombatEvent(this); }
    }

    // ── Static Factory Shortcuts ─────────────────────────────

    public static CombatEvent damage(String actorId, String actorName,
                                     String targetId, String targetName,
                                     double amount, DamageType type) {
        String msg = String.format("%s menyerang %s — %.0f damage %s",
                actorName, targetName, amount, type.displayName);
        return new Builder(EventType.DAMAGE_DEALT)
                .actor(actorId, actorName).target(targetId, targetName)
                .value(amount).damageType(type).message(msg).build();
    }

    public static CombatEvent shieldDamage(String actorId, String actorName,
                                           String targetId, String targetName,
                                           double totalDmg, double shieldAbsorbed,
                                           DamageType type) {
        String msg = String.format("%s menyerang %s — %.0f damage %s (🛡 %.0f diserap perisai)",
                actorName, targetName, totalDmg, type.displayName, shieldAbsorbed);
        return new Builder(EventType.DAMAGE_DEALT)
                .actor(actorId, actorName).target(targetId, targetName)
                .value(totalDmg).damageType(type).message(msg).build();
    }

    public static CombatEvent critDamage(String actorId, String actorName,
                                         String targetId, String targetName,
                                         double amount, DamageType type) {
        String msg = String.format("⚡ TEBASAN KERAS! %s menyerang %s — %.0f damage %s!",
                actorName, targetName, amount, type.displayName);
        return new Builder(EventType.CRITICAL_HIT)
                .actor(actorId, actorName).target(targetId, targetName)
                .value(amount).damageType(type).message(msg).build();
    }

    public static CombatEvent evaded(String actorId, String actorName,
                                     String targetId, String targetName) {
        return new Builder(EventType.DAMAGE_EVADED)
                .actor(actorId, actorName).target(targetId, targetName)
                .message(targetName + " menghindar dari serangan " + actorName + "!").build();
    }

    public static CombatEvent blocked(String actorId, String actorName,
                                      String targetId, String targetName, double amount) {
        return new Builder(EventType.DAMAGE_BLOCKED)
                .actor(actorId, actorName).target(targetId, targetName)
                .value(amount).message(targetName + " blocks! Reduced to " + (int)amount).build();
    }

    public static CombatEvent heal(String actorId, String actorName,
                                   String targetId, String targetName, double amount) {
        String msg = String.format("💚 %s heals %s for %.0f HP", actorName, targetName, amount);
        return new Builder(EventType.HEAL_RECEIVED)
                .actor(actorId, actorName).target(targetId, targetName)
                .value(amount).message(msg).build();
    }

    public static CombatEvent effectApplied(String actorId, String actorName,
                                            String targetId, String targetName,
                                            StatusEffectType effect) {
        String msg = String.format("%s applies %s to %s",
                actorName, effect.displayName, targetName);
        return new Builder(EventType.EFFECT_APPLIED)
                .actor(actorId, actorName).target(targetId, targetName)
                .effectType(effect).message(msg).build();
    }

    public static CombatEvent effectTick(String targetId, String targetName,
                                         StatusEffectType effect, double damage) {
        String msg = String.format("%s takes %.0f damage from %s",
                targetName, damage, effect.displayName);
        return new Builder(EventType.EFFECT_TICK)
                .target(targetId, targetName).effectType(effect).value(damage).message(msg).build();
    }

    public static CombatEvent death(String entityId, String entityName) {
        return new Builder(EventType.ENTITY_DIED)
                .actor(entityId, entityName)
                .message("💀 " + entityName + " telah dikalahkan!").build();
    }

    public static CombatEvent skillUsed(String actorId, String actorName, String skillId) {
        return new Builder(EventType.SKILL_USED)
                .actor(actorId, actorName).skillId(skillId)
                .message(actorName + " uses " + skillId).build();
    }

    public static CombatEvent actionPrevented(String entityId, String entityName, String reason) {
        return new Builder(EventType.ACTION_PREVENTED)
                .actor(entityId, entityName)
                .message(entityName + " cannot act: " + reason).build();
    }

    public static CombatEvent bossPhaseChange(String bossId, String bossName,
                                              int from, int to) {
        return new Builder(EventType.BOSS_PHASE_CHANGE)
                .actor(bossId, bossName)
                .message(String.format("⚠️ %s enters Phase %d!", bossName, to)).build();
    }

    public static CombatEvent bossEnrage(String bossId, String bossName) {
        return new Builder(EventType.BOSS_ENRAGE)
                .actor(bossId, bossName)
                .message("🔥 " + bossName + " ENRAGES! All stats surge!").build();
    }

    public static CombatEvent turnStart(String entityId, String entityName, int turnNumber) {
        return new Builder(EventType.TURN_START)
                .actor(entityId, entityName)
                .message(String.format("--- %s's turn (Turn %d) ---", entityName, turnNumber)).build();
    }

    // ── Getters ─────────────────────────────────────────────

    public EventType        getType()       { return type; }
    public String           getActorId()    { return actorId; }
    public String           getActorName()  { return actorName; }
    public String           getTargetId()   { return targetId; }
    public String           getTargetName() { return targetName; }
    public double           getValue()      { return value; }
    public DamageType       getDamageType() { return damageType; }
    public StatusEffectType getEffectType() { return effectType; }
    public String           getSkillId()    { return skillId; }
    public String           getMessage()    { return message; }

    @Override
    public String toString() { return message; }
}
