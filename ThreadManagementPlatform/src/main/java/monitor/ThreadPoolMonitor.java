package monitor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 后台监控线程：以固定周期打印 {@link ThreadPoolExecutor} 的运行指标，
 * 用于观察多线程管理平台在实际执行阶段的并发形态（活跃线程、队列积压等）。
 */
public class ThreadPoolMonitor implements Runnable {

    private final ThreadPoolExecutor executor;

    /** 可见性：主线程 {@link #stop()} 后监控循环应尽快结束 */
    private volatile boolean running = true;

    /** 采样周期（毫秒），不宜过小以免刷屏或干扰调度 */
    private static final int SAMPLE_INTERVAL_MS = 2000;

    public ThreadPoolMonitor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void run() {
        while (running) {
            dumpSnapshot();
            try {
                Thread.sleep(SAMPLE_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 输出当前线程池关键计数器；与 JDK 文档一致，部分指标为近似值，仅作趋势观察。
     */
    private void dumpSnapshot() {
        System.out.println("\n===== 线程池监控 =====");
        System.out.println("当前池大小(poolSize): " + executor.getPoolSize());
        System.out.println("活跃线程数(active): " + executor.getActiveCount());
        System.out.println("核心线程数(core): " + executor.getCorePoolSize());
        System.out.println("最大线程数(max): " + executor.getMaximumPoolSize());
        System.out.println("已完成任务数(completed): " + executor.getCompletedTaskCount());
        System.out.println("队列中等待任务数(queue): " + executor.getQueue().size());
        System.out.println("历史最大池大小(largest): " + executor.getLargestPoolSize());
        System.out.println("累计调度任务近似值(taskCount): " + executor.getTaskCount());
    }

    /** 请求监控线程在下一轮循环前退出（可能在 sleep 内多等待至多一个周期） */
    public void stop() {
        running = false;
    }
}
