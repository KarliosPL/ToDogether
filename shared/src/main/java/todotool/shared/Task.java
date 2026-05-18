package todotool.shared;

import java.io.Serializable;
import java.util.UUID;

public class Task implements Serializable {
    public UUID uuid;
    public String text;
    public boolean completed;

    public Task(UUID uuid, String text, boolean completed) {
        this.uuid = uuid;
        this.text = text;
        this.completed = completed;
    }

    public static Task createEmpty() {
        return new Task(UUID.randomUUID(), "", false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task other = (Task) o;
        return this.uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}