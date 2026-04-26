package arclightcity.entity.enemy;

/**
 * Tingkat ancaman enemy — mempengaruhi stat scaling, loot quality, EXP.
 */
public enum ThreatLevel {

    MINION   ("Minion",    1.0,  "Musuh lemah, sering muncul berkelompok"),
    STANDARD ("Standard",  1.5,  "Musuh biasa, backbone enemy di dungeon"),
    ELITE    ("Elite",     2.5,  "Musuh kuat dengan mechanic khusus"),
    CHAMPION ("Champion",  4.0,  "Mini-boss, muncul tiap 5 floor"),
    BOSS     ("Boss",      7.0,  "Boss utama, 1 per dungeon segment"),
    APEX     ("Apex",     15.0,  "Boss legendaris, hanya di floor terdalam");

    public final String displayName;
    public final double statMultiplier;  // multiplier stat base dibanding MINION
    public final String description;

    ThreatLevel(String displayName, double statMultiplier, String description) {
        this.displayName    = displayName;
        this.statMultiplier = statMultiplier;
        this.description    = description;
    }
}
