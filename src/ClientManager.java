import java.io.*;
import java.net.*;
import java.util.*;

public class ClientManager {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private final List<String> chatMessages = new ArrayList<>();
    private final Set<String> onlinePlayers = new HashSet<>();
    private boolean connected = false;
    private Thread listenerThread;

    public ClientManager(String playerName) {
        this.playerName = playerName;
    }

    public boolean connectToServer(String serverAddress) {
        try {
            socket = new Socket(serverAddress, 5555);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Envoyer le nom du joueur au serveur
            out.println(playerName);

            String response = in.readLine();
            if ("NAME_ACCEPTED".equals(response)) {
                connected = true;

                // Démarrer un thread pour écouter les messages du serveur
                listenerThread = new Thread(this::listenForMessages);
                listenerThread.setDaemon(true);
                listenerThread.start();

                System.out.println("Successfully connected to server as " + playerName);
                return true;
            } else {
                System.out.println("Connection failed: Name already exists or server error");
                closeConnection();
                return false;
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            closeConnection();
            return false;
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                if (message.startsWith("Online players:")) {
                    updateOnlinePlayers(message);
                } else {
                    addChatMessage(message);
                }
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("Error reading from server: " + e.getMessage());
            }
        } finally {
            closeConnection();
        }
    }

    private void updateOnlinePlayers(String message) {
        String[] parts = message.split(":");
        if (parts.length > 1) {
            onlinePlayers.clear();
            String[] players = parts[1].split(",");
            for (String player : players) {
                String trimmedPlayer = player.trim();
                if (!trimmedPlayer.isEmpty()) {
                    onlinePlayers.add(trimmedPlayer);
                }
            }
        }
    }

    public void sendMessage(String message) {
        if (connected && out != null) {
            out.println(message);
        } else {
            addChatMessage("Système: Non connecté au serveur");
        }
    }

    public void disconnect() {
        if (connected) {
            if (out != null) {
                out.println("/quit");
            }
            closeConnection();
        }
    }

    private void closeConnection() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
        socket = null;
        out = null;
        in = null;
    }

    public synchronized void addChatMessage(String message) {
        chatMessages.add(message);
        if (chatMessages.size() > 10) {
            chatMessages.remove(0);
        }
    }

    public synchronized List<String> getChatMessages() {
        return new ArrayList<>(chatMessages);
    }

    public Set<String> getOnlinePlayers() {
        return new HashSet<>(onlinePlayers);
    }

    public boolean isConnected() {
        return connected;
    }
}