package todotool.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import todotool.client.TodoCell;
import todotool.client.viewmodel.TodoItemViewModel;
import todotool.client.viewmodel.TodoListViewModel;

public class ClientController {

    @FXML
    private TextField newTaskInput;

    @FXML
    private ListView<TodoItemViewModel> listView;

    private final TodoListViewModel viewModel = new TodoListViewModel();

    @FXML
    public void initialize() {
        listView.setItems(viewModel.getItems());

        listView.setCellFactory(param -> new TodoCell(viewModel));
    }

    @FXML
    private void handleAddTask() {
        String text = newTaskInput.getText().trim();
        if (!text.isEmpty()) {
            viewModel.addNewTask(text);
            newTaskInput.clear();
        }
    }
}