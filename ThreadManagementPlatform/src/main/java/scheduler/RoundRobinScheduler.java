package scheduler;


import model.Task;
import java.util.LinkedList;
import java.util.Queue;

public class RoundRobinScheduler implements ScheduleStrategy {

    private Queue<Task> queue = new LinkedList<>();
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