package src.handlers;

import src.MainServer;
import src.models.Message;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ClientHandler extends Thread {
    private final Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientName;
    private final Map<String, ClientHandler> clients;

    public ClientHandler(Socket socket, Map<String, ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    public String getClientName() {
        return clientName;
    }

    public void run() {
        try {
            setupStreams();
            registerClient();

            Object obj;
            while ((obj = in.readObject()) != null) {
                if (obj instanceof Message message) {
                    MessageProcessor.processMessage(message, this, clients);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client error " + clientName + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void setupStreams() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    private void registerClient() throws IOException, ClassNotFoundException {
        Object initMsg = in.readObject();
        if (initMsg instanceof String name) {
            if (clients.containsKey(name)) {
                sendMessage(new Message("SERVER", name, "Name already in use. Reconnect.", "system"));
                socket.close();
                throw new IOException("Duplicate client name");
            }
            clientName = name;
            clients.put(clientName, this);

            MainServer.broadcastMessage(new Message("SERVER", "ALL", clientName + " has joined.", "system"));
            MainServer.broadcastUserList();
        }
    }

    private void cleanup() {
        if (clientName != null) {
            clients.remove(clientName);
            MainServer.broadcastMessage(new Message("SERVER", "ALL", clientName + " has left.", "system"));
            MainServer.broadcastUserList();
        }
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("Send error to " + clientName + ": " + e.getMessage());
        }
    }
}
