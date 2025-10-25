import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static final int PORT = 5555;
    private static final List<ObjectOutputStream> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🎮 Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client joined: " + socket.getInetAddress());

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                clients.add(out);

                new Thread(new ClientHandler(socket, out)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void broadcast(NetworkPacket packet) {
        synchronized (clients) {
            for (ObjectOutputStream out : clients) {
                try {
                    out.writeObject(packet);
                    out.flush();
                } catch (IOException ignored) {}
            }
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        ClientHandler(Socket socket, ObjectOutputStream out) throws IOException {
            this.socket = socket;
            this.out = out;
            this.in = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    NetworkPacket packet = (NetworkPacket) in.readObject();
                    broadcast(packet);
                }
            } catch (Exception e) {
                System.out.println("Client disconnected");
            }
        }
    }
    
}

