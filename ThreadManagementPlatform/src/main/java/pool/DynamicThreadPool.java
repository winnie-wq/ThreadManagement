package pool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 根据待执行任务数量动态设定核心线程数与最大线程数的线程池封装，
 * 任务队列使用无界 {@link LinkedBlockingQueue}（注意：极高负载下可能内存增长，课设规模下可接受）。
 */
public class DynamicThreadPool {

    private final ThreadPoolExecutor executor;

    /**
     * @param taskCount 任务数量，用于按比例选择 core/max 线程规模
     */
    public DynamicThreadPool(int taskCount) {
        int coreSize;
        int maxSize;
        if (taskCount <= 5) {
            coreSize = 2;
            maxSize = 4;
        } else if (taskCount <= 10) {
            coreSize = 4;
            maxSize = 8;
        } else {
            coreSize = 6;
            maxSize = 12;
        }

        executor = new ThreadPoolExecutor(
                coreSize,
                maxSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread th = new Thread(r, "worker-" + System.nanoTime());
                    th.setDaemon(false);
                    return th;
                });

        System.out.println("线程池初始化: 核心线程=" + coreSize + ", 最大线程=" + maxSize);
    }

    public void executeTask(Runnable task) {
        executor.execute(task);
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /** 不再接受新任务，已提交任务继续执行 */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * 尝试在超时内等线程池空闲；返回 true 表示已终止。
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    /** 尝试取消正在执行的任务并清空队列（用于超时后的兜底） */
    public void shutdownNow() {
        executor.shutdownNow();
    }
}
