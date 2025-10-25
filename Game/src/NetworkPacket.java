import java.io.Serializable;

class NetworkPacket implements Serializable {
    public String type;
    public String playerId;
    public int x, y;
    public int dx, dy;
    public int hp;
    public String enemyId;

    public NetworkPacket(String type, String playerId, int x, int y) {
        this.type = type;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }

    public NetworkPacket(String type, String enemyId, int x, int y, int hp) {
        this.type = type;
        this.enemyId = enemyId;
        this.x = x;
        this.y = y;
        this.hp = hp;
    }

    // 🔹 สำหรับ ENEMY_SHOOT
    public NetworkPacket(String type, String enemyId, int x, int y, int dx, int dy) {
        this.type = type;
        this.enemyId = enemyId;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }
}
