package com.fxstudio.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.nio.file.Path;
import java.util.function.Consumer;

public class FxEditor extends BorderPane {

    private final TextArea lineNumbers = new TextArea("1");
    private final TextArea codeArea = new TextArea();
    private final Label fileLabel = new Label("Nenhum arquivo aberto");
    private final Label statusLabel = new Label("Pronto");

    private Path currentFile;
    private Consumer<String> onContentChanged;
    private boolean internalUpdate = false;

    public FxEditor() {
        createLayout();
        configureEditor();
    }

    private void createLayout() {
        setStyle("-fx-background-color: #11141c;");

        Label editorTitle = new Label("JAVA SOURCE EDITOR");
        editorTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #9ca3af;");

        fileLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #ff9e5e;");

        HBox header = new HBox(12, editorTitle, fileLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(8, 12, 8, 12));
        header.setStyle("-fx-background-color: #191e29; -fx-border-color: #292e3a; -fx-border-width: 0 0 1 0;");

        lineNumbers.setEditable(false);
        lineNumbers.setFocusTraversable(false);
        lineNumbers.setPrefWidth(48);
        lineNumbers.setMaxWidth(48);
        lineNumbers.setStyle(
                "-fx-control-inner-background: #141822;" +
                        "-fx-background-color: #141822;" +
                        "-fx-text-fill: #5c667a;" +
                        "-fx-font-family: 'Consolas';" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8px 4px 8px 4px;");

        codeArea.setStyle(
                "-fx-control-inner-background: #11141c;" +
                        "-fx-background-color: #11141c;" +
                        "-fx-text-fill: #e2e8f0;" +
                        "-fx-highlight-fill: #354b78;" +
                        "-fx-highlight-text-fill: white;" +
                        "-fx-font-family: 'Consolas';" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 8px;");
        codeArea.setWrapText(false);

        codeArea.scrollTopProperty()
                .addListener((obs, oldVal, newVal) -> lineNumbers.setScrollTop(newVal.doubleValue()));

        HBox editorBody = new HBox(lineNumbers, codeArea);
        HBox.setHgrow(codeArea, Priority.ALWAYS);

        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        HBox footer = new HBox(statusLabel);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(5, 12, 5, 12));
        footer.setStyle("-fx-background-color: #191e29; -fx-border-color: #292e3a; -fx-border-width: 1 0 0 0;");

        setTop(header);
        setCenter(editorBody);
        setBottom(footer);
    }

    private void configureEditor() {
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updateLineNumbers(newValue);
            if (!internalUpdate) {
                statusLabel.setText("Modificado (não salvo)");
                statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #fbbf24;");
                if (onContentChanged != null)
                    onContentChanged.accept(newValue);
            }
        });
    }

    private void updateLineNumbers(String text) {
        int lines = text == null || text.isEmpty() ? 1 : text.split("\\R", -1).length;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append(i).append('\n');
        }
        lineNumbers.setText(sb.toString());
        lineNumbers.setScrollTop(codeArea.getScrollTop());
    }

    public void setCode(String code) {
        internalUpdate = true;
        codeArea.setText(code == null ? "" : code);
        updateLineNumbers(code);
        statusLabel.setText("Pronto");
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        internalUpdate = false;
    }

    public String getCode() {
        return codeArea.getText();
    }

    public void openFile(Path file, String content) {
        currentFile = file;
        setCode(content);
        fileLabel.setText(file == null ? "Nenhum arquivo aberto" : file.getFileName().toString());
    }

    public Path getCurrentFile() {
        return currentFile;
    }

    public void markSaved() {
        statusLabel.setText("Salvo");
        statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #34d399;");
    }

    public void setOnContentChanged(Consumer<String> callback) {
        this.onContentChanged = callback;
    }
}
