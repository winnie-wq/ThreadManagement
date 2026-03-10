package model;

public class Task implements Runnable {

    private int taskId;
    private String taskName;
    private int priority;
    private int executeTime;
    private TaskStatus status;

    private TaskResult result;

    public Task(int taskId,String taskName,int priority,int executeTime){

        this.taskId=taskId;
        this.taskName=taskName;
        this.priority=priority;
        this.executeTime=executeTime;
        this.status=TaskStatus.NEW;

        this.result=new TaskResult(taskId);
    }

    public int getPriority(){
        return priority;
    }

    public int getTaskId(){
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public TaskStatus getStatus(){
        return status;
    }

    public boolean isFinished() {
        return status == TaskStatus.FINISHED;
    }

    @Override
    public void run(){

        try{

            status=TaskStatus.RUNNING;

            result.start();

            System.out.println("任务 "+taskName+" 开始执行");

            Thread.sleep(executeTime*1000);

            status=TaskStatus.FINISHED;

            result.end();

            result.setSuccess(true);

            System.out.println("任务 "+taskName+" 执行完成");

        }catch(Exception e){

            status=TaskStatus.FAILED;
        }

    }

    @Override
    public String toString(){

        return "Task{"+
                "id="+taskId+
                ",name="+taskName+
                ",priority="+priority+
                ",status="+status+
                '}';

    }

}