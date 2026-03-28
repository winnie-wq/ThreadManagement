package scheduler;

import model.Task;
import java.util.PriorityQueue;
import java.util.Comparator;

public class PriorityScheduler implements ScheduleStrategy {

    /**
     * 就绪队列：Java {@link PriorityQueue}，底层为二叉堆；
     * 比较器使 priority 数值大者先出队（高优先级优先）。
     */
    private final PriorityQueue<Task> queue =
            new PriorityQueue<>(Comparator.comparingInt(Task::getPriority).reversed());

    @Override
    public void addTask(Task task) {
        queue.offer(task);
    }

    @Override
    public Task getNextTask() {
        Task task = queue.poll();
        if (task == null) {
            return null;
        }
        // 与 FCFS/RR 一致：优化器比较的是“真实墙钟”下的仿真耗时
        Thread simulator = new Thread(() -> simulateCpuBurst(task.getExecuteTime()), "PRI-sim");
        simulator.start();
        try {
            simulator.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return task;
    }

    private static void simulateCpuBurst(int executeTimeMs) {
        try {
            Thread.sleep(Math.max(0, executeTimeMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}