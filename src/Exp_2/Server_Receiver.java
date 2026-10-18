package Exp_2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server_Receiver {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(PORT));
            try (Socket socket = serverSocket.accept()) {
                System.out.println(socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " 已连接");

                // readLine, InputStreamReader 只能一次读几个字符
                // BufferedReader 有缓冲区, 可以等到一行在读出来
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // 不开 autoflush 必须手动写 flush, 否则一直存在缓冲区
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // V(R) - 接收方期待的序号
                int expectedSeq = 0;

                String rawFrame;
                while ((rawFrame = in.readLine()) != null) {
                    System.out.println("-------------------");
                    System.out.println("收到报文: " + rawFrame);

                    // 解析帧格式: 模式#序号#数据#CRC(模式可以选丢包/正常)
                    String[] parts = rawFrame.split("#", 4);
                    String mode = parts[0];
                    int seq = Integer.parseInt(parts[1]);
                    String data = parts[2];
                    String crc = parts[3];

                    // 丢包
                    if ("LOSS".equals(mode)) {
                        System.out.println("模拟丢包, 不回复 ACK");
                        continue;
                    }

                    // 校验失败等对方重传
                    boolean isCorrect = CRC.verify(data, crc, CRC.POLYNOMIAL);
                    if (!isCorrect) {
                        System.out.println("CRC 校验失败, 不回复 ACK");
                        continue;
                    }

                    System.out.println("CRC 校验通过");

                    // 判断收到的序号是不是期待的序号
                    if (seq == expectedSeq) {
                        System.out.println("序号匹配, 成功接收帧 Seq = " + seq + ", 数据内容: " + data);
                        // 1 变 0, 0 变 1
                        expectedSeq = 1 - expectedSeq;
                    } else {
                        // 如果对方没收到 ACK (ACK 丢包)会导致重复发送(超时重传)
                        System.out.println("检测到重复帧, 收到旧的 Seq = " + seq + "期待的是: " + expectedSeq + ", 丢弃");
                    }

                    if ("ACK_LOSS".equals(mode)) {
                        System.out.println("模拟 ACK 丢失");
                        // 不发 ACK
                        continue;
                    }

                    // 对方没收到 ACK 就得自己发一个要的 ACK 号了, 否则会一直发旧数据
                    System.out.println("发送 ACK: " + expectedSeq);
                    out.println("ACK#" + expectedSeq);
                }
            }
        } catch (IOException e) {
            System.out.println("连接断开: " + e);
        }
    }
}
