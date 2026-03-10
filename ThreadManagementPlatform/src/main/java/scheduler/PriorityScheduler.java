package scheduler;

import model.Task;
import queue.TaskQueue;

public class PriorityScheduler implements ScheduleStrategy {

    private TaskQueue queue;

    public PriorityScheduler(TaskQueue queue){
        this.queue=queue;
    }

    @Override
    public void addTask(Task task) {
        queue.addTask(task);
    }

    @Override
    public Task getNextTask() {
        return queue.getTask();
    }

}