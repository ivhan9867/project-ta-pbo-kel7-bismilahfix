package arclightcity.entity.player;
import arclightcity.combat.CombatManager;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.base.Entity;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * Player — karakter yang dikontrol user.
 *
 * Fitur utama:
 *  - Classless: bisa unlock skill dari pool manapun
 *  - Level up → dapat Skill Point dan stat increase
 *  - Equip hingga 4 skill aktif masuk dungeon
 *  - Background asal menentukan bonus stat awal
 */
public class Player extends Entity {

    // ── Progression ──────────────────────────────────────────
    private int    level        = 1;
    private double currentExp   = 0;
    private double expToNextLevel;
    private int    skillPoints  = 0;
    private int    statPoints   = 0;
    private long   gold         = 0;
    private int    dungeonDepth = 0; // floor terdalam yang pernah dicapai

    // ── Origin Background ─────────────────────────────────────
    private final PlayerBackground background;

    // ── Skill System ─────────────────────────────────────────
    // Diisi oleh SkillManager — placeholder String ID
    private final List<String> unlockedSkillIds  = new java.util.ArrayList<>();
    private final List<String> equippedSkillIds  = new java.util.ArrayList<>(); // max 4
    private final java.util.Map<String, Integer> skillCooldowns = new java.util.HashMap<>();

    // ── Inventory ────────────────────────────────────────────
    // Diisi oleh InventoryManager
    private final java.util.Map<String, String> equippedItems = new java.util.HashMap<>();
    // slot → itemId: "WEAPON", "ARMOR", "ACCESSORY_1", "ACCESSORY_2"

    // ── Constructor ─────────────────────────────────────────

    public Player(String name, PlayerBackground background) {
        super(name, "Player Character", EntityType.PLAYER);
        this.background = background;
        initStats();
        applyBackgroundBonus();
        expToNextLevel = calculateExpRequired(1);
        initVitals();
    }

    // ── Init Stats ───────────────────────────────────────────

    @Override
    protected void initStats() {
        // Stat base level 1 (semua background mulai dari sini)
        stats.setBase(StatType.MAX_HP,       120);
        stats.setBase(StatType.MAX_MP,       60);
        stats.setBase(StatType.HP_REGEN,     2);
        stats.setBase(StatType.MP_REGEN,     3);

        // Shield — player mulai dengan shield kecil
        stats.setBase(StatType.MAX_SHIELD,   40);
        stats.setBase(StatType.SHIELD_REGEN, 0);  // default 0, naik dari equipment
        stats.setBase(StatType.SHIELD_MULT,  0);

        stats.setBase(StatType.PHYSICAL_ATK, 15);
        stats.setBase(StatType.CYBER_ATK,    10);
        stats.setBase(StatType.ENERGY_ATK,   10);
        stats.setBase(StatType.DAMAGE_MULT,  0.0); // 0% bonus, naik dari equipment/skill
        stats.setBase(StatType.CRIT_CHANCE,  0.05);
        stats.setBase(StatType.CRIT_DAMAGE,  1.5);
        stats.setBase(StatType.ARMOR_PIERCE, 0.0);
        stats.setBase(StatType.LIFESTEAL,    0.0);

        stats.setBase(StatType.PHYSICAL_DEF, 10);
        stats.setBase(StatType.CYBER_DEF,    8);
        stats.setBase(StatType.ENERGY_DEF,   8);
        stats.setBase(StatType.EVASION,      0.05);
        stats.setBase(StatType.BLOCK_CHANCE, 0.0);
        stats.setBase(StatType.TENACITY,     0.0);

        stats.setBase(StatType.SPEED,        12);
        stats.setBase(StatType.INITIATIVE,   5);
        stats.setBase(StatType.ACCURACY,     0.92);
        stats.setBase(StatType.SKILL_POWER,  1.0);
        stats.setBase(StatType.COOLDOWN_REDUCE, 0.0);
    }

    private void applyBackgroundBonus() {
        background.applyBaseStats(stats);
    }

    // ── Level Up ─────────────────────────────────────────────

    /**
     * Tambah EXP. Jika cukup, level up otomatis (bisa multi-level).
     * @return jumlah level yang naik
     */
    public int gainExp(double amount) {
        currentExp += amount;
        int levelsGained = 0;

        while (currentExp >= expToNextLevel) {
            currentExp     -= expToNextLevel;
            level++;
            levelsGained++;
            expToNextLevel  = calculateExpRequired(level);
            onLevelUp();
        }

        return levelsGained;
    }

    private void onLevelUp() {
        skillPoints++;
        statPoints += 2;

        // Auto stat scale per level
        stats.addBase(StatType.MAX_HP,       15);
        stats.addBase(StatType.MAX_MP,       5);
        stats.addBase(StatType.MAX_SHIELD,   8);  // shield juga scale per level
        stats.addBase(StatType.PHYSICAL_ATK, 2);
        stats.addBase(StatType.CYBER_ATK,    2);
        stats.addBase(StatType.ENERGY_ATK,   2);
        stats.addBase(StatType.PHYSICAL_DEF, 1.5);
        stats.addBase(StatType.CYBER_DEF,    1.2);
        stats.addBase(StatType.ENERGY_DEF,   1.2);
        stats.addBase(StatType.SPEED,        0.5);

        // HP, Shield, dan MP penuh saat level up
        initVitals();
    }

    /**
     * EXP formula: 100 × level^1.6 (growth sedang)
     */
    private double calculateExpRequired(int currentLevel) {
        return Math.round(100 * Math.pow(currentLevel, 1.6));
    }

    // ── Skill Management ─────────────────────────────────────

    /** Unlock skill tanpa perlu skillPoints — untuk starter skill dari background */
    public void forceUnlockSkill(String skillId) {
        if (!unlockedSkillIds.contains(skillId)) {
            unlockedSkillIds.add(skillId);
        }
    }

    public java.util.List<String> getEquippedSkillIds()  {
        return java.util.Collections.unmodifiableList(equippedSkillIds);
    }
    public java.util.List<String> getUnlockedSkillIds()  {
        return java.util.Collections.unmodifiableList(unlockedSkillIds);
    }
    public boolean hasUnlockedSkill(String skillId) {
        return unlockedSkillIds.contains(skillId);
    }

    public boolean isSkillEquipped(String skillId) {
        return equippedSkillIds.contains(skillId);
    }

    public int getEquippedSkillCount() {
        return equippedSkillIds.size();
    }

    /** Spend skill points (untuk SkillTree) */
    public boolean spendSkillPoint(int cost) {
        if (skillPoints < cost) return false;
        skillPoints -= cost;
        return true;
    }

    // ── Save/Load direct setters ──────────────────────────────

    /** Untuk restore dari save — set level langsung tanpa trigger level up */
    public void setLevelDirect(int level)                          { this.level = level; }
    public void setExpDirect(double exp, double toNext)            { this.currentExp = exp; this.expToNextLevel = toNext; }
    public void setGold(long gold)                                 { this.gold = Math.max(0, gold); }
    public void addGold(long amount)                              { this.gold += amount; }
    public void fullRestore() {
        this.currentHp     = getStats().get(arclightcity.entity.stats.StatType.MAX_HP);
        this.currentMp     = getStats().get(arclightcity.entity.stats.StatType.MAX_MP);
        this.currentShield = getStats().get(arclightcity.entity.stats.StatType.MAX_SHIELD);
    }

    public void setHpDirect(double hp)                             { this.currentHp = Math.min(hp, stats.get(StatType.MAX_HP)); }
    public void setMpDirect(double mp)                             { this.currentMp = Math.min(mp, stats.get(StatType.MAX_MP)); }
    public void setShieldDirect(double shield)                     { this.currentShield = Math.min(shield, stats.get(StatType.MAX_SHIELD)); }
    public void setSkillPointsDirect(int pts)                      { this.skillPoints = pts; }

    public boolean unlockSkill(String skillId) {
        if (skillPoints <= 0 || unlockedSkillIds.contains(skillId)) return false;
        unlockedSkillIds.add(skillId);
        skillPoints--;
        return true;
    }

    public boolean equipSkill(String skillId) {
        if (!unlockedSkillIds.contains(skillId)) return false;
        if (equippedSkillIds.size() >= 4) return false;
        if (equippedSkillIds.contains(skillId)) return false;
        equippedSkillIds.add(skillId);
        return true;
    }

    public boolean unequipSkill(String skillId) {
        return equippedSkillIds.remove(skillId);
    }

    public void tickSkillCooldowns() {
        skillCooldowns.replaceAll((id, cd) -> Math.max(0, cd - 1));
    }

    public void setSkillOnCooldown(String skillId, int turns) {
        skillCooldowns.put(skillId, turns);
    }

    public int getSkillCooldown(String skillId) {
        return skillCooldowns.getOrDefault(skillId, 0);
    }

    public boolean isSkillReady(String skillId) {
        return getSkillCooldown(skillId) == 0 && equippedSkillIds.contains(skillId);
    }

    // ── Combat Decision ──────────────────────────────────────

    /**
     * Player dikontrol user — decideAction() tidak dipakai langsung.
     * Dikembalikan dari input UI di JavaFX.
     * Method ini ada karena abstract contract, tapi untuk Player
     * CombatManager akan memanggil aksi dari input user.
     */
    @Override
    public CombatAction decideAction(List<Entity> allies, List<Entity> enemies) {
        // Player controlled — handled by UI
        return CombatAction.pass();
    }

    // ── Gold ────────────────────────────────────────────────

    public boolean spendGold(long amount) {
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }

    public void gainGold(long amount) {
        gold += amount;
    }

    // ── Getters & Setters ────────────────────────────────────

    public int               getLevel()             { return level; }
    public double            getCurrentExp()        { return currentExp; }
    public double            getExpToNextLevel()    { return expToNextLevel; }
    public double            getExpPercent()        { return currentExp / expToNextLevel; }
    public int               getSkillPoints()       { return skillPoints; }
    public int               getStatPoints()        { return statPoints; }
    public void              setStatPoints(int v)   { statPoints = v; }
    public long              getGold()              { return gold; }
    public int               getDungeonDepth()      { return dungeonDepth; }
    public void              setDungeonDepth(int d) { dungeonDepth = Math.max(dungeonDepth, d); }
    public PlayerBackground  getBackground()        { return background; }
    public java.util.Map<String,String> getEquippedItems() { return equippedItems; }
}
