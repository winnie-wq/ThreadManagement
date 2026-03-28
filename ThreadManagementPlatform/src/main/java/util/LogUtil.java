package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 简单统一日志：带时间戳，便于调试与课程设计报告中截取“运行过程”输出。
 */
public final class LogUtil {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private LogUtil() {
    }

    /**
     * 输出一行 INFO 级别日志（标准输出）。
     *
     * @param message 日志正文
     */
    public static synchronized void info(String message) {
        System.out.println("[" + LocalDateTime.now().format(FMT) + "] [INFO] " + message);
    }

    /** 警告级别：仍输出到标准输出，前缀区分便于在报告中截取异常路径 */
    public static synchronized void warn(String message) {
        System.out.println("[" + LocalDateTime.now().format(FMT) + "] [WARN] " + message);
    }
}
