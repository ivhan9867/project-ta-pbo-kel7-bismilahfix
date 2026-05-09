package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.entity.player.Player;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import java.util.*;

/**
 * SkillTreeView — Pohon Jurus Asuna dengan visual constellation.
 * 3 cabang, 4 depth masing-masing. Node terhubung dengan garis.
 */
public class SkillTreeView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private final Player      player;

    record SkillNode(String id, String name, String desc,
                     String branch, int depth, int levelReq,
                     String requiresId, int spCost) {}

    private static final List<SkillNode> ALL = List.of(
        // ATTACK — merah
        new SkillNode("POWER_STRIKE","Pukulan Harimau",
            "1.8× ATK fisik.","ATTACK",0,1,null,1),
        new SkillNode("TEBASAN","Tebasan Pamungkas",
            "Instant kill jika HP<25%. Else 1.5× dmg.","ATTACK",1,5,"POWER_STRIKE",2),
        new SkillNode("SHOCKWAVE","Gempa Bumi",
            "AoE fisik ke semua musuh.","ATTACK",2,10,"TEBASAN",2),
        new SkillNode("SOVEREIGN_STRIKE","Tebasan Agung",
            "Ultimate: 3× ATK, abaikan semua armor.","ATTACK",3,18,"SHOCKWAVE",3),
        // MOBILITY — gold
        new SkillNode("PHANTOM_SHOT","Panah Bayangan",
            "Crit chance tinggi, masuk sembunyi.","MOBILITY",0,1,null,1),
        new SkillNode("SHADOW_STEP","Langkah Gaib",
            "2× crit damage, abaikan armor.","MOBILITY",1,5,"PHANTOM_SHOT",2),
        new SkillNode("NULL_FIELD","Bidang Bayangan",
            "Hapus buff musuh + evasion +30% 2 giliran.","MOBILITY",2,12,"SHADOW_STEP",2),
        new SkillNode("NULL_PROTOCOL","Protokol Nol",
            "AoE Void: semua musuh -50% DEF 3 giliran.","MOBILITY",3,20,"NULL_FIELD",3),
        // DEFENSE — ungu
        new SkillNode("IRON_SHIELD","Tameng Baja",
            "BLOK 2 giliran, kurangi damage drastis.","DEFENSE",0,1,null,1),
        new SkillNode("FIELD_BARRIER","Rajah Pelindung",
            "Shield barrier ke semua sekutu.","DEFENSE",1,5,"IRON_SHIELD",2),
        new SkillNode("ENERGY_DRAIN","Serap Tenaga",
            "Sedot MP musuh, pulihkan HP+MP.","DEFENSE",2,10,"FIELD_BARRIER",2),
        new SkillNode("DATA_FRAGMENTATION","Pecah Jiwa",
            "AoE besar + stun 1 giliran.","DEFENSE",3,18,"ENERGY_DRAIN",3)
    );

    private static final Map<String,String> BRANCH_COLOR = Map.of(
        "ATTACK","#CC3300","MOBILITY","#C8860A","DEFENSE","#7755BB");
    private static final Map<String,String> BRANCH_LABEL = Map.of(
        "ATTACK","⚔  SERANGAN","MOBILITY","💨  MOBILITAS","DEFENSE","🛡  PERTAHANAN");

    public SkillTreeView(GameEngine e, SceneRouter r) {
        engine = e; router = r; player = e.getPlayer();
    }

    public Parent build() {
        BorderPane root = UIFactory.screenRootBorder();

        // Header
        HBox hdr = new HBox(10);
        hdr.setPadding(new Insets(10,16,10,16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-background-color: #0F0A06;" +
                     "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Button back = new Button("← JURUS");
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
        Label spLbl = new Label((sp > 0 ? "✦ " : "○ ") + sp + " SP");
        spLbl.setStyle("-fx-text-fill: " + (sp > 0 ? "#FFB830" : "#3A2810") +
                       "; -fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label lvLbl = new Label("LV." + player.getLevel());
        lvLbl.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        hdr.getChildren().addAll(back, title, lvLbl, spLbl);
        root.setTop(hdr);

        // Tree area
        VBox treeArea = new VBox(0);
        treeArea.setStyle("-fx-background-color: #0A0604;");
        treeArea.setPadding(new Insets(12, 0, 20, 0));

        // Branch header labels
        HBox branchHdr = new HBox(0);
        for (String b : new String[]{"ATTACK","MOBILITY","DEFENSE"}) {
            Label bl = new Label(BRANCH_LABEL.get(b));
            bl.setStyle("-fx-text-fill: " + BRANCH_COLOR.get(b) +
                        "; -fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                        "-fx-font-weight: bold; -fx-alignment: CENTER;");
            bl.setMaxWidth(Double.MAX_VALUE);
            bl.setAlignment(Pos.CENTER);
            HBox.setHgrow(bl, Priority.ALWAYS);
            branchHdr.getChildren().add(bl);
        }
        treeArea.getChildren().add(branchHdr);

        // Render dari depth 3 (paling kuat) ke 0 (root) — visual dari atas ke bawah
        for (int depth = 3; depth >= 0; depth--) {
            treeArea.getChildren().add(buildDepthRow(depth));
            if (depth > 0) {
                treeArea.getChildren().add(buildConnectors(depth));
            }
        }

        // Hint
        Label hint = new Label(sp > 0
            ? "Klik node untuk membuka jurus | klik node terbuka untuk equip/unequip"
            : "Naiki level untuk mendapat Skill Point.");
        hint.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 9px; -fx-padding: 8 0 0 16;");
        treeArea.getChildren().add(hint);

        ScrollPane scroll = new ScrollPane(treeArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        UIFactory.fadeIn(root, 350);
        return root;
    }

    private HBox buildDepthRow(int depth) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(8, 20, 8, 20));

        for (String branch : new String[]{"ATTACK","MOBILITY","DEFENSE"}) {
            VBox cell = new VBox();
            cell.setAlignment(Pos.CENTER);
            HBox.setHgrow(cell, Priority.ALWAYS);

            ALL.stream()
               .filter(s -> s.branch().equals(branch) && s.depth() == depth)
               .findFirst()
               .ifPresent(skill -> cell.getChildren().add(buildNode(skill)));

            row.getChildren().add(cell);
        }
        return row;
    }

    private HBox buildConnectors(int depth) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER);
        row.setMinHeight(20);

        for (String branch : new String[]{"ATTACK","MOBILITY","DEFENSE"}) {
            String color = BRANCH_COLOR.get(branch);
            boolean parentUnlocked = ALL.stream()
                .filter(s -> s.branch().equals(branch) && s.depth() == depth - 1)
                .findFirst()
                .map(s -> player.hasUnlockedSkill(s.id()))
                .orElse(false);

            // Garis konektor vertikal
            VBox connector = new VBox();
            connector.setAlignment(Pos.CENTER);
            connector.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(connector, Priority.ALWAYS);

            Region line = new Region();
            line.setPrefSize(2, 20);
            line.setMaxWidth(2);
            line.setStyle("-fx-background-color: " + 
                (parentUnlocked ? color : "#2A1808") + ";");

            connector.getChildren().add(line);
            row.getChildren().add(connector);
        }
        return row;
    }

    private VBox buildNode(SkillNode skill) {
        String color = BRANCH_COLOR.get(skill.branch());
        boolean unlocked  = player.hasUnlockedSkill(skill.id());
        boolean equipped  = player.isSkillEquipped(skill.id());
        boolean canUnlock = !unlocked
            && player.getLevel() >= skill.levelReq()
            && player.getSkillPoints() >= skill.spCost()
            && (skill.requiresId() == null || player.hasUnlockedSkill(skill.requiresId()));

        // State styles
        String circleFill  = unlocked ? color + "33" : "#0F0A06";
        String circleStroke= unlocked ? color : (canUnlock ? color + "88" : "#2A1808");
        double strokeWidth = unlocked ? 2.5 : 1.5;

        VBox node = new VBox(6);
        node.setAlignment(Pos.CENTER);
        node.setPrefWidth(140);
        node.setMaxWidth(140);
        node.setCursor(unlocked || canUnlock
            ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);

        // Circle dengan icon
        StackPane circleStack = new StackPane();
        Circle bg = new Circle(28);
        bg.setFill(Color.web(circleFill));
        bg.setStroke(Color.web(circleStroke));
        bg.setStrokeWidth(strokeWidth);

        // Depth icon
        String iconTxt = switch (skill.depth()) {
            case 3 -> "⚡";
            case 2 -> "✦";
            case 1 -> "◈";
            default -> "◆";
        };
        if (!unlocked && !canUnlock) iconTxt = "🔒";

        Label icon = new Label(iconTxt);
        icon.setStyle("-fx-font-size: " + (unlocked ? 20 : 16) + "px;");

        // Equip dot
        if (equipped) {
            Circle dot = new Circle(6, Color.web(color));
            StackPane.setAlignment(dot, Pos.TOP_RIGHT);
            StackPane.setMargin(dot, new Insets(0, 2, 0, 0));
            dot.setEffect(new javafx.scene.effect.DropShadow(8, Color.web(color)));
            circleStack.getChildren().addAll(bg, icon, dot);
        } else {
            circleStack.getChildren().addAll(bg, icon);
        }

        // Pulse animation untuk unlocked nodes
        if (unlocked) {
            ScaleTransition pulse = new ScaleTransition(Duration.millis(1800), bg);
            pulse.setFromX(1.0); pulse.setFromY(1.0);
            pulse.setToX(1.05);  pulse.setToY(1.05);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();
        }

        // Skill name
        Label name = new Label(skill.name());
        name.setWrapText(true);
        name.setMaxWidth(120);
        name.setAlignment(Pos.CENTER);
        name.setStyle(
            "-fx-text-fill: " + (unlocked ? color : (canUnlock ? "#A09070" : "#3A2810")) + ";" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
            (unlocked ? "-fx-font-weight: bold;" : "")
        );

        // Status label
        String statusTxt = unlocked
            ? (equipped ? "✓ AKTIF" : "TERBUKA")
            : canUnlock ? skill.spCost() + " SP"
            : "LV." + skill.levelReq();
        String statusColor = unlocked
            ? (equipped ? color : "#2D7A45")
            : canUnlock ? "#FFB830" : "#2A1808";
        Label status = new Label(statusTxt);
        status.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");

        node.getChildren().addAll(circleStack, name, status);

        // Tooltip
        Tooltip tip = new Tooltip(
            skill.name() + "\n\n" + skill.desc() +
            "\n\nLevel: " + skill.levelReq() +
            " | SP: " + skill.spCost() +
            (skill.requiresId() != null ? "\nButuh: " + nameOf(skill.requiresId()) : "") +
            (unlocked && !equipped ? "\n\n[Klik] Aktifkan jurus ini" : "") +
            (unlocked && equipped ? "\n\n[Klik] Lepas dari slot aktif" : "") +
            (canUnlock ? "\n\n[Klik] Buka dengan " + skill.spCost() + " SP" : "")
        );
        tip.setStyle("-fx-background-color: #1A1008; -fx-border-color: " + color +
                     "; -fx-text-fill: #EDE0C8; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        Tooltip.install(node, tip);

        // Hover
        node.setOnMouseEntered(e -> {
            if (canUnlock || unlocked) bg.setStroke(Color.web(color));
        });
        node.setOnMouseExited(e -> {
            bg.setStroke(Color.web(circleStroke));
        });

        // Click
        node.setOnMouseClicked(e -> {
            if (unlocked) {
                if (equipped) {
                    player.unequipSkill(skill.id());
                } else if (player.getEquippedSkillCount() < 4) {
                    player.equipSkill(skill.id());
                } else {
                    router.addSystemChat("Slot aktif penuh (maks 4 jurus).");
                }
                router.showSkillTree();
            } else if (canUnlock) {
                if (player.spendSkillPoint(skill.spCost())) {
                    player.forceUnlockSkill(skill.id());
                    router.addSystemChat("✦ Jurus '" + skill.name() + "' dibuka!");
                    router.showSkillTree();
                }
            }
        });

        return node;
    }

    private String nameOf(String id) {
        return ALL.stream().filter(s -> s.id().equals(id))
            .findFirst().map(SkillNode::name).orElse(id);
    }
}
