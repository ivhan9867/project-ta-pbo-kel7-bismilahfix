package arclightcity.combat;
import arclightcity.entity.mercenary.MercenaryType;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;
import arclightcity.entity.stats.DamageType;

import arclightcity.combat.CombatAction;
import arclightcity.entity.enemy.Boss;
import arclightcity.entity.enemy.Enemy;
import arclightcity.entity.mercenary.Mercenary;
import arclightcity.entity.player.Player;

import java.util.*;
import java.util.function.Consumer;

/**
 * CombatManager — pusat kendali seluruh battle.
 *
 * Alur satu battle:
 *   1. setup()      → inisialisasi semua combatant, turn queue, synergy
 *   2. nextTurn()   → ambil entity berikutnya, proses turn-nya
 *   3. executeAction() → jalankan aksi (attack/skill/item/defend)
 *   4. checkEndCondition() → cek apakah battle sudah selesai
 *   5. finalize()   → hitung reward, buat CombatResult
 *
 * CombatManager tidak tahu tentang UI — semua event dikirim lewat
 * eventListener (Observer pattern) untuk ditangkap JavaFX.
 */
public class CombatManager {

    // ── Combatants ───────────────────────────────────────────
    private Player              player;
    private List<Mercenary>     activeMercs   = new ArrayList<>();
    private List<Enemy>         enemies       = new ArrayList<>();
    private List<Entity>        allAllies     = new ArrayList<>(); // player + mercs
    private List<Entity>        allEnemies    = new ArrayList<>(); // enemies

    // ── Combat State ─────────────────────────────────────────
    private final TurnQueue     turnQueue     = new TurnQueue();
    private int                 totalTurns    = 0;
    private boolean             combatActive  = false;
    private boolean             playerFleeing = false;

    // ── Event System ─────────────────────────────────────────
    // Observer pattern: UI subscribe ke sini untuk menerima event
    private final List<Consumer<CombatEvent>>  eventListeners  = new ArrayList<>();
    private final List<Consumer<List<CombatEvent>>> batchListeners = new ArrayList<>();

    // ── Pending player action ─────────────────────────────────
    // Diset oleh UI, dieksekusi saat giliran player tiba
    private CombatAction        pendingPlayerAction = null;
    private boolean             waitingForPlayer    = false;

    // ── Constructor ─────────────────────────────────────────

    public CombatManager() { }

    // ════════════════════════════════════════════════════════
    // SETUP
    // ════════════════════════════════════════════════════════

    /**
     * Siapkan combat baru.
     * @param player      player character
     * @param mercs       mercenary yang dibawa (max 2)
     * @param enemyGroup  kelompok enemy yang akan dilawan
     */
    public void setup(Player player, List<Mercenary> mercs, List<Enemy> enemyGroup) {
        this.player = player;
        this.activeMercs.clear();
        this.enemies.clear();
        this.allAllies.clear();
        this.allEnemies.clear();

        // Setup allies
        this.allAllies.add(player);
        for (Mercenary m : mercs) {
            if (activeMercs.size() < 2) { // max 2 merc
                activeMercs.add(m);
                allAllies.add(m);
            }
        }

        // Setup enemies
        this.enemies.addAll(enemyGroup);
        this.allEnemies.addAll(enemyGroup);

        // Apply mercenary synergies
        for (Mercenary m : activeMercs) {
            m.checkSynergy(activeMercs);
            if (activeMercs.size() > 1) {
                CombatEvent synergyEvent = new CombatEvent.Builder(CombatEvent.EventType.MERCENARY_SYNERGY)
                        .actor(m.getId(), m.getName())
                        .message("🔗 Synergy: " + m.getName() + " bonuses applied!")
                        .build();
                emit(synergyEvent);
            }
        }

        // Init turn queue
        turnQueue.initialize(allAllies, allEnemies);

        combatActive  = true;
        totalTurns    = 0;
        playerFleeing = false;

        emit(new CombatEvent.Builder(CombatEvent.EventType.COMBAT_START)
                .message("⚔️ Combat begins! " + enemyGroup.size() + " enemy(ies) encountered.")
                .build());
    }

    // ════════════════════════════════════════════════════════
    // TURN EXECUTION
    // ════════════════════════════════════════════════════════

    /**
     * Proses turn berikutnya.
     * Jika entity yang giliran adalah PLAYER → set waitingForPlayer = true,
     * tunggu input dari UI via submitPlayerAction().
     * Jika entity adalah AI → proses otomatis.
     *
     * @return true jika combat masih berlanjut, false jika sudah selesai
     */
    public boolean processTurn() {
        if (!combatActive) return false;

        // Ambil entity yang giliran
        Entity current = turnQueue.getCurrentTurnEntity();

        // Round selesai → mulai round baru
        if (current == null) {
            turnQueue.startNewRound();
            current = turnQueue.getCurrentTurnEntity();
            if (current == null) return false;
        }

        // Jika entity mati → skip
        if (!current.isAlive()) {
            turnQueue.advance();
            return checkEndCondition() == null;
        }

        totalTurns++;
        emit(CombatEvent.turnStart(current.getId(), current.getName(), totalTurns));

        // MP REGEN per turn: Guildmate regen 8 MP, Player regen 5 MP
        double mpRegen = (current instanceof arclightcity.entity.mercenary.Mercenary) ? 8 : 5;
        current.restoreMp(mpRegen);

        // Turn start lifecycle
        current.onTurnStart();
        emitDotResults(current);

        // Cek apakah entity bisa bertindak
        if (!current.canAct()) {
            String reason = getCannotActReason(current);
            emit(CombatEvent.actionPrevented(current.getId(), current.getName(), reason));
            current.onTurnEnd();
            turnQueue.advance();
            checkEndCondition();
            return combatActive;
        }

        // ── Player turn ───────────────────────────────────────
        if (current.getEntityType() == EntityType.PLAYER) {
            waitingForPlayer = true;
            return true; // tunggu input dari UI
        }

        // ── AI turn (Mercenary atau Enemy) ────────────────────
        CombatAction action;
        if (current.getEntityType() == EntityType.MERCENARY) {
            action = current.decideAction(allAllies, allEnemies);
        } else {
            // Enemy menyerang allies
            action = current.decideAction(allEnemies, allAllies);
        }

        executeAction(current, action);

        current.onTurnEnd();
        turnQueue.advance();

        CombatResult result = checkEndCondition();
        if (result != null) {
            combatActive = false;
            emitCombatEnd(result);
            return false;
        }

        return true;
    }

    /**
     * Dipanggil oleh UI saat player memilih aksi.
     */
    public void submitPlayerAction(CombatAction action) {
        if (!waitingForPlayer || !combatActive) return;
        waitingForPlayer = false;

        Entity playerEntity = player;
        executeAction(playerEntity, action);

        playerEntity.onTurnEnd();
        turnQueue.advance();

        CombatResult result = checkEndCondition();
        if (result != null) {
            combatActive = false;
            emitCombatEnd(result);
        }
    }

    // ════════════════════════════════════════════════════════
    // ACTION EXECUTION
    // ════════════════════════════════════════════════════════

    private void executeAction(Entity actor, CombatAction action) {
        List<CombatEvent> events;

        switch (action.getActionType()) {

            case BASIC_ATTACK -> {
                events = executeBasicAttack(actor, action.getTargetIds());
                emitBatch(events);
            }

            case USE_SKILL -> {
                events = executeSkill(actor, action.getSkillId(), action.getTargetIds());
                emitBatch(events);
            }

            case DEFEND -> {
                // Pasang Fortify 1 turn
                actor.applyEffect(new StatusEffect(StatusEffectType.FORTIFY, 1, 0, actor.getId()));
                emit(new CombatEvent.Builder(CombatEvent.EventType.EFFECT_APPLIED)
                        .actor(actor.getId(), actor.getName())
                        .message("🛡️ " + actor.getName() + " takes a defensive stance.")
                        .build());
            }

            case USE_ITEM -> {
                // Heal item — CombatManager tidak akses inventory langsung
                // heal amount diambil dari itemId yang di-encode saat CombatView submit
                double healAmt = 50; // default fallback
                String itemName = "Item";
                try {
                    String iid = action.getItemId();
                    if (iid != null) {
                        // Format: "itemId|healValue|itemName" jika ada pipe separator
                        if (iid.contains("|")) {
                            String[] parts = iid.split("\\|");
                            healAmt = Double.parseDouble(parts[1]);
                            if (parts.length > 2) itemName = parts[2];
                        }
                    }
                } catch (Exception ignored2) {}
                double actualHeal = actor.receiveHeal(healAmt);
                emit(new CombatEvent.Builder(CombatEvent.EventType.HEAL_RECEIVED)
                        .actor(actor.getId(), actor.getName())
                        .target(actor.getId(), actor.getName())
                        .value(actualHeal)
                        .message(actor.getName() + " menggunakan " + itemName +
                                 ", memulihkan " + (int)actualHeal + " HP")
                        .build());
            }

            case PASS -> {
                emit(CombatEvent.actionPrevented(actor.getId(), actor.getName(), "No action"));
            }
        }

        // Boss phase check setelah setiap aksi
        if (actor instanceof Boss boss) {
            checkBossPhase(boss);
        }
    }

    // ── Basic Attack ─────────────────────────────────────────

    private List<CombatEvent> executeBasicAttack(Entity actor, List<String> targetIds) {
        List<CombatEvent> events = new ArrayList<>();
        List<Entity> targets = resolveTargets(targetIds);

        DamageType dmgType = DamageCalculator.getDominantDamageType(actor);

        for (Entity target : targets) {
            if (!target.isAlive()) continue;

            double baseDmg = DamageCalculator.getBasicAttackDamage(actor, dmgType);
            var    calc    = DamageCalculator.calculate(actor, target, baseDmg, dmgType, 0);

            if (calc.isMissed) {
                events.add(CombatEvent.evaded(actor.getId(), actor.getName(),
                        target.getId(), target.getName()));
                continue;
            }

            Entity.DamageResult result = target.receiveDamage(calc.finalDamage, dmgType, false);
            actor.addDamageDealt(result.damage);

            if (calc.isCritical) {
                events.add(CombatEvent.critDamage(actor.getId(), actor.getName(),
                        target.getId(), target.getName(), result.damage, dmgType));
            } else if (result.blocked) {
                events.add(CombatEvent.blocked(actor.getId(), actor.getName(),
                        target.getId(), target.getName(), result.damage));
            } else if (result.shieldDamage > 0) {
                events.add(CombatEvent.shieldDamage(actor.getId(), actor.getName(),
                        target.getId(), target.getName(),
                        result.damage, result.shieldDamage, dmgType));
            } else {
                events.add(CombatEvent.damage(actor.getId(), actor.getName(),
                        target.getId(), target.getName(), result.damage, dmgType));
            }

            // Lifesteal
            double ls = DamageCalculator.applyLifesteal(actor, result.damage);
            if (ls > 0) events.add(CombatEvent.heal(actor.getId(), actor.getName(),
                    actor.getId(), actor.getName(), ls));

            // Death check
            if (!target.isAlive()) {
                events.add(CombatEvent.death(target.getId(), target.getName()));
                onEntityDeath(target);
            }
        }

        return events;
    }

    // ── Skill Execution ──────────────────────────────────────

    private List<CombatEvent> executeSkill(Entity actor, String skillId, List<String> targetIds) {
        List<Entity> targets = resolveTargets(targetIds);

        // Cek MP
        double mpCost = getSkillMpCost(skillId);
        if (!actor.spendMp(mpCost)) {
            List<CombatEvent> fail = new ArrayList<>();
            fail.add(new CombatEvent.Builder(CombatEvent.EventType.SKILL_FAILED)
                    .actor(actor.getId(), actor.getName())
                    .message(actor.getName() + " lacks MP for " + skillId
                             + " (needs " + (int)mpCost + " MP)")
                    .build());
            // Fallback ke basic attack
            fail.addAll(executeBasicAttack(actor, targetIds));
            return fail;
        }

        List<CombatEvent> events = SkillExecutor.execute(skillId, actor, targets, allAllies, allEnemies);

        // Check semua target kematian setelah skill
        for (Entity target : targets) {
            if (!target.isAlive() && events.stream()
                    .noneMatch(e -> e.getType() == CombatEvent.EventType.ENTITY_DIED
                                && e.getActorId().equals(target.getId()))) {
                events.add(CombatEvent.death(target.getId(), target.getName()));
                onEntityDeath(target);
            }
        }

        return events;
    }

    // ── HACK: enemy menyerang rekan sendiri ──────────────────

    /**
     * Jika entity punya HACK → paksa menyerang rekan sendiri.
     * Dipanggil sebelum AI decide action.
     */
    private boolean handleHackEffect(Entity actor, List<Entity> allies) {
        if (!actor.hasEffect(StatusEffectType.HACK)) return false;

        List<Entity> aliveAllies = allies.stream()
                .filter(a -> a.isAlive() && !a.getId().equals(actor.getId()))
                .toList();

        if (aliveAllies.isEmpty()) return false;

        Entity hackTarget = aliveAllies.get(new Random().nextInt(aliveAllies.size()));
        emit(new CombatEvent.Builder(CombatEvent.EventType.EFFECT_APPLIED)
                .actor(actor.getId(), actor.getName())
                .target(hackTarget.getId(), hackTarget.getName())
                .effectType(StatusEffectType.HACK)
                .message("💻 " + actor.getName() + " is HACKED — attacks " + hackTarget.getName() + "!")
                .build());

        List<CombatEvent> events = executeBasicAttack(actor, List.of(hackTarget.getId()));
        emitBatch(events);
        actor.removeEffect(StatusEffectType.HACK);
        return true;
    }

    // ════════════════════════════════════════════════════════
    // END CONDITION
    // ════════════════════════════════════════════════════════

    /**
     * Cek apakah combat sudah selesai.
     * @return CombatResult jika selesai, null jika masih berlanjut
     */
    private CombatResult checkEndCondition() {
        // Player fled
        if (playerFleeing) {
            return CombatResult.fled(totalTurns, collectLog());
        }

        // Semua enemy mati → Victory
        boolean allEnemiesDead = allEnemies.stream().noneMatch(Entity::isAlive);
        if (allEnemiesDead) {
            return buildVictoryResult();
        }

        // Player mati → Defeat
        if (!player.isAlive()) {
            return CombatResult.defeat(totalTurns, collectLog());
        }

        return null; // combat masih berlanjut
    }

    private CombatResult buildVictoryResult() {
        double totalExp  = 0;
        long   totalGold = 0;
        List<String> loot = new ArrayList<>();
        List<Enemy>  defeated = new ArrayList<>();

        for (Enemy e : enemies) {
            if (!e.isAlive()) {
                totalExp  += e.getExpReward();
                totalGold += e.getGoldReward();
                defeated.add(e);
                // Loot table akan di-resolve oleh LootManager
                loot.add(e.getLootTableId());
            }
        }

        // Grant EXP ke player
        int levelsGained = player.gainExp(totalExp);
        if (levelsGained > 0) {
            emit(new CombatEvent.Builder(CombatEvent.EventType.COMBAT_END)
                    .actor(player.getId(), player.getName())
                    .value(levelsGained)
                    .message("🎉 " + player.getName() + " gained " + levelsGained + " level(s)!")
                    .build());
        }

        // Grant gold
        player.gainGold(totalGold);

        // Mercenary mission complete + revive yang mati dengan 25% HP
        for (Mercenary m : activeMercs) {
            if (m.isAlive()) {
                m.completeMission();
            } else {
                // Revive dengan 25% HP setelah menang
                double reviveHp = m.getStats().get(arclightcity.entity.stats.StatType.MAX_HP) * 0.25;
                m.restoreVitals(reviveHp, 0, 0);
            }
        }

        CombatResult result = CombatResult.victory(totalTurns, totalExp, totalGold, loot, collectLog(), defeated);
        result.setLevelsGained(levelsGained);
        return result;
    }

    // ════════════════════════════════════════════════════════
    // PLAYER FLEE
    // ════════════════════════════════════════════════════════

    /**
     * Player mencoba melarikan diri.
     * Chance berhasil berdasarkan SPEED player vs rata-rata SPEED enemy.
     */
    public boolean attemptFlee() {
        double playerSpeed = player.getStats().get(StatType.SPEED);
        double avgEnemySpeed = allEnemies.stream()
                .filter(Entity::isAlive)
                .mapToDouble(e -> e.getStats().get(StatType.SPEED))
                .average().orElse(10);

        double fleeChance = Math.min(0.85, 0.40 + (playerSpeed - avgEnemySpeed) * 0.05);
        boolean success   = Math.random() < fleeChance;

        if (success) {
            playerFleeing = true;
            emit(new CombatEvent.Builder(CombatEvent.EventType.ENTITY_FLED)
                    .actor(player.getId(), player.getName())
                    .message("💨 " + player.getName() + " successfully fled the battle!")
                    .build());
        } else {
            emit(new CombatEvent.Builder(CombatEvent.EventType.ACTION_PREVENTED)
                    .actor(player.getId(), player.getName())
                    .message("❌ Failed to flee! Enemies block the way.")
                    .build());
        }

        return success;
    }

    // ════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════

    private List<Entity> resolveTargets(List<String> targetIds) {
        List<Entity> all = new ArrayList<>(allAllies);
        all.addAll(allEnemies);
        return all.stream()
                .filter(e -> targetIds.contains(e.getId()))
                .toList();
    }

    private void onEntityDeath(Entity dead) {
        turnQueue.removeCombatant(dead.getId());

        // Lyra Bloom: soul link — transfer HP ke sekutu saat mati
        if (dead instanceof Mercenary m && m.getMercenaryType()
                == MercenaryType.LYRA_BLOOM) {
            double transferHp = dead.getStats().get(StatType.MAX_HP) * 0.15;
            allAllies.stream().filter(Entity::isAlive).forEach(a -> {
                double healed = a.receiveHeal(transferHp);
                emit(CombatEvent.heal(dead.getId(), dead.getName(),
                        a.getId(), a.getName(), healed));
            });
        }
    }

    private void emitDotResults(Entity entity) {
        List<Entity.DotTickResult> dots = entity.tickEffects();
        for (Entity.DotTickResult dot : dots) {
            emit(CombatEvent.effectTick(entity.getId(), entity.getName(),
                    dot.effectType, dot.damage));
            if (!entity.isAlive()) {
                emit(CombatEvent.death(entity.getId(), entity.getName()));
                onEntityDeath(entity);
                break;
            }
        }
    }

    private void checkBossPhase(Boss boss) {
        int prev = boss.getCurrentPhase();
        // Phase check sudah di-handle di Boss.onTurnStart()
        // Di sini hanya emit event jika phase berubah
        // (implementasi sederhana — bisa diexpand dengan listener)
    }

    private String getCannotActReason(Entity e) {
        if (e.isStunned())  return "Stunned";
        if (e.isFrozen())   return "Frozen";
        if (e.isAsleep())   return "Asleep";
        if (e.hasEffect(StatusEffectType.FEAR) && Math.random() < 0.5) return "Feared";
        return "Incapacitated";
    }

    private double getSkillMpCost(String skillId) {
        // MP cost per skill — bisa dipindah ke SkillRegistry nanti
        return switch (skillId) {
            case "POWER_STRIKE", "NEON_VENOM", "COIL_STRIKE"   -> 15;
            case "PHANTOM_SHOT", "SHADOW_STEP", "DEEP_HACK"     -> 25;
            case "EXECUTE"                                        -> 30;
            case "VIRUS_UPLOAD", "EMP_BURST", "SIGNAL_JAM"      -> 20;
            case "SELF_DESTRUCT"                                  -> 0; // free (nyawa taruhannya)
            case "SHOCKWAVE", "VOID_RUPTURE", "CORRUPT"          -> 30;
            case "NULL_FIELD", "DATA_FRAGMENTATION"              -> 40;
            case "SOVEREIGN_STRIKE", "NULL_PROTOCOL"             -> 50;
            case "IRON_SHIELD", "FIELD_BARRIER"                  -> 20;
            case "EMERGENCY_REPAIR", "SELF_MEND"                 -> 25;
            case "TRIAGE_HEAL", "BLOOM_MEND"                     -> 30;
            case "CLEANSE_PROTOCOL"                              -> 20;
            case "NEON_BLOOM"                                     -> 40;
            case "RESONANCE"                                      -> 35;
            case "ENERGY_DRAIN"                                   -> 15;
            case "FREQUENCY_LOCK"                                 -> 30;
            case "INCENDIARY_ROUND", "SHRED_CANNON"              -> 20;
            case "OVERLOAD_SHOT"                                  -> 45;
            case "OVERLOAD_CHARGE", "OVERLOAD_CHARGE_READY"      -> 0;
            default                                               -> 10;
        };
    }

    private final List<String> combatLogMessages = new ArrayList<>();

    private List<String> collectLog() {
        return Collections.unmodifiableList(combatLogMessages);
    }

    // ════════════════════════════════════════════════════════
    // EVENT SYSTEM (Observer Pattern)
    // ════════════════════════════════════════════════════════

    /** Subscribe untuk menerima event satu per satu */
    public void addEventListener(Consumer<CombatEvent> listener) {
        eventListeners.clear(); // reset dulu baru tambah
        eventListeners.add(listener);
    }

    /** Subscribe untuk menerima batch event (hasil satu aksi) */
    public void addBatchListener(Consumer<List<CombatEvent>> listener) {
        batchListeners.clear(); // reset dulu baru tambah
        batchListeners.add(listener);
    }

    private void emit(CombatEvent event) {
        combatLogMessages.add(event.getMessage());
        eventListeners.forEach(l -> l.accept(event));
    }

    private void emitBatch(List<CombatEvent> events) {
        events.forEach(e -> combatLogMessages.add(e.getMessage()));
        batchListeners.forEach(l -> l.accept(events));
        events.forEach(e -> eventListeners.forEach(l -> l.accept(e)));
    }

    private void emitCombatEnd(CombatResult result) {
        String msg = switch (result.getOutcome()) {
            case VICTORY -> "🏆 Victory! Enemies defeated in " + totalTurns + " turns.";
            case DEFEAT  -> "💀 Defeated. The dungeon claims another soul.";
            case FLED    -> "💨 Escaped from battle.";
        };
        emit(new CombatEvent.Builder(CombatEvent.EventType.COMBAT_END).message(msg).build());
        // Kirim result ke semua listener yang mau tahu
        combatResultListeners.forEach(l -> l.accept(result));
    }

    private final java.util.List<Consumer<CombatResult>> combatResultListeners
        = new java.util.ArrayList<>(); // max 2: DungeonManager + CombatView

    /** Subscribe untuk menerima CombatResult saat battle selesai */
    public void addResultListener(Consumer<CombatResult> listener) {
        // Max 2 listener (DungeonManager logic + CombatView UI)
        // Reset jika sudah penuh untuk mencegah accumulate
        if (combatResultListeners.size() >= 2) combatResultListeners.clear();
        combatResultListeners.add(listener);
    }

    public void clearResultListeners() {
        combatResultListeners.clear();
    }

    // ════════════════════════════════════════════════════════
    // GETTERS (untuk UI query)
    // ════════════════════════════════════════════════════════

    public boolean  isCombatActive()      { return combatActive; }
    public boolean  isWaitingForPlayer()  { return waitingForPlayer; }
    public int      getTotalTurns()       { return totalTurns; }
    public Player   getPlayer()           { return player; }
    public List<Mercenary> getActiveMercs() { return Collections.unmodifiableList(activeMercs); }
    public List<Enemy>     getEnemies()     { return Collections.unmodifiableList(enemies); }
    public List<Entity>    getAllAllies()    { return Collections.unmodifiableList(allAllies); }
    public List<Entity>    getAllEnemies()   { return Collections.unmodifiableList(allEnemies); }
    public TurnQueue       getTurnQueue()   { return turnQueue; }

    /** Entity yang sekarang gilirannya — untuk highlight di UI */
    public Entity getCurrentActor() {
        return turnQueue.getCurrentTurnEntity();
    }

    /** Semua enemy yang masih hidup — untuk UI targeting */
    public List<Entity> getLivingEnemies() {
        return allEnemies.stream().filter(Entity::isAlive).toList();
    }

    /** Semua ally yang masih hidup — untuk UI targeting skill heal */
    public List<Entity> getLivingAllies() {
        return allAllies.stream().filter(Entity::isAlive).toList();
    }
}
