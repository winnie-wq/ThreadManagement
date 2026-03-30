package threadmanagement;

import model.Task;
import model.TaskStatus;
import optimizer.SchedulerOptimizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import scheduler.FCFSScheduler;
import scheduler.ScheduleStrategy;
import util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 7.3 性能测试：墙钟耗时、优化器开销、并发与队列行为。
 */
public class PerformanceTests {

    private static String captureStdout(Runnable action) {
        PrintStream old = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            action.run();
        } finally {
            System.setOut(old);
        }
        return baos.toString();
    }

    @Test
    public void test7_3_1_墙钟耗时() throws Exception {
        // 任务数量提升到约 500，用很小的 executeTime 控制墙钟总耗时；
        // 同时捕获 stdout，避免每个任务日志打印导致测试与 IDE 卡顿。
        int n = 500;
        int execMs = 1;
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            tasks.add(new Task(i, "T" + i, 1, execMs));
        }

        long start = System.currentTimeMillis();
        captureStdout(() -> {
            try {
                TestUtils.runTasks(tasks, n, 10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        long elapsed = System.currentTimeMillis() - start;

        for (Task t : tasks) {
            Assertions.assertEquals(TaskStatus.FINISHED, t.getStatus());
        }
        Assertions.assertTrue(elapsed >= execMs, "墙钟耗时应大于等于单任务执行时间");
        Assertions.assertTrue(elapsed < 8000, "墙钟耗时不应过长（允许系统波动；用于报告截图即可）");
    }

    @Test
    public void test7_3_2_优化器开销() throws Exception {
        // 选择较小的 executeTime，保证 chooseBest 不会因 MLFQ 的固定时间片而过慢
        int n = 3;
        int execMs = 50;
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            tasks.add(new Task(i, "T" + i, i, execMs));
        }

        long start = System.currentTimeMillis();
        ScheduleStrategy best = SchedulerOptimizer.chooseBest(tasks);
        long elapsed = System.currentTimeMillis() - start;

        Assertions.assertNotNull(best, "优化器应返回非空策略实例");
        Assertions.assertTrue(elapsed > 0, "优化器耗时应为正");
        // 4 次策略仿真，每次约 n*execMs，目标应在数秒以内
        Assertions.assertTrue(elapsed < 6000, "优化器墙钟耗时应控制在可接受范围内");
    }

    @Test
    public void test7_3_3_并发与队列() throws Exception {
        // permits=1 强制串行：墙钟耗时应接近 n*execMs（本测试展示队列与同步等待效应）
        int n = 500;
        int execMs = 2;
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            tasks.add(new Task(i, "T" + i, 1, execMs));
        }

        long start = System.currentTimeMillis();
        captureStdout(() -> {
            try {
                TestUtils.runTasks(tasks, 1, 10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        long elapsed = System.currentTimeMillis() - start;

        for (Task t : tasks) {
            Assertions.assertEquals(TaskStatus.FINISHED, t.getStatus());
        }
        Assertions.assertTrue(elapsed >= n * execMs - 500, "串行情况下墙钟耗时应接近总执行时间");
        Assertions.assertTrue(elapsed < n * execMs + 5000, "耗时不应出现明显异常延迟（允许系统波动）");
    }
}

