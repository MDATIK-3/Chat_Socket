package src.ui.components;

import src.client.Client;
import src.models.Message;
import src.ui.ChatUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(InputPanel.class.getName());
    private static final int BUTTON_SIZE = 36;
    private static final int BUTTON_MARGIN = 3;
    private static final int PANEL_SPACING = 8;
    
    private static final Color COLOR_BACKGROUND = new Color(245, 245, 250);
    private static final Color COLOR_BUTTON_BG = new Color(240, 240, 245);
    private static final Color COLOR_BUTTON_BORDER = new Color(200, 200, 210);
    private static final Color COLOR_BUTTON_TEXT = new Color(50, 50, 60);
    private static final Color COLOR_PRIMARY = new Color(25, 118, 210);
    private static final Color COLOR_PRIMARY_BORDER = new Color(13, 71, 161);
    private static final Color COLOR_INPUT_BG = Color.WHITE;
    private static final Color COLOR_INPUT_BORDER = new Color(210, 210, 220);
    
    private final ChatUI chatUI;
    private final JTextField messageField;
    private final JToggleButton boldButton;
    private final JToggleButton italicButton;
    private final JButton fileButton;
    private final JButton emojiButton;
    private final JButton sendButton;

    public InputPanel(ChatUI chatUI) {
        this.chatUI = chatUI;
        
        setLayout(new BorderLayout(PANEL_SPACING, PANEL_SPACING));
        setBorder(new EmptyBorder(PANEL_SPACING, 0, 0, 0));
        setBackground(COLOR_BACKGROUND);

        // Initialize all components first
        boldButton = new JToggleButton("B");
        boldButton.setFont(new Font("Dialog", Font.BOLD, 12));
        boldButton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        boldButton.setMargin(new Insets(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN));
        boldButton.setBackground(COLOR_BUTTON_BG);
        boldButton.setForeground(COLOR_BUTTON_TEXT);
        boldButton.setFocusPainted(false);
        boldButton.setToolTipText("Bold (Ctrl+B)");
        boldButton.setBorder(new LineBorder(COLOR_BUTTON_BORDER, 1, true));
        
        italicButton = new JToggleButton("I");
        italicButton.setFont(new Font("Dialog", Font.ITALIC, 12));
        italicButton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        italicButton.setMargin(new Insets(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN));
        italicButton.setBackground(COLOR_BUTTON_BG);
        italicButton.setForeground(COLOR_BUTTON_TEXT);
        italicButton.setFocusPainted(false);
        italicButton.setToolTipText("Italic (Ctrl+I)");
        italicButton.setBorder(new LineBorder(COLOR_BUTTON_BORDER, 1, true));
        
        fileButton = new JButton("📎");
        fileButton.setFont(new Font("Dialog", Font.PLAIN, 14));
        fileButton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        fileButton.setMargin(new Insets(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN));
        fileButton.setBackground(COLOR_BUTTON_BG);
        fileButton.setForeground(COLOR_BUTTON_TEXT);
        fileButton.setFocusPainted(false);
        fileButton.setToolTipText("Attach File");
        fileButton.setBorder(new LineBorder(COLOR_BUTTON_BORDER, 1, true));
        fileButton.addActionListener(e -> handleFileAttachment());
        
        emojiButton = new JButton("😊");
        emojiButton.setFont(new Font("Dialog", Font.PLAIN, 14));
        emojiButton.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        emojiButton.setMargin(new Insets(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN));
        emojiButton.setBackground(COLOR_BUTTON_BG);
        emojiButton.setForeground(COLOR_BUTTON_TEXT);
        emojiButton.setFocusPainted(false);
        emojiButton.setToolTipText("Insert Emoji");
        emojiButton.setBorder(new LineBorder(COLOR_BUTTON_BORDER, 1, true));
        emojiButton.addActionListener(e -> showEmojiPicker());
        
        messageField = new JTextField();
        messageField.setBorder(new CompoundBorder(
                new LineBorder(COLOR_INPUT_BORDER, 1, true),
                new EmptyBorder(8, 8, 8, 8)));
        messageField.setFont(new Font("Dialog", Font.PLAIN, 14));
        messageField.setBackground(COLOR_INPUT_BG);
        messageField.addActionListener(e -> sendMessage());
        
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(70, 36));
        sendButton.setBackground(COLOR_PRIMARY);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(new LineBorder(COLOR_PRIMARY_BORDER, 1, true));
        sendButton.setToolTipText("Send Message (Enter)");
        sendButton.addActionListener(e -> sendMessage());
        
        // Setup keyboard shortcuts
        setupKeyboardShortcuts();
        
        // Create format panel and add components
        JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PANEL_SPACING, 0));
        formatPanel.setBackground(COLOR_BACKGROUND);
        formatPanel.add(boldButton);
        formatPanel.add(italicButton);
        formatPanel.add(fileButton);
        formatPanel.add(emojiButton);
        
        // Add all components to main panel
        add(formatPanel, BorderLayout.NORTH);
        add(messageField, BorderLayout.CENTER);
        add(sendButton, BorderLayout.EAST);
    }

    private void setupKeyboardShortcuts() {
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_B) {
                    boldButton.doClick();
                    e.consume();
                }
                else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_I) {
                    italicButton.doClick();
                    e.consume();
                }
            }
        });
    }

    private void sendMessage() {
        String content = messageField.getText().trim();
        if (content.isEmpty() || Client.getConnection() == null) {
            return;
        }

        try {
            String formattedContent = content;
            if (boldButton.isSelected()) {
                formattedContent = "*" + formattedContent + "*";
            }
            if (italicButton.isSelected()) {
                formattedContent = "_" + formattedContent + "_";
            }
            
            String currentTab = chatUI.getChatTabs().getTitleAt(chatUI.getChatTabs().getSelectedIndex());
            String messageType = currentTab.equals(ChatUI.GROUP_TAB) ? "group" : "private";
            String recipient = currentTab.equals(ChatUI.GROUP_TAB) ? "ALL" : currentTab;

            Message message = new Message(Client.getClientName(), recipient, formattedContent, messageType);
            Client.getConnection().sendMessage(message);

            messageField.setText("");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error sending message", e);
            chatUI.appendToChat(chatUI.getChatPane(), "System", 
                "Error sending message: " + e.getMessage(), Color.RED);
        }
    }

    private void handleFileAttachment() {
        if (Client.getConnection() == null) {
            chatUI.appendToChat(chatUI.getChatPane(), "System", 
                "Not connected to server", Color.RED);
            return;
        }

        File selectedFile = Client.getFileHandler().selectFileToSend();
        if (selectedFile == null) return;

        try {
            byte[] fileData = Client.getFileHandler().readFileData(selectedFile);

            String currentTab = chatUI.getChatTabs().getTitleAt(chatUI.getChatTabs().getSelectedIndex());
            String messageType = currentTab.equals(ChatUI.GROUP_TAB) ? "group" : "private";
            String recipient = currentTab.equals(ChatUI.GROUP_TAB) ? "ALL" : currentTab;

            Message fileMessage = new Message(Client.getClientName(), recipient, 
                selectedFile.getName(), fileData, messageType);
            Client.getConnection().sendMessage(fileMessage);

            if (currentTab.equals(ChatUI.GROUP_TAB)) {
                chatUI.appendToChat(chatUI.getChatPane(), Client.getClientName(), 
                    "Sent file: " + selectedFile.getName(), chatUI.getUserColor());
            } else {
                JTextPane privatePane = chatUI.getPrivateChats().get(recipient);
                if (privatePane != null) {
                    chatUI.appendToChat(privatePane, Client.getClientName() + " → " + recipient,
                        "Sent file: " + selectedFile.getName(), chatUI.getUserColor());
                }
            }

            JOptionPane.showMessageDialog(chatUI.getFrame(),
                "File sent successfully: " + selectedFile.getName(),
                "File Sent", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error sending file", e);
            JOptionPane.showMessageDialog(chatUI.getFrame(),
                "Error sending file: " + e.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEmojiPicker() {
        EmojiPicker emojiPicker = new EmojiPicker(messageField);
        emojiPicker.show(emojiButton, 0, emojiButton.getHeight());
    }

    public JTextField getMessageField() {
        return messageField;
    }

    public JToggleButton getBoldButton() {
        return boldButton;
    }

    public JToggleButton getItalicButton() {
        return italicButton;
    }
}