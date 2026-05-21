package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.lore.DialogScript;
import arclightcity.ui.util.UIFactory;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.*;

/**
 * Arsip Lore — galeri seluruh cerita game, diakses dari Hub.
 * Act terkunci sampai player menyelesaikan act tersebut.
 */
public class LoreArchiveView {

    private final GameEngine engine;
    private final SceneRouter router;

    // Definisi setiap Act
    record Act(
        String id,          // ID unik
        String title,       // Judul act
        String subtitle,    // Lokasi / zona
        String floorRange,  // Range lantai
        String color,       // Warna tema
        String unlockKey,   // Script key yang harus sudah diplay (null = selalu terbuka)
        String[] scriptKeys,         // Dialog SEBELUM video
        String[] postVideoScriptKeys,// Dialog SETELAH video (tampil di bawah)
        boolean hasVideo    // Ada video cutscene?
    ) {}

    private static final Act[] ACTS = {
        new Act("ACT0", "ACT 0", "Malam Kejadian",
                "Jakarta → Pasar Malam Gaib", "#C8860A", null,
                new String[]{"OPENING","FIRST_DUNGEON"}, new String[]{}, true),

        new Act("ACT1", "ACT 1", "Pasar Malam Gaib",
                "Lantai 1–10 · Boss: Batara Kala", "#CC6622", "BOSS1_PRE",
                new String[]{"BOSS1_PRE","RECRUIT_GATOTKACA","RECRUIT_SRIKANDI","BOSS1_POST"}, new String[]{}, false),

        new Act("ACT2", "ACT 2", "Candi Terlarang",
                "Lantai 11–20 · Boss: Nyi Roro Kidul", "#2266AA", "BOSS2_PRE",
                new String[]{"BOSS2_PRE","THERESA_VOICE_1","BOSS2_POST"}, new String[]{}, false),

        new Act("ACT3A", "ACT 3A", "Hutan Angker",
                "Lantai 21–30 · Boss: Rangda Agung", "#446622", "BOSS3_PRE",
                new String[]{"BOSS3_PRE","RANGGA_REVEAL","BOSS3_POST"}, new String[]{}, false),

        new Act("ACT3B", "ACT 3B", "Goa Naga",
                "Lantai 31–40 · Boss: Garuda Mahaguru", "#884422", "BOSS4_PRE",
                new String[]{"BOSS4_PRE","BOSS4_POST"}, new String[]{}, false),

        new Act("ACT3C", "ACT 3C", "Kahyangan Rusak",
                "Lantai 41–50 · Boss: Semar Pamungkas", "#886622", "BOSS5_PRE",
                new String[]{"BOSS5_PRE","BOSS5_POST"}, new String[]{}, false),

        new Act("FINAL", "ACT FINAL", "Void Dimension",
                "Lantai 51 · Boss: Theresa", "#AA44FF", "FINAL_BOSS_PRE",
                new String[]{"FINAL_BOSS_PRE"},      // sebelum video
                new String[]{"ENDING"},               // ENDING dialog setelah video
                true),
    };

    // Nama tampilan per script key
    private static final Map<String,String> SCRIPT_LABELS = Map.ofEntries(
        Map.entry("OPENING",          "◈ Pembukaan — Asuna Tiba di Nusantara"),
        Map.entry("FIRST_DUNGEON",    "◈ Tutorial — Dungeon Pertama"),
        Map.entry("BOSS1_PRE",        "◈ Konfrontasi Batara Kala"),
        Map.entry("RECRUIT_GATOTKACA","◈ Rekrutmen — Gatot Kaca (Tank-RX9)"),
        Map.entry("RECRUIT_SRIKANDI", "◈ Rekrutmen — Srikandi (Kira Voss)"),
        Map.entry("BOSS1_POST",       "◈ Setelah Batara Kala Dikalahkan"),
        Map.entry("BOSS2_PRE",        "◈ Konfrontasi Nyi Roro Kidul"),
        Map.entry("THERESA_VOICE_1",  "◈ Suara Theresa — Peringatan Pertama"),
        Map.entry("BOSS2_POST",       "◈ Momen Nyai Roro — Sang Guru"),
        Map.entry("BOSS3_PRE",        "◈ Konfrontasi Rangda Agung"),
        Map.entry("RANGGA_REVEAL",    "◈ Revelasi Rangga — Vector"),
        Map.entry("BOSS3_POST",       "◈ Setelah Rangda Agung Dikalahkan"),
        Map.entry("BOSS4_PRE",        "◈ Konfrontasi Garuda Mahaguru"),
        Map.entry("BOSS4_POST",       "◈ Revelasi Srikandi — Bulu Emas Garuda"),
        Map.entry("BOSS5_PRE",        "◈ Pengorbanan Semar Pamungkas"),
        Map.entry("BOSS5_POST",       "◈ Red Blossom Katana Terbentuk"),
        Map.entry("FINAL_BOSS_PRE",   "◈ Konfrontasi Akhir — Theresa"),
        Map.entry("ENDING",           "◈ Ending — Kehangatan Pertama")
    );

    private String selectedActId = "ACT0";

    public LoreArchiveView(GameEngine engine, SceneRouter router) {
        this.engine = engine;
        this.router  = router;
    }

    public Parent build() {
        BorderPane root = UIFactory.screenRootBorder();

        // ── Header ──────────────────────────────────────────
        VBox header = new VBox(2);
        header.setPadding(new Insets(12, 20, 10, 20));
        header.setStyle("-fx-background-color:#0A0706; -fx-border-color:#3A2810; -fx-border-width:0 0 1 0;");
        Label title = new Label("◈  ARSIP LORE  —  MYTHIC ITEM OBTAINED");
        title.setStyle("-fx-text-fill:#C8860A; -fx-font-family:'Courier New'; -fx-font-size:15px; -fx-font-weight:bold;");
        Label sub = new Label("Perjalanan Asuna melintasi Nusantara — dari Pasar Malam Gaib hingga Void Dimension");
        sub.setStyle("-fx-text-fill:rgba(200,134,10,0.45); -fx-font-family:'Courier New'; -fx-font-size:10px;");
        Button backBtn = new Button("← KEMBALI KE HUB");
        backBtn.setStyle("-fx-background-color:transparent; -fx-border-color:#3A2810; -fx-border-width:1;" +
            "-fx-text-fill:#A09070; -fx-font-family:'Courier New'; -fx-font-size:10px; -fx-cursor:hand;");
        backBtn.setOnAction(e -> router.showHub());
        HBox titleRow = new HBox(10, title);
        HBox.setHgrow(title, Priority.ALWAYS);
        titleRow.getChildren().add(backBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(titleRow, sub);
        root.setTop(header);

        // ── Layout: kiri = act list, kanan = detail ──────────
        HBox body = new HBox(0);
        VBox.setVgrow(body, Priority.ALWAYS);

        // Kanan dulu — perlu referensi untuk pass ke buildActList
        ScrollPane detailScroll = new ScrollPane();
        detailScroll.setFitToWidth(true);
        detailScroll.setStyle("-fx-background:transparent; -fx-background-color:transparent;");
        HBox.setHgrow(detailScroll, Priority.ALWAYS);

        // Kiri: daftar act — pass detailScroll agar klik bisa update in-place
        VBox actList = buildActList(detailScroll);
        actList.setPrefWidth(200);
        actList.setMinWidth(200);
        actList.setMaxWidth(200);

        body.getChildren().addAll(actList, detailScroll);
        root.setCenter(body);

        // Render act default (ACT0)
        refreshDetail(detailScroll);

        return root;
    }

    // ── Panel kiri: list seluruh act ─────────────────────────
    private VBox buildActList(javafx.scene.control.ScrollPane detailPane) {
        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color:#080604; -fx-border-color:#3A2810; -fx-border-width:0 1 0 0;");
        panel.setPadding(new Insets(8, 0, 8, 0));

        Label panelHdr = new Label("  DAFTAR ACT");
        panelHdr.setStyle("-fx-text-fill:rgba(200,134,10,0.40); -fx-font-family:'Courier New';" +
            "-fx-font-size:9px; -fx-font-weight:bold; -fx-padding:4 8;");
        panel.getChildren().add(panelHdr);

        // Build rows dulu agar bisa saling referensi untuk style update
        for (Act act : ACTS) {
            boolean unlocked = isUnlocked(act);
            VBox row = buildActRow(act, unlocked, detailPane, panel);
            panel.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setPrefWidth(200);
        scroll.setStyle("-fx-background:transparent; -fx-background-color:transparent;");

        VBox wrapper = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        wrapper.setMaxWidth(200);
        wrapper.setPrefWidth(200);
        return wrapper;
    }

    private VBox buildActRow(Act act, boolean unlocked,
                              javafx.scene.control.ScrollPane detailPane,
                              VBox listContainer) {
        VBox row = new VBox(2);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setCursor(unlocked ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);

        boolean selected = act.id().equals(selectedActId);
        applyRowStyle(row, selected, act.color(), unlocked);

        Label numLbl = new Label((unlocked ? "" : "🔒  ") + act.title());
        numLbl.setStyle("-fx-text-fill:" + (unlocked ? act.color() : "#3A2810") +
            "; -fx-font-family:'Courier New'; -fx-font-size:10px; -fx-font-weight:bold;");
        Label subtitleLbl = new Label(act.subtitle());
        subtitleLbl.setStyle("-fx-text-fill:" + (unlocked ? "rgba(200,134,10,0.55)" : "#2A1808") +
            "; -fx-font-family:'Courier New'; -fx-font-size:9px;");
        subtitleLbl.setWrapText(true);
        row.getChildren().addAll(numLbl, subtitleLbl);

        if (unlocked) {
            row.setOnMouseEntered(e -> {
                if (!act.id().equals(selectedActId))
                    row.setStyle("-fx-background-color:rgba(200,134,10,0.06);");
            });
            row.setOnMouseExited(e -> applyRowStyle(row,
                act.id().equals(selectedActId), act.color(), true));
            row.setOnMouseClicked(e -> {
                selectedActId = act.id();
                // Update styling semua row tanpa rebuild view
                for (var child : listContainer.getChildren()) {
                    if (child instanceof VBox r && r.getUserData() instanceof Act a) {
                        applyRowStyle(r, a.id().equals(selectedActId), a.color(), isUnlocked(a));
                    }
                }
                // Update panel kanan in-place
                detailPane.setContent(buildActDetail(act));
            });
        }
        row.setUserData(act);
        return row;
    }

    private void applyRowStyle(VBox row, boolean selected, String color, boolean unlocked) {
        if (!unlocked) {
            row.setStyle("-fx-background-color:transparent;");
            return;
        }
        String bg    = selected ? "rgba(200,134,10,0.12)" : "transparent";
        String lBord = selected ? " -fx-border-color:#C8860A; -fx-border-width:0 0 0 2;" : "";
        row.setStyle("-fx-background-color:" + bg + ";" + lBord);
    }

    // ── Panel kanan: detail act terpilih ──────────────────────
    private void refreshDetail(ScrollPane scroll) {
        Act selected = Arrays.stream(ACTS)
            .filter(a -> a.id().equals(selectedActId))
            .findFirst().orElse(ACTS[0]);
        scroll.setContent(buildActDetail(selected));
    }

    private VBox buildActDetail(Act act) {
        boolean unlocked = isUnlocked(act);
        VBox page = new VBox(14);
        page.setPadding(new Insets(20, 24, 20, 24));
        page.setStyle("-fx-background-color:#0A0806;");

        if (!unlocked) {
            return buildLockedPage(act);
        }

        // Act header
        HBox hdr = new HBox(12);
        hdr.setAlignment(Pos.CENTER_LEFT);
        Rectangle accent = new Rectangle(4, 52, Color.web(act.color()));
        accent.setEffect(new Glow(0.25));
        VBox titleBox = new VBox(4);
        Label actNum = new Label(act.title());
        actNum.setStyle("-fx-text-fill:" + act.color() + "; -fx-font-family:'Courier New';" +
            "-fx-font-size:13px; -fx-font-weight:bold;");
        Label actSub = new Label(act.subtitle());
        actSub.setStyle("-fx-text-fill:rgba(200,134,10,0.70); -fx-font-family:'Courier New'; -fx-font-size:13px;");
        Label floorLbl = new Label(act.floorRange());
        floorLbl.setStyle("-fx-text-fill:rgba(200,134,10,0.35); -fx-font-family:'Courier New'; -fx-font-size:10px;");
        titleBox.getChildren().addAll(actNum, actSub, floorLbl);
        hdr.getChildren().addAll(accent, titleBox);
        page.getChildren().add(hdr);

        // Divider
        Region div = new Region();
        div.setPrefHeight(1);
        div.setStyle("-fx-background-color:#3A2810;");
        page.getChildren().add(div);

        // Script list
        Label scriptHdr = new Label("DIALOG & CUTSCENE");
        scriptHdr.setStyle("-fx-text-fill:rgba(200,134,10,0.45); -fx-font-family:'Courier New';" +
            "-fx-font-size:9px; -fx-font-weight:bold;");
        page.getChildren().add(scriptHdr);

        for (String key : act.scriptKeys()) {
            if (DialogScript.has(key)) {
                page.getChildren().add(buildScriptRow(key, act.color()));
            }
        }

        // Tampilkan VIDEO CUTSCENE hanya jika ada script video yang tersedia
        if (act.hasVideo()) {
            String vidKey = act.id().toLowerCase() + "_video";
            var videoBeats = arclightcity.ui.lore.DialogScript.get(vidKey);
            if (videoBeats != null && !videoBeats.isEmpty()) {
                Region div2 = new Region();
                div2.setPrefHeight(1);
                div2.setStyle("-fx-background-color:#3A2810;");
                page.getChildren().add(div2);
                Label vidHdr = new Label("VIDEO CUTSCENE");
                vidHdr.setStyle("-fx-text-fill:rgba(200,134,10,0.45); -fx-font-family:'Courier New';" +
                    "-fx-font-size:9px; -fx-font-weight:bold;");
                page.getChildren().add(vidHdr);
                page.getChildren().add(buildVideoRow(act));
            }
        }

        // Script yang tampil SETELAH video (misal ENDING di ACT FINAL)
        if (act.postVideoScriptKeys().length > 0) {
            for (String key : act.postVideoScriptKeys()) {
                if (arclightcity.ui.lore.DialogScript.has(key)) {
                    page.getChildren().add(buildScriptRow(key, act.color()));
                }
            }
        }

        return page;
    }

    private VBox buildLockedPage(Act act) {
        VBox page = new VBox(16);
        page.setPadding(new Insets(60, 40, 40, 40));
        page.setAlignment(Pos.CENTER);
        page.setStyle("-fx-background-color:#0A0806;");

        Label lock = new Label("🔒");
        lock.setStyle("-fx-font-size:48px;");
        Label title = new Label(act.title() + " — " + act.subtitle());
        title.setStyle("-fx-text-fill:#3A2810; -fx-font-family:'Courier New'; -fx-font-size:14px; -fx-font-weight:bold;");
        Label msg = new Label("Act ini masih terkunci.\nSelesaikan " + act.floorRange().split("·")[0].trim() +
            "\nuntuk membuka arsip cerita ini.");
        msg.setStyle("-fx-text-fill:#2A1808; -fx-font-family:'Courier New'; -fx-font-size:11px;");
        msg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        msg.setWrapText(true);

        page.getChildren().addAll(lock, title, msg);
        return page;
    }

    private HBox buildScriptRow(String key, String accentColor) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:rgba(255,255,255,0.025); -fx-border-color:#1E150A; -fx-border-width:0 0 1 0;");

        // Label
        Label lbl = new Label(SCRIPT_LABELS.getOrDefault(key, "◈ " + key));
        lbl.setStyle("-fx-text-fill:rgba(200,160,80,0.80); -fx-font-family:'Courier New'; -fx-font-size:11px;");
        HBox.setHgrow(lbl, Priority.ALWAYS);
        lbl.setWrapText(false);

        // Played indicator
        boolean played = engine.cutscenePlayed(key);
        Label playedLbl = new Label(played ? "✓ Sudah ditonton" : "Belum ditonton");
        playedLbl.setStyle("-fx-text-fill:" + (played ? "rgba(80,180,80,0.50)" : "rgba(180,100,40,0.40)") +
            "; -fx-font-family:'Courier New'; -fx-font-size:9px;");

        // Tombol PUTAR
        Button playBtn = new Button("▶  PUTAR");
        playBtn.setStyle("-fx-background-color:transparent; -fx-border-color:" + accentColor +
            "; -fx-border-width:1; -fx-text-fill:" + accentColor +
            "; -fx-font-family:'Courier New'; -fx-font-size:9px; -fx-padding:4 10; -fx-cursor:hand;");
        playBtn.setOnAction(e -> {
            engine.markCutscenePlayed(key); // mark supaya tidak muncul lagi sebagai "pertama kali"
            router.playCutscene(key, () -> router.showLoreArchive());
        });
        playBtn.setOnMouseEntered(ex ->
            playBtn.setStyle(playBtn.getStyle().replace("transparent", "#C8860A22")));
        playBtn.setOnMouseExited(ex ->
            playBtn.setStyle(playBtn.getStyle().replace("#C8860A22", "transparent")));

        VBox meta = new VBox(2, lbl, playedLbl);
        HBox.setHgrow(meta, Priority.ALWAYS);
        row.getChildren().addAll(meta, playBtn);
        return row;
    }

    private HBox buildVideoRow(Act act) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:rgba(100,60,200,0.06); -fx-border-color:#3A1A5A; -fx-border-width:1;");

        Label icon = new Label("🎬");
        icon.setStyle("-fx-font-size:20px;");
        Label lbl = new Label("Video Cutscene — " + act.subtitle());
        lbl.setStyle("-fx-text-fill:#AA66FF; -fx-font-family:'Courier New'; -fx-font-size:11px;");
        HBox.setHgrow(lbl, Priority.ALWAYS);

        Button playBtn = new Button("▶  TONTON");
        playBtn.setStyle("-fx-background-color:transparent; -fx-border-color:#AA44FF; -fx-border-width:1;" +
            "-fx-text-fill:#AA44FF; -fx-font-family:'Courier New'; -fx-font-size:9px; -fx-padding:4 10; -fx-cursor:hand;");
        playBtn.setOnAction(e -> router.showVideo(act.id().toLowerCase() + "_video",
            () -> router.showLoreArchive()));

        row.getChildren().addAll(icon, lbl, playBtn);
        return row;
    }

    private boolean isUnlocked(Act act) {
        if (act.unlockKey() == null) return true; // ACT 0 selalu terbuka
        return engine.cutscenePlayed(act.unlockKey());
    }
}
