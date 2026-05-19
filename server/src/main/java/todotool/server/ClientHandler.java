package todotool.server;

import todotool.shared.NetworkMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private TaskManager taskManager;
    private Consumer<NetworkMessage> onBroadcast;

    public ClientHandler(Socket socket, TaskManager taskManager, Consumer<NetworkMessage> onBroadcast)
            throws IOException {
        this.socket = socket;
        this.taskManager = taskManager;
        this.onBroadcast = onBroadcast;
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            sendMessage(new NetworkMessage(NetworkMessage.Action.SYNC_ALL,
                    taskManager.getAllTasks()));

            while (!socket.isClosed()) {
                NetworkMessage message = (NetworkMessage) objectInputStream.readObject();

                switch (message.action) {
                    case ADD -> {
                        taskManager.addTask(message.task);
                        onBroadcast.accept(message);
                    }
                    case UPDATE -> {
                        taskManager.updateTask(message.task);
                        onBroadcast.accept(message);
                    }
                    case DELETE -> {
                        taskManager.deleteTask(message.task);
                        onBroadcast.accept(message);
                    }
                    default -> { }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Odłączono klienta: " + socket.getInetAddress());
        } finally {
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(NetworkMessage message) throws IOException {
        objectOutputStream.reset();
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
    }
}