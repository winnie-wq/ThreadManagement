package scheduler;

import model.Task;
import util.ThreadPoolManager;

public class Scheduler {

    private ScheduleStrategy strategy;

    public Scheduler(ScheduleStrategy strategy){
        this.strategy = strategy;
    }

    public void submitTask(Task task){

        strategy.addTask(task);

    }

    public void schedule(){

        while(true){

            Task task = strategy.getNextTask();

            if(task == null){
                break;
            }

            ThreadPoolManager.getThreadPool().execute(task);
        }
    }
}