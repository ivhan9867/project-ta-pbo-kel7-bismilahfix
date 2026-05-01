package arclightcity.save;

import arclightcity.engine.GameEngine;
import arclightcity.entity.EntityFactory;
import arclightcity.entity.mercenary.Mercenary;
import arclightcity.entity.mercenary.MercenaryType;
import arclightcity.entity.player.Player;
import arclightcity.entity.player.PlayerBackground;
import arclightcity.entity.stats.StatType;
import arclightcity.item.*;

import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import java.util.Random;

/**
 * GameStateConverter — konversi antara GameEngine state dan GameSaveState.
 *
 * Dua operasi utama:
 *   toSaveState(engine)        → buat snapshot dari engine sekarang
 *   restoreFromSave(engine, s) → rebuild engine dari snapshot
 */
public class GameStateConverter {

    // ── SAVE: Engine → SaveState ─────────────────────────────

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
            // Equipment yang diequip
            if (inv.getEquippedWeapon()     != null)
                save.inventoryItems.add(equipToData(inv.getEquippedWeapon(), "WEAPON"));
            if (inv.getEquippedArmor()      != null)
                save.inventoryItems.add(equipToData(inv.getEquippedArmor(), "ARMOR"));
            if (inv.getEquippedAccessory1() != null)
                save.inventoryItems.add(equipToData(inv.getEquippedAccessory1(), "ACC1"));
            if (inv.getEquippedAccessory2() != null)
                save.inventoryItems.add(equipToData(inv.getEquippedAccessory2(), "ACC2"));

            // Semua item di bag (equipment + consumable + material)
            for (Item item : inv.getAllBagItems()) {
                if (item instanceof Equipment eq)
                    save.inventoryItems.add(equipToData(eq, null));
                else if (item instanceof Consumable c)
                    save.inventoryItems.add(consumableToData(c));
                else if (item instanceof Material m)
                    save.inventoryItems.add(materialToData(m));
            }
        }

        // ── Mercenaries ──────────────────────────────────────
        for (Mercenary merc : engine.getOwnedMercs()) {
            GameSaveState.MercData md = new GameSaveState.MercData();
            md.mercType      = merc.getMercenaryType().name();
            md.loyaltyLevel  = merc.getLoyaltyLevel();
            md.currentHp     = merc.getCurrentHp();
            md.currentMp     = merc.getCurrentMp();
            md.currentShield = merc.getCurrentShield();
            md.isActive      = engine.getActiveMercs().contains(merc);
            save.ownedMercs.add(md);
        }

        // ── Progress ─────────────────────────────────────────
        save.progress.deepestFloorReached = player.getDungeonDepth();

        return save;
    }

    // ── Equipment → ItemData ──────────────────────────────────

    private static GameSaveState.ItemData equipToData(Equipment eq, String slot) {
        GameSaveState.ItemData d = new GameSaveState.ItemData();
        d.itemId           = eq.getId();
        d.name             = eq.getName();
        d.rarity           = eq.getRarity().name();
        d.upgradeLevel     = eq.getUpgradeLevel();
        d.calibrationCount = eq.getCalibrationCount();
        d.slot             = slot;

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

    private static GameSaveState.ItemData consumableToData(Consumable c) {
        GameSaveState.ItemData d = new GameSaveState.ItemData();
        d.itemId      = c.getId();
        d.itemClass   = "Consumable";
        d.name        = c.getName();
        d.rarity      = c.getRarity().name();
        d.subType     = c.getConsumableType().name();
        d.effectValue = c.getEffectValue();
        d.quantity    = c.getStackCount();
        return d;
    }

    private static GameSaveState.ItemData materialToData(Material m) {
        GameSaveState.ItemData d = new GameSaveState.ItemData();
        d.itemId    = m.getId();
        d.itemClass = "Material";
        d.name      = m.getName();
        d.rarity    = m.getRarity().name();
        d.subType   = m.getMaterialType().name();
        d.quantity  = m.getQuantity();
        return d;
    }

    // ── LOAD: SaveState → Engine ──────────────────────────────

    public static void restoreFromSave(GameEngine engine, GameSaveState save) {
        if (save == null || save.player == null) return;

        PlayerBackground bg = PlayerBackground.valueOf(save.player.background);
        engine.createCharacterFromSave(save.player.name, bg, save.player);

        restoreInventory(engine, save);
        restoreMercenaries(engine, save);

        System.out.println("[Converter] Restore complete: " + save.getSummary());
    }

    // ── Inventory restore ─────────────────────────────────────

    private static void restoreInventory(GameEngine engine, GameSaveState save) {
        Inventory inv = engine.getInventory();
        if (inv == null) return;

        for (GameSaveState.ItemData d : save.inventoryItems) {
            try {
                Item item = dataToItem(d);
                if (item == null) continue;

                if (d.slot != null && item instanceof Equipment eq) {
                    switch (d.slot) {
                        case "WEAPON" -> inv.forceEquipWeapon(eq);
                        case "ARMOR"  -> inv.forceEquipArmor(eq);
                        case "ACC1"   -> inv.forceEquipAccessory1(eq);
                        case "ACC2"   -> inv.forceEquipAccessory2(eq);
                    }
                } else {
                    inv.addItem(item);
                }
            } catch (Exception e) {
                System.err.println("[Converter] Skip item: " + d.name + " — " + e.getMessage());
            }
        }
    }

    private static Item dataToItem(GameSaveState.ItemData d) {
        if (d == null || d.itemClass == null) return null;
        Item.Rarity rarity = Item.Rarity.valueOf(d.rarity);

        return switch (d.itemClass) {
            case "Weapon" -> {
                var stats = parseStats(d.baseStats);
                var w = new Weapon(d.name, "", rarity,
                        Weapon.WeaponType.valueOf(d.subType), stats);
                applyEquipData(w, d);
                yield w;
            }
            case "Armor" -> {
                var stats = parseStats(d.baseStats);
                var a = new Armor(d.name, "", rarity,
                        Armor.ArmorType.valueOf(d.subType), stats);
                applyEquipData(a, d);
                yield a;
            }
            case "Accessory" -> {
                var stats = parseStats(d.baseStats);
                var acc = new Accessory(d.name, "", rarity, stats);
                applyEquipData(acc, d);
                yield acc;
            }
            case "Consumable" -> {
                var c = new Consumable(d.name, "", rarity,
                        Consumable.ConsumableType.valueOf(d.subType), d.effectValue);
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

    private static void applyEquipData(Equipment eq, GameSaveState.ItemData d) {
        for (int i = 0; i < d.upgradeLevel; i++) eq.applyUpgrade();
        // Restore bonus stats langsung (deterministic)
        eq.getBonusStats().clear();
        parseStats(d.bonusStats).forEach((k, v) -> eq.getBonusStats().put(k, v));
    }

    private static Map<StatType, Double> parseStats(Map<String, Double> raw) {
        var result = new EnumMap<StatType, Double>(StatType.class);
        if (raw == null) return result;
        raw.forEach((k, v) -> {
            try { result.put(StatType.valueOf(k), v); }
            catch (IllegalArgumentException ignored) {}
        });
        return result;
    }

    // ── Mercenary restore ─────────────────────────────────────

    private static void restoreMercenaries(GameEngine engine, GameSaveState save) {
        engine.clearMercsForLoad();

        for (GameSaveState.MercData md : save.ownedMercs) {
            try {
                MercenaryType type = MercenaryType.valueOf(md.mercType);
                Mercenary merc     = EntityFactory.createMercenary(type);

                // Restore loyalty — set langsung ke field (tidak ada gainLoyalty)
                merc.setLoyaltyDirect(md.loyaltyLevel);

                // Restore vitals — gunakan receiveHeal/restoreMp setelah entity dibuat
                // Entity dimulai dengan HP/MP penuh, kurangi sesuai save jika perlu
                double maxHp     = merc.getStats().get(StatType.MAX_HP);
                double maxMp     = merc.getStats().get(StatType.MAX_MP);
                double maxShield = merc.getStats().get(StatType.MAX_SHIELD);

                // Set langsung via protected field accessor di Mercenary
                merc.restoreVitals(md.currentHp, md.currentMp, md.currentShield);

                engine.addOwnedMercForLoad(merc);
                if (md.isActive) engine.addActiveMercForLoad(merc);

            } catch (Exception e) {
                System.err.println("[Converter] Skip merc: " + md.mercType + " — " + e.getMessage());
            }
        }
    }
}
