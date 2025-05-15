package src.ui;

import src.client.Client;
import src.client.ClientConnection;  // Added missing import
import src.client.FileHandler;
import src.models.Message;
import src.ui.components.*;
import src.utils.ChatFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatUI {
    private JFrame frame;
    private JTextPane chatPane;
    private DefaultListModel<String> userListModel;
    private JTabbedPane chatTabs;
    private Map<String, JTextPane> privateChats;
    private JTextField messageField;
    private JToggleButton boldButton, italicButton;
    private Color userColor;
    
    public static final String GROUP_TAB = "Group Chat";

    public ChatUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Enhanced Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); 
        frame.setMinimumSize(new Dimension(600, 400));

        userColor = generateUserColor();
        privateChats = new HashMap<>();

        UserListPanel userListPanel = new UserListPanel(this);
        ChatPanel chatPanel = new ChatPanel(this);
        InputPanel inputPanel = new InputPanel(this);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(250);  
        mainSplitPane.setResizeWeight(0.2);
        mainSplitPane.setLeftComponent(userListPanel);
        mainSplitPane.setRightComponent(chatPanel);

        frame.add(mainSplitPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        this.chatPane = chatPanel.getChatPane();
        userListPanel.getUserList();
        this.userListModel = userListPanel.getUserListModel();
        this.chatTabs = chatPanel.getChatTabs();
        this.messageField = inputPanel.getMessageField();
        this.boldButton = inputPanel.getBoldButton();
        this.italicButton = inputPanel.getItalicButton();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (Client.getConnection() != null) {
                    Client.getConnection().disconnect();
                }
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private Color generateUserColor() {
        return new Color(
            100 + (int) (Math.random() * 155),
            100 + (int) (Math.random() * 155),
            100 + (int) (Math.random() * 155)
        );
    }

    public void showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(20);
        panel.add(new JLabel("Enter your username:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(
            frame, 
            panel, 
            "Login",
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                name = "Anonymous-" + System.currentTimeMillis() % 1000;
            }
            handleSuccessfulLogin(name);
        } else {
            System.exit(0);
        }
    }

    private void handleSuccessfulLogin(String name) {
        Client.setClientName(name);
        frame.setTitle("Chat Client - " + name);
        
        ClientConnection connection = new ClientConnection();
        Client.setConnection(connection);
        connection.connectToServer(name);
    }

    public void processMessage(Message msg) {
        if (msg == null) return;

        if (msg.isFileMessage()) {
            handleFileMessage(msg);
            return;
        }

        switch (msg.getType()) {
            case "private":
                handlePrivateMessage(msg);
                break;
            case "group":
                handleGroupMessage(msg);
                break;
            default:
                handleSystemMessage(msg);
        }
    }

    private void handleFileMessage(Message msg) {
        FileHandler fileHandler = Client.getFileHandler();
        try {
            File savedFile = fileHandler.saveReceivedFile(msg);
            JTextPane targetPane = getTargetPane(msg);
            
            appendToChat(targetPane, msg.getSender(), "Sent file: " + msg.getFileName(), Color.BLUE);
            
            if (fileHandler.isImageFile(msg.getFileName())) {
                fileHandler.displayImageThumbnail(targetPane, savedFile);
            }
            
            fileHandler.addPersistentFileLink(targetPane, savedFile);
        } catch (IOException e) {
            appendToChat(chatPane, "System", "Error handling file: " + e.getMessage(), Color.RED);
        }
    }

    private void handlePrivateMessage(Message msg) {
        if (msg.getSender().equals(Client.getClientName())) return;

        if (!privateChats.containsKey(msg.getSender())) {
            openPrivateChat(msg.getSender());
        }

        JTextPane privatePane = privateChats.get(msg.getSender());
        appendToChat(
            privatePane, 
            msg.getSender() + " → " + Client.getClientName(), 
            msg.getContent(), 
            new Color(0, 100, 200)
        );
    }

    private void handleGroupMessage(Message msg) {
        Color msgColor = msg.getSender().equals(Client.getClientName()) 
            ? userColor 
            : new Color(0, 100, 0);
        appendToChat(chatPane, msg.getSender(), msg.getContent(), msgColor);
    }

    private void handleSystemMessage(Message msg) {
        if (msg.getContent().startsWith("USERLIST:")) {
            updateUserList(msg.getContent().substring(9).split(","));
        } else {
            appendToChat(chatPane, "System", msg.getContent(), Color.BLUE);
        }
    }

    public JTextPane getTargetPane(Message msg) {
        if (msg.getType().equals("private")) {
            if (!privateChats.containsKey(msg.getSender())) {
                openPrivateChat(msg.getSender());
            }
            return privateChats.get(msg.getSender());
        }
        return chatPane;
    }

    public void openPrivateChat(String username) {
        if (privateChats.containsKey(username)) {
            chatTabs.setSelectedIndex(chatTabs.indexOfTab(username));
            return;
        }

        PrivateChatManager.openPrivateChat(this, username);
    }

    public void appendToChat(JTextPane textPane, String sender, String message, Color color) {
        ChatFormatter.appendMessage(textPane, sender, message, color, Client.getClientName());
    }

    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.trim().isEmpty()) {
                    userListModel.addElement(user.trim());
                }
            }
        });
    }

    // Getters
    public JFrame getFrame() { return frame; }
    public Map<String, JTextPane> getPrivateChats() { return privateChats; }
    public Color getUserColor() { return userColor; }
    public JTabbedPane getChatTabs() { return chatTabs; }
    public JTextPane getChatPane() { return chatPane; }
    public JTextField getMessageField() { return messageField; }
    public JToggleButton getBoldButton() { return boldButton; }
    public JToggleButton getItalicButton() { return italicButton; }
}