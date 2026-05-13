package todotool.shared;

import java.io.Serializable;
import java.util.List;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Action {
        SYNC_ALL,
        ADD,
        UPDATE,
        DELETE
    }

    public Action action;
    public Task task;
    public List<Task> allTasks;

    public NetworkMessage(Action action, Task task) {
        this.action = action;
        this.task = task;
    }

    public NetworkMessage(Action action, List<Task> allTasks) {
        this.action = action;
        this.allTasks = allTasks;
    }
}