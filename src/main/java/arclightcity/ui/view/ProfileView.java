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

        // Custom header
        HBox hdr = new HBox(10);
        hdr.setPadding(new Insets(10, 16, 10, 16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-background-color: #0F0A06;" +
                     "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Button back = new Button("← MARKAS");
        back.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                      "-fx-border-width: 1; -fx-text-fill: #5A3A10;" +
                      "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                      "-fx-padding: 3 8; -fx-cursor: hand;");
        back.setOnAction(e -> router.showHub());

        Label titleLbl = new Label("☰  PROFIL PENDEKAR");
        titleLbl.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 14px; -fx-font-weight: bold;" +
                          "-fx-effect: dropshadow(gaussian, #C8860A, 6, 0.3, 0, 0);");
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        Label goldLbl = new Label("⚙ " + UIFactory.formatNumber(player.getGold()));
        goldLbl.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 12px; -fx-font-weight: bold;");
        hdr.getChildren().addAll(back, titleLbl, goldLbl);

        top.getChildren().add(hdr);
        top.getChildren().add(buildTabBar(player));
        root.setTop(top);

        // ── CENTER: tab content (scrollable) ──────────────────
        VBox content = switch (tab) {
            case "PERLENGKAPAN" -> buildEquipmentTab(player);
            case "JURUS"        -> buildSkillsTab(player);
            default             -> buildStatsTab(player);
        };

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        return root;
    }

    // ── Tab Bar ───────────────────────────────────────────────

    private HBox buildTabBar(Player player) {
        HBox tabBar = new HBox(0);
        tabBar.setStyle("-fx-background-color: #150E08;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        for (String tab : new String[]{"STATISTIK", "PERLENGKAPAN", "JURUS"}) {
            Button btn = new Button(tab);
            boolean active = tab.equals(activeTab);
            btn.setStyle(
                "-fx-background-color: " + (active ? "#C8860A11" : "transparent") + ";" +
                "-fx-text-fill: " + (active ? UIFactory.CYAN : UIFactory.DIM) + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 10 20;" +
                "-fx-border-color: transparent transparent " +
                    (active ? "#C8860A" : "transparent") + " transparent;" +
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
                "-fx-background-color: #FFB83033; -fx-text-fill: #FFB830;" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                "-fx-font-weight: bold; -fx-padding: 2 6;" +
                "-fx-border-color: #FFB83066; -fx-border-width: 1;"
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
        list.getChildren().add(sectionHeader("KARAKTER"));
        list.getChildren().add(UIFactory.statRow("Level",  String.valueOf(player.getLevel())));
        list.getChildren().add(UIFactory.statRow("EXP",
                fmt(player.getCurrentExp()) + " / " + fmt(player.getExpToNextLevel())));
        list.getChildren().add(UIFactory.statRow("Emas",       fmt(player.getGold())));
        list.getChildren().add(UIFactory.statRow("Asal Usul", player.getBackground().name));
        list.getChildren().add(UIFactory.statRow("Kedalaman Dungeon",
                String.valueOf(player.getDungeonDepth())));
        list.getChildren().add(UIFactory.statRow("Poin Jurus",
                String.valueOf(player.getSkillPoints())));

        // Vitals
        list.getChildren().add(sectionHeader("VITAL"));
        list.getChildren().add(UIFactory.statRowHighlight("HP",
                fmtStat(player, StatType.MAX_HP), UIFactory.RED));
        list.getChildren().add(UIFactory.statRowHighlight("Shield",
                fmtStat(player, StatType.MAX_SHIELD), UIFactory.PURPLE));
        list.getChildren().add(UIFactory.statRow("Regenerasi HP",
                fmtStat(player, StatType.HP_REGEN) + "/turn"));
        list.getChildren().add(UIFactory.statRow("Regenerasi Tameng",
                fmtStat(player, StatType.SHIELD_REGEN) + "/turn"));
        list.getChildren().add(UIFactory.statRowHighlight("Energi (MP)",
                fmtStat(player, StatType.MAX_MP), UIFactory.CYAN));
        list.getChildren().add(UIFactory.statRow("Regenerasi Energi",
                fmtStat(player, StatType.MP_REGEN) + "/turn"));

        // Offense
        list.getChildren().add(sectionHeader("SERANGAN"));
        list.getChildren().add(UIFactory.statRowHighlight("ATK Fisik",
                fmtStat(player, StatType.PHYSICAL_ATK), "#FF6B6B"));
        list.getChildren().add(UIFactory.statRowHighlight("ATK Santet",
                fmtStat(player, StatType.CYBER_ATK), UIFactory.CYAN));
        list.getChildren().add(UIFactory.statRowHighlight("ATK Energi",
                fmtStat(player, StatType.ENERGY_ATK), UIFactory.PURPLE));
        list.getChildren().add(UIFactory.statRow("Kekuatan Jurus",
                fmtPct(player, StatType.SKILL_POWER)));
        list.getChildren().add(UIFactory.statRow("Peluang Kritis",
                fmtPct(player, StatType.CRIT_CHANCE)));
        list.getChildren().add(UIFactory.statRow("DMG Kritis",
                fmtPct(player, StatType.CRIT_DAMAGE)));
        list.getChildren().add(UIFactory.statRow("Tembus Baju Besi",
                fmtPct(player, StatType.ARMOR_PIERCE)));

        // Defense
        list.getChildren().add(sectionHeader("PERTAHANAN"));
        list.getChildren().add(UIFactory.statRow("DEF Fisik",
                fmtStat(player, StatType.PHYSICAL_DEF)));
        list.getChildren().add(UIFactory.statRow("DEF Santet",
                fmtStat(player, StatType.CYBER_DEF)));
        list.getChildren().add(UIFactory.statRow("DEF Energi",
                fmtStat(player, StatType.ENERGY_DEF)));
        list.getChildren().add(UIFactory.statRow("Menghindar",
                fmtPct(player, StatType.EVASION)));
        list.getChildren().add(UIFactory.statRow("Peluang Blokir",
                fmtPct(player, StatType.BLOCK_CHANCE)));

        // Utility
        list.getChildren().add(sectionHeader("UTILITAS"));
        list.getChildren().add(UIFactory.statRow("Kecepatan",
                fmtStat(player, StatType.SPEED)));
        list.getChildren().add(UIFactory.statRow("Inisiatif",
                fmtStat(player, StatType.INITIATIVE)));
        list.getChildren().add(UIFactory.statRow("Curi Darah",
                fmtPct(player, StatType.LIFESTEAL)));
        list.getChildren().add(UIFactory.statRow("Pengurangan CD",
                fmtPct(player, StatType.COOLDOWN_REDUCE)));
        list.getChildren().add(UIFactory.statRow("Akurasi",
                fmtPct(player, StatType.ACCURACY)));
        list.getChildren().add(UIFactory.statRow("Sinkronisasi",
                fmtPct(player, StatType.SYNC_RATE)));
        list.getChildren().add(UIFactory.statRow("Pengganda DMG",
                fmtPct(player, StatType.DAMAGE_MULT)));

        return list;
    }

    // ── EQUIPMENT Tab ─────────────────────────────────────────

    private VBox buildEquipmentTab(Player player) {
        VBox list = new VBox(0);
        list.getChildren().add(buildIdCard(player));

        Inventory inv = engine.getInventory();
        list.getChildren().add(sectionHeader("PERLENGKAPAN AKTIF"));
        list.getChildren().add(buildEquipSlot("⚔  SENJATA",    inv.getEquippedWeapon(),       inv));
        list.getChildren().add(buildEquipSlot("🛡  BAJU BESI",  inv.getEquippedArmor(),        inv));
        list.getChildren().add(buildEquipSlot("👑  HELM",       inv.getEquippedHelmet(),       inv));
        list.getChildren().add(buildEquipSlot("👢  SEPATU",     inv.getEquippedBoots(),        inv));
        list.getChildren().add(buildEquipSlot("💍  CINCIN 1",   inv.getEquippedRing1(),        inv));
        list.getChildren().add(buildEquipSlot("💍  CINCIN 2",   inv.getEquippedRing2(),        inv));
        list.getChildren().add(buildEquipSlot("◈  AKSESORI 1", inv.getEquippedAccessory1(),   inv));
        list.getChildren().add(buildEquipSlot("◈  AKSESORI 2", inv.getEquippedAccessory2(),   inv));
        list.getChildren().add(sectionHeader("PERLENGKAPAN DI TAS"));
        if (inv.getEquipmentInBag().isEmpty()) {
            Label empty = new Label("  No equipment in bag.");
            empty.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 11px; -fx-padding: 8 16;");
            list.getChildren().add(empty);
        } else {
            for (Equipment eq : inv.getEquipmentInBag()) {
                list.getChildren().add(buildEquipRow(eq, inv));
            }
        }

        return list;
    }

    private VBox buildEquipSlot(String slotName, Equipment eq, Inventory inv) {
        VBox slot = new VBox(4);
        slot.setPadding(new Insets(8, 16, 8, 16));
        slot.setStyle("-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Label nameLabel = new Label(slotName);
        nameLabel.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 10px; -fx-font-weight: bold;");
        slot.getChildren().add(nameLabel);

        if (eq == null) {
            Label empty = new Label("— Kosong —");
            empty.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 11px;");
            slot.getChildren().add(empty);
        } else {
            slot.getChildren().add(buildEquipRow(eq, inv));
        }
        return slot;
    }

    private HBox buildEquipRow(Equipment eq, Inventory inv) {
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
        statLabel.setStyle("-fx-text-fill: #A09070; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 10px;");

        info.getChildren().addAll(nameRow, statLabel);
        row.getChildren().add(info);

        // Lepas button
        javafx.scene.control.Button lepasBtn = new javafx.scene.control.Button("LEPAS");
        lepasBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #CC3300;" +
                          "-fx-border-width: 1; -fx-text-fill: #FF5533;" +
                          "-fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-padding: 4 8; -fx-cursor: hand;");
        lepasBtn.setOnAction(e -> {
            inv.unequip(eq);
            router.showProfile("PERLENGKAPAN"); // refresh immediately
        });
        row.getChildren().add(lepasBtn);
        return row;
    }

    // ── SKILLS Tab ────────────────────────────────────────────

    private VBox buildSkillsTab(Player player) {
        VBox list = new VBox(0);
        list.getChildren().add(buildIdCard(player));

        // SP Banner
        int sp = player.getSkillPoints();
        HBox spBanner = new HBox(10);
        spBanner.setPadding(new Insets(6, 12, 6, 12));
        spBanner.setAlignment(Pos.CENTER_LEFT);
        spBanner.setStyle(
            "-fx-background-color: " + (sp > 0 ? "#FFB83011" : "#1A1008") + ";" +
            "-fx-border-color: " + (sp > 0 ? "#FFB83055" : "#3A2810") + ";" +
            "-fx-border-width: 0 0 1 0;"
        );

        Label spLabel = new Label(sp > 0
            ? "✦ " + sp + " Poin Jurus tersedia!"
            : "Selesaikan dungeon untuk mendapat Poin Jurus.");
        spLabel.setStyle(
            "-fx-text-fill: " + (sp > 0 ? "#FFB830" : "#5A3A10") + ";" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;"
        );
        HBox.setHgrow(spLabel, Priority.ALWAYS);

        Button treeBtn = new Button("🌳  BUKA POHON JURUS  ▶");
        treeBtn.setStyle(
            "-fx-background-color: #C8860A22; -fx-border-color: #C8860A;" +
            "-fx-border-width: 1; -fx-text-fill: #FFB830;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
            "-fx-font-weight: bold; -fx-padding: 7 14; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 6, 0.3, 0, 0);"
        );
        treeBtn.setOnAction(e -> router.showSkillTree());
        treeBtn.setOnMouseEntered(ev -> treeBtn.setStyle(
            "-fx-background-color: #C8860A44; -fx-border-color: #FFB830;" +
            "-fx-border-width: 1; -fx-text-fill: #FFB830;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
            "-fx-font-weight: bold; -fx-padding: 7 14; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #FFB830, 10, 0.5, 0, 0);"
        ));
        treeBtn.setOnMouseExited(ev -> treeBtn.setStyle(
            "-fx-background-color: #C8860A22; -fx-border-color: #C8860A;" +
            "-fx-border-width: 1; -fx-text-fill: #FFB830;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
            "-fx-font-weight: bold; -fx-padding: 7 14; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 6, 0.3, 0, 0);"
        ));
        spBanner.getChildren().addAll(spLabel, treeBtn);
        list.getChildren().add(spBanner);

        // Jurus Aktif
        list.getChildren().add(sectionHeader("◈ JURUS AKTIF"));
        var equippedSkills = player.getEquippedSkillIds();
        if (equippedSkills.isEmpty()) {
            Label none = new Label("  Belum ada jurus aktif. Buka dari Pohon Jurus.");
            none.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-padding: 8 16;");
            list.getChildren().add(none);
        } else {
            for (String skillId : equippedSkills) {
                list.getChildren().add(buildActiveSkillCard(skillId, player));
            }
        }

        // Jurus Terbuka (tidak aktif)
        var unlocked = player.getUnlockedSkillIds().stream()
            .filter(id -> !equippedSkills.contains(id))
            .toList();
        if (!unlocked.isEmpty()) {
            list.getChildren().add(sectionHeader("◈ JURUS TERBUKA (TIDAK AKTIF)"));
            for (String skillId : unlocked) {
                list.getChildren().add(buildUnequippedSkillCard(skillId, player));
            }
        }

        return list;
    }

    private HBox buildActiveSkillCard(String skillId, Player player) {
        String name = skillDisplayName(skillId);
        String desc = skillDescription(skillId);
        boolean isAoE = desc.contains("semua") || desc.contains("AoE");

        HBox row = new HBox(10);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #C8860A0D; -fx-border-color: #C8860A33;" +
                     "-fx-border-width: 0 0 1 0;");

        // Status indicator
        VBox indicator = new VBox();
        indicator.setPrefWidth(4);
        indicator.setMinWidth(4);
        indicator.setStyle("-fx-background-color: #C8860A;");

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox nameRow = new HBox(8);
        Label nameLbl = new Label("⚔  " + name);
        nameLbl.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 12px; -fx-font-weight: bold;");
        if (isAoE) {
            Label aoeLbl = new Label("AoE");
            aoeLbl.setStyle("-fx-background-color: #7755BB22; -fx-border-color: #7755BB;" +
                            "-fx-border-width: 1; -fx-text-fill: #7755BB;" +
                            "-fx-font-family: 'Courier New'; -fx-font-size: 9px; -fx-padding: 1 5;");
            nameRow.getChildren().addAll(nameLbl, aoeLbl);
        } else {
            nameRow.getChildren().add(nameLbl);
        }

        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        descLbl.setWrapText(true);

        Label statusLbl = new Label("✓ AKTIF — READY");
        statusLbl.setStyle("-fx-text-fill: #2D7A45; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");

        info.getChildren().addAll(nameRow, descLbl, statusLbl);

        // Lepas button
        Button lepas = new Button("LEPAS");
        lepas.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                       "-fx-border-width: 1; -fx-text-fill: #5A3A10;" +
                       "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                       "-fx-padding: 4 8; -fx-cursor: hand;");
        lepas.setOnAction(e -> {
            player.unequipSkill(skillId);
            router.showProfile("JURUS");
        });

        row.getChildren().addAll(indicator, info, lepas);
        return row;
    }

    private HBox buildUnequippedSkillCard(String skillId, Player player) {
        String name = skillDisplayName(skillId);
        String desc = skillDescription(skillId);

        HBox row = new HBox(10);
        row.setPadding(new Insets(8, 16, 8, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: transparent; -fx-border-color: #2A1808;" +
                     "-fx-border-width: 0 0 1 0;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLbl = new Label("◈  " + name);
        nameLbl.setStyle("-fx-text-fill: #A09070; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 11px;");
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-text-fill: #4A3820; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        info.getChildren().addAll(nameLbl, descLbl);

        boolean canEquip = player.getEquippedSkillCount() < 4;
        Button equip = new Button("AKTIFKAN");
        equip.setDisable(!canEquip);
        equip.setStyle("-fx-background-color: transparent; -fx-border-color: " +
                       (canEquip ? "#C8860A" : "#3A2810") + ";" +
                       "-fx-border-width: 1; -fx-text-fill: " +
                       (canEquip ? "#C8860A" : "#3A2810") + ";" +
                       "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                       "-fx-padding: 4 8; -fx-cursor: " + (canEquip ? "hand" : "default") + ";");
        equip.setOnAction(e -> {
            player.equipSkill(skillId);
            router.showProfile("JURUS");
        });

        row.getChildren().addAll(info, equip);
        return row;
    }

    private String skillDisplayName(String id) {
        return switch (id) {
            case "POWER_STRIKE"       -> "Pukulan Harimau";
            case "TEBASAN"            -> "Tebasan Pamungkas";
            case "SHOCKWAVE"          -> "Gempa Bumi";
            case "SOVEREIGN_STRIKE"   -> "Tebasan Agung";
            case "PHANTOM_SHOT"       -> "Panah Bayangan";
            case "SHADOW_STEP"        -> "Langkah Gaib";
            case "NULL_FIELD"         -> "Bidang Bayangan";
            case "NULL_PROTOCOL"      -> "Protokol Nol";
            case "IRON_SHIELD"        -> "Tameng Baja";
            case "FIELD_BARRIER"      -> "Rajah Pelindung";
            case "ENERGY_DRAIN"       -> "Serap Tenaga";
            case "DATA_FRAGMENTATION" -> "Pecah Jiwa";
            default -> id.replace("_", " ");
        };
    }

    private String skillDescription(String id) {
        return switch (id) {
            case "POWER_STRIKE"       -> "Pukulan keras: 1.8× ATK fisik.";
            case "TEBASAN"            -> "Instant kill jika HP < 25%, else 1.5× damage.";
            case "SHOCKWAVE"          -> "AoE: serang semua musuh sekaligus.";
            case "SOVEREIGN_STRIKE"   -> "Ultimate: 3× ATK, abaikan semua armor.";
            case "PHANTOM_SHOT"       -> "Crit chance tinggi, masuk sembunyi.";
            case "SHADOW_STEP"        -> "2× crit damage, abaikan armor.";
            case "NULL_FIELD"         -> "Hapus semua buff musuh + evasion +30%.";
            case "NULL_PROTOCOL"      -> "AoE Void: semua musuh -50% DEF 3 giliran.";
            case "IRON_SHIELD"        -> "Buff BLOK 2 giliran, kurangi damage drastis.";
            case "FIELD_BARRIER"      -> "Shield barrier ke semua sekutu.";
            case "ENERGY_DRAIN"       -> "Sedot MP musuh, pulihkan HP+MP sendiri.";
            case "DATA_FRAGMENTATION" -> "AoE besar + stun 1 giliran.";
            default -> "Jurus khusus.";
        };
    }



    private HBox buildSkillRow(Player player, String skillId, boolean isEquipped) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(8, 16, 8, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #C8860A0A; -fx-border-color: #C8860A33;" +
                     "-fx-border-width: 0 0 1 3;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        String dispName = skillId.replace("_", " ");
        Label name = new Label(dispName);
        name.setStyle("-fx-text-fill: #C8860A; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 12px; -fx-font-weight: bold;");

        int cd = player.getSkillCooldown(skillId);
        Label statusLabel = new Label(isEquipped ? "AKTIF" + (cd > 0 ? " — CD: " + cd : " — READY") : "TERBUKA");
        statusLabel.setStyle("-fx-text-fill: " + (isEquipped ? UIFactory.GREEN : "#6A5840") +
                             "; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        info.getChildren().addAll(name, statusLabel);
        row.getChildren().add(info);

        // Equip/Unequip button
        if (isEquipped) {
            Button unequip = new Button("LEPAS");
            unequip.setStyle("-fx-background-color: transparent; -fx-border-color: #6A5840;" +
                             "-fx-border-width: 1; -fx-text-fill: #6A5840;" +
                             "-fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-cursor: hand;");
            unequip.setOnAction(e -> {
                player.unequipSkill(skillId);
                router.showProfile("JURUS");
            });
            row.getChildren().add(unequip);
        } else {
            Button equip = new Button("PASANG");
            equip.setStyle("-fx-background-color: #C8860A11; -fx-border-color: #C8860A;" +
                           "-fx-border-width: 1; -fx-text-fill: #C8860A;" +
                           "-fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-cursor: hand;");
            equip.setOnAction(e -> {
                player.equipSkill(skillId);
                router.showProfile("JURUS");
            });
            row.getChildren().add(equip);
        }

        return row;
    }

    private HBox buildLockedSkillRow(Player player, String skillId) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(8, 16, 8, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #150E08; -fx-border-color: #3A2810;" +
                     "-fx-border-width: 0 0 1 0;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        String dispName = skillId.replace("_", " ");
        Label name = new Label("🔒 " + dispName);
        name.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 12px;");
        Label cost = new Label("Biaya: 1 Poin Jurus");
        cost.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 10px;");
        info.getChildren().addAll(name, cost);
        row.getChildren().add(info);

        boolean canUnlock = player.getSkillPoints() > 0;
        Button unlock = new Button("BUKA");
        unlock.setDisable(!canUnlock);
        unlock.setStyle("-fx-background-color: " + (canUnlock ? "#FFB83011" : "transparent") + ";" +
                        "-fx-border-color: " + (canUnlock ? "#FFB830" : "#3A2810") + ";" +
                        "-fx-border-width: 1; -fx-text-fill: " + (canUnlock ? "#FFB830" : "#3A2810") + ";" +
                        "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                        "-fx-cursor: " + (canUnlock ? "hand" : "default") + ";");
        unlock.setOnAction(e -> {
            if (player.unlockSkill(skillId)) {
                router.showProfile("JURUS");
            }
        });
        row.getChildren().add(unlock);

        return row;
    }

    // ── ID Card ───────────────────────────────────────────────

    private VBox buildIdCard(Player player) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle("-fx-background-color: #150E08;" +
                      "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        // Name + avatar
        HBox nameRow = new HBox(12);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label("◈");
        avatar.setStyle("-fx-text-fill: #C8860A; -fx-font-size: 28px;");

        VBox nameInfo = new VBox(2);
        Label name = new Label(player.getName().toUpperCase());
        name.setStyle("-fx-text-fill: #C8860A; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 18px; -fx-font-weight: bold;");
        Label bgLabel = new Label(player.getBackground().name + "  •  LV." + player.getLevel());
        bgLabel.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New';" +
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
        expBar.setStyle("-fx-accent: #FFB830; -fx-background-color: #FFB83022;" +
                        "-fx-min-height: 6px; -fx-max-height: 6px;");
        Label expLabel = new Label("EXP  " + fmt(player.getCurrentExp()) +
                                   " / " + fmt(player.getExpToNextLevel()));
        expLabel.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New';" +
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
        l.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New';" +
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
