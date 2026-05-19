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

    private VBox buildEquipmentSlots(Inventory inv) {
        VBox container = new VBox(0);
        container.setStyle("-fx-background-color: #0F0A06;" +
                           "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Label title = new Label("── PERLENGKAPAN TERPASANG ──");
        title.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 10px; -fx-letter-spacing: 2; -fx-padding: 8 16 6 16;");
        container.getChildren().add(title);

        // Layout 5 kolom: [Art1] | kiri | tengah | kanan | [Art2]
        HBox layout = new HBox(6);
        layout.setPadding(new Insets(4, 16, 10, 16));
        layout.setAlignment(Pos.CENTER);

        // ── Artifact Slot 1 (KIRI) ────────────────────────────
        javafx.scene.layout.StackPane artLeft = buildArtifactEquipSlot(1, inv.getArtifactSlot1(), inv);

        // ── Kiri: Weapon + Armor ──────────────────────────────
        VBox left = new VBox(6);
        left.setAlignment(Pos.CENTER);
        left.setMinWidth(110);
        left.getChildren().add(buildEquipSlot("⚔", "SENJATA", inv.getEquippedWeapon(), inv));
        left.getChildren().add(buildEquipSlot("🛡", "BAJU BESI", inv.getEquippedArmor(), inv));

        // ── Tengah: Helm + Boots ──────────────────────────────
        VBox center = new VBox(6);
        center.setAlignment(Pos.CENTER);
        center.setMinWidth(110);
        center.getChildren().add(buildEquipSlot("👑", "HELM", inv.getEquippedHelmet(), inv));
        center.getChildren().add(buildEquipSlot("👢", "SEPATU", inv.getEquippedBoots(), inv));

        // ── Kanan: Ring1 + Ring2 ─────────────────────────────
        VBox right = new VBox(6);
        right.setAlignment(Pos.CENTER);
        right.setMinWidth(110);
        right.getChildren().add(buildEquipSlot("💍", "CINCIN 1", inv.getEquippedRing1(), inv));
        right.getChildren().add(buildEquipSlot("💍", "CINCIN 2", inv.getEquippedRing2(), inv));

        // ── Artifact Slot 2 (KANAN) ───────────────────────────
        javafx.scene.layout.StackPane artRight = buildArtifactEquipSlot(2, inv.getArtifactSlot2(), inv);

        layout.getChildren().addAll(artLeft, left, center, right, artRight);
        container.getChildren().add(layout);

        // ── Baris 2: Aksesori 1 + 2 (lebar penuh) ────────────
        HBox accRow = new HBox(6);
        accRow.setPadding(new Insets(0, 16, 8, 16));
        accRow.setAlignment(Pos.CENTER);

        VBox accLeft = new VBox(0);
        accLeft.setAlignment(Pos.CENTER);
        HBox.setHgrow(accLeft, Priority.ALWAYS);
        accLeft.getChildren().add(buildEquipSlotWide("◈", "AKSESORI 1", inv.getEquippedAccessory1(), inv));

        VBox accRight = new VBox(0);
        accRight.setAlignment(Pos.CENTER);
        HBox.setHgrow(accRight, Priority.ALWAYS);
        accRight.getChildren().add(buildEquipSlotWide("◈", "AKSESORI 2", inv.getEquippedAccessory2(), inv));

        accRow.getChildren().addAll(accLeft, accRight);
        container.getChildren().add(accRow);



        return container;
    }

    /** Slot artefak 130×130 untuk panel equipment (PERBENDAHARAAN) */
    private javafx.scene.layout.StackPane buildArtifactEquipSlot(
            int slotNum, arclightcity.item.Artifact art, Inventory inv) {
        javafx.scene.layout.StackPane slot = new javafx.scene.layout.StackPane();
        slot.setPrefSize(130, 130); slot.setMinSize(130, 130); slot.setMaxSize(130, 130);
        slot.setCursor(javafx.scene.Cursor.HAND);

        String borderColor = art != null ? art.getBorderColor() : "#3A1A5A";
        slot.setStyle("-fx-background-color:#0A0514; -fx-border-color:" + borderColor +
            "; -fx-border-width:1.5; -fx-border-radius:4; -fx-background-radius:4;");

        if (art == null) {
            // Empty state
            Label plus = new Label("+");
            plus.setStyle("-fx-text-fill:rgba(170,100,255,0.30); -fx-font-size:32px;");
            Label lbl = new Label("ARTEFAK " + slotNum);
            lbl.setStyle("-fx-text-fill:rgba(170,100,255,0.35); -fx-font-family:'Courier New'; -fx-font-size:8px;");
            javafx.scene.layout.VBox emp = new javafx.scene.layout.VBox(2, plus, lbl);
            emp.setAlignment(javafx.geometry.Pos.CENTER);
            slot.getChildren().add(emp);
            slot.setOnMouseClicked(e -> showArtifactPickForSlot(slotNum, inv));
        } else {
            // Filled state — tampilkan icon
            if (art.hasGlowEffect())
                slot.setEffect(new javafx.scene.effect.Glow(0.25));
            javafx.scene.image.Image icon = arclightcity.ui.util.AssetManager.artifactIcon(art.getArtifactType());
            if (icon != null) {
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(icon);
                iv.setFitWidth(80); iv.setFitHeight(80); iv.setPreserveRatio(true);
                slot.getChildren().add(iv);
            }
            // CD indicator
            String cdTxt = art.isReady() ? "SIAP" : "CD " + art.getCooldown();
            Label cdLbl = new Label(cdTxt);
            cdLbl.setStyle("-fx-text-fill:" + (art.isReady() ? "#44FF44" : "#AAAAAA") +
                "; -fx-font-family:'Courier New'; -fx-font-size:8px; -fx-padding:2;");
            javafx.scene.layout.StackPane.setAlignment(cdLbl, javafx.geometry.Pos.BOTTOM_CENTER);
            slot.getChildren().add(cdLbl);

            // Click → info popup
            slot.setOnMouseClicked(e -> showArtifactSlotInfo(art, slotNum, inv));
        }
        return slot;
    }

    /** Popup pilih artefak dari bag untuk di-equip ke slot player */
    private void showArtifactPickForSlot(int slotNum, Inventory inv) {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.initOwner(router.getStage());
        popup.setTitle("Pilih Artefak — Slot " + slotNum);

        VBox root = new VBox(8);
        root.setStyle("-fx-background-color:#0A0514; -fx-border-color:#5A1A8A;" +
            "-fx-border-width:2; -fx-padding:16;");
        root.setPrefWidth(310);

        Label hdr = new Label("⬡  PILIH ARTEFAK — SLOT " + slotNum);
        hdr.setStyle("-fx-text-fill:#AA66FF; -fx-font-family:'Courier New'; -fx-font-size:12px; -fx-font-weight:bold;");
        root.getChildren().add(hdr);

        boolean found = false;
        for (arclightcity.item.Item item : inv.getAllBagItems()) {
            if (!(item instanceof arclightcity.item.Artifact art)) continue;
            found = true;

            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setPadding(new javafx.geometry.Insets(6, 10, 6, 10));
            row.setStyle("-fx-background-color:rgba(255,255,255,0.03); -fx-cursor:hand;");

            javafx.scene.image.Image ic = arclightcity.ui.util.AssetManager.artifactIcon(art.getArtifactType());
            if (ic != null) {
                javafx.scene.image.ImageView mini = new javafx.scene.image.ImageView(ic);
                mini.setFitWidth(30); mini.setFitHeight(30); mini.setPreserveRatio(true);
                row.getChildren().add(mini);
            }
            VBox info2 = new VBox(2);
            Label nm = new Label(art.getArtifactType().displayName);
            nm.setStyle("-fx-text-fill:" + art.getBorderColor() + "; -fx-font-family:'Courier New'; -fx-font-size:11px;");
            Label rm = new Label("[" + art.getRarity().displayName + "] · " + art.getArtifactType().role.name());
            rm.setStyle("-fx-text-fill:rgba(255,255,255,0.35); -fx-font-family:'Courier New'; -fx-font-size:9px;");
            info2.getChildren().addAll(nm, rm);
            row.getChildren().add(info2);

            row.setOnMouseClicked(e2 -> {
                inv.equipArtifactToSlot(slotNum, art);
                popup.close();
                router.showInventory();
            });
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:rgba(170,100,255,0.10); -fx-cursor:hand;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color:rgba(255,255,255,0.03); -fx-cursor:hand;"));
            root.getChildren().add(row);
        }
        if (!found) {
            Label none = new Label("Belum punya artefak. Buka Altar Artefak.");
            none.setStyle("-fx-text-fill:rgba(255,255,255,0.30); -fx-font-family:'Courier New'; -fx-font-size:10px;");
            root.getChildren().add(none);
        }
        Button cancel = new Button("TUTUP");
        cancel.setStyle("-fx-background-color:transparent;-fx-border-color:#444;-fx-border-width:1;" +
            "-fx-text-fill:#888;-fx-font-family:'Courier New';-fx-font-size:10px;-fx-cursor:hand;");
        cancel.setOnAction(e -> popup.close());
        root.getChildren().add(cancel);

        popup.setScene(new javafx.scene.Scene(root));
        popup.sizeToScene(); popup.show();
    }

    /** Info popup artefak yang sudah terpasang di slot player */
    private void showArtifactSlotInfo(arclightcity.item.Artifact art, int slotNum, Inventory inv) {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.initOwner(router.getStage());
        popup.setTitle(art.getArtifactType().displayName);

        VBox root = new VBox(10);
        root.setStyle("-fx-background-color:#0C0514; -fx-border-color:" + art.getBorderColor() +
            "; -fx-border-width:2; -fx-padding:20;");
        root.setPrefWidth(300);

        javafx.scene.image.Image icon = arclightcity.ui.util.AssetManager.artifactIcon(art.getArtifactType());
        if (icon != null) {
            javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(icon);
            iv.setFitWidth(72); iv.setFitHeight(72); iv.setPreserveRatio(true);
            javafx.scene.layout.StackPane iBox = new javafx.scene.layout.StackPane(iv);
            iBox.setAlignment(javafx.geometry.Pos.CENTER);
            root.getChildren().add(iBox);
        }
        Label name = new Label(art.getArtifactType().displayName);
        name.setStyle("-fx-text-fill:" + art.getBorderColor() + ";-fx-font-family:'Courier New';" +
            "-fx-font-size:13px;-fx-font-weight:bold;");
        Label desc = new Label(art.getDisplaySummary());
        desc.setWrapText(true); desc.setMaxWidth(270);
        desc.setStyle("-fx-text-fill:rgba(240,230,255,0.75);-fx-font-family:'Courier New';-fx-font-size:11px;");
        Label cdLbl = new Label("CD: " + art.getScaledCooldown() + " giliran · " +
                                (art.isReady() ? "SIAP" : "CD " + art.getCooldown()));
        cdLbl.setStyle("-fx-text-fill:rgba(255,255,255,0.30);-fx-font-family:'Courier New';-fx-font-size:9px;");

        HBox btnRow = new HBox(8); btnRow.setAlignment(javafx.geometry.Pos.CENTER);
        Button lepas = new Button("LEPAS ARTEFAK");
        lepas.setStyle("-fx-background-color:transparent;-fx-border-color:#883333;-fx-border-width:1;" +
            "-fx-text-fill:#AA4444;-fx-font-family:'Courier New';-fx-font-size:10px;-fx-cursor:hand;");
        lepas.setOnAction(e -> {
            inv.unequipArtifact(slotNum);
            popup.close();
            router.showInventory();
        });
        Button close = new Button("TUTUP");
        close.setStyle("-fx-background-color:transparent;-fx-border-color:#444;-fx-border-width:1;" +
            "-fx-text-fill:#888;-fx-font-family:'Courier New';-fx-font-size:10px;-fx-cursor:hand;");
        close.setOnAction(e -> popup.close());
        btnRow.getChildren().addAll(lepas, close);

        root.getChildren().addAll(name, desc, cdLbl, btnRow);
        popup.setScene(new javafx.scene.Scene(root));
        popup.sizeToScene(); popup.show();
    }

    /** Slot lebar untuk baris aksesori */
    private HBox buildEquipSlotWide(String icon, String slotName, Equipment eq, Inventory inv) {
        HBox slot = new HBox(10);
        slot.setAlignment(Pos.CENTER_LEFT);
        slot.setPadding(new Insets(6, 10, 6, 10));
        slot.setMaxWidth(Double.MAX_VALUE);
        slot.setCursor(eq != null ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 14px;" + (eq == null ? "-fx-opacity: 0.3;" : ""));

        VBox info = new VBox(1);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLbl = new Label(slotName);
        nameLbl.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 8px;");

        if (eq == null) {
            slot.setStyle("-fx-background-color: #0A0604; -fx-border-color: #2A1808; -fx-border-width: 1;");
            Label emptyLbl = new Label("— Kosong —");
            emptyLbl.setStyle("-fx-text-fill: #2A1808; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
            info.getChildren().addAll(nameLbl, emptyLbl);
        } else {
            String rc = UIFactory.rarityColor(eq.getRarity());
            slot.setStyle("-fx-background-color: " + rc + "11; -fx-border-color: " + rc +
                          "; -fx-border-width: 1 1 1 3;");
            Label itemLbl = new Label(eq.getName());
            itemLbl.setStyle("-fx-text-fill: " + rc + "; -fx-font-family: 'Courier New';" +
                             "-fx-font-size: 11px; -fx-font-weight: bold;");
            Label upgLbl = eq.getUpgradeLevel() > 0
                ? new Label("+" + eq.getUpgradeLevel() + " | " + eq.getRarity().displayName)
                : new Label(eq.getRarity().displayName);
            upgLbl.setStyle("-fx-text-fill: " + rc + "88; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
            info.getChildren().addAll(nameLbl, itemLbl, upgLbl);
            slot.setOnMouseClicked(e -> showItemDetailPopup(eq, inv));
        }
        slot.getChildren().addAll(iconLbl, info);
        return slot;
    }

    private VBox buildEquipSlot(String icon, String slotName, Equipment eq, Inventory inv) {
        VBox slot = new VBox(2);
        slot.setAlignment(Pos.CENTER);
        slot.setPrefWidth(105);
        slot.setCursor(eq != null ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);

        if (eq == null) {
            slot.setStyle("-fx-background-color: #0A0604; -fx-border-color: #2A1808;" +
                          "-fx-border-width: 1; -fx-padding: 8;");
            Label iconLbl = new Label(icon);
            iconLbl.setStyle("-fx-font-size: 16px; -fx-opacity: 0.3;");
            Label nameLbl = new Label(slotName);
            nameLbl.setStyle("-fx-text-fill: #2A1808; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
            Label emptyLbl = new Label("— Kosong —");
            emptyLbl.setStyle("-fx-text-fill: #2A1808; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
            slot.getChildren().addAll(iconLbl, nameLbl, emptyLbl);
        } else {
            String rc = UIFactory.rarityColor(eq.getRarity());
            slot.setStyle("-fx-background-color: " + rc + "11;" +
                          "-fx-border-color: " + rc + ";" +
                          "-fx-border-width: 1 1 1 3; -fx-padding: 8;");

            Label iconLbl = new Label(icon);
            iconLbl.setStyle("-fx-font-size: 16px;");

            Label nameLbl = new Label(slotName);
            nameLbl.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 8px;");

            String displayName = eq.getName().length() > 11
                ? eq.getName().substring(0, 10) + "…" : eq.getName();
            Label itemLbl = new Label(displayName);
            itemLbl.setStyle("-fx-text-fill: " + rc + "; -fx-font-family: 'Courier New';" +
                             "-fx-font-size: 10px; -fx-font-weight: bold;");
            itemLbl.setWrapText(true);
            itemLbl.setMaxWidth(100);

            HBox badges = new HBox(4);
            badges.setAlignment(Pos.CENTER);
            if (eq.getUpgradeLevel() > 0) {
                Label upg = new Label("+" + eq.getUpgradeLevel());
                upg.setStyle("-fx-text-fill: #2D7A45; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
                badges.getChildren().add(upg);
            }
            Label rar = new Label("[" + eq.getRarity().displayName + "]");
            rar.setStyle("-fx-text-fill: " + rc + "77; -fx-font-family: 'Courier New'; -fx-font-size: 8px;");
            badges.getChildren().add(rar);

            slot.getChildren().addAll(iconLbl, nameLbl, itemLbl, badges);

            // Klik → popup detail item
            slot.setOnMouseClicked(e -> showItemDetailPopup(eq, inv));
            slot.setOnMouseEntered(ev ->
                slot.setStyle(slot.getStyle().replace(rc + "11", rc + "22")));
            slot.setOnMouseExited(ev ->
                slot.setStyle(slot.getStyle().replace(rc + "22", rc + "11")));
        }
        return slot;
    }

    /** Popup detail item seperti screenshot game RPG */
    private void showItemDetailPopup(Equipment eq, Inventory inv) {
        boolean isEquipped = inv.getAllEquipped().contains(eq);
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle(eq.getFullName());

        String rc = UIFactory.rarityColor(eq.getRarity());

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #0F0A06; -fx-border-color: " + rc +
                      "; -fx-border-width: 2;");
        root.setPrefWidth(340);

        // Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(14, 16, 12, 16));
        header.setStyle("-fx-background-color: #1A1008;" +
                        "-fx-border-color: " + rc + "44; -fx-border-width: 0 0 2 0;");

        Label nameLabel = new Label(eq.getFullName());
        nameLabel.setStyle("-fx-text-fill: " + rc + "; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 16px; -fx-font-weight: bold;" +
                           "-fx-effect: dropshadow(gaussian, " + rc + ", 8, 0.3, 0, 0);");
        nameLabel.setWrapText(true);

        HBox meta = new HBox(10);
        Label rarLabel = new Label(eq.getRarity().displayName.toUpperCase());
        rarLabel.setStyle("-fx-text-fill: " + rc + "; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 10px; -fx-font-weight: bold;" +
                          "-fx-border-color: " + rc + "; -fx-border-width: 1; -fx-padding: 1 6;");
        Label typeLabel = new Label(eq.getItemType().name());
        typeLabel.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        if (eq.getUpgradeLevel() > 0) {
            Label upgLabel = new Label("+" + eq.getUpgradeLevel() + " UPGRADE");
            upgLabel.setStyle("-fx-text-fill: #2D7A45; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
            meta.getChildren().addAll(rarLabel, typeLabel, upgLabel);
        } else {
            meta.getChildren().addAll(rarLabel, typeLabel);
        }

        // Calibration bar (10 dots)
        int calLvl = eq.getCalibrationLevel();
        HBox calBar = new HBox(3);
        calBar.setAlignment(Pos.CENTER_LEFT);
        Label calTitle = new Label("KALIBRASI  ");
        calTitle.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
        calBar.getChildren().add(calTitle);
        for (int i = 0; i < 10; i++) {
            Label dot = new Label("■");
            dot.setStyle("-fx-text-fill: " + (i < calLvl ? "#7755BB" : "#2A1808") +
                         "; -fx-font-size: 9px;");
            calBar.getChildren().add(dot);
        }

        // Upgrade bar
        int upgLvl = eq.getUpgradeLevel();
        HBox upgBar = new HBox(3);
        Label upgTitle = new Label("UPGRADE    ");
        upgTitle.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
        upgBar.setAlignment(Pos.CENTER_LEFT);
        upgBar.getChildren().add(upgTitle);
        for (int i = 0; i < 10; i++) {
            Label dot = new Label("■");
            dot.setStyle("-fx-text-fill: " + (i < upgLvl ? "#2D7A45" : "#2A1808") +
                         "; -fx-font-size: 9px;");
            upgBar.getChildren().add(dot);
        }

        header.getChildren().addAll(nameLabel, meta, calBar, upgBar);

        // Stats
        VBox statsBox = new VBox(2);
        statsBox.setPadding(new Insets(12, 16, 8, 16));
        statsBox.setStyle("-fx-border-color: #2A1808; -fx-border-width: 0 0 1 0;");

        Label descLbl = new Label(eq.getDescription());
        descLbl.setWrapText(true);
        descLbl.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 10px; -fx-font-style: italic; -fx-padding: 0 0 8 0;");
        statsBox.getChildren().add(descLbl);

        eq.getEffectiveStats().forEach((stat, val) -> {
            if (val == 0) return;
            HBox row = new HBox();
            Label sn = new Label(stat.displayName);
            sn.setStyle("-fx-text-fill: #A09070; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            HBox.setHgrow(sn, Priority.ALWAYS);
            // Gunakan StatType.isPercent() — lebih akurat dari val<1.0 heuristic
            // (CRIT_DAMAGE bisa 1.20+ tapi tetap percent stat)
            String formatted = stat.isPercent()
                ? String.format("+%.0f%%", val * 100)
                : (val >= 1 ? String.format("+%.0f", val)
                            : String.format("+%.2f", val));
            Label sv = new Label(formatted);
            sv.setStyle("-fx-text-fill: #2D7A45; -fx-font-family: 'Courier New';" +
                        "-fx-font-size: 11px; -fx-font-weight: bold;");
            row.getChildren().addAll(sn, sv);
            statsBox.getChildren().add(row);
        });

        // Action buttons
        HBox actions = new HBox(8);
        actions.setPadding(new Insets(10, 16, 14, 16));

        Button unequipBtn = new Button(isEquipped ? "LEPAS" : "PAKAI");
        String btnColor = isEquipped ? "#CC3300" : "#006633";
        String btnText  = isEquipped ? "#FF5533" : "#44FF88";
        unequipBtn.setStyle("-fx-background-color: transparent; -fx-border-color: " + btnColor + ";" +
                            "-fx-border-width: 1; -fx-text-fill: " + btnText + ";" +
                            "-fx-font-family: \'Courier New\'; -fx-font-size: 11px;" +
                            "-fx-padding: 6 14; -fx-cursor: hand;");
        unequipBtn.setOnAction(e -> {
            if (isEquipped) {
                inv.unequip(eq);
                router.addSystemChat("✓ " + eq.getName() + " dilepas.");
                popup.close();
                router.showInventory();
            } else {
                // Cek apakah perlu pilih slot (ring/aksesori saat kedua slot penuh)
                boolean needSlotPick = false;
                String forcedSlot = null;
                if (eq instanceof arclightcity.item.Armor ar &&
                    ar.getArmorType() == arclightcity.item.Armor.ArmorType.RING &&
                    inv.getEquippedRing1() != null && inv.getEquippedRing2() != null) {
                    needSlotPick = true;
                } else if (eq.getItemType() == arclightcity.item.Item.ItemType.ACCESSORY &&
                    inv.getEquippedAccessory1() != null && inv.getEquippedAccessory2() != null) {
                    needSlotPick = true;
                }

                if (needSlotPick) {
                    boolean isRing = eq instanceof arclightcity.item.Armor ar2 &&
                                     ar2.getArmorType() == arclightcity.item.Armor.ArmorType.RING;
                    String s1 = isRing ? "RING_1" : "ACCESSORY_1";
                    String s2 = isRing ? "RING_2" : "ACCESSORY_2";
                    // Tampilkan mini-dialog pilih slot
                    javafx.stage.Stage slotDlg = new javafx.stage.Stage();
                    slotDlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                    slotDlg.setTitle("Pilih Slot");
                    javafx.scene.layout.VBox dlgBox = new javafx.scene.layout.VBox(10);
                    dlgBox.setPadding(new javafx.geometry.Insets(16));
                    dlgBox.setStyle("-fx-background-color: #0D0805;");
                    javafx.scene.control.Label dlgLbl = new javafx.scene.control.Label("Ganti slot mana?");
                    dlgLbl.setStyle("-fx-text-fill: #C8860A; -fx-font-family: \'Courier New\'; -fx-font-size: 12px;");
                    String n1 = isRing ? (inv.getEquippedRing1() != null ? inv.getEquippedRing1().getName() : "-") :
                                         (inv.getEquippedAccessory1() != null ? inv.getEquippedAccessory1().getName() : "-");
                    String n2 = isRing ? (inv.getEquippedRing2() != null ? inv.getEquippedRing2().getName() : "-") :
                                         (inv.getEquippedAccessory2() != null ? inv.getEquippedAccessory2().getName() : "-");
                    javafx.scene.control.Button b1 = new javafx.scene.control.Button("Slot 1: " + n1);
                    javafx.scene.control.Button b2 = new javafx.scene.control.Button("Slot 2: " + n2);
                    String btnStyle = "-fx-background-color: #1A1008; -fx-border-color: #C8860A; -fx-border-width: 1;" +
                                      "-fx-text-fill: #FFB830; -fx-font-family: \'Courier New\'; -fx-font-size: 11px; -fx-cursor: hand;";
                    b1.setStyle(btnStyle); b2.setStyle(btnStyle);
                    b1.setMaxWidth(Double.MAX_VALUE); b2.setMaxWidth(Double.MAX_VALUE);
                    b1.setOnAction(ev2 -> {
                        inv.unequip(s1); inv.equip(eq);
                        router.addSystemChat("✓ " + eq.getName() + " ke Slot 1.");
                        slotDlg.close(); popup.close(); router.showInventory();
                    });
                    b2.setOnAction(ev2 -> {
                        inv.unequip(s2); inv.equip(eq);
                        router.addSystemChat("✓ " + eq.getName() + " ke Slot 2.");
                        slotDlg.close(); popup.close(); router.showInventory();
                    });
                    dlgBox.getChildren().addAll(dlgLbl, b1, b2);
                    slotDlg.setScene(new javafx.scene.Scene(dlgBox, 280, 130));
                    slotDlg.showAndWait();
                } else {
                    inv.equip(eq);
                    router.addSystemChat("✓ " + eq.getName() + " dipakai.");
                    popup.close();
                    router.showInventory();
                }
            }
        });

        // Tombol Upgrade langsung dari popup — tampil biaya dulu
        int upgLvl2 = eq.getUpgradeLevel();
        int[] upgCost = switch (eq.getRarity()) {
            case COMMON    -> new int[]{2*(upgLvl2+1), 0,          0};
            case UNCOMMON  -> new int[]{4*(upgLvl2+1), 1*(upgLvl2+1), 0};
            case RARE      -> new int[]{6*(upgLvl2+1), 2*(upgLvl2+1), 1*(upgLvl2+1)};
            case EPIC      -> new int[]{8*(upgLvl2+1), 4*(upgLvl2+1), 2*(upgLvl2+1)};
            case LEGENDARY -> new int[]{10*(upgLvl2+1),6*(upgLvl2+1), 4*(upgLvl2+1)};
            default        -> new int[]{2, 0, 0};
        };
        boolean canUpgrade = inv.getScrapMetal() >= upgCost[0]
            && inv.getCyberChips() >= upgCost[1]
            && inv.getNeonCrystals() >= upgCost[2]
            && eq.canUpgrade();
        String upgLabel = "⬆ UPGRADE  (Scrap:" + upgCost[0]
            + " Chip:" + upgCost[1] + " Crystal:" + upgCost[2] + ")";
        Button upgradeBtn = new Button(upgLabel);
        upgradeBtn.setStyle("-fx-background-color:" + (canUpgrade?"#C8860A15":"#2A180822") +
            "; -fx-border-color:" + (canUpgrade?"#C8860A":"#5A3A10") +
            "; -fx-border-width: 1; -fx-text-fill:" + (canUpgrade?"#FFB830":"#5A3A10") +
            "; -fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
            "; -fx-padding: 6 10; -fx-cursor: hand; -fx-wrap-text: true;");
        upgradeBtn.setDisable(!canUpgrade);
        if (!canUpgrade && !eq.canUpgrade()) upgradeBtn.setText("⬆ UPGRADE MAX");
        upgradeBtn.setOnAction(e -> {
            var res = inv.upgradeItem(eq.getId());
            popup.close();
            router.addSystemChat((res.success ? "⬆ " : "✗ ") + eq.getName() + ": " + res.message);
            router.showInventory();
        });

        // Tombol Kalibrasi — cek Cal Kit, lakukan kalibrasi, tampil hasil
        boolean hasCalKit = inv.getCalibrationKits() > 0;
        Button calibBtn = new Button("◈ KALIBRASI" + (hasCalKit ? "" : "  (Cal Kit: 0)"));
        calibBtn.setStyle("-fx-background-color:" + (hasCalKit?"#4455CC15":"#1A100822") +
            "; -fx-border-color:" + (hasCalKit?"#6677CC":"#3A2810") +
            "; -fx-border-width: 1; -fx-text-fill:" + (hasCalKit?"#88AAFF":"#5A3A10") +
            "; -fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
            "; -fx-padding: 6 14; -fx-cursor: hand;");
        calibBtn.setDisable(!hasCalKit);
        calibBtn.setOnAction(e -> {
            if (!hasCalKit) { showAlert("✗ Tidak punya Cal Kit!"); return; }
            var calResult = inv.calibrateItem(eq.getId(), 1);
            if (calResult.success) {
                router.addSystemChat("◈ " + eq.getName() + " dikalibrasi! +" +
                    calResult.message.replace("Calibration successful. ",""));
                popup.close();
                router.showInventory();
            } else {
                showAlert("✗ Kalibrasi gagal: " + calResult.message);
            }
        });

        Button closeBtn = new Button("TUTUP");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                          "-fx-border-width: 1; -fx-text-fill: #6A5840;" +
                          "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                          "-fx-padding: 6 14; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> popup.close());
        HBox.setHgrow(closeBtn, Priority.ALWAYS);

        // Baris 1: Upgrade + Kalibrasi
        HBox row1 = new HBox(8, upgradeBtn, calibBtn);
        // Baris 2: Lepas + Tutup
        HBox row2 = new HBox(8, unequipBtn, closeBtn);
        VBox actionRows = new VBox(6, row1, row2);
        actionRows.setPadding(new Insets(10, 16, 14, 16));

        root.getChildren().addAll(header, statsBox, actionRows);

        popup.setScene(new javafx.scene.Scene(root));
        popup.show();
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

        // Tombol expand bag (+)
        int curMax = inv.getMaxBagSize();
        boolean canExpand = curMax < 100;
        Button expandBtn = new Button(canExpand ? "+" : "MAX");
        expandBtn.setStyle("-fx-background-color:" + (canExpand?"#C8860A22":"#1A1008") +
            "; -fx-border-color:" + (canExpand?"#C8860A":"#3A2810") +
            "; -fx-border-width:1; -fx-border-radius:3; -fx-background-radius:3;" +
            "-fx-text-fill:" + (canExpand?"#FFB830":"#5A3A10") +
            "; -fx-font-family:'Courier New'; -fx-font-size:11px;" +
            "-fx-padding:2 8; -fx-cursor:" + (canExpand?"hand":"default") + ";");
        expandBtn.setDisable(!canExpand);
        expandBtn.setOnAction(e -> ViewsBundle.showBagExpandMenuStatic(inv, engine, router));

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(spacer, bagInfo, expandBtn);
        HBox.setHgrow(bar.getChildren().get(bar.getChildren().size() - 3), Priority.ALWAYS);

        return bar;
    }

    private HBox buildFilterTabs() {
        HBox tabs = new HBox(0);
        tabs.setStyle("-fx-background-color: #150E08; -fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        String[] filters = {"ALL","ARTIFACT","WEAPON","ARMOR","HELM","SEPATU","CINCIN","ACCESSORY","CONSUMABLE","MATERIAL"};
        String[] labels  = {"SEMUA","ARTEFAK","SENJATA","BAJU","HELM","SEPATU","CINCIN","AKSESORI","KONSUMABLE","MATERIAL"};
        for (int fi = 0; fi < filters.length; fi++) {
            final String filter = filters[fi];
            final String label  = labels[fi];
            Label tab = new Label(label);
            tab.setPadding(new Insets(8, 10, 8, 10));
            boolean active = filter.equals(activeFilter);
            tab.setStyle(
                "-fx-text-fill: " + (active ? UIFactory.CYAN : UIFactory.DIM) + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
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
                    if (activeFilter.equals("ARTIFACT")) return item instanceof arclightcity.item.Artifact;
                    // Aksesori (class Accessory) — bukan Armor
                    if (item instanceof arclightcity.item.Accessory) {
                        return activeFilter.equals("ACCESSORY");
                    }
                    // Armor dengan subtype khusus
                    if (item instanceof arclightcity.item.Armor armor) {
                        return switch (activeFilter) {
                            case "ARMOR"  -> armor.getArmorType() == arclightcity.item.Armor.ArmorType.LIGHT
                                          || armor.getArmorType() == arclightcity.item.Armor.ArmorType.MEDIUM
                                          || armor.getArmorType() == arclightcity.item.Armor.ArmorType.HEAVY
                                          || armor.getArmorType() == arclightcity.item.Armor.ArmorType.EXOSUIT;
                            case "HELM"   -> armor.getArmorType() == arclightcity.item.Armor.ArmorType.HELMET;
                            case "SEPATU" -> armor.getArmorType() == arclightcity.item.Armor.ArmorType.BOOTS;
                            case "CINCIN" -> armor.getArmorType() == arclightcity.item.Armor.ArmorType.RING;
                            default       -> false;
                        };
                    }
                    // Weapon, Consumable, Material
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
        row.setOnMouseClicked(e -> {
            if (item instanceof Equipment eq) {
                showItemDetailPopup(eq, inv);
            } else if (item instanceof arclightcity.item.Artifact art) {
                showArtifactBagPopup(art, inv, router);
            }
        });

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
                // Cek apakah slot sudah penuh (ring/accessory punya 2 slot)
                boolean isRing = eq instanceof arclightcity.item.Armor arm2
                    && arm2.getArmorType() == arclightcity.item.Armor.ArmorType.RING;
                boolean isAcc  = eq instanceof arclightcity.item.Accessory;

                if (isRing && inv.getEquippedRing1() != null && inv.getEquippedRing2() != null) {
                    // Kedua slot cincin penuh — tanya ganti yang mana
                    ViewsBundle.showSlotPickerStatic("Pilih slot cincin untuk diganti:",
                        new String[]{"Cincin Slot 1: " + inv.getEquippedRing1().getName(),
                                     "Cincin Slot 2: " + inv.getEquippedRing2().getName()},
                        choice -> {
                            if (choice == 0) inv.forceEquipRing1(eq);
                            else             inv.forceEquipRing2(eq);
                            router.showInventory();
                        });
                } else if (isAcc && inv.getEquippedAccessory1() != null && inv.getEquippedAccessory2() != null) {
                    ViewsBundle.showSlotPickerStatic("Pilih slot aksesori untuk diganti:",
                        new String[]{"Aksesori Slot 1: " + inv.getEquippedAccessory1().getName(),
                                     "Aksesori Slot 2: " + inv.getEquippedAccessory2().getName()},
                        choice -> {
                            if (choice == 0) inv.unequip(inv.getEquippedAccessory1());
                            else             inv.unequip(inv.getEquippedAccessory2());
                            inv.equip(eq);
                            router.showInventory();
                        });
                } else {
                    var result = inv.equip(eq);
                    if (!result.success) showAlert("❌ " + result.message);
                    router.showInventory();
                }
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
                boolean used = inv.useConsumable(item.getId(), engine.getPlayer(), engine.getFloorNumber());
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
            // Sort: tampilkan stat paling impactful dulu (ATK, HP, DEF)
            eq.getStatBonuses().entrySet().stream()
                .sorted((a, b) -> {
                    // Prioritas tampil: ATK > HP > DEF > lainnya
                    int ap = statPriority(a.getKey()), bp = statPriority(b.getKey());
                    return Integer.compare(ap, bp);
                })
                .limit(4)
                .forEach(entry -> {
                    String name = entry.getKey().displayName;
                    double val  = entry.getValue();
                    // Logika format: jika nilai < 2 → kemungkinan persen (0.0-1.0 range)
                    // Jika nilai >= 2 → tampil sebagai integer
                    String formatted;
                    // Gunakan isPercent() untuk konsistensi — gantikan heuristik val >= 2.0
                    if (entry.getKey().isPercent()) {
                        formatted = "+" + String.format("%.0f%%", val * 100);
                    } else if (val > 0) {
                        formatted = "+" + (val >= 1 ? (int)val : String.format("%.2f", val));
                    } else {
                        return; // skip nilai 0 atau negatif
                    }
                    sb.append(name).append(" ").append(formatted).append("  ");
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

    private static int statPriority(arclightcity.entity.stats.StatType st) {
        return switch(st) {
            case PHYSICAL_ATK, CYBER_ATK, ENERGY_ATK -> 1;
            case MAX_HP -> 2;
            case MAX_SHIELD -> 3;
            case PHYSICAL_DEF, CYBER_DEF, ENERGY_DEF -> 4;
            case CRIT_CHANCE, CRIT_DAMAGE -> 5;
            case DAMAGE_MULT -> 6;
            case LIFESTEAL -> 7;
            case BLEED_ON_HIT, BURN_ON_HIT, POISON_ON_HIT -> 8;
            case THORN -> 9;
            default -> 10;
        };
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
        StackPane rootStack = new StackPane();
        BorderPane root = UIFactory.screenRootBorder();

        // Background hub untuk layar guildmate
        try {
            var bg = arclightcity.ui.util.AssetManager.bgHub();
            if (bg != null) {
                var iv = arclightcity.ui.util.AssetManager.makeIVFill(bg,
                    arclightcity.ui.ArclightApp.GAME_WIDTH,
                    arclightcity.ui.ArclightApp.SCREEN_HEIGHT);
                iv.setOpacity(0.45); iv.setMouseTransparent(true);
                rootStack.getChildren().add(iv);
            }
        } catch(Exception ignored) {}
        rootStack.getChildren().add(root);

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
        Label crewBadge = new Label("  CREW: " + engine.getActiveMercs().size() + "/3  ");
        crewBadge.setStyle("-fx-background-color: #C8860A11; -fx-text-fill: #C8860A;" +
                           "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                           "-fx-padding: 2 8; -fx-border-color: #C8860A44; -fx-border-width: 1;");
        HBox.setMargin(crewBadge, new Insets(8, 8, 8, 8));
        tabBar.getChildren().add(crewBadge);
        top.getChildren().add(tabBar);
        root.setTop(top);
        root.setCenter(buildCenter());
        UIFactory.fadeIn(root, 300);
        return rootStack;
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
        int lv = merc.getLoyaltyLevel();
        boolean maxLv = lv >= 10;

        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color:" + (isActive ? "#0A180A" : "#150E08") + ";" +
                      "-fx-border-color:" + (isActive ? UIFactory.GREEN : "#3A2810") + ";" +
                      "-fx-border-width:1 1 1 3;");

        // Header: nama + level badge
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(merc.getMercenaryType().displayName.toUpperCase());
        nameLabel.setStyle("-fx-text-fill:" + (isActive ? UIFactory.GREEN : UIFactory.TEXT) +
            "; -fx-font-family:'Courier New'; -fx-font-size:13px; -fx-font-weight:bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        String lvColor = lv >= 8 ? "#FFD700" : lv >= 5 ? "#FFB830" : "#C8860A";
        Label lvBadge = new Label("LV." + lv);
        lvBadge.setStyle("-fx-text-fill:" + lvColor + "; -fx-font-family:'Courier New';" +
            "-fx-font-size:11px; -fx-font-weight:bold; -fx-background-color:" + lvColor + "22;" +
            "-fx-padding:2 6; -fx-background-radius:3;");

        Label roleLabel = new Label("[" + merc.getRole().name() + "]");
        roleLabel.setStyle("-fx-text-fill:#6A5840; -fx-font-family:'Courier New'; -fx-font-size:10px;");
        Label loyLbl = new Label("\u2665 " + merc.getLoyaltyTitle());
        loyLbl.setStyle("-fx-text-fill:#FF6B6B; -fx-font-family:'Courier New'; -fx-font-size:10px;");
        header.getChildren().addAll(nameLabel, lvBadge, roleLabel, loyLbl);

        Label subtitle = new Label(merc.getMercenaryType().subtitle);
        subtitle.setStyle("-fx-text-fill:#A09070; -fx-font-family:'Courier New'; -fx-font-size:11px;");

        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
            miniStat("HP",  String.valueOf((int)merc.getStats().get(StatType.MAX_HP))),
            miniStat("SPD", String.valueOf((int)merc.getStats().get(StatType.SPEED))),
            miniStat("ATK", String.valueOf((int)Math.max(
                merc.getStats().get(StatType.PHYSICAL_ATK),
                Math.max(merc.getStats().get(StatType.CYBER_ATK),
                         merc.getStats().get(StatType.ENERGY_ATK)))))
        );

        VBox vitals = UIFactory.compactVitalBars(
            merc.getCurrentHp(), merc.getStats().get(StatType.MAX_HP),
            merc.getCurrentShield(), merc.getStats().get(StatType.MAX_SHIELD),
            merc.getCurrentMp(), merc.getStats().get(StatType.MAX_MP));

        // Upgrade button
        HBox upgradeRow = new HBox(8);
        upgradeRow.setAlignment(Pos.CENTER_LEFT);
        if (!maxLv) {
            int cost = merc.getUpgradeCost();
            boolean canAfford = engine.getPlayer().getGold() >= cost;
            Button upBtn = new Button("\u2b06 UPGRADE LV." + (lv+1) + "  \u2699" + cost);
            upBtn.setStyle(
                "-fx-background-color:" + (canAfford ? "#C8860A22" : "#1A1008") + ";" +
                "-fx-border-color:" + (canAfford ? "#C8860A" : "#3A2810") + ";" +
                "-fx-border-width:1; -fx-text-fill:" + (canAfford ? "#FFB830" : "#5A3A10") + ";" +
                "-fx-font-family:'Courier New'; -fx-font-size:10px; -fx-padding:4 10;" +
                "-fx-cursor:" + (canAfford ? "hand" : "default") + ";");
            upBtn.setDisable(!canAfford);
            upBtn.setOnAction(e -> {
                if (merc.upgradeLevel(engine.getPlayer())) {
                    router.addSystemChat("\u2b06 " + merc.getMercenaryType().displayName +
                        " naik ke LV." + merc.getLoyaltyLevel() + "!");
                    engine.autoSave();
                    router.showMercenary();
                }
            });
            Label preview = new Label("+10% HP  +12% ATK  +8% DEF");
            preview.setStyle("-fx-text-fill:#3A2810; -fx-font-family:'Courier New'; -fx-font-size:9px;");
            upgradeRow.getChildren().addAll(upBtn, preview);
        } else {
            Label maxLbl = new Label("\u2726 LEVEL MAKSIMUM");
            maxLbl.setStyle("-fx-text-fill:#FFD700; -fx-font-family:'Courier New';" +
                "-fx-font-size:11px; -fx-font-weight:bold;");
            upgradeRow.getChildren().add(maxLbl);
        }

        Button toggleBtn = isActive ? UIFactory.btnDanger("KELUARKAN DARI REGU")
                                    : UIFactory.btnPrimary("TAMBAH KE REGU");
        toggleBtn.setMaxWidth(Double.MAX_VALUE);
        toggleBtn.setOnAction(e -> {
            if (isActive) engine.removeFromActiveParty(merc.getMercenaryType());
            else if (!engine.addToActiveParty(merc.getMercenaryType()))
                router.addSystemChat("Regu penuh! Keluarkan satu guildmate dulu.");
            router.showMercenary();
        });
        card.getChildren().addAll(header, subtitle, statsRow, vitals, upgradeRow, toggleBtn);

        // ── Artifact slot guildmate ─────────────────────────
        VBox artSlot = buildMercArtifactSlot(merc, engine, router);
        card.getChildren().add(artSlot);

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
        topBar.setPadding(new Insets(6, 12, 6, 12));
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
        scrollContent.setPadding(new Insets(8, 12, 8, 12));

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
        choicesBox.setPadding(new Insets(6, 12, 8, 12));
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
        header.setPadding(new Insets(8, 12, 8, 12));
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

        // Tampilkan loot dari bag (item terakhir ditambahkan)
        VBox lootDetail = new VBox(2);
        lootDetail.setPadding(new Insets(2, 0, 4, 12));
        int lootCount = result.getLootItemIds().size();
        if (lootCount > 0) {
            var allItems = engine.getInventory().getAllItems();
            int start = Math.max(0, allItems.size() - lootCount);
            allItems.subList(start, allItems.size()).forEach(item -> {
                String rc = UIFactory.rarityColor(item.getRarity());
                Label il = new Label("  ✦ " + item.getFullName());
                il.setStyle("-fx-text-fill: " + rc + "; -fx-font-family: 'Courier New';" +
                            "-fx-font-size: 10px;");
                lootDetail.getChildren().add(il);
            });
        }

        int levels = result.getLevelsGained();
        rewards.getChildren().addAll(rwdTitle, expRow, goldRow, lootRow, lootDetail);

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
        nav.setPadding(new Insets(6, 12, 8, 12));
        nav.setStyle("-fx-background-color: #0F0A06;" +
                     "-fx-border-color: #3A2810; -fx-border-width: 1 0 0 0;");

        Button cont = new Button("▶  LANJUT JELAJAH DUNGEON");
        cont.setMaxWidth(Double.MAX_VALUE);
        cont.setStyle(
            "-fx-background-color: #C8860A22; -fx-border-color: #FFB830; -fx-border-width: 1;" +
            "-fx-text-fill: #FFB830; -fx-font-family: 'Courier New'; -fx-font-size: 13px;" +
            "-fx-font-weight: bold; -fx-padding: 8 14; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 8, 0.3, 0, 0);"
        );
        cont.setOnAction(e -> {
            // Trigger boss POST cutscene jika applicable (setelah menang boss)
            int floor = engine.getFloorNumber();
            router.triggerBossPostCutscene(floor, () -> router.showDungeonMap());
        });

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
        header.setPadding(new Insets(8, 12, 8, 12));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #0F0A06;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Label fallen = new Label("◆  GUGUR  ◆");
        fallen.setStyle(
            "-fx-text-fill: #CC3300;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;"
        );

        Label sub = new Label("perjalananmu terhenti... namun belum berakhir");
        sub.setStyle("-fx-text-fill: #4A3820; -fx-font-family: 'Courier New';" +
                     "-fx-font-size: 12px; -fx-letter-spacing: 2;");

        header.getChildren().addAll(fallen, sub);
        root.setTop(header);

        // ── CENTER: stats ──────────────────────────────────
        VBox content = new VBox(16);
        content.setPadding(new Insets(8, 12, 8, 12));
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
            "-fx-padding: 8 14; -fx-cursor: hand;"
        );
        retry.setOnAction(e -> {
            // COBA LAGI: restore HP 50% (penalty mati) dan kembali ke hub
            // Item dan progress TIDAK hilang — hanya HP yang dipotong
            engine.returnToHub();
            arclightcity.entity.player.Player pl2 = engine.getPlayer();
            double maxHp2 = pl2.getStats().get(arclightcity.entity.stats.StatType.MAX_HP);
            double maxMp2 = pl2.getStats().get(arclightcity.entity.stats.StatType.MAX_MP);
            pl2.setHpDirect(maxHp2 * 0.50); // setHpDirect now resets alive=true
            pl2.restoreMp(maxMp2 * 0.50);
            // Revive semua guildmate juga di 30% HP
            for (var m : engine.getOwnedMercs()) {
                double mhp = m.getStats().get(arclightcity.entity.stats.StatType.MAX_HP);
                double mshd = m.getStats().get(arclightcity.entity.stats.StatType.MAX_SHIELD);
                double mmp = m.getStats().get(arclightcity.entity.stats.StatType.MAX_MP);
                m.restoreVitals(mhp * 0.30, mshd * 0.25, mmp * 0.30);
            }
            router.addSystemChat("⟳ Coba lagi — 50% HP dipulihkan, progress tetap.");
            router.showHub();
        });

        // KEMBALI KE HUB — bukan main menu!
        Button backHub = UIFactory.btnPrimary("◈  KEMBALI KE MARKAS");
        backHub.setMaxWidth(Double.MAX_VALUE);
        backHub.setOnAction(e -> {
            engine.returnToHub();
            // Restore HP minimal 25% agar tidak mati di hub
            arclightcity.entity.player.Player pl = engine.getPlayer();
            double maxHp = pl.getStats().get(arclightcity.entity.stats.StatType.MAX_HP);
            double maxMp = pl.getStats().get(arclightcity.entity.stats.StatType.MAX_MP);
            if (pl.getCurrentHp() <= 0) {
                pl.setHpDirect(maxHp * 0.25);
                pl.restoreMp(maxMp * 0.30);
                router.addSystemChat("⟳ Bangkit dari kekalahan — 25% HP dipulihkan.");
            }
            // Revive guildmate juga
            for (var m : engine.getOwnedMercs()) {
                if (!m.isAlive()) {
                    double mhp = m.getStats().get(arclightcity.entity.stats.StatType.MAX_HP);
                    double mshd = m.getStats().get(arclightcity.entity.stats.StatType.MAX_SHIELD);
                    double mmp = m.getStats().get(arclightcity.entity.stats.StatType.MAX_MP);
                    m.restoreVitals(mhp * 0.25, mshd * 0.20, mmp * 0.25);
                }
            }
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
        javafx.animation.FadeTransition flicker =
            new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(150), fallen);
        flicker.setFromValue(1.0); flicker.setToValue(0.7);
        flicker.setAutoReverse(true); flicker.setCycleCount(4);
        flicker.play();

        UIFactory.fadeIn(root, 600);
        return root;
    }
}

    /** Tampilkan dialog pilih slot saat slot sudah penuh */
    public static void showSlotPickerStatic(String title, String[] options,
                                 java.util.function.IntConsumer onChoice) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initStyle(javafx.stage.StageStyle.UNDECORATED);
        dialog.setAlwaysOnTop(true);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#0D0A06; -fx-border-color:#C8860A; -fx-border-width:1 1 1 3;");
        root.setPrefWidth(300);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill:#FFB830; -fx-font-family:'Courier New';" +
            "-fx-font-size:11px; -fx-font-weight:bold; -fx-padding:10 14 6 14;");
        root.getChildren().add(titleLbl);

        for (int i = 0; i < options.length; i++) {
            final int idx = i;
            HBox row = new HBox();
            row.setPadding(new Insets(8, 14, 8, 14));
            row.setStyle("-fx-background-color:transparent; -fx-border-color:#3A2810;" +
                        "-fx-border-width:1 0 0 0; -fx-cursor:hand;");
            Label lbl = new Label((i+1) + ". " + options[i]);
            lbl.setStyle("-fx-text-fill:#EDE0C8; -fx-font-family:'Courier New'; -fx-font-size:11px;");
            row.getChildren().add(lbl);
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:#1A1208;" +
                "-fx-border-color:#3A2810;-fx-border-width:1 0 0 0;-fx-cursor:hand;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color:transparent;" +
                "-fx-border-color:#3A2810;-fx-border-width:1 0 0 0;-fx-cursor:hand;"));
            row.setOnMouseClicked(e -> { dialog.close(); onChoice.accept(idx); });
            root.getChildren().add(row);
        }

        Button cancel = new Button("✗  BATAL");
        cancel.setStyle("-fx-background-color:transparent; -fx-border-color:#3A2810;" +
            "-fx-border-width:1 0 0 0; -fx-text-fill:#5A3A10;" +
            "-fx-font-family:'Courier New'; -fx-font-size:10px;" +
            "-fx-padding:7 14; -fx-cursor:hand;");
        cancel.setMaxWidth(Double.MAX_VALUE);
        cancel.setOnAction(e -> dialog.close());
        root.getChildren().add(cancel);

        dialog.setScene(new javafx.scene.Scene(root));
        dialog.show();
    }


    /** Dialog expand bag — pilih +1/+5/+10/+20 slot, bayar gold */
    public static void showBagExpandMenuStatic(arclightcity.item.Inventory inv,
                arclightcity.engine.GameEngine engine,
                arclightcity.ui.controller.SceneRouter router) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initStyle(javafx.stage.StageStyle.UNDECORATED);
        dialog.setAlwaysOnTop(true);

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color:#0D0A06; -fx-border-color:#C8860A; -fx-border-width:1 1 1 3;");
        root.setPrefWidth(280);

        Label title = new Label("⬡  PERLUAS TAS");
        title.setStyle("-fx-background-color:#C8860A;-fx-text-fill:#0A0603;" +
            "-fx-font-family:'Courier New';-fx-font-size:12px;-fx-font-weight:bold;" +
            "-fx-padding:8 14; -fx-max-width:280;");

        Label info = new Label("Kapasitas: " + inv.getBagSize() + "/" + inv.getMaxBagSize() + " (maks 100)");
        info.setStyle("-fx-text-fill:#6A5840;-fx-font-family:'Courier New';" +
            "-fx-font-size:10px;-fx-padding:6 14 4 14;");

        root.getChildren().addAll(title, info);

        // Opsi expand
        int[][] options = {{1,200},{5,800},{10,1400},{20,2400}};
        for (int[] opt : options) {
            int slots = opt[0]; int cost = opt[1];
            boolean affordable = engine.getPlayer().getGold() >= cost;
            boolean possible   = inv.getMaxBagSize() + slots <= 100;

            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8,14,8,14));
            row.setStyle("-fx-border-color:#3A2810;-fx-border-width:1 0 0 0;" +
                "-fx-cursor:" + (affordable&&possible?"hand":"default") + ";" +
                "-fx-opacity:" + (affordable&&possible?"1.0":"0.45") + ";");

            Label optLbl = new Label("+" + slots + " Slot");
            optLbl.setStyle("-fx-text-fill:#EDE0C8;-fx-font-family:'Courier New';-fx-font-size:12px;");
            HBox.setHgrow(optLbl, Priority.ALWAYS);
            Label costLbl = new Label("⚙ " + cost + " Gold");
            costLbl.setStyle("-fx-text-fill:#C8860A;-fx-font-family:'Courier New';-fx-font-size:11px;");
            row.getChildren().addAll(optLbl, costLbl);

            if (affordable && possible) {
                final int fs = slots; final int fc = cost;
                row.setOnMouseClicked(e -> {
                    engine.getPlayer().spendGold(fc);
                    inv.expandBag(fs);
                    router.addSystemChat("⊕ Tas diperluas +" + fs + " slot. Gold -" + fc);
                    dialog.close();
                    router.showInventory();
                });
                row.setOnMouseEntered(e -> row.setStyle(row.getStyle() + "-fx-background-color:#1A1208;"));
                row.setOnMouseExited(e  -> row.setStyle(row.getStyle().replace("-fx-background-color:#1A1208;","")));
            }
            root.getChildren().add(row);
        }

        Button cancel = new Button("✗  TUTUP");
        cancel.setStyle("-fx-background-color:transparent;-fx-border-color:#3A2810;" +
            "-fx-border-width:1 0 0 0;-fx-text-fill:#5A3A10;" +
            "-fx-font-family:'Courier New';-fx-font-size:10px;" +
            "-fx-padding:7 14;-fx-cursor:hand;");
        cancel.setMaxWidth(Double.MAX_VALUE);
        cancel.setOnAction(e -> dialog.close());
        root.getChildren().add(cancel);

        dialog.setScene(new javafx.scene.Scene(root));
        dialog.show();
    }


    /** Slot artefak untuk guildmate — ditampilkan di detail guildmate */
    private static VBox buildMercArtifactSlot(
            arclightcity.entity.mercenary.Mercenary merc,
            arclightcity.engine.GameEngine engine,
            arclightcity.ui.controller.SceneRouter router) {

        VBox row = new VBox(4);
        row.setStyle("-fx-background-color:#080312; -fx-border-color:#3A1A5A;" +
                     "-fx-border-width:1; -fx-padding:8 12; -fx-margin:0 0 8 0;");
        row.setMaxWidth(Double.MAX_VALUE);

        Label header = new Label("⬡  SLOT ARTEFAK GUILDMATE");
        header.setStyle("-fx-text-fill:rgba(170,102,255,0.55); -fx-font-family:'Courier New';" +
                        "-fx-font-size:10px;");

        arclightcity.item.Artifact art = merc.getEquippedArtifact();
        if (art == null) {
            // Tampilkan semua artifact player yang belum dipakai → bisa dipilih
            Label hint = new Label("  Klik artefak di bawah untuk dipasang:");
            hint.setStyle("-fx-text-fill:rgba(255,255,255,0.30); -fx-font-family:'Courier New';" +
                          "-fx-font-size:9px;");

            arclightcity.item.Inventory inv = engine.getInventory();
            VBox available = new VBox(4);
            // Tampilkan artifact dari BAG yang belum diequip merc lain
            if (inv != null) {
                for (arclightcity.item.Item item : inv.getAllBagItems()) {
                    if (!(item instanceof arclightcity.item.Artifact a)) continue;
                    // Filter: hanya role yang sesuai atau UNIVERSAL
                    arclightcity.item.ArtifactRole role = a.getArtifactType().role;
                    boolean roleOk = (role == arclightcity.item.ArtifactRole.UNIVERSAL)
                        || role.name().equalsIgnoreCase(merc.getRole().name());
                    if (!roleOk) continue;
                    Button use = artifactPickBtn(a, merc, engine, router);
                    available.getChildren().add(use);
                }
            }
            if (available.getChildren().isEmpty()) {
                Label noArt = new Label("  (Belum punya artefak — buka Altar Artefak di Hub)");
                noArt.setStyle("-fx-text-fill:rgba(255,255,255,0.20); -fx-font-family:'Courier New';" +
                               "-fx-font-size:9px;");
                available.getChildren().add(noArt);
            }
            row.getChildren().addAll(header, hint, available);
        } else {
            Label name = new Label(art.getArtifactType().displayName);
            name.setStyle("-fx-text-fill:" + art.getBorderColor() + ";" +
                          "-fx-font-family:'Courier New'; -fx-font-size:12px; -fx-font-weight:bold;");
            if (art.hasGlowEffect())
                name.setEffect(new javafx.scene.effect.Glow(0.35));

            String cdStr = art.isReady() ? "SIAP" : "CD: " + art.getCooldown() + " giliran";
            Label cdLbl = new Label("[" + art.getRarity().displayName + "] · " + cdStr);
            cdLbl.setStyle("-fx-text-fill:rgba(255,255,255,0.40); -fx-font-family:'Courier New';" +
                           "-fx-font-size:9px;");

            Button remove = new Button("LEPAS");
            remove.setStyle("-fx-background-color:transparent; -fx-border-color:#883333;" +
                            "-fx-border-width:1; -fx-text-fill:#883333; -fx-cursor:hand;" +
                            "-fx-font-family:'Courier New'; -fx-font-size:9px; -fx-padding:3 10;");
            remove.setOnAction(e -> {
                merc.unequipArtifact();
                router.showMercenary();
            });
            row.getChildren().addAll(header, name, cdLbl, remove);
        }
        return row;
    }

    private static javafx.scene.control.Button artifactPickBtn(
            arclightcity.item.Artifact art,
            arclightcity.entity.mercenary.Mercenary merc,
            arclightcity.engine.GameEngine engine,
            arclightcity.ui.controller.SceneRouter router) {

        javafx.scene.control.Button btn = new javafx.scene.control.Button(
            art.getArtifactType().displayName + " [" + art.getRarity().displayName + "]");
        btn.setStyle("-fx-background-color:transparent; -fx-border-color:" + art.getBorderColor() + ";" +
                     "-fx-border-width:1; -fx-text-fill:" + art.getBorderColor() + ";" +
                     "-fx-font-family:'Courier New'; -fx-font-size:10px; -fx-cursor:hand;" +
                     "-fx-padding:4 12; -fx-max-width:300;");
        btn.setOnAction(e -> {
            merc.equipArtifact(art);
            router.showMercenary();
        });
        return btn;
    }


    /** Popup info artefak dari bag — tampilkan icon + buff + tombol equip ke slot player */
    private static void showArtifactBagPopup(arclightcity.item.Artifact art,
                                              arclightcity.item.Inventory inv,
                                              SceneRouter router) {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.setTitle(art.getArtifactType().displayName);

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color:#0C0514; -fx-border-color:" + art.getBorderColor() +
            "; -fx-border-width:2; -fx-padding:20;");
        root.setPrefWidth(320);

        // Icon besar
        javafx.scene.image.Image icon = arclightcity.ui.util.AssetManager.artifactIcon(art.getArtifactType());
        if (icon != null) {
            javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(icon);
            iv.setFitWidth(80); iv.setFitHeight(80); iv.setPreserveRatio(true);
            javafx.scene.layout.StackPane icBox = new javafx.scene.layout.StackPane(iv);
            icBox.setAlignment(javafx.geometry.Pos.CENTER);
            if (art.hasGlowEffect())
                icBox.setEffect(new javafx.scene.effect.DropShadow(20,
                    javafx.scene.paint.Color.web(art.getBorderColor()+"99")));
            root.getChildren().add(icBox);
        }

        Label name = new Label(art.getArtifactType().displayName);
        name.setStyle("-fx-text-fill:" + art.getBorderColor() + ";-fx-font-family:'Courier New';" +
            "-fx-font-size:14px;-fx-font-weight:bold;");
        Label rar = new Label("[" + art.getRarity().displayName + "] · Role: " + art.getArtifactType().role.name());
        rar.setStyle("-fx-text-fill:rgba(255,255,255,0.40);-fx-font-family:'Courier New';-fx-font-size:10px;");
        Label desc = new Label(art.getDisplaySummary());
        desc.setWrapText(true); desc.setMaxWidth(280);
        desc.setStyle("-fx-text-fill:rgba(240,230,255,0.75);-fx-font-family:'Courier New';-fx-font-size:11px;");
        Label cd = new Label("CD: " + art.getScaledCooldown() + " giliran · " +
                             (art.isReady() ? "SIAP" : "CD " + art.getCooldown()));
        cd.setStyle("-fx-text-fill:rgba(255,255,255,0.30);-fx-font-family:'Courier New';-fx-font-size:9px;");

        root.getChildren().addAll(name, rar, desc, cd);

        // Tombol equip ke slot player
        javafx.scene.layout.HBox btnRow = new javafx.scene.layout.HBox(8);
        btnRow.setAlignment(javafx.geometry.Pos.CENTER);

        Button slot1 = new Button("→ SLOT 1");
        slot1.setStyle("-fx-background-color:transparent;-fx-border-color:#7722CC;-fx-border-width:1;" +
            "-fx-text-fill:#AA55EE;-fx-font-family:'Courier New';-fx-font-size:10px;" +
            "-fx-padding:6 12;-fx-cursor:hand;");
        slot1.setOnAction(e -> {
            inv.equipArtifactToSlot(1, art);
            popup.close();
            router.showInventory();
        });

        Button slot2 = new Button("→ SLOT 2");
        slot2.setStyle("-fx-background-color:transparent;-fx-border-color:#7722CC;-fx-border-width:1;" +
            "-fx-text-fill:#AA55EE;-fx-font-family:'Courier New';-fx-font-size:10px;" +
            "-fx-padding:6 12;-fx-cursor:hand;");
        slot2.setOnAction(e -> {
            inv.equipArtifactToSlot(2, art);
            popup.close();
            router.showInventory();
        });

        Button close = new Button("TUTUP");
        close.setStyle("-fx-background-color:transparent;-fx-border-color:#555555;-fx-border-width:1;" +
            "-fx-text-fill:#888888;-fx-font-family:'Courier New';-fx-font-size:10px;" +
            "-fx-padding:6 12;-fx-cursor:hand;");
        close.setOnAction(e -> popup.close());

        btnRow.getChildren().addAll(slot1, slot2, close);
        root.getChildren().add(btnRow);

        popup.setScene(new javafx.scene.Scene(root));
        popup.initOwner(router.getStage());
        popup.sizeToScene();
        popup.show();
    }


} // end ViewsBundle
