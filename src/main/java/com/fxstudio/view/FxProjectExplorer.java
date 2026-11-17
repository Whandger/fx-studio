package com.fxstudio.view;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FxProjectExplorer extends BorderPane {

    private final TreeView<Path> treeView = new TreeView<>();
    private Consumer<Path> onFileSelected;
    private Consumer<Path> onFileDeleted;

    public FxProjectExplorer() {
        setStyle("-fx-background-color: #161922; -fx-border-color: #292e3a; -fx-border-width: 0 1 0 0;");

        Label header = new Label("PROJECT FILES");
        header.setPadding(new Insets(9, 12, 9, 12));
        header.setMaxWidth(Double.MAX_VALUE);
        header.setStyle("-fx-background-color: #191e29; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #9ca3af; -fx-border-color: #292e3a; -fx-border-width: 0 0 1 0;");
        setTop(header);

        treeView.setShowRoot(false);
        treeView.setStyle("-fx-background-color: #161922; -fx-control-inner-background: #161922;");
        treeView.setCellFactory(view -> new TreeCell<>() {
            private final ContextMenu contextMenu = new ContextMenu();
            private final MenuItem deleteItem = new MenuItem("Delete File");

            {
                deleteItem.setStyle("-fx-text-fill: #f87171;");
                deleteItem.setOnAction(e -> {
                    Path target = getItem();
                    if (target != null) {
                        requestDelete(target);
                    }
                });
                contextMenu.getItems().add(deleteItem);
            }

            @Override
            protected void updateItem(Path path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                    return;
                }
                boolean directory = Files.isDirectory(path);
                setText(path.getFileName() == null ? path.toString() : path.getFileName().toString());
                setStyle("-fx-text-fill: " + (directory ? "#60a5fa" : "#e5e7eb") + "; -fx-font-size: 12px;");

                if (!directory && isDeletableProjectFile(path)) {
                    deleteItem.setText("Delete " + path.getFileName());
                    setContextMenu(contextMenu);
                } else {
                    setContextMenu(null);
                }
            }
        });

        treeView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                TreeItem<Path> selected = treeView.getSelectionModel().getSelectedItem();
                if (selected != null && isDeletableProjectFile(selected.getValue())) {
                    requestDelete(selected.getValue());
                    event.consume();
                }
            }
        });

        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null
                    && Files.isRegularFile(newSel.getValue())
                    && newSel.getValue().toString().endsWith(".java")
                    && onFileSelected != null) {
                onFileSelected.accept(newSel.getValue());
            }
        });

        setCenter(treeView);
    }

    private boolean isDeletableProjectFile(Path path) {
        if (path == null || !Files.isRegularFile(path)) return false;
        String name = path.getFileName().toString();
        return name.endsWith(".java") || name.endsWith(".jar");
    }

    private void requestDelete(Path target) {
        if (onFileDeleted == null || !isDeletableProjectFile(target)) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Excluir arquivo");
        alert.setHeaderText("Remover \"" + target.getFileName() + "\" do projeto?");
        alert.setContentText("O arquivo será apagado da pasta do projeto.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            onFileDeleted.accept(target);
        }
    }

    public void loadProject(Path projectDirectory) throws IOException {
        if (projectDirectory == null || !Files.isDirectory(projectDirectory)) {
            throw new IOException("Diretório do projeto inválido.");
        }
        TreeItem<Path> root = createTree(projectDirectory);
        root.setExpanded(true);
        treeView.setRoot(root);
    }

    private TreeItem<Path> createTree(Path path) throws IOException {
        TreeItem<Path> item = new TreeItem<>(path);
        item.setExpanded(true);
        if (Files.isDirectory(path)) {
            try (Stream<Path> children = Files.list(path)) {
                for (Path child : children
                        .sorted(Comparator
                                .<Path, Boolean>comparing(p -> Files.isDirectory(p))
                                .reversed()
                                .thenComparing(p -> p.getFileName().toString()))
                        .toList()) {
                    item.getChildren().add(createTree(child));
                }
            }
        }
        return item;
    }

    public void setOnFileSelected(Consumer<Path> callback) { this.onFileSelected = callback; }

    public void setOnFileDeleted(Consumer<Path> callback) { this.onFileDeleted = callback; }
}
