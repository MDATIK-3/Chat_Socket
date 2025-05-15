package src.handlers;

import src.MainServer;
import src.models.Message;

import java.util.Map;

public class MessageProcessor {

    public static void processMessage(Message message, ClientHandler sender, Map<String, ClientHandler> clients) {
        switch (message.getType()) {
            case "private" -> {
                MainServer.sendPrivateMessage(message);
                sender.sendMessage(message);
            }
            case "group" -> MainServer.broadcastMessage(message);
        }
    }
}
