package arclightcity.ui.view;

import arclightcity.ui.util.UIFactory;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import java.util.List;

/**
 * PersonaDialogBox — Kotak dialog bergaya Persona 5.
 *
 * Layout:
 * ┌──────────────────────────────────────────────────────┐
 * │ [Portrait ▓▓] ╔══════════════════════════════════╗  │
 * │ [  Nama   ▓▓] ║  Teks dialog muncul perlahan...  ║  │
 * │               ╚══════════════════════════════════╝  │
 * │                                        ▶ Lanjut     │
 * └──────────────────────────────────────────────────────┘
 *
 * Digunakan saat: boss encounter, fase transisi, lore cutscene.
 */
public class PersonaDialogBox extends VBox {

    public record DialogLine(
        String speakerName,
        String text,
        String color,        // warna identitas speaker
        boolean isEnemy      // true = portrait di kanan
    ) {}

    private final List<DialogLine> lines;
    private int currentLine = 0;
    private Runnable onFinished;

    private Label textLabel;
    private Label nameLabel;
    private Label portraitLabel;
    private Timeline typewriter;

    public PersonaDialogBox(List<DialogLine> lines, Runnable onFinished) {
        this.lines      = lines;
        this.onFinished = onFinished;

        setStyle("-fx-background-color: transparent;");
        setMaxWidth(Double.MAX_VALUE);
        build();
        showLine(0);
    }

    private void build() {
        // Main dialog HBox
        HBox dialogBox = new HBox(10);
        dialogBox.setPadding(new Insets(10, 14, 10, 14));
        dialogBox.setMaxWidth(Double.MAX_VALUE);
        dialogBox.setAlignment(Pos.CENTER_LEFT);
        dialogBox.setStyle(
            "-fx-background-color: #080604EE;" +
            "-fx-border-color: #3A2810; -fx-border-width: 1 0 0 0;"
        );

        // Portrait (kiri)
        VBox portraitBox = new VBox(4);
        portraitBox.setAlignment(Pos.CENTER);
        portraitBox.setMinWidth(56);
        portraitBox.setMaxWidth(56);

        StackPane avatarStack = new StackPane();
        Rectangle avatarBg = new Rectangle(48, 48);
        avatarBg.setFill(Color.web("#1A1008"));
        avatarBg.setArcWidth(4); avatarBg.setArcHeight(4);
        avatarBg.setStroke(Color.web("#C8860A")); avatarBg.setStrokeWidth(1.5);

        portraitLabel = new Label("⚔");
        portraitLabel.setStyle("-fx-font-size: 24px;");
        avatarStack.getChildren().addAll(avatarBg, portraitLabel);

        nameLabel = new Label("—");
        nameLabel.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 9px; -fx-font-weight: bold; -fx-alignment: CENTER;");
        nameLabel.setMaxWidth(56);
        nameLabel.setAlignment(Pos.CENTER);
        portraitBox.getChildren().addAll(avatarStack, nameLabel);

        // Text area (tengah/kanan)
        VBox textArea = new VBox(6);
        HBox.setHgrow(textArea, Priority.ALWAYS);

        textLabel = new Label("");
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(Double.MAX_VALUE);
        textLabel.setStyle(
            "-fx-text-fill: #EDE0C8;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 12px;" +
            "-fx-line-spacing: 4;"
        );

        // Advance button / indicator
        Label advBtn = new Label("▶ LANJUT");
        advBtn.setStyle("-fx-text-fill: #C8860A88; -fx-font-family: 'Courier New';" +
                        "-fx-font-size: 9px; -fx-alignment: CENTER_RIGHT;");
        advBtn.setMaxWidth(Double.MAX_VALUE);
        advBtn.setAlignment(Pos.CENTER_RIGHT);

        // Blink animation for advance indicator
        FadeTransition blink = new FadeTransition(Duration.millis(600), advBtn);
        blink.setFromValue(0.4); blink.setToValue(1.0);
        blink.setAutoReverse(true); blink.setCycleCount(Animation.INDEFINITE);
        blink.play();

        textArea.getChildren().addAll(textLabel, advBtn);
        dialogBox.getChildren().addAll(portraitBox, textArea);

        // Click anywhere to advance
        dialogBox.setOnMouseClicked(e -> advance());

        getChildren().add(dialogBox);

        // Slide in animation
        setTranslateY(60);
        setOpacity(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(350), this);
        slide.setToY(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), this);
        fadeIn.setToValue(1.0);
        ParallelTransition intro = new ParallelTransition(slide, fadeIn);
        intro.play();
    }

    private void showLine(int idx) {
        if (idx >= lines.size()) {
            finish();
            return;
        }
        DialogLine line = lines.get(idx);

        // Update portrait dan nama
        nameLabel.setText(line.speakerName().toUpperCase());
        nameLabel.setStyle("-fx-text-fill: " + line.color() + "; -fx-font-family: 'Courier New';" +
                           "-fx-font-size: 9px; -fx-font-weight: bold; -fx-alignment: CENTER;");
        portraitLabel.setText(line.isEnemy() ? "☠" : "⚔");

        // Typewriter effect
        if (typewriter != null) typewriter.stop();
        textLabel.setText("");
        String fullText = line.text();

        typewriter = new Timeline();
        for (int i = 0; i <= fullText.length(); i++) {
            final String partial = fullText.substring(0, i);
            typewriter.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * 28L), e -> textLabel.setText(partial))
            );
        }
        typewriter.play();
    }

    private void advance() {
        // Jika typewriter masih berjalan → skip ke teks penuh
        if (typewriter != null && typewriter.getStatus() == Animation.Status.RUNNING) {
            typewriter.stop();
            textLabel.setText(lines.get(currentLine).text());
            return;
        }
        // Lanjut ke baris berikutnya
        currentLine++;
        if (currentLine >= lines.size()) {
            finish();
        } else {
            showLine(currentLine);
        }
    }

    private void finish() {
        FadeTransition out = new FadeTransition(Duration.millis(300), this);
        out.setToValue(0);
        out.setOnFinished(e -> {
            if (getParent() instanceof Pane pane)
                pane.getChildren().remove(this);
            if (onFinished != null) onFinished.run();
        });
        out.play();
    }

    /** Helper: buat dialog box dan tambahkan ke parent */
    public static void show(Pane parent, List<DialogLine> lines, Runnable onFinished) {
        PersonaDialogBox box = new PersonaDialogBox(lines, onFinished);
        box.setMaxWidth(parent.getWidth() > 0 ? parent.getWidth() : 560);
        VBox.setVgrow(box, Priority.NEVER);

        // Tempatkan di bagian bawah parent
        if (parent instanceof BorderPane bp) {
            bp.setBottom(box);
        } else if (parent instanceof StackPane sp) {
            StackPane.setAlignment(box, Pos.BOTTOM_CENTER);
            sp.getChildren().add(box);
        } else {
            parent.getChildren().add(box);
        }
    }

    /** Factory: buat dialog lines untuk boss encounter */
    public static List<DialogLine> bossIntroLines(String bossName, String bossColor) {
        return List.of(
            new DialogLine("Asuna", "Jadi... kau yang menjaga lantai ini?", "#C8860A", false),
            new DialogLine(bossName, "Hm. Seorang manusia dari dunia lain. Menarik.", bossColor, true),
            new DialogLine(bossName, "Kalau kau ingin melewatiku... kau harus membunuhku lebih dulu.", bossColor, true),
            new DialogLine("Asuna", "Aku tidak punya waktu untuk basa-basi. Ayo!", "#C8860A", false)
        );
    }

    /** Factory: dialog lines untuk Theresa per fase */
    public static List<DialogLine> theresaPhaseLines(int phase) {
        return switch (phase) {
            case 1 -> List.of(
                new DialogLine("Theresa",
                    "Gadis lemah dari dunia lain... berani menantangku? Lucu.", "#7755BB", true),
                new DialogLine("Asuna",
                    "Aku bukan 'gadis lemah'. Aku Asuna — dan aku akan mengakhirimu!", "#C8860A", false)
            );
            case 3 -> List.of(
                new DialogLine("Theresa",
                    "Katana itu... TIDAK MUNGKIN! Serpihan Garuda! Darimana kau mendapatkannya!?",
                    "#CC3300", true),
                new DialogLine("Asuna",
                    "Dari para boss yang sudah aku kalahkan. Dan sekarang giliramu.", "#C8860A", false)
            );
            case 5 -> List.of(
                new DialogLine("Theresa",
                    "NUSANTARA AKAN MENJADI ES!!! ICE AGE — ULTIMA FREEZE!!!",
                    "#CC3300", true),
                new DialogLine("Asuna",
                    "Tidak akan terjadi. Selama aku masih berdiri!", "#C8860A", false)
            );
            default -> List.of(
                new DialogLine("Theresa",
                    "Kau... masih hidup?! Mustahil!", "#CC3300", true)
            );
        };
    }
}
