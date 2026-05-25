package todotool.client.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import todotool.client.TodoCell;
import todotool.client.viewmodel.TodoItemViewModel;
import todotool.client.viewmodel.TodoListViewModel;

public class ClientController {

    @FXML
    private TextField newTaskInput;

    @FXML
    private ProgressBar taskProgress;

    @FXML
    private ListView<TodoItemViewModel> listView;

    private final TodoListViewModel viewModel = new TodoListViewModel();

    @FXML private Label pomodoroLabel;
    @FXML private ProgressBar pomodoroProgress;
    @FXML private Button pomodoroButton;

    @FXML private Button saveLocalButton;
    @FXML private Button loadLocalButton;

    @FXML private TextField serverIpInput;
    @FXML private Button connectButton;
    @FXML private Label connectionStatusLabel;

    private final int POMODORO_MINUTES = 25;
    private int timeSeconds = POMODORO_MINUTES * 60;
    private Timeline timeline;
    private boolean isRunning = false;



    @FXML
    public void initialize() {
        listView.setItems(viewModel.getItems());

        listView.setCellFactory(param -> new TodoCell(viewModel));

        viewModel.getItems().addListener((javafx.collections.ListChangeListener<todotool.client.viewmodel.TodoItemViewModel>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    // Dla każdego dodanego zadania (np. z bazy) podpinamy listener do checkboxa
                    for (todotool.client.viewmodel.TodoItemViewModel item : c.getAddedSubList()) {
                        item.completedProperty().addListener((obs, oldVal, newVal) -> {
                            updateTaskProgress(); // Przelicz pasek, gdy ktoś kliknie CheckBox
                        });
                    }
                }
            }
            updateTaskProgress(); // Przelicz pasek, gdy zadanie zostanie usunięte lub dodane
        });

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        updateTimerDisplay();
    }

    @FXML
    private void handleAddTask() {
        String text = newTaskInput.getText().trim();
        if (!text.isEmpty()) {
            viewModel.addNewTask(text);
            newTaskInput.clear();
        }
    }

    @FXML
    private void handlePomodoroToggle() {
        if (isRunning) {
            timeline.pause();
            pomodoroButton.setText("Wznów");
            isRunning = false;
        } else {
            timeline.play();
            pomodoroButton.setText("Pauza");
            isRunning = true;
        }
    }

    private void updateTimer() {
        timeSeconds--;
        updateTimerDisplay();

        double totalSeconds = POMODORO_MINUTES * 60;
        double progress = (totalSeconds - timeSeconds) / totalSeconds;
        pomodoroProgress.setProgress(progress);

        if (timeSeconds <= 0) {
            timeline.stop();
            isRunning = false;
            pomodoroLabel.setText("Czas minął!");
            pomodoroButton.setText("Zacznij od nowa");
            timeSeconds = POMODORO_MINUTES * 60; // Reset dla kolejnego cyklu
            pomodoroProgress.setProgress(0.0);
        }
    }
        private void updateTimerDisplay() {
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        pomodoroLabel.setText(String.format("Pomodoro: %02d:%02d", minutes, seconds));
        }

    private void updateTaskProgress() {
        int totalTasks = viewModel.getItems().size();

        if (totalTasks == 0) {
            taskProgress.setProgress(0.0);
            return;
        }

        long completedCount = viewModel.getItems().stream()
                .filter(item -> item.completedProperty().get())
                .count();
        double progress = (double) completedCount / totalTasks;
        taskProgress.setProgress(progress);
    }

    @FXML
    private void handleConnectToServer() {
        String ip = serverIpInput.getText().trim();
        if (!ip.isEmpty()) {
            connectionStatusLabel.setText("Łączenie...");
            connectionStatusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 11px; -fx-font-weight: bold;");

            viewModel.connectToServer(ip);

            connectionStatusLabel.setText("Online");
            connectionStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: bold;");

            connectButton.setDisable(true);
            serverIpInput.setDisable(true);

            saveLocalButton.setDisable(true);
            loadLocalButton.setDisable(true);
        }



    }

    @FXML
    private void handleSaveLocal() {
        viewModel.saveLocalData();

        connectionStatusLabel.setText("Zapisano lokalnie!");
        connectionStatusLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-font-weight: bold;"); // Kolor niebieski
    }

    @FXML
    private void handleLoadLocal() {
        viewModel.loadLocalData();

        updateTaskProgress();

        connectionStatusLabel.setText("Wczytano z dysku!");
        connectionStatusLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-font-weight: bold;");
    }
}
