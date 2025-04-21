package src.handlers;

import src.ServerConfig;
import src.models.Message;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileHandler {

    public static void handleFileMessage(Message message, ClientHandler sender) {
        String uniqueName = System.currentTimeMillis() + "_" + message.getFileName();
        String path = ServerConfig.FILE_STORAGE_DIR + uniqueName;

        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(message.getFileData());

            if ("private".equals(message.getType())) {
                sender.sendMessage(message);
            } else if ("group".equals(message.getType())) {
                sender.sendMessage(message);  // Optionally keep this or remove depending on design
            }

        } catch (IOException e) {
            sender.sendMessage(new Message("SERVER", message.getSender(), "File error: " + e.getMessage(), "system"));
        }
    }
}
