package todotool.server;

import todotool.shared.NetworkMessage;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 6767;
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static TaskManager taskManager;

    public static void main(String[] args) {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        taskManager = new TaskManager(databaseManager);

        System.out.println("Serwer uruchomiony na porcie " + PORT);
        ExecutorService threadPool = Executors.newFixedThreadPool(4);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                if (clients.size() < 4) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nowy klient podłączony: " + clientSocket.getInetAddress());

                    ClientHandler clientHandler = new ClientHandler(clientSocket, taskManager, Server::broadcastToAll);
                    clients.add(clientHandler);
                    threadPool.execute(clientHandler);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastToAll(NetworkMessage msg) {
        for (ClientHandler client : clients) {
            try {
                client.sendMessage(msg);
            } catch (IOException e) {
                System.err.println("Failed to broadcast: " + e.getMessage());
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Klient odłączony. Pozostało: " + clients.size());
    }
}