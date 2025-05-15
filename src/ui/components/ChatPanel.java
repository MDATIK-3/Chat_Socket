package src.ui.components;

import src.ui.ChatUI;
import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {
    private JTextPane chatPane;
    private JTabbedPane chatTabs;

    public ChatPanel(ChatUI chatUI) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        chatTabs = new JTabbedPane();
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(new Color(252, 252, 252));
        
        JScrollPane chatScrollPane = new JScrollPane(chatPane);
        JPanel mainChatPanel = new JPanel(new BorderLayout());
        mainChatPanel.add(chatScrollPane, BorderLayout.CENTER);
        
        chatTabs.addTab(ChatUI.GROUP_TAB, mainChatPanel);
        add(chatTabs, BorderLayout.CENTER);
    }

    public JTextPane getChatPane() {
        return chatPane;
    }

    public JTabbedPane getChatTabs() {
        return chatTabs;
    }
}