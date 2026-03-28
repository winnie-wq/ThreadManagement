package service;

import history.TaskHistoryRecorder;
import model.Task;
import model.TaskStatus;
import resource.ResourceManager;
import scheduler.FCFSScheduler;
import scheduler.PriorityScheduler;
import scheduler.ScheduleStrategy;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 任务业务层：根据选定的调度策略决定提交到线程池的顺序，并用资源管理器包装实际执行。
 * 将“策略语义”与控制器、线程池解耦。
 */
public final class TaskService {

    private TaskService() {
    }

    /**
     * 按策略得到任务提交顺序（真实执行仍在线程池中并发/排队）。
     * <ul>
     *   <li>FCFS：保持输入顺序</li>
     *   <li>优先级：优先级数值大者优先</li>
     *   <li>RR / MLFQ：保持输入顺序（完整时间片轮转需任务支持剩余时间，此处与现有仿真模型一致）</li>
     * </ul>
     *
     * @param strategy 优化器选出的策略实例（用于 instanceof 判别）
     * @param tasks      用户输入的任务列表
     * @return 提交到线程池的顺序列表（新列表，不修改原列表）
     */
    public static List<Task> orderTasksForExecution(ScheduleStrategy strategy, List<Task> tasks) {
        List<Task> ordered = new ArrayList<>(tasks);
        if (strategy instanceof PriorityScheduler) {
            // 优先级调度：数值越大越优先 —— 与 PriorityScheduler 中比较器一致
            ordered.sort(Comparator.comparingInt(Task::getPriority).reversed());
        } else if (strategy instanceof FCFSScheduler) {
            // FCFS：先来先服务，保持到达顺序
            // ordered 已是原顺序拷贝
        }
        return ordered;
    }

    /**
     * 用有限资源包装任务：获得许可后再执行 {@link Task#run()}，保证 finally 中释放；
     * 若 {@code history} 非空则在 run 返回后写入一条历史记录。
     *
     * @param task    业务任务
     * @param rm      资源管理器
     * @param history 可为 null；非空时记录任务完成快照
     * @return 可交给 {@link java.util.concurrent.ThreadPoolExecutor#execute(Runnable)} 的 Runnable
     */
    public static Runnable wrapWithResource(Task task, ResourceManager rm, TaskHistoryRecorder history) {
        return () -> {
            boolean acquired = false;
            try {
                LogUtil.info("任务 [" + task.getTaskName() + "] 等待资源许可...");
                rm.acquire();
                acquired = true;
                LogUtil.info("任务 [" + task.getTaskName() + "] 已获许可，进入执行 (id=" + task.getTaskId() + ")");
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                task.setStatus(TaskStatus.FAILED);
                LogUtil.info("任务 [" + task.getTaskName() + "] 被中断，标记为失败");
            } finally {
                if (history != null) {
                    history.record(task);
                }
                if (acquired) {
                    rm.release();
                }
                LogUtil.info("任务 [" + task.getTaskName() + "] 结束包装 (已释放资源=" + acquired
                        + ", 当前可用许可≈" + rm.availablePermits() + ")");
            }
        };
    }
}
