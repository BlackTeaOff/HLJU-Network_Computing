package Exp_1;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.util.*;

public class LocalMotoristDemo {
    // 画布尺寸
    static final int WIDTH = 80;
    static final int HEIGHT = 16;

    // 道路边界 (Y轴范围, 上下)
    // 代表路的上边缘在第 2 行
    static final int ROAD_TOP = 1;
    // 代表路的下边缘在第 13 行
    static final int ROAD_BOTTOM = 13;

    static double playerX = 10.0;
    static double playerY = 7.0;
    static double mph = 0.0;
    static int score = 0;
    static int lap = 1;

    static double roadOffset = 0.0;

    static class Obstacle {
        double x, y;
        Obstacle(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    static List<Obstacle> obstacles = new ArrayList<>();
    // 随机生成障碍车的 Y 坐标偏移
    static Random random = new Random();

    // 存储在 startInputThread 里检测到的按键, 给 handleInput 读取, 修改玩家坐标
    // volatile 是防止其他线程读取的时候, 读取自己缓存的内容
    // 加上 volatile 会强制每次访问这个变量都直接读取/写入该地址
    static volatile char lastKey = '\0';
    static boolean running = true;

    public static void main(String[] args) {
        Terminal terminal = null;
        try {
            // Jline 接管系统终端
            terminal = TerminalBuilder.builder().system(true).build();

            // 关闭行缓冲
            terminal.enterRawMode();
            NonBlockingReader reader = terminal.reader();

            startInputThread(reader);

            // \033: 代表 ESC 键, 表示后面接着需要执行的命令
            // [?25l 隐藏光标
            // [2J 清空屏幕
            System.out.print("\033[?25l\033[2J");
            // Scanner scanner = new Scanner(System.in);
            // scanner.nextLine();

            // 每 50 ms 刷新一次屏幕, 1000 ms / 50 (ms / Frame) = 20 Frame
            long targetFrameTime = 50;
            // 记录生成障碍车的时间
            long lastSpawnTime = System.currentTimeMillis();

            // 游戏主循环
            while (running) {
                long startTime = System.currentTimeMillis();

                handleInput();

                if (mph < 150.0) {
                    // 防止超过 150
                    mph = Math.min(150.0, mph + 1.5);
                }

                if (mph >= 150.0) {
                    score += 5;
                }

                // 生成障碍车
                // nextInt 返回 0 到 N - 1 的随机整数(左闭右开)
                if (System.currentTimeMillis() - lastSpawnTime > 500) {
                    obstacles.add(new Obstacle(WIDTH + 5, ROAD_TOP + 1 +
                            random.nextInt(ROAD_BOTTOM - ROAD_TOP - 1)));
                    lastSpawnTime = System.currentTimeMillis();
                }

                // 自身有 0.5 的速度, 屏幕向右移动的速度越快加得越多, 最高加 1.5 的速度
                double moveSpeed = 0.5 + (mph / 150.0) * 1.5;
                Iterator<Obstacle> it = obstacles.iterator();
                // 必须用 Iterator, 否则不能删元素
                while (it.hasNext()) {
                    Obstacle obs = it.next();
                    obs.x -= moveSpeed;

                    // 碰撞判定
                    if (Math.abs(obs.x - playerX) < 4.0 && Math.abs(obs.y - playerY) < 0.25) {
                        mph = 0.0;
                        it.remove();
                        continue;
                    }

                    if (obs.x < 0) {
                        it.remove();
                    }
                }

                // System.out.println(mph);
                // System.out.println(score);
                renderFrame();

                // 维持 20 FPS, 若没到 50 ms 就等待
                long elapsed = System.currentTimeMillis() - startTime;
                long sleepTime = targetFrameTime - elapsed;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ignored) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 恢复光标
            System.out.print("\033[?25h\033[?7h\n");
            if (terminal != null) {
                try {
                    terminal.close();
                } catch (IOException ignored) {}
            }
        }

        /*
        // \033: 代表 ESC 键, 表示后面接着需要执行的命令
        // [?25l 隐藏光标
        // [2J 清空屏幕
        System.out.print("\033[?25l\033[2J");
        // Scanner scanner = new Scanner(System.in);
        // scanner.nextLine();

        // 每 50 ms 刷新一次屏幕, 1000 ms / 50 (ms / Frame) = 20 Frame
        long targetFrameTime = 50;

        // 游戏主循环
        while (running) {
            long startTime = System.currentTimeMillis();

            handleInput();

            if (mph < 150.0) {
                // 防止超过 150
                mph = Math.min(150.0, mph + 1.5);
            }

            if (mph >= 150.0) {
                score += 5;
            }

            // System.out.println(mph);
            // System.out.println(score);
            renderFrame();

            // 维持 20 FPS, 若没到 50 ms 就等待
            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = targetFrameTime - elapsed;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            }
        }
         */
    }

    // 根据 lastKey 改变玩家坐标
    static void handleInput() {
        char k = Character.toLowerCase(lastKey);
        lastKey = '\0';

        // \0 代表没有输入
        if (k == '\0') return;

        // 按下按键位移量
        double stepY = 1.0;
        double stepX = 2.0;

        // 不超过道路边界
        if (k == 'w' || k == 'u') playerY = Math.max(ROAD_TOP + 1, playerY - stepY);
        if (k == 's' || k == 'n') playerY = Math.min(ROAD_BOTTOM - 1, playerY + stepY);
        if (k == 'a' || k == 'l') playerX = Math.max(3, playerX - stepX);
        if (k == 'd' || k == 'r') playerX = Math.min(WIDTH - 1, playerX + stepX);
        if (k == 'q') running = false;
    }

    // 渲染当前帧
    static void renderFrame() {
        char[][] canvas = new char[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                // Y 轴与 X 轴
                // Y 轴决定的是哪一行, X 轴决定的是哪一列
                canvas[y][x] = ' ';
            }
        }

        // 绘制道路边缘与中间隔离带
        for (int x = 0; x < WIDTH; x++) {
            canvas[ROAD_TOP][x] = '-';
            canvas[ROAD_BOTTOM][x] = '-';
            canvas[(ROAD_TOP + ROAD_BOTTOM) / 2][x] = '=';
        }

        double dashSpeed = (mph / 150.0) * 1.5;
        roadOffset = (roadOffset + dashSpeed) % 8.0;

        int offset = (int) roadOffset;
        for (int x = 0; x < WIDTH; x++) {
            if ((x + offset) % 8 < 4) {
                canvas[ROAD_TOP + 3][x] = '-';
                canvas[ROAD_BOTTOM - 3][x] = '-';
            }
        }

        // 绘制障碍物
        for (Obstacle obs : obstacles) {
            drawSprite(canvas, (int)obs.x, (int)obs.y, "[=X=]");
        }

        // 绘制玩家车辆
        drawSprite(canvas, (int)playerX, (int)playerY, "[=P=]");

        // 组装字符串输出
        StringBuilder sb = new StringBuilder();
        // [H 复位光标到左上角
        sb.append("\033[H");

        // %-3d 左对齐, 3 和 40 都是间隔距离
        // sb.append(String.format(" LAP %d  MPH %-3d %51s SCORE: %05d\n", lap, (int)mph, "", score));

        sb.append(String.format(" LAP %d  MPH %-3d X: %-5.1f Y: %-5.1f %33s SCORE: %05d\n",
                lap, (int)mph, playerX, playerY, "", score));
        for (int y = 0; y < HEIGHT; y++) {
            // canvas[y] 代表第 y 行字符数组的指针, 把这一行的字符数组换成 String 接到 StringBuilder 的后面
            sb.append(new String(canvas[y])).append("\n");
        }
        System.out.print(sb);
    }

    // 在高度 y 上, 修改 canvas 里第 x 个字符往后的几个字符修改成自己传入的 String 的内容
    static void drawSprite(char[][] canvas, int x, int y, String sprite) {
        if (y < 0 || y >= HEIGHT) return;
        for (int i = 0; i < sprite.length(); i++) {
            int cx = x + i; // 依次向右改字符
            if (cx > 0 && cx < WIDTH) {
                canvas[y][cx] = sprite.charAt(i);
            }
        }
    }

    // 开一个线程, 不断读取按键到 lastKey
    static void startInputThread(NonBlockingReader reader) {
        Thread t = new Thread(() -> {
            try {
                while (running) {
                    // 到了 10 ms 没有就返回 -2
                    int c = reader.read(10);
                    if (c >= 0) {
                        lastKey = (char) c;
                    }
                }
            } catch (Exception ignored) {}
        });
        t.setDaemon(true);
        t.start();
    }

    /*
    // 开新线程, 不断读取键盘输入到 lastKey
    static void startInputThread() {
        // 传入所需的 Runnable 接口对象, 要实现 void run();
        // () 就是不需要传入参数, {} 里面就是方法体
        // 实际上创建了一个实现了 Runnable 接口的类
        Thread inputThread = new Thread(() -> {
            try {
                while (running) {
                    // available 返回在输入缓冲区的字节个数
                    // 如果没有直接跳过, 而不是一直阻塞在 read()
                    if (System.in.available() > 0) {
                        int c = System.in.read();
                        lastKey = (char) c;
                    }
                    // 暂停 10 ms, 防止占满 CPU
                    Thread.sleep(10);
                }
            } catch (Exception ignored) {}
        });
        // 打开守护进程, 关闭主线程这个线程也自动关闭
        inputThread.setDaemon(true);
        inputThread.start();
    }
     */
}
