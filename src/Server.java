import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class Server {
    private static final int PORT = 5555;
    private static final Set<ClientHandler> clients = new CopyOnWriteArraySet<>();
    private static final Map<String, PlayerInfo> players = new ConcurrentHashMap<>();
    private static final Set<String> playerNames = new HashSet<>();

    // Stocke les informations des joueurs
    static class PlayerInfo {
        int x, y;
        int health;
        int score;
        int shipType;

        public PlayerInfo(int shipType) {
            this.x = 380;
            this.y = 450;
            this.health = 3;
            this.score = 0;
            this.shipType = shipType;
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Game Server started on port " + PORT);
            System.out.println("Waiting for players...");

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

    public static synchronized void broadcast(GameMessage message, ClientHandler excludeClient) {
        for (ClientHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    public static synchronized void addPlayer(String name, int shipType) {
        playerNames.add(name);
        players.put(name, new PlayerInfo(shipType));
        updatePlayerList();
    }

    public static synchronized void removeClient(ClientHandler client, String name) {
        clients.remove(client);
        if (name != null) {
            playerNames.remove(name);
            players.remove(name);
            
            // Envoyer une notification à tous les clients
            GameMessage leaveMsg = GameMessage.createChatMessage("SYSTEM",
                    name + " a quitté la partie");
            broadcast(leaveMsg, null);
            
            // Mettre à jour la liste des joueurs
            updatePlayerList();
        }
    }

    public static synchronized void updatePlayerPosition(String name, int x, int y, int health, int score) {
        PlayerInfo player = players.get(name);
        if (player != null) {
            // Limiter la position X entre 0 et 750 (pour un écran de 800 de large)
            player.x = Math.max(0, Math.min(750, x));
            
            // Définir des zones de jeu distinctes pour chaque joueur
            if (players.size() <= 1) {
                // Si un seul joueur, zone centrale
                player.y = Math.max(200, Math.min(400, y));
            } else {
                // Sinon, diviser l'écran en zones
                boolean isFirstPlayer = players.keySet().iterator().next().equals(name);
                if (isFirstPlayer) {
                    player.y = Math.max(350, Math.min(500, y)); // Zone basse
                } else {
                    player.y = Math.max(100, Math.min(250, y));  // Zone haute
                }
            }
            
            player.health = Math.max(0, Math.min(3, health));
            player.score = score;

            // Envoyer la mise à jour de position à tous les clients
            GameMessage posMsg = GameMessage.createPositionMessage(name, player.x, player.y, player.health, player.score);
            broadcast(posMsg, null);

            if (health <= 0) {
                GameMessage deathMsg = GameMessage.createChatMessage("SYSTEM",
                        name + " a été éliminé !");
                broadcast(deathMsg, null);
            }
        }
    }

    public static synchronized void updatePlayerList() {
        StringBuilder sb = new StringBuilder("Online players: ");
        for (String name : playerNames) {
            sb.append(name).append(",");
        }
        GameMessage playerListMsg = GameMessage.createChatMessage("SYSTEM", sb.toString());
        for (ClientHandler client : clients) {
            client.sendMessage(playerListMsg);
        }
    }

    public static synchronized Map<String, PlayerInfo> getPlayers() {
        return new HashMap<>(players);
    }
}

class ClientHandler extends Thread {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String playerName;
    private int shipType;
    private boolean running = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Étape 1: Recevoir les informations de connexion du joueur
            GameMessage joinMsg = (GameMessage) in.readObject();
            if (joinMsg.getType() != GameMessage.MessageType.PLAYER_JOIN) {
                socket.close();
                return;
            }

            playerName = joinMsg.getPlayerName();
            shipType = joinMsg.getShipType();

            if (Server.isNameTaken(playerName)) {
                GameMessage nameExistsMsg = GameMessage.createChatMessage("SYSTEM", "NAME_EXISTS");
                sendMessage(nameExistsMsg);
                socket.close();
                return;
            } else {
                GameMessage nameAcceptedMsg = GameMessage.createChatMessage("SYSTEM", "NAME_ACCEPTED");
                sendMessage(nameAcceptedMsg);
                Server.addPlayer(playerName, shipType);

                // Envoyer les informations des autres joueurs déjà connectés
                for (Map.Entry<String, Server.PlayerInfo> entry : Server.getPlayers().entrySet()) {
                    if (!entry.getKey().equals(playerName)) {
                        Server.PlayerInfo pInfo = entry.getValue();
                        GameMessage existingPlayer = GameMessage.createJoinMessage(entry.getKey(), pInfo.shipType);
                        sendMessage(existingPlayer);
                    }
                }

                // Informer les autres joueurs de l'arrivée d'un nouveau joueur
                GameMessage newPlayerMsg = GameMessage.createJoinMessage(playerName, shipType);
                Server.broadcast(newPlayerMsg, this);

                GameMessage chatMsg = GameMessage.createChatMessage("SYSTEM",
                        playerName + " a rejoint la partie");
                Server.broadcast(chatMsg, null);

// Et pour les déconnexions:
                GameMessage leaveMsg = GameMessage.createChatMessage("SYSTEM",
                        playerName + " a quitté la partie");
                Server.broadcast(leaveMsg, null);
            }

            // Boucle principale de traitement des messages
            while (running) {
                GameMessage clientMessage = (GameMessage) in.readObject();

                switch (clientMessage.getType()) {
                    case PLAYER_POSITION:
                        Server.updatePlayerPosition(
                                playerName,
                                clientMessage.getX(),
                                clientMessage.getY(),
                                clientMessage.getHealth(),
                                clientMessage.getScore()
                        );
                        Server.broadcast(clientMessage, this);
                        break;

                    case PLAYER_SHOOT:
                        // Vérifier que le joueur est vivant avant de permettre le tir
                        Server.PlayerInfo shooter = Server.getPlayers().get(playerName);
                        if (shooter != null && shooter.health > 0) {
                            Server.broadcast(clientMessage, null);
                        }
                        break;

                    case PLAYER_HIT:
                        String hitPlayerName = clientMessage.getPlayerName();
                        Server.PlayerInfo hitPlayer = Server.getPlayers().get(hitPlayerName);
                        if (hitPlayer != null && hitPlayer.health > 0) {
                            hitPlayer.health--;
                            
                            // Augmenter le score du tireur
                            Server.PlayerInfo attackingPlayer = Server.getPlayers().get(playerName);
                            if (attackingPlayer != null) {
                                attackingPlayer.score += 10;
                                
                                // Envoyer les mises à jour des deux joueurs
                                GameMessage attackerUpdate = GameMessage.createPositionMessage(
                                    playerName,
                                    attackingPlayer.x,
                                    attackingPlayer.y,
                                    attackingPlayer.health,
                                    attackingPlayer.score
                                );
                                Server.broadcast(attackerUpdate, null);
                            }

                            // Envoyer les mises à jour de la cible
                            GameMessage targetUpdate = GameMessage.createPositionMessage(
                                hitPlayerName,
                                hitPlayer.x,
                                hitPlayer.y,
                                hitPlayer.health,
                                hitPlayer.score
                            );
                            Server.broadcast(targetUpdate, null);

                            if (hitPlayer.health <= 0) {
                                GameMessage deathMsg = GameMessage.createChatMessage("SYSTEM",
                                    hitPlayerName + " a été éliminé par " + playerName + " !");
                                Server.broadcast(deathMsg, null);
                            }
                        }
                        break;

                    case CHAT_MESSAGE:
                        GameMessage formattedMsg = GameMessage.createChatMessage(
                                playerName, clientMessage.getChatContent()
                        );
                        Server.broadcast(formattedMsg, null);
                        break;

                    default:
                        // Ignorer les messages non reconnus
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
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

    public void sendMessage(GameMessage message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message to " + playerName + ": " + e.getMessage());
            running = false;
        }
    }

    public void stopRunning() {
        this.running = false;
    }
}