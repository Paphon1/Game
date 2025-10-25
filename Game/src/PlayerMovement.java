import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// คลาส Player
class Player {
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
        label.setFont(new Font("Arial", Font.BOLD, 12));
    }

    public void setKeyPressed(int keyCode, boolean pressed) {
        switch (keyCode) {
            case KeyEvent.VK_W: wPressed = pressed; break;
            case KeyEvent.VK_S: sPressed = pressed; break;
            case KeyEvent.VK_A: aPressed = pressed; break;
            case KeyEvent.VK_D: dPressed = pressed; break;
        }
    }

    public void update(int maxWidth, int maxHeight) {
        if (wPressed) y -= MOVE_SPEED;
        if (sPressed) y += MOVE_SPEED;
        if (aPressed) x -= MOVE_SPEED;
        if (dPressed) x += MOVE_SPEED;

        x = Math.max(0, Math.min(x, maxWidth - 80));
        y = Math.max(0, Math.min(y, maxHeight - 80));

        label.setLocation(x, y);
    }

    public Bullet shoot() {
        int centerX = x + 40;
        int centerY = y + 40;
        return new Bullet(centerX, centerY, 7, 0, true);
    }

    public JLabel getLabel() { return label; }
    public Rectangle getBounds() { return label.getBounds(); }
    public boolean isVisible() { return label.isVisible(); }
    public void setVisible(boolean visible) { label.setVisible(visible); }
}

// คลาส Enemy
class Enemy {
    private JLabel label;
    private int hp;
    private int maxHp;
    private int x, y;

    public Enemy(int x, int y, int hp) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;

        label = new JLabel();
        label.setBounds(x, y, 80, 80);
        label.setOpaque(true);
        label.setBackground(Color.RED);
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        label.setFont(new Font("Arial", Font.BOLD, 10));
        updateLabel();
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
        updateLabel();
    }

    public boolean isDead() {
        return hp <= 0;
    }

    private void updateLabel() {
        label.setText("<html><center>Enemy<br>HP:" + hp + "</center></html>");
    }

    public Bullet shootAt(int targetX, int targetY, Random random) {
        int centerX = x + 40;
        int centerY = y + 40;

        double angle = Math.atan2(targetY - centerY, targetX - centerX);
        angle += Math.toRadians(-20 + random.nextInt(41));

        int dx = (int) (Math.cos(angle) * 7);
        int dy = (int) (Math.sin(angle) * 7);

        return new Bullet(centerX, centerY, dx, dy, false);
    }

    public JLabel getLabel() { return label; }
    public Rectangle getBounds() { return label.getBounds(); }
    public boolean isVisible() { return label.isVisible(); }
    public void setVisible(boolean visible) { label.setVisible(visible); }
    public int getX() { return x; }
    public int getY() { return y; }
}

// คลาส Bullet
class Bullet {
    private JLabel label;
    private int dx, dy;
    private boolean isPlayerBullet;

    public Bullet(int x, int y, int dx, int dy, boolean isPlayerBullet) {
        this.dx = dx;
        this.dy = dy;
        this.isPlayerBullet = isPlayerBullet;

        label = new JLabel("●");
        label.setBounds(x - 10, y - 10, 20, 20);
        label.setOpaque(true);
        label.setBackground(isPlayerBullet ? Color.CYAN : Color.YELLOW);
        label.setForeground(Color.BLACK);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
    }

    public void update() {
        label.setLocation(label.getX() + dx, label.getY() + dy);
    }

    public boolean isOutOfBounds(int width, int height) {
        int x = label.getX();
        int y = label.getY();
        return x < 0 || x > width || y < 0 || y > height;
    }

    public JLabel getLabel() { return label; }
    public Rectangle getBounds() { return label.getBounds(); }
    public boolean isPlayerBullet() { return isPlayerBullet; }
}

// Thread สำหรับ spawn enemies
class EnemySpawner extends Thread {
    private PlayerMovement game;
    private Random random;
    private volatile boolean running = true;

    public EnemySpawner(PlayerMovement game) {
        this.game = game;
        this.random = new Random();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(4000); // เปลี่ยนจาก 2000 เป็น 4000 (4 วินาที)
                int enemyCount = 2 + random.nextInt(3);

                for (int i = 0; i < enemyCount; i++) {
                    game.spawnEnemy();
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void stopSpawning() {
        running = false;
        interrupt();
    }
}

// Thread สำหรับ enemy shooting
class EnemyShooter extends Thread {
    private PlayerMovement game;
    private Random random;
    private volatile boolean running = true;

    public EnemyShooter(PlayerMovement game) {
        this.game = game;
        this.random = new Random();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1500);
                game.enemiesShoot(random);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void stopShooting() {
        running = false;
        interrupt();
    }
}

// Thread สำหรับ game loop
class GameLoop extends Thread {
    private PlayerMovement game;
    private volatile boolean running = true;

    public GameLoop(PlayerMovement game) {
        this.game = game;
    }

    @Override
    public void run() {
        while (running) {
            game.updateGame();

            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void stopLoop() {
        running = false;
        interrupt();
    }
}

// คลาสหลัก
public class PlayerMovement extends JFrame {
    private Player player;
    private JLabel scoreLabel;
    private List<Enemy> enemies;
    private List<Bullet> bullets;
    private Random random;
    private int score = 0;

    private GameLoop gameLoop;
    private EnemySpawner enemySpawner;
    private EnemyShooter enemyShooter;

    public PlayerMovement() {
        setTitle("Player Movement Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        random = new Random();
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();

        // สร้าง score label
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(10, 10, 150, 30);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setForeground(Color.BLACK);
        add(scoreLabel);

        // สร้าง player
        player = new Player(50, 250);
        add(player.getLabel());

        // เพิ่ม KeyListener
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                player.setKeyPressed(e.getKeyCode(), true);
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    shootPlayerBullet();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.setKeyPressed(e.getKeyCode(), false);
            }
        });

        setFocusable(true);
        setVisible(true);

        // เริ่ม threads
        startThreads();
    }

    private void startThreads() {
        gameLoop = new GameLoop(this);
        enemySpawner = new EnemySpawner(this);
        enemyShooter = new EnemyShooter(this);

        gameLoop.start();
        enemySpawner.start();
        enemyShooter.start();
    }

    public void spawnEnemy() {
        int halfWidth = getWidth() / 2;
        int x, y;

        // spawn ในครึ่งขวาของหน้าจอเท่านั้น
        x = halfWidth + random.nextInt(getWidth() - halfWidth - 100);
        y = 50 + random.nextInt(getHeight() - 150);

        int hp = 1 + random.nextInt(3);
        Enemy enemy = new Enemy(x, y, hp);

        synchronized (enemies) {
            enemies.add(enemy);
        }

        SwingUtilities.invokeLater(() -> {
            add(enemy.getLabel());
            repaint();
        });
    }

    public void enemiesShoot(Random random) {
        List<Enemy> enemiesCopy;
        synchronized (enemies) {
            enemiesCopy = new ArrayList<>(enemies);
        }

        for (Enemy enemy : enemiesCopy) {
            if (enemy.isVisible() && !enemy.isDead() && random.nextBoolean()) {
                Rectangle playerBounds = player.getBounds();
                int targetX = playerBounds.x + playerBounds.width / 2;
                int targetY = playerBounds.y + playerBounds.height / 2;

                Bullet bullet = enemy.shootAt(targetX, targetY, random);

                synchronized (bullets) {
                    bullets.add(bullet);
                }

                SwingUtilities.invokeLater(() -> add(bullet.getLabel()));
            }
        }
    }

    private void shootPlayerBullet() {
        Bullet bullet = player.shoot();

        synchronized (bullets) {
            bullets.add(bullet);
        }

        SwingUtilities.invokeLater(() -> add(bullet.getLabel()));
    }

    public void updateGame() {
        // อัพเดท player
        SwingUtilities.invokeLater(() -> {
            player.update(getWidth(), getHeight());
        });

        // อัพเดท bullets
        List<Bullet> bulletsToRemove = new ArrayList<>();

        synchronized (bullets) {
            for (Bullet bullet : bullets) {
                SwingUtilities.invokeLater(() -> bullet.update());

                if (bullet.isOutOfBounds(getWidth(), getHeight())) {
                    bulletsToRemove.add(bullet);
                    SwingUtilities.invokeLater(() -> {
                        remove(bullet.getLabel());
                        repaint();
                    });
                }
            }
            bullets.removeAll(bulletsToRemove);
        }

        // ตรวจสอบการชน
        checkCollisions();
    }

    private void checkCollisions() {
        if (!player.isVisible()) return;

        Rectangle playerBounds = player.getBounds();
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Enemy> enemiesToRemove = new ArrayList<>();

        synchronized (bullets) {
            synchronized (enemies) {
                for (Bullet bullet : new ArrayList<>(bullets)) {
                    Rectangle bulletBounds = bullet.getBounds();

                    if (bullet.isPlayerBullet()) {
                        // กระสุนของ player ชน enemy
                        for (Enemy enemy : new ArrayList<>(enemies)) {
                            if (enemy.isVisible() && !enemy.isDead()) {
                                if (bulletBounds.intersects(enemy.getBounds())) {
                                    enemy.takeDamage(1);
                                    bulletsToRemove.add(bullet);

                                    SwingUtilities.invokeLater(() -> {
                                        remove(bullet.getLabel());
                                        repaint();
                                    });

                                    if (enemy.isDead()) {
                                        enemiesToRemove.add(enemy);
                                        score++;
                                        updateScore();

                                        SwingUtilities.invokeLater(() -> {
                                            enemy.setVisible(false);
                                            remove(enemy.getLabel());
                                            repaint();
                                        });
                                    }
                                    break;
                                }
                            }
                        }
                    } else {
                        // กระสุนของ enemy ชน player
                        if (bulletBounds.intersects(playerBounds)) {
                            SwingUtilities.invokeLater(() -> {
                                player.setVisible(false);
                                JOptionPane.showMessageDialog(this,
                                        "Game Over!\nFinal Score: " + score + " enemies defeated!");
                                stopGame();
                            });
                            return;
                        }
                    }
                }

                enemies.removeAll(enemiesToRemove);
                bullets.removeAll(bulletsToRemove);
            }
        }
    }

    private void updateScore() {
        SwingUtilities.invokeLater(() -> scoreLabel.setText("Score: " + score));
    }

    public void stopGame() {
        if (gameLoop != null) gameLoop.stopLoop();
        if (enemySpawner != null) enemySpawner.stopSpawning();
        if (enemyShooter != null) enemyShooter.stopShooting();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PlayerMovement());
    }
}