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

        DatabaseManager.initDatabase();
        serverTasks.addAll(DatabaseManager.getAllTasks());

        System.out.println("Serwer uruchomiony na porcie " + PORT);
        System.out.println("Wczytano zadania z bazy: " + serverTasks.size());
        ExecutorService threadPool = Executors.newFixedThreadPool(4);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                if(clients.size() >= 4) {
                    System.out.println("Odrzucono połączenie: Osiągnięto limit 4 klientów. IP: " + clientSocket.getInetAddress());
                    clientSocket.close();
                    continue;
                }

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
                            DatabaseManager.insertTask(networkMessage.task);
                            broadcast(new NetworkMessage(NetworkMessage.Action.ADD, networkMessage.task));
                        }
                        case UPDATE -> {
                            int index = serverTasks.indexOf(networkMessage.task);
                            if (index != -1) {
                                serverTasks.set(index, networkMessage.task);
                                DatabaseManager.updateTask(networkMessage.task);
                                broadcast(new NetworkMessage(NetworkMessage.Action.UPDATE, networkMessage.task));
                            }
                        }
                        case DELETE -> {
                            if (serverTasks.remove(networkMessage.task)) {
                                DatabaseManager.deleteTask(networkMessage.task);
                                broadcast(new NetworkMessage(NetworkMessage.Action.DELETE, networkMessage.task));
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Klient " + socket.getInetAddress() + " rozłączył się");

            } finally {
                clients.remove(this);
                try {
                    if(!socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
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