package todotool.server;

import todotool.shared.NetworkMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private TaskManager taskManager;
    private Consumer<NetworkMessage> onBroadcast;
    private UUID clientId;

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
            sendMessage(new NetworkMessage(NetworkMessage.Action.SYNC, null, taskManager.getAllTasks()));

            while (!socket.isClosed()) {
                NetworkMessage message = (NetworkMessage) objectInputStream.readObject();

                if (this.clientId == null && message.senderId() != null) {
                    this.clientId = message.senderId();
                }

                switch (message.action()) {
                    case ADD -> {
                        taskManager.addTask(message.todos().getFirst());
                        onBroadcast.accept(message);
                    }
                    case UPDATE -> {
                        taskManager.updateTask(message.todos().getFirst());
                        onBroadcast.accept(message);
                    }
                    case DELETE -> {
                        taskManager.deleteTask(message.todos().getFirst());
                        onBroadcast.accept(message);
                    }
                    case LOCK, UNLOCK -> {
                        onBroadcast.accept(message);
                    }
                    default -> { }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Odłączono klienta: " + socket.getInetAddress());
        } finally {
            cleanup();
        }
    }

    public void sendMessage(NetworkMessage message) throws IOException {
        objectOutputStream.reset();
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
    }

    private void cleanup() {
        Server.removeClient(this); // Remove from the global list

        if (this.clientId != null && this.taskManager != null) {
            java.util.List<todotool.shared.Todo> unlockedTasks = taskManager.unlockTasksForClient(this.clientId);

            if (!unlockedTasks.isEmpty()) {
                System.out.println("Zwalniam porzucone blokady klienta: " + this.clientId);
                onBroadcast.accept(new NetworkMessage(NetworkMessage.Action.UPDATE, null, unlockedTasks));
            }
        }
        try {
            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}