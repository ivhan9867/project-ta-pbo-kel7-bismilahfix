package arclightcity.combat;

import arclightcity.entity.enemy.Enemy;
import arclightcity.entity.player.Player;

import java.util.List;

/**
 * Hasil akhir sebuah combat.
 * Diserahkan ke DungeonManager setelah combat selesai.
 */
public class CombatResult {

    public enum Outcome { VICTORY, DEFEAT, FLED }

    private final Outcome       outcome;
    private final int           turnsElapsed;
    private final double        totalExpGained;
    private final long          totalGoldGained;
    private int                 levelsGained = 0; // set oleh CombatManager setelah gainExp()
    private final List<String>  lootItemIds;       // item yang di-drop enemy
    private final List<String>  combatLog;         // ringkasan event combat
    private final List<Enemy>   defeatedEnemies;

    // ── Constructor ─────────────────────────────────────────

    public CombatResult(Outcome outcome, int turnsElapsed,
                        double totalExpGained, long totalGoldGained,
                        List<String> lootItemIds, List<String> combatLog,
                        List<Enemy> defeatedEnemies) {
        this.outcome         = outcome;
        this.turnsElapsed    = turnsElapsed;
        this.totalExpGained  = totalExpGained;
        this.totalGoldGained = totalGoldGained;
        this.lootItemIds     = lootItemIds;
        this.combatLog       = combatLog;
        this.defeatedEnemies = defeatedEnemies;
    }

    // ── Factory ──────────────────────────────────────────────

    public static CombatResult victory(int turns, double exp, long gold,
                                       List<String> loot, List<String> log,
                                       List<Enemy> defeated) {
        return new CombatResult(Outcome.VICTORY, turns, exp, gold, loot, log, defeated);
    }

    public static CombatResult defeat(int turns, List<String> log) {
        return new CombatResult(Outcome.DEFEAT, turns, 0, 0,
                List.of(), log, List.of());
    }

    public static CombatResult fled(int turns, List<String> log) {
        return new CombatResult(Outcome.FLED, turns, 0, 0,
                List.of(), log, List.of());
    }

    // ── Getters ─────────────────────────────────────────────

    public Outcome       getOutcome()          { return outcome; }
    public int           getTurnsElapsed()     { return turnsElapsed; }
    public double        getTotalExpGained()   { return totalExpGained; }
    public long          getTotalGoldGained()  { return totalGoldGained; }
    public int           getLevelsGained()     { return levelsGained; }
    public void          setLevelsGained(int n){ this.levelsGained = n; }
    public List<String>  getLootItemIds()      { return lootItemIds; }
    public List<String>  getCombatLog()        { return combatLog; }
    public List<Enemy>   getDefeatedEnemies()  { return defeatedEnemies; }

    public boolean isVictory() { return outcome == Outcome.VICTORY; }
    public boolean isDefeat()  { return outcome == Outcome.DEFEAT; }
    public boolean isFled()    { return outcome == Outcome.FLED; }

    @Override
    public String toString() {
        return String.format("CombatResult[%s | %d turns | EXP:%.0f | Gold:%d | Loot:%d items]",
                outcome, turnsElapsed, totalExpGained, totalGoldGained, lootItemIds.size());
    }
}
