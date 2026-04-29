package arclightcity.dungeon;
import arclightcity.combat.CombatManager;
import arclightcity.combat.CombatResult;
import arclightcity.entity.stats.DamageType;
import arclightcity.entity.stats.StatType;

import arclightcity.entity.EntityFactory;
import arclightcity.entity.enemy.Enemy;
import arclightcity.entity.mercenary.Mercenary;
import arclightcity.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * DungeonManager — controller utama seluruh dungeon run.
 *
 * Mengelola:
 *   - State dungeon saat ini (floor berapa, room mana)
 *   - Navigasi antar room
 *   - Trigger combat saat masuk ENEMY room
 *   - Trigger event saat masuk EVENT/TRAP room
 *   - Transisi antar floor
 *   - Kondisi game over / victory
 */
public class DungeonManager {

    // ── State ─────────────────────────────────────────────────
    private Player           player;
    private List<Mercenary>  activeMercs = new ArrayList<>();
    private Floor            currentFloor;
    private int              currentFloorNumber = 0;
    private boolean          dungeonActive      = false;

    // ── Sub-managers ─────────────────────────────────────────
    private final CombatManager combatManager = new CombatManager();

    // ── Event Listeners (Observer) ───────────────────────────
    private Consumer<DungeonStateEvent> stateListener;
    private Consumer<CombatManager>     combatStartListener;
    private Consumer<CombatResult>      combatEndListener;
    private Consumer<DungeonEvent>      eventRoomListener;
    private Consumer<String>            shopOpenListener;

    // ── Constructor ─────────────────────────────────────────

    public DungeonManager() { }

    // ════════════════════════════════════════════════════════
    // DUNGEON LIFECYCLE
    // ════════════════════════════════════════════════════════

    /**
     * Mulai dungeon run baru.
     */
    public void startDungeon(Player player, List<Mercenary> mercs) {
        this.player      = player;
        this.activeMercs = new ArrayList<>(mercs);
        currentFloorNumber = 0;
        dungeonActive    = true;

        emit(DungeonStateEvent.dungeonStarted(player.getName()));
        advanceToNextFloor();
    }

    /**
     * Pindah ke floor berikutnya.
     */
    public void advanceToNextFloor() {
        currentFloorNumber++;
        currentFloor = ProceduralGenerator.generateFloor(currentFloorNumber);
        player.setDungeonDepth(currentFloorNumber);

        emit(DungeonStateEvent.floorEntered(currentFloorNumber, currentFloor.getTheme()));

        // Auto-masuk room pertama (EMPTY)
        enterRoom(0);
    }

    /**
     * Player memilih pindah ke room tertentu.
     * @param roomIndex index room tujuan
     * @return true jika berhasil pindah
     */
    public boolean moveToRoom(int roomIndex) {
        if (!dungeonActive) return false;

        boolean moved = currentFloor.moveToRoom(roomIndex);
        if (!moved) {
            emit(DungeonStateEvent.error("Cannot move to room " + roomIndex));
            return false;
        }

        enterRoom(roomIndex);
        return true;
    }

    // ════════════════════════════════════════════════════════
    // ROOM ENTRY
    // ════════════════════════════════════════════════════════

    private void enterRoom(int roomIndex) {
        Room room = currentFloor.getRoom(roomIndex);
        if (room == null) return;

        room.setVisited(true);
        emit(DungeonStateEvent.roomEntered(room));

        // ── Backtrack guard ───────────────────────────────────
        // Jika room sudah cleared, tidak perlu trigger event lagi.
        // Player hanya melewati / istirahat sebentar.
        if (room.isCleared()) {
            emit(DungeonStateEvent.roomAlreadyCleared(room));
            return;
        }

        // Room belum cleared — trigger event sesuai tipe
        switch (room.getType()) {
            case EMPTY  -> handleEmptyRoom(room);
            case REST   -> handleRestRoom(room);
            case ENEMY  -> handleEnemyRoom(room, false);
            case ELITE  -> handleEnemyRoom(room, true);
            case BOSS   -> handleBossRoom(room);
            case LOOT   -> handleLootRoom(room);
            case EVENT  -> handleEventRoom(room);
            case TRAP   -> handleTrapRoom(room);
            case SHOP   -> handleShopRoom(room);
        }
    }

    // ── Room Handlers ────────────────────────────────────────

    private void handleEmptyRoom(Room room) {
        room.setCleared(true);
        emit(DungeonStateEvent.roomCleared(room, "The room is empty. A moment of quiet in the chaos."));
    }

    private void handleRestRoom(Room room) {
        // Pulihkan 35% HP dan 50% MP
        double hpRestore = player.getStats().get(StatType.MAX_HP) * 0.35;
        double mpRestore = player.getStats().get(StatType.MAX_MP) * 0.50;
        player.receiveHeal(hpRestore);
        player.restoreMp(mpRestore);

        // Merc juga pulih
        activeMercs.forEach(m -> {
            m.receiveHeal(m.getStats().get(StatType.MAX_HP) * 0.30);
            m.restoreMp(m.getStats().get(StatType.MAX_MP) * 0.40);
        });

        room.setCleared(true);
        emit(DungeonStateEvent.rest(hpRestore, mpRestore));
    }

    private void handleEnemyRoom(Room room, boolean isElite) {
        if (room.isCleared()) {
            emit(DungeonStateEvent.roomAlreadyCleared(room));
            return;
        }

        List<Enemy> enemies = room.getEnemies();
        if (enemies == null || enemies.isEmpty()) {
            room.setCleared(true);
            return;
        }

        // Setup combat
        combatManager.setup(player, activeMercs, enemies);

        // Listen hasil combat
        combatManager.addResultListener(result -> {
            if (result.isVictory()) {
                room.setCleared(true);
                emit(DungeonStateEvent.roomCleared(room,
                        "Enemies defeated! +" + (int)result.getTotalExpGained()
                        + " EXP, +" + result.getTotalGoldGained() + " Gold"));

                // Cek apakah floor selesai
                if (currentFloor.checkCompletion()) {
                    onFloorCompleted();
                }
            } else if (result.isDefeat()) {
                onPlayerDefeated();
            }
            // Notify UI
            if (combatEndListener != null) combatEndListener.accept(result);
        });

        // Notify UI untuk tampilkan combat screen
        if (combatStartListener != null) combatStartListener.accept(combatManager);

        emit(DungeonStateEvent.combatStarted(enemies, isElite));
    }

    private void handleBossRoom(Room room) {
        if (room.isCleared()) {
            emit(DungeonStateEvent.roomAlreadyCleared(room));
            return;
        }
        // Boss menggunakan combat yang sama, hanya flagging beda
        handleEnemyRoom(room, false);
        emit(DungeonStateEvent.bossEncountered(room.getEnemies().get(0).getName()));
    }

    private void handleLootRoom(Room room) {
        if (room.isCleared()) {
            emit(DungeonStateEvent.roomAlreadyCleared(room));
            return;
        }
        room.setCleared(true);
        // LootManager akan generate actual items — di sini hanya emit event
        emit(DungeonStateEvent.lootFound(room.getLootItemIds(), currentFloorNumber));
    }

    private void handleEventRoom(Room room) {
        if (room.isCleared()) {
            emit(DungeonStateEvent.roomAlreadyCleared(room));
            return;
        }

        String eventId = room.getEventId();
        DungeonEvent event = DungeonEvent.fromId(eventId);

        if (event.hasChoices()) {
            // Kirim event ke UI untuk player pilih
            if (eventRoomListener != null) eventRoomListener.accept(event);
            emit(DungeonStateEvent.eventEncountered(event));
        } else {
            // Trap / auto-event
            DungeonEvent.EventResult result = event.applyTrap(player);
            handleEventResult(result, room);
        }

        room.setCleared(true);
    }

    private void handleTrapRoom(Room room) {
        handleEventRoom(room); // trap adalah event tanpa pilihan
    }

    private void handleShopRoom(Room room) {
        room.setCleared(true);
        if (shopOpenListener != null) shopOpenListener.accept(room.getShopId());
        emit(DungeonStateEvent.shopOpened(room.getShopId()));
    }

    // ── Event Result Handler ──────────────────────────────────

    /**
     * Proses hasil dari pilihan event / trap.
     * Dipanggil dari UI saat player memilih opsi event.
     */
    public void resolveEventChoice(DungeonEvent event, int choiceIndex) {
        if (choiceIndex < 0 || choiceIndex >= event.getChoices().length) return;

        DungeonEvent.EventChoice choice = event.getChoices()[choiceIndex];
        DungeonEvent.EventResult result = choice.effect.apply(player);

        Room room = currentFloor.getCurrentRoom();
        handleEventResult(result, room);
    }

    private void handleEventResult(DungeonEvent.EventResult result, Room room) {
        switch (result.type) {
            case HEAL    -> emit(DungeonStateEvent.healed((int) result.intValue));
            case DAMAGE  -> {
                player.receiveDamage(result.intValue, DamageType.TRUE, true);
                emit(DungeonStateEvent.damaged(result.intValue));
                if (!player.isAlive()) onPlayerDefeated();
            }
            case EXP     -> emit(DungeonStateEvent.expGained(result.intValue));
            case GOLD    -> emit(DungeonStateEvent.goldGained(result.longValue));
            case LOOT_COMMON, LOOT_RARE -> emit(DungeonStateEvent.lootFound(
                    List.of("LOOT_EVENT_" + result.type), currentFloorNumber));
            case DEBUFF  -> emit(DungeonStateEvent.debuffApplied(result.stringValue));
            case SPAWN_ENEMY -> {
                // Trigger bonus enemy encounter
                List<Enemy> bonus = EntityFactory.generateEncounter(currentFloorNumber, 2);
                Room bonusRoom = new Room(-1, Room.RoomType.ENEMY,
                        List.of(currentFloor.getCurrentRoomIndex()));
                bonusRoom.setEnemies(bonus);
                handleEnemyRoom(bonusRoom, false);
            }
            case OPEN_SHOP   -> handleShopRoom(room);
            case REVEAL_MAP  -> emit(DungeonStateEvent.mapRevealed());
            case NOTHING     -> emit(DungeonStateEvent.nothing());
            default          -> { }
        }
    }

    // ════════════════════════════════════════════════════════
    // FLOOR / DUNGEON END
    // ════════════════════════════════════════════════════════

    private void onFloorCompleted() {
        emit(DungeonStateEvent.floorCompleted(currentFloorNumber));

        // Pulihkan sebagian HP/MP antar floor
        player.receiveHeal(player.getStats().get(StatType.MAX_HP) * 0.20);
        player.restoreMp(player.getStats().get(StatType.MAX_MP) * 0.30);

        emit(DungeonStateEvent.readyForNextFloor(currentFloorNumber + 1));
    }

    private void onPlayerDefeated() {
        dungeonActive = false;
        emit(DungeonStateEvent.gameOver(currentFloorNumber, player.getTotalDamageDealt()));
    }

    // ════════════════════════════════════════════════════════
    // LISTENERS
    // ════════════════════════════════════════════════════════

    public void setStateListener(Consumer<DungeonStateEvent> l)      { stateListener = l; }
    public void setCombatStartListener(Consumer<CombatManager> l)    { combatStartListener = l; }
    public void setCombatEndListener(Consumer<CombatResult> l)       { combatEndListener = l; }
    public void setEventRoomListener(Consumer<DungeonEvent> l)       { eventRoomListener = l; }
    public void setShopOpenListener(Consumer<String> l)              { shopOpenListener = l; }

    private void emit(DungeonStateEvent event) {
        if (stateListener != null) stateListener.accept(event);
    }

    // ════════════════════════════════════════════════════════
    // GETTERS
    // ════════════════════════════════════════════════════════

    public Player          getPlayer()             { return player; }
    public Floor           getCurrentFloor()       { return currentFloor; }
    public int             getCurrentFloorNumber() { return currentFloorNumber; }
    public boolean         isDungeonActive()       { return dungeonActive; }
    public CombatManager   getCombatManager()      { return combatManager; }
    public List<Mercenary> getActiveMercs()        { return activeMercs; }

    public Room getCurrentRoom() {
        return currentFloor != null ? currentFloor.getCurrentRoom() : null;
    }

    public List<Room> getAvailableNextRooms() {
        Room current = getCurrentRoom();
        if (current == null) return List.of();
        return current.getNextRoomIndexes().stream()
                .map(i -> currentFloor.getRoom(i))
                .filter(r -> r != null)
                .toList();
    }
}
