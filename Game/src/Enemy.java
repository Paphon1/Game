
import java.awt.*;
import java.util.Random;
import javax.swing.*;

class Enemy {
    private JLabel label;
    private int x, y;
    private int hp, maxHp;
    private Random random = new Random();
    private String enemyId;
    private ImageIcon enemyIcon;

    public Enemy(int x, int y, int hp, String enemyId) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
        this.enemyId = enemyId;

        try {
            // โหลดภาพจาก resource
            ImageIcon rawIcon = new ImageIcon(getClass().getResource("/assets/enemy/military.png"));
            Image scaled = rawIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            enemyIcon = new ImageIcon(scaled);
        } catch (Exception e) {
            System.err.println("❌ ไม่พบรูป enemy.png");
            enemyIcon = new ImageIcon(); // ป้องกัน error
        }

        label = new JLabel(enemyIcon);
        label.setBounds(x, y, 50, 50);
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
    }

    public void setHp(int hp) { this.hp = hp; }
    public boolean isDead() { return hp <= 0; }
    public JLabel getLabel() { return label; }
    public Rectangle getBounds() { return label.getBounds(); }
    public String getEnemyId() { return enemyId; }
    public int getHp() { return hp; }
}
