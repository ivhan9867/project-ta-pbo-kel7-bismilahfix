package arclightcity.ui.lore;

import arclightcity.ui.ArclightApp;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

/**
 * CutsceneView — Persona 5 style, maksimal dalam JavaFX.
 *
 *  ┌───────────────────────────────────────────────────────────┐
 *  │  [Background fullscreen, fade antar scene]                │
 *  │                                         [PORTRAIT BESAR]  │
 *  │                                         [kanan, bottom]   │
 *  ├───────────────────────────────────────────────────────────┤
 *  │ ▌ KI AGENG   [nama tag berwarna per karakter]             │
 *  │  "Kamu dari dimensi lain.                                 │
 *  │   Kukira masih bertahun-tahun lagi..."           LANJUT ▶ │
 *  ├───────────────────────────────────────────────────────────┤
 *  │  [Choices panel muncul di sini jika ada pilihan]          │
 *  └───────────────────────────────────────────────────────────┘
 */
public class CutsceneView extends StackPane {

    // ── Dimensi ───────────────────────────────────────────────
    private static final double W      = ArclightApp.SCREEN_WIDTH;   // 1280
    private static final double H      = ArclightApp.SCREEN_HEIGHT;  // 720
    private static final double DLG_H  = 190.0;   // tinggi dialog box
    private static final double PORT_H = H * 0.80; // tinggi portrait
    private static final double TYPE_MS = 22.0;    // ms per karakter

    // ── Warna per karakter (Persona 5 style signature colors) ─
    private static final Map<String, String> CHAR_COLORS = new HashMap<>();
    static {
        CHAR_COLORS.put("ASUNA",           "#C8860A");
        CHAR_COLORS.put("KI AGENG",        "#7A3D9A");
        CHAR_COLORS.put("GATOT KACA",      "#2A6A9A");
        CHAR_COLORS.put("SRIKANDI",        "#2A7A3A");
        CHAR_COLORS.put("NYAI RORO",       "#1A7A7A");
        CHAR_COLORS.put("BIMA",            "#9A2A2A");
        CHAR_COLORS.put("RANGGA",          "#4A4A5A");
        CHAR_COLORS.put("DEWI SRI",        "#5A7A1A");
        CHAR_COLORS.put("BATARA KALA",     "#7A6A1A");
        CHAR_COLORS.put("NYI RORO KIDUL",  "#1A6A7A");
        CHAR_COLORS.put("RANGDA AGUNG",    "#7A1A5A");
        CHAR_COLORS.put("GARUDA MAHAGURU", "#9A6A1A");
        CHAR_COLORS.put("SEMAR PAMUNGKAS", "#7A7A3A");
        CHAR_COLORS.put("THERESA",         "#2A4A7A");
        CHAR_COLORS.put("PAK WARDI",       "#7A5A1A");
        CHAR_COLORS.put("RAKSASA KALA",    "#5A2A1A");
    }

    // ── Komponen visual ───────────────────────────────────────
    private final ImageView bgView     = new ImageView();
    private final ImageView portrait   = new ImageView();
    private final Rectangle fadeBlack  = new Rectangle(W, H, Color.BLACK);
    private final MediaView mediaView  = new MediaView();

    // Dialog box komponen
    private final Rectangle dlgBg;
    private final Rectangle dlgAccent; // colored left border
    private final Label     speakerTag = new Label();
    private final Label     dialogTxt  = new Label();
    private final Label     nextBtn    = new Label("▶  LANJUT");
    private final StackPane dialogPanel;

    // Choice panel
    private final VBox choicePanel = new VBox(0);

    // ── State ─────────────────────────────────────────────────
    private final List<DialogBeat> beats;
    private int            idx     = 0;
    private boolean        typing  = false;
    private boolean        blocked = false;
    private Timeline       typer;
    private MediaPlayer    mp;
    private final Runnable onDone;
    private String         lastBg  = null;
    private String         curColor = "#C8860A";

    // ══════════════════════════════════════════════════════════
    public CutsceneView(List<DialogBeat> beats, Runnable onDone) {
        this.beats  = beats;
        this.onDone = onDone;
        setPrefSize(W, H); setMinSize(W, H); setMaxSize(W, H);

        // ── Background ────────────────────────────────────────
        bgView.setFitWidth(W); bgView.setFitHeight(H);
        bgView.setPreserveRatio(false); bgView.setOpacity(0);

        // ── Video layer ───────────────────────────────────────
        mediaView.setFitWidth(W); mediaView.setFitHeight(H);
        mediaView.setPreserveRatio(false); mediaView.setVisible(false);

        // ── Portrait (kanan, besar, dengan glow) ─────────────
        portrait.setFitHeight(PORT_H);
        portrait.setPreserveRatio(true);
        portrait.setOpacity(0);
        portrait.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.85)));
        StackPane portSlot = new StackPane(portrait);
        portSlot.setAlignment(Pos.BOTTOM_RIGHT);
        portSlot.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        portSlot.setPadding(new Insets(0, 30, DLG_H + 6, 0));
        portSlot.setPickOnBounds(false);

        // ── Dialog box ────────────────────────────────────────
        // Background panel gelap
        dlgBg = new Rectangle(W, DLG_H, Color.rgb(3, 2, 1, 0.92));
        // Garis aksen atas (tipis, berwarna per karakter)
        Rectangle dlgTopLine = new Rectangle(W, 2, Color.web("#C8860A", 0.4));
        // Garis aksen kiri (tebal, sangat berwarna — P5 style)
        dlgAccent = new Rectangle(5, DLG_H, Color.web("#C8860A"));

        // Speaker tag — nama karakter dengan warna
        speakerTag.setStyle(
            "-fx-text-fill: #FFD060;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-letter-spacing: 2;");
        speakerTag.setVisible(false);

        // Dialog text — putih bersih, readable
        dialogTxt.setStyle(
            "-fx-text-fill: #F5F0E8;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 13px;" +
            "-fx-line-spacing: 5;" +
            "-fx-wrap-text: true;");
        dialogTxt.setWrapText(true);
        dialogTxt.setMaxWidth(W * 0.66);
        dialogTxt.setEffect(new DropShadow(4, Color.BLACK));

        // LANJUT button
        nextBtn.setStyle(
            "-fx-text-fill: #C8860A;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;");
        nextBtn.setVisible(false);
        nextBtn.setOnMouseClicked(e -> advance());
        nextBtn.setOnMouseEntered(e -> nextBtn.setStyle(
            "-fx-text-fill: #FFD060; -fx-font-family:'Courier New';" +
            "-fx-font-size:11px; -fx-font-weight:bold; -fx-cursor:hand;"));
        nextBtn.setOnMouseExited(e -> nextBtn.setStyle(
            "-fx-text-fill: #C8860A; -fx-font-family:'Courier New';" +
            "-fx-font-size:11px; -fx-font-weight:bold; -fx-cursor:hand;"));

        // Blink animasi
        FadeTransition blink = new FadeTransition(Duration.millis(550), nextBtn);
        blink.setFromValue(1.0); blink.setToValue(0.1);
        blink.setCycleCount(Animation.INDEFINITE); blink.setAutoReverse(true); blink.play();

        // Layout dalam dialog box
        VBox textColumn = new VBox(5, speakerTag, dialogTxt);
        textColumn.setPadding(new Insets(18, 16, 16, 18));
        HBox.setHgrow(textColumn, Priority.ALWAYS);

        StackPane nextSlot = new StackPane(nextBtn);
        nextSlot.setAlignment(Pos.BOTTOM_RIGHT);
        nextSlot.setPadding(new Insets(0, 20, 16, 8));
        nextSlot.setMinWidth(90);

        HBox contentRow = new HBox(textColumn, nextSlot);
        contentRow.setAlignment(Pos.CENTER_LEFT);

        // Susun dialog panel dengan layers
        dialogPanel = new StackPane();
        dialogPanel.setAlignment(Pos.TOP_LEFT);
        dialogPanel.setPrefWidth(W); dialogPanel.setMaxWidth(W);
        dialogPanel.setPrefHeight(DLG_H); dialogPanel.setMinHeight(DLG_H);

        // Background rect
        StackPane.setAlignment(dlgBg, Pos.TOP_LEFT);
        // Top line
        StackPane.setAlignment(dlgTopLine, Pos.TOP_LEFT);
        // Left accent bar
        StackPane.setAlignment(dlgAccent, Pos.CENTER_LEFT);
        // Content (padded untuk skip accent bar)
        HBox paddedContent = new HBox(contentRow);
        paddedContent.setPadding(new Insets(0, 0, 0, 7)); // skip accent bar
        StackPane.setAlignment(paddedContent, Pos.CENTER_LEFT);

        dialogPanel.getChildren().addAll(dlgBg, paddedContent, dlgTopLine, dlgAccent);
        dialogPanel.setVisible(false);

        // ── Choice panel (di atas dialog) ─────────────────────
        choicePanel.setAlignment(Pos.BOTTOM_LEFT);
        choicePanel.setPadding(new Insets(0));
        choicePanel.setVisible(false);

        VBox bottomStack = new VBox(choicePanel, dialogPanel);
        bottomStack.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setAlignment(bottomStack, Pos.BOTTOM_LEFT);

        // ── SKIP button (kanan atas) ───────────────────────────
        Label skipLbl = new Label("SKIP  ✕");
        skipLbl.setStyle(
            "-fx-text-fill:rgba(255,255,255,0.22);" +
            "-fx-font-family:'Courier New'; -fx-font-size:10px;" +
            "-fx-cursor:hand; -fx-padding:10 14;");
        skipLbl.setOnMouseEntered(e -> skipLbl.setStyle(
            "-fx-text-fill:rgba(255,255,255,0.60);" +
            "-fx-font-family:'Courier New'; -fx-font-size:10px;" +
            "-fx-cursor:hand; -fx-padding:10 14;"));
        skipLbl.setOnMouseExited(e -> skipLbl.setStyle(
            "-fx-text-fill:rgba(255,255,255,0.22);" +
            "-fx-font-family:'Courier New'; -fx-font-size:10px;" +
            "-fx-cursor:hand; -fx-padding:10 14;"));
        skipLbl.setOnMouseClicked(e -> finish());
        StackPane.setAlignment(skipLbl, Pos.TOP_RIGHT);

        // ── Fade overlay ──────────────────────────────────────
        fadeBlack.setOpacity(1.0);

        // ── Susun semua layer ─────────────────────────────────
        getChildren().addAll(bgView, mediaView, portSlot,
                             bottomStack, skipLbl, fadeBlack);

        // ── Input ─────────────────────────────────────────────
        setOnMouseClicked(e -> {
            if (e.getTarget() == skipLbl || e.getTarget() == nextBtn) return;
            advance();
        });
        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) advance();
            if (e.getCode() == KeyCode.ESCAPE) finish();
        });
    }

    // ══════════════════════════════════════════════════════════
    // PUBLIC
    // ══════════════════════════════════════════════════════════
    public void start() {
        requestFocus();
        FadeTransition ft = new FadeTransition(Duration.millis(500), fadeBlack);
        ft.setToValue(0);
        ft.setOnFinished(e -> applyBeat(beats.get(0)));
        ft.play();
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════
    private void advance() {
        if (blocked) return;

        if (typing) {
            // Skip typewriter → tampilkan teks penuh seketika
            if (typer != null) { typer.stop(); typer = null; }
            typing = false;
            DialogBeat b = beats.get(idx);
            if (b.text() != null) dialogTxt.setText(b.text());
            if (b.choices() != null && b.choices().length > 0) showChoices(b.choices());
            else nextBtn.setVisible(true);
            return;
        }

        idx++;
        if (idx >= beats.size()) { finish(); return; }

        blocked = true;
        // Fade hitam antara beat
        FadeTransition out = new FadeTransition(Duration.millis(180), fadeBlack);
        out.setToValue(1.0);
        out.setOnFinished(e -> {
            applyBeat(beats.get(idx));
            FadeTransition in = new FadeTransition(Duration.millis(180), fadeBlack);
            in.setToValue(0.0);
            in.setOnFinished(ev -> blocked = false);
            in.play();
        });
        out.play();
    }

    // ══════════════════════════════════════════════════════════
    // RENDER BEAT
    // ══════════════════════════════════════════════════════════
    private void applyBeat(DialogBeat beat) {
        stopMedia();
        choicePanel.setVisible(false);
        choicePanel.getChildren().clear();

        // ── VIDEO MODE ────────────────────────────────────────
        if (beat.videoPath() != null) {
            blocked = true;
            dialogPanel.setVisible(false);
            fadePortrait(false);
            playVideo("/assets/" + beat.videoPath());
            return;
        }

        // ── IMAGE / DIALOG MODE ───────────────────────────────
        mediaView.setVisible(false);

        // Background
        if (beat.bg() != null && !beat.bg().equals(lastBg)) {
            Image img = loadImg("/assets/" + beat.bg());
            if (img != null) {
                bgView.setImage(img);
                if (beat.fadeIn()) {
                    bgView.setOpacity(0);
                    fade(bgView, 0, 1, 400);
                } else {
                    bgView.setOpacity(1);
                }
            }
            lastBg = beat.bg();
        }

        // Portrait — kanan (portretRight atau fallback ke Left)
        String porPath = beat.portraitRight() != null ? beat.portraitRight()
                       : beat.portraitLeft();
        if (porPath != null) {
            Image img = loadImg("/assets/" + porPath);
            if (img != null) {
                boolean changed = !img.equals(portrait.getImage());
                portrait.setImage(img);
                if (portrait.getOpacity() < 0.3 || changed) {
                    portrait.setOpacity(0);
                    fade(portrait, 0, 1, 300);
                }
            }
            // Update portrait glow warna karakter
            if (beat.speaker() != null) {
                String col = CHAR_COLORS.getOrDefault(beat.speaker().toUpperCase(), "#C8860A");
                portrait.setEffect(new DropShadow(25, Color.web(col + "80")));
            }
        } else {
            fadePortrait(false);
        }

        // ── FULL IMAGE MODE (tidak ada teks) ──────────────────
        if (beat.text() == null && beat.speaker() == null) {
            dialogPanel.setVisible(false);
            PauseTransition pt = new PauseTransition(Duration.millis(2600));
            pt.setOnFinished(e -> advance());
            pt.play();
            return;
        }

        // ── DIALOG MODE ───────────────────────────────────────
        dialogPanel.setVisible(true);
        nextBtn.setVisible(false);

        // Update warna per karakter
        String col = beat.speaker() != null
            ? CHAR_COLORS.getOrDefault(beat.speaker().toUpperCase(), "#C8860A")
            : "#C8860A";
        curColor = col;
        updateCharColor(col);

        // Speaker name
        if (beat.speaker() != null) {
            speakerTag.setText(beat.speaker());
            speakerTag.setStyle(
                "-fx-text-fill: " + col + ";" +
                "-fx-font-family: 'Courier New'; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-letter-spacing: 2;");
            speakerTag.setVisible(true);
        } else {
            speakerTag.setVisible(false);
        }

        if (beat.text() != null) {
            startTypewriter(beat.text(), beat.choices());
        }
    }

    // Perbarui warna aksen dialog box
    private void updateCharColor(String hex) {
        try {
            Color c = Color.web(hex);
            dlgAccent.setFill(c);
            // Top line tipis dengan warna karakter
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════════════
    // TYPEWRITER
    // ══════════════════════════════════════════════════════════
    private void startTypewriter(String full, String[] choices) {
        typing = true;
        dialogTxt.setText("");
        if (typer != null) typer.stop();
        typer = new Timeline();
        for (int i = 1; i <= full.length(); i++) {
            final String s = full.substring(0, i);
            typer.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * TYPE_MS), e -> dialogTxt.setText(s))
            );
        }
        typer.setOnFinished(e -> {
            typing = false;
            if (choices != null && choices.length > 0) showChoices(choices);
            else nextBtn.setVisible(true);
        });
        typer.play();
    }

    // ══════════════════════════════════════════════════════════
    // CHOICES — Persona 5 style
    // ══════════════════════════════════════════════════════════
    private void showChoices(String[] opts) {
        choicePanel.getChildren().clear();

        // Header separator
        Label hdr = new Label("  — Pilihanmu —");
        hdr.setStyle(
            "-fx-text-fill: rgba(200,134,10,0.40);" +
            "-fx-font-family:'Courier New'; -fx-font-size:9px;" +
            "-fx-padding: 6 24 4 12;");
        choicePanel.getChildren().add(hdr);

        for (int i = 0; i < opts.length; i++) {
            final String opt = opts[i];
            final int    num = i + 1;

            StackPane optBox = new StackPane();
            optBox.setMinWidth(W * 0.50);
            optBox.setMaxWidth(W * 0.50);
            optBox.setPrefHeight(38);
            optBox.setAlignment(Pos.CENTER_LEFT);

            Rectangle optBg = new Rectangle(W * 0.50, 38, Color.rgb(3, 2, 1, 0.88));
            Rectangle optLeft = new Rectangle(4, 38, Color.web(curColor, 0.0)); // dim awal

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(0, 16, 0, 14));

            Label numLbl = new Label(num + ".");
            numLbl.setStyle(
                "-fx-text-fill: rgba(200,134,10,0.45);" +
                "-fx-font-family:'Courier New'; -fx-font-size:11px;" +
                "-fx-font-weight:bold; -fx-min-width:16;");

            Label optLbl = new Label(opt);
            optLbl.setStyle(
                "-fx-text-fill: rgba(240,235,225,0.75);" +
                "-fx-font-family:'Courier New'; -fx-font-size:12px;" +
                "-fx-wrap-text: true;");
            optLbl.setMaxWidth(W * 0.44);

            row.getChildren().addAll(numLbl, optLbl);
            optBox.getChildren().addAll(optBg, row, optLeft);
            StackPane.setAlignment(optLeft, Pos.CENTER_LEFT);

            // Separator bawah
            Rectangle sep = new Rectangle(W * 0.50, 1, Color.rgb(200, 134, 10, 0.08));

            VBox cellWrap = new VBox(optBox, sep);

            // Hover effect
            optBox.setOnMouseEntered(e -> {
                optBg.setFill(Color.web(curColor + "22"));
                optLeft.setFill(Color.web(curColor));
                numLbl.setStyle(
                    "-fx-text-fill: " + curColor + ";" +
                    "-fx-font-family:'Courier New'; -fx-font-size:11px;" +
                    "-fx-font-weight:bold; -fx-min-width:16;");
                optLbl.setStyle(
                    "-fx-text-fill: #FFD060; -fx-font-family:'Courier New';" +
                    "-fx-font-size:12px; -fx-wrap-text:true;");
                optBox.setScaleX(1.01); optBox.setScaleY(1.01);
            });
            optBox.setOnMouseExited(e -> {
                optBg.setFill(Color.rgb(3, 2, 1, 0.88));
                optLeft.setFill(Color.web(curColor, 0.0));
                numLbl.setStyle(
                    "-fx-text-fill:rgba(200,134,10,0.45);" +
                    "-fx-font-family:'Courier New'; -fx-font-size:11px;" +
                    "-fx-font-weight:bold; -fx-min-width:16;");
                optLbl.setStyle(
                    "-fx-text-fill:rgba(240,235,225,0.75);" +
                    "-fx-font-family:'Courier New'; -fx-font-size:12px; -fx-wrap-text:true;");
                optBox.setScaleX(1.0); optBox.setScaleY(1.0);
            });
            optBox.setOnMouseClicked(e -> {
                // Pilihan dipilih → tampilkan sebagai response Asuna
                choicePanel.setVisible(false);
                dialogTxt.setText("\"" + opt + "\"");
                speakerTag.setText("ASUNA");
                String aCol = CHAR_COLORS.getOrDefault("ASUNA", "#C8860A");
                speakerTag.setStyle(
                    "-fx-text-fill:" + aCol + "; -fx-font-family:'Courier New';" +
                    "-fx-font-size:12px; -fx-font-weight:bold; -fx-letter-spacing:2;");
                speakerTag.setVisible(true);
                updateCharColor(aCol);
                nextBtn.setVisible(true);
            });
            optBox.setCursor(javafx.scene.Cursor.HAND);

            choicePanel.getChildren().add(cellWrap);
        }

        // Bottom padding
        choicePanel.getChildren().add(new Region() {{ setPrefHeight(4); }});

        choicePanel.setStyle("-fx-background-color: rgba(3,2,1,0.55);");
        choicePanel.setMaxWidth(W * 0.50);
        choicePanel.setVisible(true);
        nextBtn.setVisible(false);

        // Slide-in animation
        choicePanel.setTranslateY(30);
        choicePanel.setOpacity(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(250), choicePanel);
        slide.setToY(0); slide.play();
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), choicePanel);
        fadeIn.setToValue(1.0); fadeIn.play();
    }

    // ══════════════════════════════════════════════════════════
    // VIDEO
    // ══════════════════════════════════════════════════════════
    private void playVideo(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("[CutsceneView] Video not found: " + path);
            blocked = false; advance(); return;
        }
        try {
            mp = new MediaPlayer(new Media(url.toString()));
            mediaView.setMediaPlayer(mp);
            mediaView.setVisible(true);
            bgView.setOpacity(0);
            mp.setOnEndOfMedia(() -> Platform.runLater(() -> {
                mediaView.setVisible(false);
                try { mp.stop(); mp.dispose(); } catch (Exception ignored) {}
                mp = null;
                blocked = false;
                advance();
            }));
            mp.setOnError(() -> {
                System.err.println("[CutsceneView] Video error: " + mp.getError());
                Platform.runLater(() -> { mediaView.setVisible(false); blocked = false; advance(); });
            });
            mp.play();
        } catch (Exception ex) {
            System.err.println("[CutsceneView] Video exception: " + ex.getMessage());
            mediaView.setVisible(false); blocked = false; advance();
        }
    }

    // ══════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════
    private void fade(javafx.scene.Node node, double from, double to, double ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(from); ft.setToValue(to); ft.play();
    }

    private void fadePortrait(boolean show) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), portrait);
        ft.setToValue(show ? 1.0 : 0.0); ft.play();
    }

    private Image loadImg(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) return new Image(url.toExternalForm());
        } catch (Exception ignored) {}
        System.err.println("[CutsceneView] Missing asset: " + path);
        return null;
    }

    private void stopMedia() {
        if (mp != null) {
            try { mp.stop(); mp.dispose(); } catch (Exception ignored) {}
            mp = null;
        }
        if (typer != null) { typer.stop(); typer = null; }
        typing = false;
    }

    // ══════════════════════════════════════════════════════════
    // FINISH
    // ══════════════════════════════════════════════════════════
    public void finish() {
        stopMedia();
        blocked = true;
        FadeTransition out = new FadeTransition(Duration.millis(400), fadeBlack);
        out.setToValue(1.0);
        out.setOnFinished(e -> { if (onDone != null) onDone.run(); });
        out.play();
    }
}
