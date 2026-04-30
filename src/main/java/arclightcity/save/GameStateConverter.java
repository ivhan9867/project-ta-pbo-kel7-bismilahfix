package arclightcity.save;

import arclightcity.combat.CombatResult;
import arclightcity.engine.GameEngine;
import arclightcity.entity.mercenary.Mercenary;
import arclightcity.entity.mercenary.MercenaryType;
import arclightcity.entity.player.Player;
import arclightcity.entity.player.PlayerBackground;
import arclightcity.entity.stats.StatType;
import arclightcity.entity.EntityFactory;
import arclightcity.item.*;

import java.util.List;

/**
 * GameStateConverter — mengkonversi antara GameEngine state dan GameSaveState.
 *
 * Dua operasi utama:
 *   toSaveState(engine)    → buat snapshot dari engine sekarang
 *   restoreFromSave(state) → rebuild engine dari snapshot
 *
 * Ini layer terpisah dari SaveManager (yang hanya urusan IO).
 * Pemisahan ini memudahkan unit test dan maintenance.
 */
public class GameStateConverter {

    // ── SAVE: Engine → SaveState ──────────────────────────────

    public static GameSaveState toSaveState(GameEngine engine, boolean isAutoSave) {
        GameSaveState save = GameSaveState.create(isAutoSave);
        save.progress.lastSaveMs = System.currentTimeMillis();

        Player player = engine.getPlayer();
        if (player == null) return save;

        // ── Player data ──────────────────────────────────────
        save.player.name            = player.getName();
        save.player.background      = player.getBackground().name();
        save.player.level           = player.getLevel();
        save.player.currentExp      = player.getCurrentExp();
        save.player.expToNext       = player.getExpToNextLevel();
        save.player.gold            = player.getGold();
        save.player.currentHp       = player.getCurrentHp();
        save.player.currentMp       = player.getCurrentMp();
        save.player.currentShield   = player.getCurrentShield();
        save.player.skillPoints     = player.getSkillPoints();
        save.player.dungeonDepth    = player.getDungeonDepth();
        save.player.unlockedSkillIds.addAll(player.getUnlockedSkillIds());
        save.player.equippedSkillIds.addAll(player.getEquippedSkillIds());

        // ── Inventory ────────────────────────────────────────
        Inventory inv = engine.getInventory();
        if (inv != null) {
            // Equipment di bag
            for (Equipment eq : inv.getEquipmentInBag()) {
                save.inventoryItems.add(equipmentToData(eq, null));
            }
            // Equipment yang diequip
            if (inv.getEquippedWeapon()     != null)
                save.inventoryItems.add(equipmentToData(inv.getEquippedWeapon(), "WEAPON"));
            if (inv.getEquippedArmor()      != null)
                save.inventoryItems.add(equipmentToData(inv.getEquippedArmor(), "ARMOR"));
            if (inv.getEquippedAccessory1() != null)
                save.inventoryItems.add(equipmentToData(inv.getEquippedAccessory1(), "ACC1"));
            if (inv.getEquippedAccessory2() != null)
                save.inventoryItems.add(equipmentToData(inv.getEquippedAccessory2(), "ACC2"));
            // Consumables
            for (Consumable c : inv.getConsumables()) {
                GameSaveState.ItemData d = new GameSaveState.ItemData();
                d.itemId   = c.getId();
                d.itemClass= "Consumable";
                d.name     = c.getName();
                d.rarity   = c.getRarity().name();
                d.subType  = c.getConsumableType().name();
                d.effectValue = c.getEffectValue();
                d.quantity = c.getStackCount();
                save.inventoryItems.add(d);
            }
            // Materials
            for (Material m : inv.getMaterials()) {
                GameSaveState.ItemData d = new GameSaveState.ItemData();
                d.itemId   = m.getId();
                d.itemClass= "Material";
                d.name     = m.getName();
                d.rarity   = m.getRarity().name();
                d.subType  = m.getMaterialType().name();
                d.quantity = m.getQuantity();
                save.inventoryItems.add(d);
            }
        }

        // ── Mercenaries ──────────────────────────────────────
        for (Mercenary merc : engine.getOwnedMercs()) {
            GameSaveState.MercData md = new GameSaveState.MercData();
            md.mercType     = merc.getMercenaryType().name();
            md.loyaltyLevel = merc.getLoyaltyLevel();
            md.currentHp    = merc.getCurrentHp();
            md.currentMp    = merc.getCurrentMp();
            md.currentShield= merc.getCurrentShield();
            md.isActive     = engine.getActiveMercs().contains(merc);
            save.ownedMercs.add(md);
        }

        // ── Progress ─────────────────────────────────────────
        save.progress.deepestFloorReached = player.getDungeonDepth();
        // Stats lain bisa ditambah seiring development

        return save;
    }

    private static GameSaveState.ItemData equipmentToData(Equipment eq, String slot) {
        GameSaveState.ItemData d = new GameSaveState.ItemData();
        d.itemId          = eq.getId();
        d.name            = eq.getName();
        d.rarity          = eq.getRarity().name();
        d.upgradeLevel    = eq.getUpgradeLevel();
        d.calibrationCount= eq.getCalibrationCount();
        d.slot            = slot;

        if (eq instanceof Weapon w) {
            d.itemClass = "Weapon";
            d.subType   = w.getWeaponType().name();
        } else if (eq instanceof Armor a) {
            d.itemClass = "Armor";
            d.subType   = a.getArmorType().name();
        } else {
            d.itemClass = "Accessory";
        }

        eq.getBaseStats().forEach((k, v)  -> d.baseStats.put(k.name(), v));
        eq.getBonusStats().forEach((k, v) -> d.bonusStats.put(k.name(), v));
        return d;
    }

    // ── LOAD: SaveState → Engine ──────────────────────────────

    /**
     * Rebuild GameEngine state dari save.
     * Dipanggil setelah SaveManager.loadLatest() berhasil.
     */
    public static void restoreFromSave(GameEngine engine, GameSaveState save) {
        if (save == null || save.player == null) return;

        // 1. Buat Player baru dari background tersimpan
        PlayerBackground bg = PlayerBackground.valueOf(save.player.background);
        engine.createCharacterFromSave(save.player.name, bg, save.player);

        // 2. Restore inventory
        restoreInventory(engine, save);

        // 3. Restore mercenaries
        restoreMercenaries(engine, save);

        // 4. Engine sudah di-set ke HUB state oleh createCharacterFromSave
        System.out.println("[Converter] Restore complete: " + save.getSummary());
    }

    private static void restoreInventory(GameEngine engine, GameSaveState save) {
        Inventory inv = engine.getInventory();
        if (inv == null) return;

        for (GameSaveState.ItemData d : save.inventoryItems) {
            try {
                Item item = dataToItem(d);
                if (item == null) continue;

                if (d.slot != null) {
                    // Equip langsung ke slot
                    if (item instanceof Equipment eq) {
                        switch (d.slot) {
                            case "WEAPON" -> inv.forceEquipWeapon(eq);
                            case "ARMOR"  -> inv.forceEquipArmor(eq);
                            case "ACC1"   -> inv.forceEquipAccessory1(eq);
                            case "ACC2"   -> inv.forceEquipAccessory2(eq);
                        }
                    }
                } else {
                    inv.addItem(item);
                }
            } catch (Exception e) {
                System.err.println("[Converter] Failed to restore item: " + d.name + " — " + e.getMessage());
            }
        }
    }

    private static Item dataToItem(GameSaveState.ItemData d) {
        if (d == null) return null;
        Item.Rarity rarity = Item.Rarity.valueOf(d.rarity);

        return switch (d.itemClass) {
            case "Weapon" -> {
                var stats = parseStats(d.baseStats);
                var w = new Weapon(d.name, "", rarity,
                        Weapon.WeaponType.valueOf(d.subType), stats);
                applyEquipmentData(w, d);
                yield w;
            }
            case "Armor" -> {
                var stats = parseStats(d.baseStats);
                var a = new Armor(d.name, "", rarity,
                        Armor.ArmorType.valueOf(d.subType), stats);
                applyEquipmentData(a, d);
                yield a;
            }
            case "Accessory" -> {
                var stats = parseStats(d.baseStats);
                var acc = new Accessory(d.name, "", rarity, stats);
                applyEquipmentData(acc, d);
                yield acc;
            }
            case "Consumable" -> {
                var c = new Consumable(d.name, "", rarity,
                        Consumable.ConsumableType.valueOf(d.subType), d.effectValue);
                // Stack
                for (int i = 1; i < d.quantity; i++) c.addStack();
                yield c;
            }
            case "Material" -> {
                var m = new Material(d.name, "", rarity,
                        Material.MaterialType.valueOf(d.subType));
                if (d.quantity > 1) m.addQuantity(d.quantity - 1);
                yield m;
            }
            default -> null;
        };
    }

    private static void applyEquipmentData(Equipment eq, GameSaveState.ItemData d) {
        // Restore upgrade level
        for (int i = 0; i < d.upgradeLevel; i++) eq.applyUpgrade();
        // Restore bonus stats (calibration result)
        var rng = new java.util.Random();
        for (int i = 0; i < d.calibrationCount; i++) eq.calibrate(rng);
        // Override bonus stats dengan yang tersimpan
        eq.getBonusStats().clear();
        parseStats(d.bonusStats).forEach((k, v) -> eq.getBonusStats().put(k, v));
    }

    private static java.util.Map<StatType, Double> parseStats(
            java.util.Map<String, Double> raw) {
        var result = new java.util.EnumMap<StatType, Double>(StatType.class);
        if (raw == null) return result;
        raw.forEach((k, v) -> {
            try { result.put(StatType.valueOf(k), v); }
            catch (IllegalArgumentException ignored) { }
        });
        return result;
    }

    private static void restoreMercenaries(GameEngine engine, GameSaveState save) {
        engine.clearMercsForLoad();
        for (GameSaveState.MercData md : save.ownedMercs) {
            try {
                MercenaryType type = MercenaryType.valueOf(md.mercType);
                Mercenary merc    = EntityFactory.createMercenary(type);
                // Restore loyalty
                for (int i = 0; i < md.loyaltyLevel; i++) merc.gainLoyalty(1);
                // Restore HP/MP/Shield
                if (md.currentHp    < merc.getCurrentHp())    merc.setHpDirect(md.currentHp);
                if (md.currentMp    < merc.getCurrentMp())    merc.setMpDirect(md.currentMp);
                if (md.currentShield< merc.getCurrentShield())merc.setShieldDirect(md.currentShield);

                engine.addOwnedMercForLoad(merc);
                if (md.isActive) engine.addActiveMercForLoad(merc);
            } catch (Exception e) {
                System.err.println("[Converter] Failed to restore merc: " + md.mercType);
            }
        }
    }
}
