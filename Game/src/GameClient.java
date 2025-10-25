import java.io.*;
import java.net.*;
import java.util.function.Consumer;

class GameClient {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String playerId;
    private Consumer<NetworkPacket> onPacketReceived;

    public GameClient(String playerId, String serverIp, int port, Consumer<NetworkPacket> handler) throws IOException {
        this.playerId = playerId;
        this.onPacketReceived = handler;

        Socket socket = new Socket(serverIp, port);
        System.out.println("✅ Connected to server.");

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        new Thread(this::listen).start();
    }

    private void listen() {
        try {
            while (true) {
                NetworkPacket packet = (NetworkPacket) in.readObject();
                onPacketReceived.accept(packet);
            }
        } catch (Exception e) {
            System.out.println("Disconnected from server.");
        }
    }

    public void sendMove(int x, int y) {
        try {
            out.writeObject(new NetworkPacket("MOVE", playerId, x, y));
            out.flush();
        } catch (IOException ignored) {}
    }

    public void sendShoot(int x, int y) {
        try {
            out.writeObject(new NetworkPacket("SHOOT", playerId, x, y));
            out.flush();
        } catch (IOException ignored) {}
    }

    public void sendEnemySpawn(String enemyId, int x, int y, int hp) {
        try {
            NetworkPacket packet = new NetworkPacket("ENEMY_SPAWN", enemyId, x, y, hp);
            out.writeObject(packet);
            out.flush();
        } catch (IOException ignored) {}
    }

    public void sendEnemyHit(String enemyId, int hp) {
        try {
            NetworkPacket packet = new NetworkPacket("ENEMY_HIT", enemyId, 0, 0, hp);
            out.writeObject(packet);
            out.flush();
        } catch (IOException ignored) {}
    }

    public void sendEnemyDead(String enemyId) {
        try {
            NetworkPacket packet = new NetworkPacket("ENEMY_DEAD", enemyId, 0, 0, 0);
            out.writeObject(packet);
            out.flush();
        } catch (IOException ignored) {}
    }
}