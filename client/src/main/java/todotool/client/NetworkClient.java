package todotool.client;

import todotool.shared.NetworkMessage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkClient {
    private ObjectOutputStream out;

    public void connect(String host, int port, Consumer<NetworkMessage> onMessageReceived) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(host, port);

                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                System.out.println("Połączono z serwerem!");

                while (!socket.isClosed()) {
                    NetworkMessage msg = (NetworkMessage) in.readObject();
                    onMessageReceived.accept(msg);
                }
            } catch (Exception e) {
                System.err.println("Utracono połączenie: " + e.getMessage());
            }
        }).start();
    }

    public void send(NetworkMessage msg) {
        if (out != null) {
            try {
                out.reset();
                out.writeObject(msg);
                out.flush();
            } catch (java.net.SocketException e) {
                // Ten blok "wyłapuje" błąd zamkniętego gniazda podczas wyłączania aplikacji
                // i zapobiega drukowaniu wielkiego czerwonego tekstu w konsoli.
                System.out.println("Nie wysłano wiadomości (połączenie z serwerem zostało zamknięte).");
            } catch (Exception e) {
                // Zwykłe błędy nadal będą się wyświetlać
                System.err.println("Inny błąd podczas wysyłania: " + e.getMessage());
            }
        }
    }
}