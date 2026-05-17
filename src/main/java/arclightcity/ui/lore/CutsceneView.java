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
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

/**
 * CutsceneView — visual novel overlay, Persona 5 style.
 *
 * Layout (root = StackPane fullscreen):
 *   [bgView]          – background fullscreen
 *   [mediaView]       – video fullscreen
 *   [titleCard]       – centered title text
 *   [inner AnchorPane]
 *     portrait LEFT   – anchored bottom-left, above dialog
 *     dialogPanel     – anchored BOTTOM, full width (GUARANTEED)
 *     choicePanel     – anchored just above dialog, left side
 *   [skipLbl]         – top-right
 *   [fadeBlack]       – transition overlay
 */
public class CutsceneView extends StackPane {

    private static final double W      = ArclightApp.SCREEN_WIDTH;
    private static final double H      = ArclightApp.SCREEN_HEIGHT;
    private static final double DLG_H  = 175.0;
    private static final double PORT_H = H * 0.55;
    private static final double TYPE_MS = 20.0;

    private static final Map<String, String> CHAR_COLORS = new HashMap<>();
    static {
        CHAR_COLORS.put("ASUNA","#C8860A");           CHAR_COLORS.put("KI AGENG","#7A3D9A");
        CHAR_COLORS.put("GATOT KACA","#2A6A9A");      CHAR_COLORS.put("SRIKANDI","#2A7A3A");
        CHAR_COLORS.put("NYAI RORO","#1A7A7A");        CHAR_COLORS.put("BIMA","#9A2A2A");
        CHAR_COLORS.put("RANGGA","#4A4A5A");           CHAR_COLORS.put("DEWI SRI","#5A7A1A");
        CHAR_COLORS.put("BATARA KALA","#7A6A1A");      CHAR_COLORS.put("NYI RORO KIDUL","#1A6A7A");
        CHAR_COLORS.put("RANGDA AGUNG","#7A1A5A");     CHAR_COLORS.put("GARUDA MAHAGURU","#9A6A1A");
        CHAR_COLORS.put("SEMAR PAMUNGKAS","#7A7A3A");  CHAR_COLORS.put("THERESA","#2A4A7A");
        CHAR_COLORS.put("PAK WARDI","#7A5A1A");        CHAR_COLORS.put("RAKSASA KALA","#5A2A1A");
    }

    // Layers
    private final ImageView bgView     = new ImageView();
    private final ImageView portrait   = new ImageView();
    private final Rectangle fadeBlack  = new Rectangle(W, H, Color.BLACK);
    private final MediaView mediaView  = new MediaView();

    // Title card
    private final VBox   titleCard = new VBox(8);
    private final Label  titleLine1 = new Label();
    private final Label  titleLine2 = new Label();

    // Dialog panel — built as HBox inside AnchorPane-anchored container
    private final Rectangle dlgBg     = new Rectangle(W, DLG_H, Color.rgb(3,2,1,0.93));
    private final Rectangle dlgAccent = new Rectangle(5, DLG_H, Color.web("#C8860A"));
    private final Label  speakerTag   = new Label();
    private final Label  dialogTxt    = new Label();
    private final Label  nextBtn      = new Label("▶  LANJUT");
    private final Pane   dialogPanel;  // direct Pane for absolute positioning

    // Choice panel
    private final VBox   choiceBox = new VBox(0);
    private final Pane   choiceContainer = new Pane();

    // State
    private final List<DialogBeat> beats;
    private int         idx     = 0;
    private boolean     typing  = false;
    private boolean     blocked = false;
    private Timeline    typer;
    private MediaPlayer mp;
    private final Runnable onDone;
    private String lastBg   = null;
    private String curColor = "#C8860A";

    public CutsceneView(List<DialogBeat> beats, Runnable onDone) {
        this.beats  = beats;
        this.onDone = onDone;
        setPrefSize(W, H); setMinSize(W, H); setMaxSize(W, H);

        // ── Background ────────────────────────────────────────
        bgView.setFitWidth(W); bgView.setFitHeight(H);
        bgView.setPreserveRatio(false); bgView.setOpacity(0);

        // ── Video ─────────────────────────────────────────────
        mediaView.setFitWidth(W); mediaView.setFitHeight(H);
        mediaView.setPreserveRatio(false); mediaView.setVisible(false);

        // ── Title Card ────────────────────────────────────────
        titleLine1.setStyle("-fx-text-fill:#F0E0C0; -fx-font-family:'Courier New';" +
            "-fx-font-size:28px; -fx-font-weight:bold;");
        titleLine2.setStyle("-fx-text-fill:rgba(200,150,80,0.70); -fx-font-family:'Courier New';" +
            "-fx-font-size:16px;");
        titleCard.setAlignment(Pos.CENTER);
        titleCard.getChildren().addAll(titleLine1, titleLine2);
        titleCard.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        titleCard.setVisible(false);

        // ── Dialog Panel (Pane, absolute positioned via AnchorPane) ──
        speakerTag.setStyle("-fx-text-fill:#FFD060; -fx-font-family:'Courier New';" +
            "-fx-font-size:11px; -fx-font-weight:bold; -fx-letter-spacing:3;");
        speakerTag.setVisible(false);

        dialogTxt.setStyle("-fx-text-fill:#F5F0E8; -fx-font-family:'Courier New';" +
            "-fx-font-size:15px; -fx-wrap-text:true; -fx-line-spacing:5;");
        dialogTxt.setWrapText(true);
        dialogTxt.setPrefWidth(W * 0.62);
        dialogTxt.setMaxWidth(W * 0.62);
        dialogTxt.setEffect(new DropShadow(3, Color.BLACK));

        nextBtn.setStyle("-fx-text-fill:#C8860A; -fx-font-family:'Courier New';" +
            "-fx-font-size:12px; -fx-font-weight:bold; -fx-cursor:hand;");
        nextBtn.setLayoutX(W - 130); nextBtn.setLayoutY(DLG_H - 34);
        nextBtn.setVisible(false);
        nextBtn.setOnMouseClicked(e -> { e.consume(); advance(); });

        FadeTransition blink = new FadeTransition(Duration.millis(600), nextBtn);
        blink.setFromValue(1); blink.setToValue(0.12);
        blink.setCycleCount(Animation.INDEFINITE); blink.setAutoReverse(true); blink.play();

        // Speaker tag positioned at top-left of dialog panel
        speakerTag.setLayoutX(14); speakerTag.setLayoutY(14);

        // Dialog text below speaker
        dialogTxt.setLayoutX(14); dialogTxt.setLayoutY(38);

        // Top separator line
        Rectangle topLine = new Rectangle(W, 2, Color.web("#C8860A", 0.25));
        topLine.setLayoutX(0); topLine.setLayoutY(0);

        // Left accent bar
        dlgAccent.setLayoutX(0); dlgAccent.setLayoutY(0);

        dialogPanel = new Pane(dlgBg, topLine, dlgAccent, speakerTag, dialogTxt, nextBtn);
        dialogPanel.setPrefWidth(W);
        dialogPanel.setPrefHeight(DLG_H);
        dialogPanel.setVisible(false);

        // ── Choice Panel (above dialog) ────────────────────────
        choiceBox.setStyle("-fx-background-color:rgba(3,2,1,0.82);");
        choiceBox.setPrefWidth(W * 0.48);
        choiceBox.setMaxWidth(W * 0.48);
        choiceBox.setVisible(false);
        choiceContainer.getChildren().add(choiceBox);
        choiceContainer.setPrefWidth(W * 0.48);
        choiceContainer.setMaxWidth(W * 0.48);
        choiceContainer.setPrefHeight(200);
        choiceContainer.setVisible(false);

        // ── Portrait (LEFT side, above dialog) ────────────────
        portrait.setFitHeight(PORT_H);
        portrait.setPreserveRatio(true);
        portrait.setOpacity(0);

        // ── Inner AnchorPane — positions dialog/portrait precisely
        AnchorPane inner = new AnchorPane();
        inner.setPrefSize(W, H);
        inner.setPickOnBounds(false);
        inner.setBackground(Background.EMPTY);

        // Dialog: anchored to BOTTOM, full width
        inner.getChildren().add(dialogPanel);
        AnchorPane.setBottomAnchor(dialogPanel, 0.0);
        AnchorPane.setLeftAnchor(dialogPanel, 0.0);

        // Portrait: anchored bottom-left, just above dialog
        inner.getChildren().add(portrait);
        AnchorPane.setBottomAnchor(portrait, DLG_H + 2);
        AnchorPane.setLeftAnchor(portrait, 30.0);

        // Choices: anchored di KANAN, di atas dialog
        inner.getChildren().add(choiceContainer);
        AnchorPane.setBottomAnchor(choiceContainer, DLG_H);
        AnchorPane.setRightAnchor(choiceContainer, 0.0);

        // ── SKIP button ────────────────────────────────────────
        Label skipLbl = new Label("SKIP  ✕");
        skipLbl.setStyle("-fx-text-fill:rgba(255,255,255,0.25); -fx-font-family:'Courier New';" +
            "-fx-font-size:10px; -fx-cursor:hand; -fx-padding:10 14;");
        skipLbl.setOnMouseEntered(e -> skipLbl.setStyle(
            "-fx-text-fill:rgba(255,255,255,0.65); -fx-font-family:'Courier New';" +
            "-fx-font-size:10px; -fx-cursor:hand; -fx-padding:10 14;"));
        skipLbl.setOnMouseExited(e -> skipLbl.setStyle(
            "-fx-text-fill:rgba(255,255,255,0.25); -fx-font-family:'Courier New';" +
            "-fx-font-size:10px; -fx-cursor:hand; -fx-padding:10 14;"));
        skipLbl.setOnMouseClicked(e -> { e.consume(); finish(); });
        StackPane.setAlignment(skipLbl, Pos.TOP_RIGHT);

        fadeBlack.setOpacity(1.0);
        fadeBlack.setMouseTransparent(true); // KRITIS: jangan block klik ke choice cells

        getChildren().addAll(bgView, mediaView, titleCard, inner, skipLbl, fadeBlack);

        // ── Global click handler (only advance if not in choice) ──
        setOnMouseClicked(e -> {
            if (choiceBox.isVisible()) return; // jangan advance saat choices tampil
            advance();
        });
        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER) {
                if (!choiceBox.isVisible()) advance();
            }
            if (e.getCode() == KeyCode.ESCAPE) finish();
        });
    }

    public void start() {
        requestFocus();
        FadeTransition ft = new FadeTransition(Duration.millis(500), fadeBlack);
        ft.setToValue(0); ft.setOnFinished(e -> applyBeat(beats.get(0))); ft.play();
    }

    private void advance() {
        if (blocked) return;
        if (typing) {
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
        FadeTransition out = new FadeTransition(Duration.millis(170), fadeBlack);
        out.setToValue(1);
        out.setOnFinished(e -> {
            applyBeat(beats.get(idx));
            FadeTransition in = new FadeTransition(Duration.millis(170), fadeBlack);
            in.setToValue(0); in.setOnFinished(ev -> blocked = false); in.play();
        });
        out.play();
    }

    private void applyBeat(DialogBeat beat) {
        stopMedia();
        choiceBox.setVisible(false);
        choiceBox.getChildren().clear();
        choiceContainer.setVisible(false);
        titleCard.setVisible(false);

        // VIDEO
        if (beat.videoPath() != null) {
            blocked = true; dialogPanel.setVisible(false);
            fade(portrait, 0, 200); playVideo("/assets/" + beat.videoPath()); return;
        }
        mediaView.setVisible(false);

        // TITLE CARD
        if ("__TITLE__".equals(beat.speaker())) {
            dialogPanel.setVisible(false); fade(portrait, 0, 200);
            String[] lines = beat.text() != null ? beat.text().split("\\n") : new String[]{"",""};
            titleLine1.setText(lines.length > 0 ? lines[0] : "");
            titleLine2.setText(lines.length > 1 ? lines[1] : "");
            titleCard.setOpacity(0); titleCard.setVisible(true);
            FadeTransition tin = new FadeTransition(Duration.millis(600), titleCard);
            tin.setToValue(1); tin.play();
            PauseTransition pause = new PauseTransition(Duration.millis(2200));
            pause.setOnFinished(e -> advance()); pause.play();
            return;
        }

        // BACKGROUND
        if (beat.bg() != null && !beat.bg().equals(lastBg)) {
            Image img = loadImg("/assets/" + beat.bg());
            if (img != null) {
                bgView.setImage(img);
                if (beat.fadeIn()) { bgView.setOpacity(0); fade(bgView, 1, 400); }
                else bgView.setOpacity(1);
            }
            lastBg = beat.bg();
        }

        // PORTRAIT (LEFT side)
        String porPath = beat.portraitLeft() != null ? beat.portraitLeft()
                       : beat.portraitRight();
        if (porPath != null) {
            Image img = loadImg("/assets/" + porPath);
            if (img != null) {
                portrait.setImage(img);
                if (portrait.getOpacity() < 0.4) { portrait.setOpacity(0); fade(portrait, 1, 280); }
                String col = beat.speaker() != null
                    ? CHAR_COLORS.getOrDefault(beat.speaker().toUpperCase(), "#888888") : "#888888";
                portrait.setEffect(new DropShadow(22, Color.web(col + "70")));
            }
        } else {
            fade(portrait, 0, 180);
        }

        // FULL IMAGE (no text)
        if (beat.text() == null && beat.speaker() == null) {
            dialogPanel.setVisible(false);
            PauseTransition pt = new PauseTransition(Duration.millis(2500));
            pt.setOnFinished(e -> advance()); pt.play();
            return;
        }

        // DIALOG
        dialogPanel.setVisible(true); nextBtn.setVisible(false);
        String col = beat.speaker() != null
            ? CHAR_COLORS.getOrDefault(beat.speaker().toUpperCase(), "#C8860A") : "#C8860A";
        curColor = col;
        dlgAccent.setFill(Color.web(col));

        if (beat.speaker() != null) {
            speakerTag.setText(beat.speaker());
            speakerTag.setStyle("-fx-text-fill:" + col + "; -fx-font-family:'Courier New';" +
                "-fx-font-size:11px; -fx-font-weight:bold; -fx-letter-spacing:3;");
            speakerTag.setVisible(true);
            dialogTxt.setLayoutY(38);
        } else {
            speakerTag.setVisible(false);
            dialogTxt.setLayoutY(16);
        }

        if (beat.text() != null) startTypewriter(beat.text(), beat.choices());
    }

    private void startTypewriter(String full, String[] choices) {
        typing = true; dialogTxt.setText("");
        if (typer != null) typer.stop();
        typer = new Timeline();
        for (int i = 1; i <= full.length(); i++) {
            final String s = full.substring(0, i);
            typer.getKeyFrames().add(new KeyFrame(Duration.millis(i * TYPE_MS),
                e -> dialogTxt.setText(s)));
        }
        typer.setOnFinished(e -> {
            typing = false;
            if (choices != null && choices.length > 0) showChoices(choices);
            else nextBtn.setVisible(true);
        });
        typer.play();
    }

    private void showChoices(String[] opts) {
        choiceBox.getChildren().clear();
        Label hdr = new Label("  Pilihanmu");
        hdr.setStyle("-fx-text-fill:" + curColor + "88; -fx-font-family:'Courier New';" +
            "-fx-font-size:9px; -fx-padding:6 18 2 12;");
        choiceBox.getChildren().add(hdr);

        for (int i = 0; i < opts.length; i++) {
            final String opt = opts[i];
            final int num = i + 1;

            Rectangle optBg  = new Rectangle(W * 0.48, 40, Color.rgb(4,2,1,0.90));
            Rectangle optBar = new Rectangle(4, 40, Color.web(curColor, 0.0));

            Label numLbl = new Label(num + ".");
            numLbl.setStyle("-fx-text-fill:" + curColor + "66; -fx-font-family:'Courier New';" +
                "-fx-font-size:11px; -fx-font-weight:bold; -fx-min-width:18;");
            Label optLbl = new Label(opt);
            optLbl.setStyle("-fx-text-fill:rgba(240,235,225,0.82); -fx-font-family:'Courier New';" +
                "-fx-font-size:12px; -fx-wrap-text:true;");
            optLbl.setMaxWidth(W * 0.48);

            HBox row = new HBox(8, numLbl, optLbl);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(0, 14, 0, 12));

            StackPane cell = new StackPane(optBg, row, optBar);
            cell.setAlignment(Pos.CENTER_LEFT);
            StackPane.setAlignment(optBar, Pos.CENTER_LEFT);

            Rectangle sep = new Rectangle(W * 0.48, 1, Color.web(curColor, 0.08));
            VBox wrap = new VBox(cell, sep);

            cell.setOnMouseEntered(e -> {
                optBg.setFill(Color.web(curColor + "1A"));
                optBar.setFill(Color.web(curColor));
                optLbl.setStyle("-fx-text-fill:#FFD060; -fx-font-family:'Courier New';" +
                    "-fx-font-size:12px; -fx-wrap-text:true;");
                numLbl.setStyle("-fx-text-fill:" + curColor + "; -fx-font-family:'Courier New';" +
                    "-fx-font-size:11px; -fx-font-weight:bold; -fx-min-width:18;");
            });
            cell.setOnMouseExited(e -> {
                optBg.setFill(Color.rgb(4,2,1,0.90));
                optBar.setFill(Color.web(curColor, 0.0));
                optLbl.setStyle("-fx-text-fill:rgba(240,235,225,0.82); -fx-font-family:'Courier New';" +
                    "-fx-font-size:12px; -fx-wrap-text:true;");
                numLbl.setStyle("-fx-text-fill:" + curColor + "66; -fx-font-family:'Courier New';" +
                    "-fx-font-size:11px; -fx-font-weight:bold; -fx-min-width:18;");
            });
            // KUNCI: consume click agar parent tidak trigger advance()
            cell.setOnMouseClicked(e -> {
                e.consume(); // ← PENTING: hentikan event bubble ke parent StackPane
                choiceContainer.setVisible(false);
                choiceBox.setVisible(false);
                dialogTxt.setText("\"" + opt + "\"");
                speakerTag.setText("ASUNA");
                String aCol = CHAR_COLORS.getOrDefault("ASUNA", "#C8860A");
                speakerTag.setStyle("-fx-text-fill:" + aCol + "; -fx-font-family:'Courier New';" +
                    "-fx-font-size:11px; -fx-font-weight:bold; -fx-letter-spacing:3;");
                speakerTag.setVisible(true);
                dlgAccent.setFill(Color.web(aCol));
                nextBtn.setVisible(true);
            });
            cell.setCursor(javafx.scene.Cursor.HAND);
            choiceBox.getChildren().add(wrap);
        }

        choiceBox.setVisible(true);
        choiceContainer.setVisible(true);
        nextBtn.setVisible(false);

        // Slide-in animation
        choiceContainer.setTranslateY(20); choiceContainer.setOpacity(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(220), choiceContainer);
        slide.setToY(0); slide.play();
        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), choiceContainer);
        fadeIn.setToValue(1); fadeIn.play();
    }

    private void playVideo(String path) {
        URL url = getClass().getResource(path);
        if (url == null) { blocked = false; advance(); return; }
        try {
            mp = new MediaPlayer(new Media(url.toString()));
            mediaView.setMediaPlayer(mp); mediaView.setVisible(true); bgView.setOpacity(0);
            mp.setOnEndOfMedia(() -> Platform.runLater(() -> {
                mediaView.setVisible(false);
                try { mp.stop(); mp.dispose(); } catch(Exception i){}
                mp = null; blocked = false; advance();
            }));
            mp.setOnError(() -> Platform.runLater(() -> {
                mediaView.setVisible(false); blocked = false; advance();
            }));
            mp.play();
        } catch(Exception ex) { mediaView.setVisible(false); blocked = false; advance(); }
    }

    private void fade(javafx.scene.Node n, double to, double ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), n);
        ft.setToValue(to); ft.play();
    }

    private Image loadImg(String path) {
        try { URL u = getClass().getResource(path); if(u!=null) return new Image(u.toExternalForm()); }
        catch(Exception i) {}
        System.err.println("[CV] Missing: " + path); return null;
    }

    private void stopMedia() {
        if (mp != null) { try{mp.stop();mp.dispose();}catch(Exception i){} mp=null; }
        if (typer != null) { typer.stop(); typer=null; } typing=false;
    }

    public void finish() {
        stopMedia(); blocked = true;
        FadeTransition ft = new FadeTransition(Duration.millis(380), fadeBlack);
        ft.setToValue(1); ft.setOnFinished(e -> { if(onDone!=null) onDone.run(); }); ft.play();
    }
}
