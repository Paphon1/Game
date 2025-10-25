import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;

public class GameCore extends JFrame {
    private Player player;
    private Map<String, Player> otherPlayers = new ConcurrentHashMap<>();
    private Map<String, Enemy> enemies = new ConcurrentHashMap<>();
    private List<Bullet> bullets = Collections.synchronizedList(new ArrayList<>());
    private Random random = new Random();
    private JLabel scoreLabel;
    private int score = 0;

    private GameClient client;
    private String playerId;
    private boolean isHost = false;

    private int enemySpawnTimer = 0;
    private int enemyShootTimer = 0;
    private int enemyIdCounter = 0;

    public GameCore(String playerId, String serverIp, boolean isHost) {
        this.playerId = playerId;
        this.isHost = isHost;

        setTitle("Bullet Hell Multiplayer - " + playerId + (isHost ? " [HOST]" : ""));
        setSize(800, 600);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        player = new Player(100, 250, playerId);
        add(player.getLabel());

        scoreLabel = new JLabel("Score: 0 | Players: 1 | Enemies: 0");
        scoreLabel.setBounds(10, 10, 300, 30);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
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
                    case "MOVE" -> {
                        if (packet.playerId.equals(playerId)) return;
                        otherPlayers.putIfAbsent(packet.playerId,
                                new Player(packet.x, packet.y, packet.playerId));
                        Player p = otherPlayers.get(packet.playerId);
                        p.getLabel().setLocation(packet.x, packet.y);
                        if (p.getLabel().getParent() == null) add(p.getLabel());
                        updatePlayerCount();
                    }

                    case "SHOOT" -> {
                        if (packet.playerId.equals(playerId)) return;
                        Bullet b = new Bullet(packet.x, packet.y, 10, 0, packet.playerId);
                        bullets.add(b);
                        add(b.getLabel());
                    }

                    case "ENEMY_SPAWN" -> {
                        if (!enemies.containsKey(packet.enemyId)) {
                            Enemy e = new Enemy(packet.x, packet.y, packet.hp, packet.enemyId);
                            enemies.put(packet.enemyId, e);
                            add(e.getPanel());
                        }
                        updatePlayerCount();
                    }

                    case "ENEMY_HIT" -> {
                        Enemy hitEnemy = enemies.get(packet.enemyId);
                        if (hitEnemy != null) hitEnemy.takeDamage(1);
                    }

                    case "ENEMY_DEAD" -> {
                        Enemy deadEnemy = enemies.remove(packet.enemyId);
                        if (deadEnemy != null) remove(deadEnemy.getPanel());
                        updatePlayerCount();
                    }

                    case "ENEMY_SHOOT" -> {
                        Bullet enemyBullet = new Bullet(packet.x, packet.y, packet.dx, packet.dy, "enemy");
                        bullets.add(enemyBullet);
                        add(enemyBullet.getLabel());
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error handling packet: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void updatePlayerCount() {
        int count = 1 + otherPlayers.size();
        scoreLabel.setText("Score: " + score + " | Players: " + count + " | Enemies: " + enemies.size());
    }

    private void startGameLoop() {
        final int FPS = 60;
        final int FRAME_TIME = 1000 / FPS;

        new Thread(() -> {
            while (true) {
                long start = System.currentTimeMillis();
                updateGame();
                long elapsed = System.currentTimeMillis() - start;
                long sleep = FRAME_TIME - elapsed;
                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException ignored) {}
                }
            }
        }).start();
    }

    private void updateGame() {
        int width = getWidth();
        int height = getHeight();

        player.update(width, height);

        if (client != null) {
            client.sendMove(player.getLabel().getX(), player.getLabel().getY());
        }

        // อัปเดตกระสุนทั้งหมด
        List<Bullet> removeList = new ArrayList<>();
        synchronized (bullets) {
            for (Bullet b : bullets) {
                b.update();
                if (b.isOutOfBounds(width, height)) {
                    removeList.add(b);
                    SwingUtilities.invokeLater(() -> remove(b.getLabel()));
                }
            }
            bullets.removeAll(removeList);
        }

        // Host ควบคุม enemy
        if (isHost) {
            enemySpawnTimer++;
            enemyShootTimer++;

            if (enemySpawnTimer >= 120) {
                spawnEnemies();
                enemySpawnTimer = 0;
            }

            if (enemyShootTimer >= 60) {
                enemyShoot();
                enemyShootTimer = 0;
            }
        }

        checkCollisions();
        SwingUtilities.invokeLater(this::repaint);
    }

    private void spawnEnemies() {
        int count = 2 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            int x = 400 + random.nextInt(300);
            int y = 50 + random.nextInt(400);
            int hp = 2;
            String enemyId = "E" + (enemyIdCounter++);
            Enemy e = new Enemy(x, y, hp, enemyId);
            enemies.put(enemyId, e);
            SwingUtilities.invokeLater(() -> add(e.getPanel()));

            if (client != null) {
                client.sendEnemySpawn(enemyId, x, y, hp);
            }
        }
        updatePlayerCount();
    }

    private void enemyShoot() {
        for (Enemy e : enemies.values()) {
            if (!e.isDead() && random.nextBoolean()) {
                Bullet b = e.shootRandom();
                bullets.add(b);
                SwingUtilities.invokeLater(() -> add(b.getLabel()));

                if (client != null) {
                    client.sendEnemyShoot(e.getEnemyId(),
                            b.getLabel().getX(), b.getLabel().getY(),
                            b.getDx(), b.getDy());
                }
            }
        }
    }

    private void shootPlayer() {
        Bullet b = player.shoot();
        bullets.add(b);
        SwingUtilities.invokeLater(() -> add(b.getLabel()));

        if (client != null) {
            client.sendShoot(b.getLabel().getX(), b.getLabel().getY());
        }
    }

    private void checkCollisions() {
        Rectangle playerBounds = player.getBounds();
        List<Bullet> removeBullets = new ArrayList<>();
        List<String> deadEnemies = new ArrayList<>();

        synchronized (bullets) {
            for (Bullet b : new ArrayList<>(bullets)) {
                Rectangle bulletBounds = b.getBounds();

                if (!b.isEnemyBullet()) {
                    // ยิงศัตรู
                    for (Enemy e : enemies.values()) {
                        if (!e.isDead() && bulletBounds.intersects(e.getBounds())) {
                            e.takeDamage(1);
                            removeBullets.add(b);
                            remove(b.getLabel());

                            if (client != null) {
                                client.sendEnemyHit(e.getEnemyId(), e.getHp());
                            }

                            if (e.isDead()) {
                                deadEnemies.add(e.getEnemyId());
                                remove(e.getPanel());
                                score++;
                                if (client != null) client.sendEnemyDead(e.getEnemyId());
                            }
                            break;
                        }
                    }
                } else {
                    // กระสุนศัตรูชน player
                    if (bulletBounds.intersects(playerBounds)) {
                        gameOver();
                        return;
                    }
                }
            }
        }

        for (String id : deadEnemies) enemies.remove(id);
        bullets.removeAll(removeBullets);
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
