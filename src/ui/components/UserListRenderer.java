package src.ui.components;

import src.client.Client;
import javax.swing.*;
import java.awt.*;

public class UserListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        String username = (String) value;
        if (username.equals(Client.getClientName())) {
            label.setForeground(Client.getChatUI().getUserColor());
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        }

        return label;
    }
}