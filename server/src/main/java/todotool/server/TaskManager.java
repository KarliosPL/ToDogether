package todotool.server;

import todotool.shared.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaskManager {
    private List<Task> tasks = new CopyOnWriteArrayList<>();
    private DatabaseManager databaseManager;

    public TaskManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.tasks.addAll(databaseManager.getAllTasks());
    }

    public void addTask(Task task) {
        tasks.add(task);
        databaseManager.insertTask(task);
    }

    public void updateTask(Task task) {
        int index = tasks.indexOf(task);
        if (index >= 0) {
            tasks.set(index, task);
            databaseManager.updateTask(task);
        }
    }

    public void deleteTask(Task task) {
        if (tasks.remove(task)) {
            databaseManager.deleteTask(task);
        }
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }
}