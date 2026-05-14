package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.entity.stats.StatType;
import arclightcity.item.*;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.util.Duration;
import java.util.*;

/**
 * CityView — Menu Kota dengan 4 area:
 * 1. Toko Senjata Pak Empu   — beli pedang
 * 2. Kedai Jamu Mbah Jamu    — beli consumable
 * 3. Bengkel Empu             — upgrade/kalibrasi premium
 * 4. Penadah Barang           — jual item tidak terpakai
 */
public class CityView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private String activeArea = "MENU";

    // Shop inventories (deterministik per sesi)
    private final List<Weapon>     weaponShop    = new ArrayList<>();
    private final List<Consumable> consumableShop= new ArrayList<>();

    public CityView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
        generateShops();
    }

    private void generateShops() {
        Random rng = new Random(engine.getPlayer().getLevel() * 31L);
        // 5 senjata acak berdasarkan level
        for (int i = 0; i < 5; i++) {
            weaponShop.add(LootManager.generateCityWeapon(
                engine.getPlayer().getLevel(), rng));
        }
        // Consumables fixed
        consumableShop.add(new Consumable("Jamu Kunyit",
            "Memulihkan 80 HP.", Item.Rarity.COMMON,
            Consumable.ConsumableType.HEALTH_PACK, 80));
        consumableShop.add(new Consumable("Jamu Kunyit Murni",
            "Memulihkan 200 HP.", Item.Rarity.UNCOMMON,
            Consumable.ConsumableType.HEALTH_PACK, 200));
        consumableShop.add(new Consumable("Tirta Mahkota",
            "Memulihkan 60 MP.", Item.Rarity.COMMON,
            Consumable.ConsumableType.MP_PACK, 60));
        consumableShop.add(new Consumable("Tirta Mahkota Murni",
            "Memulihkan 150 MP.", Item.Rarity.UNCOMMON,
            Consumable.ConsumableType.MP_PACK, 150));
        consumableShop.add(new Consumable("Daun Suruh Sakti",
            "Cleanse semua debuff.", Item.Rarity.UNCOMMON,
            Consumable.ConsumableType.ANTIDOTE, 0));
        consumableShop.add(new Consumable("Sesajen Kekuatan",
            "Buff ATK +30% selama 3 giliran.", Item.Rarity.RARE,
            Consumable.ConsumableType.BUFF_ITEM, 1.3));
    }

    public Parent build() { return buildArea(activeArea); }

    public void setArea(String area) { this.activeArea = area; }

    private Parent buildArea(String area) {
        BorderPane root = UIFactory.screenRootBorder();

        // Background kota berdasarkan area
        javafx.scene.image.Image cityBg = switch (activeArea) {
            case "SENJATA"  -> arclightcity.ui.util.AssetManager.bgCitySenjata();
            case "JAMU"     -> arclightcity.ui.util.AssetManager.bgCityJamu();
            case "BENGKEL"  -> arclightcity.ui.util.AssetManager.bgCityBengkel();
            case "PENADAH"  -> arclightcity.ui.util.AssetManager.bgCityPenadah();
            default         -> arclightcity.ui.util.AssetManager.bgHub();
        };
        if (cityBg != null) {
            javafx.scene.image.ImageView bgView =
                arclightcity.ui.util.AssetManager.makeIVFill(cityBg,
                    arclightcity.ui.ArclightApp.GAME_WIDTH,
                    arclightcity.ui.ArclightApp.SCREEN_HEIGHT);
            bgView.setOpacity(0.60);
            bgView.setMouseTransparent(true);
            root.getChildren().add(0, bgView);
        }
        root.setTop(buildHeader(area));

        Parent content = switch (area) {
            case "SENJATA"  -> buildWeaponShop();
            case "JAMU"     -> buildConsumableShop();
            case "BENGKEL"  -> buildWorkshop();
            case "PENADAH"  -> buildPawnshop();
            default         -> buildCityMenu();
        };

        if (!(content instanceof BorderPane)) {
            ScrollPane scroll = new ScrollPane(content);
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                            "-fx-border-color: transparent;");
            root.setCenter(scroll);
        } else {
            root.setCenter(content);
        }

        UIFactory.fadeIn(root, 350);
        return root;
    }

    // ── Header ────────────────────────────────────────────────

    private HBox buildHeader(String area) {
        HBox hdr = new HBox(10);
        hdr.setPadding(new Insets(10, 16, 10, 16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-background-color: #0F0A06;" +
                     "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        String backLabel = area.equals("MENU") ? "← MARKAS" : "← KOTA";
        Button back = new Button(backLabel);
        back.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                      "-fx-border-width: 1; -fx-text-fill: #5A3A10;" +
                      "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                      "-fx-padding: 3 8; -fx-cursor: hand;");
        back.setOnAction(e -> {
            if (area.equals("MENU")) router.showHub();
            else router.showCity();
        });
        back.setOnMouseEntered(ev -> back.setStyle(
            "-fx-background-color: transparent; -fx-border-color: #C8860A;" +
            "-fx-border-width: 1; -fx-text-fill: #FFB830;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-padding: 3 8; -fx-cursor: hand;"));
        back.setOnMouseExited(ev -> back.setStyle(
            "-fx-background-color: transparent; -fx-border-color: #3A2810;" +
            "-fx-border-width: 1; -fx-text-fill: #5A3A10;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-padding: 3 8; -fx-cursor: hand;"));

        String titleText = switch (area) {
            case "SENJATA"  -> "🏪  TOKO SENJATA PAK EMPU";
            case "JAMU"     -> "⚗️  KEDAI JAMU MBAH JAMU";
            case "BENGKEL"  -> "🔨  BENGKEL EMPU";
            case "PENADAH"  -> "💰  PENADAH BARANG";
            default         -> "🏙️  KOTA NUSANTARA";
        };

        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 14px; -fx-font-weight: bold;" +
                       "-fx-effect: dropshadow(gaussian, #C8860A, 6, 0.3, 0, 0);");
        HBox.setHgrow(title, Priority.ALWAYS);

        Label gold = new Label("⚙ " + UIFactory.formatNumber(engine.getPlayer().getGold()));
        gold.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 12px; -fx-font-weight: bold;");

        hdr.getChildren().addAll(back, title, gold);
        return hdr;
    }

    // ── City Menu (4 area buttons) ────────────────────────────

    private VBox buildCityMenu() {
        VBox menu = new VBox(0);
        menu.setPadding(new Insets(16));
        menu.setSpacing(12);

        Label sub = new Label("Selamat datang di kota. Pilih tujuanmu:");
        sub.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        menu.getChildren().add(sub);

        String[][] areas = {
            {"SENJATA",  "🏪", "TOKO SENJATA PAK EMPU",
             "Beli pedang dan senjata berbagai kelas", "#C8860A"},
            {"JAMU",     "⚗️", "KEDAI JAMU MBAH JAMU",
             "Beli jamu, tirta, dan item konsumable lainnya", "#2D7A45"},
            {"BENGKEL",  "🔨", "BENGKEL EMPU",
             "Upgrade (+9/+10) dan kalibrasi premium senjatamu", "#7755BB"},
            {"PENADAH",  "💰", "PENADAH BARANG",
             "Jual item yang tidak terpakai dengan harga layak", "#CC3300"},
        };

        for (String[] area : areas) {
            Button btn = buildAreaButton(area[0], area[1], area[2], area[3], area[4]);
            menu.getChildren().add(btn);
        }

        return menu;
    }

    private Button buildAreaButton(String id, String icon, String name,
                                    String desc, String color) {
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setCursor(javafx.scene.Cursor.HAND);

        HBox content = new HBox(14);
        content.setAlignment(Pos.CENTER_LEFT);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px;");
        iconLbl.setMinWidth(36);

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 13px; -fx-font-weight: bold;");
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-text-fill: #4A3820; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        info.getChildren().addAll(nameLbl, descLbl);

        Label arrow = new Label("▶");
        arrow.setStyle("-fx-text-fill: " + color + "66; -fx-font-size: 14px;");

        content.getChildren().addAll(iconLbl, info, arrow);
        btn.setGraphic(content);
        btn.setPadding(new Insets(14, 16, 14, 16));
        btn.setStyle(
            "-fx-background-color: #1A1008;" +
            "-fx-border-color: " + color + "44;" +
            "-fx-border-width: 1 1 1 3;" +
            "-fx-cursor: hand; -fx-alignment: CENTER_LEFT;"
        );
        btn.setOnMouseEntered(ev -> {
            nameLbl.setStyle(nameLbl.getStyle().replace(color, "#FFB830"));
            btn.setStyle(btn.getStyle().replace(color + "44", color)
                .replace("#1A1008", "#241510"));
        });
        btn.setOnMouseExited(ev -> {
            nameLbl.setStyle(nameLbl.getStyle().replace("#FFB830", color));
            btn.setStyle(btn.getStyle().replace(color + " -fx-border", color + "44 -fx-border")
                .replace("#241510", "#1A1008"));
        });
        btn.setOnAction(e -> router.showCityArea(id));
        return btn;
    }

    // ── Weapon Shop ───────────────────────────────────────────

    private VBox buildWeaponShop() {
        VBox list = new VBox(8);
        list.setPadding(new Insets(6, 12, 6, 12));

        Label goldInfo = new Label("Emasmu: ⚙ " + UIFactory.formatNumber(engine.getPlayer().getGold()));
        goldInfo.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 0 0 8 0;");
        list.getChildren().add(goldInfo);

        for (Weapon w : weaponShop) {
            list.getChildren().add(buildShopItem(w));
        }
        return list;
    }

    // ── Consumable Shop ───────────────────────────────────────

    private VBox buildConsumableShop() {
        VBox list = new VBox(8);
        list.setPadding(new Insets(6, 12, 6, 12));

        Label info = new Label("Emasmu: ⚙ " + UIFactory.formatNumber(engine.getPlayer().getGold()));
        info.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 0 0 8 0;");
        list.getChildren().add(info);

        for (Consumable c : consumableShop) {
            list.getChildren().add(buildShopItem(c));
        }
        return list;
    }

    private VBox buildShopItem(Item item) {
        int seed = Math.abs(item.getName().hashCode());
        int price = switch (item.getRarity()) {
            case COMMON    -> 40   + (seed % 20);
            case UNCOMMON  -> 120  + (seed % 50);
            case RARE      -> 350  + (seed % 100);
            case EPIC      -> 800  + (seed % 200);
            case LEGENDARY -> 2000 + (seed % 500);
            case MYTHIC    -> 0; // tidak dijual
        };

        String rc = UIFactory.rarityColor(item.getRarity());
        VBox card = new VBox(4);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setStyle("-fx-background-color: #1A1008; -fx-border-color: " + rc + "33;" +
                      "-fx-border-width: 1 1 1 3;");

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(item.getFullName());
        name.setStyle("-fx-text-fill: " + rc + "; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 12px; -fx-font-weight: bold;");
        Label desc = new Label(item.getDescription());
        desc.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        info.getChildren().addAll(name, desc);

        Label priceLabel = new Label("⚙ " + price);
        priceLabel.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                            "-fx-font-size: 12px; -fx-font-weight: bold;");

        boolean canAfford = engine.getPlayer().getGold() >= price;
        Button buyBtn = new Button("BELI");
        buyBtn.setDisable(!canAfford);
        buyBtn.setStyle(
            "-fx-background-color: " + (canAfford ? "#C8860A22" : "transparent") + ";" +
            "-fx-border-color: " + (canAfford ? "#C8860A" : "#3A2810") + "; -fx-border-width: 1;" +
            "-fx-text-fill: " + (canAfford ? "#FFB830" : "#3A2810") + ";" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
            "-fx-padding: 5 12; -fx-cursor: " + (canAfford ? "hand" : "default") + ";");
        final int finalPrice = price;
        buyBtn.setOnAction(e -> {
            if (engine.getPlayer().spendGold(finalPrice)) {
                engine.getInventory().addItem(item);
                router.addSystemChat("✓ " + item.getName() + " dibeli!");
                engine.autoSave(); // save otomatis setelah beli
                router.showCityArea("JAMU".equals(activeArea) ? "JAMU" : "SENJATA");
            }
        });

        row.getChildren().addAll(info, priceLabel, buyBtn);
        card.getChildren().add(row);
        return card;
    }

    // ── Workshop (Bengkel Empu) ───────────────────────────────

    private VBox buildWorkshop() {
        VBox list = new VBox(0);

        // Info material
        long calibrators = countMaterial(Material.MaterialType.CALIBRATOR);
        long ultraCores  = countMaterial(Material.MaterialType.ULTRA_ENHANCE_CORE);

        VBox infoPanel = new VBox(4);
        infoPanel.setPadding(new Insets(10, 16, 10, 16));
        infoPanel.setStyle("-fx-background-color: #150E08; -fx-border-color: #3A2810;" +
                           "-fx-border-width: 0 0 1 0;");

        Label matTitle = new Label("── MATERIAL DI TAS ──");
        matTitle.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 10px; -fx-letter-spacing: 2;");

        HBox mats = new HBox(20);
        mats.getChildren().addAll(
            matLabel("🔧 Kalibrator", calibrators, "#7755BB"),
            matLabel("💎 Kristal Ultra", ultraCores, "#FF8833")
        );

        // Shop kalibrator dan ultra core
        HBox buyMats = new HBox(8);
        Button buyCal = buildMatShopBtn("Beli Kalibrator", 300, Material.MaterialType.CALIBRATOR);
        Button buyUltra = buildMatShopBtn("Beli Kristal Ultra", 800, Material.MaterialType.ULTRA_ENHANCE_CORE);
        buyMats.getChildren().addAll(buyCal, buyUltra);

        infoPanel.getChildren().addAll(matTitle, mats, buyMats);
        list.getChildren().add(infoPanel);

        // Equipped items yang bisa di-upgrade
        list.getChildren().add(buildWorkshopSection());
        return list;
    }

    private HBox matLabel(String name, long count, String color) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        Label n = new Label(name);
        n.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        Label c = new Label("×" + count);
        c.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Courier New';" +
                   "-fx-font-size: 12px; -fx-font-weight: bold;");
        box.getChildren().addAll(n, c);
        return box;
    }

    private Button buildMatShopBtn(String label, int price, Material.MaterialType type) {
        boolean canAfford = engine.getPlayer().getGold() >= price;
        Button btn = new Button(label + " ⚙" + price);
        btn.setDisable(!canAfford);
        btn.setStyle(
            "-fx-background-color: transparent; -fx-border-color: #3A2810;" +
            "-fx-border-width: 1; -fx-text-fill: " + (canAfford ? "#A09070" : "#3A2810") + ";" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
            "-fx-padding: 4 10; -fx-cursor: " + (canAfford ? "hand" : "default") + ";");
        btn.setOnAction(e -> {
            if (engine.getPlayer().spendGold(price)) {
                engine.getInventory().addItem(new Material(
                    type == Material.MaterialType.CALIBRATOR
                        ? "Kalibrator" : "Kristal Ultra Enhance",
                    type == Material.MaterialType.CALIBRATOR
                        ? "Material kalibrasi premium." : "Material upgrade +9/+10. Rate gagal tinggi!",
                    type == Material.MaterialType.CALIBRATOR
                        ? Item.Rarity.UNCOMMON : Item.Rarity.EPIC,
                    type));
                router.addSystemChat("✓ Material dibeli!");
                router.showCityArea("BENGKEL");
            }
        });
        return btn;
    }

    private VBox buildWorkshopSection() {
        VBox sec = new VBox(8);
        sec.setPadding(new Insets(6, 12, 6, 12));

        Label title = new Label("── PILIH ITEM UNTUK DIPROSES ──");
        title.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 10px; -fx-letter-spacing: 2;");
        sec.getChildren().add(title);

        Inventory inv = engine.getInventory();
        List<Equipment> allEquip = new ArrayList<>();
        if (inv.getEquippedWeapon() != null)     allEquip.add(inv.getEquippedWeapon());
        if (inv.getEquippedArmor() != null)      allEquip.add(inv.getEquippedArmor());
        if (inv.getEquippedAccessory1() != null) allEquip.add(inv.getEquippedAccessory1());
        if (inv.getEquippedAccessory2() != null) allEquip.add(inv.getEquippedAccessory2());
        allEquip.addAll(inv.getEquipmentInBag());

        if (allEquip.isEmpty()) {
            Label none = new Label("Tidak ada item untuk diproses.");
            none.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            sec.getChildren().add(none);
        } else {
            for (Equipment eq : allEquip) {
                sec.getChildren().add(buildWorkshopCard(eq));
            }
        }
        return sec;
    }

    private VBox buildWorkshopCard(Equipment eq) {
        String rc = UIFactory.rarityColor(eq.getRarity());
        VBox card = new VBox(6);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setStyle("-fx-background-color: #1A1008; -fx-border-color: " + rc + "33;" +
                      "-fx-border-width: 1 1 1 3;");

        // Header
        HBox nameRow = new HBox(8);
        Label name = new Label(eq.getFullName());
        name.setStyle("-fx-text-fill: " + rc + "; -fx-font-family: \'Courier New\';" +
                      "-fx-font-size: 12px; -fx-font-weight: bold;");
        int curLvl = eq.getUpgradeLevel();
        Label lvl = new Label("+" + curLvl + (curLvl >= eq.getMaxUpgradeFromTier() ? " ✓MAX" : ""));
        lvl.setStyle("-fx-text-fill: #2D7A45; -fx-font-family: \'Courier New\'; -fx-font-size: 11px;");
        Label tier = new Label("Tier " + eq.getItemTier());
        tier.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: \'Courier New\'; -fx-font-size: 9px;");
        HBox.setHgrow(name, Priority.ALWAYS);
        nameRow.getChildren().addAll(name, lvl, tier);

        // ── UPGRADE section ─────────────────────────────────
        HBox upgradeRow = new HBox(8);
        int maxLvl = eq.getMaxUpgradeFromTier();
        if (curLvl < maxLvl) {
            int upgCost = 80 * (curLvl + 1) * eq.getItemTier();
            boolean afford = engine.getPlayer().getGold() >= upgCost;
            Button upg = new Button("⬆ UPGRADE +" + (curLvl+1) + "  ⚙" + upgCost);
            upg.setDisable(!afford || !eq.canUpgrade());
            styleWorkshopBtn(upg, afford ? "#2D7A45" : "#3A2810", afford);
            upg.setOnAction(e -> {
                if (engine.getPlayer().spendGold(upgCost)) {
                    eq.applyUpgrade();
                    router.addSystemChat("✓ " + eq.getName() + " ditingkatkan ke +" + eq.getUpgradeLevel() + "!");
                    router.showCityArea("BENGKEL");
                }
            });
            upgradeRow.getChildren().add(upg);
        } else {
            Label maxed = new Label("✓ UPGRADE MAKSIMUM (+" + maxLvl + ")");
            maxed.setStyle("-fx-text-fill: #C8860A; -fx-font-family: \'Courier New\'; -fx-font-size: 10px;");
            upgradeRow.getChildren().add(maxed);
        }

        // ── KALIBRASI section ────────────────────────────────
        HBox calibRow = new HBox(8);
        boolean hasCalKit = countMaterial(Material.MaterialType.CALIBRATOR) > 0;
        String calLabel = "◈ KALIBRASI" + (hasCalKit ? "  (Cal Kit: " + countMaterial(Material.MaterialType.CALIBRATOR) + ")" : "  (Cal Kit: 0)");
        Button calBtn = new Button(calLabel);
        calBtn.setDisable(!hasCalKit);
        styleWorkshopBtn(calBtn, hasCalKit ? "#4455CC" : "#3A2810", hasCalKit);
        calBtn.setOnAction(e -> {
            if (!hasCalKit) return;
            var res = engine.getInventory().calibrateItem(eq.getId(), 1);
            router.addSystemChat(res.success
                ? "◈ " + eq.getName() + " dikalibrasi! " + res.message.replace("Calibration successful. ", "")
                : "✗ Kalibrasi: " + res.message);
            router.showCityArea("BENGKEL");
        });
        calibRow.getChildren().add(calBtn);

        card.getChildren().addAll(nameRow, upgradeRow, calibRow);
        return card;
    }

    private void styleWorkshopBtn(Button btn, String color, boolean enabled) {
        btn.setStyle(
            "-fx-background-color: " + (enabled ? color + "22" : "transparent") + ";" +
            "-fx-border-color: " + (enabled ? color : "#3A2810") + "; -fx-border-width: 1;" +
            "-fx-text-fill: " + (enabled ? color : "#3A2810") + ";" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
            "-fx-padding: 5 10; -fx-cursor: " + (enabled ? "hand" : "default") + ";");
        btn.setDisable(!enabled);
    }

    // ── Pawnshop (Penadah Barang) — Batch Sell ──────────────────

    // Filter state (persistent selama view aktif)
    private String pawnFilter    = "SEMUA";  // rarity key atau "EQUIPMENT"/"MATERIAL"

    /** Mengembalikan BorderPane agar filter bar sticky dan list bisa scroll sendiri */
    private BorderPane buildPawnshop() {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: transparent;");

        // ── Top: desc + filter bar ─────────────────────────
        VBox topBox = new VBox(8);
        topBox.setPadding(new Insets(10, 12, 6, 12));
        topBox.setStyle("-fx-background-color: #0A0604;");

        Label desc = new Label("Jual itemmu dengan harga 40% dari nilai aslinya.");
        desc.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        HBox filterBar = new HBox(5);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(2, 0, 2, 0));

        // Filter pill definition: key -> label
        String[][] pills = {
            {"SEMUA", "SEMUA"},
            {"COMMON",    "Common"},
            {"UNCOMMON",  "Uncommon"},
            {"RARE",      "Rare"},
            {"EPIC",      "Epic"},
            {"LEGENDARY", "Legendary"},
            {"EQUIPMENT", "⚔ Equip"},
            {"MATERIAL",  "◈ Material"},
        };

        // Batch bar (ref agar bisa di-update dari filter)
        HBox batchBar = new HBox(10);
        batchBar.setAlignment(Pos.CENTER_LEFT);
        batchBar.setPadding(new Insets(6, 0, 2, 0));

        // Item list container
        VBox listBox = new VBox(6);
        listBox.setPadding(new Insets(6, 12, 12, 12));

        // Helper: hitung sell price item
        java.util.function.Function<Item, Integer> priceOf = item -> {
            int seed = Math.abs(item.getId().hashCode());
            int base = switch (item.getRarity()) {
                case COMMON    -> 15  + (seed % 10);
                case UNCOMMON  -> 45  + (seed % 20);
                case RARE      -> 120 + (seed % 50);
                case EPIC      -> 300 + (seed % 100);
                case LEGENDARY -> 700 + (seed % 200);
                case MYTHIC    -> 0;
            };
            return (int)(base * 0.4);
        };

        // Helper: filter items
        java.util.function.Supplier<List<Item>> getFiltered = () -> {
            List<Item> all = new ArrayList<>(engine.getInventory().getAllBagItems());
            return all.stream()
                .filter(it -> it.getRarity() != Item.Rarity.MYTHIC)
                .filter(it -> switch (pawnFilter) {
                    case "COMMON"    -> it.getRarity() == Item.Rarity.COMMON;
                    case "UNCOMMON"  -> it.getRarity() == Item.Rarity.UNCOMMON;
                    case "RARE"      -> it.getRarity() == Item.Rarity.RARE;
                    case "EPIC"      -> it.getRarity() == Item.Rarity.EPIC;
                    case "LEGENDARY" -> it.getRarity() == Item.Rarity.LEGENDARY;
                    case "EQUIPMENT" -> it.getItemType() != Item.ItemType.MATERIAL;
                    case "MATERIAL"  -> it.getItemType() == Item.ItemType.MATERIAL;
                    default          -> true; // SEMUA
                })
                .collect(java.util.stream.Collectors.toList());
        };

        // Helper: refresh list + batch bar
        Runnable[] refreshRef = {null};
        refreshRef[0] = () -> {
            List<Item> filtered = getFiltered.get();

            // Batch bar
            batchBar.getChildren().clear();
            if (!filtered.isEmpty()) {
                int total = filtered.stream().mapToInt(it -> priceOf.apply(it)).sum();
                Label info = new Label("📦 " + filtered.size() + " item  ·  ⚙ " + total);
                info.setStyle("-fx-text-fill: #C8860A; -fx-font-family: \'Courier New\'; -fx-font-size: 11px;");

                Button batchBtn = new Button("JUAL SEKALIGUS");
                batchBtn.setStyle(
                    "-fx-background-color: #8B220022; -fx-border-color: #CC3300; -fx-border-width: 1;" +
                    "-fx-text-fill: #FF7755; -fx-font-family: \'Courier New\'; -fx-font-size: 11px;" +
                    "-fx-font-weight: bold; -fx-padding: 3 14; -fx-cursor: hand;");
                batchBtn.setOnMouseEntered(e -> batchBtn.setStyle(
                    "-fx-background-color: #CC330044; -fx-border-color: #FF5533; -fx-border-width: 1;" +
                    "-fx-text-fill: #FFAA88; -fx-font-family: \'Courier New\'; -fx-font-size: 11px;" +
                    "-fx-font-weight: bold; -fx-padding: 3 14; -fx-cursor: hand;"));
                batchBtn.setOnMouseExited(e -> batchBtn.setStyle(
                    "-fx-background-color: #8B220022; -fx-border-color: #CC3300; -fx-border-width: 1;" +
                    "-fx-text-fill: #FF7755; -fx-font-family: \'Courier New\'; -fx-font-size: 11px;" +
                    "-fx-font-weight: bold; -fx-padding: 3 14; -fx-cursor: hand;"));

                final int finalTotal = total;
                final List<Item> toSell = new ArrayList<>(filtered);
                batchBtn.setOnAction(e -> {
                    int goldGain = 0;
                    for (Item it : toSell) {
                        int sp = priceOf.apply(it);
                        engine.getInventory().removeItem(it.getId());
                        engine.getPlayer().addGold(sp);
                        goldGain += sp;
                        // Material bonus (sama seperti sell satuan)
                        giveMaterialBonus(it.getRarity());
                    }
                    router.addSystemChat("✦ " + toSell.size() + " item dijual · ⚙ +" + goldGain);
                    engine.autoSave();
                    router.showCityArea("PENADAH");
                });

                batchBar.getChildren().addAll(info, batchBtn);
            } else {
                Label none2 = new Label("— tidak ada item di filter ini —");
                none2.setStyle("-fx-text-fill: #3A2810; -fx-font-family: \'Courier New\'; -fx-font-size: 10px;");
                batchBar.getChildren().add(none2);
            }

            // Item list
            listBox.getChildren().clear();
            if (filtered.isEmpty()) {
                Label none = new Label("Tidak ada item untuk dijual.");
                none.setStyle("-fx-text-fill: #3A2810; -fx-font-family: \'Courier New\'; -fx-font-size: 11px; -fx-padding: 20;");
                listBox.getChildren().add(none);
            } else {
                for (Item item : filtered) {
                    listBox.getChildren().add(buildSellRow(item, (int) priceOf.apply(item)));
                }
            }
        };

        // Build filter pills
        for (String[] pill : pills) {
            String key = pill[0];
            String lbl = pill[1];

            // Separator sebelum EQUIPMENT
            if (key.equals("EQUIPMENT")) {
                Label sep = new Label("|");
                sep.setStyle("-fx-text-fill: #3A2810; -fx-font-family: \'Courier New\'; -fx-padding: 0 4;");
                filterBar.getChildren().add(sep);
            }

            Button b = new Button(lbl);
            boolean active = pawnFilter.equals(key);
            String activeStyle = "-fx-background-color: #C8860A44; -fx-border-color: #C8860A; -fx-border-width: 1;" +
                "-fx-text-fill: #FFD080; -fx-font-family: \'Courier New\'; -fx-font-size: 10px;" +
                "-fx-padding: 3 10; -fx-cursor: hand;";
            String inactiveStyle = "-fx-background-color: transparent; -fx-border-color: #2A1808; -fx-border-width: 1;" +
                "-fx-text-fill: #5A3A10; -fx-font-family: \'Courier New\'; -fx-font-size: 10px;" +
                "-fx-padding: 3 10; -fx-cursor: hand;";
            b.setStyle(active ? activeStyle : inactiveStyle);

            Runnable[] ref = {refreshRef[0]};
            b.setOnAction(ev -> {
                pawnFilter = key;
                // Update semua pill style
                for (javafx.scene.Node n : filterBar.getChildren()) {
                    if (n instanceof Button pb) {
                        boolean isActive = pb.getText().equals(lbl) ||
                            (pb == b);
                        // re-style based on pawnFilter match
                    }
                }
                // Simpler: just rebuild filter styles
                for (javafx.scene.Node n : filterBar.getChildren()) {
                    if (n instanceof Button pb) {
                        boolean isThis = pb == b;
                        // find key from pills
                        for (String[] p2 : pills) {
                            if (p2[1].equals(pb.getText())) {
                                pb.setStyle(p2[0].equals(pawnFilter) ? activeStyle : inactiveStyle);
                                break;
                            }
                        }
                    }
                }
                refreshRef[0].run();
            });
            filterBar.getChildren().add(b);
        }

        // Separator line
        javafx.scene.control.Separator sepLine = new javafx.scene.control.Separator();
        sepLine.setStyle("-fx-background-color: #2A1808;");

        topBox.getChildren().addAll(desc, filterBar, batchBar, sepLine);
        pane.setTop(topBox);

        // ── Center: scrollable item list ──────────────────
        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604; -fx-border-color: transparent;");
        pane.setCenter(scroll);

        // Initial render
        refreshRef[0].run();
        return pane;
    }

    private HBox buildSellRow(Item item, int sellPrice) {
        String rc = UIFactory.rarityColor(item.getRarity());
        HBox row = new HBox(10);
        row.setPadding(new Insets(8, 0, 8, 0));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-border-color: #2A1808; -fx-border-width: 0 0 1 0;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(item.getFullName());
        name.setStyle("-fx-text-fill: " + rc + "; -fx-font-family: 'Courier New';" +
                      "-fx-font-size: 12px; -fx-font-weight: bold;");
        Label rarityLbl = new Label("[" + item.getRarity().displayName + "]");
        rarityLbl.setStyle("-fx-text-fill: " + rc + "88; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 10px;");
        info.getChildren().addAll(name, rarityLbl);

        Label priceLabel = new Label("⚙ " + sellPrice);
        priceLabel.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                            "-fx-font-size: 12px; -fx-font-weight: bold;");

        Button sellBtn = new Button("JUAL");
        sellBtn.setStyle(
            "-fx-background-color: #CC330022; -fx-border-color: #CC3300; -fx-border-width: 1;" +
            "-fx-text-fill: #FF5533; -fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
            "-fx-padding: 4 12; -fx-cursor: hand;");
        final int fp = sellPrice;
        sellBtn.setOnAction(e -> {
            engine.getInventory().removeItem(item.getId());
            engine.getPlayer().addGold(fp);
            giveMaterialBonus(item.getRarity());
            router.addSystemChat("✓ " + item.getName() + " dijual ⚙" + fp);
            engine.autoSave();
            router.showCityArea("PENADAH");
        });

        row.getChildren().addAll(info, priceLabel, sellBtn);
        return row;
    }

    /** Berikan material bonus saat jual item (dipanggil batch maupun satuan) */
    private void giveMaterialBonus(Item.Rarity rarity) {
        java.util.Random rng = new java.util.Random();
        if (rarity == null) return;
        switch (rarity) {
            case COMMON -> {
                if (rng.nextDouble() < 0.40)
                    engine.getInventory().addItem(new Material("Scrap Metal", "", rarity, Material.MaterialType.SCRAP_METAL));
            }
            case UNCOMMON -> {
                engine.getInventory().addItem(new Material("Scrap Metal", "", rarity, Material.MaterialType.SCRAP_METAL));
                if (rng.nextDouble() < 0.20)
                    engine.getInventory().addItem(new Material("Circuit Chip", "", rarity, Material.MaterialType.CYBER_CHIP));
            }
            case RARE -> {
                engine.getInventory().addItem(new Material("Scrap Metal", "", rarity, Material.MaterialType.SCRAP_METAL));
                if (rng.nextDouble() < 0.50)
                    engine.getInventory().addItem(new Material("Circuit Chip", "", rarity, Material.MaterialType.CYBER_CHIP));
                if (rng.nextDouble() < 0.15)
                    engine.getInventory().addItem(new Material("Neon Crystal", "", rarity, Material.MaterialType.NEON_CRYSTAL));
            }
            case EPIC, LEGENDARY -> {
                engine.getInventory().addItem(new Material("Circuit Chip", "", rarity, Material.MaterialType.CYBER_CHIP));
                if (rng.nextDouble() < 0.40)
                    engine.getInventory().addItem(new Material("Neon Crystal", "", rarity, Material.MaterialType.NEON_CRYSTAL));
                if (rng.nextDouble() < 0.10)
                    engine.getInventory().addItem(new Material("Cal Kit", "", rarity, Material.MaterialType.CALIBRATION_KIT));
            }
            default -> {}
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private long countMaterial(Material.MaterialType type) {
        if (engine.getInventory() == null) return 0;
        return engine.getInventory().getAllBagItems().stream()
            .filter(i -> i instanceof Material m && m.getMaterialType() == type)
            .count();
    }

    private void consumeMaterial(Material.MaterialType type) {
        engine.getInventory().getAllBagItems().stream()
            .filter(i -> i instanceof Material m && m.getMaterialType() == type)
            .findFirst()
            .ifPresent(i -> engine.getInventory().removeItem(i.getId()));
    }
}
