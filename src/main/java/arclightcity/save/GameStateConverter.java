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
        if (player == null) {
            System.err.println("[SAVE] ERROR: player is null!");
            return save;
        }

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
                save.inventoryItems.add(equipToData(inv.getEquippedWeapon(),     "WEAPON"));
            if (inv.getEquippedArmor()      != null)
                save.inventoryItems.add(equipToData(inv.getEquippedArmor(),      "ARMOR"));
            if (inv.getEquippedHelmet()     != null)
                save.inventoryItems.add(equipToData(inv.getEquippedHelmet(),     "HELMET"));
            if (inv.getEquippedBoots()      != null)
                save.inventoryItems.add(equipToData(inv.getEquippedBoots(),      "BOOTS"));
            if (inv.getEquippedRing1()      != null)
                save.inventoryItems.add(equipToData(inv.getEquippedRing1(),      "RING_1"));
            if (inv.getEquippedRing2()      != null)
                save.inventoryItems.add(equipToData(inv.getEquippedRing2(),      "RING_2"));
            if (inv.getEquippedAccessory1() != null)
                save.inventoryItems.add(equipToData(inv.getEquippedAccessory1(), "ACC1"));
            if (inv.getEquippedAccessory2() != null)
                save.inventoryItems.add(equipToData(inv.getEquippedAccessory2(), "ACC2"));

            // Semua item di bag
            int bagCount = 0;
            for (Item item : inv.getAllBagItems()) {
                if (item instanceof Equipment eq) {
                    save.inventoryItems.add(equipToData(eq, null));
                    bagCount++;
                } else if (item instanceof Consumable c) {
                    save.inventoryItems.add(consumableToData(c));
                    bagCount++;
                } else if (item instanceof Material m) {
                    save.inventoryItems.add(materialToData(m));
                    bagCount++;
                }
            }
            // Save material counters sebagai synthetic Material items
            if (inv.getScrapMetal() > 0) {
                GameSaveState.ItemData sd = new GameSaveState.ItemData();
                sd.name = "Scrap Metal"; sd.itemClass = "Material";
                sd.subType = "SCRAP"; sd.rarity = "COMMON"; sd.quantity = inv.getScrapMetal();
                save.inventoryItems.add(sd);
            }
            if (inv.getCyberChips() > 0) {
                GameSaveState.ItemData cd = new GameSaveState.ItemData();
                cd.name = "Circuit Chip"; cd.itemClass = "Material";
                cd.subType = "CHIP"; cd.rarity = "UNCOMMON"; cd.quantity = inv.getCyberChips();
                save.inventoryItems.add(cd);
            }
            if (inv.getNeonCrystals() > 0) {
                GameSaveState.ItemData nd = new GameSaveState.ItemData();
                nd.name = "Neon Crystal"; nd.itemClass = "Material";
                nd.subType = "CRYSTAL"; nd.rarity = "RARE"; nd.quantity = inv.getNeonCrystals();
                save.inventoryItems.add(nd);
            }
            // Save artifact pocket (terpisah dari bag items)
            for (arclightcity.item.Artifact art : inv.getArtifactPocket()) {
                save.savedArtifactPocket.add(new String[]{
                    art.getArtifactType().name(), art.getRarity().name()
                });
            }
            System.out.println("[SAVE] Saved " + save.inventoryItems.size() +
                " items total (" + bagCount + " in bag + material counters) + " +
                save.savedArtifactPocket.size() + " artifacts in pocket");
        } else {
            System.err.println("[SAVE] ERROR: inventory is null!");
        }

        // ── Mercenaries ──────────────────────────────────────
        for (Mercenary merc : engine.getOwnedMercs()) {
            GameSaveState.MercData md = new GameSaveState.MercData();
            md.mercType      = merc.getMercenaryType().name();
            md.loyaltyLevel  = merc.getLoyaltyLevel();
            md.currentHp     = merc.getCurrentHp();
            md.currentMp     = merc.getCurrentMp();
            // Simpan stat aktual agar level-up HP tidak hilang saat load
            md.savedMaxHp    = merc.getStats().get(arclightcity.entity.stats.StatType.MAX_HP);
            md.savedPhysAtk  = merc.getStats().get(arclightcity.entity.stats.StatType.PHYSICAL_ATK);
            md.savedCyberAtk = merc.getStats().get(arclightcity.entity.stats.StatType.CYBER_ATK);
            md.savedSpeed    = merc.getStats().get(arclightcity.entity.stats.StatType.SPEED);
            md.currentShield = merc.getCurrentShield();
            md.isActive      = engine.getActiveMercs().contains(merc);
            md.isActive = engine.getActiveMercs().stream()
                .anyMatch(m -> m.getMercenaryType() == merc.getMercenaryType());
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
            d.subType   = w.getWeaponType() != null ? w.getWeaponType().name() : "KATANA";
        } else if (eq instanceof Armor a) {
            d.itemClass = "Armor";
            d.subType   = a.getArmorType() != null ? a.getArmorType().name() : "MEDIUM";
        } else {
            d.itemClass = "Accessory";
            d.subType   = "ACCESSORY"; // default untuk Accessory
        }
        System.out.println("[SAVE] equipToData: " + eq.getName() +
            " class=" + d.itemClass + " subType=" + d.subType + " slot=" + d.slot);

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

        PlayerBackground bg = PlayerBackground.ASUNA;
        engine.createCharacterFromSave(save.player.name, bg, save.player);

        // Restore floor number ke DungeonManager
        int savedDepth = save.player.dungeonDepth;
        if (savedDepth > 0) {
            engine.getDungeonManager().setCurrentFloorNumber(savedDepth);
        }

        restoreInventory(engine, save);
        restoreMercenaries(engine, save);

        System.out.println("[Converter] Restore complete: " + save.getSummary());
    
        // Restore artifact pocket
        if (save.savedArtifactPocket != null && engine.getInventory() != null) {
            for (String[] data : save.savedArtifactPocket) {
                try {
                    arclightcity.item.ArtifactType type = arclightcity.item.ArtifactType.valueOf(data[0]);
                    arclightcity.item.Item.Rarity   rar  = arclightcity.item.Item.Rarity.valueOf(data[1]);
                    arclightcity.item.Artifact art = new arclightcity.item.Artifact(type, rar);
                    engine.getInventory().addItem(art); // masuk pocket otomatis
                } catch (Exception ignored) {}
            }
        }

        // Recalc equipment stats setelah restore — critical untuk damage calculation
        try {
            if (engine.getPlayer() != null && engine.getInventory() != null) {
                engine.getPlayer().recalcEquipStats(engine.getInventory());
            }
        } catch (Exception ignored) {}
    }

    // ── Inventory restore ─────────────────────────────────────

    private static void restoreInventory(GameEngine engine, GameSaveState save) {
        Inventory inv = engine.getInventory();
        if (inv == null) {
            System.err.println("[LOAD] ERROR: inventory is null during restore!");
            return;
        }

        System.out.println("[LOAD] Restoring " + save.inventoryItems.size() + " items...");
        int restored = 0, failed = 0;

        for (GameSaveState.ItemData d : save.inventoryItems) {
            try {
                Item item = dataToItem(d);
                if (item == null) {
                    System.err.println("[LOAD] dataToItem returned null for: " + d.name + " class=" + d.itemClass);
                    failed++;
                    continue;
                }

                if (d.slot != null && item instanceof Equipment eq) {
                    switch (d.slot) {
                        case "WEAPON" -> inv.forceEquipWeapon(eq);
                        case "ARMOR"  -> inv.forceEquipArmor(eq);
                        case "HELMET" -> inv.forceEquipHelmet(eq);
                        case "BOOTS"  -> inv.forceEquipBoots(eq);
                        case "RING_1" -> inv.forceEquipRing1(eq);
                        case "RING_2" -> inv.forceEquipRing2(eq);
                        case "ACC1"   -> inv.forceEquipAccessory1(eq);
                        case "ACC2"   -> inv.forceEquipAccessory2(eq);
                    }
                } else {
                    boolean added = inv.addItem(item);
                    if (!added) {
                        System.err.println("[LOAD] addItem failed for: " + d.name + " (bag full?)");
                        failed++;
                        continue;
                    }
                }
                restored++;
                System.out.println("[LOAD] ✓ Restored: " + d.name + " slot=" + d.slot);
            } catch (Exception e) {
                System.err.println("[LOAD] ✗ Exception restoring " + d.name + ": " + e.getMessage());
                failed++;
            }
        }
        System.out.println("[LOAD] Done: " + restored + " restored, " + failed + " failed");
    }

    private static Weapon.WeaponType safeWeaponType(String name) {
        if (name == null) return Weapon.WeaponType.KATANA;
        try { return Weapon.WeaponType.valueOf(name); }
        catch (IllegalArgumentException e) {
            // Fallback untuk weapon type lama (BLADE, GUN, dll)
            return switch (name) {
                case "BLADE"         -> Weapon.WeaponType.KATANA;
                case "HEAVY"         -> Weapon.WeaponType.ODACHI;
                case "GUN"           -> Weapon.WeaponType.SHADOW_BLADE;
                case "CYBER_TOOL"    -> Weapon.WeaponType.GOLOK_RUNE;
                case "ENERGY_EMITTER"-> Weapon.WeaponType.KUJANG_BLADE;
                default              -> Weapon.WeaponType.KATANA;
            };
        }
    }

    private static Item dataToItem(GameSaveState.ItemData d) {
        if (d == null || d.itemClass == null) return null;

        Item.Rarity rarity;
        try {
            rarity = Item.Rarity.valueOf(d.rarity);
        } catch (Exception e) {
            rarity = Item.Rarity.COMMON;
        }

        try {
            return switch (d.itemClass) {
                case "Weapon" -> {
                    var stats = parseStats(d.baseStats);
                    var w = new Weapon(d.name, "", rarity,
                            safeWeaponType(d.subType), stats);
                    applyEquipData(w, d);
                    yield w;
                }
                case "Armor" -> {
                    var stats = parseStats(d.baseStats);
                    Armor.ArmorType armorType = safeArmorType(d.subType);
                    var a = new Armor(d.name, "", rarity, armorType, stats);
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
                    Consumable.ConsumableType cType = safeConsumableType(d.subType);
                    var c = new Consumable(d.name, "", rarity, cType, d.effectValue);
                    for (int i = 1; i < d.quantity; i++) c.addStack();
                    yield c;
                }
                case "Material" -> {
                    Material.MaterialType mType = safeMaterialType(d.subType);
                    var m = new Material(d.name, "", rarity, mType);
                    if (d.quantity > 1) m.addQuantity(d.quantity - 1);
                    yield m;
                }
                default -> null;
            };
        } catch (Exception e) {
            System.err.println("[LOAD] dataToItem failed for '" + d.name +
                "' class=" + d.itemClass + " subType=" + d.subType +
                " error=" + e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    private static Armor.ArmorType safeArmorType(String name) {
        if (name == null) return Armor.ArmorType.MEDIUM;
        try { return Armor.ArmorType.valueOf(name); }
        catch (IllegalArgumentException e) { return Armor.ArmorType.MEDIUM; }
    }

    private static Consumable.ConsumableType safeConsumableType(String name) {
        if (name == null) return Consumable.ConsumableType.HEALTH_PACK;
        try { return Consumable.ConsumableType.valueOf(name); }
        catch (IllegalArgumentException e) { return Consumable.ConsumableType.HEALTH_PACK; }
    }

    private static Material.MaterialType safeMaterialType(String name) {
        if (name == null) return Material.MaterialType.SCRAP_METAL;
        try { return Material.MaterialType.valueOf(name); }
        catch (IllegalArgumentException e) { return Material.MaterialType.SCRAP_METAL; }
    }

    private static void applyEquipData(Equipment eq, GameSaveState.ItemData d) {
        // Set level langsung tanpa trigger random stat bonus
        eq.setUpgradeLevelDirect(d.upgradeLevel);
        // Restore bonus stats persis seperti saat disimpan
        eq.restoreBonusStats(parseStats(d.bonusStats));
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

                // Restore stat aktual dari save (level-up bonuses tidak hilang)
                if (md.savedMaxHp > 0) {
                    merc.getStats().setBase(StatType.MAX_HP,       md.savedMaxHp);
                    merc.getStats().setBase(StatType.PHYSICAL_ATK, md.savedPhysAtk);
                    merc.getStats().setBase(StatType.CYBER_ATK,    md.savedCyberAtk);
                    merc.getStats().setBase(StatType.SPEED,        md.savedSpeed);
                }
                // Restore vitals menggunakan MAX_HP yang sudah benar
                merc.restoreVitals(md.currentHp, md.currentMp, md.currentShield);

                engine.addOwnedMercForLoad(merc);
                if (md.isActive) engine.addActiveMercForLoad(merc);

            } catch (Exception e) {
                System.err.println("[Converter] Skip merc: " + md.mercType + " — " + e.getMessage());
            }
        }
    }
}
