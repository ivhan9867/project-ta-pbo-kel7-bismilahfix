package arclightcity.ui.lore;

/**
 * Satu "beat" dalam dialog/cutscene.
 * choices != null → tampilkan opsi pilihan (tidak mempengaruhi cerita, hanya fun)
 */
public record DialogBeat(
    String   bg,
    String   portraitLeft,
    String   portraitRight,
    String   speaker,
    String   text,
    String   videoPath,
    boolean  fadeIn,
    String[] choices       // null = tidak ada pilihan
) {
    // ── Narasi tanpa karakter ──────────────────────────────
    public static DialogBeat narration(String bg, String text) {
        return new DialogBeat(bg, null, null, null, text, null, true, null);
    }
    // ── Dialog karakter kiri ───────────────────────────────
    public static DialogBeat left(String bg, String portrait, String speaker, String text) {
        return new DialogBeat(bg, portrait, null, speaker, text, null, false, null);
    }
    // ── Dialog karakter kanan ──────────────────────────────
    public static DialogBeat right(String bg, String portrait, String speaker, String text) {
        return new DialogBeat(bg, null, portrait, speaker, text, null, false, null);
    }
    // ── Player pilihan (tampil sebagai opsi Asuna menjawab) ──
    public static DialogBeat choose(String bg, String portrait, String speaker, String text, String... opts) {
        return new DialogBeat(bg, portrait, null, speaker, text, null, false, opts);
    }
    // ── Gambar penuh layar tanpa teks ─────────────────────
    public static DialogBeat image(String imgPath) {
        return new DialogBeat("lore/cutscene/" + imgPath, null, null, null, null, null, true, null);
    }
    // ── Video mp4 ─────────────────────────────────────────
    public static DialogBeat video(String filename) {
        return new DialogBeat(null, null, null, null, null, "lore/video/" + filename, false, null);
    }
    // ── Helpers untuk path ────────────────────────────────
    public static String cut(String f)  { return "lore/cutscene/" + f; }
    public static String bg(String f)   { return "lore/bg/" + f; }
    public static String gm(String n)   { return "portraits/guildmate/" + n + "_normal.png"; }
    public static String gmA(String n)  { return "portraits/guildmate/" + n + "_alt.png"; }
    public static String asuna()        { return "portraits/asuna/asuna_normal.png"; }
    public static String asunaL(int n)  { return "portraits/asuna/asuna_lore" + n + ".png"; }
    public static String boss(String n) { return "portraits/boss/" + n + "_normal.png"; }
    public static String bossA(String n){ return "portraits/boss/" + n + "_angry.png"; }
    public static String npc(String n)  { return "lore/npc/" + n + ".png"; }
}
