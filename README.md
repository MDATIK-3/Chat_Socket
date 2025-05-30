
# Enhanced Chat Application

A Java-based client-server chat application with support for private messaging, file sharing, and rich text formatting.

## Features

- Group chat support
- Private messaging between users
- File sharing (images and documents)
- Rich text formatting (bold, italic)
- Real-time user list updates
- Automatic image thumbnails
- Interactive file links
- System notifications

## Technical Details

- Server Port: 5000
- Max File Size: 10MB
- Supported Image Types: jpg, jpeg, png, gif, bmp
- Downloads Directory: `downloads/`
- Server File Storage: `server_files/`

## Project Structure

```
src/
├── client/         # Client-side implementation
├── handlers/       # Server message and client handlers
├── models/         # Data models (Message class)
├── ui/            # User interface components
│   └── components/ # Reusable UI elements
└── utils/         # Utility classes and configuration
```

## Running the Application

1. Start the server:
   ```bash
   javac src/MainServer.java
   java src.MainServer
   ```

2. Launch the client:
   ```bash
   javac src/Main.java
   java src.Main
   ```

## Usage

1. Enter your username when prompted
2. Use the main chat area for group messages
3. Double-click a username to start a private chat
4. Use the attachment button (📎) to share files
5. Format text using the Bold (B) and Italic (I) buttons

## Error Handling

- Automatic reconnection attempts on connection loss
- File size validation
- Duplicate username prevention
- Graceful connection termination
