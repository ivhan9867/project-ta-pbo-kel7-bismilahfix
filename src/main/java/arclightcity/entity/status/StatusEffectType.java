package arclightcity.entity.status;


/**
 * Semua jenis status effect yang ada di game.
 * Dibagi menjadi:
 *   DEBUFF   = negatif, dikenakan ke musuh
 *   BUFF     = positif, diberikan ke sekutu
 *   DOT      = damage over time
 *   CONTROL  = crowd control (prevent action)
 */
public enum StatusEffectType {

    // ── DOT (Damage over Time) ──────────────────────────────
    BURN        ("🔥 Burn",         Category.DOT,     "Damage fisik tiap turn, stack 3x"),
    BLEED       ("🩸 Bleed",        Category.DOT,     "Damage fisik tiap turn, makin parah jika bergerak"),
    VIRUS       ("💻 Virus",        Category.DOT,     "Damage cyber tiap turn, chance spread ke enemy lain"),
    IRRADIATE   ("☢️ Irradiate",    Category.DOT,     "Damage energy tiap turn + reduce DEF"),
    CORRODE     ("🧪 Corrode",      Category.DOT,     "Reduce armor + damage tiap turn"),

    // ── CONTROL (CC) ─────────────────────────────────────────
    STUN        ("⚡ Stun",         Category.CONTROL, "Skip 1 turn, tidak bisa action"),
    FREEZE      ("❄️ Freeze",       Category.CONTROL, "Skip turn, damage fisik break freeze + bonus damage"),
    HACK        ("🖥️ Hack",         Category.CONTROL, "Kontrol enemy: mereka menyerang rekan sendiri 1 turn"),
    BLIND       ("🌑 Blind",        Category.CONTROL, "Accuracy turun drastis"),
    TAUNT       ("😡 Taunt",        Category.CONTROL, "Enemy harus menyerang target ini"),
    SILENCE     ("🔇 Silence",      Category.CONTROL, "Tidak bisa pakai skill, hanya basic attack"),
    SLEEP       ("😴 Sleep",        Category.CONTROL, "Skip turn, bangun jika terkena damage"),
    FEAR        ("😱 Fear",         Category.CONTROL, "Chance 50% skip turn karena ketakutan"),

    // ── DEBUFF (Stat Reduction) ──────────────────────────────
    WEAKEN      ("💔 Weaken",       Category.DEBUFF,  "Reduce PHYSICAL_ATK dan CYBER_ATK"),
    SLOW        ("🐌 Slow",         Category.DEBUFF,  "Reduce SPEED drastis"),
    SHRED       ("🛡️ Shred",        Category.DEBUFF,  "Reduce semua DEF — cocok sebelum burst"),
    EXPOSE      ("🎯 Expose",       Category.DEBUFF,  "Serangan berikutnya ke target ini +crit chance"),
    DRAIN       ("🧿 Drain",        Category.DEBUFF,  "MP berkurang tiap turn"),
    OVERLOAD    ("⚠️ Overload",     Category.DEBUFF,  "Jika terkena Cyber attack, explode untuk bonus damage"),

    // ── BUFF (Stat Enhancement) ──────────────────────────────
    REGEN       ("💚 Regen",        Category.BUFF,    "Pulihkan HP tiap turn"),
    BARRIER     ("🛡️ Barrier",      Category.BUFF,    "Absorb damage sejumlah tertentu sebelum kena HP"),
    OVERCLOCK   ("🚀 Overclock",    Category.BUFF,    "SPEED dan ATK meningkat drastis, tapi kena CORRODE setelah habis"),
    STEALTH     ("👻 Stealth",      Category.BUFF,    "Tidak bisa ditarget langsung, hilang jika menyerang"),
    EMPOWERED   ("✨ Empowered",    Category.BUFF,    "Serangan berikutnya guaranteed crit + bonus damage"),
    FORTIFY     ("🏰 Fortify",      Category.BUFF,    "Semua DEF naik signifikan selama beberapa turn"),
    FOCUS       ("🎯 Focus",        Category.BUFF,    "Accuracy dan CRIT_CHANCE naik drastis"),
    SYNC        ("🔗 Sync",         Category.BUFF,    "Mercenary: share damage dan buff dengan player");

    // ─────────────────────────────────────────────────────────

    public enum Category { DOT, CONTROL, DEBUFF, BUFF }

    public final String displayName;
    public final Category category;
    public final String description;

    StatusEffectType(String displayName, Category category, String description) {
        this.displayName = displayName;
        this.category    = category;
        this.description = description;
    }

    public boolean isNegative() {
        return category == Category.DOT || category == Category.CONTROL || category == Category.DEBUFF;
    }

    public boolean isPositive() {
        return category == Category.BUFF;
    }
}
