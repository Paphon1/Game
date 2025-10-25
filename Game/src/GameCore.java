import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;

public class GameCore extends JFrame {

    private Player player;
    private Map<String, Player> otherPlayers = new ConcurrentHashMap<>();
    private Map<String, Enemy> enemies = new ConcurrentHashMap<>();
    private List<Bullet> bullets = new CopyOnWriteArrayList<>();
    private Random random = new Random();
    private JLabel scoreLabel;
    private int score = 0;

    private GameClient client;
    private String playerId;
    private boolean isHost = false;

    private int enemySpawnTimer = 0;
    private int enemyShootTimer = 0;
    private int enemyIdCounter = 0;

    private BufferedImage backgroundImage;

    public GameCore(String playerId, String serverIp, boolean isHost) {
        this.playerId = playerId;
        this.isHost = isHost;

        System.out.println("🎮 Starting GameCore...");
        System.out.println("   Player ID: " + playerId);
        System.out.println("   Is Host: " + isHost);

        setTitle("Bullet Hell Multiplayer - " + playerId + (isHost ? " [HOST]" : ""));
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // โหลดพื้นหลัง
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/assets/background/grass.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to load background image!");
        }

        // custom JPanel สำหรับวาดพื้นหลัง
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                }
            }
        };
        backgroundPanel.setLayout(null);
        setContentPane(backgroundPanel);

        // player
        player = new Player(50 + random.nextInt(100), 250 + random.nextInt(100), playerId);
        add(player.getLabel());

        scoreLabel = new JLabel("Score: 0 | Players: 1");
        scoreLabel.setBounds(10, 10, 300, 30);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(scoreLabel);

        connectToServer(serverIp);

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

    private void connectToServer(String ip) {
        try {
            client = new GameClient(playerId, ip, 5555, this::handleNetworkPacket);
            System.out.println("✅ Connected to server: " + ip);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Cannot connect to Server\n" + e.getMessage());
            System.exit(1);
        }
    }

    private void handleNetworkPacket(NetworkPacket packet) {
        SwingUtilities.invokeLater(() -> {
            try {
                switch (packet.type) {
                    case "MOVE":
                        if (packet.playerId.equals(playerId)) return;

                        otherPlayers.putIfAbsent(packet.playerId,
                                new Player(packet.x, packet.y, packet.playerId));
                        Player p = otherPlayers.get(packet.playerId);
                        p.getLabel().setLocation(packet.x, packet.y);
                        if (p.getLabel().getParent() == null) add(p.getLabel());
                        updatePlayerCount();
                        break;

                    case "SHOOT":
                        if (packet.playerId.equals(playerId)) return;

                        Bullet b = new Bullet(packet.x, packet.y, 10, 0, packet.playerId);
                        bullets.add(b);
                        add(b.getLabel());
                        break;

                    case "ENEMY_SPAWN":
                        if (!enemies.containsKey(packet.enemyId)) {
                            Enemy e = new Enemy(packet.x, packet.y, packet.hp, packet.enemyId);
                            enemies.put(packet.enemyId, e);
                            add(e.getLabel());
                            revalidate();
                            repaint();
                        }
                        break;

                    case "ENEMY_HIT":
                        Enemy hitEnemy = enemies.get(packet.enemyId);
                        if (hitEnemy != null && !hitEnemy.isDead()) {
                            hitEnemy.setHp(packet.hp);
                        }
                        break;

                    case "ENEMY_DEAD":
                        Enemy deadEnemy = enemies.get(packet.enemyId);
                        if (deadEnemy != null) {
                            remove(deadEnemy.getLabel());
                            enemies.remove(packet.enemyId);
                            revalidate();
                            repaint();
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updatePlayerCount() {
        int count = 1 + otherPlayers.size();
        scoreLabel.setText("Score: " + score + " | Players: " + count + " | Enemies: " + enemies.size());
    }

    private void startGameLoop() {
        int FPS = 60;
        Timer timer = new Timer(1000 / FPS, e -> updateGame());
        timer.start();
    }

    private void updateGame() {
        int width = getWidth();
        int height = getHeight();

        player.update(width, height);

        if (client != null) client.sendMove(player.getLabel().getX(), player.getLabel().getY());

        // update bullets
        List<JLabel> toRemove = new ArrayList<>();
        for (Bullet b : bullets) {
            b.update();
            if (b.isOutOfBounds(width, height)) {
                toRemove.add(b.getLabel());
                bullets.remove(b);
            }
        }

        // add/remove enemies/bullets -> repaint 1 ครั้งต่อ frame
        if (!toRemove.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                for (JLabel l : toRemove) remove(l);
                revalidate();
                repaint();
            });
        }

        // host spawn & shoot
        if (isHost) {
            enemySpawnTimer++;
            enemyShootTimer++;

            if (enemySpawnTimer >= 120) { // 2 sec
                spawnEnemies();
                enemySpawnTimer = 0;
            }

            if (enemyShootTimer >= 90) { // 1.5 sec
                enemyShoot();
                enemyShootTimer = 0;
            }
        }

        checkCollisions();
    }

    private void spawnEnemies() {
        int count = 2 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            int halfWidth = getWidth() / 2;
            int x = halfWidth + random.nextInt(getWidth() - halfWidth - 100);
            int y = 50 + random.nextInt(getHeight() - 150);
            int hp = 1 + random.nextInt(3);

            String enemyId = playerId + "_E" + (enemyIdCounter++);
            Enemy e = new Enemy(x, y, hp, enemyId);
            enemies.put(enemyId, e);

            SwingUtilities.invokeLater(() -> {
                add(e.getLabel());
                revalidate();
                repaint();
            });

            if (client != null) client.sendEnemySpawn(enemyId, x, y, hp);
        }
        updatePlayerCount();
    }

    private void enemyShoot() {
        for (Enemy e : enemies.values()) {
            if (!e.isDead() && random.nextBoolean()) {
                Bullet b = e.shootRandom();
                bullets.add(b);
                SwingUtilities.invokeLater(() -> add(b.getLabel()));
            }
        }
    }

    private void shootPlayer() {
        Bullet b = player.shoot();
        bullets.add(b);
        SwingUtilities.invokeLater(() -> add(b.getLabel()));

        if (client != null) client.sendShoot(b.getLabel().getX(), b.getLabel().getY());
    }

    private void checkCollisions() {
        Rectangle playerBounds = player.getBounds();
        List<String> removeEnemyIds = new ArrayList<>();

        synchronized (bullets) {
            List<Bullet> bulletsToRemove = new ArrayList<>();

            for (Bullet b : new ArrayList<>(bullets)) {
                Rectangle bulletBounds = b.getBounds();

                if (!b.isEnemyBullet()) {
                    // กระสุน player ชน enemy
                    boolean hit = false;
                    for (Enemy e : enemies.values()) {
                        if (!e.isDead() && bulletBounds.intersects(e.getBounds())) {
                            e.takeDamage(1);
                            hit = true;

                            System.out.println("🎯 Hit enemy: " + e.getEnemyId() + " HP: " + e.getHp());

                            if (client != null) {
                                client.sendEnemyHit(e.getEnemyId(), e.getHp());
                            }

                            if (e.isDead()) {
                                removeEnemyIds.add(e.getEnemyId());
                                score++;

                                System.out.println("💀 Enemy died: " + e.getEnemyId());

                                SwingUtilities.invokeLater(() -> {
                                    remove(e.getLabel());
                                    revalidate();
                                    repaint();
                                });

                                if (client != null) {
                                    client.sendEnemyDead(e.getEnemyId());
                                }
                            }
                            break; // bullet ชนศัตรูตัวเดียวก็พอ
                        }
                    }
                    if (hit) {
                        bulletsToRemove.add(b);
                        SwingUtilities.invokeLater(() -> remove(b.getLabel()));
                    }
                } else {
                    // กระสุน enemy ชน player
                    if (bulletBounds.intersects(playerBounds)) {
                        gameOver();
                        return;
                    }

                    // กระสุน enemy ชน player อื่นๆ
                    for (Player p : otherPlayers.values()) {
                        if (bulletBounds.intersects(p.getBounds())) {
                            bulletsToRemove.add(b);
                            SwingUtilities.invokeLater(() -> remove(b.getLabel()));
                            break;
                        }
                    }
                }
            }

            bullets.removeAll(bulletsToRemove);
        }

        // ลบ enemy ที่ตายแล้ว
        for (String enemyId : removeEnemyIds) {
            enemies.remove(enemyId);
        }

        updatePlayerCount();
    }

    private void gameOver() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "💀 Game Over!\nFinal Score: " + score);
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        String playerId = (args.length > 0) ? args[0] : "Nam";
        String serverIp = (args.length > 1) ? args[1] : "26.7.76.52";
        boolean isHost = playerId.startsWith("Nam");

        SwingUtilities.invokeLater(() -> new GameCore(playerId, serverIp, isHost));
    }
}
