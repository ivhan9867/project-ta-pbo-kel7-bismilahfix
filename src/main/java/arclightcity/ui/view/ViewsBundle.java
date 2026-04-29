package arclightcity.ui.view;

import arclightcity.combat.CombatResult;
import arclightcity.dungeon.DungeonEvent;
import arclightcity.engine.GameEngine;
import arclightcity.entity.mercenary.Mercenary;
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
        VBox root = UIFactory.screenRoot();

        root.getChildren().add(UIFactory.headerWithResources(
                "INVENTORY", () -> router.showHub(),
                engine.getPlayer().getGold(), 0));

        // ── Equipment slots header ────────────────────────
        root.getChildren().add(buildEquipmentSlots(inv));

        // ── Material counters ─────────────────────────────
        root.getChildren().add(buildMaterialBar(inv));

        UIFactory.divider();

        // ── Filter tabs ───────────────────────────────────
        root.getChildren().add(buildFilterTabs());

        // ── Item list ─────────────────────────────────────
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #050810; -fx-background: #050810; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        itemListContainer = new VBox(0);
        refreshItemList(inv);
        scroll.setContent(itemListContainer);
        root.getChildren().add(scroll);

        UIFactory.fadeIn(root, 300);
        return root;
    }

    private HBox buildEquipmentSlots(Inventory inv) {
        HBox bar = new HBox(6);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setStyle("-fx-background-color: #0C1220; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

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
                    "-fx-background-color: #080D18;" +
                    "-fx-border-color: #1C2E4466;" +
                    "-fx-border-width: 1;" +
                    "-fx-padding: 6;"
                );
                Label slotType = new Label(label);
                slotType.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
                Label empty = new Label("—");
                empty.setStyle("-fx-text-fill: #2A3A50; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
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
                slotType.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
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
        bar.setStyle("-fx-background-color: #080D18; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");
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
            name.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
            Label val = new Label(mat[1]);
            val.setStyle("-fx-text-fill: " + UIFactory.CYAN + "; -fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;");
            m.getChildren().addAll(name, val);
            bar.getChildren().add(m);
        }

        Label bagInfo = new Label("BAG " + inv.getBagSize() + "/" + inv.getMaxBagSize());
        bagInfo.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        HBox.setHgrow(new Region(), Priority.ALWAYS);
        bar.getChildren().addAll(new Region(), bagInfo);
        HBox.setHgrow(bar.getChildren().get(bar.getChildren().size() - 2), Priority.ALWAYS);

        return bar;
    }

    private HBox buildFilterTabs() {
        HBox tabs = new HBox(0);
        tabs.setStyle("-fx-background-color: #0C1220; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        String[] filters = {"ALL", "WEAPON", "ARMOR", "ACCESSORY", "CONSUMABLE", "MATERIAL"};
        for (String filter : filters) {
            Label tab = new Label(filter);
            tab.setPadding(new Insets(8, 12, 8, 12));
            boolean active = filter.equals(activeFilter);
            tab.setStyle(
                "-fx-text-fill: " + (active ? UIFactory.CYAN : UIFactory.DIM) + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
                (active ? "-fx-border-color: transparent transparent #00E5FF transparent; -fx-border-width: 0 0 2 0;" : "")
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
            empty.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-padding: 20;");
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
        String styleNormal = "-fx-background-color: #050810;" +
                             "-fx-border-color: transparent transparent #0C1220 transparent;" +
                             "-fx-border-width: 0 0 1 0;";
        String styleHover  = "-fx-background-color: #0C1220;" +
                             "-fx-border-color: transparent transparent #1C2E44 transparent;" +
                             "-fx-border-width: 0 0 1 0;";
        row.setStyle(styleNormal);
        row.setOnMouseEntered(e -> row.setStyle(styleHover));
        row.setOnMouseExited(e -> row.setStyle(styleNormal));

        // Rarity indicator
        VBox rarityBar = new VBox();
        rarityBar.setPrefWidth(3);
        rarityBar.setMinWidth(3);
        rarityBar.setStyle("-fx-background-color: " + UIFactory.rarityColor(item.getRarity()) + ";");

        // Item info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Name + rarity tag
        HBox nameRow = new HBox(6);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(item.getFullName());
        nameLabel.setStyle("-fx-text-fill: " + UIFactory.rarityColor(item.getRarity()) +
                "; -fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label rarityTag = new Label("[" + item.getRarity().displayName + "]");
        rarityTag.setStyle("-fx-text-fill: " + UIFactory.rarityColor(item.getRarity()) +
                "88; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        nameRow.getChildren().addAll(nameLabel, rarityTag);

        // Stat summary (ambil 2 stat utama)
        String statSummary = buildStatSummary(item);
        Label descLabel = new Label(statSummary);
        descLabel.setStyle("-fx-text-fill: #8899AA; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        descLabel.setWrapText(true);

        info.getChildren().addAll(nameRow, descLabel);

        // Action buttons
        VBox actions = new VBox(4);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMinWidth(80);

        if (item instanceof Equipment eq) {
            // EQUIP button
            Button equipBtn = new Button("EQUIP");
            equipBtn.setStyle("-fx-background-color: #00E5FF15; -fx-border-color: #00E5FF55;" +
                    " -fx-border-width: 1; -fx-text-fill: #00E5FF;" +
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
                    " -fx-border-color: " + (canUpgrade ? "#FFD60066" : "#5A6A8055") + ";" +
                    " -fx-border-width: 1;" +
                    " -fx-text-fill: " + (canUpgrade ? "#FFD600" : "#5A6A80") + ";" +
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
                    " -fx-border-color: " + (hasKit ? "#AA00FF66" : "#5A6A8055") + ";" +
                    " -fx-border-width: 1;" +
                    " -fx-text-fill: " + (hasKit ? "#AA00FF" : "#5A6A80") + ";" +
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
            stackLabel.setStyle("-fx-text-fill: #00E676; -fx-font-family: 'Courier New';" +
                    " -fx-font-size: 11px; -fx-font-weight: bold;");
            Button useBtn = new Button("USE");
            useBtn.setStyle("-fx-background-color: #00E67615; -fx-border-color: #00E67655;" +
                    " -fx-border-width: 1; -fx-text-fill: #00E676;" +
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
        alert.getDialogPane().setStyle("-fx-background-color: #0C1220; -fx-font-family: 'Courier New';");
        alert.showAndWait();
    }
}

// ═══════════════════════════════════════════════════════════
// MERCENARY VIEW
// ═══════════════════════════════════════════════════════════

public static class MercenaryViewImpl {

    private final GameEngine  engine;
    private final SceneRouter router;

    MercenaryViewImpl(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    Parent build() {
        VBox root = UIFactory.screenRoot();
        root.getChildren().add(UIFactory.headerWithResources(
                "MERCENARY", () -> router.showHub(), engine.getPlayer().getGold(), 0));

        // Active party info
        HBox partyBar = new HBox(8);
        partyBar.setPadding(new Insets(8, 16, 8, 16));
        partyBar.setStyle("-fx-background-color: #0C1220; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");
        Label partyLabel = new Label("ACTIVE CREW: " + engine.getActiveMercs().size() + " / 2");
        partyLabel.setStyle("-fx-text-fill: #00E5FF; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        partyBar.getChildren().add(partyLabel);
        root.getChildren().add(partyBar);

        // Owned mercs list
        Label listTitle = UIFactory.sectionTitle("MERCENARY ROSTER");
        listTitle.setPadding(new Insets(10, 16, 4, 16));
        root.getChildren().add(listTitle);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #050810; -fx-background: #050810; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox mercList = new VBox(8);
        mercList.setPadding(new Insets(8, 16, 8, 16));

        var owned = engine.getOwnedMercs();
        if (owned.isEmpty()) {
            Label none = new Label("No mercenaries hired.");
            none.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-padding: 20;");
            mercList.getChildren().add(none);
        } else {
            for (Mercenary merc : owned) {
                mercList.getChildren().add(buildMercCard(merc));
            }
        }

        scroll.setContent(mercList);
        root.getChildren().add(scroll);

        UIFactory.fadeIn(root, 300);
        return root;
    }

    private VBox buildMercCard(Mercenary merc) {
        boolean isActive = engine.getActiveMercs().contains(merc);

        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
            "-fx-background-color: " + (isActive ? "#0C1A0C" : "#0C1220") + ";" +
            "-fx-border-color: " + (isActive ? UIFactory.GREEN : "#1C2E44") + ";" +
            "-fx-border-width: 1 1 1 3;"
        );

        // Header row
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(merc.getMercenaryType().displayName.toUpperCase());
        nameLabel.setStyle("-fx-text-fill: " + (isActive ? UIFactory.GREEN : UIFactory.TEXT) + "; -fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label roleLabel = new Label("[" + merc.getRole().name() + "]");
        roleLabel.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        Label loyaltyLabel = new Label("♥ " + merc.getLoyaltyTitle());
        loyaltyLabel.setStyle("-fx-text-fill: #FF6B6B; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        header.getChildren().addAll(nameLabel, roleLabel, loyaltyLabel);

        // Subtitle
        Label subtitle = new Label(merc.getMercenaryType().subtitle + " — Loyalty Lv." + merc.getLoyaltyLevel());
        subtitle.setStyle("-fx-text-fill: #8899AA; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        // Key stats
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
            miniStat("HP",  String.valueOf((int)merc.getStats().get(StatType.MAX_HP))),
            miniStat("SHD", String.valueOf((int)merc.getStats().get(StatType.MAX_SHIELD))),
            miniStat("SPD", String.valueOf((int)merc.getStats().get(StatType.SPEED))),
            miniStat("ATK", String.valueOf((int)Math.max(
                merc.getStats().get(StatType.PHYSICAL_ATK),
                Math.max(merc.getStats().get(StatType.CYBER_ATK), merc.getStats().get(StatType.ENERGY_ATK)))))
        );

        // Vitals
        VBox vitals = UIFactory.compactVitalBars(
                merc.getCurrentHp(),     merc.getStats().get(StatType.MAX_HP),
                merc.getCurrentShield(), merc.getStats().get(StatType.MAX_SHIELD),
                merc.getCurrentMp(),     merc.getStats().get(StatType.MAX_MP));

        // Toggle active button
        Button toggleBtn = isActive
                ? UIFactory.btnDanger("REMOVE FROM CREW")
                : UIFactory.btnPrimary("ADD TO CREW");
        toggleBtn.setOnAction(e -> {
            if (isActive) engine.removeFromActiveParty(merc.getMercenaryType());
            else          engine.addToActiveParty(merc.getMercenaryType());
            router.showMercenary(); // refresh
        });

        card.getChildren().addAll(header, subtitle, statsRow, vitals, toggleBtn);
        return card;
    }

    private VBox miniStat(String name, String value) {
        VBox box = new VBox(0);
        box.setAlignment(Pos.CENTER);
        Label n = new Label(name);
        n.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #00E5FF; -fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;");
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
        VBox root = UIFactory.screenRoot();
        root.getChildren().add(UIFactory.headerBar("EVENT", null));

        // Event card
        VBox card = new VBox(12);
        card.setPadding(new Insets(20, 20, 20, 20));
        VBox.setVgrow(card, Priority.ALWAYS);

        // Category badge
        String catColor = switch (event.getCategory()) {
            case POSITIVE -> UIFactory.GREEN;
            case NEGATIVE -> UIFactory.RED;
            case CHOICE   -> UIFactory.CYAN;
            case NEUTRAL  -> UIFactory.DIM;
        };
        Label catBadge = new Label("[ " + event.getCategory().name() + " ]");
        catBadge.setStyle("-fx-text-fill: " + catColor + "; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        // Title
        Label title = new Label(event.getTitle());
        title.setStyle(
            "-fx-text-fill: " + catColor + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, " + catColor + ", 8, 0.3, 0, 0);"
        );
        title.setWrapText(true);

        // Narrative
        VBox narrativePanel = UIFactory.panel();
        Label narrative = new Label(event.getNarrative());
        narrative.setWrapText(true);
        narrative.setStyle("-fx-text-fill: #8899AA; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        narrativePanel.getChildren().add(narrative);

        card.getChildren().addAll(catBadge, title, narrativePanel, UIFactory.spacer());

        // Choices
        if (event.hasChoices()) {
            Label choiceTitle = UIFactory.sectionTitle("CHOOSE YOUR ACTION:");
            card.getChildren().add(choiceTitle);

            for (int i = 0; i < event.getChoices().length; i++) {
                final int idx = i;
                DungeonEvent.EventChoice choice = event.getChoices()[i];

                Button btn = UIFactory.btnPrimary("▶  " + choice.label);
                btn.setOnAction(e -> {
                    engine.resolveEventChoice(event, idx);
                    router.showDungeonMap();
                });
                card.getChildren().add(btn);
            }
        } else {
            Button okBtn = UIFactory.btnPrimary("CONTINUE");
            okBtn.setOnAction(e -> router.showDungeonMap());
            card.getChildren().add(okBtn);
        }

        root.getChildren().add(card);
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
        VBox root = UIFactory.screenRoot();
        root.getChildren().add(UIFactory.headerWithResources(
                "SHOP", () -> router.showDungeonMap(),
                engine.getPlayer().getGold(), 0));

        // Merchant header
        VBox header = new VBox(4);
        header.setPadding(new Insets(12, 16, 8, 16));
        header.setStyle("-fx-background-color: #0C1220; -fx-border-color: #1C2E44; -fx-border-width: 0 0 1 0;");

        Label merchantName = new Label("⚙ WANDERING MERCHANT");
        merchantName.setStyle(
            "-fx-text-fill: #FFAA00; -fx-font-family: 'Courier New';" +
            "-fx-font-size: 14px; -fx-font-weight: bold;"
        );
        Label merchantQuote = new Label("\"I have just what you need... for the right price.\"");
        merchantQuote.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        header.getChildren().addAll(merchantName, merchantQuote);
        root.getChildren().add(header);

        // Shop items
        Label itemsTitle = UIFactory.sectionTitle("FOR SALE");
        itemsTitle.setPadding(new Insets(10, 16, 4, 16));
        root.getChildren().add(itemsTitle);

        VBox itemList = new VBox(8);
        itemList.setPadding(new Insets(8, 16, 8, 16));
        VBox.setVgrow(itemList, Priority.ALWAYS);

        for (arclightcity.item.Item item : shopItems) {
            itemList.getChildren().add(buildShopRow(item));
        }

        ScrollPane scroll = new ScrollPane(itemList);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #050810; -fx-background: #050810; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);

        // Leave button
        VBox bottom = new VBox(0);
        bottom.setPadding(new Insets(10, 16, 12, 16));
        bottom.setStyle("-fx-border-color: #1C2E44; -fx-border-width: 1 0 0 0;");
        Button leave = UIFactory.btnPrimary("← LEAVE SHOP");
        leave.setOnAction(e -> router.showDungeonMap());
        bottom.getChildren().add(leave);
        root.getChildren().add(bottom);

        UIFactory.fadeIn(root, 300);
        return root;
    }

    private HBox buildShopRow(arclightcity.item.Item item) {
        // Harga berdasarkan rarity
        int price = switch (item.getRarity()) {
            case COMMON    -> 30  + (int)(Math.random() * 20);
            case UNCOMMON  -> 80  + (int)(Math.random() * 40);
            case RARE      -> 200 + (int)(Math.random() * 100);
            case EPIC      -> 500 + (int)(Math.random() * 200);
            case LEGENDARY -> 1200+ (int)(Math.random() * 300);
        };

        HBox row = new HBox(10);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
            "-fx-background-color: #0C1220;" +
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
        statLabel.setStyle("-fx-text-fill: #8899AA; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        info.getChildren().addAll(name, statLabel);

        // Price + Buy button
        VBox buyBox = new VBox(4);
        buyBox.setAlignment(Pos.CENTER_RIGHT);

        Label priceLabel = new Label("⚙ " + price);
        priceLabel.setStyle(
            "-fx-text-fill: #FFD600; -fx-font-family: 'Courier New';" +
            "-fx-font-size: 12px; -fx-font-weight: bold;"
        );

        boolean canAfford = engine.getPlayer().getGold() >= price;
        Button buyBtn = new Button("BUY");
        buyBtn.setStyle(
            "-fx-background-color: " + (canAfford ? "#FFD60022" : "#1C2E4422") + ";" +
            "-fx-border-color: " + (canAfford ? "#FFD600" : "#5A6A80") + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + (canAfford ? "#FFD600" : "#5A6A80") + ";" +
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
        VBox root = UIFactory.screenRoot();
        root.setAlignment(Pos.CENTER);

        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40, 30, 40, 30));

        Label victory = new Label("VICTORY");
        victory.setStyle(
            "-fx-text-fill: #FFD600;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 42px;" +
            "-fx-font-weight: bold;" +
            // Reduced: radius 6 spread 0.3 — readable gold glow
            "-fx-effect: dropshadow(gaussian, #FFD600, 6, 0.3, 0, 0);"
        );

        Label turns = new Label("Completed in " + result.getTurnsElapsed() + " turns");
        turns.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        VBox rewards = UIFactory.panelHighlight(
            UIFactory.sectionTitle("REWARDS"),
            UIFactory.statRowHighlight("EXP Gained", "+" + UIFactory.formatNumber((long)result.getTotalExpGained()), UIFactory.YELLOW),
            UIFactory.statRowHighlight("Gold Gained", "+" + UIFactory.formatNumber(result.getTotalGoldGained()), UIFactory.YELLOW),
            UIFactory.statRow("Loot Drops", result.getLootItemIds().size() + " item(s)")
        );

        Button continueBtn = UIFactory.btnGold("▶ CONTINUE DUNGEON");
        continueBtn.setOnAction(e -> router.showDungeonMap());

        Button hubBtn = UIFactory.btnPrimary("← RETURN TO HUB");
        hubBtn.setOnAction(e -> router.showHub());

        content.getChildren().addAll(victory, turns, rewards, continueBtn, hubBtn);
        root.getChildren().add(content);

        UIFactory.fadeIn(root, 600);
        UIFactory.glowPulse(victory, "#FFD600");
        return root;
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
        VBox root = UIFactory.screenRoot();
        root.setAlignment(Pos.CENTER);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(60, 30, 60, 30));

        Label dead = new Label("SYSTEM FAILURE");
        dead.setStyle(
            "-fx-text-fill: #FF1744;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;" +
            // Reduced: radius 6 — readable red glow
            "-fx-effect: dropshadow(gaussian, #FF1744, 6, 0.3, 0, 0);"
        );

        Label sub = new Label("CONNECTION LOST");
        sub.setStyle("-fx-text-fill: #5A6A80; -fx-font-family: 'Courier New'; -fx-font-size: 13px; -fx-letter-spacing: 4px;");

        var player = engine.getPlayer();
        VBox stats = UIFactory.panel(
            UIFactory.sectionTitle("SESSION STATS"),
            UIFactory.statRow("Floor Reached",   String.valueOf(player.getDungeonDepth())),
            UIFactory.statRow("Total Damage",     UIFactory.formatNumber((long)player.getTotalDamageDealt())),
            UIFactory.statRow("Level Reached",    String.valueOf(player.getLevel()))
        );

        Label quote = new Label("\"Death is just another form of data loss.\"");
        quote.setStyle("-fx-text-fill: #2A3A50; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        quote.setWrapText(true);

        Button retry = UIFactory.btnDanger("↺ TRY AGAIN");
        retry.setOnAction(e -> {
            engine.createCharacter(player.getName(), player.getBackground());
            router.showHub();
        });

        Button menu = UIFactory.btnPrimary("← MAIN MENU");
        menu.setOnAction(e -> router.showMainMenu());

        content.getChildren().addAll(dead, sub, stats, quote, retry, menu);
        root.getChildren().add(content);

        UIFactory.fadeIn(root, 800);
        return root;
    }
}

} // end ViewsBundle
