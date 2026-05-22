package todotool.shared;

import java.io.Serializable;
import java.util.List;

public record NetworkMessage(Action action, List<Todo> todos) implements Serializable {
    public enum Action {
        SYNC, ADD, UPDATE, DELETE
    }
}