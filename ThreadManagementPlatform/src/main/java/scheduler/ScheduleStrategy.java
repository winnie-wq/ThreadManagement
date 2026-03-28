package scheduler;

import model.Task;

/**
 * 调度策略抽象：就绪集上添加任务，以及按策略取下一名待运行任务。
 * 具体实现中可在 {@link #getNextTask()} 内使用真实 sleep 做墙钟仿真（供优化器比较）。
 */
public interface ScheduleStrategy {

    void addTask(Task task);

    /**
     * @return 下一个被“调度”的任务；若无则返回 null
     */
    Task getNextTask();
}