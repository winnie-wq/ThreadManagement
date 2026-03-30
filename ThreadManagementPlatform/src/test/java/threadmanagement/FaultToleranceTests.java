package threadmanagement;

import history.TaskHistoryRecorder;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pool.DynamicThreadPool;
import resource.ResourceManager;
import service.TaskService;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 7.2 容错性测试：输入边界、异常/中断路径、资源路径（Semaphore 与 release 路径）。
 */
public class FaultToleranceTests {

    @Test
    public void test7_2_1_输入边界() throws Exception {
        // controller：空任务列表应直接返回且不抛异常
        controller.TaskController controller = new controller.TaskController();
        controller.executeTasks(java.util.Collections.<Task>emptyList());

        // ResourceManager：permitCount 非法应抛出 IllegalArgumentException
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ResourceManager(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ResourceManager(-1));

        // Task：executeTime=0 应快速完成（Thread.sleep(0) 不应异常）
        Task t = new Task(1, "Z", 1, 0);
        TaskHistoryRecorder history = TestUtils.runTasks(java.util.Arrays.asList(t), 1, 5);
        Assertions.assertEquals(TaskStatus.FINISHED, t.getStatus());
        Assertions.assertEquals(1, history.snapshot().size());
    }

    @Test
    public void test7_2_2_异常与中断路径() throws Exception {
        // 通过取消 Future 来中断等待 Semaphore 的任务线程
        DynamicThreadPool pool = new DynamicThreadPool(2);
        ThreadPoolExecutor executor = pool.getExecutor();

        ResourceManager rm = new ResourceManager(1);
        TaskHistoryRecorder history = new TaskHistoryRecorder();

        Task t1 = new Task(1, "A", 1, 300);
        Task t2 = new Task(2, "B", 1, 300);
        t1.markSubmitted();
        t2.markSubmitted();

        Future<?> f1 = executor.submit(TaskService.wrapWithResource(t1, rm, history));
        Future<?> f2 = executor.submit(TaskService.wrapWithResource(t2, rm, history));

        // 让 t2 更可能卡在 rm.acquire 上，再取消中断
        Thread.sleep(50);
        f2.cancel(true);

        // 等待任务结束（忽略取消异常）
        try {
            f2.get(3, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
        f1.get(3, TimeUnit.SECONDS);

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        Assertions.assertEquals(TaskStatus.FINISHED, t1.getStatus(), "t1 不应被中断失败");
        Assertions.assertEquals(TaskStatus.FAILED, t2.getStatus(), "t2 应因中断等待资源而标记为 FAILED");
        Assertions.assertEquals(2, history.snapshot().size(), "历史记录应包含两条任务快照（含失败路径）");
    }

    @Test
    public void test7_2_3_资源路径() throws Exception {
        // 正常结束后，Semaphore 许可数应回到初始值（release 路径正确）
        int permits = 1;
        Task t1 = new Task(1, "A", 1, 80);
        Task t2 = new Task(2, "B", 1, 80);

        DynamicThreadPool pool = new DynamicThreadPool(2);
        ThreadPoolExecutor executor = pool.getExecutor();
        ResourceManager rm = new ResourceManager(permits);
        TaskHistoryRecorder history = new TaskHistoryRecorder();

        t1.markSubmitted();
        t2.markSubmitted();
        executor.execute(TaskService.wrapWithResource(t1, rm, history));
        executor.execute(TaskService.wrapWithResource(t2, rm, history));

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        Assertions.assertEquals(TaskStatus.FINISHED, t1.getStatus());
        Assertions.assertEquals(TaskStatus.FINISHED, t2.getStatus());
        Assertions.assertEquals(permits, rm.availablePermits(), "资源释放后 permits 应恢复到初始值");
        Assertions.assertEquals(2, history.snapshot().size());
    }
}

