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
    private arclightcity.item.Inventory inventory; // untuk recalcEquipStats sebelum combat

    // ── Event Listeners (Observer) ───────────────────────────
    private Consumer<DungeonStateEvent> stateListener;
    private Consumer<CombatManager>     combatStartListener;
    private Consumer<CombatResult>      combatEndListener;
    private Consumer<DungeonEvent>      eventRoomListener;
    private Consumer<String>            shopOpenListener;

    // ── Constructor ─────────────────────────────────────────

    public DungeonManager() { }

    public void setInventory(arclightcity.item.Inventory inv) { this.inventory = inv; }

    // ════════════════════════════════════════════════════════
    // DUNGEON LIFECYCLE
    // ════════════════════════════════════════════════════════

    /**
     * Mulai dungeon run baru.
     */
    /** Reset state dungeon untuk new game tanpa kehilangan listener */
    public void resetState() {
        this.currentFloor       = null;
        this.currentFloorNumber = 0;
        this.dungeonActive      = false;
        this.player             = null;
        this.activeMercs        = new ArrayList<>();
    }

    /** Dipakai saat load save — restore floor ke posisi tersimpan */
    public void setCurrentFloorNumber(int floor) {
        this.currentFloorNumber = Math.max(0, floor);
    }

    public void startDungeon(Player player, List<Mercenary> mercs) {
        this.player      = player;
        this.activeMercs = new ArrayList<>(mercs);
        dungeonActive    = true;

        int savedFloor = player.getDungeonDepth();

        if (savedFloor <= 0) {
            // Fresh start — mulai dari floor 1
            currentFloorNumber = 0;
            emit(DungeonStateEvent.dungeonStarted(player.getName()));
            advanceToNextFloor();
        } else {
            // Lanjut dari floor yang sudah dicapai — JANGAN regenerate!
            currentFloorNumber = savedFloor;

            // Hanya generate floor baru jika belum ada atau nomor berbeda
            if (currentFloor == null || currentFloor.getFloorNumber() != currentFloorNumber) {
                currentFloor = ProceduralGenerator.generateFloor(currentFloorNumber);
                emit(DungeonStateEvent.dungeonStarted(player.getName()));
                emit(DungeonStateEvent.floorEntered(currentFloorNumber, currentFloor.getTheme()));
                enterRoom(pickEdgeStartTile()); // start dari edge acak
            } else {
                // Return ke floor yang SAMA — preserve visited state + posisi
                emit(DungeonStateEvent.dungeonStarted(player.getName()));
                emit(DungeonStateEvent.floorEntered(currentFloorNumber, currentFloor.getTheme()));
                // Re-enter posisi terakhir, bukan room 0!
                int resumeRoom = currentFloor.getCurrentRoomIndex();
                emit(DungeonStateEvent.roomEntered(currentFloor.getRoom(resumeRoom)));
            }
        }
    }

    /**
     * Pindah ke floor berikutnya.
     */
    public void advanceToNextFloor() {
        currentFloorNumber++;
        currentFloor = ProceduralGenerator.generateFloor(currentFloorNumber);
        player.setDungeonDepth(currentFloorNumber);

        emit(DungeonStateEvent.floorEntered(currentFloorNumber, currentFloor.getTheme()));

        // Mulai dari edge tile acak (atas/bawah/kiri/kanan)
        enterRoom(pickEdgeStartTile());
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

        // REST room special case: boleh dikunjungi berkali-kali (diminishing heal)
        // Cleared hanya berarti "sudah pernah dipakai sekali", bukan "tidak bisa lagi"
        if (room.isCleared() && room.getType() != Room.RoomType.REST) {
            emit(DungeonStateEvent.roomAlreadyCleared(room));
            return;
        }

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
        // Diminishing heal per kunjungan:
        //   Visit 1: 35% HP + 50% MP
        //   Visit 2: 20% HP + 30% MP
        //   Visit 3: 10% HP + 15% MP
        //   Visit 4+: tidak ada efek (room kelelahan)
        int useCount = room.getRestUseCount();
        room.incrementRestUse();

        double hpPct, mpPct;
        String visitMsg;
        switch (useCount) {
            case 0 -> { hpPct = 0.35; mpPct = 0.50; visitMsg = "You rest and recover."; }
            case 1 -> { hpPct = 0.20; mpPct = 0.30; visitMsg = "The zone is getting crowded. Partial recovery."; }
            case 2 -> { hpPct = 0.10; mpPct = 0.15; visitMsg = "Not much left here. Minimal recovery."; }
            default -> {
                emit(DungeonStateEvent.rest(0, 0,
                        "This rest zone is spent. Nothing left to recover."));
                // Tidak set cleared — player masih bisa lewat
                return;
            }
        }

        double hpRestore = player.getStats().get(StatType.MAX_HP) * hpPct;
        double mpRestore = player.getStats().get(StatType.MAX_MP) * mpPct;
        player.receiveHeal(hpRestore);
        player.restoreMp(mpRestore);

        // Merc juga pulih (proporsi sama)
        activeMercs.forEach(m -> {
            m.receiveHeal(m.getStats().get(StatType.MAX_HP) * hpPct * 0.85);
            m.restoreMp(m.getStats().get(StatType.MAX_MP) * mpPct * 0.85);
        });

        // REST room TIDAK set cleared — bisa dikunjungi lagi
        // cleared hanya menandai "sudah dipakai pertama kali"
        if (useCount == 0) room.setCleared(true);

        emit(DungeonStateEvent.rest(hpRestore, mpRestore, visitMsg));
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

        // Recalc equipment stats sebelum combat (fix: on-hit effects)
        if (inventory != null) player.recalcEquipStats(inventory);
        // Setup combat
        combatManager.setup(player, activeMercs, enemies);

        // Reset listeners sebelum combat baru — cegah accumulate
        combatManager.clearResultListeners();

        // Listen hasil combat (slot 1: DungeonManager logic)
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
            case REVEAL_MAP    -> {
                // Reveal semua room di floor saat ini
                if (currentFloor != null) {
                    currentFloor.getRooms().forEach(r -> r.setVisited(true));
                }
                emit(DungeonStateEvent.mapRevealed());
            }
            case CALIBRATION  -> emit(DungeonStateEvent.calibrationAvailable(result.intValue));
            case NOTHING      -> emit(DungeonStateEvent.nothing());
            default           -> { }
        }
    }

    // ════════════════════════════════════════════════════════
    // FLOOR / DUNGEON END
    // ════════════════════════════════════════════════════════

    private void onFloorCompleted() {
        emit(DungeonStateEvent.floorCompleted(currentFloorNumber));

        // Setelah menang: pulihkan MP dan Shield PENUH, HP 30%
        // (Beri waktu istirahat antar floor tapi tetap butuh manajemen HP)
        double maxMp  = player.getStats().get(StatType.MAX_MP);
        double maxShd = player.getStats().get(StatType.MAX_SHIELD);
        double maxHp  = player.getStats().get(StatType.MAX_HP);
        player.receiveHeal(maxHp  * 0.30);
        player.restoreMp(maxMp); // MP penuh
        player.restoreShield(maxShd); // Shield penuh

        // Guildmate: beri sedikit regen antar lantai tapi JANGAN reset ke 25%
        for (var merc : getActiveMercs()) {
            if (merc != null && merc.isAlive()) {
                double mercMaxHp = merc.getStats().get(StatType.MAX_HP);
                double heal      = mercMaxHp * 0.15;
                double newHp     = Math.min(merc.getCurrentHp() + heal, mercMaxHp);
                merc.setHpDirect(newHp);
            }
        }

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
    /** Pilih tile start acak dari tepi grid (atas/bawah/kiri/kanan) */
    private int pickEdgeStartTile() {
        int totalRooms = currentFloor.getTotalRooms();
        if (totalRooms <= 1) return 0;
        int cols = arclightcity.dungeon.ProceduralGenerator.COLS;
        int rows = (totalRooms + cols - 1) / cols;
        java.util.List<Integer> edges = new java.util.ArrayList<>();
        for (int c2 = 0; c2 < cols; c2++) { edges.add(c2); edges.add((rows-1)*cols + c2); } // top + bottom
        for (int r = 1; r < rows-1; r++) { edges.add(r*cols); edges.add(r*cols + cols-1); } // left + right
        edges.removeIf(i -> i >= totalRooms);
        return edges.isEmpty() ? 0 : edges.get(new java.util.Random().nextInt(edges.size()));
    }


    /** True jika room saat ini adalah boss room (floor milestone) */
    public boolean isCurrentRoomBoss() {
        int f = getCurrentFloorNumber();
        return (f == 10 || f == 20 || f == 30 || f == 40 || f == 50 || f >= 51);
    }


}
