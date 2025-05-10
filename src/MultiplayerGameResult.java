import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MultiplayerGameResult extends JDialog {
    private final boolean isPvp;
    private String gameSessionId;
    private JTable resultsTable;
    private List<Map<String, Object>> sessionResults;

    public MultiplayerGameResult(JFrame parent, String gameSessionId, String playerName, int score, int level, boolean isPvp) {
        super(parent, "Résultats Multijoueur", true);
        this.gameSessionId = gameSessionId;
        this.isPvp = isPvp;

        // Enregistrer le score du joueur local
        GestionBaseDonnees.saveMultiplayerGameResult(
                playerName,
                score,
                level,
                isPvp ? "PvP" : "Coopératif",
                true, gameSessionId
        );

        // Attendre quelques secondes pour permettre aux autres clients d'enregistrer leurs scores
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Récupérer tous les résultats de cette session
        sessionResults = GestionBaseDonnees.getMultiplayerSessionResults(gameSessionId);

        initializeUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Titre
        String titleText = isPvp ? "Résultats du match PvP - Session " + gameSessionId
                : "Résultats de la partie coopérative - Session " + gameSessionId;
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Créer le tableau de résultats
        String[] columnNames = {"Position", "Joueur", "Score", "Niveau"};
        Object[][] data = new Object[sessionResults.size()][4];

        for (int i = 0; i < sessionResults.size(); i++) {
            Map<String, Object> result = sessionResults.get(i);
            data[i][0] = i + 1; // Position
            data[i][1] = result.get("playerName");
            data[i][2] = result.get("score");
            data[i][3] = result.get("level");
        }

        resultsTable = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Rendre le tableau non éditable
            }
        };

        // Amélioration de l'apparence du tableau
        resultsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        resultsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        resultsTable.setRowHeight(25);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(500, 250));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton closeButton = new JButton("Fermer");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.addActionListener(e -> dispose());

        JButton rematchButton = new JButton("Revanche");
        rematchButton.setFont(new Font("Arial", Font.BOLD, 14));
        rematchButton.addActionListener(e -> {
            // Logique pour relancer une partie avec les mêmes joueurs
            JOptionPane.showMessageDialog(this, "Fonctionnalité à implémenter");
        });

        buttonPanel.add(rematchButton);
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setResizable(false);
    }

    public static void main(String[] args) {
        // Exemple de test
        SwingUtilities.invokeLater(() -> {
            MultiplayerGameResult dialog = new MultiplayerGameResult(
                    null,
                    "ABC123",
                    "TestPlayer",
                    1500,
                    3,
                    true
            );
            dialog.setVisible(true);
        });
    }
}