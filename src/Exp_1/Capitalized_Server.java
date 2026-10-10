package Exp_1;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Capitalized_Server {
    private static final int port = 8888;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(port));
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " 连接成功");
                new Thread(() -> {
                    try (InputStream inputStream = socket.getInputStream()) {
                        // 专门读字符
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        // 自动刷新
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            printWriter.println(Capitalized.toCapital(line));
                        }
                        System.out.println(socket.getInetAddress().getHostAddress() + " 断开连接");
                    } catch (IOException e) {
                        System.out.println("连接断开: " + e);
                    }
                }).start();
            }
        } catch (IOException e) {
            System.out.println("监听异常: " + e);
        }
    }
}
