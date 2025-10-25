
import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class Player {

    private JLabel label;
    private int x, y;
    private final int MOVE_SPEED = 5;
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

    public Bullet shoot() {
        int centerX = x + label.getWidth() / 2;
        int centerY = y + label.getHeight() / 2;
        return new Bullet(centerX, centerY, 10, 0, true); // ยิงไปขวา
    }

    public JLabel getLabel() {
        return label;
    }

    public Rectangle getBounds() {
        return label.getBounds();
    }

    public boolean isVisible() {
        return label.isVisible();
    }

    public void setVisible(boolean v) {
        label.setVisible(v);
    }
}
