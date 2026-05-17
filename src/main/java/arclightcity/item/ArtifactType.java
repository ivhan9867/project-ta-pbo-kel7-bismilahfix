package arclightcity.item;

import arclightcity.entity.stats.StatType;
import arclightcity.entity.status.StatusEffectType;

/**
 * Semua jenis artefak yang bisa didapat dari gacha.
 * Setiap entry mendefinisikan: nama, deskripsi, role, efek, base CD.
 *
 * Efek dilambangkan dengan:
 *   effectStat  → StatType boost (null jika bukan stat boost)
 *   statusType  → StatusEffectType (null jika bukan status)
 *   mode        → cara apply efek
 *   baseValue   → nilai dasar (diskala per rarity di Artifact)
 *   baseDuration→ durasi turn dasar
 *   baseCooldown→ cooldown setelah dipakai
 */
public enum ArtifactType {

    // ── UNIVERSAL (semua karakter bisa pakai) ─────────────────
    VESTIGE_OF_KALA(
        "Vestige of Kala",
        "Boosts ATK for 3 turns.",
        ArtifactRole.UNIVERSAL, Mode.STAT_SELF,
        StatType.PHYSICAL_ATK, null, 0.18, 3, 5),

    TEAR_OF_SEA(
        "Tear of the Sea",
        "Restores HP based on max HP.",
        ArtifactRole.UNIVERSAL, Mode.HEAL_SELF,
        null, null, 0.20, 0, 6),

    SWIFT_WIND(
        "Swift Wind",
        "Gain SPEED boost for 2 turns.",
        ArtifactRole.UNIVERSAL, Mode.STAT_SELF,
        StatType.SPEED, null, 0.25, 2, 4),

    BARRIER_STONE(
        "Barrier Stone",
        "Apply BARRIER shield.",
        ArtifactRole.UNIVERSAL, Mode.STATUS_SELF,
        null, StatusEffectType.BARRIER, 0.15, 3, 6),

    CRYSTAL_FOCUS(
        "Crystal Focus",
        "Boost SKILL_POWER for 2 turns.",
        ArtifactRole.UNIVERSAL, Mode.STAT_SELF,
        StatType.SKILL_POWER, null, 0.30, 2, 5),

    // ── TANK ───────────────────────────────────────────────────
    IRON_SKIN(
        "Iron Skin",
        "Massively boost DEF for 3 turns.",
        ArtifactRole.TANK, Mode.STAT_SELF,
        StatType.PHYSICAL_DEF, null, 0.40, 3, 5),

    THORN_AURA(
        "Thorn Aura",
        "Apply THORN to reflect damage.",
        ArtifactRole.TANK, Mode.STAT_SELF,
        StatType.THORN, null, 0.28, 3, 4),

    STALWART_AEGIS(
        "Stalwart Aegis",
        "Apply FORTIFY stance for 3 turns.",
        ArtifactRole.TANK, Mode.STATUS_SELF,
        null, StatusEffectType.FORTIFY, 0.0, 3, 5),

    // ── HEALER ─────────────────────────────────────────────────
    SPRING_VIAL(
        "Spring Vial",
        "Heal self for 25% max HP.",
        ArtifactRole.HEALER, Mode.HEAL_SELF,
        null, null, 0.25, 0, 5),

    PURIFY_ORB(
        "Purify Orb",
        "Cleanse all debuffs from party.",
        ArtifactRole.HEALER, Mode.CLEANSE_PARTY,
        null, null, 0.0, 0, 7),

    OVERCHARGE(
        "Overcharge",
        "Apply strong REGEN for 3 turns.",
        ArtifactRole.HEALER, Mode.STATUS_SELF,
        null, StatusEffectType.REGEN, 0.10, 3, 5),

    // ── DPS ────────────────────────────────────────────────────
    BLOODFANG(
        "Bloodfang",
        "Boost CRIT DAMAGE for 2 turns.",
        ArtifactRole.DPS, Mode.STAT_SELF,
        StatType.CRIT_DAMAGE, null, 0.55, 2, 5),

    EXPOSE_MARK(
        "Expose Mark",
        "Apply WEAKEN to random enemy.",
        ArtifactRole.DPS, Mode.STATUS_ENEMY,
        null, StatusEffectType.WEAKEN, 0.0, 3, 4),

    PHANTOM_EDGE(
        "Phantom Edge",
        "Boost ARMOR PIERCE for 2 turns.",
        ArtifactRole.DPS, Mode.STAT_SELF,
        StatType.ARMOR_PIERCE, null, 0.40, 2, 5),

    // ── SUPPORT ────────────────────────────────────────────────
    EMP_SHARD(
        "EMP Shard",
        "Freeze a random enemy.",
        ArtifactRole.SUPPORT, Mode.STATUS_ENEMY,
        null, StatusEffectType.FREEZE, 0.0, 2, 6),

    BLOOM_AURA(
        "Bloom Aura",
        "Empower all party members for 2 turns.",
        ArtifactRole.SUPPORT, Mode.STATUS_PARTY,
        null, StatusEffectType.EMPOWERED, 0.0, 2, 7),

    // ── ASSASSIN ───────────────────────────────────────────────
    SHADOW_VEIL(
        "Shadow Veil",
        "Massively boost EVASION for 2 turns.",
        ArtifactRole.ASSASSIN, Mode.STAT_SELF,
        StatType.EVASION, null, 0.50, 2, 4),

    DEATH_MARK(
        "Death Mark",
        "Apply BLEED to random enemy.",
        ArtifactRole.ASSASSIN, Mode.STATUS_ENEMY,
        null, StatusEffectType.BLEED, 8.0, 3, 4),

    // ── BREAKER ────────────────────────────────────────────────
    OVERDRIVE(
        "Overdrive",
        "Boost DAMAGE MULT for 2 turns.",
        ArtifactRole.BREAKER, Mode.STAT_SELF,
        StatType.DAMAGE_MULT, null, 0.30, 2, 5),

    SEISMIC_CORE(
        "Seismic Core",
        "Stun a random enemy for 1 turn.",
        ArtifactRole.BREAKER, Mode.STATUS_ENEMY,
        null, StatusEffectType.STUN, 0.0, 1, 6);

    // ── Effect execution mode ─────────────────────────────────
    public enum Mode {
        STAT_SELF,      // temp stat boost pada diri sendiri
        HEAL_SELF,      // langsung pulihkan HP (baseValue * maxHP)
        STATUS_SELF,    // apply StatusEffect pada diri sendiri
        STATUS_ENEMY,   // apply StatusEffect ke musuh acak
        STATUS_PARTY,   // apply StatusEffect ke semua ally
        CLEANSE_PARTY   // hapus semua debuff dari party
    }

    // ── Properties ───────────────────────────────────────────
    public final String          displayName;
    public final String          description;
    public final ArtifactRole    role;
    public final Mode            mode;
    public final StatType        statType;     // null jika bukan stat boost
    public final arclightcity.entity.status.StatusEffectType statusType;
    public final double          baseValue;    // discale dengan rarity
    public final int             baseDuration; // turns
    public final int             baseCooldown; // turns setelah dipakai

    ArtifactType(String n, String d, ArtifactRole r, Mode m,
                 StatType st, arclightcity.entity.status.StatusEffectType se,
                 double bv, int bd, int bc) {
        displayName = n; description = d; role = r; mode = m;
        statType = st; statusType = se; baseValue = bv;
        baseDuration = bd; baseCooldown = bc;
    }
}
