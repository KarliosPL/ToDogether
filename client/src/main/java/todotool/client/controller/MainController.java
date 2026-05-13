package todotool.client.controller;

import todotool.client.view.TaskView;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import todotool.shared.Task;

import java.util.List;

public class MainController {

    @FXML private VBox mainLayout;
    @FXML private VBox tasksContainer;

    private final ObservableList<Task> tasksList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        tasksList.addListener((ListChangeListener<Task>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (int i = 0; i < change.getAddedSubList().size(); i++) {
                        Task newTask = change.getAddedSubList().get(i);
                        int index = change.getFrom() + i;

                        TaskView view = new TaskView(newTask, this);
                        tasksContainer.getChildren().add(index, view);

                        view.requestFieldFocus();
                    }
                } else if (change.wasRemoved()) {
                    tasksContainer.getChildren().remove(change.getFrom(), change.getFrom() + change.getRemovedSize());
                }
            }
        });
    }

    public void addTask(Task task) {
        Node focusedNode = mainLayout.getScene().getFocusOwner();
        TaskView focusedView = getViewFromNode(focusedNode);

        if (focusedView != null) {
            int currentIndex = tasksList.indexOf(focusedView.getTask());
            tasksList.add(currentIndex + 1, task);
        } else {
            tasksList.add(task);
        }
    }

    public void updateTask(Task updatedTask) {
        for (int i = 0; i < tasksList.size(); i++) {
            Task existingTask = tasksList.get(i);
            if (existingTask.equals(updatedTask)) {
                tasksList.set(i, updatedTask);
                return;
            }
        }
    }

    public void removeTask(Task task) {
        focusPreviousTask(task);
        tasksList.remove(task);
    }

    public void reloadTasks(List<Task> tasks) {
        tasksList.removeAll();
        tasksList.addAll(tasks);
    }

    private TaskView getViewFromNode(Node node) {
        if (node == null) return null;
        if (node.getParent() instanceof TaskView) return (TaskView) node.getParent();
        return null;
    }

    public void focusNextTask(Task currentTask) {
        int index = tasksList.indexOf(currentTask);
        if (index < tasksList.size() - 1) {
            TaskView view = (TaskView) tasksContainer.getChildren().get(index + 1);
            view.requestFieldFocus();
        }
    }

    public void focusPreviousTask(Task currentTask) {
        int index = tasksList.indexOf(currentTask);
        if (index > 0) {
            TaskView view = (TaskView) tasksContainer.getChildren().get(index - 1);
            view.requestFieldFocus();
        }
    }
}