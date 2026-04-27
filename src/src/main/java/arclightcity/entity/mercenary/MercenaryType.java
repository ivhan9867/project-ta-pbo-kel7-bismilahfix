package arclightcity.entity.mercenary;

/**
 * Jenis mercenary yang tersedia di game.
 */
public enum MercenaryType {

    KIRA_VOSS       ("Kira Voss",       "Ghost Sniper"),
    TANK_RX9        ("Tank-RX9",        "Combat Android"),
    SERA_MEND       ("Sera Mend",       "Field Medic"),
    VECTOR          ("Vector",          "Cyber Assassin"),
    MAGNUS_FORGE    ("Magnus Forge",    "Heavy Gunner"),
    ECHO_NULL       ("Echo Null",       "Signal Jammer"),
    LYRA_BLOOM      ("Lyra Bloom",      "Neon Shaman");

    public final String displayName;
    public final String subtitle;

    MercenaryType(String displayName, String subtitle) {
        this.displayName = displayName;
        this.subtitle    = subtitle;
    }
}
