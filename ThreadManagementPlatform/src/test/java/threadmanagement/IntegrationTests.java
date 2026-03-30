package threadmanagement;

import controller.TaskController;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import view.ConsoleView;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 7.4 系统集成测试：从入口到控制层再到线程池执行与结果输出的端到端验证。
 */
public class  IntegrationTests {

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
    public void test7_4_系统集成端到端() throws Exception {
        // 为了降低测试时长，不直接用 demo（demo 单任务较长），而是构造一个小规模任务集
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task(1, "I1", 8, 90));
        tasks.add(new Task(2, "I2", 3, 60));
        tasks.add(new Task(3, "I3", 5, 70));

        TaskController controller = new TaskController();
        String out = captureStdout(() -> controller.executeTasks(tasks));

        for (Task t : tasks) {
            Assertions.assertEquals(TaskStatus.FINISHED, t.getStatus(), "端到端集成后任务应完成: " + t.getTaskName());
        }
        Assertions.assertTrue(out.contains("执行结果"), "输出中应包含执行结果标题");
        Assertions.assertTrue(out.contains("选定调度策略"), "输出中应包含选定调度策略");
        Assertions.assertTrue(out.contains("历史记录快照"), "输出中应包含历史记录快照");
        Assertions.assertTrue(out.contains("墙钟总时间"), "输出中应包含墙钟总时间");
    }

    @Test
    public void test7_5_待优化的部分与后续改进方向() {
        // 该用例仅用于在测试阶段记录“报告可写”的待优化要点，不做行为断言。
        // 你的报告中可引用类似如下结论：
        // 1) RR/MLFQ 当前是简化墙钟仿真，未实现真正可抢占、剩余时间分割与多次入队；
        // 2) 优化器 chooseBest 直接做四次真实 sleep 仿真，开销随任务数量线性增长；
        // 3) 线程池监控输出粒度固定，若要更高可观测性可加入事件驱动/更细指标。
        Assertions.assertTrue(true);
    }
}

