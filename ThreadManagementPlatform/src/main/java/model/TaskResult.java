package model;

public class TaskResult {

    private int taskId;
    private boolean success;
    private long startTime;
    private long endTime;

    // 带参数的构造函数
    public TaskResult(int taskId) {
        this.taskId = taskId;
    }

    // 记录开始时间
    public void start() {
        startTime = System.currentTimeMillis();
    }

    // 记录结束时间
    public void end() {
        endTime = System.currentTimeMillis();
    }

    // 计算执行时间
    public long getCostTime() {
        return endTime - startTime;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTaskId() {
        return taskId;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}