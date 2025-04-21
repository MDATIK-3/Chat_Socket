package src;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import src.models.Message;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static String clientName;
    private static Socket socket;
    private static boolean connected = false;
    private static final String DOWNLOADS_DIR = "downloads/";

    // UI Components
    private static JFrame frame;
    private static JTextPane chatPane;
    private static JTextField messageField;
    private static JList<String> userList;
    private static DefaultListModel<String> userListModel;
    private static JTabbedPane chatTabs;
    private static JButton sendButton, fileButton;
    private static JPanel mainChatPanel;
    private static Map<String, JTextPane> privateChats;
    private static JComboBox<String> statusCombo;
    private static JToggleButton boldButton, italicButton;
    private static Color userColor;

    // Constants
    private static final String GROUP_TAB = "Group Chat";
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();

            // Show login dialog
            showLoginDialog();
        });

        // Create download directory if it doesn't exist
        File downloadDir = new File(DOWNLOADS_DIR);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
    }

    private static void createAndShowGUI() {
        // Initialize components
        frame = new JFrame("Enhanced Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setMinimumSize(new Dimension(600, 400));

        // Generate a random color for this user
        userColor = new Color(
                100 + (int) (Math.random() * 155),
                100 + (int) (Math.random() * 155),
                100 + (int) (Math.random() * 155));

        // Set up private chats map
        privateChats = new HashMap<>();

        // Create main split pane
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(200);
        mainSplitPane.setResizeWeight(0.15);

        // Create user panel (left side)
        JPanel userPanel = createUserPanel();
        mainSplitPane.setLeftComponent(userPanel);

        // Create chat panel (right side)
        JPanel chatPanel = createChatPanel();
        mainSplitPane.setRightComponent(chatPanel);

        frame.add(mainSplitPane);

        // Add window listener for cleanup
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });

        // Center the frame
        frame.setLocationRelativeTo(null);

        // Show the frame
        frame.setVisible(true);
    }

    private static JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusCombo = new JComboBox<>(new String[] { "Online", "Away", "Busy" });
        statusPanel.add(statusCombo, BorderLayout.CENTER);
        panel.add(statusPanel, BorderLayout.NORTH);

        // User list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setCellRenderer(new UserListRenderer());
        userList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Add double-click listener to open private chat
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null && !selectedUser.equals(clientName)) {
                        openPrivateChat(selectedUser);
                    }
                }
            }
        });

        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(BorderFactory.createTitledBorder("Online Users"));
        panel.add(userScrollPane, BorderLayout.CENTER);

        // User actions panel
        JPanel userActionsPanel = new JPanel();
        userActionsPanel.setLayout(new BoxLayout(userActionsPanel, BoxLayout.Y_AXIS));

        JButton privateMessageButton = new JButton("Private Message");
        privateMessageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        privateMessageButton.addActionListener(e -> {
            String selectedUser = userList.getSelectedValue();
            if (selectedUser != null && !selectedUser.equals(clientName)) {
                openPrivateChat(selectedUser);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Please select a user (not yourself) to message",
                        "No User Selected", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        userActionsPanel.add(privateMessageButton);
        userActionsPanel.add(Box.createVerticalStrut(5));

        panel.add(userActionsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private static JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create tabbed pane
        chatTabs = new JTabbedPane();

        // Create main chat panel
        mainChatPanel = new JPanel(new BorderLayout(5, 5));
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(new Color(252, 252, 252));
        JScrollPane chatScrollPane = new JScrollPane(chatPane);
        mainChatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Add main chat tab
        chatTabs.addTab(GROUP_TAB, mainChatPanel);

        panel.add(chatTabs, BorderLayout.CENTER);

        // Create input panel
        JPanel inputPanel = createInputPanel();
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private static JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Create formatting toolbar
        JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        boldButton = new JToggleButton("B");
        boldButton.setFont(new Font("Dialog", Font.BOLD, 12));
        boldButton.setPreferredSize(new Dimension(30, 30));

        italicButton = new JToggleButton("I");
        italicButton.setFont(new Font("Dialog", Font.ITALIC, 12));
        italicButton.setPreferredSize(new Dimension(30, 30));

        fileButton = new JButton("📎");
        fileButton.setPreferredSize(new Dimension(30, 30));
        fileButton.setToolTipText("Send File");
        fileButton.addActionListener(e -> selectAndSendFile());

        formatPanel.add(boldButton);
        formatPanel.add(italicButton);
        formatPanel.add(fileButton);

        panel.add(formatPanel, BorderLayout.NORTH);

        // Create message input field
        messageField = new JTextField();
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        messageField.addActionListener(e -> sendMessage());
        panel.add(messageField, BorderLayout.CENTER);

        // Create send button
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
    }

    private static void selectAndSendFile() {
        if (!connected) {
            appendToChat(chatPane, "System", "Not connected to server", Color.RED);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File to Send");

        // Add file filters
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Documents", "pdf", "doc", "docx", "txt"));
        fileChooser.setAcceptAllFileFilterUsed(true);

        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Check file size (limit to 10MB for example)
            long fileSize = selectedFile.length();
            if (fileSize > 10 * 1024 * 1024) {
                JOptionPane.showMessageDialog(frame,
                        "File is too large. Maximum size is 10MB.",
                        "File Too Large", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Read file bytes
                byte[] fileData = new byte[(int) fileSize];
                try (FileInputStream fis = new FileInputStream(selectedFile)) {
                    fis.read(fileData);
                }

                // Get current tab - determines if it's a private or group message
                String currentTab = chatTabs.getTitleAt(chatTabs.getSelectedIndex());
                String messageType = currentTab.equals(GROUP_TAB) ? "group" : "private";
                String recipient = currentTab.equals(GROUP_TAB) ? "ALL" : currentTab;

                // Create file message
                Message fileMessage = new Message(clientName, recipient,
                        selectedFile.getName(), fileData, messageType);

                // Send to server
                out.writeObject(fileMessage);
                out.flush();

                // Display in chat
                if (currentTab.equals(GROUP_TAB)) {
                    appendToChat(chatPane, clientName, "Sent file: " + selectedFile.getName(), userColor);
                } else {
                    // Display in private chat tab
                    JTextPane privatePane = privateChats.get(recipient);
                    if (privatePane != null) {
                        appendToChat(privatePane, clientName + " → " + recipient,
                                "Sent file: " + selectedFile.getName(), userColor);
                    }
                }

                JOptionPane.showMessageDialog(frame,
                        "File sent successfully: " + selectedFile.getName(),
                        "File Sent", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame,
                        "Error sending file: " + e.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void openPrivateChat(String username) {
        for (int i = 0; i < chatTabs.getTabCount(); i++) {
            if (chatTabs.getTitleAt(i).equals(username)) {
                chatTabs.setSelectedIndex(i);
                return;
            }
        }
        JPanel privateChatPanel = new JPanel(new BorderLayout(5, 5));
        JTextPane privateTextPane = new JTextPane();
        privateTextPane.setEditable(false);
        privateTextPane.setBackground(new Color(252, 252, 252));
        JScrollPane privateScrollPane = new JScrollPane(privateTextPane);
        privateChatPanel.add(privateScrollPane, BorderLayout.CENTER);

        privateChats.put(username, privateTextPane);

        chatTabs.addTab(username, privateChatPanel);
        int index = chatTabs.indexOfTab(username);

        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        tabPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(username + "  ");

        JButton closeButton = new JButton("×");
        closeButton.setPreferredSize(new Dimension(20, 20));
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(e -> {
            int tabIndex = chatTabs.indexOfTab(username);
            if (tabIndex != -1) {
                chatTabs.removeTabAt(tabIndex);
                privateChats.remove(username);
            }
        });
        chatTabs.addChangeListener(e -> {
            int selectedIndex = chatTabs.getSelectedIndex();
            if (selectedIndex >= 0) {
                Component comp = chatTabs.getComponentAt(selectedIndex);
                comp.validate();
                comp.repaint();
            }
        });

        tabPanel.add(titleLabel);
        tabPanel.add(closeButton);

        chatTabs.setTabComponentAt(index, tabPanel);
        chatTabs.setSelectedIndex(index);
    }

    private static void showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(20);

        panel.add(new JLabel("Enter your username:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                name = "Anonymous-" + System.currentTimeMillis() % 1000;
            }
            clientName = name;
            frame.setTitle("Chat Client - " + clientName);

            // Connect to server
            connectToServer();
        } else {
            System.exit(0);
        }
    }

    private static void connectToServer() {
        try {
            // Connect to server
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

            // Set up streams for objects
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // Send client name to server
            out.writeObject(clientName);
            out.flush();
            connected = true;

            // Add ourselves to the user list
            SwingUtilities.invokeLater(() -> {
                userListModel.addElement(clientName);
                appendToChat(chatPane, "System", "Connected to server as " + clientName, Color.BLUE);
            });

            // Start listening for incoming messages
            new Thread(new IncomingReader()).start();

        } catch (IOException e) {
            appendToChat(chatPane, "System", "Error connecting to server: " + e.getMessage(), Color.RED);
            JOptionPane.showMessageDialog(frame, "Failed to connect to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void disconnect() {
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

    private static void sendMessage() {
        if (!connected) {
            appendToChat(chatPane, "System", "Not connected to server", Color.RED);
            return;
        }

        String content = messageField.getText().trim();
        if (content.isEmpty()) {
            return;
        }

        try {
            String formattedContent = content;
            if (boldButton.isSelected()) {
                formattedContent = "*" + formattedContent + "*";
            }
            if (italicButton.isSelected()) {
                formattedContent = "_" + formattedContent + "_";
            }

            // Get current tab - determines if it's a private or group message
            String currentTab = chatTabs.getTitleAt(chatTabs.getSelectedIndex());
            String messageType = currentTab.equals(GROUP_TAB) ? "group" : "private";
            String recipient = currentTab.equals(GROUP_TAB) ? "ALL" : currentTab;

            // Create and send message object
            Message message = new Message(clientName, recipient, formattedContent, messageType);
            out.writeObject(message);
            out.flush();

            // Show message in sender's chat window
            if (!currentTab.equals(GROUP_TAB)) {
                JTextPane privatePane = privateChats.get(currentTab);
                if (privatePane != null) {
                    appendToChat(privatePane, "Me", formattedContent, Color.BLUE);
                }
            } else {
                appendToChat(chatPane, "Me", formattedContent, Color.BLUE);
            }

            // Clear input field
            messageField.setText("");

        } catch (Exception e) {
            appendToChat(chatPane, "System", "Error sending message: " + e.getMessage(), Color.RED);
        }
    }

    private static void handleFileReceived(Message fileMessage) {
        try {
            String fileName = fileMessage.getFileName();
            byte[] fileData = fileMessage.getFileData();
            String sender = fileMessage.getSender();

            // Generate unique filename
            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
            String filePath = DOWNLOADS_DIR + uniqueFileName;

            // Save file
            File savedFile = new File(filePath);
            try (FileOutputStream fos = new FileOutputStream(savedFile)) {
                fos.write(fileData);
            }

            // Determine which chat pane to use
            JTextPane targetPane;
            if (fileMessage.getType().equals("private")) {
                // Check if we have a private chat tab for this user
                if (!privateChats.containsKey(sender)) {
                    openPrivateChat(sender);
                }
                targetPane = privateChats.get(sender);
            } else {
                targetPane = chatPane;
            }

            // Add file notification to chat
            appendToChat(targetPane, sender, "Sent file: " + fileName, Color.BLUE);

            // Check if it's an image and display thumbnail if it is
            if (isImageFile(fileName)) {
                displayImageThumbnail(targetPane, savedFile);
            }

            // Add clickable link to open file
            addPersistentFileLink(targetPane, savedFile);

        } catch (IOException e) {
            System.out.println("Error handling received file: " + e.getMessage());
        }
    }

    private static boolean isImageFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                lower.endsWith(".png") || lower.endsWith(".gif") ||
                lower.endsWith(".bmp");
    }

    // Display image thumbnail in chat
    private static void displayImageThumbnail(JTextPane pane, File file) {
        try {
            // Load the image
            ImageIcon originalIcon = new ImageIcon(file.getPath());
            Image originalImg = originalIcon.getImage();

            // Scale down if needed (max 200x200)
            int maxSize = 200;
            int width = originalImg.getWidth(null);
            int height = originalImg.getHeight(null);

            if (width > maxSize || height > maxSize) {
                double scale = Math.min((double) maxSize / width, (double) maxSize / height);
                width = (int) (width * scale);
                height = (int) (height * scale);
            }

            // Create thumbnail
            Image thumbnailImg = originalImg.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon thumbnailIcon = new ImageIcon(thumbnailImg);

            // Insert image
            pane.setCaretPosition(pane.getDocument().getLength());
            pane.insertIcon(thumbnailIcon);

            // Add newline
            javax.swing.text.Document doc = pane.getDocument();
            doc.insertString(doc.getLength(), "\n", null);
        } catch (Exception e) {
            System.out.println("Error displaying image thumbnail: " + e.getMessage());
        }
    }

    // Create a more persistent file link
    private static void addPersistentFileLink(JTextPane pane, File file) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Create a styled hyperlink
                final String fileName = file.getName();

                // Insert component into text pane
                pane.setCaretPosition(pane.getDocument().getLength());

                // Use styled text instead of a button for better persistence
                javax.swing.text.StyledDocument doc = (javax.swing.text.StyledDocument) pane.getDocument();

                // Create a clickable style
                javax.swing.text.Style linkStyle = pane.addStyle("FileLink", null);
                javax.swing.text.StyleConstants.setForeground(linkStyle, Color.BLUE);
                javax.swing.text.StyleConstants.setUnderline(linkStyle, true);

                // Store file path in attribute
                linkStyle.addAttribute("file_path", file.getAbsolutePath());

                // Add the link text
                doc.insertString(doc.getLength(), "📄 Open " + fileName, linkStyle);
                doc.insertString(doc.getLength(), "\n", null);

                // Add mouse listener to handle clicks
                pane.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        javax.swing.text.StyledDocument doc = (javax.swing.text.StyledDocument) pane.getDocument();
                        javax.swing.text.Element elem = doc.getCharacterElement(pane.viewToModel(e.getPoint()));
                        javax.swing.text.AttributeSet as = elem.getAttributes();

                        Object filePath = as.getAttribute("file_path");
                        if (filePath != null) {
                            try {
                                Desktop.getDesktop().open(new File((String) filePath));
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(frame,
                                        "Error opening file: " + ex.getMessage(),
                                        "File Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void appendToChat(JTextPane textPane, String sender, String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Create timestamp
                String timestamp = TIME_FORMAT.format(new Date());

                // Get document
                javax.swing.text.Document doc = textPane.getDocument();

                // Create styled document
                javax.swing.text.StyledDocument styledDoc = (javax.swing.text.StyledDocument) doc;

                // Create styles
                javax.swing.text.Style timestampStyle = textPane.addStyle("Timestamp", null);
                javax.swing.text.StyleConstants.setForeground(timestampStyle, Color.GRAY);
                javax.swing.text.StyleConstants.setFontSize(timestampStyle, 10);

                javax.swing.text.Style senderStyle = textPane.addStyle("Sender", null);
                javax.swing.text.StyleConstants.setForeground(senderStyle, color);
                javax.swing.text.StyleConstants.setBold(senderStyle, true);

                javax.swing.text.Style messageStyle = textPane.addStyle("Message", null);
                javax.swing.text.StyleConstants.setForeground(messageStyle, Color.BLACK);

                // Handle formatting in message
                String processedMessage = message;
                if (processedMessage.contains("*")
                        && processedMessage.indexOf("*") != processedMessage.lastIndexOf("*")) {
                    processedMessage = processedMessage.replace("*", "");
                    javax.swing.text.StyleConstants.setBold(messageStyle, true);
                }
                if (processedMessage.contains("_")
                        && processedMessage.indexOf("_") != processedMessage.lastIndexOf("_")) {
                    processedMessage = processedMessage.replace("_", "");
                    javax.swing.text.StyleConstants.setItalic(messageStyle, true);
                }

                // Insert timestamp
                styledDoc.insertString(doc.getLength(), "[" + timestamp + "] ", timestampStyle);

                // Insert sender
                styledDoc.insertString(doc.getLength(), sender + ": ", senderStyle);

                // Insert message
                styledDoc.insertString(doc.getLength(), message + "\n", messageStyle);

                // Scroll to bottom
                textPane.setCaretPosition(doc.getLength());

            } catch (javax.swing.text.BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private static class IncomingReader implements Runnable {
        public void run() {
            try {
                Object received;
                while ((received = in.readObject()) != null) {
                    final Object finalReceived = received;
                    SwingUtilities.invokeLater(() -> {
                        processIncomingMessage(finalReceived);
                    });
                }
            } catch (IOException | ClassNotFoundException e) {
                if (connected) {
                    SwingUtilities.invokeLater(() -> {
                        appendToChat(chatPane, "System", "Disconnected from server", Color.RED);
                    });
                    connected = false;
                }
            }
        }

        private void processIncomingMessage(Object message) {
            if (message instanceof Message) {
                Message msg = (Message) message;

                // Handle file messages
                if (msg.isFileMessage()) {
                    handleFileReceived(msg);
                    return;
                }

                // Handle system messages
                if (msg.getSender().equals("SERVER")) {
                    // Check for user list updates
                    if (msg.getContent().startsWith("USERLIST:")) {
                        String[] users = msg.getContent().substring(9).split(",");
                        updateUserList(users);
                    } else {
                        // Regular system message
                        appendToChat(chatPane, "System", msg.getContent(), Color.BLUE);
                    }
                    return;
                }

                // Handle private message
                if (msg.getType().equals("private")) {
                    String sender = msg.getSender();
                    String content = msg.getContent();

                    // Skip if it's a message we sent
                    if (sender.equals(clientName)) {
                        return;
                    }

                    // Open private chat if not open
                    if (!privateChats.containsKey(sender)) {
                        openPrivateChat(sender);
                    }

                    // Add message to private chat
                    JTextPane privatePane = privateChats.get(sender);
                    appendToChat(privatePane, sender + " → " + clientName, content, new Color(0, 100, 200));

                    // Flash tab if not selected
                    int tabIndex = chatTabs.indexOfTab(sender);
                    if (chatTabs.getSelectedIndex() != tabIndex) {
                        // We would add a visual indicator here if needed
                    }

                    return;
                }

                // Regular group message
                if (msg.getType().equals("group")) {
                    String sender = msg.getSender();
                    String content = msg.getContent();

                    Color msgColor = Color.BLACK;
                    if (sender.equals("SERVER")) {
                        msgColor = Color.BLUE;
                    } else if (!sender.equals(clientName)) {
                        msgColor = new Color(0, 100, 0);
                    } else {
                        msgColor = userColor;
                    }

                    appendToChat(chatPane, sender, content, msgColor);
                }
            }
        }

        private void updateUserList(String[] users) {
            userListModel.clear();
            for (String user : users) {
                if (!user.trim().isEmpty()) {
                    userListModel.addElement(user.trim());
                }
            }
        }
    }

    private static class UserListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            String username = (String) value;

            // Set icon based on status (would normally come from server)
            if (username.equals(clientName)) {
                label.setForeground(userColor);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            }

            return label;
        }
    }
}