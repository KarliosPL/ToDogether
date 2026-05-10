package todotool.client.model;

public class Task {
    public String text;
    public boolean completed;

    public Task(String text, boolean completed) {
        this.text = text;
        this.completed = completed;
    }

    public static Task createEmpty() {
        return new Task("", false);
    }
}