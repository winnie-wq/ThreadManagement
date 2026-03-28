package scheduler;

import model.Task;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 先来先服务（FCFS）调度：就绪队列采用 FIFO 的 {@link Queue}，
 * 每次取出队首任务并模拟其完整执行时间（真实 sleep，用于与优化器中其它策略公平对比）。
 */
public class FCFSScheduler implements ScheduleStrategy {

    /** 就绪队列：按到达顺序排队，队首即为下一运行任务 */
    private final Queue<Task> readyQueue = new LinkedList<>();

    @Override
    public void addTask(Task task) {
        readyQueue.offer(task);
    }

    @Override
    public Task getNextTask() {
        Task task = readyQueue.poll();
        if (task == null) {
            return null;
        }
        // 在独立线程中 sleep，避免阻塞调用方线程栈上过深；与 RR/MLFQ 中写法保持一致
        Thread simulator = new Thread(() -> simulateCpuBurst(task.getExecuteTime()), "FCFS-sim");
        simulator.start();
        try {
            simulator.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return task;
    }

    /**
     * 模拟 CPU 突发：占用时间与任务 executeTime 一致（墙钟时间）。
     *
     * @param executeTimeMs 任务需要的连续 CPU 时间（毫秒）
     */
    private static void simulateCpuBurst(int executeTimeMs) {
        try {
            Thread.sleep(Math.max(0, executeTimeMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
