package Exp_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Capitalized_Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入服务器IP: ");
        String IP = scanner.nextLine();

        System.out.print("请输入服务器端口: ");
        int port = Integer.parseInt(scanner.nextLine());

        System.out.println("正在尝试连接...");

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(IP, port));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            while (true) {
                System.out.print("请输入要转大写的字符串: ");
                printWriter.println(scanner.nextLine());
                System.out.println("大写字符串: " + bufferedReader.readLine());
            }
        } catch (IOException e) {
            System.out.println("连接失败: " + e);
        }
    }
}
