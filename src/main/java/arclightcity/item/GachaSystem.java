package arclightcity.item;

import java.util.*;

/**
 * GachaSystem — sistem gacha artefak.
 *
 * Biaya:
 *   1x pull = 800 gold ATAU 1 Gacha Ticket
 *  10x pull = 7200 gold ATAU 9 tiket (1 gratis)
 *
 * Rarity rates:
 *   COMMON 40% | UNCOMMON 25% | RARE 18% | EPIC 12% | LEGENDARY 4% | MYTHIC 1%
 *
 * Pity: pull ke-80 dijamin LEGENDARY atau lebih tinggi.
 */
public class GachaSystem {

    public static final int COST_SINGLE      = 800;
    public static final int COST_TEN         = 7200;
    public static final int TICKET_COST_1    = 1;
    public static final int TICKET_COST_10   = 9;
    public static final int PITY_THRESHOLD   = 80;

    private static final Random RNG = new Random();

    private int pullCount       = 0; // counter pity (reset setelah dapat LEGENDARY+)
    private int freePullAccum   = 0; // future: accumulated free pull credits

    // ── Single pull ───────────────────────────────────────────
    public PullResult pullSingle(int playerGold, int playerTickets) {
        if (playerTickets >= TICKET_COST_1) {
            Artifact result = generateArtifact(null);
            pullCount++;
            return PullResult.success(List.of(result), 0, TICKET_COST_1);
        } else if (playerGold >= COST_SINGLE) {
            Artifact result = generateArtifact(null);
            pullCount++;
            return PullResult.success(List.of(result), COST_SINGLE, 0);
        }
        return PullResult.notEnough();
    }

    // ── Ten pull ──────────────────────────────────────────────
    public PullResult pullTen(int playerGold, int playerTickets) {
        if (playerTickets >= TICKET_COST_10) {
            return doTenPull(0, TICKET_COST_10);
        } else if (playerGold >= COST_TEN) {
            return doTenPull(COST_TEN, 0);
        }
        return PullResult.notEnough();
    }

    private PullResult doTenPull(int goldCost, int ticketCost) {
        List<Artifact> results = new ArrayList<>();
        // Garansi minimal 1 RARE dari 10 pull
        boolean gotRare = false;
        for (int i = 0; i < 10; i++) {
            pullCount++;
            Item.Rarity forced = null;
            if (i == 9 && !gotRare) forced = Item.Rarity.RARE; // garansi pull ke-10
            Artifact a = generateArtifact(forced);
            if (a.getRarity().ordinal() >= Item.Rarity.RARE.ordinal()) gotRare = true;
            results.add(a);
        }
        return PullResult.success(results, goldCost, ticketCost);
    }

    // ── Generate artifact ─────────────────────────────────────
    private Artifact generateArtifact(Item.Rarity forced) {
        Item.Rarity rarity = (forced != null) ? forced : rollRarity();

        // Pity check: pull ke-80 dijamin LEGENDARY+
        if (pullCount >= PITY_THRESHOLD && rarity.ordinal() < Item.Rarity.LEGENDARY.ordinal()) {
            rarity = Item.Rarity.LEGENDARY;
            pullCount = 0;
        }
        if (rarity.ordinal() >= Item.Rarity.LEGENDARY.ordinal()) {
            pullCount = 0; // reset pity
        }

        // Pilih ArtifactType yang sesuai
        ArtifactType[] all = ArtifactType.values();
        ArtifactType type  = all[RNG.nextInt(all.length)];
        return new Artifact(type, rarity);
    }

    // ── Rarity roll ───────────────────────────────────────────
    private Item.Rarity rollRarity() {
        double roll = RNG.nextDouble() * 100;
        if (roll < 1.0)  return Item.Rarity.MYTHIC;      // 1%
        if (roll < 5.0)  return Item.Rarity.LEGENDARY;   // 4%
        if (roll < 17.0) return Item.Rarity.EPIC;         // 12%
        if (roll < 35.0) return Item.Rarity.RARE;         // 18%
        if (roll < 60.0) return Item.Rarity.UNCOMMON;     // 25%
        return Item.Rarity.COMMON;                         // 40%
    }

    // ── Pity info ─────────────────────────────────────────────
    public int getPullCount()    { return pullCount; }
    public int getPullsToGuarantee() { return Math.max(0, PITY_THRESHOLD - pullCount); }

    // ══ PullResult ════════════════════════════════════════════
    public static class PullResult {
        public final boolean       success;
        public final List<Artifact> artifacts;
        public final int            goldSpent;
        public final int            ticketsSpent;
        public final String         failReason;

        private PullResult(boolean s, List<Artifact> a, int g, int t, String r) {
            success = s; artifacts = a; goldSpent = g; ticketsSpent = t; failReason = r;
        }

        public static PullResult success(List<Artifact> a, int g, int t) {
            return new PullResult(true, a, g, t, null);
        }
        public static PullResult notEnough() {
            return new PullResult(false, List.of(), 0, 0,
                "Gold tidak cukup! Butuh " + COST_SINGLE + " gold atau 1 Tiket Gacha.");
        }

        /** Rarity tertinggi dari hasil pull ini */
        public Item.Rarity getBestRarity() {
            return artifacts.stream()
                .map(Artifact::getRarity)
                .max(Comparator.comparingInt(Item.Rarity::ordinal))
                .orElse(Item.Rarity.COMMON);
        }
    }
}
