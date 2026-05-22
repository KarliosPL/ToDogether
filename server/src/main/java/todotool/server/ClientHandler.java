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
            sendMessage(new NetworkMessage(NetworkMessage.Action.SYNC,
                    taskManager.getAllTasks()));

            while (!socket.isClosed()) {
                NetworkMessage message = (NetworkMessage) objectInputStream.readObject();

                switch (message.action()) {
                    case ADD -> {
                        taskManager.addTask(message.todos().getFirst());
                        System.out.println("ADD message received.");
                        onBroadcast.accept(message);
                    }
                    case UPDATE -> {
                        taskManager.updateTask(message.todos().getFirst());
                        System.out.println("UPDATE message received.");
                        onBroadcast.accept(message);
                    }
                    case DELETE -> {
                        taskManager.deleteTask(message.todos().getFirst());
                        System.out.println("DELETE message received.");
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
        try {
            if (objectInputStream != null) objectInputStream.close();
            if (objectOutputStream != null) objectOutputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}