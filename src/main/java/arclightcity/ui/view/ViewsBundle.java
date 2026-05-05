package arclightcity.ui.view;

import arclightcity.combat.CombatResult;
import arclightcity.dungeon.DungeonEvent;
import arclightcity.engine.GameEngine;
import arclightcity.entity.mercenary.Mercenary;
import arclightcity.entity.mercenary.MercenaryType;
import arclightcity.entity.EntityFactory;
import arclightcity.entity.stats.StatType;
import arclightcity.item.*;
import arclightcity.item.Equipment;
import arclightcity.item.Consumable;
import arclightcity.item.Material;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;

public class ViewsBundle {


// ═══════════════════════════════════════════════════════════
// INVENTORY VIEW
// ═══════════════════════════════════════════════════════════

public static class InventoryViewImpl {

    private final GameEngine  engine;
    private final SceneRouter router;
    private VBox              itemListContainer;
    private String            activeFilter = "ALL";

    InventoryViewImpl(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    Parent build() {
        Inventory inv = engine.getInventory();
        BorderPane root = UIFactory.screenRootBorder();

        // ── TOP: header + equipment slots + material bar ──
        VBox topSection = new VBox(0);

        // Header
        HBox header = new HBox(10);
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #0F0A06;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Button back = new Button("← MARKAS");
        back.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                      "-fx-border-width: 1; -fx-text-fill: #5A3A10;" +
                      "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                      "-fx-padding: 3 8; -fx-cursor: hand;");
        back.setOnAction(e -> router.showHub());

        Label title = new Label("⊞  PERBENDAHARAAN");
        title.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 14px; -fx-font-weight: bold;" +
                       "-fx-effect: dropshadow(gaussian, #C8860A, 6, 0.3, 0, 0);");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label goldLbl = new Label("⚙ " + UIFactory.formatNumber(engine.getPlayer().getGold()));
        goldLbl.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 12px; -fx-font-weight: bold;");
        header.getChildren().addAll(back, title, goldLbl);

        topSection.getChildren().add(header);
        topSection.getChildren().add(buildEquipmentSlots(inv));
        topSection.getChildren().add(buildMaterialBar(inv));
        topSection.getChildren().add(buildFilterTabs());
        root.setTop(topSection);

        // ── CENTER: item list ──────────────────────────────
        itemListContainer = new VBox(0);
        refreshItemList(inv);

        ScrollPane scroll = new ScrollPane(itemListContainer);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        UIFactory.fadeIn(root, 300);
        return root;
    }

    private HBox buildEquipmentSlots(Inventory inv) {
        HBox bar = new HBox(6);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setStyle("-fx-background-color: #150E08; -fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        // Ambil equipment object langsung agar bisa cek rarity warna
        Equipment[] equips = {
            inv.getEquippedWeapon(),
            inv.getEquippedArmor(),
            inv.getEquippedAccessory1(),
            inv.getEquippedAccessory2()
        };
        String[] slotLabels = { "WPN", "ARM", "ACC", "ACC" };

        for (int i = 0; i < equips.length; i++) {
            Equipment eq = equips[i];
            String label = slotLabels[i];

            VBox slotBox = new VBox(2);
            slotBox.setAlignment(Pos.CENTER);
            slotBox.setPrefWidth(80);

            if (eq == null) {
                // Slot kosong — style gelap polos
                slotBox.setStyle(
                    "-fx-background-color: #0F0A06;" +
                    "-fx-border-color: #3A281066;" +
                    "-fx-border-width: 1;" +
                    "-fx-padding: 6;"
                );
                Label slotType = new Label(label);
                slotType.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
                Label empty = new Label("—");
                empty.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
                slotBox.getChildren().addAll(slotType, empty);
            } else {
                // Slot terisi — border warna sesuai rarity
                String rarityColor = UIFactory.rarityColor(eq.getRarity());
                slotBox.setStyle(
                    "-fx-background-color: " + rarityColor + "11;" +
                    "-fx-border-color: " + rarityColor + ";" +
                    "-fx-border-width: 1 1 1 3;" + // thick left border = rarity indicator
                    "-fx-padding: 6;"
                );
                Label slotType = new Label(label);
                slotType.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
                String displayName = eq.getName().length() > 9
                        ? eq.getName().substring(0, 8) + "…"
                        : eq.getName();
                Label itemName = new Label(displayName);
                itemName.setStyle(
                    "-fx-text-fill: " + rarityColor + ";" +
                    "-fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;"
                );
                // Upgrade level badge
                if (eq.getUpgradeLevel() > 0) {
                    Label upgLabel = new Label("+" + eq.getUpgradeLevel());
                    upgLabel.setStyle(
                        "-fx-text-fill: " + UIFactory.YELLOW + ";" +
                        "-fx-font-family: 'Courier New'; -fx-font-size: 10px;"
                    );
                    slotBox.getChildren().addAll(slotType, itemName, upgLabel);
                } else {
                    slotBox.getChildren().addAll(slotType, itemName);
                }
            }

            bar.getChildren().add(slotBox);
        }

        return bar;
    }

    private HBox buildMaterialBar(Inventory inv) {
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setStyle("-fx-background-color: #0F0A06; -fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");
        bar.setAlignment(Pos.CENTER_LEFT);

        String[][] mats = {
            {"⚙ Scrap",    String.valueOf(inv.getScrapMetal())},
            {"◈ Chips",    String.valueOf(inv.getCyberChips())},
            {"◆ Crystal",  String.valueOf(inv.getNeonCrystals())},
            {"✦ Cal.Kit",  String.valueOf(inv.getCalibrationKits())},
        };

        for (String[] mat : mats) {
            VBox m = new VBox(0);
            m.setAlignment(Pos.CENTER);
            Label name = new Label(mat[0]);
            name.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
            Label val = new Label(mat[1]);
            val.setStyle("-fx-text-fill: " + UIFactory.CYAN + "; -fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;");
            m.getChildren().addAll(name, val);
            bar.getChildren().add(m);
        }

        Label bagInfo = new Label("BAG " + inv.getBagSize() + "/" + inv.getMaxBagSize());
        bagInfo.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        HBox.setHgrow(new Region(), Priority.ALWAYS);
        bar.getChildren().addAll(new Region(), bagInfo);
        HBox.setHgrow(bar.getChildren().get(bar.getChildren().size() - 2), Priority.ALWAYS);

        return bar;
    }

    private HBox buildFilterTabs() {
        HBox tabs = new HBox(0);
        tabs.setStyle("-fx-background-color: #150E08; -fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        String[] filters = {"ALL", "WEAPON", "ARMOR", "ACCESSORY", "CONSUMABLE", "MATERIAL"};
        for (String filter : filters) {
            Label tab = new Label(filter);
            tab.setPadding(new Insets(8, 12, 8, 12));
            boolean active = filter.equals(activeFilter);
            tab.setStyle(
                "-fx-text-fill: " + (active ? UIFactory.CYAN : UIFactory.DIM) + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
                (active ? "-fx-border-color: transparent transparent #C8860A transparent; -fx-border-width: 0 0 2 0;" : "")
            );
            tab.setCursor(javafx.scene.Cursor.HAND);
            tab.setOnMouseClicked(e -> {
                activeFilter = filter;
                refreshItemList(engine.getInventory());
            });
            tabs.getChildren().add(tab);
        }

        return tabs;
    }

    private void refreshItemList(Inventory inv) {
        itemListContainer.getChildren().clear();

        var items = inv.getAllItems().stream()
                .filter(item -> {
                    if (activeFilter.equals("ALL")) return true;
                    return item.getItemType().name().equals(activeFilter);
                })
                .toList();

        if (items.isEmpty()) {
            Label empty = new Label("No items");
            empty.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-padding: 20;");
            itemListContainer.getChildren().add(empty);
            return;
        }

        for (Item item : items) {
            itemListContainer.getChildren().add(buildItemRow(item, inv));
        }
    }

    private HBox buildItemRow(Item item, Inventory inv) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setCursor(javafx.scene.Cursor.HAND);

        // Default: gelap dengan separator bawah
        String styleNormal = "-fx-background-color: #0A0604;" +
                             "-fx-border-color: transparent transparent #150E08 transparent;" +
                             "-fx-border-width: 0 0 1 0;";
        String styleHover  = "-fx-background-color: #150E08;" +
                             "-fx-border-color: transparent transparent #3A2810 transparent;" +
                             "-fx-border-width: 0 0 1 0;";
        row.setStyle(styleNormal);
        row.setOnMouseEntered(e -> row.setStyle(styleHover));
        row.setOnMouseExited(e -> row.setStyle(styleNormal));

        // Rarity indicator
        VBox rarityBar = new VBox();
        rarityBar.setPrefWidth(3);
        rarityBar.setMinWidth(3);
        boolean isMythic = item.getRarity() == Item.Rarity.MYTHIC;
        rarityBar.setStyle("-fx-background-color: " + UIFactory.rarityColor(item.getRarity()) + ";");

        // Item info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Name + rarity tag
        HBox nameRow = new HBox(6);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(item.getFullName());
        nameLabel.setStyle("-fx-text-fill: " + UIFactory.rarityColor(item.getRarity()) +
                "; -fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;" +
                (isMythic ? " -fx-effect: dropshadow(gaussian, #FF6B00, 8, 0.5, 0, 0);" : ""));
        Label rarityTag = new Label(isMythic ? "✦ MYTHIC ✦" : "[" + item.getRarity().displayName + "]");
        rarityTag.setStyle("-fx-text-fill: " + UIFactory.rarityColor(item.getRarity()) +
                (isMythic ? "; -fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;" +
                            " -fx-effect: dropshadow(gaussian, #FF6B00, 6, 0.4, 0, 0);"
                          : "88; -fx-font-family: 'Courier New'; -fx-font-size: 11px;"));
        nameRow.getChildren().addAll(nameLabel, rarityTag);

        // Stat summary (ambil 2 stat utama)
        String statSummary = buildStatSummary(item);
        Label descLabel = new Label(statSummary);
        descLabel.setStyle("-fx-text-fill: #A09070; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        descLabel.setWrapText(true);

        info.getChildren().addAll(nameRow, descLabel);

        // Action buttons
        VBox actions = new VBox(4);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMinWidth(80);

        if (item instanceof Equipment eq) {
            // EQUIP button
            Button equipBtn = new Button("PAKAI");
            equipBtn.setStyle("-fx-background-color: #C8860A15; -fx-border-color: #C8860A55;" +
                    " -fx-border-width: 1; -fx-text-fill: #C8860A;" +
                    " -fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                    " -fx-padding: 3 6; -fx-cursor: hand;");
            equipBtn.setOnAction(e -> {
                var result = inv.equip(eq);
                showAlert(result.success ? "✅ " + result.message : "❌ " + result.message);
                refreshItemList(inv); // refresh setelah equip
            });

            // UPGRADE button
            Button upgradeBtn = new Button("+UPG");
            boolean canUpgrade = eq.canUpgrade();
            upgradeBtn.setStyle("-fx-background-color: transparent;" +
                    " -fx-border-color: " + (canUpgrade ? "#FFB83066" : "#6A584055") + ";" +
                    " -fx-border-width: 1;" +
                    " -fx-text-fill: " + (canUpgrade ? "#FFB830" : "#6A5840") + ";" +
                    " -fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                    " -fx-padding: 3 6; -fx-cursor: " + (canUpgrade ? "hand" : "default") + ";");
            upgradeBtn.setDisable(!canUpgrade);
            upgradeBtn.setOnAction(e -> {
                var result = inv.upgradeItem(item.getId());
                showAlert(result.message);
                if (result.success) refreshItemList(inv);
            });

            // CALIBRATE button
            Button calibBtn = new Button("CAL");
            boolean hasKit = inv.getCalibrationKits() > 0;
            calibBtn.setStyle("-fx-background-color: transparent;" +
                    " -fx-border-color: " + (hasKit ? "#7755BB66" : "#6A584055") + ";" +
                    " -fx-border-width: 1;" +
                    " -fx-text-fill: " + (hasKit ? "#7755BB" : "#6A5840") + ";" +
                    " -fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                    " -fx-padding: 3 6; -fx-cursor: " + (hasKit ? "hand" : "default") + ";");
            calibBtn.setDisable(!hasKit);
            calibBtn.setOnAction(e -> {
                var result = inv.calibrateItem(item.getId(), 1);
                showAlert(result.message);
                if (result.success) refreshItemList(inv);
            });

            actions.getChildren().addAll(equipBtn, upgradeBtn, calibBtn);

        } else if (item instanceof arclightcity.item.Consumable cons) {
            Label stackLabel = new Label("x" + cons.getStackCount());
            stackLabel.setStyle("-fx-text-fill: #2D7A45; -fx-font-family: 'Courier New';" +
                    " -fx-font-size: 11px; -fx-font-weight: bold;");
            Button useBtn = new Button("GUNAKAN");
            useBtn.setStyle("-fx-background-color: #2D7A4515; -fx-border-color: #2D7A4555;" +
                    " -fx-border-width: 1; -fx-text-fill: #2D7A45;" +
                    " -fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                    " -fx-padding: 3 6; -fx-cursor: hand;");
            useBtn.setOnAction(e -> {
                boolean used = inv.useConsumable(item.getId(), engine.getPlayer());
                if (used) {
                    showAlert("✅ Used: " + item.getName());
                    refreshItemList(inv);
                }
            });
            actions.getChildren().addAll(stackLabel, useBtn);
        }

        row.getChildren().addAll(rarityBar, info, actions);
        return row;
    }

    /** Ambil ringkasan 2-3 stat utama dari item untuk ditampilkan di list */
    private String buildStatSummary(Item item) {
        if (item instanceof Equipment eq) {
            StringBuilder sb = new StringBuilder();
            eq.getStatBonuses().entrySet().stream()
                .limit(3)
                .forEach(entry -> {
                    String name = entry.getKey().displayName;
                    double val  = entry.getValue();
                    // Format persentase untuk stat yang memang dalam bentuk 0.0-1.0
                    boolean isPct = entry.getKey().name().contains("CHANCE") ||
                                    entry.getKey().name().contains("EVASION") ||
                                    entry.getKey().name().contains("PIERCE") ||
                                    entry.getKey().name().contains("MULT")   ||
                                    entry.getKey().name().contains("LIFESTEAL");
                    sb.append(name).append(": +")
                      .append(isPct ? String.format("%.0f%%", val * 100) : String.format("%.0f", val))
                      .append("  ");
                });
            return sb.length() > 0 ? sb.toString().trim() : item.getDescription();
        }
        if (item instanceof arclightcity.item.Consumable cons) {
            return cons.getConsumableType().name().replace("_", " ") +
                   " • Effect: " + (int)cons.getEffectValue();
        }
        if (item instanceof arclightcity.item.Material mat) {
            return mat.getMaterialType().name().replace("_", " ") +
                   " x" + mat.getQuantity();
        }
        return item.getDescription();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.getDialogPane().setStyle("-fx-background-color: #150E08; -fx-font-family: 'Courier New';");
        alert.showAndWait();
    }
}

// ═══════════════════════════════════════════════════════════
// MERCENARY VIEW
// ═══════════════════════════════════════════════════════════

public static class MercenaryViewImpl {

    private final GameEngine  engine;
    private final SceneRouter router;
    private String activeTab = "REGU";

    MercenaryViewImpl(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    Parent build() {
        BorderPane root = UIFactory.screenRootBorder();

        VBox top = new VBox(0);
        top.getChildren().add(UIFactory.headerWithResources(
                "MERCENARY", () -> router.showHub(),
                engine.getPlayer().getGold(), 0));

        HBox tabBar = new HBox(0);
        tabBar.setStyle("-fx-background-color: #150E08;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");
        for (String tab : new String[]{"REGU", "REKRUT"}) {
            Button btn = new Button(tab);
            boolean active = tab.equals(activeTab);
            btn.setStyle(
                "-fx-background-color: " + (active ? "#C8860A11" : "transparent") + ";" +
                "-fx-text-fill: " + (active ? UIFactory.CYAN : UIFactory.DIM) + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 10 24;" +
                "-fx-border-color: transparent transparent " +
                    (active ? "#C8860A" : "transparent") + " transparent;" +
                "-fx-border-width: 0 0 2 0; -fx-cursor: hand;"
            );
            final String t = tab;
            btn.setOnAction(e -> { activeTab = t; root.setCenter(buildCenter()); });
            tabBar.getChildren().add(btn);
        }
        Label crewBadge = new Label("  CREW: " + engine.getActiveMercs().size() + "/2  ");
        crewBadge.setStyle("-fx-background-color: #C8860A11; -fx-text-fill: #C8860A;" +
                           "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                           "-fx-padding: 2 8; -fx-border-color: #C8860A44; -fx-border-width: 1;");
        HBox.setMargin(crewBadge, new Insets(8, 8, 8, 8));
        tabBar.getChildren().add(crewBadge);
        top.getChildren().add(tabBar);
        root.setTop(top);
        root.setCenter(buildCenter());
        UIFactory.fadeIn(root, 300);
        return root;
    }

    private ScrollPane buildCenter() {
        VBox content = activeTab.equals("REKRUT") ? buildHireTab() : buildRosterTab();
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        return scroll;
    }

    private VBox buildRosterTab() {
        VBox list = new VBox(8);
        list.setPadding(new Insets(10, 16, 10, 16));
        var owned = engine.getOwnedMercs();
        if (owned.isEmpty()) {
            Label none = new Label("No mercenaries hired yet.\nVisit HIRE tab to recruit crew.");
            none.setWrapText(true);
            none.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 11px; -fx-padding: 20;");
            list.getChildren().add(none);
        } else {
            for (Mercenary merc : owned) list.getChildren().add(buildMercCard(merc));
        }
        return list;
    }

    private VBox buildMercCard(Mercenary merc) {
        boolean isActive = engine.getActiveMercs().contains(merc);
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: " + (isActive ? "#0A180A" : "#150E08") + ";" +
                      "-fx-border-color: " + (isActive ? UIFactory.GREEN : "#3A2810") + ";" +
                      "-fx-border-width: 1 1 1 3;");
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(merc.getMercenaryType().displayName.toUpperCase());
        nameLabel.setStyle("-fx-text-fill: " + (isActive ? UIFactory.GREEN : UIFactory.TEXT) +
                           "; -fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        Label roleLabel = new Label("[" + merc.getRole().name() + "]");
        roleLabel.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        Label loyaltyLabel = new Label("♥ " + merc.getLoyaltyTitle());
        loyaltyLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        header.getChildren().addAll(nameLabel, roleLabel, loyaltyLabel);
        Label subtitle = new Label(merc.getMercenaryType().subtitle);
        subtitle.setStyle("-fx-text-fill: #A09070; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
            miniStat("HP",  String.valueOf((int)merc.getStats().get(StatType.MAX_HP))),
            miniStat("SHD", String.valueOf((int)merc.getStats().get(StatType.MAX_SHIELD))),
            miniStat("SPD", String.valueOf((int)merc.getStats().get(StatType.SPEED))),
            miniStat("ATK", String.valueOf((int)Math.max(
                merc.getStats().get(StatType.PHYSICAL_ATK),
                Math.max(merc.getStats().get(StatType.CYBER_ATK),
                         merc.getStats().get(StatType.ENERGY_ATK)))))
        );
        VBox vitals = UIFactory.compactVitalBars(
                merc.getCurrentHp(),     merc.getStats().get(StatType.MAX_HP),
                merc.getCurrentShield(), merc.getStats().get(StatType.MAX_SHIELD),
                merc.getCurrentMp(),     merc.getStats().get(StatType.MAX_MP));
        Button toggleBtn = isActive ? UIFactory.btnDanger("KELUARKAN DARI REGU")
                                    : UIFactory.btnPrimary("TAMBAH KE REGU");
        toggleBtn.setMaxWidth(Double.MAX_VALUE);
        toggleBtn.setOnAction(e -> {
            if (isActive) engine.removeFromActiveParty(merc.getMercenaryType());
            else if (!engine.addToActiveParty(merc.getMercenaryType()))
                router.addSystemChat("Regu penuh! Keluarkan satu kawula dulu.");
            router.showMercenary();
        });
        card.getChildren().addAll(header, subtitle, statsRow, vitals, toggleBtn);
        return card;
    }

    private VBox buildHireTab() {
        VBox list = new VBox(8);
        list.setPadding(new Insets(10, 16, 10, 16));
        Label gold = new Label("Your Gold: ⚙ " + engine.getPlayer().getGold());
        gold.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 0 0 8 0;");
        list.getChildren().add(gold);
        for (MercenaryType type : MercenaryType.values()) {
            boolean alreadyOwned = engine.getOwnedMercs().stream()
                    .anyMatch(m -> m.getMercenaryType() == type);
            list.getChildren().add(buildHireCard(type, alreadyOwned));
        }
        return list;
    }

    private VBox buildHireCard(MercenaryType type, boolean owned) {
        Mercenary sample = EntityFactory.createMercenary(type);
        long cost = sample.getHireCost();
        boolean canAfford = engine.getPlayer().getGold() >= cost;
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #150E08; -fx-border-color: " +
                      (owned ? UIFactory.GREEN : canAfford ? "#3A281088" : "#3A281044") +
                      "; -fx-border-width: 1 1 1 3;");
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(type.displayName.toUpperCase());
        nameLabel.setStyle("-fx-text-fill: " + (owned ? UIFactory.GREEN : UIFactory.TEXT) +
                           "; -fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        Label roleLabel = new Label(type.subtitle);
        roleLabel.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        header.getChildren().addAll(nameLabel, roleLabel);
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
            miniStat("HP",  String.valueOf((int)sample.getStats().get(StatType.MAX_HP))),
            miniStat("SHD", String.valueOf((int)sample.getStats().get(StatType.MAX_SHIELD))),
            miniStat("SPD", String.valueOf((int)sample.getStats().get(StatType.SPEED))),
            miniStat("ATK", String.valueOf((int)Math.max(
                sample.getStats().get(StatType.PHYSICAL_ATK),
                Math.max(sample.getStats().get(StatType.CYBER_ATK),
                         sample.getStats().get(StatType.ENERGY_ATK)))))
        );
        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        if (owned) {
            Label ob = new Label("✓ ALREADY IN ROSTER");
            ob.setStyle("-fx-text-fill: " + UIFactory.GREEN +
                        "; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            bottomRow.getChildren().add(ob);
        } else {
            Label priceLabel = new Label("⚙ " + cost + " gold");
            priceLabel.setStyle("-fx-text-fill: " + (canAfford ? "#FFB830" : "#6A5840") +
                                "; -fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;");
            HBox.setHgrow(priceLabel, Priority.ALWAYS);
            Button hireBtn = new Button("REKRUT");
            hireBtn.setDisable(!canAfford);
            hireBtn.setStyle("-fx-background-color: " + (canAfford ? "#FFB83022" : "transparent") + ";" +
                             "-fx-border-color: " + (canAfford ? "#FFB830" : "#3A2810") + ";" +
                             "-fx-border-width: 1; -fx-text-fill: " + (canAfford ? "#FFB830" : "#6A5840") + ";" +
                             "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                             "-fx-padding: 6 16; -fx-cursor: " + (canAfford ? "hand" : "default") + ";");
            hireBtn.setOnAction(e -> {
                if (engine.hireMercenary(type)) {
                    router.addSystemChat(type.displayName + " bergabung dengan regu!");
                    router.showMercenary();
                } else {
                    router.addSystemChat("Emas tidak cukup!");
                }
            });
            bottomRow.getChildren().addAll(priceLabel, hireBtn);
        }
        card.getChildren().addAll(header, statsRow, bottomRow);
        return card;
    }

    private VBox miniStat(String name, String value) {
        VBox box = new VBox(0);
        box.setAlignment(Pos.CENTER);
        Label n = new Label(name);
        n.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #C8860A; -fx-font-family: 'Courier New';" +
                   "-fx-font-size: 11px; -fx-font-weight: bold;");
        box.getChildren().addAll(n, v);
        return box;
    }
}

// ═══════════════════════════════════════════════════════════
// EVENT VIEW
// ═══════════════════════════════════════════════════════════

public static class EventViewImpl {

    private final GameEngine     engine;
    private final SceneRouter    router;
    private final DungeonEvent event;

    EventViewImpl(GameEngine engine, SceneRouter router, DungeonEvent event) {
        this.engine = engine;
        this.router = router;
        this.event  = event;
    }

    Parent build() {
        BorderPane root = UIFactory.screenRootBorder();

        // ── TOP: header dengan kategori event ─────────────
        String catColor = switch (event.getCategory()) {
            case POSITIVE -> "#2D7A45";
            case NEGATIVE -> "#CC3300";
            case CHOICE   -> "#C8860A";
            case NEUTRAL  -> "#6A5840";
        };
        String catIcon = switch (event.getCategory()) {
            case POSITIVE -> "✦";
            case NEGATIVE -> "☠";
            case CHOICE   -> "◈";
            case NEUTRAL  -> "—";
        };
        String catName = switch (event.getCategory()) {
            case POSITIVE -> "KEBERUNTUNGAN";
            case NEGATIVE -> "BAHAYA";
            case CHOICE   -> "PILIHAN GAIB";
            case NEUTRAL  -> "PERISTIWA";
        };

        VBox topBar = new VBox(4);
        topBar.setPadding(new Insets(12, 16, 12, 16));
        topBar.setStyle("-fx-background-color: #0F0A06;" +
                        "-fx-border-color: " + catColor + "44;" +
                        "-fx-border-width: 0 0 2 0;");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button back = new Button("← KEMBALI");
        back.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                      "-fx-border-width: 1; -fx-text-fill: #5A3A10;" +
                      "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                      "-fx-padding: 3 8; -fx-cursor: hand;");
        back.setOnAction(e -> router.showDungeonMap());

        Label catBadge = new Label(catIcon + "  " + catName);
        catBadge.setStyle(
            "-fx-background-color: " + catColor + "22;" +
            "-fx-border-color: " + catColor + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + catColor + ";" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
            "-fx-font-weight: bold; -fx-padding: 3 10;"
        );
        HBox.setHgrow(catBadge, Priority.ALWAYS);
        topRow.getChildren().addAll(back, catBadge);

        Label titleLbl = new Label(event.getTitle());
        titleLbl.setWrapText(true);
        titleLbl.setStyle(
            "-fx-text-fill: " + catColor + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, " + catColor + ", 14, 0.6, 0, 0)" +
            "          , dropshadow(gaussian, " + catColor + ", 5, 0.3, 0, 0);"
        );
        topBar.getChildren().addAll(topRow, titleLbl);
        root.setTop(topBar);

        // ── CENTER: narrative + ornamen ────────────────────
        VBox scrollContent = new VBox(16);
        scrollContent.setPadding(new Insets(16, 16, 16, 16));

        // Ornamen pembatas atas
        Label ornTop = new Label("─────  " + catIcon + "  ─────────────────────────────────");
        ornTop.setStyle("-fx-text-fill: " + catColor + "44; -fx-font-family: 'Courier New';" +
                        "-fx-font-size: 11px;");

        // Narrative panel — parchment style
        VBox narrativePanel = new VBox(0);
        narrativePanel.setStyle(
            "-fx-background-color: #150E08;" +
            "-fx-border-color: " + catColor + "33;" +
            "-fx-border-width: 1 1 1 3;" +
            "-fx-padding: 14;"
        );
        Label narrative = new Label(event.getNarrative());
        narrative.setWrapText(true);
        narrative.setStyle(
            "-fx-text-fill: #A09070;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
            "-fx-line-spacing: 4;"
        );
        narrativePanel.getChildren().add(narrative);

        scrollContent.getChildren().addAll(ornTop, narrativePanel);

        ScrollPane scroll = new ScrollPane(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        // ── BOTTOM: pilihan aksi ────────────────────────────
        VBox choicesBox = new VBox(8);
        choicesBox.setPadding(new Insets(12, 16, 14, 16));
        choicesBox.setStyle(
            "-fx-background-color: #0F0A06;" +
            "-fx-border-color: #3A2810; -fx-border-width: 1 0 0 0;"
        );

        if (event.hasChoices()) {
            Label choiceTitle = new Label("── PILIH TINDAKANMU ──");
            choiceTitle.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New';" +
                                 "-fx-font-size: 10px; -fx-letter-spacing: 2;" +
                                 "-fx-padding: 0 0 6 0;");
            choicesBox.getChildren().add(choiceTitle);

            DungeonEvent.EventChoice[] choices = event.getChoices();
            for (int i = 0; i < choices.length; i++) {
                final int idx = i;
                DungeonEvent.EventChoice choice = choices[i];

                Button btn = new Button("▶  " + choice.label);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setStyle(
                    "-fx-background-color: #1A1008;" +
                    "-fx-border-color: #3A2810;" +
                    "-fx-border-width: 1 1 1 3;" +
                    "-fx-text-fill: #A09070;" +
                    "-fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
                    "-fx-padding: 10 14; -fx-cursor: hand;" +
                    "-fx-alignment: CENTER_LEFT;"
                );
                btn.setOnMouseEntered(ev -> btn.setStyle(
                    "-fx-background-color: " + catColor + "11;" +
                    "-fx-border-color: " + catColor + ";" +
                    "-fx-border-width: 1 1 1 3;" +
                    "-fx-text-fill: " + catColor + ";" +
                    "-fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
                    "-fx-padding: 10 14; -fx-cursor: hand;" +
                    "-fx-alignment: CENTER_LEFT;" +
                    "-fx-effect: dropshadow(gaussian, " + catColor + ", 6, 0.2, 0, 0);"
                ));
                btn.setOnMouseExited(ev -> btn.setStyle(
                    "-fx-background-color: #1A1008;" +
                    "-fx-border-color: #3A2810;" +
                    "-fx-border-width: 1 1 1 3;" +
                    "-fx-text-fill: #A09070;" +
                    "-fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
                    "-fx-padding: 10 14; -fx-cursor: hand;" +
                    "-fx-alignment: CENTER_LEFT;"
                ));
                btn.setOnAction(e -> {
                    engine.resolveEventChoice(event, idx);
                    router.showDungeonMap();
                });
                choicesBox.getChildren().add(btn);
            }
        } else {
            Button okBtn = UIFactory.btnPrimary("▶  LANJUTKAN PERJALANAN");
            okBtn.setMaxWidth(Double.MAX_VALUE);
            okBtn.setOnAction(e -> router.showDungeonMap());
            choicesBox.getChildren().add(okBtn);
        }
        root.setBottom(choicesBox);

        UIFactory.fadeIn(root, 400);
        return root;
    }
}

// ═══════════════════════════════════════════════════════════
// SHOP VIEW
// ═══════════════════════════════════════════════════════════

public static class ShopViewImpl {
    private final GameEngine  engine;
    private final SceneRouter router;
    private       List<arclightcity.item.Item> shopItems;

    ShopViewImpl(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
        // Generate 4 item untuk dijual berdasarkan floor level
        int floor = engine.getDungeonManager() != null
                ? engine.getDungeonManager().getCurrentFloorNumber() : 1;
        shopItems = arclightcity.item.LootManager.generateLoot("SHOP", floor);
        // Pastikan minimal 4 item
        while (shopItems.size() < 4) {
            shopItems.addAll(arclightcity.item.LootManager.generateLoot("SHOP", floor));
        }
        shopItems = new java.util.ArrayList<>(shopItems.subList(0, Math.min(4, shopItems.size())));
    }

    Parent build() {
        BorderPane root = UIFactory.screenRootBorder();

        // ── TOP: header ────────────────────────────────────
        VBox topSection = new VBox(0);

        HBox header = new HBox(10);
        header.setPadding(new Insets(10, 16, 10, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #0F0A06;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Button back = new Button("← KEMBALI");
        back.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                      "-fx-border-width: 1; -fx-text-fill: #5A3A10;" +
                      "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                      "-fx-padding: 3 8; -fx-cursor: hand;");
        back.setOnAction(e -> router.showDungeonMap());

        Label titleLbl = new Label("🧙  PASAR GAIB");
        titleLbl.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 14px; -fx-font-weight: bold;" +
                          "-fx-effect: dropshadow(gaussian, #C8860A, 6, 0.3, 0, 0);");
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        Label goldLbl = new Label("⚙ " + UIFactory.formatNumber(engine.getPlayer().getGold()));
        goldLbl.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 12px; -fx-font-weight: bold;");
        header.getChildren().addAll(back, titleLbl, goldLbl);

        // Merchant info
        VBox merchantInfo = new VBox(4);
        merchantInfo.setPadding(new Insets(10, 16, 10, 16));
        merchantInfo.setStyle("-fx-background-color: #150E08;" +
                              "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Label merchantName = new Label("⚙ PEDAGANG GAIB");
        merchantName.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                              "-fx-font-size: 13px; -fx-font-weight: bold;");
        Label merchantQuote = new Label("\"Aku punya apa yang kau butuhkan... dengan harga yang tepat.\"");
        merchantQuote.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New';" +
                               "-fx-font-size: 11px; -fx-font-style: italic;");
        merchantInfo.getChildren().addAll(merchantName, merchantQuote);

        Label itemsTitle = UIFactory.sectionTitle("── BARANG DAGANGAN ──");
        itemsTitle.setPadding(new Insets(8, 16, 4, 16));

        topSection.getChildren().addAll(header, merchantInfo, itemsTitle);
        root.setTop(topSection);

        // ── CENTER: item list ──────────────────────────────
        VBox itemList = new VBox(8);
        itemList.setPadding(new Insets(8, 16, 8, 16));
        for (arclightcity.item.Item item : shopItems) {
            itemList.getChildren().add(buildShopRow(item));
        }

        ScrollPane scroll = new ScrollPane(itemList);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        // Bottom leave button
        VBox bottom = new VBox(0);
        bottom.setPadding(new Insets(10, 16, 12, 16));
        bottom.setStyle("-fx-background-color: #0F0A06;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 1 0 0 0;");
        Button leave = UIFactory.btnPrimary("← TINGGALKAN PASAR");
        leave.setMaxWidth(Double.MAX_VALUE);
        leave.setOnAction(e -> router.showDungeonMap());
        bottom.getChildren().add(leave);
        root.setBottom(bottom);

        UIFactory.fadeIn(root, 300);
        return root;
    }

    private HBox buildShopRow(arclightcity.item.Item item) {
        // Harga deterministik berdasarkan item ID hash — tidak berubah tiap render
        int seed = Math.abs(item.getId().hashCode());
        int price = switch (item.getRarity()) {
            case COMMON    -> 30   + (seed % 20);
            case UNCOMMON  -> 80   + (seed % 40);
            case RARE      -> 200  + (seed % 100);
            case EPIC      -> 500  + (seed % 200);
            case LEGENDARY -> 1200 + (seed % 300);
            case MYTHIC    -> 0;   // Mythic tidak dijual di shop biasa
        };

        HBox row = new HBox(10);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
            "-fx-background-color: #150E08;" +
            "-fx-border-color: " + UIFactory.rarityColor(item.getRarity()) + "33;" +
            "-fx-border-width: 1 1 1 3;"
        );

        // Rarity bar
        VBox rarityBar = new VBox();
        rarityBar.setPrefWidth(3);
        rarityBar.setStyle("-fx-background-color: " + UIFactory.rarityColor(item.getRarity()) + ";");

        // Item info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(item.getFullName());
        name.setStyle(
            "-fx-text-fill: " + UIFactory.rarityColor(item.getRarity()) + ";" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;"
        );

        // Stat preview (3 stat pertama)
        String stats = item.getStatBonuses().entrySet().stream()
            .limit(3)
            .map(e -> {
                boolean isPct = e.getKey().name().contains("CHANCE") ||
                                e.getKey().name().contains("EVASION") ||
                                e.getKey().name().contains("MULT") ||
                                e.getKey().name().contains("PIERCE") ||
                                e.getKey().name().contains("LIFESTEAL");
                return e.getKey().displayName + ": +" +
                       (isPct ? String.format("%.0f%%", e.getValue()*100)
                              : String.format("%.0f", e.getValue()));
            })
            .reduce("", (a, b) -> a.isEmpty() ? b : a + "  " + b);

        Label statLabel = new Label(stats.isEmpty() ? item.getDescription() : stats);
        statLabel.setStyle("-fx-text-fill: #A09070; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        info.getChildren().addAll(name, statLabel);

        // Price + Buy button
        VBox buyBox = new VBox(4);
        buyBox.setAlignment(Pos.CENTER_RIGHT);

        Label priceLabel = new Label("⚙ " + price);
        priceLabel.setStyle(
            "-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
            "-fx-font-size: 12px; -fx-font-weight: bold;"
        );

        boolean canAfford = engine.getPlayer().getGold() >= price;
        Button buyBtn = new Button("BELI");
        buyBtn.setStyle(
            "-fx-background-color: " + (canAfford ? "#FFB83022" : "#3A281022") + ";" +
            "-fx-border-color: " + (canAfford ? "#FFB830" : "#6A5840") + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + (canAfford ? "#FFB830" : "#6A5840") + ";" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
            "-fx-padding: 4 10; -fx-cursor: " + (canAfford ? "hand" : "default") + ";"
        );
        buyBtn.setDisable(!canAfford);
        buyBtn.setOnAction(e -> {
            if (engine.getPlayer().getGold() >= price) {
                engine.getPlayer().spendGold(price);
                engine.getInventory().addItem(item);
                shopItems.remove(item);
                // Refresh view
                router.showShop();
            }
        });

        buyBox.getChildren().addAll(priceLabel, buyBtn);
        row.getChildren().addAll(rarityBar, info, buyBox);
        return row;
    }
}

// ═══════════════════════════════════════════════════════════
// VICTORY VIEW
// ═══════════════════════════════════════════════════════════

public static class VictoryViewImpl {
    private final GameEngine     engine;
    private final SceneRouter    router;
    private final CombatResult result;

    VictoryViewImpl(GameEngine engine, SceneRouter router, CombatResult result) {
        this.engine = engine;
        this.router = router;
        this.result = result;
    }

    Parent build() {
        BorderPane root = UIFactory.screenRootBorder();

        // ── TOP: header kemenangan ─────────────────────────
        VBox header = new VBox(6);
        header.setPadding(new Insets(16, 16, 16, 16));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #0F0A06;" +
                        "-fx-border-color: #C8860A44; -fx-border-width: 0 0 2 0;");

        Label victoryLbl = new Label("✦  KEMENANGAN  ✦");
        victoryLbl.setStyle(
            "-fx-text-fill: #FFB830;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 24, 0.8, 0, 0)" +
            "          , dropshadow(gaussian, #FFB830, 10, 0.5, 0, 0);"
        );

        boolean bossKilled = result.getDefeatedEnemies().stream()
            .anyMatch(e -> e instanceof arclightcity.entity.enemy.Boss);

        Label subLbl = new Label(bossKilled
            ? "⚡ BOSS DIKALAHKAN — Pecahan Mitik diperoleh!"
            : "Pertempuran selesai dalam " + result.getTurnsElapsed() + " giliran");
        subLbl.setStyle("-fx-text-fill: " + (bossKilled ? "#FF8833" : "#5A3A10") +
                        "; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        header.getChildren().addAll(victoryLbl, subLbl);
        root.setTop(header);

        // ── CENTER: rewards ────────────────────────────────
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        // Ornamen
        Label orn = new Label("─────  ✦  HASIL PERTEMPURAN  ✦  ─────");
        orn.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        orn.setMaxWidth(Double.MAX_VALUE);
        orn.setAlignment(Pos.CENTER);

        // Reward panel
        VBox rewards = new VBox(0);
        rewards.setStyle("-fx-background-color: #1A1008; -fx-border-color: #C8860A55;" +
                         "-fx-border-width: 1 1 1 3;");
        rewards.setPadding(new Insets(14));

        Label rwdTitle = UIFactory.sectionTitle("◈ HADIAH");
        rwdTitle.setPadding(new Insets(0, 0, 8, 0));

        HBox expRow = buildRewardRow("EXP Diperoleh",
            "+" + UIFactory.formatNumber((long)result.getTotalExpGained()), "#FFB830");
        HBox goldRow = buildRewardRow("Gold Diperoleh",
            "+" + UIFactory.formatNumber(result.getTotalGoldGained()), "#FFB830");
        HBox lootRow = buildRewardRow("Item Loot",
            result.getLootItemIds().size() + " item", "#A09070");

        int levels = result.getLevelsGained();
        rewards.getChildren().addAll(rwdTitle, expRow, goldRow, lootRow);

        if (levels > 0) {
            HBox lvlRow = buildRewardRow("Level Up!", "+" + levels + " Level", "#FF8833");
            rewards.getChildren().add(lvlRow);
        }

        if (bossKilled) {
            HBox fragRow = buildRewardRow("✦ Pecahan Mitik", "+1 Fragment", "#FF6B00");
            rewards.getChildren().add(fragRow);
        }

        // Player info sekarang
        var player = engine.getPlayer();
        Label nowLabel = new Label("─── Status Sekarang ───");
        nowLabel.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        HBox lvNow = buildRewardRow("Level", "LV." + player.getLevel(), "#A09070");
        HBox goldNow = buildRewardRow("Total Gold", "⚙ " + UIFactory.formatNumber(player.getGold()), "#A09070");

        content.getChildren().addAll(orn, rewards, nowLabel, lvNow, goldNow);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        // ── BOTTOM: navigation ─────────────────────────────
        VBox nav = new VBox(8);
        nav.setPadding(new Insets(12, 16, 14, 16));
        nav.setStyle("-fx-background-color: #0F0A06;" +
                     "-fx-border-color: #3A2810; -fx-border-width: 1 0 0 0;");

        Button cont = new Button("▶  LANJUT JELAJAH DUNGEON");
        cont.setMaxWidth(Double.MAX_VALUE);
        cont.setStyle(
            "-fx-background-color: #C8860A22; -fx-border-color: #FFB830; -fx-border-width: 1;" +
            "-fx-text-fill: #FFB830; -fx-font-family: 'Courier New'; -fx-font-size: 13px;" +
            "-fx-font-weight: bold; -fx-padding: 12 20; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 8, 0.3, 0, 0);"
        );
        cont.setOnAction(e -> router.showDungeonMap());

        Button hub = UIFactory.btnPrimary("◈  KEMBALI KE MARKAS");
        hub.setMaxWidth(Double.MAX_VALUE);
        hub.setOnAction(e -> router.showHub());

        nav.getChildren().addAll(cont, hub);
        root.setBottom(nav);

        // Animasi gold pulse
        UIFactory.glowPulse(victoryLbl, "#FFB830");
        UIFactory.fadeIn(root, 500);
        return root;
    }

    private HBox buildRewardRow(String label, String value, String valueColor) {
        HBox row = new HBox();
        row.setPadding(new Insets(5, 0, 5, 0));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: #2A1808; -fx-border-width: 0 0 1 0;");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        HBox.setHgrow(lbl, Priority.ALWAYS);

        Label val = new Label(value);
        val.setStyle("-fx-text-fill: " + valueColor + "; -fx-font-family: 'Courier New';" +
                     "-fx-font-size: 12px; -fx-font-weight: bold;");

        row.getChildren().addAll(lbl, val);
        return row;
    }
}

// ═══════════════════════════════════════════════════════════
// GAME OVER VIEW
// ═══════════════════════════════════════════════════════════

public static class GameOverViewImpl {
    private final GameEngine  engine;
    private final SceneRouter router;

    GameOverViewImpl(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    Parent build() {
        BorderPane root = UIFactory.screenRootBorder();

        // ── TOP: header ────────────────────────────────────
        VBox header = new VBox(4);
        header.setPadding(new Insets(16, 16, 16, 16));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #0F0A06;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Label fallen = new Label("◆  GUGUR  ◆");
        fallen.setStyle(
            "-fx-text-fill: #CC3300;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, #CC3300, 20, 0.7, 0, 0)" +
            "          , dropshadow(gaussian, #FF5500, 8, 0.4, 0, 0);"
        );

        Label sub = new Label("perjalananmu terhenti... namun belum berakhir");
        sub.setStyle("-fx-text-fill: #4A3820; -fx-font-family: 'Courier New';" +
                     "-fx-font-size: 12px; -fx-letter-spacing: 2;");

        header.getChildren().addAll(fallen, sub);
        root.setTop(header);

        // ── CENTER: stats ──────────────────────────────────
        VBox content = new VBox(16);
        content.setPadding(new Insets(20, 20, 20, 20));
        content.setAlignment(Pos.TOP_CENTER);

        var player = engine.getPlayer();

        // Ornamen pembatas
        Label ornTop = new Label("─────  ✦  CATATAN PERJALANAN  ✦  ─────");
        ornTop.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        // Stats panel
        VBox statsPanel = new VBox(0);
        statsPanel.setStyle("-fx-background-color: #1A1008; -fx-border-color: #3A2810;" +
                            "-fx-border-width: 1 1 1 3;");
        statsPanel.setPadding(new Insets(14));
        statsPanel.getChildren().addAll(
            UIFactory.statRow("Nama Pendekar",   player.getName()),
            UIFactory.statRow("Asal Usul",       player.getBackground().name),
            UIFactory.statRow("Level Tercapai",  "LV." + player.getLevel()),
            UIFactory.statRow("Floor Terdalam",  "Lantai " + player.getDungeonDepth()),
            UIFactory.statRow("Gold Terkumpul",  "⚙ " + UIFactory.formatNumber(player.getGold()))
        );

        // Kutipan Nusantara
        String[] quotes = {
            "\"Bukan kegagalan yang mempermalukan, melainkan tidak mau mencoba lagi.\"",
            "\"Jatuh tujuh kali, bangkit delapan kali.\"",
            "\"Patah tumbuh, hilang berganti — demikian hukum alam.\"",
            "\"Kesalahan adalah guru terbaik bagi mereka yang mau belajar.\""
        };
        String quote = quotes[(int)(Math.random() * quotes.length)];
        Label quoteLabel = new Label(quote);
        quoteLabel.setWrapText(true);
        quoteLabel.setMaxWidth(Double.MAX_VALUE);
        quoteLabel.setAlignment(Pos.CENTER);
        quoteLabel.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New';" +
                            "-fx-font-size: 11px; -fx-font-style: italic;");

        content.getChildren().addAll(ornTop, statsPanel, quoteLabel);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        // ── BOTTOM: action buttons ─────────────────────────
        VBox actions = new VBox(8);
        actions.setPadding(new Insets(14, 16, 16, 16));
        actions.setStyle("-fx-background-color: #0F0A06;" +
                         "-fx-border-color: #3A2810; -fx-border-width: 1 0 0 0;");

        // COBA LAGI — buat karakter baru, langsung ke hub
        Button retry = new Button("↺  COBA LAGI");
        retry.setMaxWidth(Double.MAX_VALUE);
        retry.setStyle(
            "-fx-background-color: #CC330022;" +
            "-fx-border-color: #CC3300;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #FF5533;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 12 20; -fx-cursor: hand;"
        );
        retry.setOnAction(e -> {
            engine.createCharacter(player.getName(), player.getBackground());
            arclightcity.save.SaveManager.deleteAllSaves();
            router.showHub();
        });

        // KEMBALI KE HUB — bukan main menu!
        Button backHub = UIFactory.btnPrimary("◈  KEMBALI KE MARKAS");
        backHub.setMaxWidth(Double.MAX_VALUE);
        backHub.setOnAction(e -> {
            // Tidak reset karakter — kembali ke hub dengan state saat ini
            // (Player sudah mati dalam dungeon, tapi bisa upgrade/prepare ulang)
            engine.returnToHub();
            router.showHub();
        });

        // MENU UTAMA
        Button mainMenu = new Button("← MENU UTAMA");
        mainMenu.setMaxWidth(Double.MAX_VALUE);
        mainMenu.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #3A2810; -fx-border-width: 1;" +
            "-fx-text-fill: #4A3820;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
            "-fx-padding: 8 20; -fx-cursor: hand;"
        );
        mainMenu.setOnAction(e -> router.showMainMenu());

        actions.getChildren().addAll(retry, backHub, mainMenu);
        root.setBottom(actions);

        // Animasi flicker judul
        FadeTransition flicker = new FadeTransition(Duration.millis(150), fallen);
        flicker.setFromValue(1.0); flicker.setToValue(0.7);
        flicker.setAutoReverse(true); flicker.setCycleCount(4);
        flicker.play();

        UIFactory.fadeIn(root, 600);
        return root;
    }
}

} // end ViewsBundle
