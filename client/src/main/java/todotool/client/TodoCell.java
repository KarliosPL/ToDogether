package todotool.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import todotool.client.viewmodel.TodoItemViewModel;
import todotool.client.viewmodel.TodoListViewModel;

import java.io.IOException;

public class TodoCell extends ListCell<TodoItemViewModel> {

    @FXML private HBox root;
    @FXML private CheckBox checkBox;
    @FXML private TextField textField;
    @FXML private Button deleteButton;

    public TodoCell(TodoListViewModel viewModel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/todo-cell.fxml"));
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
        }

        deleteButton.setOnAction(e -> {
            if (getItem() != null) {
                viewModel.remove(getItem());
            }
        });

        checkBox.setOnAction(e -> {
            if (getItem() != null) {
                viewModel.commitUpdate(getItem());
            }
        });

        textField.focusedProperty().addListener((obs, old, isFocused) -> {
            if (getItem() != null) {
                getItem().setFocused(isFocused);
                if (!isFocused) {
                    viewModel.commitUpdate(getItem());
                }
            }
        });
    }

    @Override
    protected void updateItem(TodoItemViewModel item, boolean empty) {
        if (getItem() != null) {
            textField.textProperty().unbindBidirectional(getItem().textProperty());
            checkBox.selectedProperty().unbindBidirectional(getItem().completedProperty());
        }

        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            textField.textProperty().bindBidirectional(item.textProperty());
            checkBox.selectedProperty().bindBidirectional(item.completedProperty());

            setGraphic(root);
        }
    }
}