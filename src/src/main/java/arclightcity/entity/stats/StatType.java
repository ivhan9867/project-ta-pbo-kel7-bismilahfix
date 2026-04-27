package arclightcity.entity.stats;

/**
 * Semua tipe stat yang ada di game.
 * Dipisah enum agar mudah di-iterate, di-buff, di-debuff secara dinamis.
 */
public enum StatType {

    // ── VITAL ──────────────────────────────────────────────
    MAX_HP          ("Max HP",          "Total HP maksimum"),
    MAX_MP          ("Max MP",          "Total MP maksimum"),
    HP_REGEN        ("HP Regen",        "HP yang pulih tiap akhir turn"),
    MP_REGEN        ("MP Regen",        "MP yang pulih tiap akhir turn"),

    // ── SHIELD (bar terpisah dari HP, kena duluan sebelum HP) ──
    MAX_SHIELD      ("Max Shield",      "Total Shield maksimum — diserap sebelum HP"),
    SHIELD_REGEN    ("Shield Regen",    "Shield yang pulih tiap akhir turn"),
    SHIELD_MULT     ("Shield Mult",     "Multiplier Shield dari equipment (%)"),

    // ── OFFENSE ────────────────────────────────────────────
    PHYSICAL_ATK    ("Physical ATK",    "Damage serangan fisik (base)"),
    CYBER_ATK       ("Cyber ATK",       "Damage serangan cyber/hack (base)"),
    ENERGY_ATK      ("Energy ATK",      "Damage serangan energi/neon (base)"),
    DAMAGE_MULT     ("Damage Mult",     "Multiplier semua damage keluar dalam % (additive)"),
    CRIT_CHANCE     ("Crit Chance",     "Persentase critical hit (0.0 - 1.0)"),
    CRIT_DAMAGE     ("Crit Damage",     "Multiplier damage saat critical (default 1.5)"),
    ARMOR_PIERCE    ("Armor Pierce",    "Persentase armor musuh yang diabaikan"),
    LIFESTEAL       ("Lifesteal",       "Persentase damage yang kembali jadi HP"),

    // ── DEFENSE ────────────────────────────────────────────
    PHYSICAL_DEF    ("Physical DEF",    "Reduksi damage fisik"),
    CYBER_DEF       ("Cyber DEF",       "Reduksi damage cyber"),
    ENERGY_DEF      ("Energy DEF",      "Reduksi damage energi"),
    EVASION         ("Evasion",         "Chance menghindari serangan (0.0 - 1.0)"),
    BLOCK_CHANCE    ("Block Chance",    "Chance memblok & reduce damage 50%"),
    TENACITY        ("Tenacity",        "Reduksi durasi status effect"),

    // ── UTILITY ────────────────────────────────────────────
    SPEED           ("Speed",           "Menentukan urutan giliran"),
    INITIATIVE      ("Initiative",      "Bonus untuk giliran pertama"),
    ACCURACY        ("Accuracy",        "Chance serangan mengenai target"),
    SKILL_POWER     ("Skill Power",     "Multiplier damage khusus skill (terpisah dari DAMAGE_MULT)"),
    COOLDOWN_REDUCE ("CDR",             "Reduksi cooldown skill (0.0 - 1.0)"),

    // ── MERCENARY SPECIFIC ──────────────────────────────────
    COMMAND_AURA    ("Command Aura",    "Bonus stat yang diberikan ke mercenary sekutu"),
    SYNC_RATE       ("Sync Rate",       "Efektivitas buff mercenary ke player");

    // ───────────────────────────────────────────────────────
    public final String displayName;
    public final String description;

    StatType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
