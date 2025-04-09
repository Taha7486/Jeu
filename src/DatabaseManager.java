import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/space_defender?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root"; // Remplace par ton utilisateur MySQL
    private static final String DB_PASS = "";     // Remplace par ton mot de passe MySQL

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        }
    }

    public static boolean saveGameResult(String playerName, int score, int level, String difficulty) {
        String sql = "INSERT INTO game_results (player_name, score, level, difficulty) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerName);
            stmt.setInt(2, score);
            stmt.setInt(3, level);
            stmt.setString(4, difficulty);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving score: " + e.getMessage());
            return false;
        }
    }

    public static List<String> getHighScores(int limit) {
        List<String> scores = new ArrayList<>();
        String sql = "SELECT player_name, MAX(score) as max_score, level, difficulty, " +
                "DATE_FORMAT(achieved_on, '%d/%m/%Y %H:%i') as date " +
                "FROM game_results " +
                "GROUP BY player_name " +  // On groupe par joueur
                "ORDER BY max_score DESC LIMIT ?";  // On trie par meilleur score

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String entry = String.format("%s - %d pts (Niv.%d %s) le %s",
                        rs.getString("player_name"),
                        rs.getInt("max_score"),  // On utilise max_score au lieu de score
                        rs.getInt("level"),
                        rs.getString("difficulty"),
                        rs.getString("date"));

                scores.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("Error getting highscores: " + e.getMessage());
        }
        return scores;
    }

    public static void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS game_results (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "player_name VARCHAR(50) NOT NULL, " +
                "score INT NOT NULL, " +
                "level INT NOT NULL, " +
                "difficulty VARCHAR(20) NOT NULL, " +
                "achieved_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
}