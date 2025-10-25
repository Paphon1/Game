
import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class Enemy {

    private JLabel label;
    private int x, y;
    private int hp;
    private Random random = new Random();

    public Enemy(int x, int y, int hp) {
        this.x = x;
        this.y = y;
        this.hp = hp;

        label = new JLabel("<html><center>Enemy<br>HP:" + hp + "</center></html>");
        label.setBounds(x, y, 80, 80);
        label.setOpaque(true);
        label.setBackground(Color.RED);
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        label.setFont(new Font("Arial", Font.BOLD, 11));
    }

    // ยิงกระสุนแบบสุ่มทิศทาง
    public Bullet shootRandom() {
        int cx = x + label.getWidth() / 2;
        int cy = y + label.getHeight() / 2;

        double angle = Math.toRadians(random.nextInt(360));
        int dx = (int) (Math.cos(angle) * 6);
        int dy = (int) (Math.sin(angle) * 6);

        return new Bullet(cx, cy, dx, dy, false);
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) {
            hp = 0;
        }
        label.setText("<html><center>Enemy<br>HP:" + hp + "</center></html>");
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public JLabel getLabel() {
        return label;
    }

    public Rectangle getBounds() {
        return label.getBounds();
    }
}
