package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.entity.player.PlayerBackground;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;

/**
 * CharacterCreateView — screen pembuatan karakter.
 *
 * Layout:
 *   ┌─────────────────────────┐
 *   │  ← BACK    CREATE CHAR  │
 *   ├─────────────────────────┤
 *   │  ENTER NAME             │
 *   │  [________________]     │
 *   ├─────────────────────────┤
 *   │  SELECT BACKGROUND      │
 *   │  ┌──────────────────┐   │
 *   │  │ STREET BRAWLER   │   │  ← selected highlight
 *   │  │ desc...          │   │
 *   │  │ ATK+12 HP+30...  │   │
 *   │  └──────────────────┘   │
 *   │  (scroll list)          │
 *   ├─────────────────────────┤
 *   │  [ BEGIN ]              │
 *   └─────────────────────────┘
 */
public class CharacterCreateView {

    private final GameEngine  engine;
    private final SceneRouter router;

    private PlayerBackground selectedBg  = PlayerBackground.STREET_BRAWLER;
    private TextField        nameField;

    public CharacterCreateView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router = router;
    }

    public Parent build() {
        VBox root = UIFactory.screenRoot();

        // Header
        root.getChildren().add(UIFactory.headerBar("CREATE CHARACTER",
                () -> router.showMainMenu()));

        // Name input
        VBox nameSection = new VBox(8);
        nameSection.setPadding(new Insets(16, 16, 8, 16));
        nameSection.getChildren().add(UIFactory.sectionTitle("CALLSIGN"));

        nameField = new TextField();
        nameField.setPromptText("Enter your name...");
        nameField.setText("Runner");
        nameField.setStyle(
            "-fx-background-color: #0C1220;" +
            "-fx-border-color: #1C2E44;" +
            "-fx-border-width: 1;" +
            "-fx-text-fill: #E0E8F0;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10;" +
            "-fx-prompt-text-fill: #5A6A80;"
        );
        nameField.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                nameField.setStyle(nameField.getStyle().replace(
                        "-fx-border-color: #1C2E44;", "-fx-border-color: #00E5FF;"));
            } else {
                nameField.setStyle(nameField.getStyle().replace(
                        "-fx-border-color: #00E5FF;", "-fx-border-color: #1C2E44;"));
            }
        });
        nameSection.getChildren().add(nameField);
        root.getChildren().add(nameSection);

        // Divider
        root.getChildren().add(UIFactory.divider());

        // Background section
        Label bgTitle = UIFactory.sectionTitle("SELECT BACKGROUND");
        bgTitle.setPadding(new Insets(12, 16, 4, 16));
        root.getChildren().add(bgTitle);

        // Background list (scrollable)
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #050810; -fx-background: #050810; -fx-border-color: transparent;");

        VBox bgList = new VBox(8);
        bgList.setPadding(new Insets(8, 16, 8, 16));
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Render semua background
        for (PlayerBackground bg : PlayerBackground.values()) {
            VBox card = buildBgCard(bg, bgList);
            bgList.getChildren().add(card);
        }

        scroll.setContent(bgList);
        root.getChildren().add(scroll);

        // Divider + Begin button
        root.getChildren().add(UIFactory.divider());

        VBox bottomBar = new VBox(0);
        bottomBar.setPadding(new Insets(12, 16, 16, 16));
        Button begin = UIFactory.btnGold("[ BEGIN — " + selectedBg.name + " ]");
        begin.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                nameField.setStyle(nameField.getStyle().replace(
                        "-fx-border-color: #1C2E44;", "-fx-border-color: #FF1744;"));
                return;
            }
            engine.createCharacter(name, selectedBg);
            router.showHub();
        });
        bottomBar.getChildren().add(begin);
        root.getChildren().add(bottomBar);

        UIFactory.fadeIn(root, 400);
        return root;
    }

    private VBox buildBgCard(PlayerBackground bg, VBox bgList) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.setCursor(javafx.scene.Cursor.HAND);
        updateCardStyle(card, bg == selectedBg);

        // Name
        Label nameLabel = new Label(bg.name.toUpperCase());
        nameLabel.setStyle(
            "-fx-text-fill: " + (bg == selectedBg ? UIFactory.CYAN : UIFactory.TEXT) + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        // Lore
        Label loreLabel = new Label(bg.lore);
        loreLabel.setWrapText(true);
        loreLabel.setStyle(
            "-fx-text-fill: #8899AA;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 12px;"
        );

        // Bonus preview (hardcoded display dari background)
        Label bonusLabel = new Label(getBonusPreview(bg));
        bonusLabel.setWrapText(true);
        bonusLabel.setStyle(
            "-fx-text-fill: " + UIFactory.GREEN + ";" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 12px;"
        );

        card.getChildren().addAll(nameLabel, loreLabel, bonusLabel);

        // Click handler
        card.setOnMouseClicked(e -> {
            selectedBg = bg;
            // Reset semua card style
            bgList.getChildren().forEach(child -> {
                if (child instanceof VBox c) {
                    PlayerBackground cbg = (PlayerBackground) c.getUserData();
                    if (cbg != null) {
                        updateCardStyle(c, cbg == selectedBg);
                        Label nl = (Label) c.getChildren().get(0);
                        nl.setStyle(nl.getStyle().replace(
                                "-fx-text-fill: #E0E8F0;", "-fx-text-fill: " + UIFactory.CYAN + ";").replace(
                                "-fx-text-fill: " + UIFactory.CYAN + ";", "-fx-text-fill: " + (cbg == selectedBg ? UIFactory.CYAN : UIFactory.TEXT) + ";"));
                    }
                }
            });
            updateCardStyle(card, true);
            nameLabel.setStyle(nameLabel.getStyle().replace(
                    UIFactory.TEXT, UIFactory.CYAN));
        });
        card.setUserData(bg);

        return card;
    }

    private void updateCardStyle(VBox card, boolean selected) {
        if (selected) {
            card.setStyle(
                "-fx-background-color: #00E5FF11;" +
                "-fx-border-color: #00E5FF;" +
                "-fx-border-width: 1 1 1 3;"
            );
        } else {
            card.setStyle(
                "-fx-background-color: #0C1220;" +
                "-fx-border-color: #1C2E44;" +
                "-fx-border-width: 1;"
            );
        }
    }

    private String getBonusPreview(PlayerBackground bg) {
        return switch (bg) {
            case STREET_BRAWLER   -> "+ PHYS ATK +12  HP +30  SPEED +3  CRIT +5%  DMG MULT +5%";
            case NETRUNNER        -> "+ CYBER ATK +15  MAX MP +25  SKILL POWER +15%  CDR +10%  DMG MULT +8%";
            case VETERAN_SOLDIER  -> "+ PHYS DEF +15  HP +50  SHIELD +60  SHIELD REGEN +3  BLOCK +8%";
            case ENERGY_ADEPT     -> "+ ENERGY ATK +18  ENERGY DEF +12  SKILL POWER +20%  LIFESTEAL +5%  DMG MULT +10%";
            case GHOST_OPERATIVE  -> "+ EVASION +10%  SPEED +6  CRIT +10%  CRIT DMG +30%  PIERCE +15%  DMG MULT +12%";
            case TECHWRIGHT       -> "+ CYBER ATK +10  PIERCE +12%  SHIELD +30  SHIELD REGEN +5  SYNC RATE +15%";
        };
    }
}
