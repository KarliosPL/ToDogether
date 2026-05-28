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
import javafx.beans.binding.Bindings;
import java.util.UUID;

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

        checkBox.focusedProperty().addListener((obs, old, isFocused) -> {
            if (getItem() != null) {
                getItem().setFocused(isFocused);
                if (isFocused) {
                    getItem().lockedByProperty().set(TodoListViewModel.CLIENT_ID);
                    viewModel.sendLock(getItem());
                } else {
                    getItem().lockedByProperty().set(null);
                    viewModel.sendUnlock(getItem());
                    viewModel.commitUpdate(getItem());
                }
            }
        });

        textField.focusedProperty().addListener((obs, old, isFocused) -> {
            if (getItem() != null) {
                getItem().setFocused(isFocused);
                if (isFocused) {
                    getItem().lockedByProperty().set(TodoListViewModel.CLIENT_ID);
                    viewModel.sendLock(getItem());
                } else {
                    getItem().lockedByProperty().set(null);
                    viewModel.sendUnlock(getItem());
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
            checkBox.disableProperty().unbind();
            root.styleProperty().unbind();
            root.setStyle("");
        }

        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            textField.textProperty().bindBidirectional(item.textProperty());
            checkBox.selectedProperty().bindBidirectional(item.completedProperty());

            var isLockedByOther = Bindings.createBooleanBinding(() -> {
                UUID currentLock = item.lockedByProperty().get();
                return currentLock != null && !currentLock.equals(TodoListViewModel.CLIENT_ID);
            }, item.lockedByProperty());

            checkBox.disableProperty().bind(isLockedByOther);
            textField.disableProperty().bind(isLockedByOther);
            deleteButton.disableProperty().bind(isLockedByOther);

            root.styleProperty().bind(Bindings.when(isLockedByOther)
                    .then("-fx-opacity: 0.6; -fx-background-color: #fee2e2;")
                    .otherwise(""));

            setGraphic(root);

            setGraphic(root);
        }
    }
}