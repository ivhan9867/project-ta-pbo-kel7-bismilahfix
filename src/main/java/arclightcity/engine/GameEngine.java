package arclightcity.engine;
import arclightcity.combat.CombatManager;
import arclightcity.combat.CombatResult;
import arclightcity.combat.CombatAction;
import arclightcity.dungeon.DungeonEvent;
import arclightcity.dungeon.DungeonManager;
import arclightcity.entity.mercenary.MercenaryType;
import arclightcity.item.*;

import arclightcity.dungeon.DungeonStateEvent;
import arclightcity.entity.EntityFactory;
import arclightcity.entity.mercenary.Mercenary;
import arclightcity.entity.player.Player;
import arclightcity.entity.player.PlayerBackground;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * GameEngine — satu titik kendali seluruh game.
 *
 * Mengelola:
 *   - Player creation
 *   - Mercenary roster
 *   - DungeonManager (dungeon run)
 *   - Inventory player
 *   - UI callbacks
 *
 * JavaFX Controller berinteraksi dengan GameEngine, bukan langsung ke subsystem.
 */
public class GameEngine {

    // ── Core Components ──────────────────────────────────────
    private Player         player;
    private Inventory      inventory;
    private DungeonManager dungeonManager;

    // ── Mercenary Roster (semua merc yang dimiliki player) ───
    private final List<Mercenary> ownedMercs   = new ArrayList<>();
    private final List<Mercenary> activeMercs  = new ArrayList<>(); // max 2 dibawa

    // ── State ─────────────────────────────────────────────────
    private GameState currentState = GameState.MAIN_MENU;

    // ── UI Callbacks ─────────────────────────────────────────
    private Consumer<GameState>       onStateChange;
    private Consumer<DungeonStateEvent> onDungeonEvent;
    private Consumer<CombatManager>   onCombatStart;
    private Consumer<CombatResult>    onCombatEnd;
    private Consumer<DungeonEvent>    onEventRoom;

    // ── Game State Enum ──────────────────────────────────────
    public enum GameState {
        MAIN_MENU, CHARACTER_CREATE, HUB, DUNGEON, COMBAT, EVENT, SHOP, GAME_OVER
    }

    // ════════════════════════════════════════════════════════
    // INITIALIZATION
    // ════════════════════════════════════════════════════════

    /**
     * Buat karakter baru dan mulai game.
     */
    public void createCharacter(String name, PlayerBackground background) {
        this.player    = new Player(name, background);
        this.inventory = new Inventory(player);

        // Reset semua list agar tidak ada sisa dari run sebelumnya
        ownedMercs.clear();
        activeMercs.clear();

        // Setup DungeonManager
        dungeonManager = new DungeonManager();
        wireDungeonListeners();

        // Beri starter items
        giveStarterItems();

        // Auto-unlock dan equip 2 skill default sesuai background
        giveStarterSkills(background);

        // Default: player punya 1 starter merc gratis (TANK-RX9)
        Mercenary starterMerc = EntityFactory.createMercenary(MercenaryType.TANK_RX9);
        ownedMercs.add(starterMerc);
        activeMercs.add(starterMerc);

        transitionTo(GameState.HUB);
    }

    /** Auto-unlock 2 skill sesuai background dan langsung equip */
    private void giveStarterSkills(PlayerBackground background) {
        String[] skills = switch (background) {
            case STREET_BRAWLER  -> new String[]{"POWER_STRIKE", "EXECUTE"};
            case NETRUNNER       -> new String[]{"DEEP_HACK", "VIRUS_UPLOAD"};
            case VETERAN_SOLDIER -> new String[]{"IRON_SHIELD", "SHOCKWAVE"};
            case ENERGY_ADEPT    -> new String[]{"ENERGY_DRAIN", "BIO_IRRADIATE"};
            case GHOST_OPERATIVE -> new String[]{"PHANTOM_SHOT", "SHADOW_STEP"};
            case TECHWRIGHT      -> new String[]{"EMP_BURST", "FIELD_BARRIER"};
        };

        for (String skillId : skills) {
            player.forceUnlockSkill(skillId);
            player.equipSkill(skillId);
        }
    }

    // ── Starter Items ─────────────────────────────────────────

    private void giveStarterItems() {
        // 3 health pack
        for (int i = 0; i < 3; i++) {
            Consumable hp = new Consumable(
                    "Health Pack", "Basic healing", Item.Rarity.COMMON,
                    Consumable.ConsumableType.HEALTH_PACK, 50);
            inventory.addItem(hp);
        }
        // 5 scrap metal
        Material scrap = new Material(
                "Scrap Metal", "Basic crafting material", Item.Rarity.COMMON,
                Material.MaterialType.SCRAP_METAL);
        scrap.addQuantity(4);
        inventory.addItem(scrap);
    }

    // ── Wire Dungeon Listeners ────────────────────────────────

    private void wireDungeonListeners() {
        dungeonManager.setStateListener(event -> {
            if (onDungeonEvent != null) onDungeonEvent.accept(event);

            // Transisi state berdasarkan event
            switch (event.type) {
                case COMBAT_STARTED  -> transitionTo(GameState.COMBAT);
                case SHOP_OPENED     -> transitionTo(GameState.SHOP);
                case EVENT_ENCOUNTERED -> transitionTo(GameState.EVENT);
                case GAME_OVER       -> transitionTo(GameState.GAME_OVER);
                case ROOM_CLEARED, ROOM_ENTERED -> transitionTo(GameState.DUNGEON);
                default -> { }
            }
        });

        dungeonManager.setCombatStartListener(cm -> {
            if (onCombatStart != null) onCombatStart.accept(cm);
        });

        dungeonManager.setCombatEndListener(result -> {
            // Auto-resolve loot dari combat
            result.getLootItemIds().forEach(tableId -> {
                List<Item> drops = LootManager.generateLoot(
                        tableId, dungeonManager.getCurrentFloorNumber());
                drops.forEach(inventory::addItem);
            });
            if (onCombatEnd != null) onCombatEnd.accept(result);
            transitionTo(GameState.DUNGEON);
        });

        dungeonManager.setEventRoomListener(event -> {
            if (onEventRoom != null) onEventRoom.accept(event);
        });

        dungeonManager.setShopOpenListener(shopId -> {
            transitionTo(GameState.SHOP);
        });
    }

    // ════════════════════════════════════════════════════════
    // DUNGEON ACTIONS
    // ════════════════════════════════════════════════════════

    public void startDungeonRun() {
        if (player == null) return;
        // Buat DungeonManager baru setiap run untuk reset state penuh
        dungeonManager = new DungeonManager();
        wireDungeonListeners();
        transitionTo(GameState.DUNGEON);
        dungeonManager.startDungeon(player, activeMercs);
    }

    public boolean moveToRoom(int roomIndex) {
        return dungeonManager.moveToRoom(roomIndex);
    }

    public void descend() {
        dungeonManager.advanceToNextFloor();
        // Auto-save setiap turun floor
        autoSave();
    }

    public void resolveEventChoice(DungeonEvent event, int choiceIndex) {
        dungeonManager.resolveEventChoice(event, choiceIndex);
    }

    // ════════════════════════════════════════════════════════
    // COMBAT ACTIONS
    // ════════════════════════════════════════════════════════

    public void submitCombatAction(CombatAction action) {
        dungeonManager.getCombatManager().submitPlayerAction(action);
    }

    public boolean attemptFlee() {
        return dungeonManager.getCombatManager().attemptFlee();
    }

    // ════════════════════════════════════════════════════════
    // MERCENARY MANAGEMENT
    // ════════════════════════════════════════════════════════

    public boolean hireMercenary(MercenaryType type) {
        Mercenary merc = EntityFactory.createMercenary(type);
        if (!player.spendGold(merc.getHireCost())) return false;
        ownedMercs.add(merc);
        return true;
    }

    public boolean addToActiveParty(MercenaryType type) {
        if (activeMercs.size() >= 2) return false;
        Mercenary merc = ownedMercs.stream()
                .filter(m -> m.getMercenaryType() == type)
                .findFirst().orElse(null);
        if (merc == null || activeMercs.contains(merc)) return false;
        activeMercs.add(merc);
        return true;
    }

    public void removeFromActiveParty(MercenaryType type) {
        activeMercs.removeIf(m -> m.getMercenaryType() == type);
    }

    // ════════════════════════════════════════════════════════
    // STATE MACHINE
    // ════════════════════════════════════════════════════════

    private void transitionTo(GameState newState) {
        currentState = newState;
        if (onStateChange != null) onStateChange.accept(newState);
    }

    public void returnToHub() {
        transitionTo(GameState.HUB);
    }

    // ════════════════════════════════════════════════════════
    // SAVE / LOAD SYSTEM
    // ════════════════════════════════════════════════════════

    /** Simpan game ke manual save slot */
    public arclightcity.save.SaveManager.SaveResult saveGame() {
        arclightcity.save.GameSaveState state =
                arclightcity.save.GameStateConverter.toSaveState(this, false);
        arclightcity.save.SaveManager.SaveResult result =
                arclightcity.save.SaveManager.saveManual(state);
        System.out.println("[GameEngine] Manual save: " + result.message());
        return result;
    }

    /** Auto-save — dipanggil otomatis setiap turun floor */
    public void autoSave() {
        arclightcity.save.GameSaveState state =
                arclightcity.save.GameStateConverter.toSaveState(this, true);
        arclightcity.save.SaveManager.SaveResult result =
                arclightcity.save.SaveManager.saveAuto(state);
        System.out.println("[GameEngine] Auto-save: " + result.message());
    }

    /** Load game dari save terbaru (manual atau auto) */
    public boolean loadGame() {
        var opt = arclightcity.save.SaveManager.loadLatest();
        if (opt.isEmpty()) {
            System.out.println("[GameEngine] No save found.");
            return false;
        }
        arclightcity.save.GameStateConverter.restoreFromSave(this, opt.get());
        return true;
    }

    /** Cek apakah ada save yang bisa di-load */
    public boolean hasSave() {
        return arclightcity.save.SaveManager.hasSave();
    }

    /** Info save terbaru untuk ditampilkan di main menu */
    public String getSaveSummary() {
        return arclightcity.save.SaveManager.getSaveSummary();
    }

    /**
     * Buat character dari save data (dipakai oleh GameStateConverter).
     * Tidak memanggil giveStarterItems/giveStarterSkills karena data
     * sudah direstored dari save.
     */
    public void createCharacterFromSave(String name, PlayerBackground background,
                                         arclightcity.save.GameSaveState.PlayerData pd) {
        this.player    = new Player(name, background);
        this.inventory = new Inventory(player);
        this.dungeonManager = new DungeonManager();
        wireDungeonListeners();

        // Restore player stats dari save
        player.setLevelDirect(pd.level);
        player.setExpDirect(pd.currentExp, pd.expToNext);
        player.setGold(pd.gold);
        player.setHpDirect(pd.currentHp);
        player.setMpDirect(pd.currentMp);
        player.setShieldDirect(pd.currentShield);
        player.setSkillPointsDirect(pd.skillPoints);
        player.setDungeonDepth(pd.dungeonDepth);

        // Restore skills
        pd.unlockedSkillIds.forEach(player::forceUnlockSkill);
        pd.equippedSkillIds.forEach(player::equipSkill);

        transitionTo(GameState.HUB);
    }

    // Helper methods untuk GameStateConverter
    public void clearMercsForLoad()              { ownedMercs.clear(); activeMercs.clear(); }
    public void addOwnedMercForLoad(Mercenary m) { ownedMercs.add(m); }
    public void addActiveMercForLoad(Mercenary m){ if (!activeMercs.contains(m)) activeMercs.add(m); }

    // ════════════════════════════════════════════════════════
    // LISTENERS (untuk JavaFX binding)
    // ════════════════════════════════════════════════════════

    public void setOnStateChange(Consumer<GameState> l)          { onStateChange = l; }
    public void setOnDungeonEvent(Consumer<DungeonStateEvent> l) { onDungeonEvent = l; }
    public void setOnCombatStart(Consumer<CombatManager> l)      { onCombatStart = l; }
    public void setOnCombatEnd(Consumer<CombatResult> l)         { onCombatEnd = l; }
    public void setOnEventRoom(Consumer<DungeonEvent> l)         { onEventRoom = l; }

    // ════════════════════════════════════════════════════════
    // GETTERS
    // ════════════════════════════════════════════════════════

    public Player          getPlayer()         { return player; }
    public Inventory       getInventory()      { return inventory; }
    public DungeonManager  getDungeonManager() { return dungeonManager; }
    public GameState       getCurrentState()   { return currentState; }
    public List<Mercenary> getOwnedMercs()     { return java.util.Collections.unmodifiableList(ownedMercs); }
    public List<Mercenary> getActiveMercs()    { return java.util.Collections.unmodifiableList(activeMercs); }

    public CombatManager getActiveCombat() {
        return dungeonManager != null ? dungeonManager.getCombatManager() : null;
    }
}
