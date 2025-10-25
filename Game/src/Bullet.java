
import java.awt.*;
import javax.swing.*;

public class Bullet {

    private JLabel label;
    private int dx, dy;
    private boolean isPlayerBullet;

    public Bullet(int x, int y, int dx, int dy, boolean isPlayerBullet) {
        this.dx = dx;
        this.dy = dy;
        this.isPlayerBullet = isPlayerBullet;

        label = new JLabel();
        label.setBounds(x, y, 10, 10);
        label.setOpaque(true);
        label.setBackground(isPlayerBullet ? Color.CYAN : Color.YELLOW);
    }

    public void update() {
        label.setLocation(label.getX() + dx, label.getY() + dy);
    }

    public boolean isOutOfBounds(int width, int height) {
        int x = label.getX(), y = label.getY();
        return x < 0 || x > width || y < 0 || y > height;
    }

    public JLabel getLabel() {
        return label;
    }

    public Rectangle getBounds() {
        return label.getBounds();
    }

    public boolean isPlayerBullet() {
        return isPlayerBullet;
    }
}
