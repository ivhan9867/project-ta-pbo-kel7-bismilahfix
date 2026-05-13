package arclightcity.combat;
import arclightcity.entity.base.Entity;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.DamageType;
import arclightcity.entity.stats.StatType;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * SkillExecutor — mengeksekusi skill berdasarkan skill ID.
 *
 * Setiap skill punya:
 *   - Damage multiplier (dari ATK base)
 *   - Damage type
 *   - MP cost
 *   - Cooldown
 *   - Status effects yang di-apply
 *   - Target type (single / AoE / self)
 *
 * Ini adalah "registry" skill — setiap skill dieksekusi di sini,
 * bukan di dalam entity masing-masing.
 */
public class SkillExecutor {

    private static final Random RNG = new Random();

    // ── Execute Entry Point ───────────────────────────────────

    /**
     * Eksekusi skill dari caster ke target(s).
     * @return list CombatEvent dari hasil eksekusi
     */
    /** MP cost per skill */
    public static double getMpCost(String skillId) {
        if (skillId == null) return 0;
        return switch (skillId) {
            // Player skills
            case "POWER_STRIKE", "PUKULAN_HARIMAU"   -> 15;
            case "EXECUTE",      "TEBASAN_PAMUNGKAS"  -> 25;
            case "PHANTOM_SHOT", "PANAH_BAYANGAN"     -> 20;
            case "SHADOW_STEP",  "LANGKAH_GAIB"       -> 18;
            case "DEEP_HACK",    "BIDANG_BAYANGAN"    -> 22;
            case "NULL_FIELD",   "PROTOKOL_NOL"       -> 30;
            case "SOVEREIGN_STRIKE","TEBASAN_AGUNG"   -> 35;
            case "NULL_PROTOCOL"                      -> 28;
            case "DATA_FRAGMENTATION","PECAH_JIWA"   -> 40;
            case "ENERGY_DRAIN",  "SERAP_TENAGA"     -> 20;
            case "IRON_SHIELD",   "TAMENG_BAJA"      -> 18;
            case "SEISMIC_SLAM",  "GEMPA_BUMI"       -> 25;
            case "SACRED_SEAL",   "RAJAH_PELINDUNG"  -> 22;
            // Guildmate skills
            case "TAUNT"                         -> 15;
            case "TRIAGE_HEAL","CLEANSE_PROTOCOL"-> 18;
            case "OVERLOAD_SHOT","SHRED_CANNON"  -> 25;
            case "EMP_BURST","FREQUENCY_LOCK"    -> 22;
            case "SIGNAL_JAM"                    -> 18;
            case "NEON_BLOOM","BLOOM_MEND"       -> 20;
            // Enemy skills - biaya rendah
            case "NEON_VENOM","COIL_STRIKE","VIRUS_UPLOAD","CORRUPT",
                 "SHOCKWAVE","VOID_RUPTURE" -> 10;
            case "SELF_DESTRUCT" -> 0;
            // Skill guildmate baru
            case "FORTIFY_TEAM","FOCUS_TEAM","SYNC_TEAM","STEALTH_STEP" -> 10;
            case "REGEN_TEAM","EMPOWER_TEAM","BARRIER_SHIELD","BLOOM_BARRIER" -> 12;
            case "STUN_SLAM","WEAKEN_AURA","BLEED_SLASH","EXPOSE_SHOT","SLOW_CURSE" -> 8;
            default -> skillId.contains("BOSS") ? 30 : 12;
        };
    }


    public static List<CombatEvent> execute(String skillId, Entity caster,
                                             List<Entity> targets,
                                             List<Entity> allAllies,
                                             List<Entity> allEnemies) {
        List<CombatEvent> events = new ArrayList<>();

        // Cek dan kurangi MP — jika tidak cukup, batalkan skill
        double mpCost = getMpCost(skillId);
        if (mpCost > 0 && caster.getCurrentMp() < mpCost) {
            events.add(CombatEvent.actionPrevented(caster.getId(), caster.getName(),
                "MP tidak cukup! (" + (int)caster.getCurrentMp() + "/" + (int)mpCost + " MP)"));
            return events;
        }
        if (mpCost > 0) caster.spendMp(mpCost);

        events.add(CombatEvent.skillUsed(caster.getId(), caster.getName(), skillId));

        return switch (skillId) {

            // ════════════════════════════════════════
            // PLAYER / UNIVERSAL SKILLS
            // ════════════════════════════════════════

            case "POWER_STRIKE" -> powerStrike(caster, targets, events);
            case "EXECUTE"      -> execute(caster, targets, events);
            case "PHANTOM_SHOT" -> phantomShot(caster, targets, events);
            case "SHADOW_STEP"  -> shadowStep(caster, targets, events);
            case "DEEP_HACK"    -> deepHack(caster, targets, events);

            // ════════════════════════════════════════
            // ENEMY SKILLS
            // ════════════════════════════════════════

            case "NEON_VENOM"           -> neonVenom(caster, targets, events);
            case "COIL_STRIKE"          -> coilStrike(caster, targets, events);
            case "VIRUS_UPLOAD"         -> virusUpload(caster, targets, events);
            case "SELF_DESTRUCT"        -> selfDestruct(caster, targets, events);
            case "SHOCKWAVE"            -> shockwave(caster, targets, events);
            case "VOID_RUPTURE"         -> voidRupture(caster, targets, events);
            case "CORRUPT"              -> corrupt(caster, targets, events);

            // ════════════════════════════════════════
            // BOSS SKILLS
            // ════════════════════════════════════════

            case "NULL_FIELD"           -> nullField(caster, targets, events);
            case "SOVEREIGN_STRIKE"     -> sovereignStrike(caster, targets, events);
            case "DATA_FRAGMENTATION"   -> dataFragmentation(caster, targets, events);
            case "NULL_PROTOCOL"        -> nullProtocol(caster, targets, events);

            // ════════════════════════════════════════
            // MERCENARY SKILLS
            // ════════════════════════════════════════

            case "IRON_SHIELD"          -> ironShield(caster, targets, events);
            case "EMERGENCY_REPAIR"     -> emergencyRepair(caster, events);
            case "TRIAGE_HEAL"          -> triageHeal(caster, targets, events);
            case "CLEANSE_PROTOCOL"     -> cleanseProtocol(caster, targets, events);
            case "FIELD_BARRIER"        -> fieldBarrier(caster, targets, events);
            case "BIO_IRRADIATE"        -> bioIrradiate(caster, targets, events);
            case "NEON_BLOOM"           -> neonBloom(caster, targets, events);
            case "BLOOM_MEND"           -> bloomMend(caster, targets, events);
            case "RESONANCE"            -> resonance(caster, targets, events);
            case "ENERGY_DRAIN"         -> energyDrain(caster, targets, events);
            case "EMP_BURST"            -> empBurst(caster, targets, events);
            case "FREQUENCY_LOCK"       -> frequencyLock(caster, targets, events);
            case "SIGNAL_JAM"           -> signalJam(caster, targets, events);
            case "SIGNAL_VIRUS"         -> signalVirus(caster, targets, events);
            case "INCENDIARY_ROUND"     -> incendiaryRound(caster, targets, events);
            case "SHRED_CANNON"         -> shredCannon(caster, targets, events);
            case "OVERLOAD_SHOT"        -> overloadShot(caster, targets, events);
            case "OVERLOAD_CHARGE"      -> overloadCharge(caster, events);

            // ─── GUILDMATE BUFF SKILLS ──────────────────────────────
            case "FORTIFY_TEAM"   -> { buffTeam(caster, targets, allAllies,
                arclightcity.entity.status.StatusEffectType.FORTIFY, 8, events, "Pertahanan meningkat!"); yield events; }
            case "FOCUS_TEAM"     -> { buffTeam(caster, targets, allAllies,
                arclightcity.entity.status.StatusEffectType.FOCUS, 6, events, "Fokus dan akurasi meningkat!"); yield events; }
            case "REGEN_TEAM"     -> { buffTeam(caster, targets, allAllies,
                arclightcity.entity.status.StatusEffectType.REGEN, 9, events, "Regenerasi HP aktif!"); yield events; }
            case "EMPOWER_TEAM"   -> { buffTeam(caster, targets, allAllies,
                arclightcity.entity.status.StatusEffectType.EMPOWERED, 6, events, "Serangan menguat!"); yield events; }
            case "SYNC_TEAM"      -> { buffTeam(caster, targets, allAllies,
                arclightcity.entity.status.StatusEffectType.SYNC, 6, events, "Kecepatan tersinkronisasi!"); yield events; }
            // ─── GUILDMATE CC SKILLS ────────────────────────────────
            // ─── GUILDMATE ATTACK SKILLS ────────────────────────────
            default -> {
                events.add(new CombatEvent.Builder(CombatEvent.EventType.SKILL_FAILED)
                        .actor(caster.getId(), caster.getName())
                        .message("Unknown skill: " + skillId).build());
                yield events;
            }
        };
    }

    // ════════════════════════════════════════════════════════
    // PLAYER / UNIVERSAL SKILLS
    // ════════════════════════════════════════════════════════

    private static List<CombatEvent> powerStrike(Entity caster, List<Entity> targets,
                                                  List<CombatEvent> events) {
        // 160% Physical ATK, chance stun
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.PHYSICAL_ATK) * 1.60;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.PHYSICAL, 0.05);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> execute(Entity caster, List<Entity> targets,
                                              List<CombatEvent> events) {
        // True damage = 200% Cyber ATK, tapi jika target HP < 20% → insta-kill
        for (Entity target : targets) {
            if (target.getHpPercent() < 0.20) {
                // Instant kill — bypass semua
                Entity.DamageResult result = target.receiveDamage(99999, DamageType.TRUE, true);
                events.add(CombatEvent.damage(caster.getId(), caster.getName(),
                        target.getId(), target.getName(), result.damage, DamageType.TRUE));
                if (!target.isAlive()) events.add(CombatEvent.death(target.getId(), target.getName()));
            } else {
                double base = caster.getStats().get(StatType.CYBER_ATK) * 2.0;
                var calc = DamageCalculator.calculate(caster, target, base, DamageType.CYBER, 0.30);
                applyCalcResult(caster, target, calc, events);
            }
        }
        return events;
    }

    private static List<CombatEvent> phantomShot(Entity caster, List<Entity> targets,
                                                   List<CombatEvent> events) {
        // 220% Physical ATK dari stealth, guaranteed hit, remove stealth setelah
        caster.removeEffect(StatusEffectType.STEALTH);
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.PHYSICAL_ATK) * 2.20;
            // Force crit via Empowered (sudah di-apply sebelumnya)
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.PHYSICAL, 0.10);
            applyCalcResult(caster, target, calc, events);
        }
        // Remove Empowered setelah dipakai
        caster.removeEffect(StatusEffectType.EMPOWERED);
        return events;
    }

    private static List<CombatEvent> shadowStep(Entity caster, List<Entity> targets,
                                                  List<CombatEvent> events) {
        // 180% Cyber ATK, bypass evasion, apply Expose
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.CYBER_ATK) * 1.80;
            // Bypass evasion: force accuracy 100%
            caster.getStats().addBuff(StatType.ACCURACY, 999);
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.CYBER, 0.20);
            caster.getStats().removeBuff(StatType.ACCURACY, 999);
            applyCalcResult(caster, target, calc, events);
            target.applyEffect(new StatusEffect(StatusEffectType.EXPOSE, 2, 0, caster.getId()));
            events.add(CombatEvent.effectApplied(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), StatusEffectType.EXPOSE));
        }
        return events;
    }

    private static List<CombatEvent> deepHack(Entity caster, List<Entity> targets,
                                               List<CombatEvent> events) {
        // 120% Cyber ATK + HACK effect
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.CYBER_ATK) * 1.20;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.CYBER, 0);
            applyCalcResult(caster, target, calc, events);
            target.applyEffect(new StatusEffect(StatusEffectType.HACK, 1, 0, caster.getId()));
            events.add(CombatEvent.effectApplied(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), StatusEffectType.HACK));
        }
        return events;
    }

    // ════════════════════════════════════════════════════════
    // ENEMY SKILLS
    // ════════════════════════════════════════════════════════

    private static List<CombatEvent> neonVenom(Entity caster, List<Entity> targets,
                                                List<CombatEvent> events) {
        // 140% Energy ATK + Bleed stack 2 + Slow
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.ENERGY_ATK) * 1.40;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.ENERGY, 0);
            applyCalcResult(caster, target, calc, events);
            target.applyEffect(new StatusEffect(StatusEffectType.BLEED, 4, 15.0, caster.getId()));
            target.applyEffect(new StatusEffect(StatusEffectType.SLOW,  2, 0.5,  caster.getId()));
        }
        return events;
    }

    private static List<CombatEvent> coilStrike(Entity caster, List<Entity> targets,
                                                  List<CombatEvent> events) {
        // AoE 120% Physical + Bleed semua
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.PHYSICAL_ATK) * 1.20;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.PHYSICAL, 0);
            applyCalcResult(caster, target, calc, events);
            target.applyEffect(new StatusEffect(StatusEffectType.BLEED, 3, 12.0, caster.getId()));
        }
        return events;
    }

    private static List<CombatEvent> virusUpload(Entity caster, List<Entity> targets,
                                                   List<CombatEvent> events) {
        // 150% Cyber ATK + Virus DOT
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.CYBER_ATK) * 1.50;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.CYBER, 0.10);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> selfDestruct(Entity caster, List<Entity> targets,
                                                    List<CombatEvent> events) {
        // AoE True Damage = 300% Physical ATK
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.PHYSICAL_ATK) * 3.0;
            Entity.DamageResult result = target.receiveDamage(base, DamageType.TRUE, true);
            events.add(CombatEvent.damage(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), result.damage, DamageType.TRUE));
            if (!target.isAlive()) events.add(CombatEvent.death(target.getId(), target.getName()));
        }
        events.add(CombatEvent.death(caster.getId(), caster.getName() + " (Self Destruct)"));
        return events;
    }

    private static List<CombatEvent> shockwave(Entity caster, List<Entity> targets,
                                                List<CombatEvent> events) {
        // AoE 130% Physical + 35% Stun chance
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.PHYSICAL_ATK) * 1.30;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.PHYSICAL, 0);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> voidRupture(Entity caster, List<Entity> targets,
                                                   List<CombatEvent> events) {
        // AoE 200% Cyber ATK True Damage
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.CYBER_ATK) * 2.0;
            Entity.DamageResult result = target.receiveDamage(base, DamageType.TRUE, true);
            events.add(CombatEvent.damage(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), result.damage, DamageType.TRUE));
            if (!target.isAlive()) events.add(CombatEvent.death(target.getId(), target.getName()));
        }
        return events;
    }

    private static List<CombatEvent> corrupt(Entity caster, List<Entity> targets,
                                              List<CombatEvent> events) {
        // Efek corrupt sudah dihandle di VoidSpecter.specialAction
        // Di sini hanya apply Cyber damage kecil
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.CYBER_ATK) * 0.80;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.CYBER, 0);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    // ════════════════════════════════════════════════════════
    // BOSS SKILLS
    // ════════════════════════════════════════════════════════

    private static List<CombatEvent> nullField(Entity caster, List<Entity> targets,
                                                List<CombatEvent> events) {
        // Efek buff removal sudah di NullKing.specialAction
        // Di sini Cyber damage ke semua
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.CYBER_ATK) * 1.10;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.CYBER, 0);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> sovereignStrike(Entity caster, List<Entity> targets,
                                                       List<CombatEvent> events) {
        // 175% Cyber ATK single target
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.CYBER_ATK) * 1.75;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.CYBER, 0.15);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> dataFragmentation(Entity caster, List<Entity> targets,
                                                         List<CombatEvent> events) {
        // AoE 140% Energy + Irradiate semua
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.ENERGY_ATK) * 1.40;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.ENERGY, 0);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> nullProtocol(Entity caster, List<Entity> targets,
                                                    List<CombatEvent> events) {
        // AoE True Damage 160% semua stat
        for (Entity target : targets) {
            double base = (caster.getStats().get(StatType.CYBER_ATK)
                         + caster.getStats().get(StatType.ENERGY_ATK)) * 0.80;
            Entity.DamageResult result = target.receiveDamage(base, DamageType.TRUE, true);
            events.add(CombatEvent.damage(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), result.damage, DamageType.TRUE));
            if (!target.isAlive()) events.add(CombatEvent.death(target.getId(), target.getName()));
        }
        return events;
    }

    // ════════════════════════════════════════════════════════
    // MERCENARY SKILLS
    // ════════════════════════════════════════════════════════

    private static List<CombatEvent> ironShield(Entity caster, List<Entity> targets,
                                                  List<CombatEvent> events) {
        // Pasang Barrier ke target
        for (Entity target : targets) {
            double power = caster.getStats().get(StatType.PHYSICAL_DEF) * 0.5;
            target.applyEffect(new StatusEffect(StatusEffectType.BARRIER, 2, power, caster.getId()));
            events.add(CombatEvent.effectApplied(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), StatusEffectType.BARRIER));
        }
        return events;
    }

    private static List<CombatEvent> emergencyRepair(Entity caster, List<CombatEvent> events) {
        double healAmt = caster.getStats().get(StatType.MAX_HP) * 0.30;
        double actual  = caster.receiveHeal(healAmt);
        events.add(CombatEvent.heal(caster.getId(), caster.getName(),
                caster.getId(), caster.getName(), actual));
        return events;
    }

    private static List<CombatEvent> triageHeal(Entity caster, List<Entity> targets,
                                                  List<CombatEvent> events) {
        double skillPower = caster.getStats().get(StatType.SKILL_POWER);
        for (Entity target : targets) {
            double healAmt = (40 + caster.getStats().get(StatType.MAX_MP) * 0.30) * skillPower;
            double actual  = target.receiveHeal(healAmt);
            events.add(CombatEvent.heal(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), actual));
        }
        return events;
    }

    private static List<CombatEvent> cleanseProtocol(Entity caster, List<Entity> targets,
                                                       List<CombatEvent> events) {
        // Cleanse sudah di SeraMend.healAction — di sini hanya log
        for (Entity target : targets) {
            events.add(new CombatEvent.Builder(CombatEvent.EventType.EFFECT_EXPIRED)
                    .actor(caster.getId(), caster.getName())
                    .target(target.getId(), target.getName())
                    .message("🧹 " + caster.getName() + " cleanses all debuffs from " + target.getName())
                    .build());
        }
        return events;
    }

    private static List<CombatEvent> fieldBarrier(Entity caster, List<Entity> targets,
                                                    List<CombatEvent> events) {
        for (Entity target : targets) {
            double power = 30 + caster.getStats().get(StatType.SKILL_POWER) * 10;
            target.applyEffect(new StatusEffect(StatusEffectType.BARRIER, 2, power, caster.getId()));
            events.add(CombatEvent.effectApplied(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), StatusEffectType.BARRIER));
        }
        return events;
    }

    private static List<CombatEvent> bioIrradiate(Entity caster, List<Entity> targets,
                                                    List<CombatEvent> events) {
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.ENERGY_ATK) * 1.20;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.ENERGY, 0);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> neonBloom(Entity caster, List<Entity> targets,
                                                List<CombatEvent> events) {
        double skillPower = caster.getStats().get(StatType.SKILL_POWER);
        for (Entity target : targets) {
            double healAmt = 30 * skillPower;
            double actual  = target.receiveHeal(healAmt);
            events.add(CombatEvent.heal(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), actual));
        }
        return events;
    }

    private static List<CombatEvent> bloomMend(Entity caster, List<Entity> targets,
                                                List<CombatEvent> events) {
        double sp = caster.getStats().get(StatType.SKILL_POWER);
        for (Entity target : targets) {
            double healAmt = (50 + caster.getStats().get(StatType.ENERGY_ATK) * 0.5) * sp;
            double actual  = target.receiveHeal(healAmt);
            events.add(CombatEvent.heal(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), actual));
        }
        return events;
    }

    private static List<CombatEvent> resonance(Entity caster, List<Entity> targets,
                                                List<CombatEvent> events) {
        // Buff sudah di-apply di LyraBloom.combatAction — log saja
        events.add(new CombatEvent.Builder(CombatEvent.EventType.EFFECT_APPLIED)
                .actor(caster.getId(), caster.getName())
                .message("✨ " + caster.getName() + " activates Resonance — party empowered!")
                .build());
        return events;
    }

    private static List<CombatEvent> energyDrain(Entity caster, List<Entity> targets,
                                                   List<CombatEvent> events) {
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.ENERGY_ATK) * 1.0;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.ENERGY, 0);
            applyCalcResult(caster, target, calc, events);
            // Heal caster dari drain
            double healAmt = calc.finalDamage * 0.40;
            double actual  = caster.receiveHeal(healAmt);
            events.add(CombatEvent.heal(caster.getId(), caster.getName(),
                    caster.getId(), caster.getName(), actual));
        }
        return events;
    }

    private static List<CombatEvent> empBurst(Entity caster, List<Entity> targets,
                                               List<CombatEvent> events) {
        // Cyber damage + stun sudah di-apply — log damage
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.CYBER_ATK) * 1.50;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.CYBER, 0.20);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> frequencyLock(Entity caster, List<Entity> targets,
                                                     List<CombatEvent> events) {
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.CYBER_ATK) * 0.80;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.CYBER, 0);
            applyCalcResult(caster, target, calc, events);
            events.add(CombatEvent.effectApplied(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), StatusEffectType.FREEZE));
        }
        return events;
    }

    private static List<CombatEvent> signalJam(Entity caster, List<Entity> targets,
                                                List<CombatEvent> events) {
        // Log AoE CC
        events.add(new CombatEvent.Builder(CombatEvent.EventType.EFFECT_APPLIED)
                .actor(caster.getId(), caster.getName())
                .message("📡 " + caster.getName() + " jams all signals — enemies silenced & blinded!")
                .build());
        return events;
    }

    private static List<CombatEvent> signalVirus(Entity caster, List<Entity> targets,
                                                   List<CombatEvent> events) {
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.CYBER_ATK) * 1.10;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.CYBER, 0);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> incendiaryRound(Entity caster, List<Entity> targets,
                                                       List<CombatEvent> events) {
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.PHYSICAL_ATK) * 1.20;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.PHYSICAL, 0);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> shredCannon(Entity caster, List<Entity> targets,
                                                   List<CombatEvent> events) {
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.PHYSICAL_ATK) * 0.90;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.PHYSICAL, 0.15);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> overloadShot(Entity caster, List<Entity> targets,
                                                    List<CombatEvent> events) {
        // 280% Physical ATK AoE setelah charge
        for (Entity target : targets) {
            double base = caster.getStats().get(StatType.PHYSICAL_ATK) * 2.80;
            var calc = DamageCalculator.calculate(caster, target, base, DamageType.PHYSICAL, 0.20);
            applyCalcResult(caster, target, calc, events);
        }
        return events;
    }

    private static List<CombatEvent> overloadCharge(Entity caster, List<CombatEvent> events) {
        events.add(new CombatEvent.Builder(CombatEvent.EventType.SKILL_USED)
                .actor(caster.getId(), caster.getName())
                .message("⚙️ " + caster.getName() + " charges the cannon... (next turn: OVERLOAD!)")
                .build());
        return events;
    }

    // ════════════════════════════════════════════════════════
    // HELPER
    // ════════════════════════════════════════════════════════

    /**
     * Apply hasil DamageCalcResult ke target dan buat CombatEvent yang sesuai.
     */
    private static void applyCalcResult(Entity caster, Entity target,
                                         DamageCalculator.DamageCalcResult calc,
                                         List<CombatEvent> events) {
        if (calc.isMissed) {
            events.add(CombatEvent.evaded(caster.getId(), caster.getName(),
                    target.getId(), target.getName()));
            return;
        }

        Entity.DamageResult result = target.receiveDamage(calc.finalDamage, calc.damageType, false);
        caster.addDamageDealt(result.damage);

        if (calc.isCritical) {
            events.add(CombatEvent.critDamage(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), result.damage, calc.damageType));
        } else if (calc.isBlocked) {
            events.add(CombatEvent.blocked(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), result.damage));
        } else if (result.shieldDamage > 0) {
            // Shield menyerap sebagian damage — tampilkan info shield
            events.add(CombatEvent.shieldDamage(caster.getId(), caster.getName(),
                    target.getId(), target.getName(),
                    result.damage, result.shieldDamage, calc.damageType));
        } else {
            events.add(CombatEvent.damage(caster.getId(), caster.getName(),
                    target.getId(), target.getName(), result.damage, calc.damageType));
        }

        // Lifesteal
        double lifestealed = DamageCalculator.applyLifesteal(caster, result.damage);
        if (lifestealed > 0) {
            events.add(CombatEvent.heal(caster.getId(), caster.getName(),
                    caster.getId(), caster.getName(), lifestealed));
        }

        // Death check
        if (!target.isAlive()) {
            events.add(CombatEvent.death(target.getId(), target.getName()));
        }
    }
    // ════════════════════════════════════════════════════════
    //  GUILDMATE SKILL IMPLEMENTATIONS
    // ════════════════════════════════════════════════════════

    /** BUFF ke seluruh tim */
    private static void buffTeam(Entity caster, List<Entity> targets, List<Entity> allAllies,
                                  arclightcity.entity.status.StatusEffectType buffType,
                                  int duration, List<CombatEvent> events, String msg) {
        for (Entity ally : allAllies) {
            if (!ally.isAlive()) continue;
            ally.applyEffect(new arclightcity.entity.status.StatusEffect(buffType, duration, 0, caster.getId()));
        }
        events.add(new CombatEvent.Builder(CombatEvent.EventType.SKILL_USED)
            .actor(caster.getId(), caster.getName())
            .message(caster.getName() + ": " + msg).build());
    }

    /** NEON_BLOOM: heal semua ally + weak damage ke musuh */
    private static void neonBloom(Entity caster, List<Entity> allAllies, List<Entity> allEnemies,
                                   List<CombatEvent> events) {
        double healAmt = 35 + caster.getStats().get(arclightcity.entity.stats.StatType.ENERGY_ATK) * 0.35;
        for (Entity ally : allAllies) {
            if (!ally.isAlive()) continue;
            double maxHp = ally.getStats().get(arclightcity.entity.stats.StatType.MAX_HP);
            // Jika HP sudah >95%, alihkan ke shield bukan heal
            if (ally.getCurrentHp() >= maxHp * 0.95) {
                ally.applyEffect(new arclightcity.entity.status.StatusEffect(
                    arclightcity.entity.status.StatusEffectType.BARRIER, 6, healAmt, caster.getId()));
                events.add(new CombatEvent.Builder(CombatEvent.EventType.SKILL_USED)
                    .actor(caster.getId(), caster.getName())
                    .message("Mekar Neon: HP penuh, Shield +" + (int)healAmt).build());
            } else {
                double actual = ally.receiveHeal(healAmt);
                events.add(new CombatEvent.Builder(CombatEvent.EventType.HEAL_RECEIVED)
                    .actor(caster.getId(), caster.getName())
                    .target(ally.getId(), ally.getName())
                    .value(actual).message("Mekar Neon +" + (int)actual + " HP").build());
            }
            ally.applyEffect(new arclightcity.entity.status.StatusEffect(
                arclightcity.entity.status.StatusEffectType.REGEN, 9, 10, caster.getId()));
        }
    }

    /** BARRIER ke target */
    private static void barrierHeal(Entity caster, List<Entity> targets, List<CombatEvent> events) {
        double shieldAmt = 40 + caster.getStats().get(arclightcity.entity.stats.StatType.ENERGY_ATK) * 0.4;
        for (Entity t : targets) {
            if (!t.isAlive()) continue;
            t.applyEffect(new arclightcity.entity.status.StatusEffect(
                arclightcity.entity.status.StatusEffectType.BARRIER, 3, shieldAmt, caster.getId()));
            events.add(new CombatEvent.Builder(CombatEvent.EventType.SKILL_USED)
                .actor(caster.getId(), caster.getName())
                .message("Barrier +" + (int)shieldAmt + " ke " + t.getName()).build());
        }
    }

    /** TRIAGE_HEAL / BLOOM_MEND: heal target */
    private static void triageheal(Entity caster, List<Entity> targets, List<CombatEvent> events) {
        double healAmt = 50 + caster.getStats().get(arclightcity.entity.stats.StatType.ENERGY_ATK) * 0.5;
        for (Entity t : targets) {
            if (!t.isAlive()) continue;
            double actual = t.receiveHeal(healAmt);
            events.add(new CombatEvent.Builder(CombatEvent.EventType.HEAL_RECEIVED)
                .actor(caster.getId(), caster.getName())
                .target(t.getId(), t.getName())
                .value(actual).message("Penyembuhan +" + (int)actual + " HP").build());
        }
    }

    /** TAUNT: paksa semua musuh serang caster */
    private static void taunt(Entity caster, List<Entity> allies, List<Entity> enemies, List<CombatEvent> events) {
        caster.applyEffect(new arclightcity.entity.status.StatusEffect(
            arclightcity.entity.status.StatusEffectType.TAUNT, 2, 0, caster.getId()));
        events.add(new CombatEvent.Builder(CombatEvent.EventType.SKILL_USED)
            .actor(caster.getId(), caster.getName())
            .message(caster.getName() + " memprovokasi semua musuh!").build());
    }

    /** STUN target */
    private static void stunTarget(Entity caster, List<Entity> targets, List<CombatEvent> events, int dur) {
        for (Entity t : targets) {
            t.applyEffect(new arclightcity.entity.status.StatusEffect(
                arclightcity.entity.status.StatusEffectType.STUN, dur, 0, caster.getId()));
        }
        events.add(new CombatEvent.Builder(CombatEvent.EventType.SKILL_USED)
            .actor(caster.getId(), caster.getName())
            .message(caster.getName() + " STUN target!").build());
    }

    /** EXPOSE target: reduce DEF */
    private static void exposeTarget(Entity caster, List<Entity> targets, List<CombatEvent> events) {
        for (Entity t : targets) {
            t.applyEffect(new arclightcity.entity.status.StatusEffect(
                arclightcity.entity.status.StatusEffectType.EXPOSE, 2, 0, caster.getId()));
        }
        events.add(new CombatEvent.Builder(CombatEvent.EventType.EFFECT_APPLIED)
            .actor(caster.getId(), caster.getName())
            .message("EXPOSE: pertahanan musuh terbuka!").build());
    }

    /** SLOW target */
    private static void slowTarget(Entity caster, List<Entity> targets, List<CombatEvent> events) {
        for (Entity t : targets) {
            t.applyEffect(new arclightcity.entity.status.StatusEffect(
                arclightcity.entity.status.StatusEffectType.SLOW, 2, 0.3, caster.getId()));
        }
        events.add(new CombatEvent.Builder(CombatEvent.EventType.EFFECT_APPLIED)
            .actor(caster.getId(), caster.getName())
            .message("SLOW: musuh melambat!").build());
    }

    /** FREEZE target */
    private static void freezeTarget(Entity caster, List<Entity> targets, List<CombatEvent> events, int dur) {
        for (Entity t : targets) {
            t.applyEffect(new arclightcity.entity.status.StatusEffect(
                arclightcity.entity.status.StatusEffectType.FREEZE, dur, 0, caster.getId()));
        }
        events.add(new CombatEvent.Builder(CombatEvent.EventType.SKILL_USED)
            .actor(caster.getId(), caster.getName())
            .message("FREEZE: musuh terkunci!").build());
    }

    /** SIGNAL_JAM: slow all */
    private static void slowAll(Entity caster, List<Entity> targets, List<CombatEvent> events) {
        for (Entity t : targets) {
            if (!t.isAlive()) continue;
            t.applyEffect(new arclightcity.entity.status.StatusEffect(
                arclightcity.entity.status.StatusEffectType.SLOW, 2, 0.3, caster.getId()));
        }
        events.add(new CombatEvent.Builder(CombatEvent.EventType.SKILL_USED)
            .actor(caster.getId(), caster.getName())
            .message("Signal Jam: semua musuh melambat!").build());
    }

    /** WEAKEN target: reduce ATK */
    private static void weakenTarget(Entity caster, List<Entity> targets, List<CombatEvent> events) {
        for (Entity t : targets) {
            t.applyEffect(new arclightcity.entity.status.StatusEffect(
                arclightcity.entity.status.StatusEffectType.WEAKEN, 2, 0.25, caster.getId()));
        }
        events.add(new CombatEvent.Builder(CombatEvent.EventType.EFFECT_APPLIED)
            .actor(caster.getId(), caster.getName())
            .message("WEAKEN: kekuatan musuh berkurang!").build());
    }

    /** STEALTH_STEP: buff stealth lalu serang */
    private static void stealthAttack(Entity caster, List<Entity> targets, List<CombatEvent> events) {
        caster.applyEffect(new arclightcity.entity.status.StatusEffect(
            arclightcity.entity.status.StatusEffectType.STEALTH, 2, 0, caster.getId()));
        // Deal damage bonus
        for (Entity t : targets) {
            if (!t.isAlive()) continue;
            double base = caster.getStats().get(arclightcity.entity.stats.StatType.PHYSICAL_ATK) * 1.5;
            double dmg = DamageCalculator.calculate(caster, t, base, DamageType.PHYSICAL, 0).finalDamage;
            t.receiveDamage(dmg, DamageType.PHYSICAL, false);
            events.add(new CombatEvent.Builder(CombatEvent.EventType.DAMAGE_DEALT)
                .actor(caster.getId(), caster.getName())
                .target(t.getId(), t.getName())
                .value(dmg).message("Langkah Bayangan: " + (int)dmg + " damage").build());
        }
    }

    /** BLEED_SLASH: serangan + aplikasi BLEED */
    private static void bleedAttack(Entity caster, List<Entity> targets, List<CombatEvent> events) {
        for (Entity t : targets) {
            if (!t.isAlive()) continue;
            double base = caster.getStats().get(arclightcity.entity.stats.StatType.PHYSICAL_ATK) * 1.0;
            double dmg = DamageCalculator.calculate(caster, t, base, DamageType.PHYSICAL, 0).finalDamage;
            t.receiveDamage(dmg, DamageType.PHYSICAL, false);
            t.applyEffect(new arclightcity.entity.status.StatusEffect(
                arclightcity.entity.status.StatusEffectType.BLEED, 3, 6, caster.getId()));
            events.add(new CombatEvent.Builder(CombatEvent.EventType.DAMAGE_DEALT)
                .actor(caster.getId(), caster.getName())
                .target(t.getId(), t.getName())
                .value(dmg).message("Tebasan Darah: " + (int)dmg + " + BLEED").build());
        }
    }


}
