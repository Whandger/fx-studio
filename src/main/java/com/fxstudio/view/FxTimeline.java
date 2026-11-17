package com.fxstudio.view;

import com.fxstudio.model.FxAnimation;
import com.fxstudio.model.FxFrame;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;

public class FxTimeline extends VBox {

    private final Slider scrubSlider = new Slider(0, 0, 0);
    private final HBox frameContainer = new HBox(5);
    private final Label frameCounter = new Label("Frame 0 / 0 | age: 0.00");

    private FxAnimation animation;
    private IntConsumer onFrameSelected;
    private boolean updatingSlider = false;

    public FxTimeline() {
        setSpacing(8);
        setPadding(new Insets(10, 14, 10, 14));
        setMinHeight(125);
        setMaxHeight(145);

        Label title = new Label("TIMELINE");
        title.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #9ca3af;");

        frameCounter.setStyle("-fx-font-size: 11px; -fx-font-family: 'Consolas'; -fx-text-fill: #e5e7eb;");

        HBox header = new HBox(14, title, frameCounter);
        header.setAlignment(Pos.CENTER_LEFT);

        scrubSlider.setMaxWidth(Double.MAX_VALUE);
        scrubSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingSlider || animation == null || animation.isEmpty()) return;
            int idx = newVal.intValue();
            animation.setCurrentFrameIndex(idx);
            updateSelectionOnly();
            if (onFrameSelected != null) onFrameSelected.accept(idx);
        });

        ScrollPane scrollPane = new ScrollPane(frameContainer);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        frameContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, scrubSlider, scrollPane);
        setStyle("-fx-background-color: #161922; -fx-border-color: #292e3a; -fx-border-width: 1 0 0 0;");
    }

    public void setAnimation(FxAnimation animation) {
        this.animation = animation;
        refresh();
    }

    public void setOnFrameSelected(IntConsumer callback) { this.onFrameSelected = callback; }

    public void refresh() {
        frameContainer.getChildren().clear();

        if (animation == null || animation.isEmpty()) {
            updatingSlider = true;
            scrubSlider.setMin(0);
            scrubSlider.setMax(0);
            scrubSlider.setValue(0);
            updatingSlider = false;
            frameCounter.setText("Frame 0 / 0 | age: 0.00");
            return;
        }

        List<FxFrame> frames = animation.getFrames();
        updatingSlider = true;
        scrubSlider.setMin(0);
        scrubSlider.setMax(Math.max(0, frames.size() - 1));
        scrubSlider.setValue(animation.getCurrentFrameIndex());
        updatingSlider = false;

        for (int i = 0; i < frames.size(); i++) {
            final int frameIndex = i;
            VBox card = createFrameCard(frameIndex, frames.get(i));
            card.setOnMouseClicked(event -> {
                animation.setCurrentFrameIndex(frameIndex);
                updateSelection();
                if (onFrameSelected != null) onFrameSelected.accept(frameIndex);
            });
            frameContainer.getChildren().add(card);
        }

        updateSelection();
    }

    public void updateSelection() {
        if (animation == null || animation.isEmpty()) {
            frameCounter.setText("Frame 0 / 0 | age: 0.00");
            return;
        }
        updatingSlider = true;
        scrubSlider.setValue(animation.getCurrentFrameIndex());
        updatingSlider = false;
        updateSelectionOnly();
    }

    private void updateSelectionOnly() {
        int total = animation.getFrameCount();
        int selected = animation.getCurrentFrameIndex();
        FxFrame current = animation.getCurrentFrame();

        frameCounter.setText(String.format(
                Locale.ROOT,
                "Frame %d / %d  |  age: %.2f / %d  |  Triângulos: %d",
                selected + 1,
                total,
                current != null ? current.getAge() : 0f,
                current != null ? current.getDuration() : 0,
                current != null ? current.getTriangleCount() : 0
        ));

        for (int i = 0; i < frameContainer.getChildren().size(); i++) {
            VBox card = (VBox) frameContainer.getChildren().get(i);
            boolean active = (i == selected);
            card.setStyle(
                    "-fx-background-color: " + (active ? "#2d4470" : "#222632") + ";" +
                    "-fx-border-color: " + (active ? "#6096ff" : "#323846") + ";" +
                    "-fx-border-radius: 4; -fx-background-radius: 4;" +
                    "-fx-border-width: " + (active ? "2" : "1") + ";"
            );
        }
    }

    private VBox createFrameCard(int index, FxFrame frame) {
        Label num = new Label(String.format(Locale.ROOT, "t=%.1f", frame.getAge()));
        num.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");

        Label tris = new Label(frame.getTriangleCount() + " tris");
        tris.setStyle("-fx-font-size: 9px; -fx-text-fill: #9ca3af;");

        VBox card = new VBox(3, num, tris);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(4, 6, 4, 6));
        card.setMinWidth(58);
        card.setMinHeight(44);
        return card;
    }
}
