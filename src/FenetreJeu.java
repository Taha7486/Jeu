
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
            System.err.println("Couldn't set system ");
        }

        showMenu();
        setVisible(true);
    }

    public void showMenu() {
        cleanUpCurrentPanel();
        menuPanel = new MenuPrincipal(this);
        switchToPanel(menuPanel);
    }

    public void startGame(String playerName, int difficulty, int shipType) {
        cleanUpCurrentPanel();
        gamePanel = new Bouclejeu(this, playerName, difficulty, shipType);
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

            JLabel title = new JLabel("HIGH SCORES", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 36));
            title.setForeground(new Color(255, 215, 0));
            title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            add(title, BorderLayout.NORTH);

            JTextArea scoresArea = new JTextArea();
            scoresArea.setEditable(false);
            scoresArea.setBackground(new Color(30, 30, 50));
            scoresArea.setForeground(Color.WHITE);
            scoresArea.setFont(new Font("Arial", Font.PLAIN, 18));

            // Utilisez DatabaseManager au lieu de HighscoreManager
            List<String> highscores = GestionBaseDonnees.getHighScores(10); // 10 meilleurs scores
            if (highscores.isEmpty()) {
                scoresArea.setText("No scores recorded yet");
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < highscores.size(); i++) {
                    // Formatage amélioré
                    sb.append(String.format("%2d. %s%n", i+1, highscores.get(i)));
                }
                scoresArea.setText(sb.toString());
            }

            JScrollPane scrollPane = new JScrollPane(scoresArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            add(scrollPane, BorderLayout.CENTER);

            JButton backButton = new JButton("BACK TO MENU");
            backButton.addActionListener(e -> parent.showMenu());
            styleButton(backButton, new Color(70, 130, 180));

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(new Color(30, 30, 50));
            buttonPanel.add(backButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    private static void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(Color.BLACK); // Changé de WHITE à BLACK
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {

            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
                button.setForeground(Color.BLACK);
            }
        });
    }
}
