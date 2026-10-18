package Exp_2;

public class CRC {
    // 多项式
    public static final String POLYNOMIAL = "10011";

    public static String modulo2Divide(String dividend, String divisor) {
        int dLen = divisor.length();
        char[] cur = dividend.toCharArray();

        // i 是在 cur 上的索引
        // cur.length - dLen 代表最多能向右移动几位
        // 当 i = cur.length - dLen的时候, 加上 dLen 正好等于 cur.length(保证数组不越界)
        // 逐位检查被除数, 如果开头是 1 就进行模 2 除法(与除数逐位异或)
        // 如果开头是 0, 会直接跳过当前位, 遍历下一位
        for (int i = 0; i <= cur.length - dLen; i++) {
            if (cur[i] == '1') {
                // 开头是 1, 逐位与除数异或
                // 遍历除数
                for (int j = 0; j < dLen; j++) {
                    // 从被除数当前位向后依次异或, 直接把结果保存到 cur 里
                    cur[i + j] = (cur[i + j] == divisor.charAt(j) ? '0' : '1');
                }
            }
            // 首位为 0, 直接跳过该位
        }

        // 除数长度 dLen, 余数长度 dLen - 1, 从末尾 cur.length 减去 (dLen - 1) 开始
        return new String(cur, cur.length - (dLen - 1), dLen - 1);
    }

    // 把要传输的字符串(数据)转成二进制字符串, 后面补 0 作为被除数
    public static String stringToBinary(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            // 把 c 这个字符转成二进制字符串, 实际上是把 ASCII 码转成二进制字符串
            String bin = Integer.toBinaryString(c);
            // 不足 8 位补 0, 每一个字符必须占 8 位, 否则会乱
            // 假如有的字 1 位, 有的字 7 位, 结合起来可能会和其他字符冲突
            while (bin.length() < 8) {
                bin = "0" + bin;
            }
            sb.append(bin);
        }
        return sb.toString();
    }

    // 把 data 转为二进制字符串之后, 后面补 0 变成被除数, 然后交给模 2 除法算余数(CRC)
    public static String calculateCRC(String dataStr, String poly) {
        String binaryData = stringToBinary(dataStr);

        // 补多项式(除数) - 1 个 0
        int zeros = poly.length() - 1;
        StringBuilder dividend = new StringBuilder(binaryData);
        for (int i = 0; i < zeros; i++) {
            dividend.append("0");
        }

        // 模 2 除法之后就是 CRC
        return modulo2Divide(dividend.toString(), poly);
    }

    // 接收端用被除数 + CRC 模 2 除以多项式, 如果余数等于 0 就代表没问题
    public static boolean verify(String dataStr, String crc, String poly) {
        // 被除数, CRC 不用转成二进制因为本身就是二进制了
        String binaryData = stringToBinary(dataStr) + crc;

        String reminder = modulo2Divide(binaryData, poly);

        // 余数是 0 则没问题
        for (char c : reminder.toCharArray()) {
            if (c != '0') {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String testDividend = "11010110110000";
        String testPoly = "10011";
        String reminder = modulo2Divide(testDividend, testPoly);
        System.out.println("余数: " + reminder);

        String message = "HELLO";

        // 发送方算 CRC
        String crc = calculateCRC(message, POLYNOMIAL);
        System.out.println("原始数据: " + message);
        System.out.println("计算出的 CRC: " + crc);

        // 模拟正常接收 (没被篡改)
        boolean check1 = verify(message, crc, POLYNOMIAL);
        System.out.println("正常数据校验结果 (应为 true): " + check1);

        // 模拟在网络传输中被篡改 (数据变成了 "JELLO", 或者 CRC 变了)
        boolean check2 = verify("JELLO", crc, POLYNOMIAL);
        System.out.println("被篡改数据校验结果 (应为 false): " + check2);
    }
}
