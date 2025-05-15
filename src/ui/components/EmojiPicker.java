
package src.ui.components;

import javax.swing.*;
import java.awt.*;

public class EmojiPicker extends JPopupMenu {
    private static final String[] COMMON_EMOJIS = {
        "😊", "😂", "❤️", "👍", "🎉",
        "😎", "🤔", "😄", "👋", "🌟",
        "💪", "🙌", "✨", "🔥", "💯"
    };

    public EmojiPicker(JTextField targetField) {
        setLayout(new GridLayout(3, 5, 2, 2));
        
        for (String emoji : COMMON_EMOJIS) {
            JButton emojiButton = new JButton(emoji);
            emojiButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            emojiButton.setBorderPainted(false);
            emojiButton.setContentAreaFilled(false);
            emojiButton.addActionListener(e -> {
                targetField.replaceSelection(emoji);
                this.setVisible(false);
            });
            add(emojiButton);
        }
    }
}
