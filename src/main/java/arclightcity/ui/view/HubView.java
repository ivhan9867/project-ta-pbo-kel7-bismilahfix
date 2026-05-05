package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.entity.player.Player;
import arclightcity.entity.stats.StatType;
import arclightcity.ui.ArclightApp;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * HubView — Markas Pendekar. Titik istirahat dan persiapan.
 * Tema: Pendopo megah dengan ornamen batik kerajaan.
 */
public class HubView {

    private final GameEngine  engine;
    private final SceneRouter router;

    public HubView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    public Parent build() {
        Player player = engine.getPlayer();
        BorderPane root = UIFactory.screenRootBorder();

        // ── TOP: Player identity bar ──────────────────────
        root.setTop(buildIdentityBar(player));

        // ── CENTER: Scrollable content ────────────────────
        VBox center = new VBox(0);

        // Vitals
        center.getChildren().add(buildVitalsSection(player));

        // District banner
        center.getChildren().add(buildDistrictBanner(player));

        // Mythic fragment progress
        long frags = getMythicFragmentCount();
        center.getChildren().add(buildMythicBar(frags));

        // Navigation grid
        center.getChildren().add(buildNavGrid());

        // Active crew
        center.getChildren().add(buildCrewPreview(player));

        ScrollPane scroll = new ScrollPane(center);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        // ── BOTTOM: Quick action bar ──────────────────────
        root.setBottom(buildQuickBar(player));

        // Idle chat trigger
        new Timeline(new KeyFrame(Duration.millis(600),
            e -> router.emitChat(MercenaryDialogue.Trigger.HUB_IDLE))).play();

        UIFactory.fadeIn(root, 400);
        return root;
    }

    // ── Identity Bar ──────────────────────────────────────

    private VBox buildIdentityBar(Player player) {
        VBox bar = new VBox(0);
        bar.setStyle("-fx-background-color: #0F0A06;" +
                     "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        // Top row: ornamen + judul
        HBox topRow = new HBox(10);
        topRow.setPadding(new Insets(10, 16, 8, 16));
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label ornL = new Label("◆");
        ornL.setStyle("-fx-text-fill: #3A2810; -fx-font-size: 14px;");

        Label title = new Label("MYTHIC ITEM OBTAINED");
        title.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 11px; -fx-font-weight: bold; -fx-letter-spacing: 3;");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label ornR = new Label("◆");
        ornR.setStyle("-fx-text-fill: #3A2810; -fx-font-size: 14px;");
        topRow.getChildren().addAll(ornL, title, ornR);

        // Player row
        HBox playerRow = new HBox(12);
        playerRow.setPadding(new Insets(0, 16, 12, 16));
        playerRow.setAlignment(Pos.CENTER_LEFT);

        // Avatar circle
        StackPane avatar = new StackPane();
        Circle avatarBg = new Circle(24, Color.web("#1A1008"));
        avatarBg.setStroke(Color.web("#C8860A"));
        avatarBg.setStrokeWidth(1.5);
        Label avatarIcon = new Label("⚔");
        avatarIcon.setStyle("-fx-font-size: 18px;");
        avatar.getChildren().addAll(avatarBg, avatarIcon);

        // Player info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(player.getName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 16px; -fx-font-weight: bold;" +
                           "-fx-effect: dropshadow(gaussian, #C8860A, 6, 0.3, 0, 0);");

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);

        Label lvBadge = new Label("LV." + player.getLevel());
        lvBadge.setStyle("-fx-background-color: #C8860A22; -fx-text-fill: #C8860A;" +
                         "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                         "-fx-font-weight: bold; -fx-padding: 2 6;" +
                         "-fx-border-color: #C8860A44; -fx-border-width: 1;");

        Label bgBadge = new Label(player.getBackground().name);
        bgBadge.setStyle("-fx-background-color: #1A1008; -fx-text-fill: #6A5840;" +
                         "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                         "-fx-padding: 2 6; -fx-border-color: #3A2810; -fx-border-width: 1;");

        Label depthBadge = new Label("Lantai " + player.getDungeonDepth());
        depthBadge.setStyle("-fx-background-color: #1A1008; -fx-text-fill: #5A3A10;" +
                            "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                            "-fx-padding: 2 6; -fx-border-color: #3A2810; -fx-border-width: 1;");

        badges.getChildren().addAll(lvBadge, bgBadge, depthBadge);
        info.getChildren().addAll(nameLabel, badges);

        // Gold + Fragment
        VBox resources = new VBox(4);
        resources.setAlignment(Pos.CENTER_RIGHT);

        Label gold = new Label("⚙ " + UIFactory.formatNumber(player.getGold()));
        gold.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 13px; -fx-font-weight: bold;");

        long frags = getMythicFragmentCount();
        Label fragLbl = new Label("✦ " + frags + "/5");
        fragLbl.setStyle("-fx-text-fill: " + (frags > 0 ? "#FF6B00" : "#2A1808") +
                         "; -fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                         "-fx-font-weight: bold;");
        fragLbl.setTooltip(new javafx.scene.control.Tooltip(
            "Serpihan Red Essence: " + frags + "/5\n" +
            "Kumpulkan dari 5 boss (F10/20/30/40/50)\n" +
            "Untuk menempa Red Blossom Katana"));

        resources.getChildren().addAll(gold, fragLbl);
        playerRow.getChildren().addAll(avatar, info, resources);
        bar.getChildren().addAll(topRow, playerRow);
        return bar;
    }

    // ── Vitals Section ────────────────────────────────────

    private VBox buildVitalsSection(Player player) {
        VBox sec = new VBox(6);
        sec.setPadding(new Insets(12, 16, 10, 16));
        sec.setStyle("-fx-background-color: #0F0A06;" +
                     "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        // EXP bar
        double expPct = player.getExpToNextLevel() > 0
                ? player.getCurrentExp() / player.getExpToNextLevel() : 0;

        HBox expRow = new HBox(8);
        expRow.setAlignment(Pos.CENTER_LEFT);
        Label expLbl = new Label("EXP");
        expLbl.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        expLbl.setMinWidth(30);

        ProgressBar expBar = new ProgressBar(expPct);
        expBar.setPrefHeight(6);
        HBox.setHgrow(expBar, Priority.ALWAYS);
        expBar.setStyle("-fx-accent: #C8860A; -fx-background-color: #1A1008;" +
                        "-fx-min-height: 6; -fx-max-height: 6;");

        Label expVal = new Label(UIFactory.formatNumber((long)player.getCurrentExp()) +
                                 " / " + UIFactory.formatNumber((long)player.getExpToNextLevel()));
        expVal.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        expRow.getChildren().addAll(expLbl, expBar, expVal);

        // HP + Shield + MP in one row
        VBox vitals = UIFactory.compactVitalBars(
            player.getCurrentHp(),     player.getStats().get(StatType.MAX_HP),
            player.getCurrentShield(), player.getStats().get(StatType.MAX_SHIELD),
            player.getCurrentMp(),     player.getStats().get(StatType.MAX_MP));

        sec.getChildren().addAll(expRow, vitals);
        return sec;
    }

    // ── District Banner ───────────────────────────────────

    private HBox buildDistrictBanner(Player player) {
        HBox banner = new HBox();
        banner.setPadding(new Insets(10, 16, 10, 16));
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setStyle("-fx-background-color: #150E08;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        String district = getDistrictName(player.getDungeonDepth());

        VBox info = new VBox(2);
        Label districtLbl = new Label("◈ " + district);
        districtLbl.setStyle("-fx-text-fill: #C8860A; -fx-font-family: 'Courier New';" +
                             "-fx-font-size: 13px; -fx-font-weight: bold;");
        Label subLbl = new Label("Lokasi markas saat ini — siap untuk terjun ke kedalaman");
        subLbl.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        info.getChildren().addAll(districtLbl, subLbl);
        banner.getChildren().add(info);
        return banner;
    }

    // ── Mythic Bar ────────────────────────────────────────

    private HBox buildMythicBar(long frags) {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #1A0A04;" +
                     "-fx-border-color: #FF6B0044; -fx-border-width: 0 0 1 0;");

        Label icon = new Label("✦");
        icon.setStyle("-fx-text-fill: #FF6B00; -fx-font-size: 14px;" +
                      "-fx-effect: dropshadow(gaussian, #FF6B00, 6, 0.5, 0, 0);");

        Label lbl = new Label("SERPIHAN RED ESSENCE: " + frags + " / 5");
        lbl.setStyle("-fx-text-fill: #FF8833; -fx-font-family: 'Courier New';" +
                     "-fx-font-size: 12px; -fx-font-weight: bold;");
        HBox.setHgrow(lbl, Priority.ALWAYS);

        Label hint = new Label(frags >= 5 ? "✓ SIAP TEMPA!" : "Kalahkan boss di F10/20/30/40/50");
        hint.setStyle("-fx-text-fill: " + (frags >= 3 ? "#FF6B00" : "#3A2810") +
                      "; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        // Progress dots
        HBox dots = new HBox(4);
        for (int i = 0; i < 5; i++) {
            Circle dot = new Circle(5, Color.web(i < frags ? "#FF6B00" : "#2A1808"));
            if (i < frags) dot.setEffect(new javafx.scene.effect.DropShadow(
                6, Color.web("#FF6B00")));
            dots.getChildren().add(dot);
        }

        bar.getChildren().addAll(icon, lbl, hint, dots);
        return bar;
    }

    // ── Navigation Grid ───────────────────────────────────

    private VBox buildNavGrid() {
        VBox sec = new VBox(0);

        Label secTitle = new Label("── TINDAKAN ──");
        secTitle.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 10px; -fx-padding: 10 16 6 16;");
        sec.getChildren().add(secTitle);

        // Primary action - masuk dungeon
        Button enterDungeon = buildNavButton(
            "▶  MASUK DUNGEON",
            "Jelajahi lantai dungeon berikutnya",
            "#FFB830", "#C8860A22", "#FFB830", true);
        enterDungeon.setOnAction(e -> {
            engine.startDungeonRun();
            router.emitChat(MercenaryDialogue.Trigger.HUB_ENTER_DUNGEON);
            router.showDungeonMap();
        });
        sec.getChildren().add(enterDungeon);

        // Masuk kota
        Button enterCity = buildNavButton(
            "🏙  MASUK KOTA",
            "Toko senjata, jamu, bengkel empu & penadah barang",
            "#A09070", "transparent", "#5A3A10", false);
        enterCity.setOnAction(e -> router.showCity());
        sec.getChildren().add(enterCity);

        // Secondary actions - 2 per row
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setPadding(new Insets(0));

        Button guildmate = buildNavButton("◈  GUILDMATE", "Kelola & rekrut guildmate",
            "#A09070", "transparent", "#5A3A10", false);
        guildmate.setOnAction(e -> router.showMercenary());

        Button inv = buildNavButton("⊞  PERBENDAHARAAN", "Kelola senjata & item",
            "#A09070", "transparent", "#5A3A10", false);
        inv.setOnAction(e -> router.showInventory());

        Button profile = buildNavButton("☰  PROFIL PENDEKAR", "Lihat stat & jurus",
            "#A09070", "transparent", "#5A3A10", false);
        profile.setOnAction(e -> router.showProfile());

        Button save = buildNavButton("💾  SIMPAN", "Simpan perkembangan",
            "#5A3A10", "transparent", "#3A2810", false);
        save.setOnAction(e -> {
            var r = engine.saveGame();
            router.addSystemChat(r.success() ? "✦ " + r.message() : "✗ " + r.message());
        });

        GridPane.setHgrow(guildmate, Priority.ALWAYS);
        GridPane.setHgrow(inv, Priority.ALWAYS);
        GridPane.setHgrow(profile, Priority.ALWAYS);
        GridPane.setHgrow(save, Priority.ALWAYS);

        grid.add(guildmate,  0, 0);
        grid.add(inv,     1, 0);
        grid.add(profile, 0, 1);
        grid.add(save,    1, 1);

        sec.getChildren().add(grid);
        return sec;
    }

    private Button buildNavButton(String text, String subtitle,
                                   String textColor, String bgColor,
                                   String borderColor, boolean isPrimary) {
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(2);
        content.setAlignment(Pos.CENTER_LEFT);
        Label mainLbl = new Label(text);
        mainLbl.setStyle("-fx-text-fill: " + textColor + "; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: " + (isPrimary ? "14" : "12") + "px; -fx-font-weight: bold;");
        Label subLbl = new Label(subtitle);
        subLbl.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        content.getChildren().addAll(mainLbl, subLbl);

        btn.setGraphic(content);
        btn.setPadding(new Insets(isPrimary ? 14 : 10, 16, isPrimary ? 14 : 10, 16));
        btn.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 0 0 1 0;" +
            "-fx-cursor: hand; -fx-alignment: CENTER_LEFT;"
        );
        btn.setOnMouseEntered(ev -> {
            mainLbl.setStyle(mainLbl.getStyle().replace(textColor, "#FFB830"));
            btn.setStyle(btn.getStyle().replace(bgColor, "#C8860A11"));
        });
        btn.setOnMouseExited(ev -> {
            mainLbl.setStyle(mainLbl.getStyle().replace("#FFB830", textColor));
            btn.setStyle(btn.getStyle().replace("#C8860A11", bgColor));
        });
        return btn;
    }

    // ── Crew Preview ──────────────────────────────────────

    private VBox buildCrewPreview(Player player) {
        VBox sec = new VBox(0);
        sec.setStyle("-fx-border-color: #3A2810; -fx-border-width: 1 0 0 0;");

        Label secTitle = new Label("── REGU AKTIF ──");
        secTitle.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 10px; -fx-padding: 10 16 6 16;");
        sec.getChildren().add(secTitle);

        var mercs = engine.getActiveMercs();
        if (mercs.isEmpty()) {
            Label none = new Label("  Tidak ada guildmate aktif — kunjungi menu GUILDMATE");
            none.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 11px; -fx-padding: 8 16 12 16;");
            sec.getChildren().add(none);
        } else {
            for (var merc : mercs) {
                HBox card = new HBox(10);
                card.setPadding(new Insets(8, 16, 8, 16));
                card.setAlignment(Pos.CENTER_LEFT);
                card.setStyle("-fx-border-color: #2A1808; -fx-border-width: 0 0 1 0;");

                Circle dot = new Circle(5, Color.web("#C8860A"));
                VBox mInfo = new VBox(2);
                HBox.setHgrow(mInfo, Priority.ALWAYS);

                Label mName = new Label(merc.getMercenaryType().displayName.toUpperCase());
                mName.setStyle("-fx-text-fill: #A09070; -fx-font-family: 'Courier New';" +
                               "-fx-font-size: 12px; -fx-font-weight: bold;");
                Label mRole = new Label(merc.getRole().name() + "  ♥ " + merc.getLoyaltyTitle());
                mRole.setStyle("-fx-text-fill: #4A3820; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
                mInfo.getChildren().addAll(mName, mRole);

                VBox vitals = UIFactory.compactVitalBars(
                    merc.getCurrentHp(),     merc.getStats().get(StatType.MAX_HP),
                    merc.getCurrentShield(), merc.getStats().get(StatType.MAX_SHIELD),
                    merc.getCurrentMp(),     merc.getStats().get(StatType.MAX_MP));
                vitals.setMaxWidth(120);

                card.getChildren().addAll(dot, mInfo, vitals);
                sec.getChildren().add(card);
            }
        }
        return sec;
    }

    // ── Quick Bar (Bottom) ────────────────────────────────

    private HBox buildQuickBar(Player player) {
        HBox bar = new HBox(0);
        bar.setStyle("-fx-background-color: #0F0A06;" +
                     "-fx-border-color: #3A2810; -fx-border-width: 1 0 0 0;");

        String[][] items = {
            {"⚔", "DUNGEON"},
            {"◈", "GUILDMATE"},
            {"⊞", "ITEM"},
            {"☰", "PROFIL"}
        };

        Runnable[] actions = {
            () -> { engine.startDungeonRun();
                    router.emitChat(MercenaryDialogue.Trigger.HUB_ENTER_DUNGEON);
                    router.showDungeonMap(); },
            router::showMercenary,
            router::showInventory,
            router::showProfile
        };

        for (int i = 0; i < items.length; i++) {
            VBox btn = buildQuickBtn(items[i][0], items[i][1]);
            final int idx = i;
            btn.setOnMouseClicked(e -> actions[idx].run());
            HBox.setHgrow(btn, Priority.ALWAYS);
            bar.getChildren().add(btn);
            if (i < items.length - 1) {
                Region sep = new Region();
                sep.setPrefWidth(1);
                sep.setStyle("-fx-background-color: #3A2810;");
                bar.getChildren().add(sep);
            }
        }
        return bar;
    }

    private VBox buildQuickBtn(String icon, String label) {
        VBox btn = new VBox(3);
        btn.setAlignment(Pos.CENTER);
        btn.setPadding(new Insets(8, 4, 8, 4));
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setStyle("-fx-background-color: transparent;");

        Label icn = new Label(icon);
        icn.setStyle("-fx-text-fill: #6A5840; -fx-font-size: 16px;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #4A3820; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");

        btn.getChildren().addAll(icn, lbl);
        btn.setOnMouseEntered(e -> {
            icn.setStyle("-fx-text-fill: #FFB830; -fx-font-size: 16px;");
            lbl.setStyle("-fx-text-fill: #C8860A; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
            btn.setStyle("-fx-background-color: #C8860A11;");
        });
        btn.setOnMouseExited(e -> {
            icn.setStyle("-fx-text-fill: #6A5840; -fx-font-size: 16px;");
            lbl.setStyle("-fx-text-fill: #4A3820; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
            btn.setStyle("-fx-background-color: transparent;");
        });
        return btn;
    }

    // ── Helpers ───────────────────────────────────────────

    private String getDistrictName(int depth) {
        if (depth <= 0)  return "PENDOPO MARKAS";
        if (depth <= 3)  return "PASAR MALAM GAIB";
        if (depth <= 6)  return "CANDI TERLARANG";
        if (depth <= 10) return "HUTAN ANGKER";
        if (depth <= 15) return "GOA NAGA";
        return "KAHYANGAN RUSAK";
    }

    private long getMythicFragmentCount() {
        if (engine.getInventory() == null) return 0;
        return engine.getInventory().getAllBagItems().stream()
            .filter(i -> i instanceof arclightcity.item.Material m
                      && m.getMaterialType() == arclightcity.item.Material.MaterialType.MYTHIC_FRAGMENT)
            .count();
    }
}
