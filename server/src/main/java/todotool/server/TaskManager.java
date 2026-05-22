package todotool.server;

import todotool.shared.Todo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaskManager {
    private List<Todo> todos = new CopyOnWriteArrayList<>();
    private DatabaseManager databaseManager;

    public TaskManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.todos.addAll(databaseManager.getAllTasks());
    }

    public void addTask(Todo todo) {
        todos.add(todo);
        databaseManager.insertTask(todo);
    }

    public void updateTask(Todo todo) {
        int index = todos.indexOf(todo);
        if (index >= 0) {
            todos.set(index, todo);
            databaseManager.updateTask(todo);
        }
    }

    public void deleteTask(Todo todo) {
        if (todos.remove(todo)) {
            databaseManager.deleteTask(todo);
        }
    }

    public List<Todo> getAllTasks() {
        return new ArrayList<>(todos);
    }
}