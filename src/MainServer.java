package src;

import src.handlers.ClientHandler;
import src.models.Message;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainServer {
    public static final int PORT = ServerConfig.PORT;
    public static final String FILE_STORAGE_DIR = ServerConfig.FILE_STORAGE_DIR;
    public static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Enhanced Chat server started on port " + PORT + "...");

        File fileDir = new File(FILE_STORAGE_DIR);
        if (!fileDir.exists()) fileDir.mkdirs();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from: " + clientSocket.getInetAddress().getHostAddress());
                new ClientHandler(clientSocket, clients).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        }
    }

    public static void broadcastMessage(Message message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    public static void sendPrivateMessage(Message message) {
        ClientHandler target = clients.get(message.getRecipient());
        if (target != null) target.sendMessage(message);
    }

    public static void broadcastUserList() {
        StringBuilder userList = new StringBuilder("USERLIST:");
        for (String name : clients.keySet()) {
            userList.append(name).append(",");
        }
        Message msg = new Message("SERVER", "ALL", userList.toString(), "system");
        broadcastMessage(msg);
    }
}
