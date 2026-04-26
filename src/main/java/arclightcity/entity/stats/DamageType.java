package arclightcity.entity.stats;

/**
 * Tipe damage — menentukan DEF mana yang dipakai untuk kalkulasi.
 * Setiap tipe punya "flavor" berbeda untuk skill dan enemy.
 */
public enum DamageType {

    PHYSICAL  ("Physical",  "Serangan fisik mentah — dikurangi PHYSICAL_DEF",  StatType.PHYSICAL_DEF),
    CYBER     ("Cyber",     "Serangan hack/virus — dikurangi CYBER_DEF",        StatType.CYBER_DEF),
    ENERGY    ("Energy",    "Serangan plasma/neon — dikurangi ENERGY_DEF",      StatType.ENERGY_DEF),
    TRUE      ("True",      "Damage murni — tidak bisa dikurangi DEF apapun",   null),
    HEAL      ("Heal",      "Bukan damage — memulihkan HP",                     null);

    public final String displayName;
    public final String description;
    public final StatType resistanceStat; // stat DEF yang dipakai untuk resist, null = unresistable

    DamageType(String displayName, String description, StatType resistanceStat) {
        this.displayName   = displayName;
        this.description   = description;
        this.resistanceStat = resistanceStat;
    }
}
