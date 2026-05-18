package arclightcity.ui.util;

import javafx.scene.media.*;
import javafx.application.Platform;
import java.net.URL;

/**
 * AudioManager — singleton untuk BGM dan SFX.
 *
 * Volume: BGM 55%, SFX 60%.
 * BGM: seamless loop, auto-crossfade saat ganti.
 * SFX: fire-and-forget AudioClip.
 *
 * Mapping BGM per layar:
 *   main_menu → bgm_main_menu
 *   hub       → bgm_hub
 *   dungeon per tema → bgm_dungeon_{pasar/candi/hutan/goa/kahyangan}
 *   combat normal  → bgm_combat_normal
 *   combat elite   → bgm_combat_elite
 *   boss regular   → bgm_boss_regular
 *   boss semar     → bgm_boss_semar
 *   boss theresa   → bgm_theresa
 *   shop/city      → bgm_shop
 *   cutscene open  → bgm_cutscene_opening
 *   cutscene end   → bgm_cutscene_ending
 *   victory        → (sfx) bgm_victory
 *   gameover       → (sfx) bgm_gameover
 */
public class AudioManager {

    private static AudioManager INSTANCE;

    private static final double BGM_VOLUME  = 0.55;
    private static final double SFX_VOLUME  = 0.60;
    private static final double FADE_STEP   = 0.05;
    private static final long   FADE_DELAY  = 40;  // ms per step

    private MediaPlayer currentBgm  = null;
    private String      currentKey  = "";
    private boolean     muted       = false;

    private AudioManager() {}

    public static AudioManager get() {
        if (INSTANCE == null) INSTANCE = new AudioManager();
        return INSTANCE;
    }

    // ── BGM ───────────────────────────────────────────────────
    public void playBgm(String filename) {
        if (muted) return;
        if (filename != null && filename.equals(currentKey)) return; // sudah diputar

        MediaPlayer next = loadBgm(filename);
        if (next == null) return;

        if (currentBgm != null) {
            fadeOutAndStop(currentBgm);
        }
        currentBgm = next;
        currentKey  = filename;
        currentBgm.setVolume(0);
        currentBgm.setCycleCount(MediaPlayer.INDEFINITE);
        currentBgm.play();
        fadeIn(currentBgm);
    }

    public void stopBgm() {
        if (currentBgm != null) {
            fadeOutAndStop(currentBgm);
            currentBgm = null;
            currentKey  = "";
        }
    }

    public void setMuted(boolean m) {
        muted = m;
        if (m && currentBgm != null) currentBgm.setMute(true);
        else if (!m && currentBgm != null) currentBgm.setMute(false);
    }

    // ── SFX ───────────────────────────────────────────────────
    public void playSfx(String filename) {
        if (muted) return;
        URL url = getClass().getResource("/assets/audio/sfx/" + filename);
        if (url == null) return;
        try {
            AudioClip clip = new AudioClip(url.toString());
            clip.setVolume(SFX_VOLUME);
            clip.play();
        } catch (Exception ignored) {}
    }

    // Shortcut SFX methods
    public void sfxHitPhysical() { playSfx("sfx_hit_physical.wav"); }
    public void sfxHitCyber()    { playSfx("sfx_hit_cyber.wav");    }
    public void sfxHitEnergy()   { playSfx("sfx_hit_energy.wav");   }
    public void sfxCritical()    { playSfx("sfx_critical.wav");     }
    public void sfxHeal()        { playSfx("sfx_heal.wav");         }
    public void sfxMiss()        { playSfx("sfx_miss.wav");         }
    public void sfxVictory()     { playSfx("bgm_victory.wav");      }
    public void sfxGameOver()    { playSfx("bgm_gameover.wav");     }

    // ── BGM key helpers ───────────────────────────────────────
    public static String BGM_MAIN_MENU    = "bgm_main_menu.ogg";
    public static String BGM_HUB          = "bgm_hub.ogg";
    public static String BGM_SHOP         = "bgm_shop.ogg";
    public static String BGM_COMBAT       = "bgm_combat_normal.ogg";
    public static String BGM_COMBAT_ELITE = "bgm_combat_elite.ogg";
    public static String BGM_BOSS         = "bgm_boss_regular.ogg";
    public static String BGM_BOSS_SEMAR   = "bgm_boss_semar.ogg";
    public static String BGM_THERESA      = "bgm_theresa.ogg";
    public static String BGM_CUTSCENE_OP  = "bgm_cutscene_opening.ogg";
    public static String BGM_CUTSCENE_END = "bgm_cutscene_ending.ogg";

    /** Pilih BGM dungeon berdasarkan floor number */
    public static String bgmForFloor(int floor) {
        return switch ((floor - 1) / 10) {
            case 0  -> "bgm_dungeon_pasar.ogg";
            case 1  -> "bgm_dungeon_candi.ogg";
            case 2  -> "bgm_dungeon_hutan.ogg";
            case 3  -> "bgm_dungeon_goa.ogg";
            default -> "bgm_dungeon_kahyangan.ogg";
        };
    }

    // ── Private helpers ───────────────────────────────────────
    private MediaPlayer loadBgm(String filename) {
        if (filename == null) return null;
        URL url = getClass().getResource("/assets/audio/bgm/" + filename);
        if (url == null) { System.err.println("[Audio] Missing: " + filename); return null; }
        try {
            return new MediaPlayer(new Media(url.toString()));
        } catch (Exception e) {
            System.err.println("[Audio] Error loading: " + filename);
            return null;
        }
    }

    private void fadeIn(MediaPlayer mp) {
        new Thread(() -> {
            double vol = 0;
            while (vol < BGM_VOLUME && mp == currentBgm) {
                final double v = vol;
                Platform.runLater(() -> mp.setVolume(v));
                vol += FADE_STEP;
                try { Thread.sleep(FADE_DELAY); } catch (InterruptedException ignored) {}
            }
            Platform.runLater(() -> { if (mp == currentBgm) mp.setVolume(BGM_VOLUME); });
        }, "bgm-fade-in").start();
    }

    private void fadeOutAndStop(MediaPlayer mp) {
        new Thread(() -> {
            double vol = mp.getVolume();
            while (vol > 0) {
                final double v = Math.max(0, vol);
                Platform.runLater(() -> mp.setVolume(v));
                vol -= FADE_STEP;
                try { Thread.sleep(FADE_DELAY); } catch (InterruptedException ignored) {}
            }
            Platform.runLater(() -> { try { mp.stop(); mp.dispose(); } catch (Exception ignored) {} });
        }, "bgm-fade-out").start();
    }
}
