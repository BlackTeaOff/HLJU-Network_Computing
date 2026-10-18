package Exp_2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class Client_Sender {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 8888;

    public static void main(String[] args) {
        System.out.println("正在连接接收端: " + SERVER_IP + ":" + PORT);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(SERVER_IP, PORT));
            socket.setSoTimeout(3000);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            // V(S) - 发送方要发送的序号
            int currentSeq = 0;

            while (true) {
                System.out.println("--------------------");
                System.out.println("1. 正常发送数据帧");
                // 对方会扔掉, 这边超时重传
                System.out.println("2. 模拟数据损坏");
                // 对方会静默, 这边超时重传
                System.out.println("3. 模拟数据帧丢失");
                // 对方会不发确认帧, 这边超时重传, 之后对方收到重复帧, 发最新的 ACK
                System.out.println("4. 模拟确认帧丢失");
                System.out.print("请输入操作码: ");
                String choice = scanner.nextLine();

                System.out.print("请输入要发送的字符串: ");
                String data = scanner.nextLine();

                String crc = CRC.calculateCRC(data, CRC.POLYNOMIAL);

                String sendMode = "NORMAL";
                String sendData = data;
                String sendCrc = crc;

                if ("2".equals(choice)) {
                    /*
                    // 修改 CRC 的最后一位
                    char lastChar = sendCrc.charAt(sendCrc.length() - 1);
                    // 左闭右开...
                    sendCrc = sendCrc.substring(0, sendCrc.length() - 1) + (lastChar == '1' ? '0' : '1');
                     */

                    // 修改数据的最后一位
                    char lastChar = sendData.charAt(sendData.length() - 1);
                    sendData = sendData.substring(0, sendData.length() - 1) + (lastChar == '1' ? '0' : '1');
                } else if ("3".equals(choice)) {
                    sendMode = "LOSS";
                } else if ("4".equals(choice)) {
                    sendMode = "ACK_LOSS";
                }

                // 如果没收到 ACK 或没收到正确 ACK 就一直重传

                boolean ackReceived = false;
                while (!ackReceived) {
                    System.out.println("--------------------");
                    String frame = sendMode + "#" + currentSeq + "#" + sendData + "#" + sendCrc;

                    long startTime = System.currentTimeMillis();
                    System.out.println("发送数据帧 序号:" + currentSeq + ", 数据: " + sendData + ", CRC: " + sendCrc);
                    out.println(frame);

                    try {
                        String ackResponse = in.readLine();

                        if (ackResponse != null && ackResponse.startsWith("ACK#")) {
                            int ackSeq = Integer.parseInt(ackResponse.split("#")[1]);
                            System.out.println("收到 ACK#" + ackSeq);

                            // 回答的 ACK 等于下一个要发的序号就对
                            if (ackSeq == (1 - currentSeq)) {
                                System.out.println("数据帧 Seq = " + currentSeq + " 已被确认");
                                currentSeq = 1 - currentSeq;
                                ackReceived = true;
                            } else {
                                System.out.println("[无效确认]");
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        System.out.println("--------------------");
                        System.out.println("等待 " + elapsed + " ms 未收到 ACK");
                        System.out.println("重传数据帧 " + currentSeq);

                        sendMode = "NORMAL";
                        sendData = data;
                        sendCrc = crc;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("连接断开: " + e);
        }
    }
}
