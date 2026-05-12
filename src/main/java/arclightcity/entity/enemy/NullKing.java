package arclightcity.entity.enemy;
import arclightcity.combat.CombatAction;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.StatType;


import java.util.List;

/**
 * NULL KING — The Corrupted Sovereign
 * Boss zona awal (Floor 10), ThreatLevel: BOSS
 *
 * Lore: Dulunya CEO MegaCorp cabang Sector 7. Saat eksperimen
 * digital consciousness-nya gagal, kesadarannya tersebar ke seluruh
 * jaringan dan menjadi sesuatu yang tidak lagi manusiawi.
 * Sekarang dia mengklaim seluruh Mythic Item Obtained sebagai "domain"-nya.
 *
 * ═══ UNIQUE MECHANIC: NULL FIELD ═══
 * Boss ini bisa membuat "Null Field" yang menonaktifkan semua buff party
 * dan mencegah pemberian buff baru selama 2 turn.
 *
 * PHASE 1 (HP 100-60%): "The Sovereign" — angkuh, calculated, CC berat
 * PHASE 2 (HP 60-30%): "Fragmented" — mulai panik, AoE serangan masif
 * PHASE 3 (HP <30%):   "Null Protocol" — semua stat meledak, spam Null Field
 */
public class NullKing extends Boss {

    private boolean nullFieldActive  = false;
    private int     nullFieldCooldown = 0;
    private int     cloneCount       = 0; // Phase 2: spawn phantom clone

    public NullKing() {
        super("Batara Kala",
              "Suaranya terdengar dari mana-mana dan tidak dari mana-mana. " +
              "'Kalian semua hanyalah data dalam sistemku.'",
              EnemyRace.ANDROID,
              ThreatLevel.BOSS,
              "LOOT_BOSS_ZONE1",
              3,                        // 3 fase
              new double[]{0.60, 0.30}  // threshold fase 2 dan 3
        );

        this.uniqueMechanicName = "Null Field";
        this.uniqueMechanicDesc = "Menonaktifkan semua buff party dan prevent buff baru selama 2 turn";
        this.enrageTurnLimit    = 25;

        initStats();
        expReward  = 800;
        goldReward = 500;
        initVitals();
    }

    @Override
    protected void initStats() {
        stats.setBase(StatType.MAX_HP,       1200);
        stats.setBase(StatType.MAX_MP,       200);
        stats.setBase(StatType.MAX_SHIELD,   400);  // Boss punya shield besar
        stats.setBase(StatType.SHIELD_REGEN, 20);   // regen shield per turn
        stats.setBase(StatType.MP_REGEN,     20);
        stats.setBase(StatType.HP_REGEN,     15);
        stats.setBase(StatType.PHYSICAL_ATK, 35);
        stats.setBase(StatType.CYBER_ATK,    55);
        stats.setBase(StatType.ENERGY_ATK,   30);
        stats.setBase(StatType.DAMAGE_MULT,  0.15); // +15% semua damage
        stats.setBase(StatType.PHYSICAL_DEF, 25);
        stats.setBase(StatType.CYBER_DEF,    40);
        stats.setBase(StatType.ENERGY_DEF,   20);
        stats.setBase(StatType.BLOCK_CHANCE, 0.15);
        stats.setBase(StatType.TENACITY,     0.60);
        stats.setBase(StatType.SPEED,        14);
        stats.setBase(StatType.CRIT_CHANCE,  0.15);
        stats.setBase(StatType.CRIT_DAMAGE,  1.80);
        stats.setBase(StatType.ACCURACY,     0.95);
    }

    @Override
    public void onTurnStart() {
        super.onTurnStart();
        if (nullFieldCooldown > 0) nullFieldCooldown--;
        if (nullFieldActive && turnElapsed % 2 == 0) nullFieldActive = false;
    }

    @Override
    protected CombatAction normalAction(List<Entity> allies, List<Entity> enemies) {
        return switch (currentPhase) {
            case 1 -> phase1Action(enemies);
            case 2 -> phase2Action(allies, enemies);
            case 3 -> phase3Action(enemies);
            default -> CombatAction.basicAttack(List.of(getRandomTarget(enemies).getId()));
        };
    }

    @Override
    protected CombatAction specialAction(List<Entity> allies, List<Entity> enemies) {
        // NULL FIELD — mechanic unik
        if (nullFieldCooldown == 0) {
            nullFieldCooldown = 4;
            nullFieldActive   = true;

            // Hapus semua buff dari semua musuh (party player)
            enemies.stream().filter(Entity::isAlive).forEach(e -> {
                for (StatusEffectType positive : List.of(
                        StatusEffectType.REGEN, StatusEffectType.BARRIER,
                        StatusEffectType.EMPOWERED, StatusEffectType.FORTIFY,
                        StatusEffectType.FOCUS, StatusEffectType.STEALTH,
                        StatusEffectType.OVERCLOCK, StatusEffectType.SYNC)) {
                    e.removeEffect(positive);
                }
                // Apply Overload sebagai insult injury
                e.applyEffect(new StatusEffect(StatusEffectType.OVERLOAD, 2, 0, this.id));
            });

            List<String> allIds = enemies.stream().filter(Entity::isAlive)
                    .map(Entity::getId).toList();
            return CombatAction.useSkill("NULL_FIELD", allIds);
        }

        return normalAction(allies, enemies);
    }

    private CombatAction phase1Action(List<Entity> enemies) {
        // Phase 1: CC berat + damage terkontrol
        Entity target = getLowestHpTarget(enemies);
        if (target == null) return CombatAction.pass();

        // Hack → target menyerang rekan sendiri
        if (!target.hasEffect(StatusEffectType.HACK) && Math.random() < 0.35) {
            target.applyEffect(new StatusEffect(StatusEffectType.HACK, 1, 0, this.id));
        }

        target.applyEffect(new StatusEffect(StatusEffectType.VIRUS, 2, 15.0, this.id));
        return CombatAction.useSkill("SOVEREIGN_STRIKE", List.of(target.getId()));
    }

    private CombatAction phase2Action(List<Entity> allies, List<Entity> enemies) {
        // Phase 2: Panik, AoE semua musuh
        List<String> allIds = enemies.stream().filter(Entity::isAlive)
                .map(Entity::getId).toList();

        enemies.stream().filter(Entity::isAlive).forEach(e -> {
            e.applyEffect(new StatusEffect(StatusEffectType.IRRADIATE, 2, 20.0, this.id));
            e.applyEffect(new StatusEffect(StatusEffectType.SHRED,     2, 15.0, this.id));
        });

        return CombatAction.useSkill("DATA_FRAGMENTATION", allIds);
    }

    private CombatAction phase3Action(List<Entity> enemies) {
        // Phase 3: Null Protocol — spam CC + true damage
        Entity target = getLowestHpTarget(enemies);
        if (target == null) return CombatAction.pass();

        // Fear semua
        enemies.stream().filter(Entity::isAlive).forEach(e ->
                e.applyEffect(new StatusEffect(StatusEffectType.FEAR, 1, 0, this.id)));

        List<String> allIds = enemies.stream().filter(Entity::isAlive)
                .map(Entity::getId).toList();
        return CombatAction.useSkill("NULL_PROTOCOL", allIds);
    }

    @Override
    protected void onPhaseTransition(int fromPhase, int toPhase) {
        if (toPhase == 2) {
            // Armor melemah, serangan meningkat
            stats.addBase(StatType.PHYSICAL_DEF, -10);
            stats.addBase(StatType.CYBER_ATK,     20);
            stats.addBase(StatType.SPEED,          3);
            // Aktifkan Null Field segera
            nullFieldCooldown = 0;
        }
        if (toPhase == 3) {
            // Null Protocol: semua stat meledak
            stats.addBase(StatType.CYBER_ATK,   35);
            stats.addBase(StatType.ENERGY_ATK,  25);
            stats.addBase(StatType.SPEED,        5);
            stats.addBase(StatType.CRIT_CHANCE,  0.30);
            // Null Field spam
            nullFieldCooldown = 0;
        }
    }

    @Override
    protected void onEnrage() {
        super.onEnrage();
        // Null King enrage: permanent Null Field + serangan tidak bisa di-miss
        nullFieldActive   = true;
        nullFieldCooldown = 0;
        stats.setBase(StatType.ACCURACY, 1.0);
    }

    @Override
    protected boolean shouldUseDesperateAction() { return false; } // Boss tidak flee
    @Override
    protected int getSpecialCooldown()           { return 3; }

    public boolean isNullFieldActive() { return nullFieldActive; }
}
