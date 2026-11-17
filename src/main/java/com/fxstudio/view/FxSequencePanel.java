package com.fxstudio.view;

import com.fxstudio.model.FxEffectSequence;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.IntConsumer;

/**
 * Optional replay-queue UI. It does not replace the existing single-effect controls.
 */
public final class FxSequencePanel extends BorderPane {

    private final ListView<FxEffectSequence.Entry> list = new ListView<>();
    private final CheckBox loopCheck = new CheckBox("Loop entire queue");

    private IntConsumer onRemove;
    private IntConsumer onMoveUp;
    private IntConsumer onMoveDown;
    private Runnable onReplay;
    private Runnable onStop;
    private Runnable onLoopChanged;

    public FxSequencePanel() {
        setPadding(new Insets(8));
        setStyle("-fx-background-color: #141923;");

        Label title = new Label("EFFECT REPLAY QUEUE");
        title.setStyle("-fx-text-fill: #d1d5db; -fx-font-size: 11px; -fx-font-weight: bold;");

        list.setPrefHeight(105);
        list.setMinHeight(70);
        list.setPlaceholder(new Label("No queued effects. Use 'Add to Queue' after compiling an effect."));
        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(FxEffectSequence.Entry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                } else {
                    setText(entry.name() + "   |   " + entry.frames().size() + " frames");
                }
            }
        });

        Button up = new Button("Move Up");
        Button down = new Button("Move Down");
        Button remove = new Button("Remove");
        Button replay = new Button("Replay Queue");
        Button stop = new Button("Stop");

        up.setOnAction(e -> {
            if (onMoveUp != null) onMoveUp.accept(list.getSelectionModel().getSelectedIndex());
        });
        down.setOnAction(e -> {
            if (onMoveDown != null) onMoveDown.accept(list.getSelectionModel().getSelectedIndex());
        });
        remove.setOnAction(e -> {
            if (onRemove != null) onRemove.accept(list.getSelectionModel().getSelectedIndex());
        });
        replay.setOnAction(e -> {
            if (onReplay != null) onReplay.run();
        });
        stop.setOnAction(e -> {
            if (onStop != null) onStop.run();
        });
        loopCheck.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if (onLoopChanged != null) onLoopChanged.run();
        });

        HBox editButtons = new HBox(6, up, down, remove);
        editButtons.setAlignment(Pos.CENTER_LEFT);

        HBox playbackButtons = new HBox(8, replay, stop, loopCheck);
        playbackButtons.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(7, title, list, editButtons, playbackButtons);
        setCenter(content);
    }

    public void refresh(FxEffectSequence sequence) {
        int selected = list.getSelectionModel().getSelectedIndex();
        list.getItems().setAll(sequence.getEntries());

        if (!list.getItems().isEmpty()) {
            list.getSelectionModel().select(Math.max(0, Math.min(selected, list.getItems().size() - 1)));
        }
    }

    public int getSelectedIndex() {
        return list.getSelectionModel().getSelectedIndex();
    }

    public void select(int index) {
        if (index >= 0 && index < list.getItems().size()) {
            list.getSelectionModel().select(index);
        }
    }

    public void setOnRemove(IntConsumer callback) { onRemove = callback; }
    public void setOnMoveUp(IntConsumer callback) { onMoveUp = callback; }
    public void setOnMoveDown(IntConsumer callback) { onMoveDown = callback; }
    public void setOnReplay(Runnable callback) { onReplay = callback; }
    public void setOnStop(Runnable callback) { onStop = callback; }
    public void setOnLoopChanged(Runnable callback) { onLoopChanged = callback; }
    public boolean isLoopSelected() { return loopCheck.isSelected(); }
}
