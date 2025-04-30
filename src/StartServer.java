public class StartServer {
    public static void main(String[] args) {
        try {
            System.out.println("Démarrage du serveur Space Defender...");
            ServerManager.startServer();
            System.out.println("Serveur démarré avec succès!");
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage du serveur: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}