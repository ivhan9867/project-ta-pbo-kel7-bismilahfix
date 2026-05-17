package arclightcity.ui.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.HashMap;
import java.util.Map;

/**
 * AssetManager — load dan cache semua gambar game.
 * Semua path relatif ke /assets/ di resources.
 */
public class AssetManager {

    private static final Map<String, Image> cache = new HashMap<>();
    private static final String BASE = "/assets/";

    // ── Artifact icons ────────────────────────────────────────
    public static Image artifactIcon(arclightcity.item.ArtifactType type) {
        if (type == null) return null;
        String key = "icons/artifact/icon_artifact_" + type.name().toLowerCase() + ".png";
        return load(key);
    }

    // ── Backgrounds ──────────────────────────────────────────
    public static Image bgMainMenu()     { return load("backgrounds/main_menu.png"); }
    public static Image bgHub()          { return load("backgrounds/hub.png"); }
    public static Image bgCitySenjata()  { return load("backgrounds/city_senjata.png"); }
    public static Image bgCityJamu()     { return load("backgrounds/city_jamu.png"); }
    public static Image bgCityBengkel()  { return load("backgrounds/city_bengkel.png"); }
    public static Image bgCityPenadah()  { return load("backgrounds/city_penadah.png"); }
    public static Image bgBossRoom()     { return load("backgrounds/boss_room.png"); }

    public static Image bgDungeon(int floor) {
        int theme = floor <= 9 ? 1 : floor <= 19 ? 2 : floor <= 29 ? 3 : floor <= 39 ? 4 : 5;
        return load("backgrounds/dungeon_" + theme + ".png");
    }

    // ── Portraits Asuna ───────────────────────────────────────
    public static Image portraitAsuna()      { return load("portraits/asuna/asuna_normal.png"); }
    public static Image portraitAsunaAngry() { return load("portraits/asuna/asuna_angry.png"); }
    public static Image portraitAsunaFull()  { return load("portraits/asuna/asuna_fullbody.png"); }
    public static Image portraitAsunaLore(int n) { return load(String.format("portraits/asuna/asuna_lore%d.png", n)); }

    // ── Portraits Boss ────────────────────────────────────────
    public static Image portraitBoss(int floor, boolean angry) {
        String base = switch (floor / 10) {
            case 1  -> "batara_kala";
            case 2  -> "nyi_roro";
            case 3  -> "rangda";
            case 4  -> "garuda";
            case 5  -> "semar";
            default -> "theresa";
        };
        return load("portraits/boss/" + base + (angry ? "_angry" : "_normal") + ".png");
    }
    public static Image portraitTheresa()      { return load("portraits/boss/theresa_normal.png"); }
    public static Image portraitTheresaAngry() { return load("portraits/boss/theresa_angry.png"); }

    // ── Portraits Guildmate ───────────────────────────────────
    public static Image portraitGuildmate(String name, boolean alt) {
        String key = guildmatePortraitKey(name);
        return load("portraits/guildmate/" + key + (alt ? "_alt" : "_normal") + ".png");
    }

    // ── Sprites Asuna ─────────────────────────────────────────
    public static Image spriteAsuna(String pose) {
        return load("sprites/asuna/" + pose + ".png");
    }

    // ── Sprites Enemy ─────────────────────────────────────────
    public static Image spriteEnemy(String enemyName, String pose) {
        String key = enemySpriteKey(enemyName);
        return load("sprites/enemy/" + key + "_" + pose + ".png");
    }

    // ── Sprites Guildmate ─────────────────────────────────────
    public static Image spriteGuildmate(String name, String pose) {
        String key = guildmateSpriteKey(name);
        return load("sprites/guildmate/" + key + "_" + pose + ".png");
    }

    // ── Sprites Boss ─────────────────────────────────────────
    public static Image spriteBoss(String key, String pose) {
        return load("sprites/boss/" + key + "_" + pose + ".png");
    }

    /** Sprite boss berdasarkan floor dungeon */
    public static Image spriteBossByFloor(int floor, String pose) {
        String key = switch (floor) {
            case 10 -> "batarakala";
            case 20 -> "nyirorokidul";
            case 30 -> "rangdaagung";
            case 40 -> "garudamahaguru";
            case 50 -> "semarpamungkas";
            default -> "theresa";
        };
        return load("sprites/boss/" + key + "_" + pose + ".png");
    }

    // ── Skill Icons ───────────────────────────────────────────
    public static Image iconSkill(String skillId) {
        if (skillId == null) return null;
        String filename = switch (skillId.toUpperCase()) {
            case "POWER_STRIKE","PUKULAN_HARIMAU"    -> "pukulan_harimau";
            case "EXECUTE","TEBASAN_PAMUNGKAS"       -> "tebasan_pamungkas";
            case "PHANTOM_SHOT","PANAH_BAYANGAN"     -> "panah_bayangan";
            case "SHADOW_STEP","LANGKAH_GAIB"        -> "langkah_gaib";
            case "DEEP_HACK","BIDANG_BAYANGAN"       -> "bidang_bayangan";
            case "NULL_FIELD","PROTOKOL_NOL"         -> "protokol_nol";
            case "SOVEREIGN_STRIKE","TEBASAN_AGUNG"  -> "tebasan_agung";
            case "NULL_PROTOCOL"                     -> "protokol_nol";
            case "DATA_FRAGMENTATION","PECAH_JIWA"   -> "pecah_jiwa";
            case "ENERGY_DRAIN","SERAP_TENAGA"       -> "serap_tenaga";
            case "IRON_SHIELD","TAMENG_BAJA"         -> "tameng_baja";
            case "SEISMIC_SLAM","GEMPA_BUMI"         -> "gempa_bumi";
            case "SACRED_SEAL","RAJAH_PELINDUNG"     -> "rajah_pelindung";
            default -> skillId.toLowerCase().replace(" ", "_");
        };
        return load("icons/skill/" + filename + ".png");
    }

    // ── UI ────────────────────────────────────────────────────
    public static Image logo() { return load("ui/logo.png"); }

    // ── Lore ─────────────────────────────────────────────────
    public static Image loreImage(int index) {
        return load(String.format("lore/lore_%02d.png", index));
    }

    // ── ImageView helpers ────────────────────────────────────
    public static ImageView makeIV(Image img, double w, double h) {
        if (img == null) return new ImageView();
        ImageView iv = new ImageView(img);
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    public static ImageView makeIVFill(Image img, double w, double h) {
        if (img == null) return new ImageView();
        ImageView iv = new ImageView(img);
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(false);
        iv.setSmooth(true);
        return iv;
    }

    // ── Internal loader ───────────────────────────────────────
    private static Image load(String path) {
        return cache.computeIfAbsent(path, p -> {
            try {
                var stream = AssetManager.class.getResourceAsStream(BASE + p);
                if (stream == null) {
                    System.err.println("[AssetManager] Missing: " + BASE + p);
                    return null;
                }
                return new Image(stream);
            } catch (Exception e) {
                System.err.println("[AssetManager] Error: " + p + " — " + e.getMessage());
                return null;
            }
        });
    }

    // ── Key maps ─────────────────────────────────────────────
    public static String enemySpriteKey(String name) {
        if (name == null) return "leakpengembara";
        String key = switch (name.toLowerCase().trim()) {
            // Pemetaan utama
            case "leak pengembara"    -> "leakpengembara";
            case "naga basuki"        -> "nagabasuki";
            case "genderuwo mekanik"  -> "genderuwomekanik";
            case "raksasa kala"       -> "raksasakala";
            case "kuntilanak abadi"   -> "kuntilanakabadi";
            case "tuyul pencuri"      -> "tuyulpencuri";
            case "wewe gombel"        -> "wewegombel";
            case "pocong prajurit","pocong listrik","pocong" -> "pocongprajurit";
            case "banaspati mekanik","banaspati" -> "banaspatimekanik";
            case "celeng buto","celeng" -> "celengbuto";
            case "ular nagabanda","ular naga","nagabanda" -> "nagabanda";
            case "demit pabrik","demit" -> "demitpabrik";
            case "siluman harimau","siluman" -> "silumanharimau";
            case "jenglot purba","jenglot" -> "jenglotpurba";
            case "buto ijo","buto" -> "butoijo";
            case "gajah mungkur","gajah" -> "gajahmungkur";
            case "manananggal" -> "manananggal";
            case "leyak penyihir","leyak" -> "leyakpenyihir";
            case "rangkiang raksasa","rangkiang"        -> "rangkiangraksasa";
            // ── Sprite belum ada — fallback ke musuh yang mirip ──────────────
            case "rangda merah","rangdamerah"            -> "leyakpenyihir";
            case "barong rusak","barongrusak"            -> "banaspatimekanik";
            case "babi ngepet","babingepet"              -> "celengbuto";
            case "neon serpent","neonserpent"            -> "nagabanda";
            case "glitch drone","glitchdrone"            -> "demitpabrik";
            case "garuda korup","garudakorup"            -> "leakpengembara";
            case "detya wesi","detyawesi"                -> "raksasakala";
            case "naga cyber","naga" -> "nagacyber";
            case "tank rx-9","tank-rx9","tank rx9","tank" -> "rx9";
            // Fallback: strip spasi dan karakter non-alpha
            default -> name.toLowerCase().replaceAll("[^a-z0-9]", "");
        };
        // Verifikasi sprite ada, jika tidak pakai fallback leakpengembara
        var test = load("sprites/enemy/" + key + "_idle.png");
        if (test == null) {
            System.err.println("[AssetManager] No sprite for '" + name + "' (key=" + key + "), using fallback");
            return "leakpengembara";
        }
        return key;
    }

    public static String guildmateSpriteKey(String name) {
        if (name == null) return "gatotkaca";
        return switch (name.toLowerCase().trim()) {
            case "gatot kaca","tank-rx9","tank rx9" -> "gatotkaca";
            case "nyai roro","sera mend"             -> "nyairoro";
            case "ki ageng","echo null"              -> "kiageng";
            case "dewi sri","lyra bloom"             -> "dewisri";
            case "srikandi","kira voss"              -> "srikandi";
            case "rangga","vector"                   -> "rangga";
            case "bima","magnus forge"               -> "bima";
            default -> name.toLowerCase().replaceAll("[^a-z]", "");
        };
    }

    public static String guildmatePortraitKey(String name) {
        if (name == null) return "gatotkaca";
        return switch (name.toLowerCase().trim()) {
            case "gatot kaca","tank-rx9","tank rx9" -> "gatotkaca";
            case "nyai roro","sera mend"             -> "nyairoro";
            case "ki ageng","echo null"              -> "kiageng";
            case "dewi sri","lyra bloom"             -> "dewisri";
            case "srikandi","kira voss"              -> "srikandi";
            case "rangga","vector"                   -> "rangga";
            case "bima","magnus forge"               -> "bima";
            default -> "gatotkaca"; // fallback ke gatotkaca, bukan nama acak
        };
    }
}
