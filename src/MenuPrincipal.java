import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuPrincipal extends JPanel {
    private FenetreJeu parent;
    private JTextField nameField;
    private JComboBox<String> difficultySelector;
    private JComboBox<String> shipSelector;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private HighScorePanel highScorePanel;

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

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        JPanel menuPanel = createMenuPanel();
        mainPanel.add(menuPanel, "menu");

        // Création du panneau de scores
        highScorePanel = new HighScorePanel();
        mainPanel.add(highScorePanel, "scores");

        add(mainPanel, BorderLayout.CENTER);

        // Afficher le menu par défaut
        cardLayout.show(mainPanel, "menu");

        // Panneau central avec options de jeu
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(0, 2, 10, 10));
        centerPanel.setBackground(new Color(30, 30, 50));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        // Champ nom du joueur
        JLabel nameLabel = new JLabel("Nom du joueur:");
        nameLabel.setForeground(Color.WHITE);
        nameField = new JTextField("Joueur");

        // Sélecteur de difficulté
        JLabel difficultyLabel = new JLabel("Difficulté:");
        difficultyLabel.setForeground(Color.WHITE);
        difficultySelector = new JComboBox<>(new String[]{"Facile", "Normal", "Difficile"});

        // Sélecteur de vaisseau
        JLabel shipLabel = new JLabel("Vaisseau:");
        shipLabel.setForeground(Color.WHITE);
        shipSelector = new JComboBox<>(new String[]{"Vaisseau 1", "Vaisseau 2", "Vaisseau 3"});

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
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(20, 20, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);

        // Bouton Mode Solo
        JButton soloButton = createButton("Mode Solo");
        soloButton.addActionListener(e -> showDifficultySelection(false));
        panel.add(soloButton, gbc);

        // Bouton Mode Multijoueur
        JButton multiButton = createButton("Mode Multijoueur");
        multiButton.addActionListener(e -> showMultiplayerOptions());
        panel.add(multiButton, gbc);

        // Bouton Meilleurs Scores
        JButton scoresButton = createButton("Meilleurs Scores");
        scoresButton.addActionListener(e -> showHighScores());
        panel.add(scoresButton, gbc);

        // Bouton Quitter
        JButton quitButton = createButton("Quitter");
        quitButton.addActionListener(e -> System.exit(0));
        panel.add(quitButton, gbc);

        return panel;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(50, 50, 150));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(300, 60));
        return button;
    }

    private void showDifficultySelection(boolean isMultiplayer) {
        // Implémentation de la méthode
    }

    private void showMultiplayerOptions() {
        // Implémentation de la méthode
    }

    private void showHighScores() {
        if (highScorePanel != null) {
            highScorePanel.refreshScores();
            cardLayout.show(mainPanel, "scores");
        }
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
}