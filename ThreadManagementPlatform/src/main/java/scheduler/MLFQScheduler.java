package scheduler;

import model.Task;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 多级反馈队列（MLFQ）简化模型：三级就绪队列，时间片逐级放大。
 * <ul>
 *   <li>新任务进入第一级队列；每级调度时模拟一个时间片</li>
 *   <li>若任务所需时间大于当前时间片且存在下级队列，则降级入下一级</li>
 *   <li>第三级之后不再降级，一次模拟剩余规则由时间片与总执行时间比较决定</li>
 * </ul>
 * 存储结构：三个 {@link Queue}（均为 {@link LinkedList}），体现不同优先级就绪链。
 */
public class MLFQScheduler implements ScheduleStrategy {

    private final Queue<Task> level1 = new LinkedList<>();
    private final Queue<Task> level2 = new LinkedList<>();
    private final Queue<Task> level3 = new LinkedList<>();

    private final int TIME1 = 1000;
    private final int TIME2 = 2000;
    private final int TIME3 = 4000;

    @Override
    public void addTask(Task task){ level1.offer(task); }

    @Override
    public Task getNextTask(){
        Task task = null;
        if(!level1.isEmpty()){
            task = level1.poll();
            executeTask(task, TIME1, level2);
        } else if(!level2.isEmpty()){
            task = level2.poll();
            executeTask(task, TIME2, level3);
        } else if(!level3.isEmpty()){
            task = level3.poll();
            executeTask(task, TIME3, null);
        }
        return task;
    }

    private void executeTask(Task task, int timeSlice, Queue<Task> nextQueue){
        Thread t = new Thread(() -> {
            try{
                Thread.sleep(Math.min(timeSlice, task.getExecuteTime()));
                if(nextQueue != null && timeSlice < task.getExecuteTime()){
                    nextQueue.offer(task);
                }
            }catch(Exception e){ e.printStackTrace(); }
        });
        t.start();
        try{ t.join(); }catch(Exception e){ e.printStackTrace(); }
    }
}