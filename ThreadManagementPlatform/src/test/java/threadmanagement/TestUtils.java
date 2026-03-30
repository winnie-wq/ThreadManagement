package threadmanagement;

import history.TaskHistoryRecorder;
import model.Task;
import resource.ResourceManager;
import service.TaskService;

import pool.DynamicThreadPool;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 测试辅助工具：以最少等待时间构建线程池执行并收集历史。
 */
public final class TestUtils {

    private TestUtils() {
    }

    /**
     * 以给定 permits 运行任务列表，等待线程池终止并返回历史记录器。
     */
    public static TaskHistoryRecorder runTasks(List<Task> tasks, int permits, long awaitSeconds) throws InterruptedException {
        DynamicThreadPool pool = new DynamicThreadPool(tasks.size());
        ResourceManager rm = new ResourceManager(permits);
        TaskHistoryRecorder history = new TaskHistoryRecorder();

        for (Task t : tasks) {
            t.markSubmitted();
            pool.executeTask(TaskService.wrapWithResource(t, rm, history));
        }

        pool.shutdown();
        pool.awaitTermination(awaitSeconds, TimeUnit.SECONDS);
        return history;
    }
}

