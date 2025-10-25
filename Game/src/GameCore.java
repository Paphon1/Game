
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

        // ควบคุมปุ่ม
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

        startGameThreads();
    }

    private void startGameThreads() {
        // Thread อัพเดทเกม ~60 FPS
        new Thread(() -> {
            while (true) {
                updateGame();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();

        // Thread spawn enemy ทุก 2 วิ
        new Thread(() -> {
            while (true) {
                spawnEnemies();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();

        // Thread ให้ enemy ยิงกระสุน
        new Thread(() -> {
            while (true) {
                synchronized (enemies) {
                    for (Enemy e : enemies) {
                        if (!e.isDead() && random.nextBoolean()) {
                            Bullet b = e.shootRandom();
                            synchronized (bullets) {
                                bullets.add(b);
                            }
                            SwingUtilities.invokeLater(() -> add(b.getLabel()));
                        }
                    }
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    private void spawnEnemies() {
        int count = 2 + random.nextInt(3); // 2–4 ตัว
        for (int i = 0; i < count; i++) {
            int x = 400 + random.nextInt(350);
            int y = 50 + random.nextInt(450);
            int hp = 1 + random.nextInt(3);
            Enemy e = new Enemy(x, y, hp);
            synchronized (enemies) {
                enemies.add(e);
            }
            SwingUtilities.invokeLater(() -> add(e.getLabel()));
        }
    }

    private void shootPlayer() {
        Bullet b = player.shoot();
        synchronized (bullets) {
            bullets.add(b);
        }
        SwingUtilities.invokeLater(() -> add(b.getLabel()));
    }

    private void updateGame() {
        SwingUtilities.invokeLater(() -> player.update(getWidth(), getHeight()));

        List<Bullet> toRemove = new ArrayList<>();
        synchronized (bullets) {
            for (Bullet b : bullets) {
                SwingUtilities.invokeLater(b::update);
                if (b.isOutOfBounds(getWidth(), getHeight())) {
                    toRemove.add(b);
                    SwingUtilities.invokeLater(() -> remove(b.getLabel()));
                }
            }
            bullets.removeAll(toRemove);
        }

        checkCollisions();
    }

    private void checkCollisions() {
        Rectangle playerBounds = player.getBounds();
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Enemy> enemiesToRemove = new ArrayList<>();

        synchronized (bullets) {
            synchronized (enemies) {
                for (Bullet b : new ArrayList<>(bullets)) {
                    Rectangle bulletBounds = b.getBounds();
                    if (b.isPlayerBullet()) {
                        for (Enemy e : new ArrayList<>(enemies)) {
                            if (!e.isDead() && bulletBounds.intersects(e.getBounds())) {
                                e.takeDamage(1);
                                bulletsToRemove.add(b);
                                if (e.isDead()) {
                                    enemiesToRemove.add(e);
                                    score++;
                                    SwingUtilities.invokeLater(() -> {
                                        remove(e.getLabel());
                                        updateScore();
                                    });
                                }
                                SwingUtilities.invokeLater(() -> remove(b.getLabel()));
                                break;
                            }
                        }
                    } else {
                        if (bulletBounds.intersects(playerBounds)) {
                            gameOver();
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
        scoreLabel.setText("Score: " + score);
    }

    private void gameOver() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Game Over! Final Score: " + score);
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameCore::new);
    }
}
