package arclightcity.entity;
import arclightcity.entity.mercenary.MercenaryType;

import arclightcity.entity.enemy.*;
import arclightcity.entity.mercenary.*;
import arclightcity.entity.player.Player;
import arclightcity.entity.player.PlayerBackground;

import java.util.List;
import java.util.Random;

/**
 * EntityFactory — satu titik untuk membuat semua entity di game.
 *
 * Kegunaan:
 *  - Spawn enemy berdasarkan floor dan kesulitan
 *  - Buat mercenary berdasarkan type
 *  - Buat player baru
 *  - Generate kelompok enemy (encounter) untuk sebuah room
 */
public class EntityFactory {

    private static final Random RNG = new Random();

    // ── Player ───────────────────────────────────────────────

    public static Player createPlayer(String name, PlayerBackground background) {
        return new Player(name, background);
    }

    // ── Mercenary ────────────────────────────────────────────

    public static Mercenary createMercenary(MercenaryType type) {
        return switch (type) {
            case KIRA_VOSS    -> new KiraVoss();
            case TANK_RX9     -> new TankRX9();
            case SERA_MEND    -> new SeraMend();
            case VECTOR       -> new Vector();
            case MAGNUS_FORGE -> new MagnusForge();
            case ECHO_NULL    -> new EchoNull();
            case LYRA_BLOOM   -> new LyraBloom();
        };
    }

    public static List<Mercenary> createAllMercenaries() {
        return List.of(
                new KiraVoss(),
                new TankRX9(),
                new SeraMend(),
                new Vector(),
                new MagnusForge(),
                new EchoNull(),
                new LyraBloom()
        );
    }

    // ── Enemy (by type) ──────────────────────────────────────

    public static Enemy createEnemy(EnemySpawnType type, int floor) {
        Enemy enemy = switch (type) {
            case STREET_THUG  -> new StreetThug();
            case NEON_SERPENT -> new NeonSerpent();
            case GLITCH_DRONE -> new GlitchDrone();
            case IRON_CLAD    -> new IronClad();
            case VOID_SPECTER -> new VoidSpecter();
            case NULL_KING    -> new NullKing();
        };
        enemy.scaleToFloor(floor);
        return enemy;
    }

    // ── Enemy Encounter Generator ────────────────────────────

    /**
     * Generate kelompok enemy yang sesuai untuk room di floor tertentu.
     * Mempertimbangkan dungeon difficulty dan variasi encounter.
     *
     * @param floor        nomor floor dungeon
     * @param roomDifficulty  1=easy, 2=normal, 3=hard, 4=elite
     * @return list enemy untuk encounter ini
     */
    public static List<Enemy> generateEncounter(int floor, int roomDifficulty) {
        return switch (roomDifficulty) {
            case 1 -> generateEasyEncounter(floor);
            case 2 -> generateNormalEncounter(floor);
            case 3 -> generateHardEncounter(floor);
            case 4 -> generateEliteEncounter(floor);
            default -> generateNormalEncounter(floor);
        };
    }

    private static List<Enemy> generateEasyEncounter(int floor) {
        // 1-2 musuh lemah
        if (floor <= 5) {
            return RNG.nextBoolean()
                    ? List.of(spawn(EnemySpawnType.STREET_THUG, floor))
                    : List.of(spawn(EnemySpawnType.STREET_THUG, floor),
                              spawn(EnemySpawnType.GLITCH_DRONE, floor));
        }
        return List.of(spawn(EnemySpawnType.NEON_SERPENT, floor));
    }

    private static List<Enemy> generateNormalEncounter(int floor) {
        // 2-3 musuh standar
        if (floor <= 5) {
            return List.of(
                    spawn(EnemySpawnType.STREET_THUG, floor),
                    spawn(EnemySpawnType.GLITCH_DRONE, floor)
            );
        }
        if (floor <= 10) {
            int roll = RNG.nextInt(3);
            return switch (roll) {
                case 0 -> List.of(
                        spawn(EnemySpawnType.NEON_SERPENT, floor),
                        spawn(EnemySpawnType.GLITCH_DRONE, floor));
                case 1 -> List.of(
                        spawn(EnemySpawnType.STREET_THUG, floor),
                        spawn(EnemySpawnType.STREET_THUG, floor),
                        spawn(EnemySpawnType.NEON_SERPENT, floor));
                default -> List.of(
                        spawn(EnemySpawnType.GLITCH_DRONE, floor),
                        spawn(EnemySpawnType.GLITCH_DRONE, floor));
            };
        }
        return List.of(
                spawn(EnemySpawnType.NEON_SERPENT, floor),
                spawn(EnemySpawnType.VOID_SPECTER, floor)
        );
    }

    private static List<Enemy> generateHardEncounter(int floor) {
        // 3 musuh atau 1 elite
        if (floor <= 8) {
            return List.of(
                    spawn(EnemySpawnType.STREET_THUG, floor),
                    spawn(EnemySpawnType.NEON_SERPENT, floor),
                    spawn(EnemySpawnType.GLITCH_DRONE, floor)
            );
        }
        return List.of(
                spawn(EnemySpawnType.IRON_CLAD, floor),
                spawn(EnemySpawnType.GLITCH_DRONE, floor)
        );
    }

    private static List<Enemy> generateEliteEncounter(int floor) {
        // 1 elite atau 1 elite + support
        if (floor <= 8) {
            return List.of(spawn(EnemySpawnType.IRON_CLAD, floor));
        }
        return List.of(
                spawn(EnemySpawnType.VOID_SPECTER, floor),
                spawn(EnemySpawnType.GLITCH_DRONE, floor)
        );
    }

    /**
     * Generate Boss untuk floor milestone.
     */
    public static Enemy generateBoss(int floor) {
        // Floor 10: Null King
        if (floor == 10) return spawn(EnemySpawnType.NULL_KING, floor);
        // Fallback: scaled Iron Clad sebagai mini-boss
        return spawn(EnemySpawnType.IRON_CLAD, floor);
    }

    // ── Helper ───────────────────────────────────────────────

    private static Enemy spawn(EnemySpawnType type, int floor) {
        return createEnemy(type, floor);
    }

    // ── Enum Spawn Types ─────────────────────────────────────

    public enum EnemySpawnType {
        STREET_THUG,
        NEON_SERPENT,
        GLITCH_DRONE,
        IRON_CLAD,
        VOID_SPECTER,
        NULL_KING
    }
}
