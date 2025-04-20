import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Bouclejeu extends JPanel {
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private int score = 0;
    private final Random random = new Random();
    private int spawnTimer = 0;

    private final Joueur player;
    private final Image background;
    private int backgroundY = 0;
    private int scrollSpeed = 2;
    private final Gestionniveux pp;
    private boolean isLevelTransition = false;
    private long transitionStartTime;
    private boolean gameOver = false;
    private final Image playerLifeIcon;
    private final String playerName;
    private final int initialDifficulty;
    private final FenetreJeu parent;

    private Timer gameTimer;

    public Bouclejeu(FenetreJeu parent, String playerName, int difficulty, int shipType) {
        this.parent = parent;
        this.playerName = playerName;
        this.initialDifficulty = difficulty;
        this.pp = new Gestionniveux(difficulty);

        this.background = GestionRessources.getImage("/background.png");
        this.player = new Joueur(380, 450, shipType);
        this.playerLifeIcon = GestionRessources.getImage("ship_" + shipType + ".png")
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
        if (gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
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
                pp.levelUp();
                scrollSpeed = 2 + pp.getCurrentLevel() / 3;
            }
            return;
        }

        player.update();
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
        int type = random.nextInt(3); // 0: basic, 1: fast, 2: tank
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
                        score += (enemy.getType() == 0) ? 10 :
                                (enemy.getType() == 1) ? 15 : 30;
                        pp.enemyDefeated();
                    }
                }
            });
        });

        new ArrayList<>(enemies).forEach(enemy -> {
            if (enemy.isAlive() && enemy.getHitbox().intersects(player.getHitbox())) {
                enemy.takeDamage(enemy.getMaxHealth()); // Changed from getHealth() to getMaxHealth()
                player.takeDamage();
                if (player.getHealth() <= 0) {
                    gameOver = true;
                    GestionBaseDonnees.saveGameResult(
                            playerName,
                            score,
                            pp.getCurrentLevel(),
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

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Scrolling background
        g.drawImage(background, 0, backgroundY, getWidth(), getHeight(), null);
        g.drawImage(background, 0, backgroundY - getHeight(), getWidth(), getHeight(), null);

        // Entities
        enemies.forEach(e -> e.draw(g));
        projectiles.forEach(p -> p.draw(g));
        player.draw(g);


        drawLives(g);

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
        int y = 40;  // Moved from bottom to top

        // Optional: Add a subtle background for better visibility
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x - 5, y - 25,
                player.getHealth() * 25 + 10,  // Dynamic width based on lives
                50, 10, 10);

        // Redraw the elements on top of the background
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Difficulty: " + getDifficultyString(initialDifficulty), x, y - 10);

        for (int i = 0; i < player.getHealth(); i++) {
            g.drawImage(playerLifeIcon, x + (i * 25), y + 5, 25, 30, null);
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
        // Dark semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Main game over panel
        int panelWidth = 600;
        int panelHeight = 400;
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = (getHeight() - panelHeight) / 2;

        // Panel background with border
        Graphics2D g2d = (Graphics2D) g;
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

        // Score display
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        String scoreText = "YOUR SCORE: " + score;
        g2d.drawString(scoreText,
                panelX + (panelWidth - g2d.getFontMetrics().stringWidth(scoreText)) / 2,
                panelY + 110);

        // Player stats
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        String levelText = "Level Reached: " + pp.getCurrentLevel();
        String difficultyText = "Difficulty: " + getDifficultyString(initialDifficulty);

        g2d.drawString(levelText, panelX + 50, panelY + 160);
        g2d.drawString(difficultyText, panelX + panelWidth - 250, panelY + 160);

        // High scores section
        g2d.setColor(new Color(200, 200, 255));
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("TOP SCORES", panelX + 50, panelY + 200);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(panelX + 50, panelY + 210, panelX + panelWidth - 50, panelY + 210);

        List<String> highscores = GestionBaseDonnees.getHighScores(5);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));

        for (int i = 0; i < highscores.size(); i++) {
            String entry = String.format("%d. %s", i+1, highscores.get(i));
            g2d.drawString(entry, panelX + 60, panelY + 240 + i * 30);
        }

        // Continue prompt
        g2d.setColor(new Color(150, 255, 150));
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        String continueText = "Press Espace to return to menu";
        g2d.drawString(continueText,
                panelX + (panelWidth - g2d.getFontMetrics().stringWidth(continueText)) / 2,
                panelY + panelHeight - 40);


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
