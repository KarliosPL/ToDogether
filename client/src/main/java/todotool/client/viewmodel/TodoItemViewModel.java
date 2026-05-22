package todotool.client.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import todotool.shared.Todo;

public class TodoItemViewModel {
    private final Todo todo;
    private final StringProperty text = new SimpleStringProperty();
    private final BooleanProperty completed = new SimpleBooleanProperty();

    private boolean isFocused = false;

    public TodoItemViewModel(Todo todo) {
        this.todo = todo;
        this.text.set(todo.text);
        this.completed.set(todo.completed);

        this.text.addListener((obs, old, newVal) -> this.todo.text = newVal);
        this.completed.addListener((obs, old, newVal) -> this.todo.completed = newVal);
    }

    public StringProperty textProperty() { return text; }
    public BooleanProperty completedProperty() { return completed; }
    public Todo getOriginalTodo() { return todo; }

    public boolean isFocused() { return isFocused; }
    public void setFocused(boolean focused) { isFocused = focused; }
}