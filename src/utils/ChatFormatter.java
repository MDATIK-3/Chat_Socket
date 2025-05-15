package src.utils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatFormatter {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    public static void appendMessage(JTextPane textPane, String sender, 
                                   String message, Color color, String clientName) {
        SwingUtilities.invokeLater(() -> {
            try {
                String timestamp = TIME_FORMAT.format(new Date());
                StyledDocument doc = textPane.getStyledDocument();

                // Create styles
                Style timestampStyle = textPane.addStyle("Timestamp", null);
                StyleConstants.setForeground(timestampStyle, Color.GRAY);
                StyleConstants.setFontSize(timestampStyle, 10);

                Style senderStyle = textPane.addStyle("Sender", null);
                StyleConstants.setForeground(senderStyle, color);
                StyleConstants.setBold(senderStyle, true);

                Style messageStyle = textPane.addStyle("Message", null);
                StyleConstants.setForeground(messageStyle, Color.BLACK);

                // Handle formatting in message
                String processedMessage = message;
                if (processedMessage.contains("*")
                        && processedMessage.indexOf("*") != processedMessage.lastIndexOf("*")) {
                    processedMessage = processedMessage.replace("*", "");
                    StyleConstants.setBold(messageStyle, true);
                }
                if (processedMessage.contains("_")
                        && processedMessage.indexOf("_") != processedMessage.lastIndexOf("_")) {
                    processedMessage = processedMessage.replace("_", "");
                    StyleConstants.setItalic(messageStyle, true);
                }

                doc.insertString(doc.getLength(), "[" + timestamp + "] ", timestampStyle);

                doc.insertString(doc.getLength(), sender + ": ", senderStyle);

                doc.insertString(doc.getLength(), processedMessage + "\n", messageStyle);

                textPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
}