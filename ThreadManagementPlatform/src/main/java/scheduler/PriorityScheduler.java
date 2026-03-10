package scheduler;

import model.Task;
import java.util.PriorityQueue;
import java.util.Comparator;

public class PriorityScheduler implements ScheduleStrategy {

    private PriorityQueue<Task> queue = new PriorityQueue<>(Comparator.comparingInt(Task::getPriority).reversed());

    @Override
    public void addTask(Task task){ queue.offer(task); }

    @Override
    public Task getNextTask(){ return queue.poll(); }
}