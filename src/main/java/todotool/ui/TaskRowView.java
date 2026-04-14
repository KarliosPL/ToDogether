package todotool.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import todotool.controller.EditorController;

public class TaskRowView extends HBox {

    public int level;
    public boolean isFolded, isParent, isDone;

    public final Region indentSpacer = new Region();
    public final HBox card;
    public final TextField textField;
    public final Label foldLabel;

    private final EditorController controller;

    public TaskRowView(int level, String text, EditorController controller) {
        this.level = level;
        this.controller = controller;
        this.indentSpacer.setPrefWidth(level * 25);

        foldLabel = new Label("\u276F");
        foldLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        foldLabel.setTextFill(Color.web("#333333"));
        foldLabel.setMinWidth(16);
        foldLabel.setAlignment(Pos.CENTER);
        foldLabel.setOnMouseClicked(e -> {
            if (foldLabel.isVisible()) {
                isFolded = !isFolded;
                controller.refreshListState();
            }
        });

        textField = new TextField(text);
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.focusedProperty().addListener((obs, o, focus) -> {
            updateCardStyle();
            if (focus) controller.getView().scrollToNode(this);
        });

        card = new HBox(5, foldLabel, textField);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(4, 6, 4, 6));

        card.prefWidthProperty().bind(controller.getView().getResponsiveCardWidth());
        card.setMaxWidth(USE_PREF_SIZE);

        card.setOnMouseClicked(e -> requestFocus());

        getChildren().addAll(indentSpacer, card);
        setAlignment(Pos.CENTER_LEFT);

        updateTextStyle();
        setupKeys();
    }

    public void updateTextStyle() {
        textField.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-text-fill: " + (isDone ? "#228B22" : "#111111") + ";");
    }

    public void updateCardStyle() {
        boolean focus = textField.isFocused();
        card.setStyle("-fx-background-color: " + (focus ? "#FFFFFF" : (isParent ? "#E0E0E0" : "#FAFAFA")) +
                "; -fx-background-radius: 3; -fx-border-color: " + (focus ? "#1a1aff" : "transparent") +
                "; -fx-border-width: 1.5; -fx-border-radius: 3;");
    }

    private void setupKeys() {
        textField.addEventFilter(KeyEvent.KEY_PRESSED, e -> controller.handleKeyPress(e, this));
    }

    public void setIndentLevel(int newLevel) {
        this.level = newLevel;
        indentSpacer.setPrefWidth(level * 25);
    }

    @Override
    public void requestFocus() {
        textField.requestFocus();
        textField.positionCaret(textField.getText().length());
    }
}