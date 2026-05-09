package arclightcity.ui.view;

import arclightcity.engine.GameEngine;
import arclightcity.save.SaveManager;
import arclightcity.ui.controller.SceneRouter;
import arclightcity.ui.util.UIFactory;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.util.Duration;

/**
 * SaveLoadView — UI slot save/load seperti game RPG.
 * 3 slot manual + 1 auto-save.
 * Mode: SAVE (simpan) atau LOAD (muat).
 */
public class SaveLoadView {

    private final GameEngine  engine;
    private final SceneRouter router;
    private final boolean     isSaveMode;
    private int               selectedSlot = 1;

    public SaveLoadView(GameEngine engine, SceneRouter router, boolean isSaveMode) {
        this.engine     = engine;
        this.router     = router;
        this.isSaveMode = isSaveMode;
    }

    public Parent build() {
        BorderPane root = UIFactory.screenRootBorder();

        // ── Header ────────────────────────────────────────────
        HBox hdr = new HBox(10);
        hdr.setPadding(new Insets(10, 16, 10, 16));
        hdr.setAlignment(Pos.CENTER_LEFT);
        hdr.setStyle("-fx-background-color: #0F0A06;" +
                     "-fx-border-color: #3A2810; -fx-border-width: 0 0 1 0;");

        Button back = new Button(engine.getPlayer() != null ? "← MARKAS" : "← MENU");
        back.setStyle("-fx-background-color: transparent; -fx-border-color: #3A2810;" +
                      "-fx-border-width: 1; -fx-text-fill: #5A3A10;" +
                      "-fx-font-family: 'Courier New'; -fx-font-size: 10px;" +
                      "-fx-padding: 3 8; -fx-cursor: hand;");
        back.setOnAction(e -> {
            if (engine.getPlayer() != null) router.showHub();
            else router.showMainMenu();
        });

        Label title = new Label(isSaveMode ? "💾  SIMPAN GAME" : "📂  MUAT GAME");
        title.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 14px; -fx-font-weight: bold;");
        HBox.setHgrow(title, Priority.ALWAYS);

        hdr.getChildren().addAll(back, title);
        root.setTop(hdr);

        // ── Slot grid (2×2) ──────────────────────────────────
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: #0A0604;");

        Label subtitle = new Label(isSaveMode
            ? "Pilih slot untuk menyimpan progress:"
            : "Pilih slot untuk memuat game:");
        subtitle.setStyle("-fx-text-fill: #5A3A10; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        content.getChildren().add(subtitle);

        // Grid 2×2
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);

        // Slot 1-3 manual + auto
        VBox[] slots = new VBox[4];
        slots[0] = buildSlotCard(1, "SLOT 1",  SaveManager.getSlotSummary(1), false);
        slots[1] = buildSlotCard(2, "SLOT 2",  SaveManager.getSlotSummary(2), false);
        slots[2] = buildSlotCard(3, "SLOT 3",  SaveManager.getSlotSummary(3), false);
        slots[3] = buildSlotCard(0, "AUTO SAVE", SaveManager.getAutoSaveSummary(), true);

        grid.add(slots[0], 0, 0);
        grid.add(slots[1], 1, 0);
        grid.add(slots[2], 0, 1);
        grid.add(slots[3], 1, 1);

        GridPane.setHgrow(slots[0], Priority.ALWAYS);
        GridPane.setHgrow(slots[1], Priority.ALWAYS);
        GridPane.setHgrow(slots[2], Priority.ALWAYS);
        GridPane.setHgrow(slots[3], Priority.ALWAYS);

        content.getChildren().add(grid);
        VBox.setVgrow(grid, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #0A0604; -fx-background: #0A0604;" +
                        "-fx-border-color: transparent;");
        root.setCenter(scroll);

        UIFactory.fadeIn(root, 350);
        return root;
    }

    private VBox buildSlotCard(int slotNum, String slotLabel,
                                String summary, boolean isAuto) {
        boolean isEmpty = summary.equals("EMPTY");
        boolean isAuto2 = isAuto;

        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinHeight(120);

        String baseBg    = isEmpty ? "#1A1008" : "#1A1208";
        String baseBorder= isEmpty ? "#3A2810" : "#2D7A45";

        card.setStyle(
            "-fx-background-color: " + baseBg + ";" +
            "-fx-border-color: " + baseBorder + ";" +
            "-fx-border-width: 2;" +
            "-fx-cursor: " + ((isAuto && isSaveMode) ? "default" : "hand") + ";"
        );

        // Slot label
        Label label = new Label(slotLabel);
        label.setStyle("-fx-text-fill: #FFB830; -fx-font-family: 'Courier New';" +
                       "-fx-font-size: 13px; -fx-font-weight: bold;");

        if (isEmpty) {
            Label emptyLbl = new Label("— KOSONG —");
            emptyLbl.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New';" +
                              "-fx-font-size: 11px;");
            card.getChildren().addAll(label, emptyLbl);
        } else {
            // Parse summary
            String[] lines = summary.split("\n");
            for (String line : lines) {
                if (line.isBlank()) continue;
                Label lbl = new Label(line.trim());
                lbl.setStyle("-fx-text-fill: #A09070; -fx-font-family: 'Courier New';" +
                             "-fx-font-size: 10px;");
                if (label.getParent() == null) card.getChildren().add(label);
                card.getChildren().add(lbl);
            }
            if (card.getChildren().isEmpty()) card.getChildren().add(label);
        }

        // Tombol aksi
        if (isAuto && isSaveMode) {
            // Auto save tidak bisa di-overwrite manual
            Label info = new Label("Otomatis setiap turun floor");
            info.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");
            card.getChildren().add(info);
        } else {
            HBox btnRow = new HBox(8);
            btnRow.setAlignment(Pos.CENTER);

            if (isSaveMode && !isAuto) {
                Button saveBtn = new Button("💾 SIMPAN");
                saveBtn.setStyle(
                    "-fx-background-color: #C8860A22; -fx-border-color: #C8860A;" +
                    "-fx-border-width: 1; -fx-text-fill: #FFB830;" +
                    "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                    "-fx-padding: 5 12; -fx-cursor: hand;");
                saveBtn.setOnAction(e -> {
                    var state = arclightcity.save.GameStateConverter
                        .toSaveState(engine, false);
                    var result = SaveManager.saveSlot(state, slotNum);
                    router.addSystemChat("💾 " + result.message());
                    router.showSaveLoad(true);
                });
                btnRow.getChildren().add(saveBtn);

            } else if (!isSaveMode) {
                if (!isEmpty) {
                    Button loadBtn = new Button("📂 MUAT");
                    loadBtn.setStyle(
                        "-fx-background-color: #2D7A4522; -fx-border-color: #2D7A45;" +
                        "-fx-border-width: 1; -fx-text-fill: #4DCA75;" +
                        "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                        "-fx-padding: 5 12; -fx-cursor: hand;");
                    loadBtn.setOnAction(e -> {
                        var saveOpt = isAuto
                            ? SaveManager.loadAuto()
                            : SaveManager.loadSlot(slotNum);
                        saveOpt.ifPresent(save -> {
                            arclightcity.save.GameStateConverter
                                .restoreFromSave(engine, save);
                            router.addSystemChat("📂 Game dimuat dari " + slotLabel);
                            router.showHub();
                        });
                    });
                    btnRow.getChildren().add(loadBtn);

                    Button delBtn = new Button("🗑");
                    delBtn.setStyle(
                        "-fx-background-color: transparent; -fx-border-color: #CC3300;" +
                        "-fx-border-width: 1; -fx-text-fill: #CC3300;" +
                        "-fx-font-family: 'Courier New'; -fx-font-size: 11px;" +
                        "-fx-padding: 5 8; -fx-cursor: hand;");
                    delBtn.setOnAction(e -> {
                        if (isAuto) SaveManager.deleteAllSaves();
                        else SaveManager.deleteSlot(slotNum);
                        router.showSaveLoad(false);
                    });
                    btnRow.getChildren().add(delBtn);
                } else {
                    Label noSave = new Label("Tidak ada save");
                    noSave.setStyle("-fx-text-fill: #3A2810; -fx-font-family: 'Courier New';" +
                                   "-fx-font-size: 10px;");
                    btnRow.getChildren().add(noSave);
                }
            }

            if (!btnRow.getChildren().isEmpty()) {
                card.getChildren().add(btnRow);
            }
        }

        // Hover effect
        if (!(isAuto && isSaveMode)) {
            card.setOnMouseEntered(e ->
                card.setStyle(card.getStyle()
                    .replace(baseBg, "#241810")
                    .replace(baseBorder, "#FFB830")));
            card.setOnMouseExited(e ->
                card.setStyle(card.getStyle()
                    .replace("#241810", baseBg)
                    .replace("#FFB830", baseBorder)));
        }

        return card;
    }
}
