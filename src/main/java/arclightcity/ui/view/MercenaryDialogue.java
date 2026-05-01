package arclightcity.ui.view;

import arclightcity.entity.mercenary.MercenaryType;

import java.util.*;

/**
 * MercenaryDialogue — database dialog per mercenary dan trigger.
 *
 * Setiap merc punya kepribadian unik yang tercermin dalam dialog:
 *   KIRA_VOSS    — dingin, profesional, sedikit kata
 *   TANK_RX9     — formal, android, logis
 *   SERA_MEND    — hangat, caring, medis
 *   VECTOR       — sarkastis, arogan, overconfident
 *   MAGNUS_FORGE — kasar, blak-blakan, antusias
 *   ECHO_NULL    — misterius, teknis, cryptic
 *   LYRA_BLOOM   — positif, spiritual, poetic
 */
public class MercenaryDialogue {

    public enum Trigger {
        // Hub
        HUB_IDLE,
        HUB_ENTER_DUNGEON,

        // Dungeon
        DUNGEON_ENTER_FLOOR,
        DUNGEON_ENTER_ENEMY,
        DUNGEON_ENTER_BOSS,
        DUNGEON_ENTER_LOOT,
        DUNGEON_ENTER_REST,
        DUNGEON_ENTER_SHOP,
        DUNGEON_ENTER_TRAP,
        DUNGEON_BOSS_CLEARED,

        // Combat
        COMBAT_START,
        COMBAT_PLAYER_LOW_HP,
        COMBAT_MERC_ACTION,
        COMBAT_ENEMY_DIES,
        COMBAT_VICTORY,
        COMBAT_DEFEAT,
        COMBAT_BOSS_START,
    }

    private static final Random RNG = new Random();

    // ── Dialog Database ───────────────────────────────────────

    private static final Map<MercenaryType, Map<Trigger, List<String>>> DIALOGUES = new HashMap<>();

    static {
        // ── SRIKANDI — Pemanah Bayangan ────────────────────────────
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.HUB_IDLE,
            "...",
            "Scanning perimeter.",
            "Keep your guard up.",
            "I've seen runners get comfortable. They don't last.",
            "Every second idle is a second wasted."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.HUB_ENTER_DUNGEON,
            "Finally.",
            "Moving out.",
            "Stay behind me.",
            "Target zone confirmed."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.DUNGEON_ENTER_ENEMY,
            "Contact.",
            "Target acquired.",
            "Hostiles inbound.",
            "I'll take point."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.DUNGEON_ENTER_BOSS,
            "High value target. Don't get in my line of fire.",
            "This one's different. Stay sharp.",
            "I've seen what these things do. Focus."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.DUNGEON_ENTER_LOOT,
            "Check for traps first.",
            "Don't touch anything I haven't cleared.",
            "Loot fast. We're exposed."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.DUNGEON_ENTER_REST,
            "Two minutes. Not more.",
            "Rest. I'll keep watch.",
            "Don't get comfortable."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_START,
            "Initiating.",
            "Engaging.",
            "On my mark.",
            "Suppressing fire."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_PLAYER_LOW_HP,
            "You're bleeding. Pull back.",
            "Stay alive. I can't carry a corpse.",
            "Don't die yet. We're not done."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_ENEMY_DIES,
            "Target down.",
            "Neutralized.",
            "Clean.",
            "Next."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_VICTORY,
            "Area clear.",
            "Efficient.",
            "That's how it's done."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_DEFEAT,
            "Fall back. Now.",
            "Tactical withdrawal.",
            "We underestimated them."
        );
        addDialogues(MercenaryType.KIRA_VOSS, Trigger.COMBAT_BOSS_START,
            "Full suppression. Go.",
            "Don't give it a chance to breathe.",
            "Priority target. Everything we have."
        );

        // ── GATOT KACA — Ksatria Baja ───────────────────────────
        addDialogues(MercenaryType.TANK_RX9, Trigger.HUB_IDLE,
            "Systems nominal. Standing by.",
            "Threat assessment: 0 hostiles detected.",
            "Power cells at 98%. Ready for deployment.",
            "Maintenance protocols complete.",
            "Calculating optimal combat parameters."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.HUB_ENTER_DUNGEON,
            "Deploying combat subroutines.",
            "Threat anticipation: elevated. Preparing.",
            "Formation alpha. I lead.",
            "Structural integrity: 100%. Deploying."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.DUNGEON_ENTER_ENEMY,
            "3 hostiles detected. Classification: standard.",
            "Threat level: moderate. Engaging protocols.",
            "Combat formation: defensive. Protecting asset."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.DUNGEON_ENTER_BOSS,
            "WARNING: Anomalous threat signature detected.",
            "Reclassifying threat level: CRITICAL.",
            "Calculating 847 combat scenarios. Proceeding with highest success rate."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.DUNGEON_ENTER_REST,
            "Running diagnostics.",
            "Recharging energy cells. Efficiency: optimal.",
            "Structural repairs initiated."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.COMBAT_START,
            "Combat mode: ACTIVE.",
            "Initiating combat subroutines.",
            "Taunt protocols: online. Drawing fire."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.COMBAT_PLAYER_LOW_HP,
            "WARNING: Asset HP critical. Redirecting damage.",
            "Activating shield matrix. Covering asset.",
            "I will absorb the damage. Stay back."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.COMBAT_ENEMY_DIES,
            "Target eliminated.",
            "Threat neutralized. Next target.",
            "Updating threat registry."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.COMBAT_VICTORY,
            "All threats eliminated. Mission success.",
            "Combat efficiency: 94.7%.",
            "Returning to standby."
        );
        addDialogues(MercenaryType.TANK_RX9, Trigger.COMBAT_BOSS_START,
            "Maximum power to shields. Standing firm.",
            "This unit will not yield.",
            "Structural integrity: holding. Engaging at full capacity."
        );

        // ── NYAI RORO — Tabib Mistis ─────────────────────────────
        addDialogues(MercenaryType.SERA_MEND, Trigger.HUB_IDLE,
            "Let me know if you need a checkup.",
            "I restocked the med-kits. We're ready.",
            "How are you feeling? Don't push yourself too hard.",
            "I worry about everyone here sometimes.",
            "Remember to breathe. All of you."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.HUB_ENTER_DUNGEON,
            "I'll keep everyone alive. That's a promise.",
            "Stay close. I can't heal what I can't reach.",
            "Med-bay is open. Let's go."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.DUNGEON_ENTER_ENEMY,
            "Be careful. I'll handle the healing.",
            "Don't do anything reckless!",
            "I'm watching everyone's vitals."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.DUNGEON_ENTER_BOSS,
            "I have a bad feeling about this. Stay close.",
            "Whatever happens, I won't stop healing.",
            "...everyone come back alive. Please."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.DUNGEON_ENTER_REST,
            "Oh good, a rest point! Let me check everyone.",
            "Sit down. I'm running diagnostics.",
            "This is exactly what we needed."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.COMBAT_START,
            "Triage mode on. I've got everyone.",
            "Don't be a hero. Let me heal.",
            "I'm right here. Don't panic."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.COMBAT_PLAYER_LOW_HP,
            "Hold on! Applying emergency stasis!",
            "You're hurt bad — incoming heal!",
            "Stay with me! Don't you dare give up!"
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.COMBAT_ENEMY_DIES,
            "One less to worry about.",
            "Good, now let me focus on keeping everyone alive.",
            "Focus. There might be more."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.COMBAT_VICTORY,
            "Everyone okay? Let me check.",
            "We made it. I'm so relieved.",
            "Running post-combat vitals. Good news: alive."
        );
        addDialogues(MercenaryType.SERA_MEND, Trigger.COMBAT_DEFEAT,
            "No no no — everyone fall back!",
            "Retreat! I'll cover you!",
            "We can't win this one. Move!"
        );

        // ── RANGGA — Pembunuh Bayaran ─────────────────────────────
        addDialogues(MercenaryType.VECTOR, Trigger.HUB_IDLE,
            "Still here. Unfortunately.",
            "Don't look at me like that.",
            "The pay better be worth this.",
            "I've worked with worse. Not many.",
            "I'm bored. That's dangerous for everyone."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.HUB_ENTER_DUNGEON,
            "About time. I was going insane.",
            "Try to keep up.",
            "Don't slow me down.",
            "Finally something to kill."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.DUNGEON_ENTER_ENEMY,
            "Already? Good.",
            "I'll take the hard ones. You handle the scraps.",
            "Target practice."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.DUNGEON_ENTER_BOSS,
            "Oh, THAT's what we're dealing with. Interesting.",
            "Finally. A real challenge.",
            "Don't expect me to bail you out. Actually... yeah I will. But don't tell anyone."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.DUNGEON_ENTER_LOOT,
            "Dibs on anything with crit.",
            "If it's useful, I'm taking it.",
            "...not bad."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.COMBAT_START,
            "Let's make this quick.",
            "Engaging. Try not to get in my way.",
            "Hack initiated. They won't know what hit them."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.COMBAT_PLAYER_LOW_HP,
            "Don't die. It would look bad for me.",
            "Get it together! I'm not your babysitter!",
            "...fine. I've got you. Don't make it weird."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.COMBAT_ENEMY_DIES,
            "Too easy.",
            "Pathetic.",
            "One down.",
            "Not even worth commenting on."
        );
        addDialogues(MercenaryType.VECTOR, Trigger.COMBAT_VICTORY,
            "Obviously.",
            "Did you doubt me?",
            "You're welcome, by the way.",
            "I was bored the whole time. Just so you know."
        );

        // ── BIMA — Petarung Agung ──────────────────────────
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.HUB_IDLE,
            "I've been cleaning my cannon. She's beautiful.",
            "Ready to blow something up. Just say the word.",
            "I heard there's a boss on floor 3. Can't wait.",
            "This place needs more explosions.",
            "HAHAHA. Sorry. I just thought of something funny."
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.HUB_ENTER_DUNGEON,
            "YEAH! Let's GO!",
            "Time to make some noise!",
            "Charge cells loaded. Morale: MAXIMUM."
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.DUNGEON_ENTER_ENEMY,
            "I love this part!",
            "Multiple targets? Even better.",
            "CHARGE SHOT READY. Stand back!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.DUNGEON_ENTER_BOSS,
            "OH. OH THAT'S A BIG ONE. HAHA!",
            "NOW we're talking! Full power!",
            "I've been waiting for this all floor!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.DUNGEON_ENTER_LOOT,
            "Ooh, anything go boom?",
            "Digging through loot is my second favorite thing.",
            "Heavy armor or I'm not interested."
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.DUNGEON_ENTER_REST,
            "Finally. My arms were getting tired. The cannon's heavy.",
            "Rest? I don't need rest. ...okay maybe a little.",
            "Refueling! Back to 100% in no time."
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.COMBAT_START,
            "FIRE IN THE HOLE!",
            "MAXIMUM FIREPOWER!",
            "Let the big one go first!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.COMBAT_PLAYER_LOW_HP,
            "Hey! Nobody touches my runner!",
            "GET AWAY FROM THEM! *cannon charging*",
            "Cover! MOVE! I've got this!"
        );
        addDialogues(MercenaryType.MAGNUS_FORGE, Trigger.COMBAT_VICTORY,
            "HAHAHAHA! YES!",
            "THAT'S WHAT I'M TALKING ABOUT!",
            "Easy. What's next?"
        );

        // ── KI AGENG — Dukun Tua ────────────────────────────
        addDialogues(MercenaryType.ECHO_NULL, Trigger.HUB_IDLE,
            "Signal frequencies nominal. Awaiting disruption orders.",
            "I detected 3 surveillance nodes in this building. Already jammed them.",
            "Silence is the loudest signal.",
            "...",
            "Processing background noise. Interesting patterns."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.HUB_ENTER_DUNGEON,
            "Mapping signal topology. Initiating.",
            "Communication channels prepped for interference.",
            "They won't be calling for help."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.DUNGEON_ENTER_ENEMY,
            "Detecting communication bursts. Suppressing.",
            "They've noticed us. Limiting their coordination.",
            "Signal jam: active. They're deaf now."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.DUNGEON_ENTER_BOSS,
            "Anomalous signal source detected. High complexity.",
            "This one's shielded. EMP will take time.",
            "Interesting. I've never seen this frequency before."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.COMBAT_START,
            "Frequency lock: initiated.",
            "Disruption field: active.",
            "EMP queued. Awaiting optimal window."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.COMBAT_PLAYER_LOW_HP,
            "Asset integrity compromised. Rerouting threat signals.",
            "Jamming their targeting systems. Buy yourself time.",
            "Disrupting enemy coordination. Use the window."
        );
        addDialogues(MercenaryType.ECHO_NULL, Trigger.COMBAT_VICTORY,
            "Signal environment clear.",
            "Threat frequency: nullified.",
            "The static has settled."
        );

        // ── DEWI SRI — Penjaga Keseimbangan ─────────────────────────────
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.HUB_IDLE,
            "The neon flows through all things. Even this dusty hub.",
            "I feel good energy today. Today will be significant.",
            "Rest while you can. The city never does.",
            "I brewed something. It's not poisonous. Probably.",
            "Every light in this city was once darkness. Remember that."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.HUB_ENTER_DUNGEON,
            "The dungeon calls. The energy is restless.",
            "I'll channel the resonance for us. Stay close.",
            "May the neon light guide our path."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.DUNGEON_ENTER_ENEMY,
            "They carry darkness. We carry light. Simple.",
            "Resonance field warming up.",
            "I feel their intent. It's not kind. Preparing."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.DUNGEON_ENTER_BOSS,
            "This one... the void surrounds it. Be careful.",
            "Ancient malice. We'll need everything we have.",
            "Even darkness can be turned to light. Let's prove it."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.DUNGEON_ENTER_REST,
            "The space breathes easier here. Rest, all of you.",
            "I'll perform a cleansing ritual while we rest.",
            "Peace. Brief as it is, savor it."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.DUNGEON_ENTER_LOOT,
            "The neon leaves gifts for those who seek.",
            "Something here calls to me. Interesting.",
            "The city provides."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.COMBAT_START,
            "Resonance: open. Bloom: ready.",
            "I channel the neon for all of us.",
            "Flow with the light. Block the dark."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.COMBAT_PLAYER_LOW_HP,
            "No no no — hold on, channeling now!",
            "The bloom finds you — hold on!",
            "I will not lose you today!"
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.COMBAT_ENEMY_DIES,
            "Return to the void from which you came.",
            "The light prevails. As always.",
            "May you find peace in dissolution."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.COMBAT_VICTORY,
            "The neon sings. We endure.",
            "Harmony restored. For now.",
            "Light always finds a way."
        );
        addDialogues(MercenaryType.LYRA_BLOOM, Trigger.DUNGEON_BOSS_CLEARED,
            "The darkness breaks. The floor is ours.",
            "Even the mightiest void cannot hold forever.",
            "We've earned this. Descend when ready."
        );
    }

    // ── Public API ────────────────────────────────────────────

    /**
     * Ambil dialog random dari mercenary untuk trigger tertentu.
     * Return null jika tidak ada dialog untuk kombinasi ini.
     */
    public static String getDialogue(MercenaryType type, Trigger trigger) {
        Map<Trigger, List<String>> byType = DIALOGUES.get(type);
        if (byType == null) return null;
        List<String> lines = byType.get(trigger);
        if (lines == null || lines.isEmpty()) return null;
        return lines.get(RNG.nextInt(lines.size()));
    }

    /**
     * Ambil dialog dari semua merc aktif untuk trigger tertentu.
     * Hanya 1-2 merc yang akan berbicara (acak, tidak semua sekaligus).
     */
    public static List<ChatMessage> getGroupDialogue(
            List<arclightcity.entity.mercenary.Mercenary> mercs, Trigger trigger) {
        List<ChatMessage> result = new ArrayList<>();
        if (mercs == null || mercs.isEmpty()) return result;

        // Shuffle agar tidak selalu merc pertama yang ngomong duluan
        List<arclightcity.entity.mercenary.Mercenary> shuffled = new ArrayList<>(mercs);
        Collections.shuffle(shuffled, RNG);

        // Ambil dialog dari 1-2 merc (tidak semua ngobrol setiap saat)
        int maxSpeakers = Math.min(shuffled.size(), 1 + RNG.nextInt(2));

        for (int i = 0; i < maxSpeakers; i++) {
            arclightcity.entity.mercenary.Mercenary merc = shuffled.get(i);
            String line = getDialogue(merc.getMercenaryType(), trigger);
            if (line != null) {
                result.add(new ChatMessage(
                        merc.getMercenaryType(),
                        merc.getName(),
                        line
                ));
            }
        }

        return result;
    }

    // ── Helper ────────────────────────────────────────────────

    private static void addDialogues(MercenaryType type, Trigger trigger, String... lines) {
        DIALOGUES.computeIfAbsent(type, k -> new EnumMap<>(Trigger.class))
                 .computeIfAbsent(trigger, k -> new ArrayList<>())
                 .addAll(Arrays.asList(lines));
    }

    // ── ChatMessage record ─────────────────────────────────────

    public record ChatMessage(
            MercenaryType mercType,
            String        mercName,
            String        text
    ) {}
}
