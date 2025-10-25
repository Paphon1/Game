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
    private List<Bullet> bullets = new ArrayList<>();
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

        setTitle("Bullet Hell Multiplayer - " + playerId);
        setSize(800, 600);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

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
            System.out.println("Connected to server: " + ip);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Cannot connect to Server\n" + e.getMessage());
            System.exit(1);
        }
    }

    private void handleNetworkPacket(NetworkPacket packet) {
        SwingUtilities.invokeLater(() -> {
            switch (packet.type) {
                case "MOVE" -> {
                    if (packet.playerId.equals(playerId)) return;

                    otherPlayers.putIfAbsent(packet.playerId,
                            new Player(packet.x, packet.y, packet.playerId));
                    Player p = otherPlayers.get(packet.playerId);
                    p.getLabel().setLocation(packet.x, packet.y);
                    if (p.getLabel().getParent() == null) {
                        add(p.getLabel());
                    }
                    updatePlayerCount();
                }
                case "SHOOT" -> {
                    if (packet.playerId.equals(playerId)) return;

                    Bullet b = new Bullet(packet.x, packet.y, 8, 0, packet.playerId);
                    bullets.add(b);
                    add(b.getLabel());
                }
                case "ENEMY_SPAWN" -> {
                    if (!enemies.containsKey(packet.enemyId)) {
                        Enemy e = new Enemy(packet.x, packet.y, packet.hp, packet.enemyId);
                        enemies.put(packet.enemyId, e);
                        add(e.getLabel());
                    }
                }
                case "ENEMY_HIT" -> {
                    Enemy e = enemies.get(packet.enemyId);
                    if (e != null && !e.isDead()) {
                        e.setHp(packet.hp);
                    }
                }
                case "ENEMY_DEAD" -> {
                    Enemy e = enemies.get(packet.enemyId);
                    if (e != null) {
                        remove(e.getLabel());
                        enemies.remove(packet.enemyId);
                    }
                }
            }
        });
    }

    private void updatePlayerCount() {
        int count = 1 + otherPlayers.size();
        scoreLabel.setText("Score: " + score + " | Players: " + count);
    }

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

    private void updateGame() {
        int width = getWidth();
        int height = getHeight();

        player.update(width, height);

        if (client != null) {
            client.sendMove(player.getLabel().getX(), player.getLabel().getY());
        }

        List<Bullet> removeBullets = new ArrayList<>();
        synchronized (bullets) {
            for (Bullet b : bullets) {
                b.update();
                if (b.isOutOfBounds(width, height)) {
                    remove(b.getLabel());
                    removeBullets.add(b);
                }
            }
            bullets.removeAll(removeBullets);
        }

        // เฉพาะ host เท่านั้นที่ spawn enemies
        if (isHost) {
            enemySpawnTimer++;
            enemyShootTimer++;

            if (enemySpawnTimer >= 240) { // 4 วินาที
                spawnEnemies();
                enemySpawnTimer = 0;
            }

            if (enemyShootTimer >= 90) { // 1.5 วินาที
                enemyShoot();
                enemyShootTimer = 0;
            }
        }

        checkCollisions();
        repaint();
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
            add(e.getLabel());

            // ส่ง enemy ไปยัง clients อื่น
            if (client != null) {
                NetworkPacket packet = new NetworkPacket("ENEMY_SPAWN", enemyId, x, y, hp);
                try {
                    client.sendEnemySpawn(enemyId, x, y, hp);
                } catch (Exception ignored) {}
            }
        }
    }

    private void enemyShoot() {
        for (Enemy e : enemies.values()) {
            if (!e.isDead() && random.nextBoolean()) {
                Bullet b = e.shootRandom();
                synchronized (bullets) {
                    bullets.add(b);
                }
                add(b.getLabel());
            }
        }
    }

    private void shootPlayer() {
        Bullet b = player.shoot();
        synchronized (bullets) {
            bullets.add(b);
        }
        add(b.getLabel());

        if (client != null) {
            client.sendShoot(b.getLabel().getX(), b.getLabel().getY());
        }
    }

    private void checkCollisions() {
        Rectangle playerBounds = player.getBounds();
        List<Enemy> removeEnemies = new ArrayList<>();
        List<Bullet> removeBullets = new ArrayList<>();

        synchronized (bullets) {
            for (Bullet b : new ArrayList<>(bullets)) {
                Rectangle bulletBounds = b.getBounds();

                if (!b.isEnemyBullet()) {
                    // กระสุน player ชน enemy
                    for (Enemy e : enemies.values()) {
                        if (!e.isDead() && bulletBounds.intersects(e.getBounds())) {
                            e.takeDamage(1);
                            remove(b.getLabel());
                            removeBullets.add(b);

                            if (client != null) {
                                client.sendEnemyHit(e.getEnemyId(), e.getHp());
                            }

                            if (e.isDead()) {
                                removeEnemies.add(e);
                                score++;
                                remove(e.getLabel());

                                if (client != null) {
                                    client.sendEnemyDead(e.getEnemyId());
                                }
                            }
                            break;
                        }
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
                            remove(b.getLabel());
                            removeBullets.add(b);
                            break;
                        }
                    }
                }
            }
        }

        for (Enemy e : removeEnemies) {
            enemies.remove(e.getEnemyId());
        }

        synchronized (bullets) {
            bullets.removeAll(removeBullets);
        }

        updatePlayerCount();
    }

    private void gameOver() {
        JOptionPane.showMessageDialog(this, "💀 Game Over!\nFinal Score: " + score);
        System.exit(0);
    }

    public static void main(String[] args) {
        String playerId = (args.length > 0) ? args[0] : "Nam" + new Random().nextInt(999);
        String serverIp = (args.length > 1) ? args[1] : "26.7.76.52";
        boolean isHost = (args.length > 2) && args[2].equals("host");

        SwingUtilities.invokeLater(() -> new GameCore(playerId, serverIp, isHost));
    }
}