package hse.java.commander;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainController {

    @FXML
    private ListView<Path> leftPane;
    @FXML
    private ListView<Path> rightPane;
    @FXML
    private Label leftPathLabel;
    @FXML
    private Label rightPathLabel;
    @FXML
    private Button btnCopy;
    @FXML
    private Button btnMove;
    @FXML
    private Button btnDelete;

    private Path leftDir;
    private Path rightDir;
    private ListView<Path> focusedPane;

    public void setInitialDirs(Path leftStart, Path rightStart) {
        if (leftStart != null) this.leftDir = leftStart;
        if (rightStart != null) this.rightDir = rightStart;
        refreshBoth();
    }

    @FXML
    public void initialize() {
        if (leftDir == null) leftDir = Paths.get(System.getProperty("user.home"));
        if (rightDir == null) rightDir = Paths.get(System.getProperty("user.home"));

        Callback<ListView<Path>, ListCell<Path>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (item.getFileName() == null) {
                        setText(item.toString());
                    } else {
                        setText(item.getFileName().toString());
                    }
                    if (Files.isDirectory(item)) {
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        };
        leftPane.setCellFactory(cellFactory);
        rightPane.setCellFactory(cellFactory);

        setupPaneInteraction(leftPane, true);
        setupPaneInteraction(rightPane, false);

        btnCopy.setOnAction(e -> transferSelected(TransferOp.COPY));
        btnMove.setOnAction(e -> transferSelected(TransferOp.MOVE));
        btnDelete.setOnAction(e -> transferSelected(TransferOp.DELETE));

        refreshBoth();
    }

    private void setupPaneInteraction(ListView<Path> pane, boolean isLeft) {
        pane.setOnMouseClicked(event -> {
            focusedPane = pane;
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Path selected = pane.getSelectionModel().getSelectedItem();
                if (selected == null) return;

                Path current = isLeft ? leftDir : rightDir;
                if (selected.equals(current.getParent())) {
                    Path parent = current.getParent();
                    if (parent != null) {
                        if (isLeft) leftDir = parent;
                        else rightDir = parent;
                        refreshBoth();
                    }
                    return;
                }
                if (Files.isDirectory(selected)) {
                    if (isLeft) leftDir = selected;
                    else rightDir = selected;
                    refreshBoth();
                }
            }
        });
    }

    private enum TransferOp { COPY, MOVE, DELETE }

    private void transferSelected(TransferOp op) {
        if (focusedPane == null) return;
        Path selected = focusedPane.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Path sourceDir = (focusedPane == leftPane) ? leftDir : rightDir;
        Path targetDir = (focusedPane == leftPane) ? rightDir : leftDir;

        if (selected.equals(sourceDir.getParent())) return;

        Path src = sourceDir.resolve(selected.getFileName());

        if (op == TransferOp.DELETE) {
            try {
                if (Files.isDirectory(src)) {
                    deleteDirectory(src);
                } else {
                    Files.deleteIfExists(src);
                }
            } catch (IOException e) {
                showError("Delete failed", e.getMessage());
            }
            refreshBoth();
            return;
        }

        Path dst = targetDir.resolve(selected.getFileName());
        try {
            if (op == TransferOp.COPY) {
                if (Files.isDirectory(src)) {
                    copyDirectory(src, dst);
                } else {
                    Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                }
            } else if (op == TransferOp.MOVE) {
                Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            showError(op == TransferOp.COPY ? "Copy failed" : "Move failed", e.getMessage());
        }
        refreshBoth();
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> walk = Files.walk(source)) {
            walk.forEach(sourcePath -> {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                try {
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to copy " + sourcePath + ": " + e.getMessage());
                }
            });
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete " + path + ": " + e.getMessage());
                        }
                    });
        }
    }

    private void refreshBoth() {
        safeRefresh(leftPane, leftDir, leftPathLabel);
        safeRefresh(rightPane, rightDir, rightPathLabel);
    }

    private void safeRefresh(ListView<Path> pane, Path dir, Label label) {
        if (dir == null) {
            pane.setItems(FXCollections.observableArrayList());
            if (label != null) label.setText("");
            return;
        }

        ObservableList<Path> items = FXCollections.observableArrayList();
        Path parent = dir.getParent();
        if (parent != null) items.add(parent);

        try (Stream<Path> stream = Files.list(dir)) {
            stream.sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                    .forEach(items::add);
        } catch (IOException e) {
        }
        pane.setItems(items);
        if (label != null) label.setText(dir.toString());
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}