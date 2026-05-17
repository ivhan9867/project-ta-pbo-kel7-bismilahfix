package arclightcity.item;

import java.io.Serializable;

/**
 * Artifact — item khusus dari sistem gacha.
 *
 * Berbeda dari Equipment:
 * - Tidak di-upgrade
 * - Tidak ada base stat
 * - Aktif berdasarkan Cooldown (bukan MP)
 * - Aktivasi TIDAK memakan turn
 * - Setiap tier (rarity) memperkuat nilai dan durasi efek
 */
public class Artifact extends Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ArtifactType type;
    private int cooldownRemaining = 0;  // 0 = siap dipakai

    public Artifact(ArtifactType type, Rarity rarity) {
        super(generateId(type, rarity), type.displayName, ItemType.ARTIFACT, rarity);
        this.type = type;
    }

    private static String generateId(ArtifactType t, Rarity r) {
        return "ARTIFACT_" + t.name() + "_" + r.name() + "_" + (int)(System.nanoTime() % 100000);
    }

    // ── Cooldown ──────────────────────────────────────────────
    public boolean isReady()  { return cooldownRemaining <= 0; }
    public int  getCooldown() { return cooldownRemaining; }

    /** Dipanggil setiap awal giliran entity ini */
    public void tickCooldown() {
        if (cooldownRemaining > 0) cooldownRemaining--;
    }

    /** Dipanggil setelah artifact diaktifkan */
    public void activate() {
        cooldownRemaining = getScaledCooldown();
    }

    // ── Scaled values per rarity ──────────────────────────────
    private double rarityMult() {
        return switch (getRarity()) {
            case COMMON    -> 1.0;
            case UNCOMMON  -> 1.25;
            case RARE      -> 1.55;
            case EPIC      -> 1.90;
            case LEGENDARY -> 2.35;
            case MYTHIC    -> 2.80;
        };
    }

    private int rarityDurBonus() {
        return switch (getRarity()) {
            case COMMON, UNCOMMON -> 0;
            case RARE             -> 0;
            case EPIC             -> 1;
            case LEGENDARY        -> 1;
            case MYTHIC           -> 2;
        };
    }

    /** Nilai efek yang sudah discale dengan rarity */
    public double getScaledValue() {
        return type.baseValue * rarityMult();
    }

    /** Durasi efek yang sudah discale */
    public int getScaledDuration() {
        return Math.max(1, type.baseDuration + rarityDurBonus());
    }

    /** CD setelah aktivasi — MYTHIC sedikit lebih pendek */
    public int getScaledCooldown() {
        int base = type.baseCooldown;
        return switch (getRarity()) {
            case LEGENDARY -> Math.max(3, base - 1);
            case MYTHIC    -> Math.max(2, base - 2);
            default        -> base;
        };
    }

    // ── Getters ───────────────────────────────────────────────
    public ArtifactType getArtifactType() { return type; }

    @Override
    public String getDisplaySummary() {
        return getRarity().displayName + " Artifact | CD: " + getScaledCooldown() + " turn | "
               + type.description;
    }

    @Override
    public String getFullName() {
        return "[" + getRarity().displayName + "] " + type.displayName;
    }

    @Override
    public java.util.Map<arclightcity.entity.stats.StatType, Double> getStatBonuses() {
        return java.util.Collections.emptyMap(); // Artifact tidak punya stat pasif
    }

    /** Path relatif icon artefak ini dalam assets/ */
    public String iconPath() {
        return "icons/artifact/icon_artifact_" + type.name().toLowerCase() + ".png";
    }

    /** Warna border berdasarkan rarity, untuk UI */
    public String getBorderColor() {
        return switch (getRarity()) {
            case COMMON    -> "#505050";
            case UNCOMMON  -> "#2D7A3A";
            case RARE      -> "#2D5A9A";
            case EPIC      -> "#8A2D9A";
            case LEGENDARY -> "#C8860A";
            case MYTHIC    -> "#CC2244";
        };
    }

    /** True jika rarity cukup tinggi untuk efek glow */
    public boolean hasGlowEffect() {
        return getRarity().ordinal() >= Rarity.EPIC.ordinal();
    }
}
