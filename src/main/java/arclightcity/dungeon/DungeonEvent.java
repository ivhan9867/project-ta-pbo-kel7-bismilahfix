package arclightcity.dungeon;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;
import arclightcity.entity.stats.DamageType;
import arclightcity.entity.stats.StatType;

import arclightcity.entity.player.Player;

import java.util.Random;

/**
 * DungeonEvent — event yang terjadi di EVENT room atau TRAP room.
 *
 * Setiap event punya:
 *   - Deskripsi narasi
 *   - Satu atau lebih pilihan (choices)
 *   - Efek berdasarkan pilihan
 *
 * Untuk event yang tidak punya pilihan (TRAP), efek langsung diapply.
 */
public class DungeonEvent {

    public enum EventCategory { POSITIVE, NEGATIVE, NEUTRAL, CHOICE }

    private final String        eventId;
    private final String        title;
    private final String        narrative;
    private final EventCategory category;
    private final EventChoice[] choices; // null jika tidak ada pilihan

    // ── Constructor ─────────────────────────────────────────

    public DungeonEvent(String eventId, String title, String narrative,
                        EventCategory category, EventChoice[] choices) {
        this.eventId   = eventId;
        this.title     = title;
        this.narrative = narrative;
        this.category  = category;
        this.choices   = choices;
    }

    // ── Static Factory ───────────────────────────────────────

    public static DungeonEvent fromId(String eventId) {
        return switch (eventId) {

            // ── POSITIVE EVENTS ──────────────────────────────

            case "EVENT_CALIBRATION" -> new DungeonEvent(
                eventId,
                "🔮 Altar Kalibrasi",
                "Kamu menemukan altar kalibrasi kuno yang masih memancarkan energi. " +
                "Ukirannya asing, tapi mekanismenya masih bisa diakses.",
                EventCategory.POSITIVE,
                new EventChoice[]{
                    new EventChoice("Kalibrasi item terbaik",
                        p -> EventResult.calibration(p, 1)),
                    new EventChoice("Lewati (simpan waktu)",
                        p -> EventResult.nothing())
                }
            );

            case "EVENT_NEON_FOUNTAIN" -> new DungeonEvent(
                eventId,
                "💧 Sumber Mata Air Gaib",
                "Sebuah sumber mata air tersembunyi memancarkan cahaya kebiruan. " +
                "Aromanya harum seperti bunga surga — airnya tampak menyembuhkan.",
                EventCategory.POSITIVE,
                new EventChoice[]{
                    new EventChoice("Minum dari fountain (+30% HP max)",
                        p -> {
                            double heal = p.getStats().get(StatType.MAX_HP) * 0.30;
                            p.receiveHeal(heal);
                            return EventResult.heal((int) heal);
                        }),
                    new EventChoice("Rendam senjata (Weapon +2 level temp)",
                        p -> EventResult.tempBuff("WEAPON_BOOST", 5)),
                    new EventChoice("Abaikan (terlalu aneh)",
                        p -> EventResult.nothing())
                }
            );

            case "EVENT_DATA_CACHE" -> new DungeonEvent(
                eventId,
                "📜 Gulungan Ilmu Kuno",
                "Sebuah modul memori tua terjatuh dari dinding yang retak. " +
                "Masih menyimpan data berharga yang bisa dikonversi jadi pengalaman tempur.",
                EventCategory.POSITIVE,
                new EventChoice[]{
                    new EventChoice("Pelajari gulungan (+EXP besar)",
                        p -> {
                            double exp = 80 + p.getLevel() * 20;
                            p.gainExp(exp);
                            return EventResult.exp((int) exp);
                        }),
                    new EventChoice("Jual ke kolektor (+Gold besar)",
                        p -> {
                            long gold = 100 + p.getLevel() * 30L;
                            p.gainGold(gold);
                            return EventResult.gold(gold);
                        })
                }
            );

            // ── NEGATIVE EVENTS (TRAP) ─────────────────────────

            case "TRAP_ELECTRIC" -> new DungeonEvent(
                eventId,
                "⚡ Jebakan Petir Raksasa",
                "Lantai berubah jadi grid konduktor. Sengatan listrik menghantam " +
                "sebelum kamu bisa bereaksi.",
                EventCategory.NEGATIVE,
                null // tidak ada pilihan — efek langsung
            );

            case "TRAP_CORRODE" -> new DungeonEvent(
                eventId,
                "🌿 Kabut Racun Hutan",
                "Kabut asam menyembur dari ventilasi tersembunyi. " +
                "Armor-mu mulai tergerus oleh zat korosif.",
                EventCategory.NEGATIVE,
                null
            );

            case "TRAP_ALARM" -> new DungeonEvent(
                eventId,
                "🥁 Gong Peringatan",
                "Sensor keamanan aktif. Alarm meraung, dan bala bantuan korporat " +
                "segera dipanggil ke lokasi.",
                EventCategory.NEGATIVE,
                null // trigger enemy encounter tambahan
            );

            case "TRAP_FREEZE" -> new DungeonEvent(
                eventId,
                "🧊 Kutukan Beku",
                "Sistem cryo darurat aktif. Cairan nitrogen cair menyemprot " +
                "dari langit-langit, membekukan gerakanmu sesaat.",
                EventCategory.NEGATIVE,
                null
            );

            case "TRAP_NEON_BURN" -> new DungeonEvent(
                eventId,
                "🔥 Api Banaspati",
                "Pipa energi neon retak dan menyemprot langsung ke arahmu. " +
                "Rasanya seperti dibakar dari dalam.",
                EventCategory.NEGATIVE,
                null
            );

            // ── CHOICE EVENTS ──────────────────────────────────

            case "EVENT_MYSTERY_BOX" -> new DungeonEvent(
                eventId,
                "🎁 Peti Misterius",
                "Sebuah kontainer tersegel tergeletak di tengah ruangan. " +
                "Tidak ada label. Tidak ada tanda. Tapi ada sesuatu di dalamnya.",
                EventCategory.CHOICE,
                new EventChoice[]{
                    new EventChoice("Buka (risiko tinggi, reward tinggi)",
                        p -> {
                            double roll = Math.random();
                            if (roll < 0.40) return EventResult.rareLoot();
                            if (roll < 0.70) return EventResult.gold(150);
                            return EventResult.trap("ELECTRIC"); // bad luck
                        }),
                    new EventChoice("Tendang dari jauh (damage kecil, reward kecil)",
                        p -> {
                            double roll = Math.random();
                            if (roll < 0.60) return EventResult.gold(40);
                            return EventResult.damage(15);
                        }),
                    new EventChoice("Biarkan saja",
                        p -> EventResult.nothing())
                }
            );

            case "EVENT_MERCHANT" -> new DungeonEvent(
                eventId,
                "🧙 Pedagang Gaib",
                "Seorang pedagang misterius duduk di sudut ruangan, " +
                "dikelilingi koper-koper penuh barang dagangan.",
                EventCategory.NEUTRAL,
                new EventChoice[]{
                    new EventChoice("Beli item (buka toko)",
                        p -> EventResult.openShop()),
                    new EventChoice("Tanya informasi (reveal map)",
                        p -> EventResult.revealMap()),
                    new EventChoice("Pergi",
                        p -> EventResult.nothing())
                }
            );

            case "EVENT_CORRUPTED_LOOT" -> new DungeonEvent(
                eventId,
                "🗡️ Harta Terkutuk",
                "Tumpukan loot terlihat bagus dari jauh, tapi dekat-dekat " +
                "kamu mencium bau kutukan yang sudah terlalu familiar.",
                EventCategory.CHOICE,
                new EventChoice[]{
                    new EventChoice("Ambil semua (risiko kena KUTUKAN)",
                        p -> {
                            if (Math.random() < 0.55) {
                                p.applyEffect(new StatusEffect(
                                        StatusEffectType.VIRUS, 5, 10.0, "DUNGEON"));
                                return EventResult.lootWithDebuff("VIRUS");
                            }
                            return EventResult.rareLoot();
                        }),
                    new EventChoice("Ambil yang bersih saja (loot biasa)",
                        p -> EventResult.commonLoot()),
                    new EventChoice("Berdoa dan tinggalkan (aman, tidak dapat apa-apa)",
                        p -> EventResult.nothing())
                }
            );

            default -> new DungeonEvent(
                eventId, "Unknown Event", "Sesuatu terjadi...",
                EventCategory.NEUTRAL,
                new EventChoice[]{ new EventChoice("Lanjut", p -> EventResult.nothing()) }
            );
        };
    }

    // ── Apply Trap (tanpa pilihan) ───────────────────────────

    public EventResult applyTrap(Player player) {
        return switch (eventId) {
            case "TRAP_ELECTRIC" -> {
                double dmg = player.getStats().get(StatType.MAX_HP) * 0.12;
                player.receiveDamage(dmg, DamageType.ENERGY, true);
                yield EventResult.damage((int) dmg);
            }
            case "TRAP_CORRODE" -> {
                player.applyEffect(new StatusEffect(StatusEffectType.CORRODE, 3, 8.0, "DUNGEON"));
                yield EventResult.debuff("CORRODE");
            }
            case "TRAP_ALARM" -> EventResult.spawnEnemy();
            case "TRAP_FREEZE" -> {
                player.applyEffect(new StatusEffect(StatusEffectType.FREEZE, 2, 0, "DUNGEON"));
                yield EventResult.debuff("FREEZE");
            }
            case "TRAP_NEON_BURN" -> {
                double dmg = player.getStats().get(StatType.MAX_HP) * 0.15;
                player.receiveDamage(dmg, DamageType.PHYSICAL, true);
                player.applyEffect(new StatusEffect(StatusEffectType.BURN, 2, 10.0, "DUNGEON"));
                yield EventResult.damageAndDebuff((int) dmg, "BURN");
            }
            default -> EventResult.nothing();
        };
    }

    // ── Getters ─────────────────────────────────────────────

    public String        getEventId()   { return eventId; }
    public String        getTitle()     { return title; }
    public String        getNarrative() { return narrative; }
    public EventCategory getCategory()  { return category; }
    public EventChoice[] getChoices()   { return choices; }
    public boolean       hasChoices()   { return choices != null && choices.length > 0; }

    // ── Inner Classes ────────────────────────────────────────

    @FunctionalInterface
    public interface ChoiceEffect {
        EventResult apply(Player player);
    }

    public static class EventChoice {
        public final String       label;
        public final ChoiceEffect effect;

        public EventChoice(String label, ChoiceEffect effect) {
            this.label  = label;
            this.effect = effect;
        }
    }

    /** Hasil dari sebuah event / pilihan */
    public static class EventResult {
        public enum ResultType {
            NOTHING, HEAL, DAMAGE, EXP, GOLD, LOOT_COMMON, LOOT_RARE,
            DEBUFF, LOOT_WITH_DEBUFF, DAMAGE_AND_DEBUFF,
            TEMP_BUFF, OPEN_SHOP, REVEAL_MAP, SPAWN_ENEMY,
            CALIBRATION, TRAP
        }

        public final ResultType type;
        public final int        intValue;
        public final long       longValue;
        public final String     stringValue;

        private EventResult(ResultType type, int intVal, long longVal, String strVal) {
            this.type        = type;
            this.intValue    = intVal;
            this.longValue   = longVal;
            this.stringValue = strVal;
        }

        public static EventResult nothing()                         { return new EventResult(ResultType.NOTHING, 0, 0, null); }
        public static EventResult heal(int amount)                  { return new EventResult(ResultType.HEAL, amount, 0, null); }
        public static EventResult damage(int amount)                { return new EventResult(ResultType.DAMAGE, amount, 0, null); }
        public static EventResult exp(int amount)                   { return new EventResult(ResultType.EXP, amount, 0, null); }
        public static EventResult gold(long amount)                 { return new EventResult(ResultType.GOLD, 0, amount, null); }
        public static EventResult commonLoot()                      { return new EventResult(ResultType.LOOT_COMMON, 0, 0, null); }
        public static EventResult rareLoot()                        { return new EventResult(ResultType.LOOT_RARE, 0, 0, null); }
        public static EventResult debuff(String effectName)         { return new EventResult(ResultType.DEBUFF, 0, 0, effectName); }
        public static EventResult lootWithDebuff(String debuff)     { return new EventResult(ResultType.LOOT_WITH_DEBUFF, 0, 0, debuff); }
        public static EventResult damageAndDebuff(int dmg, String d){ return new EventResult(ResultType.DAMAGE_AND_DEBUFF, dmg, 0, d); }
        public static EventResult tempBuff(String buff, int turns)  { return new EventResult(ResultType.TEMP_BUFF, turns, 0, buff); }
        public static EventResult openShop()                        { return new EventResult(ResultType.OPEN_SHOP, 0, 0, null); }
        public static EventResult revealMap()                       { return new EventResult(ResultType.REVEAL_MAP, 0, 0, null); }
        public static EventResult spawnEnemy()                      { return new EventResult(ResultType.SPAWN_ENEMY, 0, 0, null); }
        public static EventResult calibration(Player p, int slots)  { return new EventResult(ResultType.CALIBRATION, slots, 0, null); }
        public static EventResult trap(String trapId)               { return new EventResult(ResultType.TRAP, 0, 0, trapId); }
    }
}
