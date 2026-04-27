package arclightcity.entity.enemy;

/**
 * Ras/kategori enemy — menentukan flavor lore dan resistensi.
 */
public enum EnemyRace {

    HUMAN       ("Human",       "Manusia biasa atau augmented. Rentan Cyber attack."),
    CYBORG      ("Cyborg",      "Setengah manusia, setengah mesin. Resist Physical, lemah Energy."),
    ANDROID     ("Android",     "Sepenuhnya mesin. Resist Cyber, lemah ke Virus."),
    MUTANT      ("Mutant",      "Terekspos radiasi neon. HP tinggi, resist Energy."),
    SPECTER     ("Specter",     "Entitas digital yang bocor ke dunia nyata. Kebal Physical."),
    BEAST       ("Beast",       "Binatang yang bermutasi di kota. Agresif dan cepat."),
    CORPORATE   ("Corporate",   "Punya backup dan gadget mahal. Bisa summon bala bantuan."),
    ANOMALY     ("Anomaly",     "Sesuatu yang tidak seharusnya ada. Mechanic tidak terduga.");

    public final String displayName;
    public final String description;

    EnemyRace(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
