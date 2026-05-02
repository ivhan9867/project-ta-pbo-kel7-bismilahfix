package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.entity.player.PlayerBackground;
import arclightcity.ui.ArclightApp;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.util.Duration;

/**
 * CharacterCreateView — Pilih asal usul dan nama karakter.
 * Tema: Prasasti kuno Nusantara — pilih jalan hidupmu.
 */
public class CharacterCreateView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private PlayerBackground  selectedBg = PlayerBackground.STREET_BRAWLER;
    private TextField         nameField;

    public CharacterCreateView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    public Parent build() {
        BorderPane root = UIFactory.screenRootBorder();

        // ── TOP: header ───────────────────────────────────
        VBox header = buildHeader();
        root.setTop(header);

        // ── CENTER: scrollable content ────────────────────
        VBox content = new VBox(16);
        content.setPadding(new Insets(16));

        // Nama karakter
        content.getChildren().add(buildNameSection());

        // Pilih asal usul
        Label bgTitle = new Label("── PILIH ASAL USUL ──");
        bgTitle.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 10px; -fx-font-weight: bold; -fx-letter-spacing: 2;");
        content.getChildren().add(bgTitle);

        // Grid background cards
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);

        PlayerBackground[] bgs = PlayerBackground.values();
        for (int i = 0; i < bgs.length; i++) {
            VBox card = buildBgCard(bgs[i]);
            grid.add(card, i % 2, i / 2);
            GridPane.setHgrow(card, Priority.ALWAYS);
        }
        content.getChildren().add(grid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        // ── BOTTOM: confirm button ────────────────────────
        root.setBottom(buildBottomAction());

        UIFactory.fadeIn(root, 400);
        return root;
    }

    private VBox buildHeader() {
        VBox hdr = new VBox(4);
        hdr.setPadding(new Insets(14, 16, 14, 16));
        hdr.setStyle("-fx-background-color: #0F0A06;" +
                     "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Button back = new Button("← KEMBALI");
        back.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                      "-fx-border-width: 1; -fx-text-fill: #6A5840;" +
                      "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                      "-fx-padding: 4 10; -fx-cursor: hand;");
        back.setOnAction(e -> router.showMainMenu());
        back.setOnMouseEntered(ev -> back.setStyle(
            "-fx-background-color: transparent; -fx-border-color: #C8860A;" +
            "-fx-border-width: 1; -fx-text-fill: #FFB830;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-padding: 4 10; -fx-cursor: hand;"));
        back.setOnMouseExited(ev -> back.setStyle(
            "-fx-background-color: transparent; -fx-border-color: #3A2810;" +
            "-fx-border-width: 1; -fx-text-fill: #6A5840;" +
            "-fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-padding: 4 10; -fx-cursor: hand;"));

        Label title = new Label("✦  PILIH JALANMU  ✦");
        title.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 15px; -fx-font-weight: bold;" +
                       "-fx-effect: dropshadow(gaussian, #C8860A, 8, 0.3, 0, 0);");
        HBox.setHgrow(title, Priority.ALWAYS);

        row.getChildren().addAll(back, title);

        Label sub = new Label("Setiap asal usul menentukan skill awal dan takdir petualanganmu");
        sub.setStyle("-fx-text-fill: #4A3820; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        hdr.getChildren().addAll(row, sub);
        return hdr;
    }

    private VBox buildNameSection() {
        VBox sec = new VBox(6);
        sec.setPadding(new Insets(12, 0, 4, 0));

        Label lbl = new Label("── NAMA PENDEKAR ──");
        lbl.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New';" +
                     "-fx-font-size: 10px; -fx-font-weight: bold; -fx-letter-spacing: 2;");

        nameField = new TextField();
        nameField.setPromptText("Masukkan nama karaktermu...");
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setStyle(
            "-fx-background-color: #1A1008;" +
            "-fx-border-color: #3A2810;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #EDE0C8;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 14px;" +
            "-fx-prompt-text-fill: #3A2810;" +
            "-fx-padding: 10 12;"
        );
        nameField.focusedProperty().addListener((obs, old, focused) ->
            nameField.setStyle(nameField.getStyle() +
                (focused ? "-fx-border-color: #C8860A;" : "-fx-border-color: #3A2810;"))
        );

        sec.getChildren().addAll(lbl, nameField);
        return sec;
    }

    private VBox buildBgCard(PlayerBackground bg) {
        boolean isSelected = bg == selectedBg;

        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setStyle(
            "-fx-background-color: " + (isSelected ? "#C8860A1A" : "#1A1008") + ";" +
            "-fx-border-color: " + (isSelected ? "#FFB830" : "#3A2810") + ";" +
            "-fx-border-width: " + (isSelected ? "1 1 1 3" : "1") + ";" +
            (isSelected ? "-fx-effect: dropshadow(gaussian, #C8860A, 10, 0.3, 0, 0);" : "")
        );

        // Header: ikon + nama
        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        String icon = switch (bg) {
            case STREET_BRAWLER  -> "🥊";
            case NETRUNNER       -> "💻";
            case VETERAN_SOLDIER -> "🪖";
            case ENERGY_ADEPT    -> "⚡";
            case GHOST_OPERATIVE -> "👻";
            case TECHWRIGHT      -> "🔧";
        };

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px;");

        Label nameLbl = new Label(bg.name.toUpperCase());
        nameLbl.setStyle(
            "-fx-text-fill: " + (isSelected ? "#FFB830" : "#A09070") + ";" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

        if (isSelected) {
            Label sel = new Label("✦");
            sel.setStyle("-fx-text-fill: #FFB830; -fx-font-size: 10px;");
            HBox.setHgrow(nameLbl, Priority.ALWAYS);
            nameRow.getChildren().addAll(iconLbl, nameLbl, sel);
        } else {
            nameRow.getChildren().addAll(iconLbl, nameLbl);
        }

        // Lore text
        Label lore = new Label(bg.lore);
        lore.setWrapText(true);
        lore.setStyle("-fx-text-fill: #6A5840; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");

        // Starter skills
        String skills = switch (bg) {
            case STREET_BRAWLER  -> "Pukulan Harimau + Tebasan";
            case NETRUNNER       -> "Santet Digital + Upload Santet";
            case VETERAN_SOLDIER -> "Tameng Baja + Gempa Bumi";
            case ENERGY_ADEPT    -> "Serap Tenaga + Racun Semesta";
            case GHOST_OPERATIVE -> "Panah Bayangan + Langkah Gaib";
            case TECHWRIGHT      -> "Ledakan Petir + Rajah Pelindung";
        };
        Label skillLbl = new Label("⚔ " + skills);
        skillLbl.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");

        card.getChildren().addAll(nameRow, lore, skillLbl);

        // Click handler — rebuild view dengan bg baru dipilih
        card.setOnMouseClicked(e -> {
            selectedBg = bg;
            router.showCharacterCreateWith(selectedBg, nameField.getText());
        });

        return card;
    }

    private HBox buildBottomAction() {
        HBox bottom = new HBox(12);
        bottom.setPadding(new Insets(12, 16, 14, 16));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setStyle("-fx-background-color: #0F0A06;" +
                        "-fx-border-color: #3A2810; -fx-border-width: 1 0 0 0;");

        // Asal usul yang dipilih
        Label chosen = new Label("Asal Usul: " + selectedBg.name);
        chosen.setStyle("-fx-text-fill: #C8860A; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        HBox.setHgrow(chosen, Priority.ALWAYS);

        Button start = new Button("MULAI PETUALANGAN  ▶");
        start.setStyle(
            "-fx-background-color: #C8860A22;" +
            "-fx-border-color: #FFB830;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #FFB830;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 8, 0.3, 0, 0);"
        );
        start.setOnMouseEntered(ev -> start.setStyle(
            "-fx-background-color: #C8860A44; -fx-border-color: #FFB830; -fx-border-width: 1;" +
            "-fx-text-fill: #FFB830; -fx-font-family: 'Courier New'; -fx-font-size: 13px;" +
            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #FFB830, 16, 0.5, 0, 0);"));
        start.setOnMouseExited(ev -> start.setStyle(
            "-fx-background-color: #C8860A22; -fx-border-color: #FFB830; -fx-border-width: 1;" +
            "-fx-text-fill: #FFB830; -fx-font-family: 'Courier New'; -fx-font-size: 13px;" +
            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, #C8860A, 8, 0.3, 0, 0);"));
        start.setOnAction(e -> {
            String name = nameField != null ? nameField.getText().trim() : "";
            if (name.isEmpty()) name = selectedBg.name;
            engine.createCharacter(name, selectedBg);
            router.showHub();
        });

        bottom.getChildren().addAll(chosen, start);
        return bottom;
    }

    /** Dipanggil saat bg card diklik — rebuild dengan state terbaru */
    public void setSelectedBg(PlayerBackground bg) { this.selectedBg = bg; }
    public void setNameText(String name) {
        if (nameField != null) nameField.setText(name);
    }
}
