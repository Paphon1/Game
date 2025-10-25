import java.io.Serializable;

public class NetworkPacket implements Serializable {
    public String type;
    public String playerId;
    public int x, y, dx, dy, hp;
    public String enemyId;

    // Constructor หลัก
    public NetworkPacket(String type, String playerId, int x, int y) {
        this.type = type;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }

    // สำหรับ Enemy ยิง
    public NetworkPacket(String type, String playerId, int x, int y, int dx, int dy, String enemyId) {
        this.type = type;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.enemyId = enemyId;
    }

    // สำหรับ Enemy spawn / hit / dead
    public NetworkPacket(String type, String playerId, String enemyId, int x, int y, int hp) {
        this.type = type;
        this.playerId = playerId;
        this.enemyId = enemyId;
        this.x = x;
        this.y = y;
        this.hp = hp;
    }
}
