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
        String filename = skillId.toLowerCase().replace(" ", "_") + ".png";
        return load("icons/skill/" + filename);
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
        return switch (name.toLowerCase().trim()) {
            case "leak pengembara"    -> "leakpengembara";
            case "naga basuki"        -> "nagabasuki";
            case "genderuwo mekanik"  -> "genderuwomekanik";
            case "raksasa kala"       -> "raksasakala";
            case "kuntilanak abadi"   -> "kuntilanakabadi";
            case "tuyul pencuri"      -> "tuyulpencuri";
            case "wewe gombel"        -> "wewegombel";
            case "pocong prajurit"    -> "pocongprajurit";
            case "banaspati mekanik"  -> "banaspatimekanik";
            case "celeng buto"        -> "celengbuto";
            case "ular nagabanda"     -> "nagabanda";
            case "demit pabrik"       -> "demitpabrik";
            case "siluman harimau"    -> "silumanharimau";
            case "jenglot purba"      -> "jenglotpurba";
            case "buto ijo"           -> "butoijo";
            case "gajah mungkur"      -> "gajahmungkur";
            case "manananggal"        -> "manananggal";
            case "leyak penyihir"     -> "leyakpenyihir";
            case "rangkiang raksasa"  -> "rangkiangraksasa";
            case "naga cyber"         -> "nagacyber";
            case "tank rx-9", "tank-rx9", "tank rx9" -> "rx9";
            default -> name.toLowerCase().replaceAll("[^a-z0-9]", "");
        };
    }

    public static String guildmateSpriteKey(String name) {
        if (name == null) return "gatotkaca";
        return switch (name.toLowerCase().trim()) {
            case "gatot kaca" -> "gatotkaca";
            case "nyai roro"  -> "nyairoro";
            case "ki ageng"   -> "kiageng";
            case "dewi sri"   -> "dewisri";
            case "srikandi"   -> "srikandi";
            case "rangga"     -> "rangga";
            case "bima"       -> "bima";
            default -> name.toLowerCase().replaceAll("[^a-z]", "");
        };
    }

    public static String guildmatePortraitKey(String name) {
        if (name == null) return "gatotkaca";
        return switch (name.toLowerCase().trim()) {
            case "gatot kaca" -> "gatotkaca";
            case "nyai roro"  -> "nyairoro";
            case "ki ageng"   -> "kiageng";
            case "dewi sri"   -> "dewisri";
            default -> name.toLowerCase().replaceAll("[^a-z]", "");
        };
    }
}
