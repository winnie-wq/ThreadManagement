package scheduler;

import model.Task;
import java.util.LinkedList;
import java.util.Queue;

public class MLFQScheduler implements ScheduleStrategy {

    private Queue<Task> level1 = new LinkedList<>();
    private Queue<Task> level2 = new LinkedList<>();
    private Queue<Task> level3 = new LinkedList<>();

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