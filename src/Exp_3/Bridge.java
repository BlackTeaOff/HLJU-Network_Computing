package Exp_3;

import java.text.SimpleDateFormat;
import java.util.*;

public class Bridge {
    String name;

    // 该网桥连接了哪些设备
    Map<String, Integer> physicalPorts = new HashMap<>();

    Map<String, TableEntry> forwardingTable = new HashMap<>();

    public Bridge(String name) {
        this.name = name;
    }

    // 返回该网桥向哪些接口转发的列表, 用来判断需不需要给另一个网桥发送
    List<Integer> receiveFrame(String src, String dest, int inPort) {
        String now = new SimpleDateFormat("HH:mm:ss").format(new Date());

        // 源地址在表里
        if (forwardingTable.containsKey(src)) {
            // 更新时间戳
            forwardingTable.get(src).timeStamp = now;
        } else {
            // 不在表里就记录
            forwardingTable.put(src, new TableEntry(inPort, now));
        }

        // 目的地址在表里
        if (forwardingTable.containsKey(dest)) {
            // 进来的接口和目的接口不同
            if (forwardingTable.get(dest).port != inPort) {
                // 单独转发
                System.out.println(name + " 将 " + src + "->" + dest + " 转发至端口 " + forwardingTable.get(dest).port);
                // 如果发 B1 的第二个接口, 那会使 B2 也处理这个帧
                return new ArrayList<>(List.of(forwardingTable.get(dest).port));
            } else {
                // 进来的接口和目的接口相同则丢弃
                System.out.println(name +  " 将 " + src + "->" + dest + " 丢弃");
                return new ArrayList<>();
            }
        } else {
            // 因为端口号可能重复, 所以去重
            Set<Integer> uniqueValues = new HashSet<>(physicalPorts.values());
            List<Integer> forwardingPorts = new ArrayList<>();

            // 目的地址不在表里, 向除了帧进来的接口的其他接口转发
            for (Integer port : uniqueValues) {
                if (port != inPort) {
                    System.out.println(name + " 将 " + src + "->" + dest + " 转发至端口 " + port);
                    forwardingPorts.add(port);
                }
            }
            return forwardingPorts;
        }
    }

    // Entry 是一对键值对, 可以拿这个来遍历 Map 的键和值
    void printStatus() {
        System.out.println("---- " + name + " 转发表 " + "----");
        for (Map.Entry<String, TableEntry> entry : forwardingTable.entrySet()) {
            String host = entry.getKey();
            TableEntry table = entry.getValue();
            System.out.printf("%s %4d %10s\n", host, table.port, table.timeStamp);
        }
        System.out.println("------------------");
    }
}
