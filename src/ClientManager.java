import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

public class ClientManager {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String playerName;
    private int shipType;
    private final List<String> chatMessages = new ArrayList<>();
    private final Set<String> onlinePlayers = new HashSet<>();
    private final Map<String, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();
    private final List<RemoteProjectile> remoteProjectiles = new ArrayList<>();
    private boolean connected = false;
    private Thread listenerThread;

    public void sendHitMessage(String targetPlayerName) {
        if (connected && out != null) {
            try {
                GameMessage hitMsg = GameMessage.createHitMessage(targetPlayerName);
                out.writeObject(hitMsg);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error sending hit message: " + e.getMessage());
                closeConnection();
            }
        }
    }
    // Représente un joueur distant
    public static class RemotePlayer {
        private String name;
        private int x, y;
        private int health;
        private int score;
        private int shipType;
        private Image[] sprites;

        public RemotePlayer(String name, int shipType) {
            this.name = name;
            this.shipType = shipType;
            this.x = 380;
            this.y = 150;  // Les joueurs distants commencent plus haut
            this.health = 3;
            this.score = 0;
            this.sprites = new Image[3];
            loadSprites();
        }

        private void loadSprites() {
            for (int i = 0; i < 3; i++) {
                sprites[i] = GestionRessources.getImage("/ship_" + shipType + ".png");
            }
        }

        public void update(int x, int y, int health, int score) {
            this.x = x;
            this.y = y;
            this.health = health;
            this.score = score;
        }

        public void draw(Graphics g) {
            // Dessine avec couleur différente pour distinguer du joueur local
            g.drawImage(sprites[0], x, y, 50, 60, null);

            // Nom du joueur
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(name, x, y - 20);

            // Barre de vie
            g.setColor(Color.RED);
            g.fillRect(x, y - 15, 50, 5);
            g.setColor(Color.CYAN);  // Couleur différente pour l'adversaire
            g.fillRect(x, y - 15, (int)(50 * ((double)health / 3)), 5);
        }

        public Rectangle getHitbox() {
            return new Rectangle(x, y, 50, 60);
        }

        public String getName() { return name; }
        public int getHealth() { return health; }

        public void setVerticalBounds(int i, int i1) {
        }
    }

    // Représente un projectile envoyé par un joueur distant
    public static class RemoteProjectile {
        private int x, y;
        private boolean active = true;
        private final int speed = 10;

        public RemoteProjectile(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void update() {
            y -= speed;
            if (y < 0) {
                active = false;
            }
        }

        public void draw(Graphics g) {
            g.setColor(Color.CYAN);  // Couleur différente pour les projectiles adverses
            g.fillRect(x, y, 5, 15);
        }

        public Rectangle getHitbox() {
            return new Rectangle(x, y, 5, 15);
        }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    public ClientManager(String playerName, int shipType) {
        this.playerName = playerName;
        this.shipType = shipType;
    }

    public boolean connectToServer(String serverAddress) {
        try {
            socket = new Socket(serverAddress, 5555);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Envoyer les infos du joueur au serveur
            GameMessage joinMessage = GameMessage.createJoinMessage(playerName, shipType);
            out.writeObject(joinMessage);
            out.flush();

            // Attendre la réponse du serveur
            GameMessage response = (GameMessage) in.readObject();
            if (response.getChatContent().equals("NAME_ACCEPTED")) {
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
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection error: " + e.getMessage());
            closeConnection();
            return false;
        }
    }

    private void listenForMessages() {
        try {
            while (connected) {
                GameMessage message = (GameMessage) in.readObject();

                switch (message.getType()) {
                    case PLAYER_JOIN:
                        String newPlayerName = message.getPlayerName();
                        if (!newPlayerName.equals(playerName)) {
                            onlinePlayers.add(newPlayerName);
                            remotePlayers.put(newPlayerName, new RemotePlayer(newPlayerName, message.getShipType()));
                        }
                        break;

                    case PLAYER_POSITION:
                        String posPlayerName = message.getPlayerName();
                        RemotePlayer player = remotePlayers.get(posPlayerName);
                        if (player != null) {
                            player.update(message.getX(), message.getY(), message.getHealth(), message.getScore());
                        } else if (!posPlayerName.equals(playerName)) {
                            // Si c'est un nouveau joueur non encore enregistré
                            remotePlayers.put(posPlayerName, new RemotePlayer(posPlayerName, 0));
                            onlinePlayers.add(posPlayerName);
                        }
                        break;

                    case PLAYER_SHOOT:
                        if (!message.getPlayerName().equals(playerName)) {
                            remoteProjectiles.add(new RemoteProjectile(
                                    message.getProjectileX(), message.getProjectileY()));
                        }
                        break;

                    case CHAT_MESSAGE:
                        String chatMsg = message.getPlayerName() + ": " + message.getChatContent();
                        addChatMessage(chatMsg);

                        // Vérifier si c'est un message système avec liste de joueurs
                        if (message.getPlayerName().equals("SYSTEM") &&
                                message.getChatContent().startsWith("Online players:")) {
                            updateOnlinePlayers(message.getChatContent());
                        }
                        break;

                    default:
                        // Ignorer les messages non reconnus
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
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
            String[] players = parts[1].split(",");
            for (String player : players) {
                String trimmedPlayer = player.trim();
                if (!trimmedPlayer.isEmpty() && !trimmedPlayer.equals(playerName)) {
                    onlinePlayers.add(trimmedPlayer);
                    // Si c'est un joueur qu'on ne connaît pas encore, on l'ajoute
                    if (!remotePlayers.containsKey(trimmedPlayer)) {
                        remotePlayers.put(trimmedPlayer, new RemotePlayer(trimmedPlayer, 0));
                    }
                }
            }
        }
    }

    public void sendPosition(int x, int y, int health, int score) {
        if (connected && out != null) {
            try {
                GameMessage posMsg = GameMessage.createPositionMessage(playerName, x, y, health, score);
                out.writeObject(posMsg);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error sending position: " + e.getMessage());
                closeConnection();
            }
        }
    }

    public void sendProjectile(int x, int y) {
        if (connected && out != null) {
            try {
                GameMessage shootMsg = GameMessage.createShootMessage(playerName, x, y);
                out.writeObject(shootMsg);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error sending projectile: " + e.getMessage());
                closeConnection();
            }
        }
    }

    public void sendChatMessage(String content) {
        if (connected && out != null) {
            try {
                GameMessage chatMsg = GameMessage.createChatMessage(playerName, content);
                out.writeObject(chatMsg);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error sending chat: " + e.getMessage());
                closeConnection();
            }
        } else {
            addChatMessage("Système: Non connecté au serveur");
        }
    }

    public void disconnect() {
        if (connected) {
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

    public Collection<RemotePlayer> getRemotePlayers() {
        return remotePlayers.values();
    }

    public List<RemoteProjectile> getRemoteProjectiles() {
        return new ArrayList<>(remoteProjectiles);
    }

    public void updateRemoteProjectiles() {
        remoteProjectiles.removeIf(p -> !p.isActive());
        for (RemoteProjectile proj : remoteProjectiles) {
            proj.update();
        }
    }


    public boolean isConnected() {
        return connected;
    }
}