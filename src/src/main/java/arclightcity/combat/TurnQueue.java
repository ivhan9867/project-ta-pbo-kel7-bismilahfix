package arclightcity.combat;
import arclightcity.entity.base.Entity;
import arclightcity.entity.stats.StatType;


import java.util.*;

/**
 * TurnQueue — mengelola urutan giliran dalam combat.
 *
 * Sistem: Initiative-based Speed
 *   - Setiap entity punya "initiative counter"
 *   - Tiap tick: counter += SPEED
 *   - Saat counter ≥ 100 → giliran entity tersebut
 *   - Setelah giliran: counter -= 100
 *
 * Sistem ini lebih dinamis dari round-based karena:
 *   - Entity cepat bisa dapat giliran 2x sebelum entity lambat 1x
 *   - Speed buff/debuff langsung berpengaruh pada giliran berikutnya
 */
public class TurnQueue {

    // ── Inner class: slot satu entity di queue ────────────────
    public static class TurnSlot {
        public final Entity entity;
        public double       initiative; // SPEED stat snapshot saat init

        public TurnSlot(Entity entity) {
            this.entity     = entity;
            this.initiative = entity.getStats().get(StatType.SPEED)
                            + entity.getStats().get(StatType.INITIATIVE)
                            + (Math.random() * 5); // sedikit randomness untuk tie-break
        }
    }

    // ── Fields ───────────────────────────────────────────────
    private final List<TurnSlot>    allSlots    = new ArrayList<>();
    private final List<Entity>      turnOrder   = new ArrayList<>(); // urutan satu round
    private int                     currentIndex = 0;
    private int                     roundNumber  = 0;
    private final Map<String, Double> actionPoints = new HashMap<>(); // entityId → AP

    // ── Setup ────────────────────────────────────────────────

    /**
     * Inisialisasi queue dengan semua combatant.
     * Dipanggil sekali di awal combat.
     */
    public void initialize(List<Entity> allies, List<Entity> enemies) {
        allSlots.clear();
        actionPoints.clear();

        for (Entity e : allies)  addCombatant(e);
        for (Entity e : enemies) addCombatant(e);

        buildTurnOrder();
    }

    public void addCombatant(Entity entity) {
        allSlots.add(new TurnSlot(entity));
        actionPoints.put(entity.getId(), 0.0);
    }

    public void removeCombatant(String entityId) {
        allSlots.removeIf(slot -> slot.entity.getId().equals(entityId));
        actionPoints.remove(entityId);
    }

    // ── Turn Order Builder ────────────────────────────────────

    /**
     * Bangun urutan turn untuk satu round.
     * Dijalankan di awal tiap round baru.
     */
    private void buildTurnOrder() {
        roundNumber++;
        turnOrder.clear();
        currentIndex = 0;

        // Tick action points semua entity
        for (TurnSlot slot : allSlots) {
            if (!slot.entity.isAlive()) continue;
            double speed = slot.entity.getStats().get(StatType.SPEED);
            double ap    = actionPoints.getOrDefault(slot.entity.getId(), 0.0);
            ap += speed;
            actionPoints.put(slot.entity.getId(), ap);
        }

        // Entity dengan AP ≥ 100 → dapat giliran, subtract 100
        List<TurnSlot> readySlots = new ArrayList<>();
        for (TurnSlot slot : allSlots) {
            if (!slot.entity.isAlive()) continue;
            double ap = actionPoints.getOrDefault(slot.entity.getId(), 0.0);
            if (ap >= 100) {
                readySlots.add(slot);
                actionPoints.put(slot.entity.getId(), ap - 100);
            }
        }

        // Jika tidak ada yang ready → tick lagi (untuk entity sangat lambat)
        if (readySlots.isEmpty()) {
            // Force semua entity untuk dapat giliran minimal 1 kali per round
            for (TurnSlot slot : allSlots) {
                if (slot.entity.isAlive()) {
                    readySlots.add(slot);
                }
            }
        }

        // Sort berdasarkan initiative (speed + bonus + sedikit random)
        readySlots.sort((a, b) -> {
            double aSpeed = a.entity.getStats().get(StatType.SPEED) + a.initiative;
            double bSpeed = b.entity.getStats().get(StatType.SPEED) + b.initiative;
            return Double.compare(bSpeed, aSpeed); // descending
        });

        for (TurnSlot slot : readySlots) {
            turnOrder.add(slot.entity);
        }
    }

    // ── Turn Navigation ──────────────────────────────────────

    /**
     * Ambil entity yang sekarang giliran-nya.
     * @return entity berikutnya yang harus bertindak, atau null jika round selesai
     */
    public Entity getCurrentTurnEntity() {
        // Skip entity mati
        while (currentIndex < turnOrder.size() && !turnOrder.get(currentIndex).isAlive()) {
            currentIndex++;
        }
        if (currentIndex >= turnOrder.size()) return null;
        return turnOrder.get(currentIndex);
    }

    /**
     * Maju ke entity berikutnya dalam round.
     * @return true jika masih ada entity tersisa di round ini, false jika round selesai
     */
    public boolean advance() {
        currentIndex++;
        // Skip yang mati
        while (currentIndex < turnOrder.size() && !turnOrder.get(currentIndex).isAlive()) {
            currentIndex++;
        }
        return currentIndex < turnOrder.size();
    }

    /**
     * Cek apakah round saat ini sudah selesai.
     */
    public boolean isRoundComplete() {
        return getCurrentTurnEntity() == null;
    }

    /**
     * Mulai round baru. Rebuild turn order.
     */
    public void startNewRound() {
        buildTurnOrder();
    }

    // ── Utility ─────────────────────────────────────────────

    /** Ambil snapshot urutan turn untuk ditampilkan di UI */
    public List<Entity> getTurnOrderSnapshot() {
        return Collections.unmodifiableList(turnOrder);
    }

    public int  getRoundNumber() { return roundNumber; }
    public int  getCurrentIndex(){ return currentIndex; }

    /**
     * Berapa entity yang masih hidup dari sisi ini?
     */
    public long countAlive(List<Entity> group) {
        return group.stream().filter(Entity::isAlive).count();
    }
}
