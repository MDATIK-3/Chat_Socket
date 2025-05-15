package src.client;

import javax.swing.*;
import src.ui.ChatUI;
import src.utils.Config;


public class Client {
    public static final String SERVER_ADDRESS = Config.SERVER_ADDRESS; ;
    public static final int SERVER_PORT = Config.SERVER_PORT;
    public static final String DOWNLOADS_DIR = Config.DOWNLOADS_DIR;

    private static String clientName;
    private static ChatUI chatUI;
    private static ClientConnection connection;
    private static FileHandler fileHandler;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            chatUI = new ChatUI();
            fileHandler = new FileHandler();

            // Show login dialog
            chatUI.showLoginDialog();
        });
    }

    // Getters and setters
    public static String getClientName() {
        return clientName;
    }

    public static void setClientName(String name) {
        clientName = name;
    }

    public static ChatUI getChatUI() {
        return chatUI;
    }

    public static ClientConnection getConnection() {
        return connection;
    }

    public static void setConnection(ClientConnection conn) {
        connection = conn;
    }

    public static FileHandler getFileHandler() {
        return fileHandler;
    }
}
