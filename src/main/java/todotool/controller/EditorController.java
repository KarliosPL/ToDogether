package todotool.controller;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import todotool.ui.EditorView;
import todotool.ui.TaskRowView;

public class EditorController {

    private final EditorView view;

    public EditorController(EditorView view) {
        this.view = view;
    }

    public EditorView getView() {
        return view;
    }

    public void addTask(int index, int level, String text) {
        Platform.runLater(() -> {
            TaskRowView row = new TaskRowView(level, text, this);
            int safeIndex = Math.min(index, view.getTaskContainer().getChildren().size());
            view.getTaskContainer().getChildren().add(safeIndex, row);

            refreshListState();
            view.getTaskContainer().applyCss();
            view.getTaskContainer().layout();
            row.requestFocus();
        });
    }

    public void refreshListState() {
        Platform.runLater(() -> {
            int hideLevel = Integer.MAX_VALUE;
            var tasks = view.getTaskContainer().getChildren();

            for (int i = 0; i < tasks.size(); i++) {
                TaskRowView row = (TaskRowView) tasks.get(i);
                if (row.level <= hideLevel) hideLevel = Integer.MAX_VALUE;

                boolean isVisible = hideLevel == Integer.MAX_VALUE;
                row.setVisible(isVisible);
                row.setManaged(isVisible);
                if (isVisible && row.isFolded) hideLevel = row.level;

                row.isParent = (i + 1 < tasks.size()) && (((TaskRowView) tasks.get(i + 1)).level > row.level);
                row.foldLabel.setVisible(row.isParent);
                row.foldLabel.setManaged(row.isParent);
                row.foldLabel.setRotate(row.isFolded ? 0 : 90);
                row.foldLabel.setCursor(row.isParent ? Cursor.HAND : Cursor.DEFAULT);
                row.textField.setFont(Font.font("Segoe UI", row.isParent ? FontWeight.BOLD : FontWeight.NORMAL, row.isParent ? 15 : 14));
                row.updateCardStyle();
            }
        });
    }

    public void handleKeyPress(KeyEvent e, TaskRowView sender) {
        int idx = view.getTaskContainer().getChildren().indexOf(sender);
        boolean consumed = true;

        switch (e.getCode()) {
            case D -> {
                if (e.isControlDown()) toggleDone(idx, sender);
                else consumed = false;
            }
            case TAB -> {
                if (e.isShiftDown()) {
                    if (sender.level > 0) sender.setIndentLevel(sender.level - 1);
                } else {
                    sender.setIndentLevel(sender.level + 1);
                }
                refreshListState();
                view.scrollToNode(sender);
            }
            case ENTER -> {
                int i = idx + 1;
                while (i < view.getTaskContainer().getChildren().size() && ((TaskRowView) view.getTaskContainer().getChildren().get(i)).level > sender.level) {
                    i++;
                }
                addTask(i, sender.level, "");
            }
            case BACK_SPACE -> {
                if (sender.textField.getText().isEmpty()) {
                    removeTask(sender, idx);
                } else {
                    consumed = false;
                }
            }
            case UP -> focusRow(idx, -1);
            case DOWN -> focusRow(idx, 1);
            default -> consumed = false;
        }

        if (consumed) e.consume();
    }

    private void removeTask(TaskRowView task, int idx) {
        view.getTaskContainer().getChildren().remove(task);
        refreshListState();

        if (view.getTaskContainer().getChildren().isEmpty()) {
            view.setPlaceholderVisible(true);
        } else if (!focusRow(idx, -1)) {
            focusRow(-1, 1);
        }
    }

    private void toggleDone(int myIndex, TaskRowView sender) {
        sender.isDone = !sender.isDone;
        sender.updateTextStyle();

        for (int i = myIndex + 1; i < view.getTaskContainer().getChildren().size(); i++) {
            TaskRowView child = (TaskRowView) view.getTaskContainer().getChildren().get(i);
            if (child.level > sender.level) {
                child.isDone = sender.isDone;
                child.updateTextStyle();
            } else {
                break;
            }
        }
    }

    private boolean focusRow(int start, int dir) {
        for (int i = start + dir; i >= 0 && i < view.getTaskContainer().getChildren().size(); i += dir) {
            TaskRowView r = (TaskRowView) view.getTaskContainer().getChildren().get(i);
            if (r.isVisible()) {
                r.requestFocus();
                return true;
            }
        }
        return false;
    }
}