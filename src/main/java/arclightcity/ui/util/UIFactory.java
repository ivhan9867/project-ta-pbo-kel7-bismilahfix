package arclightcity.ui.util;
import arclightcity.ui.ArclightApp;
import arclightcity.item.Item;
import arclightcity.entity.stats.DamageType;
import arclightcity.entity.stats.StatType;
import arclightcity.entity.status.StatusEffect;
import arclightcity.entity.status.StatusEffectType;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * UIFactory — semua komponen reusable UI game.
 * Dipakai oleh semua View agar konsisten.
 */
public class UIFactory {

    // ── Color Constants ───────────────────────────────────
    public static final String CYAN    = "#00E5FF";
    public static final String YELLOW  = "#FFD600";
    public static final String RED     = "#FF1744";
    public static final String PURPLE  = "#AA00FF";
    public static final String GREEN   = "#00E676";
    public static final String ORANGE  = "#FF6B00";
    public static final String DIM     = "#5A6A80";
    public static final String TEXT    = "#E0E8F0";
    public static final String BG_DEEP = "#050810";
    public static final String BG_PANEL= "#0C1220";
    public static final String BORDER  = "#1C2E44";

    // ══════════════════════════════════════════════════════
    // SCREEN WRAPPER
    // ══════════════════════════════════════════════════════

    /**
     * Buat root VBox untuk setiap screen.
     */
    public static VBox screenRoot() {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: " + BG_DEEP + ";");
        root.setPrefSize(ArclightApp.GAME_WIDTH, ArclightApp.SCREEN_HEIGHT);
        root.setMinSize(ArclightApp.GAME_WIDTH, ArclightApp.SCREEN_HEIGHT);
        root.setMaxWidth(ArclightApp.GAME_WIDTH);
        return root;
    }

    // ══════════════════════════════════════════════════════
    // HEADER BAR (top bar dengan back button + resource)
    // ══════════════════════════════════════════════════════

    public static HBox headerBar(String title, Runnable onBack) {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

        if (onBack != null) {
            Button back = new Button("← BACK");
            back.getStyleClass().add("btn-small");
            back.setOnAction(e -> onBack.run());
            bar.getChildren().add(back);
        }

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-family: 'Courier New', monospace; -fx-font-size: 14px; -fx-font-weight: bold;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        bar.getChildren().add(titleLabel);

        return bar;
    }

    /**
     * Header dengan resource bar (gold + floor info).
     */
    public static HBox headerWithResources(String title, Runnable onBack,
                                            long gold, int floor) {
        HBox bar = headerBar(title, onBack);

        HBox resources = new HBox(12);
        resources.setAlignment(Pos.CENTER_RIGHT);

        if (floor > 0) {
            Label floorLabel = new Label("F" + floor);
            floorLabel.setStyle("-fx-text-fill: " + CYAN + "; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
            resources.getChildren().add(floorLabel);
        }

        Label goldLabel = new Label("⚙ " + formatNumber(gold));
        goldLabel.setStyle("-fx-text-fill: " + YELLOW + "; -fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;");
        resources.getChildren().add(goldLabel);

        bar.getChildren().add(resources);
        return bar;
    }

    // ══════════════════════════════════════════════════════
    // VITAL BARS (HP / Shield / MP / EXP)
    // ══════════════════════════════════════════════════════

    /**
     * Buat satu bar dengan label.
     * @param label  nama bar (HP / SHIELD / MP)
     * @param color  hex warna bar
     * @param current nilai saat ini
     * @param max    nilai maksimum
     */
    public static VBox vitalBar(String label, String color,
                                 double current, double max) {
        VBox box = new VBox(3);

        HBox labelRow = new HBox();
        labelRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-text-fill: " + DIM + "; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        Label valueLabel = new Label(formatNumber((long)current) + "/" + formatNumber((long)max));
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        labelRow.getChildren().addAll(nameLabel, valueLabel);

        ProgressBar bar = new ProgressBar(max > 0 ? current / max : 0);
        bar.setPrefWidth(Double.MAX_VALUE);
        bar.setPrefHeight(7);
        bar.setStyle(
            "-fx-accent: " + color + ";" +
            "-fx-background-color: " + adjustOpacity(color, 0.15) + ";" +
            "-fx-min-height: 7px; -fx-max-height: 7px;"
        );

        box.getChildren().addAll(labelRow, bar);
        return box;
    }

    /**
     * Compact vital bars untuk kartu entity di combat.
     */
    public static VBox compactVitalBars(double hp, double maxHp,
                                         double shield, double maxShield,
                                         double mp, double maxMp) {
        VBox box = new VBox(3);

        // HP bar
        box.getChildren().add(vitalBar("HP", RED, hp, maxHp));

        // Shield bar (hanya tampil jika max > 0)
        if (maxShield > 0) {
            box.getChildren().add(vitalBar("SHD", PURPLE, shield, maxShield));
        }

        // MP bar
        if (maxMp > 0) {
            box.getChildren().add(vitalBar("MP", "#2979FF", mp, maxMp));
        }

        return box;
    }

    // ══════════════════════════════════════════════════════
    // STAT ROW (untuk profile screen)
    // ══════════════════════════════════════════════════════

    public static HBox statRow(String name, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 12, 5, 12));
        row.setStyle("-fx-border-color: transparent transparent " + BORDER + " transparent; -fx-border-width: 0 0 1 0;");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: #8899AA; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + CYAN + "; -fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;");

        row.getChildren().addAll(nameLabel, valueLabel);
        return row;
    }

    public static HBox statRowHighlight(String name, String value, String color) {
        HBox row = statRow(name, value);
        // Override value color
        Label val = (Label) row.getChildren().get(1);
        val.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold;");
        return row;
    }

    // ══════════════════════════════════════════════════════
    // PANEL WRAPPERS
    // ══════════════════════════════════════════════════════

    public static VBox panel(javafx.scene.Node... children) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-border-color: " + BORDER + "; -fx-border-width: 1;");
        box.getChildren().addAll(children);
        return box;
    }

    public static VBox panelHighlight(javafx.scene.Node... children) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-border-color: " + CYAN + "; -fx-border-width: 1 1 1 3;");
        box.getChildren().addAll(children);
        return box;
    }

    public static Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #8899AA; -fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-font-weight: bold;");
        return label;
    }

    // ══════════════════════════════════════════════════════
    // BUTTONS
    // ══════════════════════════════════════════════════════

    public static Button btnPrimary(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: " + CYAN + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + CYAN + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + CYAN + "22;" +
            "-fx-border-color: " + CYAN + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + CYAN + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, " + CYAN + ", 10, 0.3, 0, 0);"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: " + CYAN + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + CYAN + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }

    public static Button btnDanger(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: " + RED + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + RED + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;"
        );
        return btn;
    }

    public static Button btnGold(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
            "-fx-background-color: " + YELLOW + "22;" +
            "-fx-border-color: " + YELLOW + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + YELLOW + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;"
        );
        return btn;
    }

    // ══════════════════════════════════════════════════════
    // STATUS EFFECT BADGE
    // ══════════════════════════════════════════════════════

    public static Label effectBadge(StatusEffect effect) {
        Label badge = new Label(effect.getType().displayName);
        String color = switch (effect.getType().category) {
            case BUFF    -> GREEN;
            case DEBUFF  -> "#FF6B6B";
            case DOT     -> ORANGE;
            case CONTROL -> RED;
        };
        badge.setStyle(
            "-fx-background-color: " + color + "22;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 9px;" +
            "-fx-padding: 2 5;" +
            "-fx-label-padding: 0;"
        );

        Tooltip tip = new Tooltip(effect.getType().description +
                "\nTurns: " + effect.getRemainingTurns() +
                (effect.getStackCount() > 1 ? " | Stack: " + effect.getStackCount() : ""));
        tip.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-text-fill: " + TEXT + "; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        Tooltip.install(badge, tip);

        return badge;
    }

    // ══════════════════════════════════════════════════════
    // DAMAGE TYPE LABEL
    // ══════════════════════════════════════════════════════

    public static String damageTypeColor(DamageType type) {
        return switch (type) {
            case PHYSICAL -> "#FF8A65";
            case CYBER    -> CYAN;
            case ENERGY   -> PURPLE;
            case TRUE     -> YELLOW;
            case HEAL     -> GREEN;
        };
    }

    // ══════════════════════════════════════════════════════
    // RARITY STYLE
    // ══════════════════════════════════════════════════════

    public static String rarityColor(Item.Rarity rarity) {
        return rarity.hexColor;
    }

    public static String rarityBorderStyle(Item.Rarity rarity) {
        return "-fx-border-color: " + rarity.hexColor + "; -fx-border-width: 1 1 1 3;";
    }

    // ══════════════════════════════════════════════════════
    // ANIMATIONS
    // ══════════════════════════════════════════════════════

    /**
     * Fade in sebuah node dari opacity 0 ke 1.
     */
    public static void fadeIn(javafx.scene.Node node, double durationMs) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * Flash/pulse animasi untuk damage number.
     */
    public static void flashNode(javafx.scene.Node node) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), node);
        scale.setFromX(1); scale.setFromY(1);
        scale.setToX(1.3); scale.setToY(1.3);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    /**
     * Shake animasi untuk node yang terkena damage.
     */
    public static void shake(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0); tt.setByX(6);
        tt.setAutoReverse(true);
        tt.setCycleCount(6);
        tt.play();
    }

    /**
     * Glow pulse untuk entity yang giliran-nya aktif.
     */
    public static Timeline glowPulse(javafx.scene.Node node, String glowColor) {
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(Color.web(glowColor));
        shadow.setRadius(0);
        node.setEffect(shadow);

        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(shadow.radiusProperty(), 0)),
            new KeyFrame(Duration.millis(1000),
                // Max radius 6 — cukup terlihat, tidak blur teks
                new KeyValue(shadow.radiusProperty(), 6)),
            new KeyFrame(Duration.millis(2000),
                new KeyValue(shadow.radiusProperty(), 0))
        );
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
        return tl;
    }

    // ══════════════════════════════════════════════════════
    // DIVIDER
    // ══════════════════════════════════════════════════════

    public static Separator divider() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        return sep;
    }

    public static Region spacer() {
        Region r = new Region();
        VBox.setVgrow(r, Priority.ALWAYS);
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    // ══════════════════════════════════════════════════════
    // UTILITY
    // ══════════════════════════════════════════════════════

    public static String formatNumber(long n) {
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 1_000)     return String.format("%.1fK", n / 1_000.0);
        return String.valueOf(n);
    }

    private static String adjustOpacity(String hex, double opacity) {
        // Tambah opacity sebagai alpha hex (approx)
        int alpha = (int)(opacity * 255);
        return hex + String.format("%02X", alpha);
    }
}
