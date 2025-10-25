import java.io.*;
import java.net.*;

public class GameClient {
    private static final String SERVER_IP = "127.0.0.1"; // หรือ IP ของ server จริง
    private static final int PORT = 5555;

    public static void main(String[] args) {
        new GameClient().start();
    }

    public void start() {
        try (Socket socket = new Socket(SERVER_IP, PORT)) {
            System.out.println("Connected to server.");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Thread สำหรับฟังข้อความจาก server
            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println("Server says: " + msg);
                        // TODO: อัปเดตสถานะเกมตามข้อความ
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }).start();

            // ส่งข้อมูลจำลอง (ตำแหน่งผู้เล่น)
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while ((input = console.readLine()) != null) {
                out.println(input); // ส่งไปยัง server
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
