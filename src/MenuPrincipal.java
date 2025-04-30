import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class MenuPrincipal extends JPanel {
<<<<<<< HEAD
    private FenetreJeu parent;
    private JTextField nameField;
    private JComboBox<String> difficultySelector;
    private JComboBox<String> shipSelector;
=======
    private final FenetreJeu parent;
    private JCheckBox multiplayerCheckbox;
>>>>>>> ce0b86bab71375ff8ae15ec4c34a3c16a883b4b1

    public MenuPrincipal(FenetreJeu parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 50));

        // Titre du jeu
        JLabel titleLabel = new JLabel("SPACE DEFENDER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Panneau central avec options de jeu
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(0, 2, 10, 10));
        centerPanel.setBackground(new Color(30, 30, 50));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

<<<<<<< HEAD
        // Champ nom du joueur
        JLabel nameLabel = new JLabel("Nom du joueur:");
=======
    private void addTitle(GridBagConstraints gbc) {
        JLabel title = new JLabel("Space Defender", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(new Color(255, 215, 0));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        add(title, gbc);
    }

    private void addOptionsPanel(GridBagConstraints gbc) {
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBackground(new Color(0, 0, 0, 150));
        optionsPanel.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        gbc.gridy++;
        add(optionsPanel, gbc);

        GridBagConstraints gbcOptions = new GridBagConstraints();
        gbcOptions.insets = new Insets(10, 10, 10, 10);
        gbcOptions.gridx = 0;
        gbcOptions.gridy = 0;
        gbcOptions.anchor = GridBagConstraints.LINE_START;

        addPlayerNameField(optionsPanel, gbcOptions);
        addMultiplayerCheckbox(optionsPanel, gbcOptions);
        addDifficultyCombo(optionsPanel, gbcOptions);
        addShipCombo(optionsPanel, gbcOptions);
    }

    private void addMultiplayerCheckbox(JPanel panel, GridBagConstraints gbc) {
        gbc.gridy++;
        multiplayerCheckbox = new JCheckBox("Two-Player Mode (Local)");
        multiplayerCheckbox.setForeground(Color.WHITE);
        multiplayerCheckbox.setOpaque(false);
        panel.add(multiplayerCheckbox, gbc);
    }

    private void addPlayerNameField(JPanel panel, GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Player Name:");
>>>>>>> ce0b86bab71375ff8ae15ec4c34a3c16a883b4b1
        nameLabel.setForeground(Color.WHITE);
        nameField = new JTextField("Joueur");

        // Sélecteur de difficulté
        JLabel difficultyLabel = new JLabel("Difficulté:");
        difficultyLabel.setForeground(Color.WHITE);
        difficultySelector = new JComboBox<>(new String[]{"Facile", "Normal", "Difficile"});

<<<<<<< HEAD
        // Sélecteur de vaisseau
        JLabel shipLabel = new JLabel("Vaisseau:");
=======
    private void addDifficultyCombo(JPanel panel, GridBagConstraints gbc) {
        gbc.gridy++;
        JLabel levelLabel = new JLabel("Difficulty:");
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(levelLabel, gbc);

        gbc.gridy++;
        String[] levels = {"Easy", "Normal", "Hard"};
        JComboBox<String> levelCombo = new JComboBox<>(levels);
        levelCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        levelCombo.setSelectedIndex(1);
        panel.add(levelCombo, gbc);
    }

    private void addShipCombo(JPanel panel, GridBagConstraints gbc) {
        gbc.gridy++;
        JLabel shipLabel = new JLabel("Ship:");
>>>>>>> ce0b86bab71375ff8ae15ec4c34a3c16a883b4b1
        shipLabel.setForeground(Color.WHITE);
        shipSelector = new JComboBox<>(new String[]{"Vaisseau 1", "Vaisseau 2", "Vaisseau 3"});

<<<<<<< HEAD
        centerPanel.add(nameLabel);
        centerPanel.add(nameField);
        centerPanel.add(difficultyLabel);
        centerPanel.add(difficultySelector);
        centerPanel.add(shipLabel);
        centerPanel.add(shipSelector);

        add(centerPanel, BorderLayout.CENTER);

        // Panneau des boutons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(30, 30, 50));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));

        // Bouton pour démarrer le jeu
        JButton startButton = new JButton("JOUER");
        startButton.setPreferredSize(new Dimension(150, 50));
        styleButton(startButton, new Color(50, 205, 50));
        startButton.addActionListener(e -> {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Veuillez entrer un nom de joueur", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int difficulty = difficultySelector.getSelectedIndex() + 1;
            int shipType = shipSelector.getSelectedIndex();
            parent.startGame(playerName, difficulty, shipType, false);
        });

        // Bouton pour le mode multijoueur
        JButton multiplayerButton = new JButton("MULTIJOUEUR");
        multiplayerButton.setPreferredSize(new Dimension(150, 50));
        styleButton(multiplayerButton, new Color(70, 130, 180));
        multiplayerButton.addActionListener(e -> parent.showMultiplayerMenu());

        // Bouton pour les scores élevés
        JButton highscoresButton = new JButton("HIGH SCORES");
        highscoresButton.setPreferredSize(new Dimension(150, 50));
        styleButton(highscoresButton, new Color(218, 165, 32));
        highscoresButton.addActionListener(e -> parent.showHighscores());

        // Bouton pour quitter
        JButton quitButton = new JButton("QUITTER");
        quitButton.setPreferredSize(new Dimension(150, 50));
        styleButton(quitButton, new Color(178, 34, 34));
        quitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(startButton);
        buttonPanel.add(multiplayerButton);
        buttonPanel.add(highscoresButton);
        buttonPanel.add(quitButton);

        add(buttonPanel, BorderLayout.SOUTH);
=======
        gbc.gridy++;
        String[] ships = {"Standard", "Fast", "Heavy"};
        JComboBox<String> shipCombo = new JComboBox<>(ships);
        shipCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(shipCombo, gbc);
>>>>>>> ce0b86bab71375ff8ae15ec4c34a3c16a883b4b1
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
                button.setForeground(Color.BLACK);
            }
        });
    }
<<<<<<< HEAD
=======

    private void startGame() {
        String playerName = "Player1";
        Component[] components = ((JPanel) getComponent(1)).getComponents();

        for (Component comp : components) {
            if (comp instanceof JTextField) {
                playerName = ((JTextField) comp).getText().trim();
                if (playerName.isEmpty()) playerName = "Player1";
            }
        }

        boolean isMultiplayer = multiplayerCheckbox.isSelected();

        int difficulty = 3; // Normal default
        for (Component comp : components) {
            if (comp instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>) comp;
                if (combo.getItemAt(0).toString().equals("Easy")) {
                    switch (combo.getSelectedIndex()) {
                        case 0: difficulty = 1; break;
                        case 1: difficulty = 3; break;
                        case 2: difficulty = 5; break;
                    }
                }
            }
        }

        int shipType = 0; // Standard default
        for (Component comp : components) {
            if (comp instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>) comp;
                if (combo.getItemAt(0).toString().contains("Standard")) {
                    shipType = combo.getSelectedIndex();
                }
            }
        }

        parent.startGame(playerName, difficulty, shipType, isMultiplayer);
    }

    private void showHighscores() {
        parent.showHighscores();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawStarBackground(g);
    }

    private void drawStarBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Create a radial gradient for a nebula effect
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        float radius = Math.max(getWidth(), getHeight()) * 0.8f;
        float[] fractions = {0.1f, 0.4f, 0.7f, 1.0f};
        Color[] colors = {
                new Color(10, 0, 30),     // Deep purple
                new Color(50, 0, 80),     // Cosmic violet
                new Color(20, 10, 60),    // Space blue
                new Color(5, 0, 20)       // Dark void
        };

        RadialGradientPaint gradient = new RadialGradientPaint(
                center, radius, fractions, colors
        );

        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Add subtle twinkling stars
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            int x = (int) (Math.random() * getWidth());
            int y = (int) (Math.random() * getHeight());
            int size = 1 + (int) (Math.random() * 2);
            g2d.fillOval(x, y, size, size);
        }

        // Add a faint galaxy spiral
        g2d.setColor(new Color(70, 40, 120, 50));
        for (int i = 0; i < 5; i++) {
            int spiralX = center.x + (int) (Math.cos(i * 0.5) * i * 30);
            int spiralY = center.y + (int) (Math.sin(i * 0.5) * i * 30);
            g2d.fillOval(spiralX, spiralY, 200 - i * 30, 200 - i * 30);
        }
    }
>>>>>>> ce0b86bab71375ff8ae15ec4c34a3c16a883b4b1
}