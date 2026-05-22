package todotool.client.viewmodel;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import todotool.client.NetworkClient;
import todotool.shared.NetworkMessage;
import todotool.shared.NetworkMessage.Action;
import todotool.shared.Todo;

import java.util.List;
import java.util.UUID;

public class TodoListViewModel {
    private final ObservableList<TodoItemViewModel> items = FXCollections.observableArrayList();
    private final NetworkClient network = new NetworkClient();

    public TodoListViewModel() {
        network.connect("localhost", 2137, this::handleIncoming);
    }

    public ObservableList<TodoItemViewModel> getItems() { return items; }

    public void addNewTask(String text) {
        Todo newTodo = new Todo(text);
        items.add(new TodoItemViewModel(newTodo));
        network.send(new NetworkMessage(Action.ADD, List.of(newTodo)));
    }

    public void remove(TodoItemViewModel item) {
        items.remove(item);
        network.send(new NetworkMessage(Action.DELETE, List.of(item.getOriginalTodo())));
    }

    public void commitUpdate(TodoItemViewModel item) {
        network.send(new NetworkMessage(Action.UPDATE, List.of(item.getOriginalTodo())));
    }

    private void handleIncoming(NetworkMessage msg) {
        Platform.runLater(() -> {
            switch (msg.action()) {
                case SYNC -> {
                    items.clear();
                    for (Todo t : msg.todos()) {
                        items.add(new TodoItemViewModel(t));
                    }
                }
                case ADD -> {
                    Todo incoming = msg.todos().getFirst();
                    if (findById(incoming.uuid) == null) {
                        items.add(new TodoItemViewModel(incoming));
                    }
                }
                case DELETE -> {
                    Todo incoming = msg.todos().getFirst();
                    items.removeIf(i -> i.getOriginalTodo().uuid.equals(incoming.uuid));
                }
                case UPDATE -> {
                    Todo incoming = msg.todos().getFirst();
                    TodoItemViewModel target = findById(incoming.uuid);
                    if (target != null) {
                        if (!target.isFocused()) {
                            target.textProperty().set(incoming.text);
                        }
                        target.completedProperty().set(incoming.completed);
                    }
                }
            }
        });
    }

    private TodoItemViewModel findById(UUID uuid) {
        return items.stream()
                .filter(i -> i.getOriginalTodo().uuid.equals(uuid))
                .findFirst()
                .orElse(null);
    }
}