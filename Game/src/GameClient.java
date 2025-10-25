import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<NetworkPacket> packetHandler;
    private boolean running = true;

    public GameClient(String playerId, String ip, int port, Consumer<NetworkPacket> handler) throws IOException {
        socket = new Socket(ip, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        this.packetHandler = handler;

        new Thread(() -> {
            try {
                while (running) {
                    NetworkPacket packet = (NetworkPacket) in.readObject();
                    handler.accept(packet);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Connection closed: " + e.getMessage());
            }
        }).start();
    }

    private synchronized void sendPacket(NetworkPacket packet) {
        try {
            out.writeObject(packet);
            out.flush();
        } catch (IOException e) {
            System.err.println("❌ Failed to send packet: " + e.getMessage());
        }
    }

    // ========= ส่งแพ็กเกจต่าง ๆ =========
    public void sendMove(int x, int y) {
        sendPacket(new NetworkPacket("MOVE", "player", x, y));
    }

    public void sendShoot(int x, int y) {
        sendPacket(new NetworkPacket("SHOOT", "player", x, y));
    }

    public void sendEnemySpawn(String enemyId, int x, int y, int hp) {
        sendPacket(new NetworkPacket("ENEMY_SPAWN", "host", enemyId, x, y, hp));
    }

    public void sendEnemyHit(String enemyId, int hp) {
        sendPacket(new NetworkPacket("ENEMY_HIT", "host", enemyId, 0, 0, hp));
    }

    public void sendEnemyDead(String enemyId) {
        sendPacket(new NetworkPacket("ENEMY_DEAD", "host", enemyId, 0, 0, 0));
    }

    // ✅ ส่งข้อมูลการยิงกระสุนของศัตรู
    public void sendEnemyShoot(String enemyId, int x, int y, int dx, int dy) {
        sendPacket(new NetworkPacket("ENEMY_SHOOT", "host", x, y, dx, dy, enemyId));
    }

    public void close() {
        running = false;
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}
