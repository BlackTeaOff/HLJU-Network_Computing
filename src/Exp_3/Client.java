package Exp_3;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 8888));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            Scanner sc = new Scanner(System.in);
            System.out.println("1. 自动");
            System.out.println("2. 手动");
            String choice = sc.nextLine();
            switch (choice) {
                case ("1") :
                    auto(writer);
                    break;
                case ("2") :
                    manual(writer);
                    break;
            }
        } catch (Exception e) {
            System.out.println("连接失败: " + e);
        }
    }

    static void manual(PrintWriter writer) {
        while (true) {
            System.out.print("请输入要发送的文字: ");
            Scanner sc = new Scanner(System.in);
            String message = sc.nextLine();
            writer.println(message);
        }
    }

    static void auto(PrintWriter writer) {
        Random random = new Random();
        String hosts = "ABCDEFGH";

        try {
            while (true) {
                // 左闭右开
                char sender = hosts.charAt(random.nextInt(hosts.length()));
                char receiver = hosts.charAt(random.nextInt(hosts.length()));
                String message = sender + "->" + receiver;
                System.out.println("发送 " + message);
                writer.println(message);
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            System.out.println("中断操作: " + e);
        }
    }
}
