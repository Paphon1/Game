import java.awt.*;
import javax.swing.*;

class Bullet {
    private JLabel label;
    private int dx, dy;
    private String ownerId;

    public Bullet(int x, int y, int dx, int dy, String ownerId) {
        this.dx = dx;
        this.dy = dy;
        this.ownerId = ownerId;

        label = new JLabel();
        label.setBounds(x, y, 10, 10);
        label.setOpaque(true);
        label.setBackground(ownerId.equals("enemy") ? Color.YELLOW : Color.CYAN);
    }

    public void update() {
        label.setLocation(label.getX() + dx, label.getY() + dy);
    }

    public boolean isOutOfBounds(int width, int height) {
        int x = label.getX(), y = label.getY();
        return x < 0 || x > width || y < 0 || y > height;
    }

    public JLabel getLabel() { return label; }
    public Rectangle getBounds() { return label.getBounds(); }
    public String getOwnerId() { return ownerId; }
    public boolean isEnemyBullet() { return ownerId.equals("enemy"); }

    // 🔹 สำหรับส่งค่าความเร็วของกระสุนไป client
    public int getDx() { return dx; }
    public int getDy() { return dy; }
}
