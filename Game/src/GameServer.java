import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static final int PORT = 5555;
    private static final int FPS = 30;

    private List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        new GameServer().start();
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Client connected: " + socket.getInetAddress());
                    ClientHandler handler = new ClientHandler(socket);
                    synchronized (clients) {
                        clients.add(handler);
                    }
                    new Thread(handler).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Game loop (จำลองข้อมูลเกมกลาง)
        while (true) {
            broadcast("SERVER_TICK " + System.currentTimeMillis());
            try {
                Thread.sleep(1000 / FPS);
            } catch (InterruptedException ignored) {}
        }
    }

    private void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                c.send(message);
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public void run() {
            try {
                String input;
                while ((input = in.readLine()) != null) {
                    System.out.println("Received: " + input);
                    // ส่งต่อให้ client อื่น
                    broadcast("PLAYER_MOVE " + input);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected");
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        public void send(String msg) {
            out.println(msg);
        }
    }
}
