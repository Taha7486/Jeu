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
    private int player2Score = 0;
    private final Random random = new Random();
    private int spawnTimer = 0;

    private Joueur player;
    private Joueur player2;
    private final Image background;
    private int backgroundY = 0;
    private int scrollSpeed = 2;
    private final Gestionniveux pp;
    private boolean isLevelTransition = false;
    private long transitionStartTime;
    private boolean gameOver = false;
    private final Image playerLifeIcon;
    private final Image player2LifeIcon;
    private final String playerName;
    private final String player2Name = "Player2";
    private final int initialDifficulty;
    private final FenetreJeu parent;
    private ClientManager clientManager;
    private boolean isMultiplayer = false;
    private boolean isTwoPlayerMode = false;
    private String serverAddress = "localhost";
    private Timer gameTimer;
    private final List<String> chatMessages = new ArrayList<>();

    public Bouclejeu(FenetreJeu parent, String playerName, int difficulty, int shipType, boolean isMultiplayer) {
        this.parent = parent;
        this.playerName = playerName;
        this.initialDifficulty = difficulty;
        this.isMultiplayer = isMultiplayer;
        this.isTwoPlayerMode = isMultiplayer;
        this.pp = new Gestionniveux(difficulty);

        this.background = GestionRessources.getImage("/background.png");
        this.playerLifeIcon = GestionRessources.getImage("/ship_" + shipType + ".png")
                .getScaledInstance(30, 36, Image.SCALE_SMOOTH);
        this.player2LifeIcon = GestionRessources.getImage("/ship_" + shipType + ".png")
                .getScaledInstance(30, 36, Image.SCALE_SMOOTH);

        if (isMultiplayer) {
            clientManager = new ClientManager(playerName);
            if (!clientManager.connectToServer(serverAddress)) {
                JOptionPane.showMessageDialog(this, "Échec de connexion au serveur ou nom déjà pris", "Erreur", JOptionPane.ERROR_MESSAGE);
                parent.showMenu();
                return;
            }
        }

        this.player = new Joueur(300, 450, shipType, true);
        if (isTwoPlayerMode) {
            this.player2 = new Joueur(500, 450, shipType, false);
        }

        setFocusable(true);
        setupKeyListeners();
        startGameLoop();
    }

    private void handleChatInput() {
        String message = JOptionPane.showInputDialog(this, "Entrez votre message:");
        if (message != null && !message.trim().isEmpty()) {
            if (isMultiplayer) {
                clientManager.sendMessage(message);
            } else {
                addChatMessage(playerName + ": " + message);
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
                if (isTwoPlayerMode && isPlayer2MovementKey(e.getKeyCode())) {
                    handlePlayer2KeyRelease(e.getKeyCode());
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

        // Player 1 controls
        if (e.getKeyCode() == KeyEvent.VK_SPACE && player.canShoot()) {
            projectiles.add(new Projectile(player.getCenterX(), player.getY(), true));
            player.shoot();
        } else if (isMovementKey(e.getKeyCode())) {
            player.handleKeyPress(e.getKeyCode());
        }

        // Player 2 controls (only in two-player mode)
        if (isTwoPlayerMode) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && player2.canShoot()) {
                projectiles.add(new Projectile(player2.getCenterX(), player2.getY(), false));
                player2.shoot();
            } else if (isPlayer2MovementKey(e.getKeyCode())) {
                handlePlayer2KeyPress(e.getKeyCode());
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            parent.showMenu();
        }
    }

    private void handlePlayer2KeyPress(int keyCode) {
        switch(keyCode) {
            case KeyEvent.VK_A:
                player2.handleKeyPress(KeyEvent.VK_LEFT);
                break;
            case KeyEvent.VK_D:
                player2.handleKeyPress(KeyEvent.VK_RIGHT);
                break;
            case KeyEvent.VK_W:
                player2.handleKeyPress(KeyEvent.VK_UP);
                break;
            case KeyEvent.VK_S:
                player2.handleKeyPress(KeyEvent.VK_DOWN);
                break;
        }
    }

    private void handlePlayer2KeyRelease(int keyCode) {
        switch(keyCode) {
            case KeyEvent.VK_A:
                player2.handleKeyRelease(KeyEvent.VK_LEFT);
                break;
            case KeyEvent.VK_D:
                player2.handleKeyRelease(KeyEvent.VK_RIGHT);
                break;
            case KeyEvent.VK_W:
                player2.handleKeyRelease(KeyEvent.VK_UP);
                break;
            case KeyEvent.VK_S:
                player2.handleKeyRelease(KeyEvent.VK_DOWN);
                break;
        }
    }

    private boolean isMovementKey(int keyCode) {
        return keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
                keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN;
    }

    private boolean isPlayer2MovementKey(int keyCode) {
        return keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_D ||
                keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_S;
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
        if (isLevelTransition) {
            if (System.currentTimeMillis() - transitionStartTime > 2000) {
                isLevelTransition = false;
                pp.levelUp();
                scrollSpeed = 2 + pp.getCurrentLevel() / 3;
            }
            return;
        }

        player.update();
        if (isTwoPlayerMode) {
            player2.update();
        }
        updateBackground();

        if (++spawnTimer >= pp.getAdjustedSpawnInterval()) {
            spawnEnemy();
            spawnTimer = 0;
        }

        enemies.forEach(e -> e.update(scrollSpeed));
        projectiles.forEach(Projectile::update);

        handleCollisions();

        enemies.removeIf(e -> !e.isAlive() || e.isOutOfScreen(getHeight()));
        projectiles.removeIf(p -> !p.isActive());

        if (pp.isLevelCompleted()) {
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
        int baseSpeed = pp.getEnemySpeed();
        int type = random.nextInt(3);
        enemies.add(new Enemy(
                random.nextInt(getWidth() - 50),
                -50,
                baseSpeed,
                type));
    }

    private void handleCollisions() {
        new ArrayList<>(enemies).forEach(enemy -> {
            new ArrayList<>(projectiles).forEach(projectile -> {
                if (projectile.isActive() && enemy.isAlive() &&
                        projectile.getHitbox().intersects(enemy.getHitbox())) {
                    enemy.takeDamage(1);
                    projectile.setActive(false);

                    if (!enemy.isAlive()) {
                        int points = (enemy.getType() == 0) ? 10 :
                                (enemy.getType() == 1) ? 15 : 30;

                        if (projectile.isPlayer1Projectile()) {
                            score += points;
                        } else {
                            player2Score += points;
                        }

                        pp.enemyDefeated();
                    }
                }
            });
        });

        new ArrayList<>(enemies).forEach(enemy -> {
            if (enemy.isAlive() && enemy.getHitbox().intersects(player.getHitbox())) {
                enemy.takeDamage(enemy.getMaxHealth());
                player.takeDamage();
                if (player.getHealth() <= 0) {
                    gameOver = true;
                    saveGameResults();
                }
            }

            if (isTwoPlayerMode && enemy.isAlive() && enemy.getHitbox().intersects(player2.getHitbox())) {
                enemy.takeDamage(enemy.getMaxHealth());
                player2.takeDamage();
                if (player2.getHealth() <= 0 && !gameOver) {
                    gameOver = true;
                    saveGameResults();
                }
            }
        });
    }

    private void saveGameResults() {
        GestionBaseDonnees.saveGameResult(
                playerName,
                score,
                pp.getCurrentLevel(),
                getDifficultyString(initialDifficulty)
        );

        if (isTwoPlayerMode) {
            GestionBaseDonnees.saveGameResult(
                    player2Name,
                    player2Score,
                    pp.getCurrentLevel(),
                    getDifficultyString(initialDifficulty)
            );
        }
    }

    private void resetGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        parent.showMenu();
    }

    public void cleanUp() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (isMultiplayer && clientManager != null) {
            clientManager.disconnect();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Scrolling background
        g.drawImage(background, 0, backgroundY, getWidth(), getHeight(), null);
        g.drawImage(background, 0, backgroundY - getHeight(), getWidth(), getHeight(), null);

        // Entities
        enemies.forEach(e -> e.draw(g));
        projectiles.forEach(p -> p.draw(g));
        player.draw(g);
        if (isTwoPlayerMode) {
            player2.draw(g);
        }

        drawLives(g);
        if (isTwoPlayerMode) {
            drawPlayer2Lives(g);
        }

        // Level transition
        if (isLevelTransition) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            String message = "LEVEL " + (pp.getCurrentLevel() + 1) + "!";
            g.drawString(message,
                    getWidth() / 2 - g.getFontMetrics().stringWidth(message) / 2,
                    getHeight() / 2);
        }

        drawChatBox(g);

        // Game Over
        if (gameOver) {
            drawGameOverScreen(g);
        }
    }

    private void drawLives(Graphics g) {
        int x = 20;
        int y = 40;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x - 5, y - 25,
                player.getHealth() * 25 + 10,
                50, 10, 10);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(playerName + ": " + score, x, y - 10);

        for (int i = 0; i < player.getHealth(); i++) {
            g.drawImage(playerLifeIcon, x + (i * 25), y + 5, 25, 30, null);
        }
    }

    private void drawPlayer2Lives(Graphics g) {
        int x = getWidth() - 220;
        int y = 40;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x - 5, y - 25,
                player2.getHealth() * 25 + 10,
                50, 10, 10);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(player2Name + ": " + player2Score, x, y - 10);

        for (int i = 0; i < player2.getHealth(); i++) {
            g.drawImage(player2LifeIcon, x + (i * 25), y + 5, 25, 30, null);
        }
    }

    private String getDifficultyString(int difficulty) {
        switch (difficulty) {
            case 1: return "Easy";
            case 3: return "Normal";
            case 5: return "Hard";
            default: return "Custom";
        }
    }

    private void drawGameOverScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Dark semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Main game over panel
        int panelWidth = 600;
        int panelHeight = 400;
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = (getHeight() - panelHeight) / 2;

        // Panel background with border
        g2d.setColor(new Color(40, 40, 60));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);
        g2d.setColor(new Color(100, 100, 150));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

        // Title with gradient effect
        GradientPaint gradient = new GradientPaint(
                panelX, panelY, Color.RED,
                panelX + panelWidth, panelY, Color.ORANGE
        );
        g2d.setPaint(gradient);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "GAME OVER";
        g2d.drawString(title,
                panelX + (panelWidth - g2d.getFontMetrics().stringWidth(title)) / 2,
                panelY + 60);

        // Determine winner in two-player mode
        if (isTwoPlayerMode) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            String winnerText;
            if (score > player2Score) {
                winnerText = playerName + " WINS!";
            } else if (player2Score > score) {
                winnerText = player2Name + " WINS!";
            } else {
                winnerText = "DRAW!";
            }
            g2d.drawString(winnerText,
                    panelX + (panelWidth - g2d.getFontMetrics().stringWidth(winnerText)) / 2,
                    panelY + 110);
        }

        // Score display
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        String scoreText = playerName + ": " + score;
        g2d.drawString(scoreText,
                panelX + (panelWidth - g2d.getFontMetrics().stringWidth(scoreText)) / 2,
                panelY + (isTwoPlayerMode ? 150 : 110));

        if (isTwoPlayerMode) {
            String score2Text = player2Name + ": " + player2Score;
            g2d.drawString(score2Text,
                    panelX + (panelWidth - g2d.getFontMetrics().stringWidth(score2Text)) / 2,
                    panelY + 190);
        }

        // Continue prompt
        g2d.setColor(new Color(150, 255, 150));
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        String continueText = "Press SPACE to return to menu";
        g2d.drawString(continueText,
                panelX + (panelWidth - g2d.getFontMetrics().stringWidth(continueText)) / 2,
                panelY + panelHeight - 40);
    }

    private void drawChatBox(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Semi-transparent background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(
                getWidth() - 210,
                getHeight() - 150,
                200,
                140,
                15,
                15
        );

        // Border
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(getWidth() - 210, getHeight() - 150, 200, 140, 15, 15);

        // Chat text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        // Display messages
        int yPos = getHeight() - 130;
        List<String> messages = isMultiplayer ?
                clientManager.getChatMessages() : chatMessages;

        for (String message : messages) {
            g2d.drawString(message, getWidth() - 200, yPos);
            yPos += 20;
        }

        // Chat indicator
        g2d.drawString("Chat (T to write)", getWidth() - 200, getHeight() - 30);
    }
}