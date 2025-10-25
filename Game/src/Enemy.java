import java.awt.*;
import java.util.Random;
import javax.swing.*;

class Enemy {
    private JLabel label;
    private int x, y;
    private int hp, maxHp;
    private Random random = new Random();
    private String enemyId;

    public Enemy(int x, int y, int hp, String enemyId) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
        this.enemyId = enemyId;

        label = new JLabel();
        label.setBounds(x, y, 50, 50);
        label.setOpaque(true);
        label.setBackground(Color.RED);
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        label.setFont(new Font("Arial", Font.BOLD, 11));
        updateLabel();
    }

    private void updateLabel() {
        label.setText("<html><center>Enemy<br>HP:" + hp + "/" + maxHp + "</center></html>");
    }

    public Bullet shootRandom() {
        int cx = x + label.getWidth() / 2;
        int cy = y + label.getHeight() / 2;

        double angle = Math.toRadians(random.nextInt(360));
        int dx = (int) (Math.cos(angle) * 6);
        int dy = (int) (Math.sin(angle) * 6);

        return new Bullet(cx, cy, dx, dy, "enemy");
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0;
        updateLabel();
    }

    public void setHp(int hp) {
        this.hp = hp;
        updateLabel();
    }

    public boolean isDead() { return hp <= 0; }
    public JLabel getLabel() { return label; }
    public Rectangle getBounds() { return label.getBounds(); }
    public String getEnemyId() { return enemyId; }
    public int getHp() { return hp; }
}