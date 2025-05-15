package src.client;

import src.models.Message;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.Color;

public class ClientConnection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected = false;

    public void connectToServer(String clientName) {
        try {
            socket = new Socket(Client.SERVER_ADDRESS, Client.SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            out.writeObject(clientName);
            out.flush();
            connected = true;
            
            new Thread(new IncomingReader()).start();
            
        } catch (IOException e) {
            Client.getChatUI().appendToChat(Client.getChatUI().getChatPane(), "System", "Error connecting to server: " + e.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(null, "Failed to connect to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void disconnect() {
        if (connected) {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                connected = false;
            } catch (IOException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public void sendMessage(Message message) throws IOException {
        if (connected) {
            try {
                out.writeObject(message);
                out.flush();
                out.reset();
            } catch (IOException e) {
                connected = false;
                throw e;
            }
        }
    }

    private class IncomingReader implements Runnable {
        public void run() {
            try {
                Object received;
                while ((received = in.readObject()) != null) {
                    processIncomingMessage(received);
                }
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    Client.getChatUI().appendToChat(Client.getChatUI().getChatPane(), "System", "Disconnected from server", Color.RED);
                    connected = false;
                }
            }
        }

        private void processIncomingMessage(Object message) {
            if (message instanceof Message) {
                Message msg = (Message) message;
                SwingUtilities.invokeLater(() -> {
                    Client.getChatUI().processMessage(msg);
                });
            }
        }
    }
}