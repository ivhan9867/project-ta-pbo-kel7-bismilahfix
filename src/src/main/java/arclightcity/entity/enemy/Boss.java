package arclightcity.entity.enemy;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;


import java.util.ArrayList;
import java.util.List;

/**
 * Base class semua BOSS di game.
 *
 * Mechanic Boss:
 *  - Multi-phase: 2-3 fase berbeda dengan AI dan stat yang berubah
 *  - Phase Health: Boss masuk fase baru saat HP mencapai threshold
 *  - Enrage: jika battle terlalu lama, Boss masuk Enrage mode
 *  - Unique Mechanic: setiap boss punya mechanic yang tidak ada di enemy biasa
 */
public abstract class Boss extends Enemy {

    // ── Phase System ─────────────────────────────────────────
    protected int            currentPhase      = 1;
    protected final int      totalPhases;
    protected final double[] phaseThresholds;  // HP% trigger masuk fase baru [0.70, 0.40]
    protected final List<Boolean> phaseTriggered = new ArrayList<>();

    // ── Enrage System ────────────────────────────────────────
    protected boolean enraged         = false;
    protected int     enrageTurnLimit = 20; // boss enrage setelah N turn
    protected int     turnElapsed     = 0;

    // ── Boss Info ─────────────────────────────────────────────
    protected String  uniqueMechanicName;
    protected String  uniqueMechanicDesc;
    protected boolean defeatFirst = false; // apakah player ini pertama kali kalahkan boss ini

    // ── Constructor ─────────────────────────────────────────

    protected Boss(String name, String description,
                   EnemyRace race, ThreatLevel threatLevel,
                   String lootTableId, int totalPhases, double[] phaseThresholds) {
        super(name, description, EntityType.BOSS, race, threatLevel, lootTableId);

        this.totalPhases     = totalPhases;
        this.phaseThresholds = phaseThresholds;

        // init phase triggered list
        for (int i = 0; i < totalPhases - 1; i++) {
            phaseTriggered.add(false);
        }
    }

    // ── Turn Lifecycle ───────────────────────────────────────

    @Override
    public void onTurnStart() {
        super.onTurnStart();
        turnElapsed++;
        checkPhaseTransition();
        checkEnrage();
    }

    // ── Phase System ─────────────────────────────────────────

    protected void checkPhaseTransition() {
        double hpPct = getHpPercent();
        for (int i = 0; i < phaseThresholds.length; i++) {
            if (hpPct <= phaseThresholds[i] && !phaseTriggered.get(i)) {
                phaseTriggered.set(i, true);
                int newPhase = i + 2; // phase 2, 3, dst
                onPhaseTransition(currentPhase, newPhase);
                currentPhase = newPhase;
                break;
            }
        }
    }

    /**
     * Dipanggil saat transisi ke fase baru.
     * Override di subclass untuk mechanic per-fase.
     */
    protected abstract void onPhaseTransition(int fromPhase, int toPhase);

    // ── Enrage System ─────────────────────────────────────────

    private void checkEnrage() {
        if (!enraged && turnElapsed >= enrageTurnLimit) {
            enraged = true;
            onEnrage();
        }
    }

    /**
     * Dipanggil saat boss enrage — stat meningkat drastis.
     * Default implementation bisa di-override.
     */
    protected void onEnrage() {
        stats.addBase(StatType.PHYSICAL_ATK, stats.get(StatType.PHYSICAL_ATK) * 0.50);
        stats.addBase(StatType.CYBER_ATK,    stats.get(StatType.CYBER_ATK)    * 0.50);
        stats.addBase(StatType.ENERGY_ATK,   stats.get(StatType.ENERGY_ATK)   * 0.50);
        stats.addBase(StatType.SPEED,        10);
        stats.addBase(StatType.CRIT_CHANCE,  0.20);
    }

    // ── Getters ─────────────────────────────────────────────

    public int     getCurrentPhase()       { return currentPhase; }
    public int     getTotalPhases()        { return totalPhases; }
    public boolean isEnraged()             { return enraged; }
    public int     getTurnElapsed()        { return turnElapsed; }
    public String  getUniqueMechanicName() { return uniqueMechanicName; }
    public String  getUniqueMechanicDesc() { return uniqueMechanicDesc; }
}
