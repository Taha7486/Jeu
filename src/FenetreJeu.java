import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FenetreJeu extends JFrame {
    private MenuPrincipal menuPanel;
    private Bouclejeu gamePanel;
    private HighScorePanel highScorePanel;
    private MultiplayerPanel multiplayerPanel;

    public FenetreJeu() {
        setTitle("Space Defender");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Couldn't set system look and feel: " + e.getMessage());
        }

        showMenu();
        setVisible(true);
    }

    public void showMenu() {
        cleanUpCurrentPanel();
        menuPanel = new MenuPrincipal(this);
        switchToPanel(menuPanel);
    }

    public void startGame(String playerName, int difficulty, int shipType, boolean isMultiplayer) {
        startGame(playerName, difficulty, shipType, isMultiplayer, "localhost");
    }

    public void startGame(String playerName, int difficulty, int shipType, boolean isMultiplayer, String serverAddress) {
        cleanUpCurrentPanel();
        gamePanel = new Bouclejeu(this, playerName, difficulty, shipType, isMultiplayer);
        if (isMultiplayer) {
            if (!serverAddress.equals("localhost")) {
                gamePanel.setServerAddress(serverAddress);
            }
        }
        switchToPanel(gamePanel);
    }

    public void showHighscores() {
        cleanUpCurrentPanel();
        highScorePanel = new HighScorePanel();
        switchToPanel(highScorePanel);
    }

    public void showMultiplayerMenu() {
        cleanUpCurrentPanel();
        multiplayerPanel = new MultiplayerPanel(this);
        switchToPanel(multiplayerPanel);
    }

    public void startMultiplayerServer() {
        try {
            new Thread(() -> {
                try {
                    Server.main(new String[]{});
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Erreur lors du démarrage du serveur: " + e.getMessage(),
                            "Erreur serveur", JOptionPane.ERROR_MESSAGE);
                }
            }).start();

            JOptionPane.showMessageDialog(this,
                    "Serveur démarré avec succès sur le port 5555.\nLes autres joueurs peuvent se connecter à votre IP.",
                    "Serveur démarré", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de démarrer le serveur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cleanUpCurrentPanel() {
        if (gamePanel != null) {
            gamePanel.cleanupMultiplayer();
            remove(gamePanel);
            gamePanel = null;
        }
        if (menuPanel != null) {
            remove(menuPanel);
            menuPanel = null;
        }
        if (highScorePanel != null) {
            remove(highScorePanel);
            highScorePanel = null;
        }
        if (multiplayerPanel != null) {
            remove(multiplayerPanel);
            multiplayerPanel = null;
        }
    }

    private void switchToPanel(JPanel panel) {
        getContentPane().removeAll();
        getContentPane().add(panel);
        revalidate();
        repaint();
        panel.requestFocusInWindow();
    }

    private static class MultiplayerPanel extends JPanel {
        private JTextField nameField;
        private JTextField serverField;
        private JComboBox<String> shipSelector;
        private JComboBox<String> difficultySelector;
        private FenetreJeu parent;

        public MultiplayerPanel(FenetreJeu parent) {
            this.parent = parent;
            setLayout(new BorderLayout());
            setBackground(new Color(30, 30, 50));

            // Titre
            JLabel title = new JLabel("MODE MULTIJOUEUR", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 36));
            title.setForeground(new Color(255, 215, 0));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            add(title, BorderLayout.NORTH);

            // Panel formulaire
            JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            formPanel.setBackground(new Color(30, 30, 50));
            formPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

            // Champ nom du joueur
            JLabel nameLabel = new JLabel("Nom du joueur:");
            nameLabel.setForeground(Color.WHITE);
            nameField = new JTextField("Joueur");

            // Champ adresse du serveur
            JLabel serverLabel = new JLabel("Adresse du serveur:");
            serverLabel.setForeground(Color.WHITE);
            serverField = new JTextField("localhost");

            // Sélecteur de vaisseau
            JLabel shipLabel = new JLabel("Vaisseau:");
            shipLabel.setForeground(Color.WHITE);
            shipSelector = new JComboBox<>(new String[]{"Vaisseau 1", "Vaisseau 2", "Vaisseau 3"});

            // Sélecteur de difficulté
            JLabel difficultyLabel = new JLabel("Difficulté:");
            difficultyLabel.setForeground(Color.WHITE);
            difficultySelector = new JComboBox<>(new String[]{"Facile", "Normal", "Difficile"});

            // Ajout des composants
            formPanel.add(nameLabel);
            formPanel.add(nameField);
            formPanel.add(serverLabel);
            formPanel.add(serverField);
            formPanel.add(shipLabel);
            formPanel.add(shipSelector);
            formPanel.add(difficultyLabel);
            formPanel.add(difficultySelector);

            add(formPanel, BorderLayout.CENTER);

            // Panel boutons
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(new Color(30, 30, 50));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));

            // Bouton Héberger
            JButton hostButton = new JButton("HÉBERGER UNE PARTIE");
            styleButton(hostButton, new Color(70, 130, 180));
            hostButton.addActionListener(e -> parent.startMultiplayerServer());

            // Bouton Rejoindre
            JButton joinButton = new JButton("REJOINDRE UNE PARTIE");
            styleButton(joinButton, new Color(50, 205, 50));
            joinButton.addActionListener(e -> {
                String playerName = nameField.getText().trim();
                if (playerName.isEmpty()) {
                    JOptionPane.showMessageDialog(parent,
                            "Veuillez entrer un nom de joueur",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                parent.startGame(playerName,
                        difficultySelector.getSelectedIndex() + 1,
                        shipSelector.getSelectedIndex(),
                        true,
                        serverField.getText());
            });

            // Bouton Retour
            JButton backButton = new JButton("RETOUR");
            styleButton(backButton, new Color(178, 34, 34));
            backButton.addActionListener(e -> parent.showMenu());

            buttonPanel.add(hostButton);
            buttonPanel.add(joinButton);
            buttonPanel.add(backButton);

            add(buttonPanel, BorderLayout.SOUTH);
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

    private static void styleButton(JButton button, Color color) {
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

class HighScorePanel extends JPanel {
    private JTabbedPane tabbedPane;

    public HighScorePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("MEILLEURS SCORES", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.YELLOW);
        add(titleLabel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();

        JPanel soloPanel = createScorePanel(false);
        tabbedPane.addTab("Mode Solo", soloPanel);

        JPanel multiPanel = createScorePanel(true);
        tabbedPane.addTab("Mode Multijoueur", multiPanel);

        JPanel sessionsPanel = createSessionsPanel();
        tabbedPane.addTab("Sessions Multijoueur", sessionsPanel);

        add(tabbedPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Retour au menu");
        backButton.addActionListener(e -> {
            Container parent = getParent();
            if (parent != null) {
                CardLayout cl = (CardLayout) parent.getLayout();
                cl.show(parent, "menu");
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createScorePanel(boolean multiplayer) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<String> scores = multiplayer ?
                GestionBaseDonnees.getHighScores(10, String.valueOf(true)) :
                GestionBaseDonnees.getHighScores(10, String.valueOf(false));

        JList<String> scoreList = new JList<>(scores.toArray(new String[0]));
        scoreList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        scoreList.setBackground(new Color(20, 20, 50));
        scoreList.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(scoreList);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSessionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<String> sessions = GestionBaseDonnees.getTopMultiplayerSessions(10);
        JList<String> sessionList = new JList<>(sessions.toArray(new String[0]));
        sessionList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        sessionList.setBackground(new Color(20, 20, 50));
        sessionList.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(sessionList);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void refreshScores() {
        tabbedPane.removeAll();
        tabbedPane.addTab("Mode Solo", createScorePanel(false));
        tabbedPane.addTab("Mode Multijoueur", createScorePanel(true));
        tabbedPane.addTab("Sessions Multijoueur", createSessionsPanel());
        revalidate();
        repaint();
    }
}