import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FenetreJeu extends JFrame {
    private MenuPrincipal menuPanel;
    private Bouclejeu gamePanel;
    private HighscorePanel highscorePanel;

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
        cleanUpCurrentPanel();
        gamePanel = new Bouclejeu(this, playerName, difficulty, shipType, isMultiplayer);
        switchToPanel(gamePanel);
    }

    public void showHighscores() {
        cleanUpCurrentPanel();
        highscorePanel = new HighscorePanel(this);
        switchToPanel(highscorePanel);
    }

    private void cleanUpCurrentPanel() {
        if (gamePanel != null) {
            gamePanel.cleanUp();
            remove(gamePanel);
            gamePanel = null;
        }
        if (menuPanel != null) {
            remove(menuPanel);
            menuPanel = null;
        }
        if (highscorePanel != null) {
            remove(highscorePanel);
            highscorePanel = null;
        }
    }

    private void switchToPanel(JPanel panel) {
        add(panel);
        revalidate();
        repaint();
        panel.requestFocusInWindow();
    }

    private static class HighscorePanel extends JPanel {
        public HighscorePanel(FenetreJeu parent) {
            setLayout(new BorderLayout());
            setBackground(new Color(30, 30, 50));

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Single Player", createSinglePlayerScoresPanel());
            tabbedPane.addTab("Two Players", createTwoPlayerResultsPanel());

            add(tabbedPane, BorderLayout.CENTER);
            add(createBackButtonPanel(parent), BorderLayout.SOUTH);
        }

        private JPanel createSinglePlayerScoresPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(new Color(30, 30, 50));

            JTextArea scoresArea = new JTextArea();
            scoresArea.setEditable(false);
            scoresArea.setBackground(new Color(30, 30, 50));
            scoresArea.setForeground(Color.WHITE);
            scoresArea.setFont(new Font("Arial", Font.PLAIN, 18));

            List<String> highscores = GestionBaseDonnees.getHighScores(10);
            if (highscores.isEmpty()) {
                scoresArea.setText("No single player scores recorded yet");
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < highscores.size(); i++) {
                    sb.append(String.format("%2d. %s%n", i+1, highscores.get(i)));
                }
                scoresArea.setText(sb.toString());
            }

            JScrollPane scrollPane = new JScrollPane(scoresArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            panel.add(scrollPane, BorderLayout.CENTER);

            return panel;
        }

        private JPanel createTwoPlayerResultsPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(new Color(30, 30, 50));

            JTextArea resultsArea = new JTextArea();
            resultsArea.setEditable(false);
            resultsArea.setBackground(new Color(30, 30, 50));
            resultsArea.setForeground(Color.WHITE);
            resultsArea.setFont(new Font("Arial", Font.PLAIN, 18));

            List<String> results = GestionBaseDonnees.getTwoPlayerResults(10);
            if (results.isEmpty()) {
                resultsArea.setText("No two-player matches recorded yet");
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < results.size(); i++) {
                    sb.append(String.format("%2d. %s%n", i+1, results.get(i)));
                }
                resultsArea.setText(sb.toString());
            }

            JScrollPane scrollPane = new JScrollPane(resultsArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            panel.add(scrollPane, BorderLayout.CENTER);

            return panel;
        }

        private JPanel createBackButtonPanel(FenetreJeu parent) {
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(new Color(30, 30, 50));

            JButton backButton = new JButton("BACK TO MENU");
            backButton.addActionListener(e -> parent.showMenu());
            styleButton(backButton, new Color(70, 130, 180));

            buttonPanel.add(backButton);
            return buttonPanel;
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