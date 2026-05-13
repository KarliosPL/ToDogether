package todotool.client;

import todotool.client.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import todotool.shared.NetworkMessage;
import todotool.shared.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();

        MainController mainController = loader.getController();

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Todotool");
        primaryStage.setScene(scene);
        primaryStage.show();

        try {
            Socket socket = new Socket("localhost", 1337);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    Task newTask = Task.createEmpty();
                    try {
                        objectOutputStream.reset();
                        objectOutputStream.writeObject(new NetworkMessage(NetworkMessage.Action.ADD, newTask));
                        objectOutputStream.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    keyEvent.consume();
                }
            });

            Thread networkThread = getThread(objectInputStream, mainController);
            networkThread.start();
        } catch (IOException e) {
            System.out.println("Nie udało się połączyć z serwerem.");
        }
    }

    private static Thread getThread(ObjectInputStream objectInputStream, MainController mainController) {
        Thread networkThread = new Thread(() -> {
            try {
                while (true) {
                    NetworkMessage networkMessage =
                            (NetworkMessage) objectInputStream.readObject();

                    switch (networkMessage.action) {
                        case SYNC_ALL -> {
                            System.out.println("SYNC");
                            javafx.application.Platform.runLater(() ->
                                    mainController.reloadTasks(networkMessage.allTasks)
                            );
                        }

                        case ADD -> {
                            javafx.application.Platform.runLater(() ->
                                    mainController.addTask(networkMessage.task)
                            );
                        }

                        case UPDATE -> {
                            javafx.application.Platform.runLater(() ->
                                    mainController.updateTask(networkMessage.task)
                            );
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

        networkThread.setDaemon(true);
        return networkThread;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
