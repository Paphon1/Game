import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;

/**
 * Bullet Hell Game - Multiplayer Version
 * ใช้งานร่วมกับ GameServer.java และ GameClient.java
 */
public class GameCore extends JFrame {

    private Player player;
    private Map<String, Player> otherPlayers = new ConcurrentHashMap<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private Random random = new Random();
    private JLabel scoreLabel;
    private int score = 0;

    // Network
    private GameClient client;
    private String playerId;

    // Spawn timers
    private int enemySpawnTimer = 0;
    private int enemyShootTimer = 0;

    public GameCore(String playerId, String serverIp) {
        this.playerId = playerId;

        setTitle("Bullet Hell Multiplayer - " + playerId);
        setSize(800, 600);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        player = new Player(50 + random.nextInt(100), 250 + random.nextInt(100));
        add(player.getLabel());

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(10, 10, 200, 30);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(scoreLabel);

        // เชื่อมต่อเซิร์ฟเวอร์
        connectToServer(serverIp);

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

    /** เชื่อมต่อกับ Server */
    private void connectToServer(String ip) {
        try {
            client = new GameClient(playerId, ip, 5555, this::handleNetworkPacket);
            System.out.println("Connected to server: " + ip);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Can not connect Server \n" + e.getMessage());
            System.exit(1);
        }
    }

    /** เมื่อได้รับ packet จาก server */
    private void handleNetworkPacket(NetworkPacket packet) {
        SwingUtilities.invokeLater(() -> {
            if (packet.playerId.equals(playerId)) return; // ไม่อัปเดตตัวเอง

            switch (packet.type) {
                case "MOVE" -> {
                    otherPlayers.putIfAbsent(packet.playerId, new Player(packet.x, packet.y));
                    Player p = otherPlayers.get(packet.playerId);
                    p.getLabel().setLocation(packet.x, packet.y);
                    add(p.getLabel());
                }
                case "SHOOT" -> {
                    Bullet b = new Bullet(packet.x, packet.y, 8, 0, false);
                    bullets.add(b);
                    add(b.getLabel());
                }
            }
        });
    }

    /** Loop หลักของเกม (60 FPS) */
    private void startGameLoop() {
        final int FPS = 60;
        final int FRAME_TIME = 1000 / FPS;

        new Thread(() -> {
            while (true) {
                long start = System.currentTimeMillis();
                updateGame();
                long timeTaken = System.currentTimeMillis() - start;
                long sleep = FRAME_TIME - timeTaken;
                if (sleep > 0) {
                    try { Thread.sleep(sleep); } catch (InterruptedException ignored) {}
                }
            }
        }).start();
    }

    /** อัปเดตสถานะเกมทุกเฟรม */
    private void updateGame() {
        int width = getWidth();
        int height = getHeight();

        // อัปเดตตำแหน่ง Player
        player.update(width, height);

        // ส่งตำแหน่ง player ไป server
        if (client != null) {
            client.sendMove(player.getLabel().getX(), player.getLabel().getY());
        }

        // อัปเดต bullets
        List<Bullet> removeBullets = new ArrayList<>();
        for (Bullet b : bullets) {
            b.update();
            if (b.isOutOfBounds(width, height)) {
                remove(b.getLabel());
                removeBullets.add(b);
            }
        }
        bullets.removeAll(removeBullets);

        // Spawn enemies ทุก 2 วินาที
        enemySpawnTimer++;
        enemyShootTimer++;

        if (enemySpawnTimer >= 120) {
            spawnEnemies();
            enemySpawnTimer = 0;
        }

        // Enemy ยิงทุก 1.5 วินาที
        if (enemyShootTimer >= 90) {
            enemyShoot();
            enemyShootTimer = 0;
        }

        checkCollisions();
        repaint();
    }

    /** สร้างศัตรู */
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

    /** ศัตรูยิง */
    private void enemyShoot() {
        for (Enemy e : enemies) {
            if (!e.isDead() && random.nextBoolean()) {
                Bullet b = e.shootRandom();
                bullets.add(b);
                add(b.getLabel());
            }
        }
    }

    /** ยิงจากผู้เล่น */
    private void shootPlayer() {
        Bullet b = player.shoot();
        bullets.add(b);
        add(b.getLabel());

        if (client != null) {
            client.sendShoot(b.getLabel().getX(), b.getLabel().getY());
        }
    }

    /** ตรวจจับการชน */
    private void checkCollisions() {
        Rectangle playerBounds = player.getBounds();
        List<Enemy> removeEnemies = new ArrayList<>();
        List<Bullet> removeBullets = new ArrayList<>();

        for (Bullet b : new ArrayList<>(bullets)) {
            Rectangle bulletBounds = b.getBounds();
            if (b.isPlayerBullet()) {
                for (Enemy e : enemies) {
                    if (!e.isDead() && bulletBounds.intersects(e.getBounds())) {
                        e.takeDamage(1);
                        remove(b.getLabel());
                        removeBullets.add(b);
                        if (e.isDead()) {
                            removeEnemies.add(e);
                            score++;
                            remove(e.getLabel());
                        }
                        break;
                    }
                }
            } else {
                if (bulletBounds.intersects(playerBounds)) {
                    gameOver();
                }
            }
        }

        enemies.removeAll(removeEnemies);
        bullets.removeAll(removeBullets);
        scoreLabel.setText("Score: " + score);
    }

    /** เกมจบ */
    private void gameOver() {
        JOptionPane.showMessageDialog(this, "💀 Game Over!\nFinal Score: " + score);
        System.exit(0);
    }

    /** เริ่มเกม */
    public static void main(String[] args) {
        // ถ้าไม่ใส่ argument → เล่นเดี่ยว
        String playerId = (args.length > 0) ? args[0] : "Player" + new Random().nextInt(999);
        String serverIp = (args.length > 1) ? args[1] : "127.0.0.1";

        SwingUtilities.invokeLater(() -> new GameCore(playerId, serverIp));
    }
}
