package com.fxstudio.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FxConsole extends BorderPane {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final TextArea output = new TextArea();
    private final Label statusLabel = new Label("READY");

    public FxConsole() {
        setStyle("-fx-background-color: #0e1118; -fx-border-color: #292e3a; -fx-border-width: 1 0 0 0;");

        Label title = new Label("COMPILER & EXECUTION CONSOLE");
        title.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #9ca3af;");

        statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #34d399;");
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        Button clearButton = new Button("Clear");
        clearButton.setFocusTraversable(false);
        clearButton.setStyle("-fx-background-color: #262b38; -fx-text-fill: #d1d5db; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
        clearButton.setOnAction(event -> clear());

        HBox header = new HBox(10, title, statusLabel, clearButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(6, 12, 6, 12));
        header.setStyle("-fx-background-color: #191e29;");

        output.setEditable(false);
        output.setWrapText(true);
        output.setStyle(
                "-fx-control-inner-background: #0e1118;" +
                "-fx-background-color: #0e1118;" +
                "-fx-text-fill: #d1d5db;" +
                "-fx-font-family: 'Consolas';" +
                "-fx-font-size: 12px;"
        );

        setTop(header);
        setCenter(output);
    }

    public void info(String message) { append("INFO", message); }

    public void success(String message) {
        append("OK", message);
        statusLabel.setText("SUCCESS");
        statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #34d399;");
    }

    public void warning(String message) {
        append("WARN", message);
        statusLabel.setText("WARNING");
        statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #fbbf24;");
    }

    public void error(String message) {
        append("ERROR", message);
        statusLabel.setText("ERROR");
        statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #f87171;");
    }

    public void clear() {
        output.clear();
        statusLabel.setText("READY");
        statusLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #34d399;");
    }

    private void append(String level, String message) {
        String timestamp = LocalTime.now().format(TIME_FORMAT);
        output.appendText("[" + timestamp + "] [" + level + "] " + message + System.lineSeparator());
        output.positionCaret(output.getLength());
    }
}
