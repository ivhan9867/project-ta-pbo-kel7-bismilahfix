package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.entity.player.PlayerBackground;
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
 * CharacterCreateView — Intro lore + masukkan nama Asuna.
 * Tidak ada lagi class selection — pemain selalu Asuna.
 */
public class CharacterCreateView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private TextField         nameField;

    public CharacterCreateView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    public Parent build() {
        BorderPane root = UIFactory.screenRootBorder();

        // ── TOP: header ───────────────────────────────────
        root.setTop(buildHeader());

        // ── CENTER: lore + name input ─────────────────────
        VBox content = new VBox(10);
        content.setPadding(new Insets(6, 12, 6, 12));

        content.getChildren().add(buildLoreCard());
        content.getChildren().add(buildStatsPreview());
        content.getChildren().add(buildNameSection());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        // ── BOTTOM: start button ──────────────────────────
        root.setBottom(buildBottomBar());

        UIFactory.fadeIn(root, 500);
        return root;
    }

    // ── Header ────────────────────────────────────────────

    private HBox buildHeader() {
        HBox hdr = new HBox(12);
        hdr.setPadding(new Insets(6, 12, 6, 12));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-background-color: #0F0A06;" +
                     "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Button back = new Button("← KEMBALI");
        back.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                      "-fx-border-width: 1; -fx-text-fill: #5A3A10;" +
                      "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                      "-fx-padding: 3 8; -fx-cursor: hand;");
        back.setOnAction(e -> router.showMainMenu());

        Label title = new Label("✦  MULAI PETUALANGAN");
        title.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 13px; -fx-font-weight: bold;" +
                       "-fx-effect: dropshadow(gaussian, #C8860A, 8, 0.3, 0, 0);");
        HBox.setHgrow(title, Priority.ALWAYS);

        hdr.getChildren().addAll(back, title);
        return hdr;
    }

    // ── Lore Card ─────────────────────────────────────────

    private VBox buildLoreCard() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #150E08;" +
                      "-fx-border-color: #C8860A44; -fx-border-width: 1 1 1 3;" +
                      "-fx-padding: 16;");

        // Avatar + Nama
        HBox avatarRow = new HBox(16);
        avatarRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        Circle avatarBg = new Circle(32, Color.web("#0F0A06"));
        avatarBg.setStroke(Color.web("#FFB830")); avatarBg.setStrokeWidth(2);
        Label avatarTxt = new Label("⚔");
        avatarTxt.setStyle("-fx-font-size: 28px;");

        // Pulse animation avatar
        ScaleTransition pulse = new ScaleTransition(Duration.millis(1800), avatarBg);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.05);  pulse.setToY(1.05);
        pulse.setAutoReverse(true); pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        avatar.getChildren().addAll(avatarBg, avatarTxt);

        VBox charInfo = new VBox(4);
        HBox.setHgrow(charInfo, Priority.ALWAYS);

        Label charName = new Label("ASUNA");
        charName.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 18px; -fx-font-weight: bold;" +
                          "-fx-effect: dropshadow(gaussian, #C8860A, 12, 0.5, 0, 0);");

        Label charTitle = new Label("Pendekar Pedang dari Dunia Lain");
        charTitle.setStyle("-fx-text-fill: #C8860A; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 11px;");

        Label charType = new Label("Tipe: Slash ⚔  |  Senjata Andalan: Katana");
        charType.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 10px;");

        charInfo.getChildren().addAll(charName, charTitle, charType);
        avatarRow.getChildren().addAll(avatar, charInfo);

        // Divider
        Label div = new Label("─────────────────────────────────────────");
        div.setStyle("-fx-text-fill: #2A1808; -fx-font-family: 'Courier New';");

        // Lore paragraf
        String[] loreParts = {
            "Asuna — seorang perempuan muda yang gemar bermain game hack-and-slash " +
            "dengan katana sebagai senjata andalannya.",

            "Suatu malam, ketika kelelahan dan layar game-nya masih menyala... " +
            "ia terbangun di sebuah desa kuno di Nusantara.",

            "Di sana, ras Demon Lord dipimpin Theresa sedang menghancurkan tatanan alam, " +
            "mengubah tanah hijau Nusantara menjadi dataran es hampa.",

            "Untuk mengakhiri segalanya, Asuna harus mengumpulkan 5 Serpihan Red Essence " +
            "dari para boss untuk menempa ✦ Red Blossom Katana — satu-satunya senjata " +
            "yang dapat melukai Theresa."
        };

        VBox loreBox = new VBox(8);
        for (String part : loreParts) {
            Label lbl = new Label(part);
            lbl.setWrapText(true);
            lbl.setStyle("-fx-text-fill: #A09070; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 11px; -fx-line-spacing: 3;");
            loreBox.getChildren().add(lbl);
        }

        // Misi utama
        VBox mission = new VBox(4);
        mission.setStyle("-fx-background-color: #1A0A04; -fx-border-color: #FF6B0044;" +
                         "-fx-border-width: 1; -fx-padding: 10;");
        Label missionTitle = new Label("✦  MISI UTAMA");
        missionTitle.setStyle("-fx-text-fill: #FF8833; -fx-font-family: 'Courier New';" +
                              "-fx-font-size: 11px; -fx-font-weight: bold;");
        Label missionDesc = new Label(
            "Kumpulkan 5 Serpihan Red Essence dari 5 boss (Floor 10/20/30/40/50)\n" +
            "→ Tempa Red Blossom Katana\n" +
            "→ Lawan Theresa di Floor 51"
        );
        missionDesc.setStyle("-fx-text-fill: #6A4020; -fx-font-family: 'Courier New';" +
                             "-fx-font-size: 11px;");
        mission.getChildren().addAll(missionTitle, missionDesc);

        card.getChildren().addAll(avatarRow, div, loreBox, mission);
        return card;
    }

    // ── Stats Preview ─────────────────────────────────────

    private VBox buildStatsPreview() {
        VBox panel = new VBox(6);
        panel.setStyle("-fx-background-color: #1A1008; -fx-border-color: #3A2810;" +
                       "-fx-border-width: 1; -fx-padding: 12;");

        Label title = new Label("── KEMAMPUAN AWAL ASUNA ──");
        title.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 10px; -fx-font-weight: bold; -fx-letter-spacing: 2;");

        // 3 kolom stat
        GridPane grid = new GridPane();
        grid.setHgap(24); grid.setVgap(4);

        String[][] stats = {
            {"HP", "120", "ATK Fisik", "22", "Kecepatan", "16"},
            {"Kritis", "18%", "Evasion", "12%", "Lifesteal", "5%"},
            {"Armor Tembus", "10%", "Crit DMG", "50%", "Inisiatif", "14"}
        };

        for (int row = 0; row < stats.length; row++) {
            for (int col = 0; col < 3; col++) {
                HBox pair = new HBox(6);
                pair.setAlignment(Pos.CENTER_LEFT);
                Label name = new Label(stats[row][col*2]);
                name.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New';" +
                              "-fx-font-size: 10px; -fx-min-width: 80;");
                Label val = new Label(stats[row][col*2+1]);
                val.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                             "-fx-font-size: 11px; -fx-font-weight: bold;");
                pair.getChildren().addAll(name, val);
                grid.add(pair, col, row);
            }
        }

        Label skills = new Label("Jurus Awal: Pukulan Harimau  ✦  Panah Bayangan");
        skills.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        panel.getChildren().addAll(title, grid, skills);
        return panel;
    }

    // ── Name Input ────────────────────────────────────────

    private VBox buildNameSection() {
        VBox sec = new VBox(8);

        Label lbl = new Label("── NAMA PANGGILANMU ──");
        lbl.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New';" +
                     "-fx-font-size: 10px; -fx-font-weight: bold; -fx-letter-spacing: 2;");

        Label hint = new Label("Biarkan kosong untuk menggunakan nama 'Asuna'");
        hint.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        nameField = new TextField();
        nameField.setPromptText("Asuna");
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setStyle(
            "-fx-background-color: #1A1008; -fx-border-color: #3A2810; -fx-border-width: 1;" +
            "-fx-text-fill: #EDE0C8; -fx-font-family: 'Courier New'; -fx-font-size: 14px;" +
            "-fx-prompt-text-fill: #3A2810; -fx-padding: 10 12;"
        );
        nameField.focusedProperty().addListener((obs, old, focused) -> {
            String base = "-fx-background-color: #1A1008; -fx-border-width: 1;" +
                          "-fx-text-fill: #EDE0C8; -fx-font-family: 'Courier New';" +
                          "-fx-font-size: 14px; -fx-prompt-text-fill: #3A2810; -fx-padding: 10 12;";
            nameField.setStyle(base + (focused
                ? "-fx-border-color: #C8860A; -fx-effect: dropshadow(gaussian, #C8860A, 8, 0.2, 0, 0);"
                : "-fx-border-color: #3A2810;"));
        });

        sec.getChildren().addAll(lbl, hint, nameField);
        return sec;
    }

    // ── Bottom Bar ────────────────────────────────────────

    private HBox buildBottomBar() {
        HBox bottom = new HBox(12);
        bottom.setPadding(new Insets(6, 12, 8, 12));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setStyle("-fx-background-color: #0F0A06;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 1 0 0 0;");

        Label info = new Label("Perjalanan Asuna di Nusantara dimulai...");
        info.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        HBox.setHgrow(info, Priority.ALWAYS);

        Button start = new Button("MULAI PETUALANGAN  ▶");
        start.setStyle(
            "-fx-background-color: #C8860A22; -fx-border-color: #FFB830; -fx-border-width: 1;" +
            "-fx-text-fill: #FFB830; -fx-font-family: 'Courier New'; -fx-font-size: 13px;" +
            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 8, 0.3, 0, 0);"
        );
        start.setOnMouseEntered(ev -> start.setStyle(
            "-fx-background-color: #C8860A44; -fx-border-color: #FFB830; -fx-border-width: 1;" +
            "-fx-text-fill: #FFB830; -fx-font-family: 'Courier New'; -fx-font-size: 13px;" +
            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #FFB830, 16, 0.5, 0, 0);"
        ));
        start.setOnMouseExited(ev -> start.setStyle(
            "-fx-background-color: #C8860A22; -fx-border-color: #FFB830; -fx-border-width: 1;" +
            "-fx-text-fill: #FFB830; -fx-font-family: 'Courier New'; -fx-font-size: 13px;" +
            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 8, 0.3, 0, 0);"
        ));
        start.setOnAction(e -> {
            String name = nameField != null && !nameField.getText().trim().isEmpty()
                          ? nameField.getText().trim()
                          : "Asuna";
            engine.createCharacter(name, PlayerBackground.ASUNA);
            router.showHub();
        });

        bottom.getChildren().addAll(info, start);
        return bottom;
    }

    public void setSelectedBg(PlayerBackground bg) { /* no-op, always ASUNA */ }
    public void setNameText(String name) {
        if (nameField != null) nameField.setText(name);
    }
}
