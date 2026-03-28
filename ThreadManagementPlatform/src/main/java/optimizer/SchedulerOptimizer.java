package optimizer;

import model.Task;
import scheduler.FCFSScheduler;
import scheduler.MLFQScheduler;
import scheduler.PriorityScheduler;
import scheduler.RoundRobinScheduler;
import scheduler.ScheduleStrategy;
import util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 调度优化器：对多种策略各做一次“真实 sleep”的墙钟仿真，选取耗时最短者。
 * 任务列表在每次 test 中使用深拷贝（新建 Task），避免互相污染状态。
 */
public class SchedulerOptimizer {

    /**
     * 依次仿真 FCFS、优先级、RR、MLFQ，返回墙钟时间最短的策略的新实例。
     * 若耗时相同，优先顺序：FCFS → 优先级 → RR → MLFQ（先达到最小者胜出）。
     *
     * @param tasks 用户任务（只读使用，内部会 clone）
     * @return 选中的策略实现（新对象，供展示与提交顺序判别）
     */
    public static ScheduleStrategy chooseBest(List<Task> tasks) {
        long fcfsTime = test(new FCFSScheduler(), tasks);
        LogUtil.info("策略仿真 FCFS 墙钟耗时: " + fcfsTime + " ms");

        long pTime = test(new PriorityScheduler(), tasks);
        LogUtil.info("策略仿真 优先级 墙钟耗时: " + pTime + " ms");

        long rrTime = test(new RoundRobinScheduler(), tasks);
        LogUtil.info("策略仿真 时间片轮转 墙钟耗时: " + rrTime + " ms");

        long mlfqTime = test(new MLFQScheduler(), tasks);
        LogUtil.info("策略仿真 多级反馈队列 墙钟耗时: " + mlfqTime + " ms");

        long min = fcfsTime;
        ScheduleStrategy best = new FCFSScheduler();
        if (pTime < min) {
            min = pTime;
            best = new PriorityScheduler();
        }
        if (rrTime < min) {
            min = rrTime;
            best = new RoundRobinScheduler();
        }
        if (mlfqTime < min) {
            best = new MLFQScheduler();
        }
        LogUtil.info("自动选择策略: " + best.getClass().getSimpleName());
        return best;
    }

    /**
     * 对给定策略跑完所有就绪任务，返回从开始到 drain 完毕的墙钟毫秒数。
     */
    private static long test(ScheduleStrategy strategy, List<Task> tasks) {
        List<Task> copy = cloneTasks(tasks);
        for (Task t : copy) {
            strategy.addTask(t);
        }
        long start = System.currentTimeMillis();
        while (strategy.getNextTask() != null) {
            // 空循环：getNextTask 内部已通过 sleep/join 推进仿真
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    private static List<Task> cloneTasks(List<Task> tasks) {
        List<Task> list = new ArrayList<>();
        for (Task t : tasks) {
            list.add(new Task(t.getTaskId(), t.getTaskName(), t.getPriority(), t.getExecuteTime()));
        }
        return list;
    }
}