package todotool.client.viewmodel;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import todotool.client.NetworkClient;
import todotool.shared.NetworkMessage;
import todotool.shared.NetworkMessage.Action;
import todotool.shared.Todo;
import java.io.*;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

public class TodoListViewModel {
    private final ObservableList<TodoItemViewModel> items = FXCollections.observableArrayList();
    private final NetworkClient network = new NetworkClient();
    private static final String LOCAL_SAVE_FILE = "offline_tasks.dat";
    public static final UUID CLIENT_ID = UUID.randomUUID();
    public TodoListViewModel() {
        loadLocalData();
    }

    public ObservableList<TodoItemViewModel> getItems() { return items; }

    public void addNewTask(String text) {
        Todo newTodo = new Todo(text);
        items.add(new TodoItemViewModel(newTodo));
        network.send(new NetworkMessage(Action.ADD, CLIENT_ID, List.of(newTodo)));
    }

    public void remove(TodoItemViewModel item) {
        items.remove(item);
        network.send(new NetworkMessage(Action.DELETE, CLIENT_ID, List.of(item.getOriginalTodo())));
    }

    public void commitUpdate(TodoItemViewModel item) {
        network.send(new NetworkMessage(Action.UPDATE, CLIENT_ID, List.of(item.getOriginalTodo())));
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

                    System.out.println("[Klient PRZYCHODZĄCE] Otrzymano UPDATE. lockedBy z serwera: " + incoming.lockedBy);
                    TodoItemViewModel target = findById(incoming.uuid);
                    if (target != null) {
                        if (!target.isFocused()) {
                            target.textProperty().set(incoming.text);
                        }
                        target.completedProperty().set(incoming.completed);
                        target.lockedByProperty().set(incoming.lockedBy);
                    }
                }
                case LOCK -> {
                    Todo incoming = msg.todos().getFirst();
                    TodoItemViewModel target = findById(incoming.uuid);
                    if (target != null && !msg.senderId().equals(CLIENT_ID)) {
                        target.lockedByProperty().set(msg.senderId());
                    }
                }
                case UNLOCK -> {
                    Todo incoming = msg.todos().getFirst();
                    TodoItemViewModel target = findById(incoming.uuid);
                    if (target != null) {
                        target.lockedByProperty().set(null);
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

    public void connectToServer(String ipAccess) {
        network.connect(ipAccess, 6767, this::handleIncoming);
    }
    public void saveLocalData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(LOCAL_SAVE_FILE))) {
            List<Todo> todosToSave = new ArrayList<>();
            for (TodoItemViewModel item : items) {
                todosToSave.add(item.getOriginalTodo());
            }
            oos.writeObject(todosToSave);
        } catch (IOException e) {
            System.err.println("Nie udało się zapisać lokalnych danych: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadLocalData() {
        File file = new File(LOCAL_SAVE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<Todo> loadedTodos = (List<Todo>) ois.readObject();
                items.clear();
                for (Todo t : loadedTodos) {
                    items.add(new TodoItemViewModel(t));
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Nie udało się wczytać lokalnych danych: " + e.getMessage());
            }
        }
    }
    public void sendLock(TodoItemViewModel item) {
        network.send(new NetworkMessage(Action.LOCK, CLIENT_ID, List.of(item.getOriginalTodo())));
    }

    public void sendUnlock(TodoItemViewModel item) {
        network.send(new NetworkMessage(Action.UNLOCK, CLIENT_ID, List.of(item.getOriginalTodo())));
    }

}