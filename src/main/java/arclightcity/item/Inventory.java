package arclightcity.item;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;

import arclightcity.entity.player.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Inventory — menyimpan semua item milik player.
 *
 * Fitur:
 *   - Slot equipment: WEAPON, ARMOR, ACCESSORY_1, ACCESSORY_2
 *   - Bag: semua item lain (consumable, material, unequipped equipment)
 *   - Max capacity bag bisa diexpand
 *   - Auto-stack consumable & material yang sama
 */
public class Inventory {

    // ── Equipment Slots ──────────────────────────────────────
    private Equipment equippedWeapon     = null;
    private Equipment equippedArmor      = null;
    private Equipment equippedAccessory1 = null;
    private Equipment equippedAccessory2 = null;

    // ── Bag ──────────────────────────────────────────────────
    private final List<Item> bag = new ArrayList<>();
    private       int        maxBagSize = 30;

    // ── Material shorthand (quick access) ───────────────────
    private int scrapMetal   = 0;
    private int cyberChips   = 0;
    private int neonCrystals = 0;
    private int voidFragments= 0;
    private int calibrationKits = 0;
    private int upgradeCores = 0;

    // ── Player ref (untuk apply/remove stat) ────────────────
    private final Player player;

    // ── Constructor ─────────────────────────────────────────

    public Inventory(Player player) {
        this.player = player;
    }

    // ════════════════════════════════════════════════════════
    // EQUIPMENT
    // ════════════════════════════════════════════════════════

    public EquipResult equip(Equipment equipment) {
        // Tentukan slot
        String slot = determineSlot(equipment);
        if (slot == null) return EquipResult.fail("Invalid equipment type.");

        // Unequip yang sekarang di slot itu (masuk bag)
        Equipment old = getEquippedInSlot(slot);
        if (old != null) {
            removeStatBonuses(old);
            bag.add(old);
        }

        // Pasang equipment baru
        setEquippedInSlot(slot, equipment);
        bag.remove(equipment);
        applyStatBonuses(equipment);

        return EquipResult.success(slot, equipment, old);
    }

    public EquipResult unequip(String slot) {
        Equipment current = getEquippedInSlot(slot);
        if (current == null) return EquipResult.fail("No equipment in slot: " + slot);
        if (bag.size() >= maxBagSize) return EquipResult.fail("Bag is full!");

        removeStatBonuses(current);
        setEquippedInSlot(slot, null);
        bag.add(current);

        return EquipResult.success(slot, null, current);
    }

    // ── Stat apply/remove ────────────────────────────────────

    private void applyStatBonuses(Equipment eq) {
        eq.getStatBonuses().forEach((stat, val) ->
                player.getStats().addEquipmentBonus(stat, val));
    }

    private void removeStatBonuses(Equipment eq) {
        eq.getStatBonuses().forEach((stat, val) ->
                player.getStats().addEquipmentBonus(stat, -val));
    }

    // ── Slot helpers ─────────────────────────────────────────

    private String determineSlot(Equipment eq) {
        return switch (eq.getItemType()) {
            case WEAPON    -> "WEAPON";
            case ARMOR     -> "ARMOR";
            case ACCESSORY -> equippedAccessory1 == null ? "ACCESSORY_1" : "ACCESSORY_2";
            default        -> null;
        };
    }

    private Equipment getEquippedInSlot(String slot) {
        return switch (slot) {
            case "WEAPON"      -> equippedWeapon;
            case "ARMOR"       -> equippedArmor;
            case "ACCESSORY_1" -> equippedAccessory1;
            case "ACCESSORY_2" -> equippedAccessory2;
            default            -> null;
        };
    }

    private void setEquippedInSlot(String slot, Equipment eq) {
        switch (slot) {
            case "WEAPON"      -> equippedWeapon     = eq;
            case "ARMOR"       -> equippedArmor      = eq;
            case "ACCESSORY_1" -> equippedAccessory1 = eq;
            case "ACCESSORY_2" -> equippedAccessory2 = eq;
        }
    }

    // ════════════════════════════════════════════════════════
    // BAG MANAGEMENT
    // ════════════════════════════════════════════════════════

    public boolean addItem(Item item) {
        if (item instanceof Material mat) {
            addMaterial(mat);
            return true;
        }
        if (item instanceof Consumable cons) {
            // Auto-stack consumable sama
            Optional<Item> existing = bag.stream()
                    .filter(i -> i instanceof Consumable c
                            && c.getName().equals(cons.getName()))
                    .findFirst();
            if (existing.isPresent()) {
                ((Consumable) existing.get()).addStack();
                return true;
            }
        }
        if (bag.size() >= maxBagSize) return false;
        bag.add(item);
        return true;
    }

    public boolean removeItem(String itemId) {
        return bag.removeIf(i -> i.getId().equals(itemId));
    }

    public Item findById(String itemId) {
        return bag.stream().filter(i -> i.getId().equals(itemId)).findFirst().orElse(null);
    }

    // ════════════════════════════════════════════════════════
    // MATERIAL MANAGEMENT
    // ════════════════════════════════════════════════════════

    private void addMaterial(Material mat) {
        switch (mat.getMaterialType()) {
            case SCRAP_METAL      -> scrapMetal      += mat.getQuantity();
            case CYBER_CHIP       -> cyberChips      += mat.getQuantity();
            case NEON_CRYSTAL     -> neonCrystals    += mat.getQuantity();
            case VOID_FRAGMENT    -> voidFragments   += mat.getQuantity();
            case CALIBRATION_KIT  -> calibrationKits += mat.getQuantity();
            case UPGRADE_CORE     -> upgradeCores    += mat.getQuantity();
            default               -> bag.add(mat);
        }
    }

    // ════════════════════════════════════════════════════════
    // UPGRADE & CALIBRATION
    // ════════════════════════════════════════════════════════

    public UpgradeSystem.UpgradeResult upgradeItem(String itemId) {
        Item item = findById(itemId);
        if (!(item instanceof Equipment eq)) return UpgradeSystem.UpgradeResult.fail("Item not found or not upgradeable.");

        // Hitung material yang dibutuhkan sebelum upgrade
        int lvl = eq.getUpgradeLevel();
        int[] cost = switch (eq.getRarity()) {
            case COMMON    -> new int[]{2*(lvl+1), 0,         0};
            case UNCOMMON  -> new int[]{4*(lvl+1), 1*(lvl+1), 0};
            case RARE      -> new int[]{6*(lvl+1), 2*(lvl+1), 1*(lvl+1)};
            case EPIC      -> new int[]{8*(lvl+1), 4*(lvl+1), 2*(lvl+1)};
            case LEGENDARY -> new int[]{10*(lvl+1),6*(lvl+1), 4*(lvl+1)};
        };

        var result = UpgradeSystem.upgrade(eq, scrapMetal, cyberChips, neonCrystals);
        if (result.success) {
            scrapMetal   -= result.scrapUsed;
            cyberChips   -= result.chipsUsed;
            neonCrystals -= result.crystalsUsed;
            // Re-apply stats jika item sedang diequip
            reapplyIfEquipped(eq);
        }
        return result;
    }

    public CalibrationSystem.CalibrationResult calibrateItem(String itemId, int kitCount) {
        Item item = findById(itemId);
        if (!(item instanceof Equipment eq)) {
            return CalibrationSystem.CalibrationResult.fail("Item not found.");
        }
        if (calibrationKits < kitCount) {
            return CalibrationSystem.CalibrationResult.fail(
                    "Not enough Calibration Kits. Have: " + calibrationKits);
        }

        var result = CalibrationSystem.calibrate(eq, kitCount);
        if (result.success) {
            calibrationKits -= kitCount;
            reapplyIfEquipped(eq);
        }
        return result;
    }

    private void reapplyIfEquipped(Equipment eq) {
        List<Equipment> allSlots = List.of(
                equippedWeapon    != null ? equippedWeapon     : eq,
                equippedArmor     != null ? equippedArmor      : eq,
                equippedAccessory1!= null ? equippedAccessory1 : eq,
                equippedAccessory2!= null ? equippedAccessory2 : eq
        );
        boolean isEquipped = (eq == equippedWeapon || eq == equippedArmor
                           || eq == equippedAccessory1 || eq == equippedAccessory2);
        if (isEquipped) {
            // Recalculate semua equipment bonuses dari nol
            player.getStats().clearEquipmentBonuses();
            if (equippedWeapon     != null) applyStatBonuses(equippedWeapon);
            if (equippedArmor      != null) applyStatBonuses(equippedArmor);
            if (equippedAccessory1 != null) applyStatBonuses(equippedAccessory1);
            if (equippedAccessory2 != null) applyStatBonuses(equippedAccessory2);

            // Recalculate MAX_SHIELD dengan SHIELD_MULT
            recalcShield();
        }
    }

    /**
     * Recalculate MAX_SHIELD berdasarkan base + SHIELD_MULT dari equipment.
     * MAX_SHIELD_final = MAX_SHIELD_base × (1 + SHIELD_MULT)
     * Kemudian clamp currentShield agar tidak melebihi max baru.
     */
    private void recalcShield() {
        double baseShield = player.getStats().getBaseMap()
                .getOrDefault(StatType.MAX_SHIELD, 0.0);
        double shieldMult = player.getStats().get(StatType.SHIELD_MULT);
        double newMax     = baseShield * (1.0 + shieldMult);

        // Update equipment layer untuk MAX_SHIELD
        double equipBonus = newMax - baseShield;
        player.getStats().setEquipment(StatType.MAX_SHIELD, equipBonus);

        // Clamp currentShield
        player.setShield(Math.min(player.getCurrentShield(), newMax));
    }

    // ════════════════════════════════════════════════════════
    // CONSUMABLE USE
    // ════════════════════════════════════════════════════════

    public boolean useConsumable(String itemId, Entity target) {
        Item item = findById(itemId);
        if (!(item instanceof Consumable cons)) return false;

        boolean used = cons.useOne();
        if (!used) return false;

        switch (cons.getConsumableType()) {
            case HEALTH_PACK -> target.receiveHeal(cons.getEffectValue());
            case MP_PACK     -> target.restoreMp(cons.getEffectValue());
            case ANTIDOTE    -> {
                target.removeEffect(StatusEffectType.BURN);
                target.removeEffect(StatusEffectType.BLEED);
                target.removeEffect(StatusEffectType.VIRUS);
                target.removeEffect(StatusEffectType.CORRODE);
            }
            case BUFF_ITEM   -> target.applyEffect(new StatusEffect(
                    StatusEffectType.EMPOWERED, 2, 0, "ITEM"));
            case GRENADE     -> { /* dihandle oleh CombatManager */ }
        }

        // Hapus dari bag jika stack habis
        if (cons.getStackCount() <= 0) removeItem(itemId);
        return true;
    }

    // ════════════════════════════════════════════════════════
    // QUERIES
    // ════════════════════════════════════════════════════════

    public List<Item>      getAllItems()         { return Collections.unmodifiableList(bag); }
    public List<Equipment> getEquipmentInBag()  {
        return bag.stream().filter(i -> i instanceof Equipment)
                .map(i -> (Equipment) i).collect(Collectors.toList());
    }
    public List<Consumable> getConsumables() {
        return bag.stream().filter(i -> i instanceof Consumable)
                .map(i -> (Consumable) i).collect(Collectors.toList());
    }

    public Equipment getEquippedWeapon()     { return equippedWeapon; }
    public Equipment getEquippedArmor()      { return equippedArmor; }
    public Equipment getEquippedAccessory1() { return equippedAccessory1; }
    public Equipment getEquippedAccessory2() { return equippedAccessory2; }

    public int getScrapMetal()      { return scrapMetal; }
    public int getCyberChips()      { return cyberChips; }
    public int getNeonCrystals()    { return neonCrystals; }
    public int getVoidFragments()   { return voidFragments; }
    public int getCalibrationKits() { return calibrationKits; }
    public int getUpgradeCores()    { return upgradeCores; }
    public int getBagSize()         { return bag.size(); }
    public int getMaxBagSize()      { return maxBagSize; }
    public void expandBag(int slots){ maxBagSize += slots; }

    // ════════════════════════════════════════════════════════
    // RESULT CLASSES
    // ════════════════════════════════════════════════════════

    public static class EquipResult {
        public final boolean   success;
        public final String    message;
        public final String    slot;
        public final Equipment newItem;
        public final Equipment oldItem;

        private EquipResult(boolean s, String m, String slot, Equipment n, Equipment o) {
            success = s; message = m; this.slot = slot; newItem = n; oldItem = o;
        }
        public static EquipResult success(String slot, Equipment n, Equipment o) {
            String msg = n != null ? "✅ Equipped " + n.getName() + " in " + slot
                                   : "Unequipped from " + slot;
            return new EquipResult(true, msg, slot, n, o);
        }
        public static EquipResult fail(String reason) {
            return new EquipResult(false, "❌ " + reason, null, null, null);
        }
    }

    // ── Save/Load direct equip (bypass validation) ────────────
    /** Dipakai saat restore dari save — langsung set slot tanpa cek kondisi */
    public void forceEquipWeapon(Equipment eq)     { equippedWeapon     = eq; }
    public void forceEquipArmor(Equipment eq)      { equippedArmor      = eq; }
    public void forceEquipAccessory1(Equipment eq) { equippedAccessory1 = eq; }
    public void forceEquipAccessory2(Equipment eq) { equippedAccessory2 = eq; }
}
