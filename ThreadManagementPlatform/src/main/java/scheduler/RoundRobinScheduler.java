package scheduler;

import model.Task;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 时间片轮转（RR）：就绪队列 FIFO，每次调度取队首并模拟一个时间片的 CPU 时间。
 * <p>
 * 课设说明：当前实现每个任务在一次 {@link #getNextTask()} 中至多模拟
 * {@link #TIME_SLICE} 毫秒；若任务总时长小于时间片则只睡眠总时长。
 * 与“可抢占、多次入队直到完成”的完整 RR 相比做了简化，便于与优化器墙钟仿真对接。
 */
public class RoundRobinScheduler implements ScheduleStrategy {

    /** 就绪队列：{@link LinkedList} 实现的双端队列，按到达顺序组织 */
    private final Queue<Task> queue = new LinkedList<>();

    /** 时间片长度（毫秒），与 MLFQ 中第一级时间片同量级，便于对比实验 */
    private final int TIME_SLICE = 1000;

    @Override
    public void addTask(Task task){ queue.offer(task); }

    @Override
    public Task getNextTask(){
        Task task = queue.poll();
        if(task == null) return null;
        Thread t = new Thread(() -> {
            try{
                Thread.sleep(Math.min(TIME_SLICE, task.getExecuteTime()));
            }catch(Exception e){ e.printStackTrace(); }
        });
        t.start();
        try{ t.join(); }catch(Exception e){ e.printStackTrace(); }
        return task;
    }
}