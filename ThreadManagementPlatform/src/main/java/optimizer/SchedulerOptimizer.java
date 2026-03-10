//自动选择最快算法
package optimizer;


import scheduler.*;
import model.Task;
import java.util.ArrayList;
import java.util.List;

public class SchedulerOptimizer {

    public static ScheduleStrategy chooseBest(List<Task> tasks){
        long pTime = test(new PriorityScheduler(), tasks);
        long rrTime = test(new RoundRobinScheduler(), tasks);
        long mlfqTime = test(new MLFQScheduler(), tasks);

        long min = Math.min(pTime, Math.min(rrTime, mlfqTime));
        if(min == pTime) return new PriorityScheduler();
        else if(min == rrTime) return new RoundRobinScheduler();
        else return new MLFQScheduler();
    }

    private static long test(ScheduleStrategy strategy, List<Task> tasks){
        List<Task> copy = cloneTasks(tasks);
        for(Task t : copy) strategy.addTask(t);
        long start = System.currentTimeMillis();
        while(strategy.getNextTask() != null){}
        long end = System.currentTimeMillis();
        return end - start;
    }

    private static List<Task> cloneTasks(List<Task> tasks){
        List<Task> list = new ArrayList<>();
        for(Task t : tasks){
            list.add(new Task(t.getTaskId(), t.getTaskName(), t.getPriority(), t.getExecuteTime()));
        }
        return list;
    }
}