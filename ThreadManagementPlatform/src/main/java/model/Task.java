package model;

public class Task implements Runnable {

    private int taskId;
    private String taskName;
    private int priority;
    private int executeTime;
    private TaskStatus status;

    public Task(int taskId, String taskName, int priority, int executeTime){
        this.taskId = taskId;
        this.taskName = taskName;
        this.priority = priority;
        this.executeTime = executeTime;
        this.status = TaskStatus.NEW;
    }

    public int getTaskId(){ return taskId; }
    public String getTaskName(){ return taskName; }
    public int getPriority(){ return priority; }
    public int getExecuteTime(){ return executeTime; }
    public TaskStatus getStatus(){ return status; }

    public void setStatus(TaskStatus status){ this.status = status; }

    @Override
    public void run() {
        status = TaskStatus.RUNNING;
        try{
            Thread.sleep(executeTime);
        }catch(Exception e){
            e.printStackTrace();
            status = TaskStatus.FAILED;
            return;
        }
        status = TaskStatus.FINISHED;
    }
}