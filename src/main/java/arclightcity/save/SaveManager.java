package arclightcity.save;

import java.io.*;
import java.nio.file.*;
import java.util.Optional;

/**
 * SaveManager — mengelola semua operasi save/load ke disk.
 *
 * File struktur:
 *   %APPDATA%/ArclightCity/  (Windows)
 *   ~/.arclight/             (Linux/Mac)
 *   ├── save_manual.dat      ← manual save (1 slot)
 *   └── save_auto.dat        ← auto-save backup
 *
 * Menggunakan Java Serialization (ObjectOutputStream/ObjectInputStream).
 * GameSaveState implements Serializable sehingga seluruh object tree
 * bisa di-serialize tanpa library eksternal.
 */
public class SaveManager {

    // ── File names ────────────────────────────────────────────
    private static final String MANUAL_FILENAME = "save_manual.dat";
    private static final String AUTO_FILENAME   = "save_auto.dat";
    private static final String SAVE_DIR_NAME   = "ArclightCity";

    // ── Result type ───────────────────────────────────────────

    public record SaveResult(boolean success, String message) {
        public static SaveResult ok(String msg)   { return new SaveResult(true, msg);  }
        public static SaveResult fail(String msg) { return new SaveResult(false, msg); }
    }

    // ── Save Directory ────────────────────────────────────────

    /**
     * Dapatkan direktori save yang sesuai dengan OS.
     * Windows: %APPDATA%\ArclightCity\
     * Mac/Linux: ~/.arclight/
     */
    public static Path getSaveDirectory() {
        String os = System.getProperty("os.name", "").toLowerCase();
        Path base;
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            base = appData != null
                    ? Paths.get(appData)
                    : Paths.get(System.getProperty("user.home"));
        } else {
            base = Paths.get(System.getProperty("user.home"), ".arclight");
        }
        return base.resolve(SAVE_DIR_NAME);
    }

    /** Pastikan direktori save ada, buat jika belum */
    private static void ensureSaveDir() throws IOException {
        Path dir = getSaveDirectory();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    // ── Save ──────────────────────────────────────────────────

    /** Simpan manual save */
    public static SaveResult saveManual(GameSaveState state) {
        return saveToFile(state, MANUAL_FILENAME);
    }

    /** Simpan auto-save */
    public static SaveResult saveAuto(GameSaveState state) {
        return saveToFile(state, AUTO_FILENAME);
    }

    private static SaveResult saveToFile(GameSaveState state, String filename) {
        try {
            ensureSaveDir();
            Path file = getSaveDirectory().resolve(filename);

            // Backup file lama sebelum overwrite (safety)
            if (Files.exists(file)) {
                Path backup = getSaveDirectory().resolve(filename + ".bak");
                Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file.toFile())))) {
                oos.writeObject(state);
            }

            System.out.println("[SaveManager] Saved to: " + file);
            return SaveResult.ok("Game saved! [" + state.savedAt + "]");

        } catch (IOException e) {
            System.err.println("[SaveManager] Save failed: " + e.getMessage());
            return SaveResult.fail("Save failed: " + e.getMessage());
        }
    }

    // ── Load ──────────────────────────────────────────────────

    /** Load manual save. Empty Optional jika tidak ada atau corrupt. */
    public static Optional<GameSaveState> loadManual() {
        return loadFromFile(MANUAL_FILENAME);
    }

    /** Load auto-save. */
    public static Optional<GameSaveState> loadAuto() {
        return loadFromFile(AUTO_FILENAME);
    }

    /**
     * Load save terbaru — coba manual dulu, lalu auto.
     * Kembalikan yang paling baru berdasarkan timestamp.
     */
    public static Optional<GameSaveState> loadLatest() {
        Optional<GameSaveState> manual = loadManual();
        Optional<GameSaveState> auto   = loadAuto();

        if (manual.isEmpty()) return auto;
        if (auto.isEmpty())   return manual;

        // Pilih yang paling baru
        long manualTime = manual.get().progress.lastSaveMs;
        long autoTime   = auto.get().progress.lastSaveMs;
        return autoTime > manualTime ? auto : manual;
    }

    private static Optional<GameSaveState> loadFromFile(String filename) {
        try {
            Path file = getSaveDirectory().resolve(filename);
            if (!Files.exists(file)) return Optional.empty();

            try (ObjectInputStream ois = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(file.toFile())))) {
                Object obj = ois.readObject();
                if (obj instanceof GameSaveState state) {
                    System.out.println("[SaveManager] Loaded: " + state);
                    return Optional.of(state);
                }
            }
        } catch (InvalidClassException e) {
            // serialVersionUID mismatch — save dari versi lama
            System.err.println("[SaveManager] Incompatible save version: " + e.getMessage());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[SaveManager] Load failed (" + filename + "): " + e.getMessage());
        }
        return Optional.empty();
    }

    // ── Status ────────────────────────────────────────────────

    /** Cek apakah ada save yang bisa di-load */
    public static boolean hasSave() {
        return Files.exists(getSaveDirectory().resolve(MANUAL_FILENAME)) ||
               Files.exists(getSaveDirectory().resolve(AUTO_FILENAME));
    }

    /** Info singkat save yang tersedia */
    public static String getSaveSummary() {
        Optional<GameSaveState> latest = loadLatest();
        return latest.map(GameSaveState::getSummary).orElse("No save found");
    }

    /** Hapus semua save (untuk New Game) */
    public static void deleteAllSaves() {
        try {
            Path dir = getSaveDirectory();
            for (String f : new String[]{MANUAL_FILENAME, AUTO_FILENAME,
                                          MANUAL_FILENAME + ".bak", AUTO_FILENAME + ".bak"}) {
                Files.deleteIfExists(dir.resolve(f));
            }
            System.out.println("[SaveManager] All saves deleted.");
        } catch (IOException e) {
            System.err.println("[SaveManager] Delete failed: " + e.getMessage());
        }
    }
}
