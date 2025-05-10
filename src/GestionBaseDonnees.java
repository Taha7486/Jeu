import java.sql.*;
import java.util.*;


public class GestionBaseDonnees {
    // Configuration de la connexion
    private static final String DB_URL = "jdbc:mysql://localhost:3306/space_defender?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root"; // À remplacer
    private static final String DB_PASS = "";     // À remplacer

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trouvé : " + e.getMessage());
        }
    }

    /**
     * Enregistre un résultat de partie en SOLO
     /**
     * Enregistre un résultat de partie en SOLO
     */
    public static boolean saveSoloGameResult(String playerName, int score, int level, String difficulty) {
        return saveGameResult(playerName, score, level, difficulty, "solo", null);
    }

    /**
     * Enregistre un résultat de partie en MULTIJOUEUR
     */
    public static boolean saveMultiplayerGameResult(String playerName, int score, int level,
                                                    String difficulty, boolean b, String gameSessionId) {
        return saveGameResult(playerName, score, level, difficulty, "multiplayer", gameSessionId);
    }

    private static boolean saveGameResult(String playerName, int score, int level,
                                          String difficulty, String gameMode, String gameSessionId) {
        String sql = "INSERT INTO game_results (player_name, score, level, difficulty, game_mode, game_session_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerName);
            stmt.setInt(2, score);
            stmt.setInt(3, level);
            stmt.setString(4, difficulty);
            stmt.setString(5, gameMode);
            stmt.setString(6, gameSessionId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur sauvegarde score : " + e.getMessage());
            return false;
        }
    }


    /**
     * Récupère les meilleurs scores SOLO
     */
    public static List<String> getSoloHighScores(int limit, boolean multiplayer) {
        return getHighScores(limit, "solo");
    }

    /**
     * Récupère les meilleurs scores MULTIJOUEUR
     */
    public static List<String> getMultiplayerHighScores(int limit) {
        return getHighScores(limit, "multiplayer");
    }

    public static List<String> getHighScores(int limit, String gameMode) {
        List<String> scores = new ArrayList<>();
        String sql = "SELECT player_name, score, level, difficulty, " +
                "DATE_FORMAT(achieved_on, '%d/%m/%Y') as date " +
                "FROM game_results " +
                "WHERE game_mode = ? " +
                "ORDER BY score DESC LIMIT ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, gameMode);
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                scores.add(String.format("%s - %d pts (Niv.%d %s) le %s",
                        rs.getString("player_name"),
                        rs.getInt("score"),
                        rs.getInt("level"),
                        rs.getString("difficulty"),
                        rs.getString("date")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération scores : " + e.getMessage());
        }
        return scores;
    }

    /**
     * Récupère les meilleures sessions multijoueur
     */
    /**
     * Récupère les résultats d'une session multijoueur spécifique
     */
    public static List<Map<String, Object>> getMultiplayerSessionResults(String sessionId) {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT player_name, score, level FROM game_results " +
                "WHERE game_session_id = ? " +
                "ORDER BY score DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("playerName", rs.getString("player_name"));
                row.put("score", rs.getInt("score"));
                row.put("level", rs.getInt("level"));
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération session: " + e.getMessage());
        }
        return results;
    }
    public static List<String> getTopMultiplayerSessions(int limit) {
        List<String> sessions = new ArrayList<>();
        String sql = "SELECT game_session_id, " +
                "DATE_FORMAT(MIN(achieved_on), '%d/%m/%Y %H:%i') as session_date, " +
                "COUNT(DISTINCT player_name) as player_count, " +
                "MAX(score) as top_score " +
                "FROM game_results " +
                "WHERE game_mode = 'multiplayer' " +
                "GROUP BY game_session_id " +
                "ORDER BY top_score DESC LIMIT ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sessions.add(String.format("Session %s - %d joueurs - Meilleur score: %d",
                        rs.getString("session_date"),
                        rs.getInt("player_count"),
                        rs.getInt("top_score")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération sessions : " + e.getMessage());
        }
        return sessions;
    }

    /**
     * Génère un ID unique pour une session multijoueur
     */
    public static String generateSessionId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Teste la connexion à la base
     */
    public static boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            return true;
        } catch (SQLException e) {
            System.err.println("Échec connexion DB : " + e.getMessage());
            return false;
        }
    }

}