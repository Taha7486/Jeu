
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
//Charge et cache les images pour éviter de les recharger
public class ImageLoader {
    //Utilise une HashMap pour stocker les images
    private static final HashMap<String, Image> images = new HashMap<>();

    public static Image getImage(String filename) {
        try {
            return ImageIO.read(new File(filename));
        } catch (IOException e) {
            System.err.println("Erreur de chargement: " + filename);
            e.printStackTrace();
            return null;
        }
    }
}
