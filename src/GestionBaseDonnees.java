import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionBaseDonnees {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/space_defender?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

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
                "GROUP BY player_name " +
                "ORDER BY max_score DESC LIMIT ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String entry = String.format("%s - %d pts (Niv.%d %s) le %s",
                        rs.getString("player_name"),
                        rs.getInt("max_score"),
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

    public static List<String> getTwoPlayerResults(int limit) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT player1_name, player1_score, player2_name, player2_score, " +
                "DATE_FORMAT(played_on, '%d/%m/%Y %H:%i') as date " +
                "FROM two_player_results " +
                "ORDER BY GREATEST(player1_score, player2_score) DESC LIMIT ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String entry = String.format("%s %d - %d %s le %s",
                        rs.getString("player1_name"),
                        rs.getInt("player1_score"),
                        rs.getInt("player2_score"),
                        rs.getString("player2_name"),
                        rs.getString("date"));

                results.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("Error getting two-player results: " + e.getMessage());
        }
        return results;
    }
}