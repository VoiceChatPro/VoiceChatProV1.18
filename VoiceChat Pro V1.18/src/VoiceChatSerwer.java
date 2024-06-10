import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class VoiceChatSerwer {
    private static final int CHAT_PORT = 12346; // Port dla czatu tekstowego
    private static final int VOICE_PORT = 5000; // Port dla czatu głosowego
    private static final int IMAGE_PORT = 6000; // Port dla wysyłania obrazków

    private Set<PrintWriter> clientWriters = new HashSet<>();
    private Set<DataOutputStream> imageOutputStreams = new HashSet<>();

    public static void main(String[] args) {
        new VoiceChatSerwer().startServer();
    }

    private void startServer() {
        try {
            ServerSocket chatServerSocket = new ServerSocket(CHAT_PORT);
            ServerSocket voiceServerSocket = new ServerSocket(VOICE_PORT);
            ServerSocket imageServerSocket = new ServerSocket(IMAGE_PORT);

            System.out.println("Serwer czatu tekstowego uruchomiony na porcie: " + CHAT_PORT);
            System.out.println("Serwer czatu głosowego uruchomiony na porcie: " + VOICE_PORT);
            System.out.println("Serwer obrazków uruchomiony na porcie: " + IMAGE_PORT);

            while (true) {
                Socket chatSocket = chatServerSocket.accept();
                Socket voiceSocket = voiceServerSocket.accept();
                Socket imageSocket = imageServerSocket.accept();
                new ClientHandler(chatSocket, voiceSocket, imageSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private Socket chatSocket;
        private Socket voiceSocket;
        private Socket imageSocket;
        private BufferedReader reader;
        private PrintWriter writer;
        private DataOutputStream imageOutputStream;

        public ClientHandler(Socket chatSocket, Socket voiceSocket, Socket imageSocket) {
            this.chatSocket = chatSocket;
            this.voiceSocket = voiceSocket;
            this.imageSocket = imageSocket;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
                writer = new PrintWriter(chatSocket.getOutputStream(), true);
                imageOutputStream = new DataOutputStream(imageSocket.getOutputStream());

                clientWriters.add(writer);
                imageOutputStreams.add(imageOutputStream);

                // Wątek do odbierania wiadomości
                Thread receivingThread = new Thread(() -> {
                    try {
                        String message;
                        while ((message = reader.readLine()) != null) {
                            if (message.startsWith("IMAGE:")) {
                                broadcastImage(message.substring(6));
                            } else {
                                broadcastMessage(message);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                receivingThread.start();

                // Wątek do odbierania danych głosowych
                // (pozostawiony do celów referencyjnych, możesz dostosować go do obsługi danych głosowych)
                /*Thread voiceReceivingThread = new Thread(() -> {
                    try {
                        // Tu umieść kod odbierania danych głosowych od klienta
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                voiceReceivingThread.start();*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcastMessage(String message) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }

        private void broadcastImage(String imageData) {
            for (DataOutputStream outputStream : imageOutputStreams) {
                try {
                    outputStream.writeUTF(imageData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
