import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.*;

class Player {

    private JLabel label;
    private int x, y;
    private final int MOVE_SPEED = 5;
    private int directionX = 1, directionY = 0;
    private boolean wPressed, sPressed, aPressed, dPressed;
    private String playerId;

    // ภาพตามทิศทาง
    private ImageIcon upIcon, downIcon, leftIcon, rightIcon;

    public Player(int startX, int startY, String playerId) {
        this.x = startX;
        this.y = startY;
        this.playerId = playerId;

        // โหลดและ scale ภาพ
        upIcon = loadAndScale("/assets/tank/tank_up.png", 80, 80);
        downIcon = loadAndScale("/assets/tank/tank_down.png", 80, 80);
        leftIcon = loadAndScale("/assets/tank/tank_left.png", 80, 80);
        rightIcon = loadAndScale("/assets/tank/tank_right.png", 80, 80);

        label = new JLabel(rightIcon); // เริ่มต้นหันขวา
        label.setBounds(x, y, 80, 80);

        // ❌ เอา label สีฟ้าออก
        label.setOpaque(false);  // ทำให้ background โปร่งใส
        label.setBorder(null);   // เอาเส้นขอบออก
    }

    // ฟังก์ชันโหลดและ scale ภาพ
    private ImageIcon loadAndScale(String path, int width, int height) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("❌ Resource not found: " + path);
                return new ImageIcon();
            }
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            System.err.println("❌ Error loading image: " + path + " | " + e.getMessage());
            return new ImageIcon();
        }
    }

    public void setKeyPressed(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.VK_W -> wPressed = pressed;
            case KeyEvent.VK_S -> sPressed = pressed;
            case KeyEvent.VK_A -> aPressed = pressed;
            case KeyEvent.VK_D -> dPressed = pressed;
        }
        updateDirection();
    }

    private void updateDirection() {
        int dx = 0, dy = 0;
        if (wPressed) dy -= 1;
        if (sPressed) dy += 1;
        if (aPressed) dx -= 1;
        if (dPressed) dx += 1;

        if (dx != 0 || dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            directionX = (int) Math.round(dx / length);
            directionY = (int) Math.round(dy / length);

            // เปลี่ยนภาพ player ตามทิศ
            if (Math.abs(dx) > Math.abs(dy)) {
                label.setIcon(dx > 0 ? rightIcon : leftIcon);
            } else {
                label.setIcon(dy > 0 ? downIcon : upIcon);
            }
        }
    }

    public void update(int maxWidth, int maxHeight) {
        if (wPressed) y -= MOVE_SPEED;
        if (sPressed) y += MOVE_SPEED;
        if (aPressed) x -= MOVE_SPEED;
        if (dPressed) x += MOVE_SPEED;

        x = Math.max(0, Math.min(x, maxWidth - label.getWidth()));
        y = Math.max(0, Math.min(y, maxHeight - label.getHeight()));
        label.setLocation(x, y);
    }

    public Bullet shoot() {
        int centerX = x + label.getWidth() / 2;
        int centerY = y + label.getHeight() / 2;
        int speed = 10;
        int dx = directionX * speed;
        int dy = directionY * speed;

        if (dx == 0 && dy == 0) dx = speed;

        return new Bullet(centerX, centerY, dx, dy, playerId);
    }

    public JLabel getLabel() { return label; }
    public Rectangle getBounds() { return label.getBounds(); }
    public String getPlayerId() { return playerId; }
}
