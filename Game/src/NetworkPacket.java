import java.io.Serializable;

class NetworkPacket implements Serializable {
    public String type;  // "MOVE", "SHOOT", "ENEMY_SPAWN", "ENEMY_HIT", "ENEMY_DEAD"
    public String playerId;
    public int x, y;
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
}