package hse.java.commander;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Collectors;

public class MainController {

    @FXML
    public ListView<String> left;
    @FXML
    public ListView<String> right;
    @FXML
    public Button copy;
    @FXML
    public Button move;
    @FXML
    public Button delete;

    private Path leftDir;
    private Path rightDir;
    private ListView<String> activePanel;

    public void setInitialDirs(Path leftStart, Path rightStart) {
        this.leftDir = leftStart;
        this.rightDir = rightStart;
    }

    @FXML
    public void initialize() {
        if (leftDir == null) leftDir = Paths.get(System.getProperty("user.home"));
        if (rightDir == null) rightDir = Paths.get(System.getProperty("user.home"));

        left.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                navigate(left);
            }
            activePanel = left;
        });
        right.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                navigate(right);
            }
            activePanel = right;
        });

        activePanel = left;

        copy.setOnAction(e -> performCopy());
        move.setOnAction(e -> performMove());
        delete.setOnAction(e -> performDelete());

        refresh(left, leftDir);
        refresh(right, rightDir);
    }

    private void navigate(ListView<String> panel) {
        String selected = panel.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Path currentDir = panel == left ? leftDir : rightDir;
        Path newDir;
        if (selected.equals("..")) {
            newDir = currentDir.getParent();
            if (newDir == null) return;
        } else {
            newDir = currentDir.resolve(selected);
            if (!Files.isDirectory(newDir)) return;
        }

        if (panel == left) {
            leftDir = newDir;
            refresh(left, leftDir);
        } else {
            rightDir = newDir;
            refresh(right, rightDir);
        }
    }

    private void refresh(ListView<String> panel, Path dir) {
        try {
            var items = Files.list(dir)
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
            items.add(0, "..");
            Platform.runLater(() -> panel.getItems().setAll(items));
        } catch (IOException e) {
            showError("Cannot read directory", e.getMessage());
        }
    }

    private void performCopy() {
        if (activePanel == null) return;
        Path sourceDir = activePanel == left ? leftDir : rightDir;
        Path targetDir = activePanel == left ? rightDir : leftDir;
        String selected = activePanel.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("..")) return;

        Path source = sourceDir.resolve(selected);
        Path target = targetDir.resolve(selected);

        try {
            if (Files.isDirectory(source)) {
                copyDirectory(source, target);
            } else {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
            refreshBoth();
        } catch (IOException e) {
            showError("Copy failed", e.getMessage());
            refreshBoth();
        }
    }

    private void performMove() {
        if (activePanel == null) return;
        Path sourceDir = activePanel == left ? leftDir : rightDir;
        Path targetDir = activePanel == left ? rightDir : leftDir;
        String selected = activePanel.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("..")) return;

        Path source = sourceDir.resolve(selected);
        Path target = targetDir.resolve(selected);

        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            refreshBoth();
        } catch (IOException e) {
            showError("Move failed", e.getMessage());
            refreshBoth();
        }
    }

    private void performDelete() {
        if (activePanel == null) return;
        Path dir = activePanel == left ? leftDir : rightDir;
        String selected = activePanel.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals("..")) return;

        Path target = dir.resolve(selected);
        try {
            if (Files.isDirectory(target)) {
                deleteDirectory(target);
            } else {
                Files.delete(target);
            }
            refreshBoth();
        } catch (IOException e) {
            showError("Delete failed", e.getMessage());
            refreshBoth();
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (var stream = Files.walk(source)) {
            stream.forEach(sourcePath -> {
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
        try (var stream = Files.walk(dir)) {
            stream.sorted((a, b) -> b.compareTo(a))
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
        refresh(left, leftDir);
        refresh(right, rightDir);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}