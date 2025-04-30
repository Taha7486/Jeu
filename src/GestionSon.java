import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GestionSon {
    private static final Map<String, Clip> soundCache = new HashMap<>();
    private static final ExecutorService soundPool = Executors.newFixedThreadPool(4);
    private static float volume = 1.0f;
    private static boolean soundEnabled = true;
    private static Clip backgroundMusic;

    // Liste des effets sonores
    public static final String SOUND_SHOOT = "shoot.wav";
    public static final String SOUND_EXPLOSION = "explosion.wav";
    public static final String SOUND_HIT = "hit.wav";
    public static final String SOUND_POWERUP = "powerup.wav";
    public static final String SOUND_GAME_OVER = "gameover.wav";
    public static final String SOUND_VICTORY = "victory.wav";

    static {
        // Précharger les sons au démarrage
        try {
            loadSound(SOUND_SHOOT);
            // Charger les autres sons quand ils seront disponibles
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des sons: " + e.getMessage());
        }
    }

    public static void loadSound(String filename) {
        if (soundCache.containsKey(filename)) return;

        try {
            InputStream audioSrc = GestionSon.class.getResourceAsStream("/resources/" + filename);
            if (audioSrc == null) {
                audioSrc = GestionSon.class.getResourceAsStream("/" + filename);
            }
            if (audioSrc == null) {
                throw new RuntimeException("Son non trouvé: " + filename);
            }

            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            // Appliquer le volume actuel
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(20f * (float) Math.log10(volume));
            }
            
            soundCache.put(filename, clip);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du son " + filename + ": " + e.getMessage());
        }
    }

    public static void playSound(String filename) {
        if (!soundEnabled) return;

        soundPool.execute(() -> {
            try {
                Clip clip = soundCache.get(filename);
                if (clip == null) {
                    loadSound(filename);
                    clip = soundCache.get(filename);
                }
                if (clip != null) {
                    clip.setFramePosition(0);
                    clip.start();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la lecture du son " + filename + ": " + e.getMessage());
            }
        });
    }

    public static void playSoundWithPitch(String filename, float pitch) {
        if (!soundEnabled) return;

        soundPool.execute(() -> {
            try {
                Clip clip = soundCache.get(filename);
                if (clip == null) {
                    loadSound(filename);
                    clip = soundCache.get(filename);
                }
                if (clip != null) {
                    if (clip.isControlSupported(FloatControl.Type.SAMPLE_RATE)) {
                        FloatControl pitchControl = (FloatControl) clip.getControl(FloatControl.Type.SAMPLE_RATE);
                        pitchControl.setValue(pitchControl.getValue() * pitch);
                    }
                    clip.setFramePosition(0);
                    clip.start();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la lecture du son " + filename + ": " + e.getMessage());
            }
        });
    }

    public static void playBackgroundMusic(String filename, boolean loop) {
        if (!soundEnabled) return;

        try {
            if (backgroundMusic != null) {
                backgroundMusic.stop();
                backgroundMusic.close();
            }

            loadSound(filename);
            backgroundMusic = soundCache.get(filename);
            if (backgroundMusic != null) {
                if (loop) {
                    backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                }
                backgroundMusic.start();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de la musique de fond: " + e.getMessage());
        }
    }

    public static void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public static void setVolume(float newVolume) {
        volume = Math.max(0.0f, Math.min(1.0f, newVolume));
        for (Clip clip : soundCache.values()) {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(20f * (float) Math.log10(volume));
            }
        }
    }

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        if (!enabled && backgroundMusic != null) {
            backgroundMusic.stop();
        } else if (enabled && backgroundMusic != null) {
            backgroundMusic.start();
        }
    }

    public static void cleanup() {
        for (Clip clip : soundCache.values()) {
            clip.close();
        }
        soundCache.clear();
        if (backgroundMusic != null) {
            backgroundMusic.close();
        }
        soundPool.shutdown();
    }
} 