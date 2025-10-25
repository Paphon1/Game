import java.awt.*;
import java.util.Random;
import javax.swing.*;

class Enemy {
    private JPanel enemyPanel;
    private JLabel hpLabel;
    private int x, y;
    private int hp;
    private Random random = new Random();
    private String enemyId;

    public Enemy(int x, int y, int hp, String enemyId) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.enemyId = enemyId;

        enemyPanel = new JPanel(null);
        enemyPanel.setBounds(x, y, 60, 70);

        JLabel body = new JLabel();
        body.setOpaque(true);
        body.setBackground(Color.RED);
        body.setBounds(5, 20, 50, 50);

        hpLabel = new JLabel("HP: " + hp, SwingConstants.CENTER);
        hpLabel.setFont(new Font("Arial", Font.BOLD, 12));
        hpLabel.setForeground(Color.WHITE);
        hpLabel.setBounds(0, 0, 60, 20);

        enemyPanel.add(hpLabel);
        enemyPanel.add(body);
    }

    public Bullet shootRandom() {
        int cx = x + 30;
        int cy = y + 40;
        double angle = Math.toRadians(random.nextInt(360));
        int dx = (int) (Math.cos(angle) * 6);
        int dy = (int) (Math.sin(angle) * 6);
        return new Bullet(cx, cy, dx, dy, "enemy");
    }

    public void takeDamage(int dmg) {
        hp = Math.max(0, hp - dmg);
        hpLabel.setText("HP: " + hp);
    }

    public boolean isDead() { return hp <= 0; }
    public JLabel getLabel() { return null; }
    public JComponent getPanel() { return enemyPanel; }
    public Rectangle getBounds() { return enemyPanel.getBounds(); }
    public String getEnemyId() { return enemyId; }
    public int getHp() { return hp; }
}
