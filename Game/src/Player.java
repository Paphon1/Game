
import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class Player {

    private JLabel label;
    private int x, y;
    private final int MOVE_SPEED = 5;
    private int directionX = 1, directionY = 0; // เริ่มหันไปขวา
    private boolean wPressed, sPressed, aPressed, dPressed;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        label = new JLabel("Player");
        label.setBounds(x, y, 80, 80);
        label.setOpaque(true);
        label.setBackground(Color.BLUE);
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    public void setKeyPressed(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.VK_W ->
                wPressed = pressed;
            case KeyEvent.VK_S ->
                sPressed = pressed;
            case KeyEvent.VK_A ->
                aPressed = pressed;
            case KeyEvent.VK_D ->
                dPressed = pressed;
        }
        updateDirection();
    }

    private void updateDirection() {
        int dx = 0, dy = 0;
        if (wPressed) {
            dy -= 1;
        }
        if (sPressed) {
            dy += 1;
        }
        if (aPressed) {
            dx -= 1;
        }
        if (dPressed) {
            dx += 1;
        }
        // ป้องกัน dx=0,dy=0
        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            directionX = (int) Math.round(dx / length);
            directionY = (int) Math.round(dy / length);
        }
    }

    public void update(int maxWidth, int maxHeight) {
        if (wPressed) {
            y -= MOVE_SPEED;
        }
        if (sPressed) {
            y += MOVE_SPEED;
        }
        if (aPressed) {
            x -= MOVE_SPEED;
        }
        if (dPressed) {
            x += MOVE_SPEED;
        }

        x = Math.max(0, Math.min(x, maxWidth - label.getWidth()));
        y = Math.max(0, Math.min(y, maxHeight - label.getHeight()));
        label.setLocation(x, y);
    }

    // ยิงตามทิศที่ Player กำลังหัน
    public Bullet shoot() {
        int centerX = x + label.getWidth() / 2;
        int centerY = y + label.getHeight() / 2;
        int speed = 10;
        int dx = directionX * speed;
        int dy = directionY * speed;

        // ถ้า player ไม่ขยับ → default ขวา
        if (dx == 0 && dy == 0) {
            dx = speed;
        }

        return new Bullet(centerX, centerY, dx, dy, true);
    }

    public JLabel getLabel() {
        return label;
    }

    public Rectangle getBounds() {
        return label.getBounds();
    }
}
