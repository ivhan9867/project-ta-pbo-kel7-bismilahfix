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
    public  static final String SLOT1_FILENAME  = "save_slot1.dat";
    public  static final String SLOT2_FILENAME  = "save_slot2.dat";
    public  static final String SLOT3_FILENAME  = "save_slot3.dat";
    public  static final String AUTO_FILENAME   = "save_auto.dat";
    // Alias untuk backward compat
    private static final String MANUAL_FILENAME = SLOT1_FILENAME;
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

    /** Simpan ke slot tertentu (1-3) */
    public static SaveResult saveSlot(GameSaveState state, int slot) {
        String filename = switch (slot) {
            case 2  -> SLOT2_FILENAME;
            case 3  -> SLOT3_FILENAME;
            default -> SLOT1_FILENAME;
        };
        return saveToFile(state, filename);
    }

    /** Simpan manual save (default ke slot 1) */
    public static SaveResult saveManual(GameSaveState state) {
        return saveToFile(state, SLOT1_FILENAME);
    }

    /** Info auto-save */
    public static String getAutoSaveSummary() {
        return loadAuto().map(s -> s.getSummary()).orElse("EMPTY");
    }

    /** Hapus auto-save */
    public static void deleteAutoSave() {
        try {
            java.nio.file.Path file = getSaveDirectory().resolve(AUTO_FILENAME);
            java.nio.file.Files.deleteIfExists(file);
        } catch (Exception ignored) {}
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

    /** Load dari slot tertentu */
    public static Optional<GameSaveState> loadSlot(int slot) {
        String filename = switch (slot) {
            case 2  -> SLOT2_FILENAME;
            case 3  -> SLOT3_FILENAME;
            default -> SLOT1_FILENAME;
        };
        return loadFromFile(filename);
    }

    /** Info save per slot */
    public static String getSlotSummary(int slot) {
        return loadSlot(slot).map(s -> s.getSummary()).orElse("EMPTY");
    }

    /** Hapus save di slot tertentu */
    public static void deleteSlot(int slot) {
        String filename = switch (slot) {
            case 2  -> SLOT2_FILENAME;
            case 3  -> SLOT3_FILENAME;
            default -> SLOT1_FILENAME;
        };
        try {
            java.nio.file.Path file = getSaveDirectory().resolve(filename);
            java.nio.file.Files.deleteIfExists(file);
        } catch (Exception ignored) {}
    }

    /** Load manual save. Empty Optional jika tidak ada atau corrupt. */
    public static Optional<GameSaveState> loadManual() {
        return loadFromFile(SLOT1_FILENAME);
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
            // serialVersionUID mismatch — save dari versi lama, hapus otomatis
            System.err.println("[SaveManager] Save version mismatch, deleting: " + e.getMessage());
            try {
                java.nio.file.Path file = getSaveDirectory().resolve(filename);
                java.nio.file.Files.deleteIfExists(file);
            } catch (Exception ignored) {}
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
