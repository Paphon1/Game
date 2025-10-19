import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PlayerMovement extends JFrame {
    private JLabel player;
    private final int MOVE_SPEED = 5;
    private boolean running = true;

    // ตัวแปรเก็บสถานะการกดปุ่ม
    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean aPressed = false;
    private boolean dPressed = false;

    public PlayerMovement() {
        // ตั้งค่า Frame
        setTitle("Player Movement Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // สร้าง player label
        player = new JLabel("Player");
        player.setBounds(50, 250, 80, 80); // วางไว้ที่ซ้ายกลาง
        player.setOpaque(true);
        player.setBackground(Color.BLUE);
        player.setForeground(Color.WHITE);
        player.setHorizontalAlignment(SwingConstants.CENTER);
        player.setVerticalAlignment(SwingConstants.CENTER);
        player.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        add(player);

        // เพิ่ม KeyListener
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        wPressed = true;
                        break;
                    case KeyEvent.VK_S:
                        sPressed = true;
                        break;
                    case KeyEvent.VK_A:
                        aPressed = true;
                        break;
                    case KeyEvent.VK_D:
                        dPressed = true;
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        wPressed = false;
                        break;
                    case KeyEvent.VK_S:
                        sPressed = false;
                        break;
                    case KeyEvent.VK_A:
                        aPressed = false;
                        break;
                    case KeyEvent.VK_D:
                        dPressed = false;
                        break;
                }
            }
        });

        setFocusable(true);
        setVisible(true);

        // เริ่ม game loop thread
        startGameLoop();
    }

    private void startGameLoop() {
        Thread gameThread = new Thread(() -> {
            while (running) {
                updatePlayerPosition();

                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        gameThread.start();
    }

    private void updatePlayerPosition() {
        int x = player.getX();
        int y = player.getY();

        // อัพเดทตำแหน่งตามปุ่มที่กด
        if (wPressed) {
            y -= MOVE_SPEED;
        }
        if (sPressed) {
            y += MOVE_SPEED;
        }
        if (aPressed) {
            x -= MOVE_SPEED;
        }
        if (dPressed) {
            x += MOVE_SPEED;
        }

        // จำกัดไม่ให้ออกนอก frame
        x = Math.max(0, Math.min(x, getWidth() - player.getWidth()));
        y = Math.max(0, Math.min(y, getHeight() - player.getHeight()));

        // อัพเดท UI ใน EDT
        final int finalX = x;
        final int finalY = y;
        SwingUtilities.invokeLater(() -> player.setLocation(finalX, finalY));
    }

    public void stopGame() {
        running = false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PlayerMovement());
    }
}