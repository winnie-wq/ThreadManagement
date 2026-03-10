package scheduler;

import model.Task;

import java.util.LinkedList;
import java.util.Queue;

public class MLFQScheduler implements ScheduleStrategy {

    private Queue<Task> level1 = new LinkedList<>();
    private Queue<Task> level2 = new LinkedList<>();
    private Queue<Task> level3 = new LinkedList<>();

    private int timeSlice1 = 1000;
    private int timeSlice2 = 2000;
    private int timeSlice3 = 4000;

    @Override
    public void addTask(Task task) {

        level1.offer(task);

    }

    @Override
    public Task getNextTask() {

        Task task = null;

        if(!level1.isEmpty()){
            task = level1.poll();
            executeTask(task,timeSlice1,level2);
        }
        else if(!level2.isEmpty()){
            task = level2.poll();
            executeTask(task,timeSlice2,level3);
        }
        else if(!level3.isEmpty()){
            task = level3.poll();
            executeTask(task,timeSlice3,null);
        }

        return task;
    }

    private void executeTask(Task task,int timeSlice,Queue<Task> nextQueue){

        new Thread(() -> {

            try {

                System.out.println("执行任务: "+task.getTaskName());

                Thread.sleep(timeSlice);

                if(!task.isFinished() && nextQueue != null){

                    System.out.println("任务降级到下一队列");

                    nextQueue.offer(task);

                }

            }catch(Exception e){

                e.printStackTrace();

            }

        }).start();
    }
}