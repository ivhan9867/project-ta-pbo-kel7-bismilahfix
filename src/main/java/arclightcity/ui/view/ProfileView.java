package arclightcity.ui.view;

import arclightcity.entity.stats.StatType;
import arclightcity.engine.GameEngine;
import arclightcity.entity.player.Player;
import arclightcity.item.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;
import java.util.List;

/**
 * ProfileView — stat sheet + equipment + skills.
 * Tabs: STATS | EQUIPMENT | SKILLS
 */
public class ProfileView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private String activeTab = "STATS";

    public ProfileView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    public void setActiveTab(String tab) { this.activeTab = tab; }

    public Parent build() {
        return buildWithTab(activeTab);
    }

    private Parent buildWithTab(String tab) {
        activeTab = tab;
        Player player = engine.getPlayer();

        BorderPane root = UIFactory.screenRootBorder();

        // ── TOP: header + tab bar ─────────────────────────────
        VBox top = new VBox(0);
        top.getChildren().add(UIFactory.headerWithResources(
                "PROFILE", () -> router.showHub(),
                player.getGold(), 0));
        top.getChildren().add(buildTabBar(player));
        root.setTop(top);

        // ── CENTER: tab content (scrollable) ──────────────────
        VBox content = switch (tab) {
            case "EQUIPMENT" -> buildEquipmentTab(player);
            case "SKILLS"    -> buildSkillsTab(player);
            default          -> buildStatsTab(player);
        };

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #050810; -fx-background: #050810;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        return root;
    }

    // ── Tab Bar ───────────────────────────────────────────────

    private HBox buildTabBar(Player player) {
        HBox tabBar = new HBox(0);
        tabBar.setStyle("-fx-background-color: #0C1220;" +
                        "-fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        for (String tab : new String[]{"STATS", "EQUIPMENT", "SKILLS"}) {
            Button btn = new Button(tab);
            boolean active = tab.equals(activeTab);
            btn.setStyle(
                "-fx-background-color: " + (active ? "#00E5FF11" : "transparent") + ";" +
                "-fx-text-fill: " + (active ? UIFactory.CYAN : UIFactory.DIM) + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 10 20;" +
                "-fx-border-color: transparent transparent " +
                    (active ? "#00E5FF" : "transparent") + " transparent;" +
                "-fx-border-width: 0 0 2 0; -fx-cursor: hand;"
            );
            final String t = tab;
            btn.setOnAction(e -> router.showProfile(t));
            tabBar.getChildren().add(btn);
        }

        // Skill points badge
        int sp = player.getSkillPoints();
        if (sp > 0) {
            Label badge = new Label(" " + sp + " SP ");
            badge.setStyle(
                "-fx-background-color: #FFD60033; -fx-text-fill: #FFD600;" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                "-fx-font-weight: bold; -fx-padding: 2 6;" +
                "-fx-border-color: #FFD60066; -fx-border-width: 1;"
            );
            HBox.setMargin(badge, new Insets(8, 8, 8, 8));
            tabBar.getChildren().add(badge);
        }

        return tabBar;
    }

    // ── STATS Tab ─────────────────────────────────────────────

    private VBox buildStatsTab(Player player) {
        VBox list = new VBox(0);

        // ID card
        list.getChildren().add(buildIdCard(player));

        // Character
        list.getChildren().add(sectionHeader("CHARACTER"));
        list.getChildren().add(UIFactory.statRow("Level",  String.valueOf(player.getLevel())));
        list.getChildren().add(UIFactory.statRow("EXP",
                fmt(player.getCurrentExp()) + " / " + fmt(player.getExpToNextLevel())));
        list.getChildren().add(UIFactory.statRow("Gold",       fmt(player.getGold())));
        list.getChildren().add(UIFactory.statRow("Background", player.getBackground().name));
        list.getChildren().add(UIFactory.statRow("Floor Depth",
                String.valueOf(player.getDungeonDepth())));
        list.getChildren().add(UIFactory.statRow("Skill Points",
                String.valueOf(player.getSkillPoints())));

        // Vitals
        list.getChildren().add(sectionHeader("VITALS"));
        list.getChildren().add(UIFactory.statRowHighlight("HP",
                fmtStat(player, StatType.MAX_HP), UIFactory.RED));
        list.getChildren().add(UIFactory.statRowHighlight("Shield",
                fmtStat(player, StatType.MAX_SHIELD), UIFactory.PURPLE));
        list.getChildren().add(UIFactory.statRow("HP Regen",
                fmtStat(player, StatType.HP_REGEN) + "/turn"));
        list.getChildren().add(UIFactory.statRow("Shield Regen",
                fmtStat(player, StatType.SHIELD_REGEN) + "/turn"));
        list.getChildren().add(UIFactory.statRowHighlight("Energy (MP)",
                fmtStat(player, StatType.MAX_MP), UIFactory.CYAN));
        list.getChildren().add(UIFactory.statRow("MP Regen",
                fmtStat(player, StatType.MP_REGEN) + "/turn"));

        // Offense
        list.getChildren().add(sectionHeader("OFFENSE"));
        list.getChildren().add(UIFactory.statRowHighlight("Physical ATK",
                fmtStat(player, StatType.PHYSICAL_ATK), "#FF6B6B"));
        list.getChildren().add(UIFactory.statRowHighlight("Cyber ATK",
                fmtStat(player, StatType.CYBER_ATK), UIFactory.CYAN));
        list.getChildren().add(UIFactory.statRowHighlight("Energy ATK",
                fmtStat(player, StatType.ENERGY_ATK), UIFactory.PURPLE));
        list.getChildren().add(UIFactory.statRow("Skill Power",
                fmtPct(player, StatType.SKILL_POWER)));
        list.getChildren().add(UIFactory.statRow("Crit Chance",
                fmtPct(player, StatType.CRIT_CHANCE)));
        list.getChildren().add(UIFactory.statRow("Crit DMG",
                fmtPct(player, StatType.CRIT_DAMAGE)));
        list.getChildren().add(UIFactory.statRow("Armor Pierce",
                fmtPct(player, StatType.ARMOR_PIERCE)));

        // Defense
        list.getChildren().add(sectionHeader("DEFENSE"));
        list.getChildren().add(UIFactory.statRow("Physical DEF",
                fmtStat(player, StatType.PHYSICAL_DEF)));
        list.getChildren().add(UIFactory.statRow("Cyber DEF",
                fmtStat(player, StatType.CYBER_DEF)));
        list.getChildren().add(UIFactory.statRow("Energy DEF",
                fmtStat(player, StatType.ENERGY_DEF)));
        list.getChildren().add(UIFactory.statRow("Evasion",
                fmtPct(player, StatType.EVASION)));
        list.getChildren().add(UIFactory.statRow("Block Chance",
                fmtPct(player, StatType.BLOCK_CHANCE)));

        // Utility
        list.getChildren().add(sectionHeader("UTILITY"));
        list.getChildren().add(UIFactory.statRow("Speed",
                fmtStat(player, StatType.SPEED)));
        list.getChildren().add(UIFactory.statRow("Initiative",
                fmtStat(player, StatType.INITIATIVE)));
        list.getChildren().add(UIFactory.statRow("Lifesteal",
                fmtPct(player, StatType.LIFESTEAL)));
        list.getChildren().add(UIFactory.statRow("CDR",
                fmtPct(player, StatType.COOLDOWN_REDUCE)));
        list.getChildren().add(UIFactory.statRow("Accuracy",
                fmtPct(player, StatType.ACCURACY)));
        list.getChildren().add(UIFactory.statRow("Sync Rate",
                fmtPct(player, StatType.SYNC_RATE)));
        list.getChildren().add(UIFactory.statRow("DMG Multiplier",
                fmtPct(player, StatType.DAMAGE_MULT)));

        return list;
    }

    // ── EQUIPMENT Tab ─────────────────────────────────────────

    private VBox buildEquipmentTab(Player player) {
        VBox list = new VBox(0);
        list.getChildren().add(buildIdCard(player));

        Inventory inv = engine.getInventory();
        list.getChildren().add(sectionHeader("EQUIPPED GEAR"));

        // Weapon
        list.getChildren().add(buildEquipSlot("⚔ WEAPON",
                inv.getEquippedWeapon()));
        // Armor
        list.getChildren().add(buildEquipSlot("🛡 ARMOR",
                inv.getEquippedArmor()));
        // Accessories
        list.getChildren().add(buildEquipSlot("◈ ACCESSORY 1",
                inv.getEquippedAccessory1()));
        list.getChildren().add(buildEquipSlot("◈ ACCESSORY 2",
                inv.getEquippedAccessory2()));

        list.getChildren().add(sectionHeader("EQUIPMENT IN BAG"));
        if (inv.getEquipmentInBag().isEmpty()) {
            Label empty = new Label("  No equipment in bag.");
            empty.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 11px; -fx-padding: 8 16;");
            list.getChildren().add(empty);
        } else {
            for (Equipment eq : inv.getEquipmentInBag()) {
                list.getChildren().add(buildEquipRow(eq));
            }
        }

        return list;
    }

    private VBox buildEquipSlot(String slotName, Equipment eq) {
        VBox slot = new VBox(4);
        slot.setPadding(new Insets(8, 16, 8, 16));
        slot.setStyle("-fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        Label nameLabel = new Label(slotName);
        nameLabel.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 10px; -fx-font-weight: bold;");
        slot.getChildren().add(nameLabel);

        if (eq == null) {
            Label empty = new Label("— Empty —");
            empty.setStyle("-fx-text-fill: #2A3A50; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 11px;");
            slot.getChildren().add(empty);
        } else {
            slot.getChildren().add(buildEquipRow(eq));
        }
        return slot;
    }

    private HBox buildEquipRow(Equipment eq) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(6, 0, 6, 0));
        row.setAlignment(Pos.CENTER_LEFT);

        String rarityColor = UIFactory.rarityColor(eq.getRarity());

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox nameRow = new HBox(8);
        Label name = new Label(eq.getFullName());
        name.setStyle("-fx-text-fill: " + rarityColor +
                      "; -fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
                      "-fx-font-weight: bold;");
        Label lvlBadge = new Label("+" + eq.getUpgradeLevel());
        lvlBadge.setStyle("-fx-text-fill: " + UIFactory.GREEN +
                          "; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        nameRow.getChildren().addAll(name, lvlBadge);

        // Top 3 stats
        String stats = eq.getStatBonuses().entrySet().stream().limit(3)
                .map(e -> e.getKey().displayName + " +" +
                          String.format("%.0f", e.getValue()))
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "  " + b);
        Label statLabel = new Label(stats);
        statLabel.setStyle("-fx-text-fill: #8899AA; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 10px;");

        info.getChildren().addAll(nameRow, statLabel);
        row.getChildren().add(info);
        return row;
    }

    // ── SKILLS Tab ────────────────────────────────────────────

    private VBox buildSkillsTab(Player player) {
        VBox list = new VBox(0);
        list.getChildren().add(buildIdCard(player));

        // Skill Points available
        if (player.getSkillPoints() > 0) {
            HBox spBanner = new HBox(8);
            spBanner.setPadding(new Insets(10, 16, 10, 16));
            spBanner.setAlignment(Pos.CENTER_LEFT);
            spBanner.setStyle("-fx-background-color: #FFD60011; -fx-border-color: #FFD60066;" +
                              "-fx-border-width: 0 0 1 0;");
            Label spLabel = new Label("⬆ " + player.getSkillPoints() +
                                      " Skill Point(s) available — click to unlock!");
            spLabel.setStyle("-fx-text-fill: #FFD600; -fx-font-family: 'Courier New';" +
                             "-fx-font-size: 12px; -fx-font-weight: bold;");
            spBanner.getChildren().add(spLabel);
            list.getChildren().add(spBanner);
        }

        // Equipped skills
        list.getChildren().add(sectionHeader("EQUIPPED SKILLS"));
        List<String> equipped = player.getEquippedSkillIds();
        if (equipped.isEmpty()) {
            list.getChildren().add(emptyLabel("No skills equipped."));
        } else {
            equipped.forEach(sid -> list.getChildren().add(buildSkillRow(player, sid, true)));
        }

        // Unlocked but not equipped
        list.getChildren().add(sectionHeader("UNLOCKED SKILLS"));
        boolean hasUnequipped = false;
        for (String sid : player.getUnlockedSkillIds()) {
            if (!equipped.contains(sid)) {
                list.getChildren().add(buildSkillRow(player, sid, false));
                hasUnequipped = true;
            }
        }
        if (!hasUnequipped)
            list.getChildren().add(emptyLabel("All unlocked skills are equipped."));

        // Available to unlock
        list.getChildren().add(sectionHeader("AVAILABLE TO UNLOCK"));
        String[] allSkills = {"POWER_STRIKE","EXECUTE","DEEP_HACK","VIRUS_UPLOAD",
                              "PHANTOM_SHOT","SHADOW_STEP","IRON_SHIELD","SHOCKWAVE",
                              "ENERGY_DRAIN","BIO_IRRADIATE","EMP_BURST","FIELD_BARRIER"};
        boolean hasLocked = false;
        for (String sid : allSkills) {
            if (!player.getUnlockedSkillIds().contains(sid)) {
                list.getChildren().add(buildLockedSkillRow(player, sid));
                hasLocked = true;
            }
        }
        if (!hasLocked)
            list.getChildren().add(emptyLabel("All skills unlocked!"));

        return list;
    }

    private HBox buildSkillRow(Player player, String skillId, boolean isEquipped) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(8, 16, 8, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #00E5FF0A; -fx-border-color: #00E5FF33;" +
                     "-fx-border-width: 0 0 1 3;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        String dispName = skillId.replace("_", " ");
        Label name = new Label(dispName);
        name.setStyle("-fx-text-fill: #00E5FF; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 12px; -fx-font-weight: bold;");

        int cd = player.getSkillCooldown(skillId);
        Label statusLabel = new Label(isEquipped ? "EQUIPPED" + (cd > 0 ? " — CD: " + cd : " — READY") : "UNLOCKED");
        statusLabel.setStyle("-fx-text-fill: " + (isEquipped ? UIFactory.GREEN : "#5A6A80") +
                             "; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        info.getChildren().addAll(name, statusLabel);
        row.getChildren().add(info);

        // Equip/Unequip button
        if (isEquipped) {
            Button unequip = new Button("UNEQUIP");
            unequip.setStyle("-fx-background-color: transparent; -fx-border-color: #5A6A80;" +
                             "-fx-border-width: 1; -fx-text-fill: #5A6A80;" +
                             "-fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-cursor: hand;");
            unequip.setOnAction(e -> {
                player.unequipSkill(skillId);
                router.showProfile("SKILLS");
            });
            row.getChildren().add(unequip);
        } else {
            Button equip = new Button("EQUIP");
            equip.setStyle("-fx-background-color: #00E5FF11; -fx-border-color: #00E5FF;" +
                           "-fx-border-width: 1; -fx-text-fill: #00E5FF;" +
                           "-fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-cursor: hand;");
            equip.setOnAction(e -> {
                player.equipSkill(skillId);
                router.showProfile("SKILLS");
            });
            row.getChildren().add(equip);
        }

        return row;
    }

    private HBox buildLockedSkillRow(Player player, String skillId) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(8, 16, 8, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #0C1220; -fx-border-color: #1C2E44;" +
                     "-fx-border-width: 0 0 1 0;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        String dispName = skillId.replace("_", " ");
        Label name = new Label("🔒 " + dispName);
        name.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 12px;");
        Label cost = new Label("Cost: 1 Skill Point");
        cost.setStyle("-fx-text-fill: #2A3A50; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 10px;");
        info.getChildren().addAll(name, cost);
        row.getChildren().add(info);

        boolean canUnlock = player.getSkillPoints() > 0;
        Button unlock = new Button("UNLOCK");
        unlock.setDisable(!canUnlock);
        unlock.setStyle("-fx-background-color: " + (canUnlock ? "#FFD60011" : "transparent") + ";" +
                        "-fx-border-color: " + (canUnlock ? "#FFD600" : "#1C2E44") + ";" +
                        "-fx-border-width: 1; -fx-text-fill: " + (canUnlock ? "#FFD600" : "#2A3A50") + ";" +
                        "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                        "-fx-cursor: " + (canUnlock ? "hand" : "default") + ";");
        unlock.setOnAction(e -> {
            if (player.unlockSkill(skillId)) {
                router.showProfile("SKILLS");
            }
        });
        row.getChildren().add(unlock);

        return row;
    }

    // ── ID Card ───────────────────────────────────────────────

    private VBox buildIdCard(Player player) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle("-fx-background-color: #0C1220;" +
                      "-fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        // Name + avatar
        HBox nameRow = new HBox(12);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label("◈");
        avatar.setStyle("-fx-text-fill: #00E5FF; -fx-font-size: 28px;");

        VBox nameInfo = new VBox(2);
        Label name = new Label(player.getName().toUpperCase());
        name.setStyle("-fx-text-fill: #00E5FF; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 18px; -fx-font-weight: bold;");
        Label bgLabel = new Label(player.getBackground().name + "  •  LV." + player.getLevel());
        bgLabel.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 11px;");
        nameInfo.getChildren().addAll(name, bgLabel);
        nameRow.getChildren().addAll(avatar, nameInfo);
        card.getChildren().add(nameRow);

        // EXP bar
        double expPct = player.getExpToNextLevel() > 0
                ? player.getCurrentExp() / player.getExpToNextLevel() : 0;
        ProgressBar expBar = new ProgressBar(expPct);
        expBar.setPrefWidth(Double.MAX_VALUE);
        expBar.setPrefHeight(6);
        expBar.setStyle("-fx-accent: #FFD600; -fx-background-color: #FFD60022;" +
                        "-fx-min-height: 6px; -fx-max-height: 6px;");
        Label expLabel = new Label("EXP  " + fmt(player.getCurrentExp()) +
                                   " / " + fmt(player.getExpToNextLevel()));
        expLabel.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 10px;");

        card.getChildren().addAll(expBar, expLabel);
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────

    private VBox sectionHeader(String title) {
        VBox sec = new VBox();
        Label label = UIFactory.sectionTitle("── " + title + " ──");
        label.setPadding(new Insets(10, 16, 4, 16));
        sec.getChildren().add(label);
        return sec;
    }

    private Label emptyLabel(String text) {
        Label l = new Label("  " + text);
        l.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New';" +
                   "-fx-font-size: 11px; -fx-padding: 8 16;");
        return l;
    }

    private String fmt(double val) { return UIFactory.formatNumber((long)val); }
    private String fmt(long val)   { return UIFactory.formatNumber(val); }

    private String fmtStat(Player p, StatType t) {
        return String.format("%.0f", p.getStats().get(t));
    }

    private String fmtPct(Player p, StatType t) {
        return String.format("%.1f%%", p.getStats().get(t) * 100);
    }
}
