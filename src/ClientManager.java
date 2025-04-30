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

    // Représente un joueur distant
    public static class RemotePlayer {
        private String name;
        private int x, y;
        private int health;
        private int score;
        private int shipType;
        private Image[] sprites;
        private int minY, maxY; // Ajoutez ces champs

        public void setVerticalBounds(int minY, int maxY) {
            this.minY = minY;
            this.maxY = maxY;
        }


        public RemotePlayer(String name, int shipType) {
            this.name = name;
            this.shipType = shipType;
            this.x = 380;
            this.y = 150;  // Changé de 450 à 150 pour être en haut
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
            // Appliquer les limites verticales
            this.y = Math.max(minY, Math.min(maxY, y));
            this.health = health;
            this.score = score;
        }


        public void draw(Graphics g) {
            if (health <= 0) return; // Ne pas dessiner les joueurs morts

            // Dessiner le vaisseau avec une légère rotation
            g.drawImage(sprites[0], x, y, 50, 60, null);

            // Nom du joueur avec un fond semi-transparent
            String displayName = name + " (" + score + ")";
            Font nameFont = new Font("Arial", Font.BOLD, 14);
            g.setFont(nameFont);
            int nameWidth = g.getFontMetrics().stringWidth(displayName);
            
            // Fond semi-transparent pour le nom
            g.setColor(new Color(0, 0, 0, 128));
            g.fillRect(x + 25 - nameWidth/2 - 2, y - 35, nameWidth + 4, 20);
            
            // Nom du joueur
            g.setColor(Color.YELLOW);
            g.drawString(displayName, x + 25 - nameWidth/2, y - 20);

            // Barre de vie avec contour
            int healthBarWidth = 50;
            int healthBarHeight = 5;
            // Fond noir
            g.setColor(Color.BLACK);
            g.fillRect(x, y - 10, healthBarWidth, healthBarHeight);
            // Contour blanc
            g.setColor(Color.WHITE);
            g.drawRect(x - 1, y - 11, healthBarWidth + 1, healthBarHeight + 1);
            // Barre de vie
            g.setColor(new Color(0, 255, 128)); // Vert clair
            g.fillRect(x, y - 10, (int)(healthBarWidth * ((double)health / 3)), healthBarHeight);
        }

        public Rectangle getHitbox() {
            return new Rectangle(x + 10, y + 10, 30, 40); // Hitbox plus précise
        }

        public String getName() { return name; }
        public int getHealth() { return health; }
    }

    // Représente un projectile envoyé par un joueur distant
    public static class RemoteProjectile {
        private int x, y;
        private boolean active = true;
        private final int speed = 8; // Vitesse réduite pour une meilleure synchronisation
        private boolean isFromTopPlayer;
        private long creationTime;
        private String shooterName;

        public RemoteProjectile(int x, int y, boolean isFromTopPlayer, String shooterName) {
            this.x = x;
            this.y = y;
            this.isFromTopPlayer = isFromTopPlayer;
            this.shooterName = shooterName;
            this.creationTime = System.currentTimeMillis();
        }

        public void update() {
            // Si le projectile existe depuis plus de 3 secondes, le désactiver
            if (System.currentTimeMillis() - creationTime > 3000) {
                active = false;
                return;
            }

            // Mise à jour de la position
            y += isFromTopPlayer ? speed : -speed;
            
            // Désactiver si hors écran
            if (y < -20 || y > 620) {
                active = false;
            }
        }

        public void draw(Graphics g) {
            if (!active) return;
            
            // Effet de trainée
            g.setColor(new Color(0, 255, 255, 128));
            int trailLength = isFromTopPlayer ? 10 : -10;
            g.fillRect(x - 1, y - trailLength, 7, Math.abs(trailLength) * 2);
            
            // Projectile principal
            g.setColor(Color.CYAN);
            g.fillRect(x, y, 5, 15);
        }

        public Rectangle getHitbox() {
            return new Rectangle(x + 1, y + 2, 3, 11); // Hitbox plus précise
        }

        public String getShooterName() {
            return shooterName;
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

            GameMessage joinMessage = GameMessage.createJoinMessage(playerName, shipType);
            out.writeObject(joinMessage);
            out.flush();

            GameMessage response = (GameMessage) in.readObject();
            // Vérification plus robuste de la réponse
            if (response != null && response.getType() == GameMessage.MessageType.CHAT_MESSAGE
                    && "NAME_ACCEPTED".equals(response.getChatContent())) {
                connected = true;
                listenerThread = new Thread(this::listenForMessages);
                listenerThread.setDaemon(true);
                listenerThread.start();
                return true;
            } else {
                closeConnection();
                return false;
            }
        } catch (Exception e) {
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
                            RemotePlayer shooter = remotePlayers.get(message.getPlayerName());
                            if (shooter != null) {
                                boolean isFromTop = shooter.y < 300;
                                remoteProjectiles.add(new RemoteProjectile(
                                    message.getProjectileX(),
                                    message.getProjectileY(),
                                    isFromTop,
                                    message.getPlayerName()
                                ));
                            }
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
    public void sendHitMessage(String hitPlayerName) {
        if (connected && out != null) {
            try {
                GameMessage hitMsg = GameMessage.createHitMessage(hitPlayerName);
                out.writeObject(hitMsg);
                out.flush();
                
                // Effet sonore ou visuel local
                RemotePlayer hitPlayer = remotePlayers.get(hitPlayerName);
                if (hitPlayer != null) {
                    // Mettre à jour localement pour une réponse immédiate
                    hitPlayer.health = Math.max(0, hitPlayer.health - 1);
                }
            } catch (IOException e) {
                System.err.println("Error sending hit message: " + e.getMessage());
            }
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
        // Mettre à jour et nettoyer les projectiles inactifs
        Iterator<RemoteProjectile> iterator = remoteProjectiles.iterator();
        while (iterator.hasNext()) {
            RemoteProjectile proj = iterator.next();
            proj.update();
            if (!proj.isActive()) {
                iterator.remove();
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }
}