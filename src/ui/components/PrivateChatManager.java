package src.ui.components;

import src.ui.ChatUI;
import javax.swing.*;
import java.awt.*;

public class PrivateChatManager {
    public static void openPrivateChat(ChatUI chatUI, String username) {
        JPanel privateChatPanel = new JPanel(new BorderLayout(5, 5));
        JTextPane privateTextPane = new JTextPane();
        privateTextPane.setEditable(false);
        privateTextPane.setBackground(new Color(252, 252, 252));
        JScrollPane privateScrollPane = new JScrollPane(privateTextPane);
        privateChatPanel.add(privateScrollPane, BorderLayout.CENTER);

        chatUI.getPrivateChats().put(username, privateTextPane);
        chatUI.getChatTabs().addTab(username, privateChatPanel);
        int index = chatUI.getChatTabs().indexOfTab(username);

        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        tabPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(username + "  ");
        JButton closeButton = new JButton("×");
        closeButton.setPreferredSize(new Dimension(20, 20));
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(e -> {
            int tabIndex = chatUI.getChatTabs().indexOfTab(username);
            if (tabIndex != -1) {
                chatUI.getChatTabs().removeTabAt(tabIndex);
                chatUI.getPrivateChats().remove(username);
            }
        });

        tabPanel.add(titleLabel);
        tabPanel.add(closeButton);

        chatUI.getChatTabs().setTabComponentAt(index, tabPanel);
        chatUI.getChatTabs().setSelectedIndex(index);
    }
}