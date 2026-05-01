package arclightcity.entity.mercenary;

/**
 * Jenis mercenary yang tersedia di game.
 */
public enum MercenaryType {

    KIRA_VOSS       ("Srikandi",      "Pemanah Bayangan"),
    TANK_RX9        ("Gatot Kaca",    "Ksatria Baja"),
    SERA_MEND       ("Nyai Roro",     "Tabib Mistis"),
    VECTOR          ("Rangga",        "Pembunuh Bayaran"),
    MAGNUS_FORGE    ("Bima",          "Petarung Agung"),
    ECHO_NULL       ("Ki Ageng",      "Dukun Tua"),
    LYRA_BLOOM      ("Dewi Sri",      "Penjaga Keseimbangan");

    public final String displayName;
    public final String subtitle;

    MercenaryType(String displayName, String subtitle) {
        this.displayName = displayName;
        this.subtitle    = subtitle;
    }
}
