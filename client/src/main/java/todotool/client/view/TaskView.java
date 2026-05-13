package todotool.client.view;

import todotool.client.controller.MainController;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import todotool.shared.Task;

public class TaskView extends HBox {

    private final Task task;
    private final MainController mainController;
    private final TextField textField;

    public TaskView(Task task, MainController mainController) {
        this.task = task;
        this.mainController = mainController;

        this.getStyleClass().add("task-view");

        this.textField = new TextField(task.text);
        this.textField.getStyleClass().add("text-field");

        HBox.setHgrow(this.textField, Priority.ALWAYS);
        this.getChildren().add(textField);

        setupListeners();
    }

    public Task getTask() {
        return task;
    }

    public void requestFieldFocus() {
        textField.requestFocus();
    }

    private void setupListeners() {
        textField.textProperty().addListener((obs, oldText, newText) -> task.text = newText);

        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.UP) {
                mainController.focusPreviousTask(this.task);
                event.consume();
            }
            else if (event.getCode() == KeyCode.DOWN) {
                mainController.focusNextTask(this.task);
                event.consume();
            }
            else if (event.getCode() == KeyCode.BACK_SPACE && textField.getText().isEmpty()) {
                mainController.removeTask(this.task);
                event.consume();
            }
        });
    }
}