package Exp_3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Server {
    static Bridge B1 = new Bridge("B1");
    static Bridge B2 = new Bridge("B2");

    public static void initializeBridge(Bridge b1, Bridge b2) {
        // 代表 B1 网桥通过端口 1 连接到 A
        B1.physicalPorts.put("A", 1);
        B1.physicalPorts.put("B", 1);
        B1.physicalPorts.put("H", 4);
        B1.physicalPorts.put("G", 3);
        B1.physicalPorts.put("C", 2);
        B1.physicalPorts.put("D", 2);
        B1.physicalPorts.put("B2", 2);

        B2.physicalPorts.put("C", 1);
        B2.physicalPorts.put("D", 1);
        B2.physicalPorts.put("E", 2);
        B2.physicalPorts.put("F", 2);
        B2.physicalPorts.put("B1", 1);
    }

    public static void main(String[] args) {
        initializeBridge(B1, B2);

        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(8888));
            try (Socket socket = serverSocket.accept()) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = bufferedReader.readLine()) != null) {
                    String[] parts = message.split("->");
                    String src = parts[0];
                    String dest = parts[1];
                    // System.out.println(src);
                    // System.out.println(dest);
                    handleFrame(src, dest);
                }
            } catch (Exception e) {
                System.out.println("连接断开: " + e);
            }
        } catch (Exception e) {
            System.out.println("监听异常: " + e);
        }
    }

    static void handleFrame(String src, String dest) {
        if (B1.physicalPorts.containsKey(src)) {
            System.out.println(src + "->" + dest + " 经过网桥 B1");
            List<Integer> forwardingPorts =  B1.receiveFrame(src, dest, B1.physicalPorts.get(src));
            // B1 如果向接口 2 转发, 那就调用 B2 的 receiveFrame(入口是 1)
            if (forwardingPorts.contains(2)) {
                B2.receiveFrame(src, dest, 1);
            }
        }
        if (B2.physicalPorts.containsKey(src)) {
            System.out.println(src + "->" + dest + " 经过网桥 B2");
            List<Integer> forwardingPorts =  B2.receiveFrame(src, dest, B2.physicalPorts.get(src));
            // B2 如果向接口 1 转发, 那就调用 B1 的 receiveFrame(入口是2)
            if (forwardingPorts.contains(1)) {
                B1.receiveFrame(src, dest, 2);
            }
        }
        B1.printStatus();
        B2.printStatus();
        System.out.println("----------------------------------------");
    }
}
