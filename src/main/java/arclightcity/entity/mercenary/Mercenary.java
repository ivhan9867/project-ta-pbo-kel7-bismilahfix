package arclightcity.entity.mercenary;
import arclightcity.combat.CombatManager;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.base.Entity;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * Base class semua Mercenary — sekutu AI yang bisa dibawa player ke dungeon.
 *
 * Fitur:
 *  - Role: TANK / DPS / SUPPORT / SPECIALIST
 *  - AI behavior disesuaikan role
 *  - Bisa diequip item sendiri (slot terpisah dari player)
 *  - Loyalty: makin sering dipakai → bonus stat kecil
 *  - Synergy: beberapa merc punya bonus jika dipasangkan bersama
 */
public abstract class Mercenary extends Entity {

    public enum Role { TANK, DPS, SUPPORT, SPECIALIST }

    // ── Identity ─────────────────────────────────────────────
    protected final MercenaryType mercenaryType;
    protected final Role          role;
    protected final String        lore;
    protected       int           hireCost;    // gold untuk sewa
    protected       int           loyaltyLevel = 0; // 0-10, naik setelah tiap dungeon
    protected       int           missionCount  = 0; // total misi selesai

    // ── Equipment Slots ──────────────────────────────────────
    protected String weaponItemId    = null;
    protected String armorItemId     = null;
    protected String accessoryItemId = null;

    // ── AI Config ─────────────────────────────────────────────
    protected double healThreshold   = 0.40; // HP % trigger heal behavior
    protected double aggressiveness  = 0.70; // 0-1, makin tinggi makin nekat

    // ── Constructor ─────────────────────────────────────────

    protected Mercenary(String name, String lore, MercenaryType type, Role role, int hireCost) {
        super(name, lore, EntityType.MERCENARY);
        this.mercenaryType = type;
        this.role          = role;
        this.lore          = lore;
        this.hireCost      = hireCost;
    }

    // ── Loyalty System ───────────────────────────────────────

    public void completeMission() {
        missionCount++;
        if (missionCount % 3 == 0 && loyaltyLevel < 10) {
            loyaltyLevel++;
            applyLoyaltyBonus();
        }
    }

    /**
     * Tiap level loyalty memberi bonus kecil ke stat merc.
     */
    private void applyLoyaltyBonus() {
        stats.addBase(StatType.MAX_HP,        8);
        stats.addBase(StatType.PHYSICAL_ATK,  2);
        stats.addBase(StatType.CYBER_ATK,     2);
        stats.addBase(StatType.SPEED,         0.5);
        // Subclass bisa override untuk bonus unik
        onLoyaltyLevelUp(loyaltyLevel);
    }

    protected void onLoyaltyLevelUp(int newLevel) { }

    // ── Synergy Check ────────────────────────────────────────

    /**
     * Cek apakah merc ini punya synergy dengan merc lain dalam party.
     * Dipanggil oleh CombatManager saat setup battle.
     */
    public void checkSynergy(List<Mercenary> partyMembers) {
        for (Mercenary other : partyMembers) {
            if (other == this) continue;
            applySynergyWith(other);
        }
    }

    protected void applySynergyWith(Mercenary other) { }

    // ── AI Core ──────────────────────────────────────────────

    @Override
    public CombatAction decideAction(List<Entity> allies, List<Entity> enemies) {
        // Cek apakah ada sekutu butuh heal
        if (role == Role.SUPPORT || role == Role.TANK) {
            Entity criticalAlly = findCriticalAlly(allies);
            if (criticalAlly != null && shouldHeal()) {
                return healAction(criticalAlly, allies, enemies);
            }
        }

        // Cek HP diri sendiri
        if (getHpPercent() < healThreshold && canSelfHeal()) {
            return selfHealAction(allies, enemies);
        }

        return combatAction(allies, enemies);
    }

    // ── Abstract AI Methods ──────────────────────────────────

    /** Aksi utama saat bertarung */
    protected abstract CombatAction combatAction(List<Entity> allies, List<Entity> enemies);

    /** Aksi heal ke sekutu (jika role support/tank) */
    protected CombatAction healAction(Entity target, List<Entity> allies, List<Entity> enemies) {
        return combatAction(allies, enemies); // default: override di support
    }

    /** Self heal jika HP kritis */
    protected CombatAction selfHealAction(List<Entity> allies, List<Entity> enemies) {
        return combatAction(allies, enemies); // default: override jika punya heal
    }

    protected boolean shouldHeal()   { return false; }
    protected boolean canSelfHeal()  { return false; }

    // ── Helper ───────────────────────────────────────────────

    protected Entity findCriticalAlly(List<Entity> allies) {
        return allies.stream()
                .filter(a -> a.isAlive() && a.getHpPercent() < healThreshold)
                .min(java.util.Comparator.comparingDouble(Entity::getHpPercent))
                .orElse(null);
    }

    protected Entity getLowestHpEnemy(List<Entity> enemies) {
        return enemies.stream().filter(Entity::isAlive)
                .min(java.util.Comparator.comparingDouble(Entity::getHpPercent))
                .orElse(null);
    }

    protected Entity getRandomEnemy(List<Entity> enemies) {
        List<Entity> alive = enemies.stream().filter(Entity::isAlive).toList();
        if (alive.isEmpty()) return null;
        return alive.get((int)(Math.random() * alive.size()));
    }

    // ── Equipment ────────────────────────────────────────────

    public void equipWeapon(String itemId)    { this.weaponItemId    = itemId; }
    public void equipArmor(String itemId)     { this.armorItemId     = itemId; }
    public void equipAccessory(String itemId) { this.accessoryItemId = itemId; }
    public void unequipWeapon()               { this.weaponItemId    = null; }
    public void unequipArmor()                { this.armorItemId     = null; }
    public void unequipAccessory()            { this.accessoryItemId = null; }

    // ── Getters ─────────────────────────────────────────────

    public MercenaryType getMercenaryType() { return mercenaryType; }
    public Role          getRole()          { return role; }
    public String        getLore()          { return lore; }
    public int           getHireCost()      { return hireCost; }
    public int           getLoyaltyLevel()  { return loyaltyLevel; }
    public int           getMissionCount()  { return missionCount; }
    public String        getWeaponItemId()  { return weaponItemId; }
    public String        getArmorItemId()   { return armorItemId; }

    public String getLoyaltyTitle() {
        return switch (loyaltyLevel) {
            case 0, 1 -> "Stranger";
            case 2, 3 -> "Acquaintance";
            case 4, 5 -> "Ally";
            case 6, 7 -> "Trusted";
            case 8, 9 -> "Bonded";
            case 10   -> "Soul Sync";
            default   -> "Unknown";
        };
    }

    // ── Save/Load helpers ─────────────────────────────────────

    /** Set loyalty langsung tanpa trigger level up (untuk restore dari save) */
    public void setLoyaltyDirect(int level) {
        this.loyaltyLevel = Math.max(0, Math.min(10, level));
    }

    /** Restore vitals dari save — set HP/MP/Shield langsung */
    public void restoreVitals(double hp, double mp, double shield) {
        double maxHp     = getStats().get(arclightcity.entity.stats.StatType.MAX_HP);
        double maxMp     = getStats().get(arclightcity.entity.stats.StatType.MAX_MP);
        double maxShield = getStats().get(arclightcity.entity.stats.StatType.MAX_SHIELD);
        this.currentHp     = Math.min(hp,     maxHp);
        this.currentMp     = Math.min(mp,     maxMp);
        this.currentShield = Math.min(shield, maxShield);
    }
}
