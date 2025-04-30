import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bouclejeu extends JPanel {
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private int score = 0;
    private final Random random = new Random();
    private int spawnTimer = 0;
    private static final int NETWORK_UPDATE_INTERVAL = 50; // ms
    private long lastNetworkUpdate;

    private Joueur player;
    private final Image background;
    private int backgroundY = 0;
    private int scrollSpeed = 2;
    private final Gestionniveux levelManager;
    private boolean isLevelTransition = false;
    private long transitionStartTime;
    private boolean gameOver = false;
    private final Image playerLifeIcon;
    private final String playerName;
    private final int initialDifficulty;
    private final int shipType;
    private final FenetreJeu parent;
    private ClientManager clientManager;
    private boolean isMultiplayer = false;
    private String serverAddress = "localhost";
    private Timer gameTimer;
    private final List<String> chatMessages = new ArrayList<>();
    private boolean pvpMode = false;
    public Bouclejeu(FenetreJeu parent, String playerName, int difficulty, int shipType, boolean isMultiplayer) {
        this.parent = parent;
        this.playerName = playerName;
        this.initialDifficulty = difficulty;
        this.shipType = shipType;
        this.isMultiplayer = isMultiplayer;
        this.levelManager = new Gestionniveux(difficulty);
        this.pvpMode = isMultiplayer;
        this.player = new Joueur(380, 450, shipType);
        this.background = GestionRessources.getImage("/background.png");
        this.playerLifeIcon = GestionRessources.getImage("/ship_" + shipType + ".png")
                .getScaledInstance(30, 36, Image.SCALE_SMOOTH);
        if (isMultiplayer) {
            try {
                clientManager = new ClientManager(playerName, shipType);
                if (!clientManager.connectToServer(serverAddress)) {
                    throw new RuntimeException("Échec de connexion au serveur");
                }
            } catch (Exception e) {
                throw new RuntimeException("Erreur de connexion multijoueur: " + e.getMessage());
            }
        }

        // Initialisation des joueurs - MODIFICATION PRINCIPALE ICI
        if (isMultiplayer && pvpMode) {
            // Joueur local en bas
            this.player = new Joueur(380, 450, shipType);
            // Limiter le mouvement vertical
            this.player.setVerticalBounds(400, 550); // Ne peut pas monter au-dessus de y=400

            // Position des adversaires en haut
            if (clientManager != null) {
                for (ClientManager.RemotePlayer remotePlayer : clientManager.getRemotePlayers()) {
                    remotePlayer.update(380, 150, 3, 0); // Y=150 pour le haut
                    remotePlayer.setVerticalBounds(50, 200); // Ne peut pas descendre en dessous de y=200
                }
            }
        }

        setFocusable(true);
        setupKeyListeners();
        startGameLoop();
    }
    public ClientManager getClientManager() {
        return clientManager;
    }
    private void handleChatInput() {
        String message = JOptionPane.showInputDialog(this, "Entrez votre message:");
        if (message != null && !message.trim().isEmpty()) {
            if (isMultiplayer) {
                clientManager.sendChatMessage(message);
            } else {
                addChatMessage("Vous: " + message);
            }
        }
    }

    private void addChatMessage(String message) {
        chatMessages.add(message);
        if (chatMessages.size() > 10) {
            chatMessages.remove(0);
        }
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (isMovementKey(e.getKeyCode())) {
                    player.handleKeyRelease(e.getKeyCode());
                }
            }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_T) {
            handleChatInput();
            return;
        }
        if (gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
            resetGame();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE && player.canShoot()) {
            Projectile projectile = new Projectile(player.getCenterX(), player.getY());
            projectiles.add(projectile);
            player.shoot();

            // En mode multijoueur, envoyer l'info du tir au serveur
            if (isMultiplayer) {
                clientManager.sendProjectile(player.getCenterX(), player.getY());
            }
        } else if (isMovementKey(e.getKeyCode())) {
            player.handleKeyPress(e.getKeyCode());
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            parent.showMenu();
        }
    }

    private boolean isMovementKey(int keyCode) {
        return keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
                keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN;
    }

    private void startGameLoop() {
        gameTimer = new Timer(16, e -> {
            if (!gameOver) {
                updateGame();
            }
            repaint();
        });
        gameTimer.start();
    }

    private void updateGame() {

        if (player == null || gameOver) {
            return;
        }
        if (isLevelTransition) {
            if (System.currentTimeMillis() - transitionStartTime > 2000) {
                isLevelTransition = false;
                levelManager.levelUp();
                scrollSpeed = 2 + levelManager.getCurrentLevel() / 3;
            }
            return;
        }

        player.update();

        // Envoyer position au serveur en mode multijoueur
        if (isMultiplayer) {
            clientManager.sendPosition(player.getX(), player.getY(), player.getHealth(), score);
            clientManager.updateRemoteProjectiles();

            // En PvP, limiter la position Y du joueur local
            if (pvpMode) {
                if (player.getY() < 300) { // Empêcher de monter trop haut
                    player = new Joueur(player.getX(), 300, shipType);
                }
            }
        }

        updateBackground();

        if (!pvpMode && ++spawnTimer >= levelManager.getAdjustedSpawnInterval()) {
            spawnEnemy();
            spawnTimer = 0;
        }

        enemies.forEach(e -> e.update(scrollSpeed));
        projectiles.forEach(Projectile::update);

        handleCollisions();

        enemies.removeIf(e -> !e.isAlive() || e.isOutOfScreen(getHeight()));
        projectiles.removeIf(p -> !p.isActive());

        if (!pvpMode && levelManager.isLevelCompleted()) {
            isLevelTransition = true;
            transitionStartTime = System.currentTimeMillis();
        }

    }


    private void updateBackground() {
        backgroundY += scrollSpeed;
        if (backgroundY >= getHeight()) {
            backgroundY = 0;
        }
    }

    private void spawnEnemy() {
        int baseSpeed = levelManager.getEnemySpeed();
        int type = random.nextInt(3); // 0: basic, 1: fast, 2: tank
        enemies.add(new Enemy(
                random.nextInt(getWidth() - 50),
                -50,
                baseSpeed,
                type));
    }
    private void handleCollisions() {
        // Collisions entre projectiles locaux et ennemis (mode solo)
        new ArrayList<>(enemies).forEach(enemy -> {
            new ArrayList<>(projectiles).forEach(projectile -> {
                if (projectile.isActive() && enemy.isAlive() &&
                        projectile.getHitbox().intersects(enemy.getHitbox())) {
                    enemy.takeDamage(1);
                    projectile.setActive(false);

                    if (!enemy.isAlive()) {
                        score += (enemy.getType() == 0) ? 10 :
                                (enemy.getType() == 1) ? 15 : 30;
                        levelManager.enemyDefeated();
                    }
                }
            });
        });

        // Collisions entre joueur local et ennemis
        new ArrayList<>(enemies).forEach(enemy -> {
            if (enemy.isAlive() && enemy.getHitbox().intersects(player.getHitbox())) {
                enemy.takeDamage(enemy.getMaxHealth());
                player.takeDamage();
                checkGameOver();
            }
        });

        // En mode multijoueur, vérifier les collisions
        if (isMultiplayer) {
            // Collisions entre projectiles distants et joueur local
            new ArrayList<>(clientManager.getRemoteProjectiles()).forEach(projectile -> {
                if (projectile.isActive() && projectile.getHitbox().intersects(player.getHitbox())) {
                    projectile.setActive(false);
                    player.takeDamage();
                    checkGameOver();
                }
            });

            // AJOUTEZ VOTRE CODE ICI - Début du nouveau bloc
            if (pvpMode) {
                // Vérifier les collisions entre projectiles locaux et joueurs distants
                for (Projectile projectile : new ArrayList<>(projectiles)) {
                    for (ClientManager.RemotePlayer remotePlayer : clientManager.getRemotePlayers()) {
                        if (projectile.isActive() && projectile.getHitbox().intersects(remotePlayer.getHitbox())) {
                            projectile.setActive(false);
                            // Envoyer un message de hit au serveur
                            clientManager.sendHitMessage(remotePlayer.getName());
                        }
                    }
                }

                // Vérifier si un joueur distant est mort
                for (ClientManager.RemotePlayer remotePlayer : clientManager.getRemotePlayers()) {
                    if (remotePlayer.getHealth() <= 0 && !gameOver) {
                        gameOver = true;
                        // Envoyer un message de victoire au serveur
                        clientManager.sendChatMessage("J'ai gagné !");
                    }
                }
            }
            // FIN du nouveau bloc
        }
    }

    private void checkGameOver() {
        if (player.getHealth() <= 0) {
            gameOver = true;
        }
    }

    private void resetGame() {
        player = new Joueur(380, 450, shipType);
        enemies.clear();
        projectiles.clear();
        score = 0;
        gameOver = false;
        levelManager.reset();

        if (isMultiplayer) {
            // Réinitialiser les données multijoueur
            clientManager.getRemoteProjectiles().clear();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dessiner l'arrière-plan
        g.drawImage(background, 0, backgroundY - getHeight(), getWidth(), getHeight(), null);
        g.drawImage(background, 0, backgroundY, getWidth(), getHeight(), null);

        // Dessiner les ennemis
        enemies.forEach(e -> e.draw(g));

        // Dessiner les projectiles locaux
        projectiles.forEach(p -> p.draw(g));

        // En mode multijoueur, dessiner les joueurs distants et leurs projectiles
        if (isMultiplayer) {
            for (ClientManager.RemotePlayer remotePlayer : clientManager.getRemotePlayers()) {
                remotePlayer.draw(g);
            }

            for (ClientManager.RemoteProjectile remoteProjectile : clientManager.getRemoteProjectiles()) {
                remoteProjectile.draw(g);
            }

            // Dessiner les messages de chat
            drawChatMessages(g);
        }

        // Dessiner le joueur local
        player.draw(g);

        // Afficher le score et le niveau
        drawHUD(g);

        // Afficher les transitions de niveau
        if (isLevelTransition) {
            drawLevelTransition(g);
        }

        // Afficher l'écran de fin de jeu
        if (gameOver) {
            drawGameOver(g);
        }
    }

    private void drawChatMessages(Graphics g) {
        // Fond semi-transparent
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(10, 10, 300, 150); // Taille fixe

        // Texte
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 12));

        List<String> messages = isMultiplayer ? clientManager.getChatMessages() : chatMessages;

        // Afficher seulement les 8 derniers messages
        int startIdx = Math.max(0, messages.size() - 8);
        int y = 30;

        for (int i = startIdx; i < messages.size(); i++) {
            // Tronquer les messages trop longs
            String msg = messages.get(i);
            if (msg.length() > 40) {
                msg = msg.substring(0, 37) + "...";
            }
            g.drawString(msg, 20, y);
            y += 15;
        }

        // Instruction pour le chat
        g.setColor(Color.YELLOW);
        g.drawString("Appuyez sur T pour discuter", 20, 160);
    }

    private void drawHUD(Graphics g) {
        // Dessiner le score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, getHeight() - 50);

        // Dessiner le niveau seulement en mode solo/coop
        if (!pvpMode) {
            g.setColor(Color.YELLOW);
            g.drawString("Niveau: " + levelManager.getCurrentLevel(), 20, getHeight() - 20);
        }

        // Dessiner les vies restantes
        for (int i = 0; i < player.getHealth(); i++) {
            g.drawImage(playerLifeIcon, getWidth() - 40 - (i * 35), getHeight() - 40, null);
        }

        // En mode multijoueur, afficher les joueurs en ligne
        if (isMultiplayer) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("Joueurs: " + (clientManager.getOnlinePlayers().size() + 1),
                    getWidth() - 200, 30);

            // En PvP, afficher le mode de jeu
            if (pvpMode) {
                g.setColor(Color.RED);
                g.drawString("MODE PvP", getWidth() - 100, 60);
            }
        }
    }

    private void drawLevelTransition(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String message = "NIVEAU " + (levelManager.getCurrentLevel() + 1);
        int stringWidth = g.getFontMetrics().stringWidth(message);
        g.drawString(message, (getWidth() - stringWidth) / 2, getHeight() / 2);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String message = "GAME OVER";
        int stringWidth = g.getFontMetrics().stringWidth(message);
        g.drawString(message, (getWidth() - stringWidth) / 2, getHeight() / 2 - 40);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String scoreMsg = "Score final: " + score;
        stringWidth = g.getFontMetrics().stringWidth(scoreMsg);
        g.drawString(scoreMsg, (getWidth() - stringWidth) / 2, getHeight() / 2 + 10);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        String restartMsg = "Appuyez sur ESPACE pour recommencer";
        stringWidth = g.getFontMetrics().stringWidth(restartMsg);
        g.drawString(restartMsg, (getWidth() - stringWidth) / 2, getHeight() / 2 + 50);
    }

    public void cleanupMultiplayer() {
        if (isMultiplayer && clientManager != null) {
            // Envoyer un message de déconnexion avant de fermer
            GameMessage leaveMsg = GameMessage.createChatMessage("SYSTEM",
                    playerName + " a quitté la partie");
            clientManager.sendChatMessage(leaveMsg.getChatContent());

            // Attendre un court instant pour que le message parte
            try { Thread.sleep(200); } catch (InterruptedException e) {}

            clientManager.disconnect();
        }
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    // Méthode utilitaire pour déterminer si un point est dans la zone de jeu
    private boolean isInBounds(int x, int y) {
        return x >= 0 && x <= getWidth() && y >= 0 && y <= getHeight();
    }

    // Méthode pour régler le mode PvP
    public void setPvpMode(boolean pvpMode) {
        this.pvpMode = pvpMode;
    }

    // Méthode pour obtenir le score actuel
    public int getScore() {
        return score;
    }

    // Méthode pour obtenir le joueur
    public Joueur getPlayer() {
        return player;
    }
    // Méthode pour changer l'adresse du serveur (si besoin de se connecter à un autre serveur)
    public void setServerAddress(String address) {
        this.serverAddress = address;
        if (isMultiplayer && clientManager != null) {
            clientManager.disconnect();
            clientManager = new ClientManager(playerName, shipType);
            if (!clientManager.connectToServer(serverAddress)) {
                JOptionPane.showMessageDialog(this, "Échec de connexion au serveur", "Erreur", JOptionPane.ERROR_MESSAGE);
                parent.showMenu();
            }
        }
    }
    public void cleanUp() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (isMultiplayer && clientManager != null) {
            clientManager.disconnect();
        }
    }
}