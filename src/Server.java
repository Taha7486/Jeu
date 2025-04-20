import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class Server {
    private static final int PORT = 5555;
    private static final Set<ClientHandler> clients = new CopyOnWriteArraySet<>();
    private static final Set<String> playerNames = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientThread = new ClientHandler(clientSocket);
                clients.add(clientThread);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized boolean isNameTaken(String name) {
        return playerNames.contains(name);
    }

    public static synchronized void broadcast(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    public static synchronized void addPlayerName(String name) {
        playerNames.add(name);
        updatePlayerList();
    }

    public static synchronized void removeClient(ClientHandler client, String name) {
        clients.remove(client);
        playerNames.remove(name);
        System.out.println("Player " + name + " disconnected");
        broadcast(name + " a quitté le jeu", null);
        updatePlayerList();
    }

    public static synchronized void updatePlayerList() {
        String playerList = "Online players: " + String.join(",", playerNames);
        for (ClientHandler client : clients) {
            client.sendMessage(playerList);
        }
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private boolean running = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Étape 1: Recevoir le nom du joueur
            playerName = in.readLine();
            if (Server.isNameTaken(playerName)) {
                out.println("NAME_EXISTS");
                socket.close();
                return;
            } else {
                out.println("NAME_ACCEPTED");
                Server.addPlayerName(playerName);
                Server.broadcast(playerName + " a rejoint le jeu", this);
            }

            String clientMessage;
            while (running && (clientMessage = in.readLine()) != null) {
                if (clientMessage.equals("/quit")) {
                    break;
                }
                Server.broadcast(playerName + ": " + clientMessage, this);
            }
        } catch (IOException e) {
            System.err.println("Error handling client " + playerName + ": " + e.getMessage());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
            Server.removeClient(this, playerName);
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void stopRunning() {
        this.running = false;
    }
}