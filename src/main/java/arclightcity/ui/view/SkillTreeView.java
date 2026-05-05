package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.entity.player.Player;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import java.util.*;

/**
 * SkillTreeView — Pohon skill Asuna bergaya network/constellation.
 *
 * Struktur tree (3 cabang dari root):
 *
 *   CABANG KIRI — Serangan:
 *     ROOT: Pukulan Harimau → Tebasan Pamungkas → Gempa Bumi → Serangan Topan
 *
 *   CABANG TENGAH — Mobilitas:
 *     ROOT: Panah Bayangan → Langkah Gaib → Bayangan Ganda → Ribuan Sayap
 *
 *   CABANG KANAN — Pertahanan/Support:
 *     ROOT: Tameng Baja → Rajah Pelindung → Serap Tenaga → Santet Balik
 *
 * Syarat unlock: harus buka skill sebelumnya di cabang + level minimum.
 */
public class SkillTreeView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private final Player      player;

    // Definisi semua node skill
    record SkillNode(
        String id,          // ID unik
        String name,        // Nama tampilan
        String description, // Deskripsi
        String branch,      // "ATTACK" | "MOBILITY" | "DEFENSE"
        int    depth,       // 0=root, 1,2,3=deeper
        int    levelReq,    // Level minimum
        String requiresId,  // ID skill yang harus dibuka dulu (null=tidak perlu)
        int    spCost       // Skill point cost
    ) {}

    private static final List<SkillNode> ALL_SKILLS = List.of(
        // ── ATTACK BRANCH ──────────────────────────────────────────
        new SkillNode("POWER_STRIKE",   "Pukulan Harimau",
            "Serangan keras: 1.8× ATK fisik.",
            "ATTACK", 0, 1, null, 1),
        new SkillNode("TEBASAN",        "Tebasan Pamungkas",
            "Instant kill jika HP < 25%. Else: 1.5× damage.",
            "ATTACK", 1, 5, "POWER_STRIKE", 2),
        new SkillNode("SHOCKWAVE",      "Gempa Bumi",
            "AoE fisik ke semua musuh sekaligus.",
            "ATTACK", 2, 10, "TEBASAN", 2),
        new SkillNode("SOVEREIGN_STRIKE","Tebasan Agung",
            "Ultimate slash: 3× ATK, abaikan semua armor.",
            "ATTACK", 3, 18, "SHOCKWAVE", 3),

        // ── MOBILITY BRANCH ────────────────────────────────────────
        new SkillNode("PHANTOM_SHOT",   "Panah Bayangan",
            "Serangan cepat: crit chance tinggi, masuk sembunyi.",
            "MOBILITY", 0, 1, null, 1),
        new SkillNode("SHADOW_STEP",    "Langkah Gaib",
            "Teleport di belakang musuh: 2× crit, abaikan armor.",
            "MOBILITY", 1, 5, "PHANTOM_SHOT", 2),
        new SkillNode("NULL_FIELD",     "Bidang Bayangan",
            "Hapus semua buff musuh + evasion +30% 2 giliran.",
            "MOBILITY", 2, 12, "SHADOW_STEP", 2),
        new SkillNode("NULL_PROTOCOL",  "Protokol Nol",
            "AoE Void: semua musuh -50% DEF selama 3 giliran.",
            "MOBILITY", 3, 20, "NULL_FIELD", 3),

        // ── DEFENSE BRANCH ─────────────────────────────────────────
        new SkillNode("IRON_SHIELD",    "Tameng Baja",
            "Buff BLOK 2 giliran, kurangi damage drastis.",
            "DEFENSE", 0, 1, null, 1),
        new SkillNode("FIELD_BARRIER",  "Rajah Pelindung",
            "Shield barrier ke semua sekutu.",
            "DEFENSE", 1, 5, "IRON_SHIELD", 2),
        new SkillNode("ENERGY_DRAIN",   "Serap Tenaga",
            "Sedot MP musuh, pulihkan HP + MP sendiri.",
            "DEFENSE", 2, 10, "FIELD_BARRIER", 2),
        new SkillNode("DATA_FRAGMENTATION","Pecah Jiwa",
            "Pecahkan energi musuh: AoE besar + stun 1 giliran.",
            "DEFENSE", 3, 18, "ENERGY_DRAIN", 3)
    );

    private static final Map<String, String> BRANCH_COLORS = Map.of(
        "ATTACK",   "#CC3300",  // merah api
        "MOBILITY", "#C8860A",  // gold
        "DEFENSE",  "#7755BB"   // amethyst
    );

    public SkillTreeView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
        this.player = engine.getPlayer();
    }

    public Parent build() {
        BorderPane root = UIFactory.screenRootBorder();

        // ── Header ────────────────────────────────────────
        HBox header = new HBox(10);
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #0F0A06;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Button back = new Button("← PROFIL");
        back.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                      "-fx-border-width: 1; -fx-text-fill: #5A3A10;" +
                      "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                      "-fx-padding: 3 8; -fx-cursor: hand;");
        back.setOnAction(e -> router.showProfile("JURUS"));

        Label title = new Label("✦  POHON JURUS ASUNA");
        title.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 14px; -fx-font-weight: bold;" +
                       "-fx-effect: dropshadow(gaussian, #C8860A, 6, 0.3, 0, 0);");
        HBox.setHgrow(title, Priority.ALWAYS);

        int sp = player.getSkillPoints();
        Label spLabel = new Label("✦ " + sp + " SP tersedia");
        spLabel.setStyle("-fx-text-fill: " + (sp > 0 ? "#FFB830" : "#3A2810") +
                         "; -fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
                         "-fx-font-weight: bold;");

        Label lvLabel = new Label("LV." + player.getLevel());
        lvLabel.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        header.getChildren().addAll(back, title, lvLabel, spLabel);
        root.setTop(header);

        // ── Tree content ──────────────────────────────────
        VBox treeArea = new VBox(0);
        treeArea.setStyle("-fx-background-color: #0A0604;");

        // Branch labels
        HBox branchLabels = buildBranchLabels();
        treeArea.getChildren().add(branchLabels);

        // Skill nodes per depth (3 cabang × 4 depth)
        for (int depth = 3; depth >= 0; depth--) {
            HBox row = buildDepthRow(depth);
            treeArea.getChildren().add(row);
            if (depth > 0) {
                treeArea.getChildren().add(buildConnectorRow(depth));
            }
        }

        // SP hint
        Label hint = new Label(sp > 0
            ? "Klik node untuk membuka jurus. Jurus lebih dalam lebih kuat!"
            : "Selesaikan dungeon untuk mendapat Skill Point.");
        hint.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 10px; -fx-padding: 8 16;");
        treeArea.getChildren().add(hint);

        ScrollPane scroll = new ScrollPane(treeArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        UIFactory.fadeIn(root, 400);
        return root;
    }

    private HBox buildBranchLabels() {
        HBox row = new HBox(0);
        row.setPadding(new Insets(12, 0, 8, 0));

        String[][] branches = {
            {"ATTACK",   "⚔  SERANGAN",  "#CC3300"},
            {"MOBILITY", "💨  MOBILITAS", "#C8860A"},
            {"DEFENSE",  "🛡  PERTAHANAN","#7755BB"},
        };
        for (String[] b : branches) {
            Label lbl = new Label(b[1]);
            lbl.setStyle("-fx-text-fill: " + b[2] + "; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 11px; -fx-font-weight: bold;" +
                         "-fx-alignment: CENTER;");
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setAlignment(Pos.CENTER);
            HBox.setHgrow(lbl, Priority.ALWAYS);
            row.getChildren().add(lbl);
        }
        return row;
    }

    private HBox buildDepthRow(int depth) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(4, 16, 4, 16));

        String[] branches = {"ATTACK", "MOBILITY", "DEFENSE"};
        for (String branch : branches) {
            ALL_SKILLS.stream()
                .filter(s -> s.branch().equals(branch) && s.depth() == depth)
                .findFirst()
                .ifPresentOrElse(
                    skill -> {
                        VBox node = buildSkillNode(skill);
                        HBox.setHgrow(node, Priority.ALWAYS);
                        row.getChildren().add(node);
                    },
                    () -> {
                        Region gap = new Region();
                        HBox.setHgrow(gap, Priority.ALWAYS);
                        row.getChildren().add(gap);
                    }
                );
        }
        return row;
    }

    private HBox buildConnectorRow(int depth) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(0, 16, 0, 16));

        String[] branches = {"ATTACK", "MOBILITY", "DEFENSE"};
        for (String branch : branches) {
            String color = BRANCH_COLORS.get(branch);
            boolean parentUnlocked = ALL_SKILLS.stream()
                .filter(s -> s.branch().equals(branch) && s.depth() == depth - 1)
                .findFirst()
                .map(s -> player.hasUnlockedSkill(s.id()))
                .orElse(false);

            Label connector = new Label("│");
            connector.setStyle("-fx-text-fill: " + (parentUnlocked ? color : "#2A1808") +
                               "; -fx-font-size: 16px; -fx-alignment: CENTER;");
            connector.setMaxWidth(Double.MAX_VALUE);
            connector.setAlignment(Pos.CENTER);
            HBox.setHgrow(connector, Priority.ALWAYS);
            row.getChildren().add(connector);
        }
        return row;
    }

    private VBox buildSkillNode(SkillNode skill) {
        String color   = BRANCH_COLORS.get(skill.branch());
        boolean unlocked  = player.hasUnlockedSkill(skill.id());
        boolean equipped  = player.isSkillEquipped(skill.id());
        boolean canUnlock = canUnlock(skill);
        boolean hasLock   = !unlocked && !canUnlock;

        VBox node = new VBox(4);
        node.setAlignment(Pos.CENTER);
        node.setPadding(new Insets(8, 6, 8, 6));
        node.setCursor(unlocked || canUnlock
            ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);

        // Circle icon
        StackPane circle = new StackPane();
        Circle bg = new Circle(26);
        bg.setFill(Color.web(unlocked ? color + "33" : "#1A1008"));
        bg.setStroke(Color.web(unlocked ? color : (canUnlock ? color + "88" : "#2A1808")));
        bg.setStrokeWidth(unlocked ? 2.5 : 1.5);

        if (unlocked) {
            // Pulse animation untuk skill yang sudah dibuka
            ScaleTransition st = new ScaleTransition(Duration.millis(1500), bg);
            st.setFromX(1.0); st.setToX(1.05);
            st.setFromY(1.0); st.setToY(1.05);
            st.setAutoReverse(true);
            st.setCycleCount(Animation.INDEFINITE);
            st.play();
        }

        String iconText = hasLock ? "🔒" :
                skill.depth() == 3 ? "⚡" :
                skill.depth() == 2 ? "✦" :
                skill.depth() == 1 ? "◈" : "◆";
        Label icon = new Label(iconText);
        icon.setStyle("-fx-font-size: " + (unlocked ? "18" : "14") + "px;" +
                      "-fx-text-fill: " + (unlocked ? color : (canUnlock ? color : "#3A2810")) + ";");

        // Badge equipped
        if (equipped) {
            Circle equippedDot = new Circle(5, Color.web(color));
            StackPane.setAlignment(equippedDot, Pos.TOP_RIGHT);
            circle.getChildren().addAll(bg, icon, equippedDot);
        } else {
            circle.getChildren().addAll(bg, icon);
        }

        // Skill name
        Label name = new Label(skill.name());
        name.setWrapText(true);
        name.setMaxWidth(100);
        name.setAlignment(Pos.CENTER);
        name.setStyle(
            "-fx-text-fill: " + (unlocked ? color : (canUnlock ? "#A09070" : "#3A2810")) + ";" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
            (unlocked ? "-fx-font-weight: bold;" : "")
        );

        // Cost / level req label
        String subText = unlocked
            ? (equipped ? "✓ AKTIF" : "TERBUKA")
            : canUnlock
                ? skill.spCost() + " SP"
                : "LV." + skill.levelReq();
        Label sub = new Label(subText);
        sub.setStyle("-fx-text-fill: " + (unlocked ? (equipped ? color : "#2D7A45")
                                                    : (canUnlock ? "#FFB830" : "#2A1808")) +
                     "; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");

        node.getChildren().addAll(circle, name, sub);

        // Hover tooltip
        Tooltip tip = new Tooltip(skill.name() + "\n\n" + skill.description() +
            "\n\nLevel min: " + skill.levelReq() +
            "\nBiaya: " + skill.spCost() + " SP" +
            (skill.requiresId() != null ? "\nButuh: " +
                ALL_SKILLS.stream().filter(s -> s.id().equals(skill.requiresId()))
                    .findFirst().map(SkillNode::name).orElse("?") : ""));
        tip.setStyle("-fx-background-color: #1A1008; -fx-border-color: " + color +
                     "; -fx-text-fill: #EDE0C8; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        Tooltip.install(node, tip);

        // Click action
        node.setOnMouseClicked(e -> {
            if (unlocked) {
                // Toggle equip/unequip
                if (equipped) player.unequipSkill(skill.id());
                else if (player.getEquippedSkillCount() < 4) player.equipSkill(skill.id());
                router.showSkillTree();
            } else if (canUnlock) {
                // Unlock
                if (player.spendSkillPoint(skill.spCost())) {
                    player.forceUnlockSkill(skill.id());
                    router.addSystemChat("✦ Jurus " + skill.name() + " dibuka!");
                    router.showSkillTree();
                }
            }
        });

        // Hover effect
        node.setOnMouseEntered(ev -> {
            if (!unlocked && canUnlock)
                bg.setStroke(Color.web(color));
        });
        node.setOnMouseExited(ev -> {
            if (!unlocked && canUnlock)
                bg.setStroke(Color.web(color + "88"));
        });

        return node;
    }

    private boolean canUnlock(SkillNode skill) {
        if (player.hasUnlockedSkill(skill.id())) return false;
        if (player.getLevel() < skill.levelReq()) return false;
        if (player.getSkillPoints() < skill.spCost()) return false;
        if (skill.requiresId() != null && !player.hasUnlockedSkill(skill.requiresId())) return false;
        return true;
    }
}
