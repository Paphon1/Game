
import java.awt.*;
import java.util.Random;
import javax.swing.*;

class Enemy {

    private JLabel label;
    private int x, y;
    private int hp, maxHp;
    private Random random = new Random();
    private String enemyId;
    private ImageIcon enemyIcon; // ✅ เพิ่มรูปศัตรู

    public Enemy(int x, int y, int hp, String enemyId) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
        this.enemyId = enemyId;

        // ✅ โหลดรูปภาพ
        enemyIcon = new ImageIcon("../../assets/enemy/military.png");

        // ✅ สร้าง JLabel ที่แสดงรูปภาพ
        label = new JLabel(enemyIcon);
        label.setBounds(x, y, enemyIcon.getIconWidth(), enemyIcon.getIconHeight());
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
        if (hp < 0) {
            hp = 0;
        }
        // ❌ ไม่ต้องอัปเดตข้อความ เพราะไม่มี text แล้ว
    }

    public void setHp(int hp) {
        this.hp = hp;
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

    public String getEnemyId() {
        return enemyId;
    }

    public int getHp() {
        return hp;
    }
}
