package arclightcity.entity.enemy;

import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.base.EntityType;
import arclightcity.entity.stats.StatType;
import java.util.List;
import java.util.Random;

/**
 * THERESA — Final Boss Sejati, Floor 51.
 * Pemimpin para Demon Lord yang ingin mengubah Nusantara
 * menjadi dataran es hampa dan dingin.
 *
 * Hanya bisa dilawan dengan Red Blossom Katana.
 * 6 fase: setiap fase Theresa mengubah taktik sepenuhnya.
 *
 * Dialog per fase menggambarkan lore:
 * - Fase 1: "Gadis lemah dari dunia lain... berani menantangku?"
 * - Fase 2: "Kau masih hidup? Menarik. Tunjukkan kekuatanmu."
 * - Fase 3 (40%): "Katana itu... Tidak mungkin! SERPIHAN GARUDA!"
 * - Fase 4 (20%): "Aku tidak akan dibunuh oleh gamer receh!"
 * - Fase 5 (10%): [Theresa menggunakan kekuatan penuh] "ICE AGE!!!"
 */
public class Theresa extends Boss {

    private static final Random RNG = new Random();
    private int attackPattern = 0;

    private static final String[] PHASE_DIALOGS = {
        "Gadis lemah dari dunia lain... berani menantangku? Lucu.",
        "Kau masih hidup? Menarik. Tunjukkan padaku kekuatanmu sesungguhnya.",
        "Katana itu... TIDAK MUNGKIN! Serpihan Garuda! Darimana kau mendapatkannya!?",
        "ARGH! Aku tidak akan dibunuh oleh seorang gamer dari dunia receh!!",
        "NUSANTARA AKAN MENJADI ES!!! ICE AGE — ULTIMA FREEZE!!!",
        "Im... impossible... seorang manusia... mengalahkan... aku..."
    };

    public Theresa() {
        super("Theresa",
              "Pemimpin tertinggi para Demon Lord — entitas yang datang dari kekosongan " +
              "antara dimensi untuk membekukan seluruh Nusantara. Hanya Red Blossom Katana " +
              "yang mampu melukai dirinya.",
              EnemyRace.DEMON, ThreatLevel.BOSS,
              "LOOT_FINAL_BOSS", 6,
              new double[]{0.85, 0.65, 0.40, 0.20, 0.10});
        initStats();
        expReward  = 9999;
        goldReward = 2000;
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,      5000);
        stats.setBase(StatType.MAX_MP,       800);
        stats.setBase(StatType.MAX_SHIELD,   800);

        // Serangan — semua tipe, tidak ada kelemahan
        stats.setBase(StatType.PHYSICAL_ATK, 100);
        stats.setBase(StatType.CYBER_ATK,    100);
        stats.setBase(StatType.ENERGY_ATK,   120); // Ice/energy primary

        // Pertahanan — sangat tinggi, hanya Red Blossom yang menembus
        stats.setBase(StatType.PHYSICAL_DEF,  60);
        stats.setBase(StatType.CYBER_DEF,      60);
        stats.setBase(StatType.ENERGY_DEF,     80);
        stats.setBase(StatType.EVASION,      0.20);
        stats.setBase(StatType.BLOCK_CHANCE, 0.15);

        // Utilitas
        stats.setBase(StatType.SPEED,          18);
        stats.setBase(StatType.SKILL_POWER,  0.60);
        stats.setBase(StatType.DAMAGE_MULT,  0.40);
        stats.setBase(StatType.HP_REGEN,       12); // regenerasi kuat
        stats.setBase(StatType.SHIELD_REGEN,    8);
    }

    @Override
    protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        // Rotasi 3 pola serangan berdasarkan fase
        attackPattern = (attackPattern + 1) % 3;
        Entity target = getLowestHpTarget(enemies);
        if (target == null) return CombatAction.pass();

        int phase = getCurrentPhase();

        return switch (attackPattern) {
            case 0 -> CombatAction.useSkill("NULL_FIELD", List.of(target.getId()));
            case 1 -> {
                List<String> all = enemies.stream()
                    .filter(Entity::isAlive).map(Entity::getId).toList();
                yield all.isEmpty() ? CombatAction.pass()
                    : CombatAction.useSkill("VOID_RUPTURE", all);
            }
            default -> {
                if (phase >= 3) {
                    // Fase tinggi: serangan lebih mematikan
                    List<String> all = enemies.stream()
                        .filter(Entity::isAlive).map(Entity::getId).toList();
                    yield all.isEmpty() ? CombatAction.pass()
                        : CombatAction.useSkill("DATA_FRAGMENTATION", all);
                }
                yield CombatAction.basicAttack(List.of(target.getId()));
            }
        };
    }

    @Override
    protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        Entity target = getHighestAtkTarget(enemies);
        return target == null ? CombatAction.pass()
            : CombatAction.useSkill("SOVEREIGN_STRIKE", List.of(target.getId()));
    }

    @Override
    protected CombatAction desperateAction(List<Entity> allies, List<Entity> enemies) {
        // Fase akhir: serangan semua dengan kekuatan penuh
        List<String> all = enemies.stream()
            .filter(Entity::isAlive).map(Entity::getId).toList();
        return all.isEmpty() ? CombatAction.pass()
            : CombatAction.useSkill("NULL_PROTOCOL", all);
    }

    @Override
    protected void onPhaseTransition(int fromPhase, int toPhase) {
        // Dialog otomatis per fase — dikirim via combat log
        if (toPhase <= PHASE_DIALOGS.length) {
            // Phase dialog disimpan untuk ditampilkan di CombatView
            System.out.println("[THERESA - FASE " + toPhase + "] " +
                               PHASE_DIALOGS[toPhase - 1]);
        }
    }

    /** Ambil dialog Theresa untuk fase tertentu */
    public String getPhaseDialog(int phase) {
        int idx = Math.max(0, Math.min(phase - 1, PHASE_DIALOGS.length - 1));
        return PHASE_DIALOGS[idx];
    }

    /**
     * Theresa tidak bisa mati sebelum fase 6.
     * Setiap fase-boundary meng-floor HP-nya ke 1 (bukan 0).
     * Phase 1: HP tidak boleh < 85% max
     * Phase 2: HP tidak boleh < 65% max
     * Phase 3: HP tidak boleh < 40% max
     * Phase 4: HP tidak boleh < 20% max
     * Phase 5: HP tidak boleh < 10% max
     * Phase 6: bisa mati
     */
    @Override
    public DamageResult receiveDamage(double rawDamage, arclightcity.entity.stats.DamageType type, boolean ignoreArmor) {
        double maxHp = getStats().get(arclightcity.entity.stats.StatType.MAX_HP);
        double[] phaseBounds = {0.85, 0.65, 0.40, 0.20, 0.10, 0.0};
        int currentPh = getCurrentPhase();

        DamageResult result = super.receiveDamage(rawDamage, type, ignoreArmor);

        // Floor HP per phase — Theresa tidak bisa mati sebelum fase 6
        if (currentPh <= 5 && isAlive()) {
            double minHpForPhase = maxHp * phaseBounds[currentPh - 1] + 1;
            if (getCurrentHp() < minHpForPhase) setHpDirect(minHpForPhase);
        }
        return result;
    }

    public int getCurrentPhase() {
        double hpPct = getCurrentHp() / getStats().get(StatType.MAX_HP);
        if (hpPct > 0.85) return 1;
        if (hpPct > 0.65) return 2;
        if (hpPct > 0.40) return 3;
        if (hpPct > 0.20) return 4;
        if (hpPct > 0.10) return 5;
        return 6;
    }
}
