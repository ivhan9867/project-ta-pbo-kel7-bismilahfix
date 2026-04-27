package arclightcity.entity.status;

/**
 * Satu instance aktif dari StatusEffect pada sebuah entity.
 * Menyimpan: tipe, sisa durasi, kekuatan, dan siapa yang memasang effect ini.
 */
public class StatusEffect {

    private final StatusEffectType type;
    private int    remainingTurns;
    private double power;          // damage DOT, stat reduction amount, dll
    private int    stackCount;     // beberapa effect bisa stack (misal Burn stack 3x)
    private final String sourceId; // ID entity yang memasang effect ini

    // ── Constructor ─────────────────────────────────────────

    public StatusEffect(StatusEffectType type, int durationTurns, double power, String sourceId) {
        this.type           = type;
        this.remainingTurns = durationTurns;
        this.power          = power;
        this.stackCount     = 1;
        this.sourceId       = sourceId;
    }

    // ── Turn Lifecycle ───────────────────────────────────────

    /**
     * Dipanggil di akhir tiap turn.
     * @return true jika effect masih aktif, false jika sudah habis
     */
    public boolean tick() {
        remainingTurns--;
        return remainingTurns > 0;
    }

    public boolean isExpired() {
        return remainingTurns <= 0;
    }

    /**
     * Refresh durasi (misal tertimpa effect sama lagi).
     * Jika bisa stack → tambah stack, power naik.
     * Jika tidak → hanya refresh durasi.
     */
    public void refresh(int newDuration, double additionalPower, int maxStacks) {
        this.remainingTurns = newDuration;
        if (stackCount < maxStacks) {
            stackCount++;
            this.power += additionalPower;
        }
    }

    // ── Damage Calculation (untuk DOT) ───────────────────────

    /**
     * Hitung damage DOT per tick berdasarkan stack dan power.
     */
    public double calculateDotDamage() {
        return power * stackCount;
    }

    // ── Getters ─────────────────────────────────────────────

    public StatusEffectType getType()           { return type; }
    public int    getRemainingTurns()           { return remainingTurns; }
    public double getPower()                    { return power; }
    public int    getStackCount()               { return stackCount; }
    public String getSourceId()                 { return sourceId; }

    @Override
    public String toString() {
        return String.format("%s [%d turn(s), stack:%d, power:%.1f]",
                type.displayName, remainingTurns, stackCount, power);
    }
}
