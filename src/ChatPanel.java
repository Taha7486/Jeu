import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChatPanel extends JPanel {
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendButton;

    public ChatPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 200));
        setBorder(BorderFactory.createTitledBorder("Chat"));

        // Chat display area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(30, 30, 40));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        inputField = new JTextField();
        inputField.setBackground(new Color(50, 50, 60));
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.addActionListener(this::sendMessage);

        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(this::sendMessage);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void sendMessage(ActionEvent e) {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            addMessage("You: " + message);
            inputField.setText("");
            SoundManager.playSound("/message_sent.wav");
        }
    }

    public void addMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public JTextField getInputField() {
        return inputField;
    }
}