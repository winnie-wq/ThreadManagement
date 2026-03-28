package controller;

import config.ExecutionConfig;
import history.TaskHistoryRecorder;
import model.Task;
import model.TaskMetricsSummary;
import model.TaskStatus;
import monitor.ThreadPoolMonitor;
import optimizer.SchedulerOptimizer;
import pool.DynamicThreadPool;
import resource.ResourceManager;
import scheduler.ScheduleStrategy;
import service.TaskService;
import util.LogUtil;

import java.util.List;

/**
 * 控制层：串联“策略仿真选择 → 动态线程池 → 资源受限执行 → 监控 → 结果汇总”。
 */
public class TaskController {

    /**
     * 对任务列表执行完整流水线：标记提交时间、优化器选策略、按策略排序提交、等待完成并打印报表。
     *
     * @param tasks 非空任务集合（空则直接返回）
     */
    public void executeTasks(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            LogUtil.info("任务列表为空，无需执行");
            return;
        }

        long wallStart = System.currentTimeMillis();

        for (Task t : tasks) {
            t.markSubmitted();
        }

        // 真实 sleep 仿真四种策略墙钟耗时，选出最短者
        ScheduleStrategy strategy = SchedulerOptimizer.chooseBest(tasks);

        int permits = Math.min(ExecutionConfig.DEFAULT_RESOURCE_PERMITS, Math.max(1, tasks.size()));
        ResourceManager resourceManager = new ResourceManager(permits);
        DynamicThreadPool pool = new DynamicThreadPool(tasks.size());
        TaskHistoryRecorder history = new TaskHistoryRecorder();

        ThreadPoolMonitor monitor = new ThreadPoolMonitor(pool.getExecutor());
        Thread monitorThread = new Thread(monitor, "pool-monitor");
        monitorThread.setDaemon(true);
        monitorThread.start();

        List<Task> ordered = TaskService.orderTasksForExecution(strategy, tasks);
        LogUtil.info("线程池提交顺序已按策略准备，任务数=" + ordered.size());

        for (Task t : ordered) {
            pool.executeTask(TaskService.wrapWithResource(t, resourceManager, history));
        }

        pool.shutdown();
        boolean finished = false;
        try {
            finished = pool.awaitTermination(ExecutionConfig.POOL_AWAIT_MINUTES, ExecutionConfig.POOL_AWAIT_UNIT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LogUtil.warn("主线程等待线程池时被中断");
        }
        if (!finished) {
            LogUtil.warn("线程池在限定时间内未完全结束，执行 shutdownNow 兜底");
            pool.shutdownNow();
        }

        monitor.stop();
        try {
            monitorThread.join(ExecutionConfig.MONITOR_JOIN_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long wallEnd = System.currentTimeMillis();
        printReport(strategy, wallEnd - wallStart, tasks, history);
    }

    private static void printReport(
            ScheduleStrategy strategy,
            long totalMs,
            List<Task> tasks,
            TaskHistoryRecorder history) {

        System.out.println("\n========== 执行结果 ==========");
        System.out.println("选定调度策略: " + strategy.getClass().getSimpleName());
        System.out.println("墙钟总时间(含优化器四次仿真): " + totalMs + " ms");

        boolean allOk = true;
        for (Task t : tasks) {
            if (t.getStatus() != TaskStatus.FINISHED) {
                allOk = false;
                break;
            }
        }
        System.out.println("任务终态: " + (allOk ? "全部成功完成" : "存在未完成或失败任务"));

        TaskMetricsSummary.printAggregate(tasks);

        System.out.println("\n---------- 各任务时间指标 ----------");
        for (Task t : tasks) {
            long waitMs = -1;
            if (t.getSubmitTimeMs() >= 0 && t.getStartTimeMs() >= 0) {
                waitMs = t.getStartTimeMs() - t.getSubmitTimeMs();
            }
            long turnaroundMs = -1;
            if (t.getSubmitTimeMs() >= 0 && t.getFinishTimeMs() >= 0) {
                turnaroundMs = t.getFinishTimeMs() - t.getSubmitTimeMs();
            }
            System.out.printf(
                    "id=%d name=%-12s status=%-9s waitMs=%s turnaroundMs=%s%n",
                    t.getTaskId(),
                    t.getTaskName(),
                    t.getStatus(),
                    waitMs >= 0 ? Long.toString(waitMs) : "n/a",
                    turnaroundMs >= 0 ? Long.toString(turnaroundMs) : "n/a");
        }

        System.out.println("\n---------- 历史记录快照 ----------");
        for (String line : history.snapshot()) {
            System.out.println(line);
        }
    }
}
