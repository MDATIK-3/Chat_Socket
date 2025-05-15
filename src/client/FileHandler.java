package src.client;

import src.models.Message;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Arrays;

public class FileHandler {
    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    public File selectFileToSend() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File to Send");
        
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Documents", "pdf", "doc", "docx", "txt"));
        fileChooser.setAcceptAllFileFilterUsed(true);

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.length() > 10 * 1024 * 1024) {
                JOptionPane.showMessageDialog(null,
                        "File is too large. Maximum size is 10MB.",
                        "File Too Large", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return selectedFile;
        }
        return null;
    }

    public byte[] readFileData(File file) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] fileData = new byte[(int) file.length()];
            int bytesRead = 0;
            int offset = 0;
            while (offset < fileData.length && (bytesRead = bis.read(fileData, offset, fileData.length - offset)) != -1) {
                offset += bytesRead;
            }
            return fileData;
        }
    }

    public File saveReceivedFile(Message fileMessage) throws IOException {
        String fileName = fileMessage.getFileName();
        byte[] fileData = fileMessage.getFileData();
        
        File downloadDir = new File(Client.DOWNLOADS_DIR);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        
        String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
        String filePath = Client.DOWNLOADS_DIR + uniqueFileName;
        
        File savedFile = new File(filePath);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(savedFile))) {
            bos.write(fileData);
            bos.flush();
        }
        return savedFile;
    }

    public boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return Arrays.stream(IMAGE_EXTENSIONS).anyMatch(lower::endsWith);
    }

    public void displayImageThumbnail(JTextPane pane, File file) {
        try {
            ImageIcon originalIcon = new ImageIcon(file.getPath());
            Image originalImg = originalIcon.getImage();

            int maxSize = 200;
            int width = originalImg.getWidth(null);
            int height = originalImg.getHeight(null);

            if (width > maxSize || height > maxSize) {
                double scale = Math.min((double) maxSize / width, (double) maxSize / height);
                width = (int) (width * scale);
                height = (int) (height * scale);
            }

            Image thumbnailImg = originalImg.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon thumbnailIcon = new ImageIcon(thumbnailImg);

            pane.setCaretPosition(pane.getDocument().getLength());
            pane.insertIcon(thumbnailIcon);

            StyledDocument doc = pane.getStyledDocument();
            doc.insertString(doc.getLength(), "\n", null);
        } catch (Exception e) {
            System.out.println("Error displaying image thumbnail: " + e.getMessage());
        }
    }

    public void addPersistentFileLink(JTextPane pane, File file) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = pane.getStyledDocument();
                Style style = pane.addStyle("FileLink", null);
                StyleConstants.setForeground(style, Color.BLUE);
                StyleConstants.setUnderline(style, true);
                style.addAttribute("file_path", file.getAbsolutePath());

                doc.insertString(doc.getLength(), "📄 Open " + file.getName(), style);
                doc.insertString(doc.getLength(), "\n", null);

                pane.addMouseListener(new MouseAdapter() {
                    @SuppressWarnings("deprecation")
                    public void mouseClicked(MouseEvent e) {
                        int pos = pane.viewToModel(e.getPoint());
                        Element elem = doc.getCharacterElement(pos);
                        AttributeSet as = elem.getAttributes();

                        Object filePath = as.getAttribute("file_path");
                        if (filePath != null) {
                            try {
                                Desktop.getDesktop().open(new File((String) filePath));
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null,
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


   
}