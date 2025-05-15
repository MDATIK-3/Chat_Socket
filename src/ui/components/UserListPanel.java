package src.ui.components;

import src.client.Client;
import src.ui.ChatUI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UserListPanel extends JPanel {
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JComboBox<String> statusCombo;

    public UserListPanel(ChatUI chatUI) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusCombo = new JComboBox<>(new String[] { "Online", "Away", "Busy" });
        statusPanel.add(statusCombo, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.NORTH);

        // User list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setCellRenderer(new UserListRenderer());
        userList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Double-click to open private chat
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null && !selectedUser.equals(Client.getClientName())) {
                        chatUI.openPrivateChat(selectedUser);
                    }
                }
            }
        });

        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder("Online Users"));
        add(userScrollPane, BorderLayout.CENTER);

        // User actions panel
        JPanel userActionsPanel = new JPanel();
        userActionsPanel.setLayout(new BoxLayout(userActionsPanel, BoxLayout.Y_AXIS));

        JButton privateMessageButton = new JButton("Private Message");
        privateMessageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        privateMessageButton.addActionListener(e -> {
            String selectedUser = userList.getSelectedValue();
            if (selectedUser != null && !selectedUser.equals(Client.getClientName())) {
                chatUI.openPrivateChat(selectedUser);
            } else {
                JOptionPane.showMessageDialog(chatUI.getFrame(),
                        "Please select a user (not yourself) to message",
                        "No User Selected", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        userActionsPanel.add(privateMessageButton);
        userActionsPanel.add(Box.createVerticalStrut(5));

        add(userActionsPanel, BorderLayout.SOUTH);
    }

    public JList<String> getUserList() {
        return userList;
    }

    public DefaultListModel<String> getUserListModel() {
        return userListModel;
    }
}