package arclightcity.ui.view;

import arclightcity.entity.mercenary.MercenaryType;
import arclightcity.ui.ArclightApp;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.*;

/**
 * MercChatPanel — panel chat mercenary di sisi kanan layar (340px).
 *
 * Layout:
 *   ┌─────────────────────────┐
 *   │ ◈ ✦ BISIK GUILDMATE            │  ← header
 *   ├─────────────────────────┤
 *   │ [chat scroll area]      │
 *   │ > KiraVoss:             │
 *   │   "Target locked."      │
 *   │                         │
 *   │ > TankRX9:              │
 *   │   "Standing by."        │
 *   │                         │
 *   │ [SYSTEM] Floor 1 entered│
 *   └─────────────────────────┘
 *
 * Chat message muncul dengan fade-in animation.
 * Auto-scroll ke bawah setelah pesan baru.
 */
public class MercChatPanel extends VBox {

    private static final double WIDTH = ArclightApp.CHAT_WIDTH;

    // Warna per merc untuk avatar dot
    private static final Map<MercenaryType, String> MERC_COLORS = Map.of(
        MercenaryType.KIRA_VOSS,     "#C8860A",   // gold — Srikandi
        MercenaryType.TANK_RX9,      "#8855CC",   // ungu — Gatot Kaca
        MercenaryType.SERA_MEND,     "#44AA44",   // hijau — Nyai Roro
        MercenaryType.VECTOR,        "#CC2200",   // merah — Rangga
        MercenaryType.MAGNUS_FORGE,  "#AA5500",   // coklat — Bima
        MercenaryType.ECHO_NULL,     "#2255AA",   // biru tua — Ki Ageng
        MercenaryType.LYRA_BLOOM,    "#FFB830"    // gold terang — Dewi Sri
    );

    private final VBox        messageContainer;
    private final ScrollPane  scrollPane;
    private final List<String> onlineNames = new ArrayList<>();

    // ── Constructor ──────────────────────────────────────────

    public MercChatPanel() {
        setPrefWidth(WIDTH);
        setMinWidth(WIDTH);
        setMaxWidth(WIDTH);
        setPrefHeight(ArclightApp.SCREEN_HEIGHT);
        setStyle(
            "-fx-background-color: #080604;" +
            "-fx-border-color: #2A1A08;" +
            "-fx-border-width: 0 0 0 1;"
        );

        // ── Header ────────────────────────────────────────────
        HBox header = new HBox(8);
        header.setPadding(new Insets(12, 14, 12, 14));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #0F0A06; -fx-border-color: #2A1A08; -fx-border-width: 0 0 1 0;");

        // Pulsing dot indicator
        Circle dot = new Circle(4, Color.web("#C8860A"));
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,       new KeyValue(dot.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(800), new KeyValue(dot.opacityProperty(), 0.3)),
            new KeyFrame(Duration.millis(1600),new KeyValue(dot.opacityProperty(), 1.0))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        Label headerLabel = new Label("✦ BISIK GUILDMATE");
        headerLabel.setStyle(
            "-fx-text-fill: #6A5840;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-letter-spacing: 2;"
        );
        HBox.setHgrow(headerLabel, Priority.ALWAYS);

        Label channelLabel = new Label("CH.1");
        channelLabel.setStyle(
            "-fx-text-fill: #3A2810;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 11px;"
        );

        header.getChildren().addAll(dot, headerLabel, channelLabel);
        getChildren().add(header);

        // ── Message area ──────────────────────────────────────
        messageContainer = new VBox(2);
        messageContainer.setPadding(new Insets(8, 10, 8, 10));
        messageContainer.setFillWidth(true);

        scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle(
            "-fx-background-color: #080604;" +
            "-fx-background: #080604;" +
            "-fx-border-color: transparent;"
        );
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        getChildren().add(scrollPane);

        // Welcome message
        addSystemMessage("✦ BISIK GUILDMATE ONLINE");
        addSystemMessage("Guildmate siap menemanimu......");
    }

    // ── Public API ────────────────────────────────────────────

    /**
     * Tambah dialog dari mercenary dengan animasi fade-in.
     */
    public void addMercMessage(MercenaryDialogue.ChatMessage msg) {
        Platform.runLater(() -> {
            try {
                VBox bubble = buildMercBubble(msg);
                messageContainer.getChildren().add(bubble);
                trimMessages();
                scrollToBottom();
            } catch (Exception e) {
                // Jika gagal (panel belum siap), retry setelah 500ms
                new Timeline(new KeyFrame(Duration.millis(500),
                    ev -> addMercMessage(msg))).play();
            }
        });
    }

    /**
     * Tambah beberapa dialog sekaligus (dengan delay antar pesan).
     */
    public void addMercMessages(List<MercenaryDialogue.ChatMessage> messages) {
        for (int i = 0; i < messages.size(); i++) {
            final MercenaryDialogue.ChatMessage msg = messages.get(i);
            final long delayMs = i * 800L; // delay 800ms antar pesan
            Timeline t = new Timeline(new KeyFrame(Duration.millis(delayMs),
                    e -> addMercMessage(msg)));
            t.play();
        }
    }

    /**
     * Tambah pesan sistem (floor enter, combat start, dll).
     */
    public void addSystemMessage(String text) {
        Platform.runLater(() -> {
            Label sysMsg = new Label("— " + text + " —");
            sysMsg.setMaxWidth(Double.MAX_VALUE);
            sysMsg.setAlignment(Pos.CENTER);
            sysMsg.setStyle(
                "-fx-text-fill: #3A2810;" +
                "-fx-font-family: 'Courier New';" +
                "-fx-font-size: 11px;" +
                "-fx-padding: 4 0;"
            );
            sysMsg.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(600), sysMsg);
            ft.setToValue(1);
            ft.play();

            messageContainer.getChildren().add(sysMsg);
            trimMessages();
            scrollToBottom();
        });
    }

    /**
     * Emit trigger — cari dialog yang sesuai dari active mercs.
     */
    public void emitTrigger(
            List<arclightcity.entity.mercenary.Mercenary> mercs,
            MercenaryDialogue.Trigger trigger) {
        List<MercenaryDialogue.ChatMessage> msgs =
                MercenaryDialogue.getGroupDialogue(mercs, trigger);
        if (!msgs.isEmpty()) {
            addMercMessages(msgs);
        }
    }

    /** Clear semua pesan dan reset ke state awal */
    public void clear() {
        Platform.runLater(() -> {
            messageContainer.getChildren().clear();
            addSystemMessage("✦ BISIK GUILDMATE ONLINE");
        });
    }

    // ── Message Builder ───────────────────────────────────────

    private VBox buildMercBubble(MercenaryDialogue.ChatMessage msg) {
        String color = MERC_COLORS.getOrDefault(msg.mercType(), "#C8860A");

        // Container
        HBox container = new HBox(8);
        container.setPadding(new Insets(6, 8, 6, 8));
        container.setAlignment(Pos.TOP_LEFT);
        container.setMaxWidth(Double.MAX_VALUE);

        // Portrait box (circle + icon + nama di bawah)
        VBox portrait = new VBox(2);
        portrait.setAlignment(Pos.TOP_CENTER);
        portrait.setMinWidth(44);
        portrait.setMaxWidth(44);

        // Avatar circle dengan portrait gambar atau inisial
        javafx.scene.layout.StackPane avatarStack = new javafx.scene.layout.StackPane();
        Circle avatarBg = new Circle(20, Color.web(color + "22"));
        avatarBg.setStroke(Color.web(color));
        avatarBg.setStrokeWidth(1.5);

        javafx.scene.image.Image portraitImg =
            arclightcity.ui.util.AssetManager.portraitGuildmate(msg.mercName(), false);
        if (portraitImg != null) {
            javafx.scene.image.ImageView piv =
                arclightcity.ui.util.AssetManager.makeIV(portraitImg, 36, 36);
            Circle clip = new Circle(18, 18, 18);
            piv.setClip(clip);
            avatarStack.getChildren().addAll(avatarBg, piv);
        } else {
            String initials = msg.mercName().length() >= 2
                ? msg.mercName().substring(0, 2).toUpperCase()
                : msg.mercName().toUpperCase();
            Label avatarLbl = new Label(initials);
            avatarLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Courier New';" +
                               "-fx-font-size: 9px; -fx-font-weight: bold;");
            avatarStack.getChildren().addAll(avatarBg, avatarLbl);
        }

        // Nama di bawah avatar
        String shortName = msg.mercName().contains(" ")
            ? msg.mercName().split(" ")[0] : msg.mercName();
        Label nameLbl = new Label(shortName);
        nameLbl.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Courier New';" +
                         "-fx-font-size: 8px; -fx-font-weight: bold; -fx-alignment: CENTER;");
        nameLbl.setMaxWidth(44);
        nameLbl.setAlignment(Pos.CENTER);

        portrait.getChildren().addAll(avatarStack, nameLbl);

        // Message bubble di kanan
        VBox bubble = new VBox(2);
        HBox.setHgrow(bubble, Priority.ALWAYS);
        bubble.setPadding(new Insets(6, 8, 6, 8));
        bubble.setStyle(
            "-fx-background-color: " + color + "0D;" +
            "-fx-border-color: " + color + "33;" +
            "-fx-border-width: 1 1 1 2;"
        );

        // Time stamp
        Label timeLabel = new Label(getCurrentTime());
        timeLabel.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 8px;");

        // Message text
        Label textLabel = new Label(msg.text());
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(200);
        textLabel.setStyle(
            "-fx-text-fill: #A09070;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 11px;"
        );

        bubble.getChildren().addAll(timeLabel, textLabel);
        container.getChildren().addAll(portrait, bubble);

        // Wrap in VBox for fade animation
        VBox wrapper = new VBox(container);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(400), wrapper);
        ft.setToValue(1.0);
        ft.play();

        return wrapper;
    }

    // ── Helpers ───────────────────────────────────────────────

    private void scrollToBottom() {
        Timeline scroll = new Timeline(
            new KeyFrame(Duration.millis(100),
                e -> scrollPane.setVvalue(1.0))
        );
        scroll.play();
    }

    /** Hapus pesan lama jika terlalu banyak (max 40 pesan) */
    private void trimMessages() {
        while (messageContainer.getChildren().size() > 40) {
            messageContainer.getChildren().remove(0);
        }
    }

    private String getCurrentTime() {
        java.time.LocalTime now = java.time.LocalTime.now();
        return String.format("%02d:%02d", now.getHour(), now.getMinute());
    }
}
