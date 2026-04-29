package arclightcity.ui.view;
import arclightcity.entity.stats.StatType;

import arclightcity.engine.GameEngine;
import arclightcity.entity.player.Player;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;

/**
 * ProfileView — stat sheet karakter.
 * Mirip dengan Profile > Stats screen di Arclight City asli.
 */
public class ProfileView {

    private final GameEngine  engine;
    private final SceneRouter router;

    public ProfileView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    public Parent build() {
        Player player = engine.getPlayer();
        VBox root = UIFactory.screenRoot();

        root.getChildren().add(UIFactory.headerWithResources(
                "PROFILE", () -> router.showHub(),
                player.getGold(), 0));

        // Tab bar: STATS | EQUIPMENT (stub)
        HBox tabBar = new HBox(0);
        tabBar.setStyle("-fx-background-color: #0C1220; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        String[] tabs = {"STATS", "EQUIPMENT", "SKILLS"};
        for (String tab : tabs) {
            Label t = new Label(tab);
            t.setPadding(new Insets(10, 20, 10, 20));
            boolean active = tab.equals("STATS");
            t.setStyle(
                "-fx-text-fill: " + (active ? UIFactory.CYAN : UIFactory.DIM) + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;" +
                (active ? "-fx-border-color: transparent transparent #00E5FF transparent; -fx-border-width: 0 0 2 0;" : "")
            );
            tabBar.getChildren().add(t);
        }
        root.getChildren().add(tabBar);

        // ID Card (mirip Arclight City ID card di screenshot)
        root.getChildren().add(buildIdCard(player));

        // Stats list (scrollable)
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #050810; -fx-background: #050810; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox statsList = new VBox(0);

        // ── SECTION: LEVEL ─────────────────────────────
        statsList.getChildren().add(sectionHeader("CHARACTER"));
        statsList.getChildren().add(UIFactory.statRow("Level",     String.valueOf(player.getLevel())));
        statsList.getChildren().add(UIFactory.statRow("EXP",
                UIFactory.formatNumber((long)player.getCurrentExp()) + " / " +
                UIFactory.formatNumber((long)player.getExpToNextLevel())));
        statsList.getChildren().add(UIFactory.statRow("Gold", UIFactory.formatNumber(player.getGold())));
        statsList.getChildren().add(UIFactory.statRow("Background", player.getBackground().name));
        statsList.getChildren().add(UIFactory.statRow("Floor Reached", String.valueOf(player.getDungeonDepth())));

        // ── SECTION: VITALS ────────────────────────────
        statsList.getChildren().add(sectionHeader("VITALS"));
        statsList.getChildren().add(UIFactory.statRowHighlight("Health",
                formatStat(player, StatType.MAX_HP), UIFactory.RED));
        statsList.getChildren().add(UIFactory.statRowHighlight("Shield",
                formatStat(player, StatType.MAX_SHIELD), UIFactory.PURPLE));
        statsList.getChildren().add(UIFactory.statRow("HP Regen",
                formatStat(player, StatType.HP_REGEN) + "/turn"));
        statsList.getChildren().add(UIFactory.statRow("Shield Regen",
                formatStat(player, StatType.SHIELD_REGEN) + "/turn"));
        statsList.getChildren().add(UIFactory.statRow("Energy (MP)",
                formatStat(player, StatType.MAX_MP)));
        statsList.getChildren().add(UIFactory.statRow("MP Regen",
                formatStat(player, StatType.MP_REGEN) + "/turn"));

        // ── SECTION: OFFENSE ───────────────────────────
        statsList.getChildren().add(sectionHeader("OFFENSE"));
        statsList.getChildren().add(UIFactory.statRow("Physical ATK",
                formatStat(player, StatType.PHYSICAL_ATK)));
        statsList.getChildren().add(UIFactory.statRow("Cyber ATK",
                formatStat(player, StatType.CYBER_ATK)));
        statsList.getChildren().add(UIFactory.statRow("Energy ATK",
                formatStat(player, StatType.ENERGY_ATK)));
        statsList.getChildren().add(UIFactory.statRowHighlight("Damage Mult",
                formatPercent(player, StatType.DAMAGE_MULT), UIFactory.YELLOW));
        statsList.getChildren().add(UIFactory.statRow("Critical Chance",
                formatPercent(player, StatType.CRIT_CHANCE)));
        statsList.getChildren().add(UIFactory.statRow("Critical Damage",
                String.format("%.0f%%", player.getStats().get(StatType.CRIT_DAMAGE) * 100)));
        statsList.getChildren().add(UIFactory.statRow("Armor Pierce",
                formatPercent(player, StatType.ARMOR_PIERCE)));
        statsList.getChildren().add(UIFactory.statRow("Lifesteal",
                formatPercent(player, StatType.LIFESTEAL)));
        statsList.getChildren().add(UIFactory.statRow("Skill Power",
                String.format("%.0f%%", player.getStats().get(StatType.SKILL_POWER) * 100)));

        // ── SECTION: DEFENSE ───────────────────────────
        statsList.getChildren().add(sectionHeader("DEFENSE"));
        statsList.getChildren().add(UIFactory.statRow("Physical DEF",
                formatStat(player, StatType.PHYSICAL_DEF)));
        statsList.getChildren().add(UIFactory.statRow("Cyber DEF",
                formatStat(player, StatType.CYBER_DEF)));
        statsList.getChildren().add(UIFactory.statRow("Energy DEF",
                formatStat(player, StatType.ENERGY_DEF)));
        statsList.getChildren().add(UIFactory.statRow("Evasion",
                formatPercent(player, StatType.EVASION)));
        statsList.getChildren().add(UIFactory.statRow("Block Chance",
                formatPercent(player, StatType.BLOCK_CHANCE)));
        statsList.getChildren().add(UIFactory.statRow("Tenacity",
                formatPercent(player, StatType.TENACITY)));

        // ── SECTION: UTILITY ───────────────────────────
        statsList.getChildren().add(sectionHeader("UTILITY"));
        statsList.getChildren().add(UIFactory.statRow("Speed",
                formatStat(player, StatType.SPEED)));
        statsList.getChildren().add(UIFactory.statRow("Accuracy",
                formatPercent(player, StatType.ACCURACY)));
        statsList.getChildren().add(UIFactory.statRow("CDR",
                formatPercent(player, StatType.COOLDOWN_REDUCE)));

        scroll.setContent(statsList);
        root.getChildren().add(scroll);

        UIFactory.fadeIn(root, 300);
        return root;
    }

    // ── ID Card ───────────────────────────────────────────

    private VBox buildIdCard(Player player) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle(
            "-fx-background-color: #0C1220;" +
            "-fx-border-color: #1C2E44;" +
            "-fx-border-width: 0 0 1 0;"
        );

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        // Avatar placeholder (ASCII art)
        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.setPrefSize(70, 70);
        avatar.setStyle(
            "-fx-background-color: #080D18;" +
            "-fx-border-color: #00E5FF55;" +
            "-fx-border-width: 1;"
        );
        Label avatarIcon = new Label("◈");
        avatarIcon.setStyle("-fx-text-fill: #00E5FF; -fx-font-size: 28px;");
        avatar.getChildren().add(avatarIcon);

        // Info block
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label cityLabel = new Label("ARCLIGHT CITY");
        cityLabel.setStyle(
            "-fx-text-fill: #00E5FF; -fx-font-family: 'Courier New'; " +
            "-fx-font-size: 13px; -fx-font-weight: bold; " +
            "-fx-effect: dropshadow(gaussian, #00E5FF, 5, 0.3, 0, 0);"
        );

        Label nameLabel = new Label(player.getName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: #E0E8F0; -fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label bgLabel = new Label("Background / " + player.getBackground().name);
        bgLabel.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        Label lvLabel = new Label("Level / " + player.getLevel() + " CHARACTER");
        lvLabel.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        info.getChildren().addAll(cityLabel, nameLabel, bgLabel, lvLabel);
        row.getChildren().addAll(avatar, info);

        // Barcode (decorative)
        Label barcode = new Label("║ ║║ ║║║ ║ ║║║ ║ ║║║ ║║ ║║ ║║║");
        barcode.setStyle("-fx-text-fill: #1C2E44; -fx-font-size: 8px;");

        // EXP bar (replaces the social credit bar from original)
        VBox expSection = new VBox(2);
        Label expTitle = new Label("CHARACTER LEVEL — " + player.getLevel());
        expTitle.setStyle("-fx-text-fill: #00E5FF; -fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;");

        ProgressBar expBar = new ProgressBar(player.getExpPercent());
        expBar.setMaxWidth(Double.MAX_VALUE);
        expBar.setStyle("-fx-accent: #FFD600; -fx-min-height: 6px; -fx-max-height: 6px; -fx-background-color: #1C1400;");

        Label expVal = new Label(UIFactory.formatNumber((long)player.getCurrentExp()) + " / " +
                UIFactory.formatNumber((long)player.getExpToNextLevel()));
        expVal.setStyle("-fx-text-fill: #FFD600; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        expVal.setAlignment(Pos.CENTER_RIGHT);
        expVal.setMaxWidth(Double.MAX_VALUE);

        expSection.getChildren().addAll(expTitle, expBar, expVal);

        card.getChildren().addAll(row, barcode, expSection);
        return card;
    }

    // ── Helpers ───────────────────────────────────────────

    private VBox sectionHeader(String title) {
        VBox section = new VBox();
        Label label = new Label("  " + title);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPadding(new Insets(8, 12, 4, 12));
        label.setStyle(
            "-fx-text-fill: #5A6A80;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: #080D18;"
        );
        section.getChildren().add(label);
        return section;
    }

    private String formatStat(Player p, StatType type) {
        double val = p.getStats().get(type);
        if (val == Math.floor(val)) return String.valueOf((int)val);
        return String.format("%.1f", val);
    }

    private String formatPercent(Player p, StatType type) {
        return String.format("%.0f%%", p.getStats().get(type) * 100);
    }
}
