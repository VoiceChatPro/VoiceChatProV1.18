import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;

public class VoiceChatAplikacja {
    private static final int CHAT_PORT = 12346; // Port dla czatu tekstowego
    private static final int VOICE_PORT = 5000; // Port dla czatu głosowego

    private String username;
    private Socket chatSocket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private JTextPane chatArea;
    private JTextField inputField;
    private JComboBox<String> channelComboBox;
    private boolean isVoiceChatRunning = false;
    private Thread voiceChatThread;

    public VoiceChatAplikacja() {
        JFrame frame = new JFrame("VoiceChat Pro " + username);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Menu bar setup
        JMenuBar menuBar = new JMenuBar();

        // Menu 1
        JMenu menu1 = new JMenu("Kanały");
        JMenuItem addChannelItem = new JMenuItem("Dodaj kanał");
        JMenuItem removeChannelItem = new JMenuItem("Usuń kanał");
        JMenuItem renameChannelItem = new JMenuItem("Zmień nazwę kanału");
        JMenuItem closeAppItem = new JMenuItem("Zamknij program");

        addChannelItem.addActionListener(e -> addChannel());
        removeChannelItem.addActionListener(e -> removeChannel());
        renameChannelItem.addActionListener(e -> renameChannel());
        closeAppItem.addActionListener(e -> closeApp());

        menu1.add(addChannelItem);
        menu1.add(removeChannelItem);
        menu1.add(renameChannelItem);
        menu1.add(closeAppItem);

        // Menu 2
        JMenu menu2 = new JMenu("Opcje");
        JMenuItem option4 = new JMenuItem("Opcja 1");
        JMenuItem option5 = new JMenuItem("Opcja 2");
        JMenuItem option6 = new JMenuItem("Opcja 3");
        option4.setEnabled(false);
        option5.setEnabled(false);
        option6.setEnabled(false);
        menu2.add(option4);
        menu2.add(option5);
        menu2.add(option6);

        // Menu Appearance
        JMenu appearanceMenu = new JMenu("Wygląd");
        JMenuItem lightModeItem = new JMenuItem("Tryb jasny");
        JMenuItem darkModeItem = new JMenuItem("Tryb ciemny");
        JMenuItem javaModeItem = new JMenuItem("Tryb Javy");

        lightModeItem.addActionListener(e -> setLightMode(frame));
        darkModeItem.addActionListener(e -> setDarkMode(frame));
        javaModeItem.addActionListener(e -> setJavaMode(frame));

        appearanceMenu.add(lightModeItem);
        appearanceMenu.add(darkModeItem);
        appearanceMenu.add(javaModeItem);

        // Menu Pomoc
        JMenu helpMenu = new JMenu("Pomoc");
        JMenuItem helpItem = new JMenuItem("Pomoc");
        JMenuItem infoItem = new JMenuItem("Informacje");

        helpItem.addActionListener(e -> {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Program służy do komunikacji głosowej i tekstowej.", JLabel.LEFT), BorderLayout.CENTER);
            JOptionPane.showMessageDialog(frame, panel, "Pomoc", JOptionPane.INFORMATION_MESSAGE);
        });

        infoItem.addActionListener(e -> {
            ImageIcon infoIcon = new ImageIcon("info.png"); // Ścieżka do pliku z obrazkiem informacyjnym
            JLabel infoLabel = new JLabel("", infoIcon, JLabel.CENTER);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(infoLabel, BorderLayout.NORTH);
            panel.add(new JLabel("© Prawa autorskie: VOICECHAT PRO", JLabel.CENTER), BorderLayout.CENTER);
            panel.add(new JLabel("VOICECHAT PRO V1.18 2024-06-10", JLabel.CENTER), BorderLayout.SOUTH);
            JOptionPane.showMessageDialog(frame, panel, "Informacje", JOptionPane.INFORMATION_MESSAGE);
        });

        helpMenu.add(helpItem);
        helpMenu.add(infoItem);

        // Add menus to the menu bar
        menuBar.add(menu1);
        menuBar.add(menu2);
        menuBar.add(appearanceMenu);
        menuBar.add(helpMenu);

        frame.setJMenuBar(menuBar);

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        inputField = new JTextField();
        inputPanel.add(inputField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        inputPanel.add(sendButton, BorderLayout.EAST);
        sendButton.addActionListener(e -> sendMessage());

        inputField.addActionListener(e -> sendMessage()); // Wysyłanie wiadomości po naciśnięciu Entera

        JButton sendImageButton = new JButton("Send Image");
        inputPanel.add(sendImageButton, BorderLayout.WEST);
        sendImageButton.addActionListener(e -> sendImage());

        String[] emojis = {
                "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "🥹", "☺️", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘",
                "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🥸", "🤩", "🥳", "🙂‍↕️", "😏", "😒", "🙂‍↔️", "😞",
                "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😮‍💨", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶",
                "😱", "😨", "😰", "😥", "😓", "🫣", "🤗", "🫡", "🤔", "🫢", "🤭", "🤫", "🤥", "😶", "😶‍🌫️", "😐", "😑", "😬", "🫨", "🫠", "🙄",
                "😯", "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "😵‍💫", "🫥", "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🤑",
                "🤠", "😈", "👿", "👹", "👺", "🤡", "💩", "👻", "💀", "☠️", "👽", "👾", "🤖", "🎃", "😺", "😸", "😹", "😻", "😼", "😽", "🙀",
                "😿", "😾", "🇦🇫", "🇦🇽", "🇦🇱", "🇩🇿", "🇦🇸", "🇦🇩", "🇦🇴", "🇦🇮", "🇦🇶", "🇦🇬", "🇦🇷", "🇦🇲", "🇦🇼", "🇦🇺", "🇦🇹", "🇦🇿",
                "🇧🇸", "🇧🇭", "🇧🇩", "🇧🇧", "🇧🇾", "🇧🇪", "🇧🇿", "🇧🇯", "🇧🇲", "🇧🇹", "🇧🇴", "🇧🇦", "🇧🇼", "🇧🇷", "🇮🇴", "🇻🇬", "🇧🇳", "🇧🇬",
                "🇧🇫", "🇧🇮", "🇨🇻", "🇰🇭", "🇨🇲", "🇨🇦", "🇮🇨", "🇰🇾", "🇨🇫", "🇹🇩", "🇨🇱", "🇨🇳", "🇨🇽", "🇨🇨", "🇨🇴", "🇰🇲", "🇨🇩", "🇨🇬",
                "🇨🇰", "🇨🇷", "🇭🇷", "🇨🇺", "🇨🇼", "🇨🇾", "🇨🇿", "🇩🇰", "🇩🇯", "🇩🇲", "🇩🇴", "🇪🇨", "🇪🇬", "🇸🇻", "🇬🇶", "🇪🇷", "🇪🇪", "🇸🇿",
                "🇪🇹", "🇪🇺", "🇫🇰", "🇫🇴", "🇫🇯", "🇫🇮", "🇫🇷", "🇬🇫", "🇵🇫", "🇹🇫", "🇬🇦", "🇬🇲", "🇬🇪", "🇩🇪", "🇬🇭", "🇬🇮", "🇬🇷", "🇬🇱",
                "🇬🇩", "🇬🇵", "🇬🇺", "🇬🇹", "🇬🇬", "🇬🇳", "🇬🇼", "🇬🇾", "🇭🇹", "🇭🇳", "🇭🇰", "🇭🇺", "🇮🇸", "🇮🇳", "🇮🇩", "🇮🇷", "🇮🇶", "🇮🇪",
                "🇮🇲", "🇮🇱", "🇮🇹", "🇯🇲", "🇯🇵", "🇯🇪", "🇯🇴", "🇰🇿", "🇰🇪", "🇰🇮", "🇽🇰", "🇰🇼", "🇰🇬", "🇱🇦", "🇱🇻", "🇱🇧", "🇱🇸", "🇱🇷",
                "🇱🇾", "🇱🇮", "🇱🇹", "🇱🇺", "🇲🇴", "🇲🇬", "🇲🇼", "🇲🇾", "🇲🇻", "🇲🇱", "🇲🇹", "🇲🇭", "🇲🇶", "🇲🇷", "🇲🇺", "🇾🇹", "🇲🇽", "🇫🇲",
                "🇲🇩", "🇲🇨", "🇲🇳", "🇲🇪", "🇲🇸", "🇲🇦", "🇲🇿", "🇲🇲", "🇳🇦", "🇳🇷", "🇳🇵", "🇳🇱", "🇳🇨", "🇳🇿", "🇳🇮", "🇳🇪", "🇳🇬", "🇳🇺",
                "🇳🇫", "🇰🇵", "🇲🇰", "🇲🇵", "🇳🇴", "🇴🇲", "🇵🇰", "🇵🇼", "🇵🇸", "🇵🇦", "🇵🇬", "🇵🇾", "🇵🇪", "🇵🇭", "🇵🇳", "🇵🇱", "🇵🇹", "🇵🇷",
                "🇶🇦", "🇷🇪", "🇷🇴", "🇷🇺", "🇷🇼", "🇼🇸", "🇸🇲", "🇸🇦", "🇸🇳", "🇷🇸", "🇸🇨", "🇸🇱", "🇸🇬", "🇸🇽", "🇸🇰", "🇸🇮", "🇸🇧", "🇸🇴",
                "🇿🇦", "🇰🇷", "🇸🇸", "🇪🇸", "🇱🇰", "🇧🇱", "🇸🇭", "🇰🇳", "🇱🇨", "🇵🇲", "🇻🇨", "🇸🇩", "🇸🇷", "🇸🇯", "🇸🇪", "🇨🇭", "🇸🇾", "🇹🇼",
                "🇹🇯", "🇹🇿", "🇹🇭", "🇹🇱", "🇹🇬", "🇹🇰", "🇹🇴", "🇹🇹", "🇹🇳", "🇹🇷", "🇹🇲", "🇹🇨", "🇹🇻", "🇺🇬", "🇺🇦", "🇦🇪", "🇬🇧", "🇺🇸",
                "🇻🇮", "🇺🇾", "🇺🇿", "🇻🇺", "🇻🇦", "🇻🇪", "🇻🇳", "🇼🇫", "🇪🇭", "🇾🇪", "🇿🇲", "🇿🇼"
        };

        JComboBox<String> emojiComboBox = new JComboBox<>(emojis);
        inputPanel.add(emojiComboBox, BorderLayout.EAST);

        emojiComboBox.addActionListener(e -> {
            String selectedEmoji = (String) emojiComboBox.getSelectedItem();
            if (selectedEmoji != null) {
                inputField.setText(inputField.getText() + selectedEmoji);
            }
        });

        frame.add(inputPanel, BorderLayout.SOUTH);

        JPanel channelPanel = new JPanel();
        channelPanel.setLayout(new BorderLayout());

        channelComboBox = new JComboBox<>();
        channelPanel.add(new JLabel("Channels"), BorderLayout.NORTH);
        channelPanel.add(channelComboBox, BorderLayout.CENTER);

        frame.add(channelPanel, BorderLayout.WEST);

        frame.setVisible(true);

        startVoiceChatClient();
        connectToServer();
    }

    private void connectToServer() {
        username = JOptionPane.showInputDialog("Enter your username:");
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Username cannot be empty!");
            System.exit(1);
        }

        try {
            chatSocket = new Socket("localhost", CHAT_PORT);
            reader = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(chatSocket.getOutputStream()));

            // Send username to the server
            writer.write(username);
            writer.newLine();
            writer.flush();

            // Start a new thread to listen for incoming messages
            Thread listenerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        if (message.startsWith("IMAGE:")) {
                            byte[] imageBytes = Base64.getDecoder().decode(message.substring(6));
                            ImageIcon imageIcon = new ImageIcon(imageBytes);
                            chatArea.insertIcon(imageIcon);
                        } else {
                            chatArea.setText(chatArea.getText() + message + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            listenerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            try {
                writer.write(message);
                writer.newLine();
                writer.flush();
                chatArea.setText(chatArea.getText() + "You: " + message + "\n");
                inputField.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                writer.write("IMAGE:" + base64Image);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startVoiceChatClient() {
        isVoiceChatRunning = true;
        voiceChatThread = new Thread(() -> {
            try {
                Socket voiceSocket = new Socket("localhost", VOICE_PORT);
                AudioFormat format = new AudioFormat(44100.0f, 16, 2, true, true);
                DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
                TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
                targetLine.open(format);
                targetLine.start();

                DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
                SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
                sourceLine.open(format);
                sourceLine.start();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                while (isVoiceChatRunning) {
                    int bytesRead = targetLine.read(buffer, 0, buffer.length);
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    if (voiceSocket.isConnected()) {
                        OutputStream out = voiceSocket.getOutputStream();
                        out.write(buffer, 0, bytesRead);
                        out.flush();
                    }
                }
                targetLine.stop();
                targetLine.close();
                sourceLine.stop();
                sourceLine.close();
                voiceSocket.close();
            } catch (LineUnavailableException | IOException e) {
                e.printStackTrace();
            }
        });
        voiceChatThread.start();
    }

    private void stopVoiceChatClient() {
        isVoiceChatRunning = false;
        try {
            if (voiceChatThread != null) {
                voiceChatThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void changeChannel() {
        try {
            String selectedChannel = (String) channelComboBox.getSelectedItem();
            if (selectedChannel != null) {
                writer.write("Changed channel: " + selectedChannel);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addChannel() {
        String newChannel = JOptionPane.showInputDialog("Enter new channel name:");
        if (newChannel != null && !newChannel.trim().isEmpty()) {
            try {
                writer.write("New channel added: " + newChannel);
                writer.newLine();
                writer.flush();
                channelComboBox.addItem(newChannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeChannel() {
        String selectedChannel = (String) channelComboBox.getSelectedItem();
        if (selectedChannel != null) {
            try {
                writer.write("Channel removed: " + selectedChannel);
                writer.newLine();
                writer.flush();
                channelComboBox.removeItem(selectedChannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void renameChannel() {
        String selectedChannel = (String) channelComboBox.getSelectedItem();
        if (selectedChannel != null) {
            String newChannelName = JOptionPane.showInputDialog("Enter new channel name:");
            if (newChannelName != null && !newChannelName.trim().isEmpty()) {
                try {
                    writer.write("Channel renamed from " + selectedChannel + " to " + newChannelName);
                    writer.newLine();
                    writer.flush();
                    channelComboBox.removeItem(selectedChannel);
                    channelComboBox.addItem(newChannelName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void closeApp() {
        int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Confirm Exit",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void startVoiceChat() {
        if (!isVoiceChatRunning) {
            isVoiceChatRunning = true;
            voiceChatThread = new Thread(this::runVoiceChat);
            voiceChatThread.start();

            JFrame voiceChatFrame = new JFrame("Voice Chat");
            voiceChatFrame.setSize(300, 150);
            voiceChatFrame.setLayout(new BorderLayout());

            JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
            volumeSlider.setMajorTickSpacing(10);
            volumeSlider.setPaintTicks(true);
            volumeSlider.setPaintLabels(true);
            voiceChatFrame.add(volumeSlider, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());

            JButton muteButton = new JButton("Mute");
            buttonPanel.add(muteButton);
            muteButton.addActionListener(e -> muteVoiceChat());

            JButton stopVoiceChatButton = new JButton("Stop VoiceChat");
            buttonPanel.add(stopVoiceChatButton);
            stopVoiceChatButton.addActionListener(e -> {
                stopVoiceChat();
                voiceChatFrame.dispose();
            });

            voiceChatFrame.add(buttonPanel, BorderLayout.CENTER);

            voiceChatFrame.setVisible(true);
        }
    }

    private void runVoiceChat() {
        try {
            Socket voiceSocket = new Socket("localhost", VOICE_PORT);
            DataInputStream dataInputStream = new DataInputStream(voiceSocket.getInputStream());
            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(getAudioFormat());
            sourceDataLine.open(getAudioFormat());
            sourceDataLine.start();

            byte[] buffer = new byte[10000];
            int bytesRead;
            while (isVoiceChatRunning && (bytesRead = dataInputStream.read(buffer)) != -1) {
                sourceDataLine.write(buffer, 0, bytesRead);
            }

            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void muteVoiceChat() {
        // Implementacja funkcji wyciszenia czatu głosowego
    }

    private void stopVoiceChat() {
        isVoiceChatRunning = false;
        if (voiceChatThread != null) {
            voiceChatThread.interrupt();
            voiceChatThread = null;
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    // Metoda ustawiająca tryb jasny
    private static void setLightMode(JFrame frame) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Metoda ustawiająca tryb ciemny
    private static void setDarkMode(JFrame frame) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Metoda ustawiająca tryb Javy
    private static void setJavaMode(JFrame frame) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void addAppToSystemTray() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "VoiceChat Pro "+ username);
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("VoiceChat Pro "+ username);

            PopupMenu popup = new PopupMenu();
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> System.exit(0));
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VoiceChatAplikacja::new);
    }
}
