
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuPanel extends JPanel {
    private final GameWindow parent;
    private Clip menuMusic;

    public MenuPanel(GameWindow parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        setBackground(new Color(30, 30, 50));
        setupUI();
        playMenuMusic();
    }

    private void setupUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        addTitle(gbc);
        addOptionsPanel(gbc);
        addStartButton(gbc);
        addHighscoresButton(gbc);
        addSettingsButton(gbc);
        addChatButton(gbc);

        setupKeyboardShortcut();
    }

    private void addTitle(GridBagConstraints gbc) {
        JLabel title = new JLabel("SPACE DEFENDER", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(new Color(255, 215, 0));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        add(title, gbc);
    }

    private void addOptionsPanel(GridBagConstraints gbc) {
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBackground(new Color(0, 0, 0, 150));
        optionsPanel.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        gbc.gridy++;
        add(optionsPanel, gbc);

        GridBagConstraints gbcOptions = new GridBagConstraints();
        gbcOptions.insets = new Insets(10, 10, 10, 10);
        gbcOptions.gridx = 0;
        gbcOptions.gridy = 0;
        gbcOptions.anchor = GridBagConstraints.LINE_START;

        addPlayerNameField(optionsPanel, gbcOptions);
        addDifficultyCombo(optionsPanel, gbcOptions);
        addShipCombo(optionsPanel, gbcOptions);
    }

    private void addPlayerNameField(JPanel panel, GridBagConstraints gbc) {
        JLabel nameLabel = new JLabel("Player Name:");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(nameLabel, gbc);

        gbc.gridy++;
        JTextField nameField = new JTextField(15);
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));
        nameField.setText("Player1");
        panel.add(nameField, gbc);
    }

    private void addDifficultyCombo(JPanel panel, GridBagConstraints gbc) {
        gbc.gridy++;
        JLabel levelLabel = new JLabel("Difficulty:");
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(levelLabel, gbc);

        gbc.gridy++;
        String[] levels = { "Easy", "Normal", "Hard", "Extreme" };
        JComboBox<String> levelCombo = new JComboBox<>(levels);
        levelCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        levelCombo.setSelectedIndex(1);
        panel.add(levelCombo, gbc);
    }

    private void addShipCombo(JPanel panel, GridBagConstraints gbc) {
        gbc.gridy++;
        JLabel shipLabel = new JLabel("Ship:");
        shipLabel.setForeground(Color.WHITE);
        shipLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(shipLabel, gbc);

        gbc.gridy++;
        String[] ships = {
                "Standard Fighter (balanced)",
                "Fast Interceptor (speed++)",
                "Heavy Cruiser (damage++)"
        };
        JComboBox<String> shipCombo = new JComboBox<>(ships);
        shipCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(shipCombo, gbc);
    }

    private void addStartButton(GridBagConstraints gbc) {
        gbc.gridy++;
        JButton startButton = createStyledButton("START GAME");
        startButton.addActionListener(e -> startGame());
        add(startButton, gbc);
    }

    private void addHighscoresButton(GridBagConstraints gbc) {
        gbc.gridy++;
        JButton highscoresButton = createStyledButton("HIGH SCORES", new Color(46, 139, 87));
        highscoresButton.addActionListener(e -> showHighscores());
        add(highscoresButton, gbc);
    }

    private void addSettingsButton(GridBagConstraints gbc) {
        gbc.gridy++;
        JButton settingsButton = createStyledButton("SETTINGS", new Color(139, 0, 139));
        settingsButton.addActionListener(e -> showSettings());
        add(settingsButton, gbc);
    }

    private JButton createStyledButton(String text) {
        return createStyledButton(text, new Color(70, 130, 180));
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
                SoundManager.playSound("/button_hover.wav");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void setupKeyboardShortcut() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start");
        getActionMap().put("start", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });
    }

    private void playMenuMusic() {


        SoundManager.playMusic("/menu_music.wav");

    }

    private void startGame() {
        SoundManager.playSound("/button_click.wav");

        String playerName = "Player1"; // Default name
        Component[] components = ((JPanel)getComponent(1)).getComponents();
        for (Component comp : components) {
            if (comp instanceof JTextField) {
                playerName = ((JTextField)comp).getText().trim();
                if (playerName.isEmpty()) playerName = "Player1";
            }
        }

        int difficulty = 3; // Normal default
        for (Component comp : components) {
            if (comp instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>)comp;
                if (combo.getItemAt(0).toString().equals("Easy")) {
                    switch (combo.getSelectedIndex()) {
                        case 0: difficulty = 1; break;
                        case 1: difficulty = 3; break;
                        case 2: difficulty = 5; break;
                        case 3: difficulty = 7; break;
                    }
                }
            }
        }

        int shipType = 0; // Standard default
        for (Component comp : components) {
            if (comp instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>)comp;
                if (combo.getItemAt(0).toString().contains("Standard")) {
                    shipType = combo.getSelectedIndex();
                }
            }
        }

        parent.startGame(playerName, difficulty, shipType);
    }

    private void showHighscores() {
        SoundManager.playSound("/button_click.wav");
        parent.showHighscores();
    }

    private void showSettings() {
        SoundManager.playSound("/button_click.wav");
        parent.showSettings();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawStarBackground(g);
    }

    private void drawStarBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        for (int i = 0; i < 100; i++) {
            int x = (int)(Math.random() * getWidth());
            int y = (int)(Math.random() * getHeight());
            int size = 1 + (int)(Math.random() * 2);
            g2d.fillOval(x, y, size, size);
        }
    }

    private void addChatButton(GridBagConstraints gbc) {
        gbc.gridy++;
        JButton chatButton = new JButton("CHAT (Coming Soon)");
        chatButton.setFont(new Font("Arial", Font.BOLD, 16));
        chatButton.setBackground(new Color(255, 165, 0));
        chatButton.setForeground(Color.BLACK);
        chatButton.setFocusPainted(false);
        chatButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        chatButton.addActionListener(e -> {
            SoundManager.playSound("/button_click.wav");
            JOptionPane.showMessageDialog(this,
                    "Chat system will be fully functional in multiplayer mode",
                    "Chat Info",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        chatButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                chatButton.setBackground(new Color(255, 185, 60));
                SoundManager.playSound("/button_hover.wav");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                chatButton.setBackground(new Color(255, 165, 0));
            }
        });

        add(chatButton, gbc);
    }
}