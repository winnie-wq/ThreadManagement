package scheduler;

import model.Task;
import java.util.LinkedList;
import java.util.Queue;

public class RoundRobinScheduler implements ScheduleStrategy {

    private Queue<Task> queue = new LinkedList<>();

    private int timeSlice = 1000; // 1秒时间片

    @Override
    public void addTask(Task task) {
        queue.offer(task);
    }

    @Override
    public Task getNextTask() {

        Task task = queue.poll();

        if(task == null){
            return null;
        }

        new Thread(() -> {
            try {

                System.out.println("执行任务: " + task.getTaskName());

                Thread.sleep(timeSlice);

                if(!task.isFinished()){

                    System.out.println("时间片结束，任务重新加入队列");

                    queue.offer(task);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();

        return task;
    }
}