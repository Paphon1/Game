
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class GameCore extends JFrame {

    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private Random random = new Random();
    private JLabel scoreLabel;
    private int score = 0;

    // Counters สำหรับ spawn / shoot
    private int enemySpawnTimer = 0;
    private int enemyShootTimer = 0;

    public GameCore() {
        setTitle("Bullet Hell Game");
        setSize(800, 600);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        player = new Player(50, 250);
        add(player.getLabel());

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(10, 10, 200, 30);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(scoreLabel);

        // Keyboard control
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                player.setKeyPressed(e.getKeyCode(), true);
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    shootPlayer();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.setKeyPressed(e.getKeyCode(), false);
            }
        });

        setFocusable(true);
        setVisible(true);

        startGameLoop();
    }

    private void startGameLoop() {
        final int FPS = 60;
        final int FRAME_TIME = 1000 / FPS; // ms per frame

        new Thread(() -> {
            while (true) {
                long start = System.currentTimeMillis();

                updateGame();

                long timeTaken = System.currentTimeMillis() - start;
                long sleepTime = FRAME_TIME - timeTaken;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }).start();
    }

    private void updateGame() {
        int width = getWidth();
        int height = getHeight();

        // Update player
        player.update(width, height);

        // Update bullets
        List<Bullet> bulletsToRemove = new ArrayList<>();
        for (Bullet b : bullets) {
            b.update();
            if (b.isOutOfBounds(width, height)) {
                bulletsToRemove.add(b);
            }
        }

        // Update enemies timers
        enemySpawnTimer++;
        enemyShootTimer++;

        // Spawn enemy ทุก 2 วิ
        if (enemySpawnTimer >= 120) { // 60 FPS * 2 sec
            spawnEnemies();
            enemySpawnTimer = 0;
        }

        // Enemy shoot ทุก 1.5 วิ
        if (enemyShootTimer >= 90) { // 60 FPS * 1.5 sec
            enemyShoot();
            enemyShootTimer = 0;
        }

        // Collision
        checkCollisions();

        // Remove bullets
        for (Bullet b : bulletsToRemove) {
            remove(b.getLabel());
            bullets.remove(b);
        }

        // Repaint once per frame
        repaint();
    }

    private void spawnEnemies() {
        int count = 2 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            int x = 400 + random.nextInt(350);
            int y = 50 + random.nextInt(450);
            int hp = 1 + random.nextInt(3);
            Enemy e = new Enemy(x, y, hp);
            enemies.add(e);
            add(e.getLabel());
        }
    }

    private void enemyShoot() {
        for (Enemy e : enemies) {
            if (!e.isDead() && random.nextBoolean()) {
                Bullet b = e.shootRandom();
                bullets.add(b);
                add(b.getLabel());
            }
        }
    }

    private void shootPlayer() {
        Bullet b = player.shoot();
        bullets.add(b);
        add(b.getLabel());
    }

    private void checkCollisions() {
        Rectangle playerBounds = player.getBounds();
        List<Enemy> enemiesToRemove = new ArrayList<>();
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet b : new ArrayList<>(bullets)) {
            Rectangle bulletBounds = b.getBounds();
            if (b.isPlayerBullet()) {
                for (Enemy e : enemies) {
                    if (!e.isDead() && bulletBounds.intersects(e.getBounds())) {
                        e.takeDamage(1);
                        bulletsToRemove.add(b);
                        if (e.isDead()) {
                            enemiesToRemove.add(e);
                            score++;
                            remove(e.getLabel());
                        }
                        remove(b.getLabel());
                        break;
                    }
                }
            } else {
                if (bulletBounds.intersects(playerBounds)) {
                    gameOver();
                }
            }
        }

        enemies.removeAll(enemiesToRemove);
        bullets.removeAll(bulletsToRemove);
        scoreLabel.setText("Score: " + score);
    }

    private void gameOver() {
        JOptionPane.showMessageDialog(this, "Game Over! Final Score: " + score);
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameCore::new);
    }
}
