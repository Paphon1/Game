import java.awt.*;
import javax.swing.*;

class Bullet {
    private JLabel label;
    private int x, y, dx, dy;
    private String owner; // "player" หรือ "enemy"

    public Bullet(int x, int y, int dx, int dy, String owner) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.owner = owner;

        label = new JLabel();
        label.setOpaque(true);
        label.setBackground(owner.equals("enemy") ? Color.ORANGE : Color.CYAN);
        label.setBounds(x, y, 8, 8);
    }

    public void update() {
        x += dx;
        y += dy;
        label.setLocation(x, y);
    }

    public boolean isOutOfBounds(int width, int height) {
        return x < 0 || y < 0 || x > width || y > height;
    }

    public boolean isEnemyBullet() {
        return owner.equals("enemy");
    }

    public JLabel getLabel() { return label; }
    public Rectangle getBounds() { return label.getBounds(); }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
}
