import java.io.Serializable;

public class NetworkPacket implements Serializable {
    public String type;  // "MOVE", "SHOOT"
    public String playerId;
    public int x, y;

    public NetworkPacket(String type, String playerId, int x, int y) {
        this.type = type;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }
}
