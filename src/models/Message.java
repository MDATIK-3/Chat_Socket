package src.models;
import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String sender;
    private String recipient;
    private String content;
    private String type; // "private", "group", "file"
    private byte[] fileData;
    private String fileName;
    
    // Standard message constructor
    public Message(String sender, String recipient, String content, String type) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.type = type;
        this.fileData = null;
        this.fileName = null;
    }
    
    // File message constructor
    public Message(String sender, String recipient, String fileName, byte[] fileData, String type) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = "File: " + fileName;
        this.fileName = fileName;
        this.fileData = fileData;
        this.type = type;
    }
    
    public String getSender() {
        return sender;
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getType() {
        return type;
    }
    
    public byte[] getFileData() {
        return fileData;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public boolean isFileMessage() {
        return fileData != null && fileName != null;
    }
    
    @Override
    public String toString() {
        if (isFileMessage()) {
            return "[" + type + "] " + sender + " -> " + recipient + ": File: " + fileName;
        }
        return "[" + type + "] " + sender + " -> " + recipient + ": " + content;
    }
}