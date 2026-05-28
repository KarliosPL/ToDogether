package todotool.shared;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Todo implements Serializable {
    private static final long serialVersionUID = 5L;

    public UUID uuid; // Notice we use UUID to match your database
    public String text;
    public boolean completed;
    public UUID lockedBy;

    public Todo(UUID uuid, String text, boolean completed, UUID lockedBy) {
        this.uuid = uuid;
        this.text = text;
        this.completed = completed;
        this.lockedBy = lockedBy;
    }

    public Todo(String text) {
        this(UUID.randomUUID(), text, false, null);;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Todo todo = (Todo) o;
        return Objects.equals(uuid, todo.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}