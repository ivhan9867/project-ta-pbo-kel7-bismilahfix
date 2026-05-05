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
            // Standard
            case STREET_THUG    -> new StreetThug();
            case NEON_SERPENT   -> new NeonSerpent();
            case GLITCH_DRONE   -> new GlitchDrone();
            case TUYUL_PENCURI  -> new TuyulPencuri();
            case WEWE_GOMBEL    -> new WeweGombel();
            case POCONG_LISTRIK -> new PocongListrik();
            case BANASPATI      -> new Banaspati();
            case BABI_NGEPET    -> new BabiNgepet();
            // Elite
            case IRON_CLAD      -> new IronClad();
            case VOID_SPECTER   -> new VoidSpecter();
            case RANGDA_MERAH   -> new RangdaMerah();
            case BARONG_RUSAK   -> new BarongRusak();
            case LEYAK_API      -> new LeyakApi();
            case GARUDA_KORUP   -> new GarudaKorup();
            case DETYA_WESI     -> new DetyaWesi();
            // Boss
            case NULL_KING          -> new NullKing();
            case NYI_RORO_KIDUL     -> new NyiRoroKidul();
            case RANGDA_AGUNG       -> new RangdaAgung();
            case GARUDA_MAHAGURU    -> new GarudaMahaguru();
            case SEMAR_PAMUNGKAS    -> new SemarPamungkas();
            case THERESA            -> new Theresa();
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
        if (floor <= 3)  return List.of(spawn(EnemySpawnType.STREET_THUG, floor));
        if (floor <= 6)  return List.of(spawn(EnemySpawnType.TUYUL_PENCURI, floor));
        if (floor <= 9)  return List.of(spawn(EnemySpawnType.WEWE_GOMBEL, floor));
        if (floor <= 13) return List.of(spawn(EnemySpawnType.POCONG_LISTRIK, floor));
        if (floor <= 17) return List.of(spawn(EnemySpawnType.BANASPATI, floor));
        return List.of(spawn(EnemySpawnType.BABI_NGEPET, floor));
    }

    private static List<Enemy> generateNormalEncounter(int floor) {
        int roll = RNG.nextInt(3);
        if (floor <= 3) return switch(roll) {
            case 0 -> List.of(spawn(EnemySpawnType.STREET_THUG,floor), spawn(EnemySpawnType.GLITCH_DRONE,floor));
            case 1 -> List.of(spawn(EnemySpawnType.TUYUL_PENCURI,floor), spawn(EnemySpawnType.STREET_THUG,floor));
            default-> List.of(spawn(EnemySpawnType.NEON_SERPENT,floor));
        };
        if (floor <= 6) return switch(roll) {
            case 0 -> List.of(spawn(EnemySpawnType.WEWE_GOMBEL,floor), spawn(EnemySpawnType.TUYUL_PENCURI,floor));
            case 1 -> List.of(spawn(EnemySpawnType.BANASPATI,floor), spawn(EnemySpawnType.GLITCH_DRONE,floor));
            default-> List.of(spawn(EnemySpawnType.POCONG_LISTRIK,floor), spawn(EnemySpawnType.NEON_SERPENT,floor));
        };
        if (floor <= 10) return switch(roll) {
            case 0 -> List.of(spawn(EnemySpawnType.BABI_NGEPET,floor), spawn(EnemySpawnType.BANASPATI,floor));
            case 1 -> List.of(spawn(EnemySpawnType.WEWE_GOMBEL,floor), spawn(EnemySpawnType.POCONG_LISTRIK,floor));
            default-> List.of(spawn(EnemySpawnType.BANASPATI,floor), spawn(EnemySpawnType.BANASPATI,floor));
        };
        if (floor <= 15) return switch(roll) {
            case 0 -> List.of(spawn(EnemySpawnType.BABI_NGEPET,floor), spawn(EnemySpawnType.WEWE_GOMBEL,floor));
            case 1 -> List.of(spawn(EnemySpawnType.POCONG_LISTRIK,floor), spawn(EnemySpawnType.TUYUL_PENCURI,floor), spawn(EnemySpawnType.BANASPATI,floor));
            default-> List.of(spawn(EnemySpawnType.NEON_SERPENT,floor), spawn(EnemySpawnType.GARUDA_KORUP,floor));
        };
        return List.of(spawn(EnemySpawnType.GARUDA_KORUP,floor), spawn(EnemySpawnType.BANASPATI,floor));
    }

    private static List<Enemy> generateHardEncounter(int floor) {
        int roll = RNG.nextInt(3);
        if (floor <= 5) return List.of(
            spawn(EnemySpawnType.STREET_THUG,floor), spawn(EnemySpawnType.NEON_SERPENT,floor), spawn(EnemySpawnType.TUYUL_PENCURI,floor));
        if (floor <= 8) return switch(roll) {
            case 0 -> List.of(spawn(EnemySpawnType.WEWE_GOMBEL,floor), spawn(EnemySpawnType.POCONG_LISTRIK,floor), spawn(EnemySpawnType.BANASPATI,floor));
            case 1 -> List.of(spawn(EnemySpawnType.BABI_NGEPET,floor), spawn(EnemySpawnType.TUYUL_PENCURI,floor));
            default-> List.of(spawn(EnemySpawnType.IRON_CLAD,floor), spawn(EnemySpawnType.GLITCH_DRONE,floor));
        };
        if (floor <= 12) return switch(roll) {
            case 0 -> List.of(spawn(EnemySpawnType.RANGDA_MERAH,floor));
            case 1 -> List.of(spawn(EnemySpawnType.BARONG_RUSAK,floor));
            default-> List.of(spawn(EnemySpawnType.LEYAK_API,floor), spawn(EnemySpawnType.BANASPATI,floor));
        };
        if (floor <= 17) return switch(roll) {
            case 0 -> List.of(spawn(EnemySpawnType.GARUDA_KORUP,floor));
            case 1 -> List.of(spawn(EnemySpawnType.DETYA_WESI,floor));
            default-> List.of(spawn(EnemySpawnType.LEYAK_API,floor), spawn(EnemySpawnType.GARUDA_KORUP,floor));
        };
        return List.of(spawn(EnemySpawnType.DETYA_WESI,floor), spawn(EnemySpawnType.GARUDA_KORUP,floor));
    }

    private static List<Enemy> generateEliteEncounter(int floor) {
        if (floor <= 5)  return List.of(spawn(EnemySpawnType.IRON_CLAD, floor));
        if (floor <= 8)  return List.of(spawn(EnemySpawnType.RANGDA_MERAH, floor));
        if (floor <= 12) return List.of(spawn(EnemySpawnType.BARONG_RUSAK, floor), spawn(EnemySpawnType.BANASPATI, floor));
        if (floor <= 16) return List.of(spawn(EnemySpawnType.GARUDA_KORUP, floor), spawn(EnemySpawnType.LEYAK_API, floor));
        return List.of(spawn(EnemySpawnType.DETYA_WESI, floor));
    }

    /**
     * Generate Boss untuk floor milestone.
     */
    /**
     * Generate boss sesuai floor.
     * Setiap 10 floor ada main boss yang drop Red Essence Shard.
     * Floor 51: Theresa — Final Boss, butuh Red Blossom Katana.
     */
    public static Enemy generateBoss(int floor) {
        // Boss utama per 10 floor — setiap satu drop Red Essence Shard
        if (floor == 10) return spawn(EnemySpawnType.NULL_KING,         floor); // Batara Kala
        if (floor == 20) return spawn(EnemySpawnType.NYI_RORO_KIDUL,    floor); // Nyi Roro Kidul
        if (floor == 30) return spawn(EnemySpawnType.RANGDA_AGUNG,       floor); // Rangda Agung
        if (floor == 40) return spawn(EnemySpawnType.GARUDA_MAHAGURU,    floor); // Garuda Mahaguru
        if (floor == 50) return spawn(EnemySpawnType.SEMAR_PAMUNGKAS,    floor); // Semar Pamungkas
        if (floor >= 51) return spawn(EnemySpawnType.THERESA,            floor); // Final Boss

        // Mini-boss antara milestone (floor bukan kelipatan 10)
        if (floor < 10)  return spawn(EnemySpawnType.IRON_CLAD,          floor);
        if (floor < 20)  return spawn(EnemySpawnType.VOID_SPECTER,        floor);
        if (floor < 30)  return spawn(EnemySpawnType.RANGDA_MERAH,        floor);
        if (floor < 40)  return spawn(EnemySpawnType.GARUDA_KORUP,        floor);
        if (floor < 50)  return spawn(EnemySpawnType.DETYA_WESI,          floor);
        return spawn(EnemySpawnType.SEMAR_PAMUNGKAS, floor);
    }

    // ── Helper ───────────────────────────────────────────────

    private static Enemy spawn(EnemySpawnType type, int floor) {
        return createEnemy(type, floor);
    }

    // ── Enum Spawn Types ─────────────────────────────────────

    public enum EnemySpawnType {
        // Standard (Floor 1-6)
        STREET_THUG, NEON_SERPENT, GLITCH_DRONE,
        TUYUL_PENCURI, WEWE_GOMBEL, POCONG_LISTRIK, BANASPATI, BABI_NGEPET,
        // Elite (Floor 4-17)
        IRON_CLAD, VOID_SPECTER,
        RANGDA_MERAH, BARONG_RUSAK, LEYAK_API, GARUDA_KORUP, DETYA_WESI,
        // Boss per 10 floor (memberikan Red Essence Shard)
        NULL_KING,       // Floor 10 — Boss 1
        NYI_RORO_KIDUL,  // Floor 20 — Boss 2
        RANGDA_AGUNG,    // Floor 30 — Boss 3
        GARUDA_MAHAGURU, // Floor 40 — Boss 4
        SEMAR_PAMUNGKAS, // Floor 50 — Boss 5
        THERESA          // Floor 51 — Final Boss (butuh Red Blossom Katana)
    }
}
