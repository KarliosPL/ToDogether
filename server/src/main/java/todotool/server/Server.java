package todotool.server;

import todotool.shared.NetworkMessage;
import todotool.shared.Task;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 1337;

    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    private static final List<Task> serverTasks = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("Serwer uruchomiony na porcie " + PORT);

        ExecutorService threadPool = Executors.newFixedThreadPool(4);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowy klient podłączony: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            try {
                sendMessage(new NetworkMessage(NetworkMessage.Action.SYNC_ALL, serverTasks));
                while (!socket.isClosed()) {
                    NetworkMessage networkMessage = (NetworkMessage) objectInputStream.readObject();
                    switch (networkMessage.action) {
                        case ADD -> {
                            serverTasks.add(networkMessage.task);
                            broadcast(new NetworkMessage(NetworkMessage.Action.ADD, networkMessage.task));
                        }
                        case UPDATE -> {
                        }
                        case DELETE -> {
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        public void sendMessage(NetworkMessage msg) throws IOException {
            objectOutputStream.reset();
            objectOutputStream.writeObject(msg);
            objectOutputStream.flush();
        }
    }

    public static void broadcast(NetworkMessage msg) {
        for (ClientHandler client : clients) {
            try {
                client.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}