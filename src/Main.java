import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ResourceManager.preloadResources();

            GameWindow window = new GameWindow();
            window.setLocationRelativeTo(null);

        });
    }
}
