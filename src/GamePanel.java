// GamePanel.java

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private int score = 0;
    private final Random random = new Random();
    private int spawnTimer = 0;

    private final Player player;
    private final Image background;
    private int backgroundY = 0;
    private int scrollSpeed = 2;


    private final LevelManager levelManager;
    private boolean isLevelTransition = false;
    private long transitionStartTime;
    private boolean gameOver = false;
    private final Image playerLifeIcon;
    private final String playerName;
    private final int initialDifficulty;
    private final GameWindow parent;

    private Timer gameTimer;  // Changed to javax.swing.Timer

    public GamePanel(GameWindow parent, String playerName, int difficulty, int shipType) {
        this.parent = parent;
        this.playerName = playerName;
        this.initialDifficulty = difficulty;
        this.levelManager = new LevelManager(difficulty);

        this.background = ResourceManager.getImage("/background.png");
        this.player = new Player(380, 450, shipType);
        this.playerLifeIcon = ResourceManager.getImage("ship_" + shipType + ".png")
                .getScaledInstance(30, 36, Image.SCALE_SMOOTH);


        setFocusable(true);
        setupKeyListeners();
        startGameLoop();
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

            private boolean isMovementKey(int keyCode) {
                return keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
                        keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN;
            }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        if (gameOver && e.getKeyCode() == KeyEvent.VK_R) {
            resetGame();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE && player.canShoot()) {
            projectiles.add(new Projectile(player.getCenterX(), player.getY()));
            player.shoot();

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
        if (isLevelTransition) {
            if (System.currentTimeMillis() - transitionStartTime > 2000) {
                isLevelTransition = false;
                levelManager.levelUp();
                scrollSpeed = 2 + levelManager.getCurrentLevel() / 3;
            }
            return;
        }

        player.update();
        updateBackground();

        if (++spawnTimer >= levelManager.getAdjustedSpawnInterval()) {
            spawnEnemy();
            spawnTimer = 0;
        }

        enemies.forEach(e -> e.update(scrollSpeed));
        projectiles.forEach(Projectile::update);

        handleCollisions();

        enemies.removeIf(e -> !e.isAlive() || e.isOutOfScreen(getHeight()));
        projectiles.removeIf(p -> !p.isActive());

        if (levelManager.isLevelCompleted()) {
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
        // Projectile-enemy collisions
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


        // Player-enemy collisions
        new ArrayList<>(enemies).forEach(enemy -> {
            if (enemy.isAlive() && enemy.getHitbox().intersects(player.getHitbox())) {
                enemy.takeDamage(enemy.getMaxHealth()); // Changed from getHealth() to getMaxHealth()
                player.takeDamage();
                if (player.getHealth() <= 0) {
                    gameOver = true;
                    DatabaseManager.saveGameResult(
                            playerName,
                            score,
                            levelManager.getCurrentLevel(),
                            getDifficultyString(initialDifficulty)
                    );
                }
            }
        });
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

        // UI
        drawInfoBoard(g);
        drawLives(g);

        // Level transition
        if (isLevelTransition) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            String message = "LEVEL " + (levelManager.getCurrentLevel() + 1) + "!";
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

    private void drawInfoBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(getWidth() - 210, 10, 200, 100, 15, 15);
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(getWidth() - 210, 10, 200, 100, 15, 15);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));

        int yPos = 35;
        g2d.drawString("Player: " + playerName, getWidth() - 200, yPos);
        g2d.drawString("Score: " + score, getWidth() - 200, yPos + 25);
        g2d.drawString("Level: " + levelManager.getCurrentLevel(), getWidth() - 200, yPos + 50);
        g2d.drawString("Enemies: " + levelManager.getEnemiesDefeated() + "/" +
                levelManager.getEnemiesToNextLevel(), getWidth() - 200, yPos + 75);
    }

    private void drawLives(Graphics g) {
        int x = 20;
        int y = getHeight() - 50;

        for (int i = 0; i < player.getHealth(); i++) {
            g.drawImage(playerLifeIcon, x + (i * 35), y, null);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Difficulty: " + getDifficultyString(initialDifficulty), x, y - 20);
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
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String message = "GAME OVER - Score: " + score;
        g.drawString(message,
                getWidth() / 2 - g.getFontMetrics().stringWidth(message) / 2,
                getHeight() / 2);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press R to return to menu",
                getWidth() / 2 - 100,
                getHeight() / 2 + 50);

        // Display highscores
        List<String> highscores = DatabaseManager.getHighScores(20);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Top Scores:", 50, getHeight() / 2 + 100);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        for (int i = 0; i < Math.min(5, highscores.size()); i++) {
            g.drawString(highscores.get(i), 50, getHeight() / 2 + 130 + i * 25);
        }
    }

    private final List<String> chatMessages = Arrays.asList(
            "Joueur1: Salut !",
            "Joueur2: Ça va ?",
            "Joueur1: Prêt pour la mission !"
    );

    private void drawChatBox(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        // Fond semi-transparent
        g2d.setColor(new Color(0, 0, 0, 150)); // Noir semi-transparent
        g2d.fillRoundRect(
                getWidth() - 210,       // Position X (coin droit)
                getHeight() - 150,      // Position Y (en bas)
                200,                    // Largeur
                140,                    // Hauteur
                15,                     // Arrondi des coins
                15
        );

        // Bordure
        g2d.setColor(new Color(255, 255, 255, 100)); // Bordure blanche
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(getWidth() - 210, getHeight() - 150, 200, 140, 15, 15);

        // Texte du chat
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        // Affichage des messages
        int yPos = getHeight() - 130;
        for (String message : chatMessages) {
            g2d.drawString(message, getWidth() - 200, yPos);
            yPos += 20; // Espacement entre lignes
        }

        // Indicateur de chat (simulation)
        g2d.drawString("Chat (T pour écrire)", getWidth() - 200, getHeight() - 30);
    }
}