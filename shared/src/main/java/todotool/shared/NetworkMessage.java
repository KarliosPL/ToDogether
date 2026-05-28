package todotool.shared;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record NetworkMessage(Action action, UUID senderId, List<Todo> todos) implements Serializable {
    public enum Action {
        SYNC, ADD, UPDATE, DELETE, LOCK, UNLOCK
    }
}