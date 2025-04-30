import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

public class GestionRessources {
    private static final Map<String, Image> images = new HashMap<>();

    public static void preloadResources() {
        System.out.println("Préchargement des ressources...");
        loadImage("background.png");
        loadImage("game_icon.png");

        for (int i = 0; i < 3; i++) {
            loadImage("ship_" + i + ".png");
            loadImage("enemy_" + (i == 0 ? "basic" : i == 1 ? "fast" : "tank") + ".png");
        }
<<<<<<< HEAD
        System.out.println("Préchargement terminé. Images chargées: " + images.size());
=======
>>>>>>> ce0b86bab71375ff8ae15ec4c34a3c16a883b4b1
    }

    public static Image getImage(String filename) {
        if (!images.containsKey(filename)) {
            loadImage(filename);
        }
        return images.getOrDefault(filename, createPlaceholderImage());
    }

    private static void loadImage(String filename) {
        try {
<<<<<<< HEAD
            // Essayer de charger depuis le dossier resources
            String basePath = new File("").getAbsolutePath();
            File resourceFile = new File(basePath + "/src/resources/" + filename);
            System.out.println("Tentative de chargement depuis: " + resourceFile.getAbsolutePath());
            
            if (!resourceFile.exists()) {
                System.err.println("Fichier non trouvé: " + resourceFile.getAbsolutePath());
                throw new Exception("File not found: " + filename);
            }

            BufferedImage image = ImageIO.read(resourceFile);
            if (image == null) {
                throw new Exception("Failed to load image: " + filename);
            }
            
            System.out.println("Image chargée avec succès: " + filename);
=======
            BufferedImage image = ImageIO.read(GestionRessources.class.getResourceAsStream(filename));
>>>>>>> ce0b86bab71375ff8ae15ec4c34a3c16a883b4b1
            images.put(filename, image);
        } catch (Exception e) {
            System.err.println("Erreur de chargement de l'image: " + filename);
            System.err.println("Message d'erreur: " + e.getMessage());
            e.printStackTrace();
            images.put(filename, createPlaceholderImage());
        }
    }

    private static Image createPlaceholderImage() {
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.MAGENTA);
        g2d.fillRect(0, 0, 50, 50);
        g2d.setColor(Color.BLACK);
        g2d.drawString("X", 20, 30);
        g2d.dispose();
        return img;
    }
}