package todotool.ui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import todotool.controller.EditorController;

public class EditorView {

    public static final double LINE_HEIGHT = 30.5;

    private final StackPane root;
    private final VBox taskContainer;
    private final Pane viewport;
    private final Label placeholderLabel;

    private final DoubleBinding responsiveCardWidth;
    private EditorController controller;

    public EditorView() {
        taskContainer = new VBox(1);
        taskContainer.setPadding(new Insets(100, 0, 300, 0));

        viewport = new Pane(taskContainer);
        viewport.setStyle("-fx-background-color: #EAEAEA;");

        responsiveCardWidth = Bindings.createDoubleBinding(
                () -> Math.max(300.0, viewport.getWidth() * 0.95),
                viewport.widthProperty()
        );

        viewport.widthProperty().addListener((obs, o, n) ->
                taskContainer.setLayoutX(Math.max(0, (n.doubleValue() - responsiveCardWidth.get()) / 2))
        );

        Label hint = new Label("Scroll: Up/Down | Tab: Indent | Ctrl+D: Done");
        hint.setFont(Font.font("Segoe UI", 12));
        hint.setTextFill(Color.web("#777777"));
        hint.setPadding(new Insets(10));

        placeholderLabel = new Label("Naciśnij ENTER, aby dodać nowe zadanie");
        placeholderLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        placeholderLabel.setTextFill(Color.web("#666666"));
        placeholderLabel.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-padding: 10 20; -fx-background-radius: 20;");

        root = new StackPane(viewport, placeholderLabel, hint);
        StackPane.setAlignment(hint, Pos.TOP_RIGHT);
        StackPane.setAlignment(placeholderLabel, Pos.CENTER);

        setupScrolling();
    }

    public void setController(EditorController controller) {
        this.controller = controller;
    }

    public StackPane getRoot() { return root; }
    public VBox getTaskContainer() { return taskContainer; }
    public DoubleBinding getResponsiveCardWidth() { return responsiveCardWidth; }

    public void setPlaceholderVisible(boolean visible) {
        placeholderLabel.setVisible(visible);
        if (visible) {
            viewport.requestFocus();
            taskContainer.setTranslateY(0);
        }
    }

    private void setupScrolling() {
        final double[] scrollAccumulator = {0.0};
        final double SCROLL_THRESHOLD = 40.0;

        viewport.setOnScroll(e -> {
            scrollAccumulator[0] += e.getDeltaY();

            if (Math.abs(scrollAccumulator[0]) >= SCROLL_THRESHOLD) {
                double direction = Math.signum(scrollAccumulator[0]);
                taskContainer.setTranslateY(Math.round((taskContainer.getTranslateY() + direction * LINE_HEIGHT) / LINE_HEIGHT) * LINE_HEIGHT);
                scrollAccumulator[0] -= direction * SCROLL_THRESHOLD;
            }
            e.consume();
        });
    }

    public void setupGlobalKeyBindings(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (taskContainer.getChildren().isEmpty() && e.getCode() == KeyCode.ENTER) {
                setPlaceholderVisible(false);
                controller.addTask(0, 0, "");
                e.consume();
            }
        });
    }

    public void scrollToNode(Region node) {
        Platform.runLater(() -> {
            if (node.getScene() == null) return;

            node.getParent().layout();
            taskContainer.layout();

            Bounds b = node.localToScene(node.getBoundsInLocal());
            double pad = LINE_HEIGHT, diff = 0;

            if (b.getMinY() < pad) diff = pad - b.getMinY();
            else if (b.getMaxY() > viewport.getHeight() - pad) diff = viewport.getHeight() - pad - b.getMaxY();

            taskContainer.setTranslateY(Math.round((taskContainer.getTranslateY() + diff) / LINE_HEIGHT) * LINE_HEIGHT);
        });
    }
}