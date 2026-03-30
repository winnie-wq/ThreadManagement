package threadmanagement;

import controller.TaskController;
import model.Task;
import model.TaskMetricsSummary;
import model.TaskStatus;
import optimizer.SchedulerOptimizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import scheduler.*;
import view.ConsoleView;

import history.TaskHistoryRecorder;
import monitor.ThreadPoolMonitor;
import pool.DynamicThreadPool;
import resource.ResourceManager;
import service.TaskService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 7.1 功能测试：任务录入、调度行为、优化器择优、线程池生命周期、监控输出与信号量行为等。
 */
public class FunctionalTests {

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
    public void test7_1_1_任务录入与MainDemo演示数据功能验证() throws Exception {
        List<Task> tasks = ConsoleView.buildDemoTasks();
        Assertions.assertEquals(5, tasks.size(), "demo 任务数量应为 5");

        String out = captureStdout(() -> new TaskController().executeTasks(tasks));
        for (Task t : tasks) {
            Assertions.assertEquals(TaskStatus.FINISHED, t.getStatus(), "demo 任务应成功结束: " + t.getTaskName());
        }
        Assertions.assertTrue(out.contains("执行结果"), "输出中应包含执行结果");
        Assertions.assertTrue(out.contains("选定调度策略"), "输出中应包含选定调度策略");
    }

    @Test
    public void test7_1_2_四类调度策略行为符合设计需求() throws Exception {
        // FCFS：应按插入顺序出队
        List<Task> fcfsTasks = java.util.Arrays.asList(
                new Task(1, "A", 1, 5),
                new Task(2, "B", 9, 5),
                new Task(3, "C", 3, 5)
        );
        FCFSScheduler fcfs = new FCFSScheduler();
        for (Task t : fcfsTasks) fcfs.addTask(t);
        List<Integer> fcfsOrder = new ArrayList<>();
        for (Task t = fcfs.getNextTask(); t != null; t = fcfs.getNextTask()) {
            fcfsOrder.add(t.getTaskId());
        }
        Assertions.assertEquals(java.util.Arrays.asList(1, 2, 3), fcfsOrder, "FCFS 应保持队列 FIFO 顺序");

        // 优先级：priority 数值越大越先
        List<Task> priTasks = java.util.Arrays.asList(
                new Task(1, "A", 1, 5),
                new Task(2, "B", 9, 5),
                new Task(3, "C", 3, 5)
        );
        PriorityScheduler pri = new PriorityScheduler();
        for (Task t : priTasks) pri.addTask(t);
        List<Integer> priOrder = new ArrayList<>();
        for (Task t = pri.getNextTask(); t != null; t = pri.getNextTask()) {
            priOrder.add(t.getTaskId());
        }
        Assertions.assertEquals(java.util.Arrays.asList(2, 3, 1), priOrder, "优先级调度应按 priority 降序");

        // RR：简化实现为每次 poll 队首，故出队顺序应保持插入顺序
        List<Task> rrTasks = java.util.Arrays.asList(
                new Task(1, "A", 1, 5),
                new Task(2, "B", 9, 5),
                new Task(3, "C", 3, 5)
        );
        RoundRobinScheduler rr = new RoundRobinScheduler();
        for (Task t : rrTasks) rr.addTask(t);
        List<Integer> rrOrder = new ArrayList<>();
        for (Task t = rr.getNextTask(); t != null; t = rr.getNextTask()) {
            rrOrder.add(t.getTaskId());
        }
        Assertions.assertEquals(java.util.Arrays.asList(1, 2, 3), rrOrder, "RR 简化实现应保持队列顺序");

        // MLFQ：用 executeTime = 1100ms 验证降级触发（L1 时间片 1000ms 后降至 L2，L2 后不再降级）
        MLFQScheduler mlfq = new MLFQScheduler();
        Task mTask = new Task(1, "M", 1, 1100);
        mlfq.addTask(mTask);
        long start = System.currentTimeMillis();
        int cnt = 0;
        for (Task t = mlfq.getNextTask(); t != null; t = mlfq.getNextTask()) {
            cnt++;
        }
        long elapsed = System.currentTimeMillis() - start;
        Assertions.assertEquals(2, cnt, "MLFQ 在 executeTime=1100ms 时应被返回两次（L1 后降级到 L2）");
        Assertions.assertTrue(elapsed >= 1900, "MLFQ 仿真耗时应接近 1000ms + 1100ms（允许波动）");
        Assertions.assertTrue(elapsed < 3000, "MLFQ 仿真耗时不应过长（允许波动）");
    }

    @Test
    public void test7_1_3_策略优化器墙钟仿真与自动择优结果验证() {
        // executeTime=1100ms 时：RR 约 1000ms；FCFS/Priority 约 1100ms；MLFQ 约 1000ms+1100ms=2100ms
        List<Task> tasks = java.util.Arrays.asList(new Task(1, "T", 1, 1100));
        ScheduleStrategy best = SchedulerOptimizer.chooseBest(tasks);
        Assertions.assertTrue(best instanceof RoundRobinScheduler, "应选择 RR（墙钟仿真最短）");
    }

    @Test
    public void test7_1_4_动态线程池提交执行结束路径验证() throws Exception {
        List<Task> tasks = java.util.Arrays.asList(
                new Task(1, "T1", 5, 80),
                new Task(2, "T2", 2, 80)
        );
        TestUtils.runTasks(new ArrayList<>(tasks), 2, 5);
        for (Task t : tasks) {
            Assertions.assertEquals(TaskStatus.FINISHED, t.getStatus());
        }
    }

    @Test
    public void test7_1_5_线程池监控周期输出与指标项验证() throws Exception {
        DynamicThreadPool pool = new DynamicThreadPool(1);
        ResourceManager rm = new ResourceManager(1);
        ThreadPoolMonitor monitor = new ThreadPoolMonitor(pool.getExecutor());
        Thread monitorThread = new Thread(monitor, "test-monitor");

        String out = captureStdout(() -> {
            monitorThread.setDaemon(true);
            monitorThread.start();
            pool.executeTask(TaskService.wrapWithResource(new Task(1, "M", 1, 10), rm, null));
            pool.shutdown();
            try {
                Thread.sleep(2100); // 等待至少一个采样周期
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            monitor.stop();
        });
        Assertions.assertTrue(out.contains("线程池监控"), "监控输出应包含线程池监控标识");
        Assertions.assertTrue(out.contains("当前池大小"), "监控输出应包含指标项");
        // 确保线程退出
        monitorThread.join(2500);
    }

    @Test
    public void test7_1_6_信号量许可与任务串并行行为验证() throws Exception {
        // permits=1：应近似串行（第二个任务的 startTime 应不早于第一个 finishTime）
        Task a = new Task(1, "A", 1, 200);
        Task b = new Task(2, "B", 1, 200);
        List<Task> tasks1 = java.util.Arrays.asList(a, b);
        TestUtils.runTasks(new ArrayList<>(tasks1), 1, 10);
        Assertions.assertEquals(TaskStatus.FINISHED, a.getStatus());
        Assertions.assertEquals(TaskStatus.FINISHED, b.getStatus());
        Assertions.assertTrue(b.getStartTimeMs() >= a.getFinishTimeMs() - 50,
                "permits=1 时应串行（第二任务开始时间应在第一个完成之后或接近）");

        // permits=2：应允许并行（整体耗时应明显小于两者串行总和）
        Task c = new Task(3, "C", 1, 250);
        Task d = new Task(4, "D", 1, 250);
        long start = System.currentTimeMillis();
        TestUtils.runTasks(java.util.Arrays.asList(c, d), 2, 10);
        long elapsed = System.currentTimeMillis() - start;
        Assertions.assertEquals(TaskStatus.FINISHED, c.getStatus());
        Assertions.assertEquals(TaskStatus.FINISHED, d.getStatus());
        Assertions.assertTrue(elapsed < 450, "permits=2 时总耗时应小于串行执行时间（允许波动）");
    }

    @Test
    public void test7_1_7_任务状态等待周转时间历史快照汇总表验证() throws Exception {
        Task a = new Task(1, "A", 1, 120);
        Task b = new Task(2, "B", 1, 160);
        List<Task> tasks = java.util.Arrays.asList(a, b);
        TaskHistoryRecorder history = TestUtils.runTasks(new ArrayList<>(tasks), 1, 10);

        // 状态与时间戳校验
        for (Task t : tasks) {
            Assertions.assertEquals(TaskStatus.FINISHED, t.getStatus());
            Assertions.assertTrue(t.getSubmitTimeMs() >= 0);
            Assertions.assertTrue(t.getStartTimeMs() >= t.getSubmitTimeMs());
            Assertions.assertTrue(t.getFinishTimeMs() >= t.getStartTimeMs());
        }

        // 历史快照校验
        List<String> snapshot = history.snapshot();
        Assertions.assertEquals(2, snapshot.size(), "历史快照行数应与任务数一致");
        Assertions.assertTrue(snapshot.get(0).contains("id="), "快照内容应包含 id 字段");

        // 汇总表校验（只要不报错即可，进一步可由报告截图解释数值）
        TaskMetricsSummary.printAggregate(tasks);
    }
}

