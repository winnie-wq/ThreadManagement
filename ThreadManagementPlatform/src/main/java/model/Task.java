package model;

/**
 * 平台中的可运行任务单元：包含标识、优先级、执行时长及生命周期状态，
 * 并记录提交/开始/结束时间戳，便于计算等待时间与周转时间。
 */
public class Task implements Runnable {

    private final int taskId;
    private final String taskName;
    private final int priority;
    private final int executeTime;

    /** 任务生命周期状态，由本类与线程池执行路径更新 */
    private volatile TaskStatus status;

    /**
     * 时间戳（毫秒，{@link System#currentTimeMillis()}）：
     * submit — 被平台接收并记录；-1 表示尚未记录。
     */
    private volatile long submitTimeMs = -1L;
    /** start — 进入 RUNNING 的时刻 */
    private volatile long startTimeMs = -1L;
    /** finish — 成功结束或失败路径上最后更新的完成参考时刻 */
    private volatile long finishTimeMs = -1L;

    public Task(int taskId, String taskName, int priority, int executeTime) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.priority = priority;
        this.executeTime = executeTime;
        this.status = TaskStatus.NEW;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getPriority() {
        return priority;
    }

    public int getExecuteTime() {
        return executeTime;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public long getSubmitTimeMs() {
        return submitTimeMs;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public long getFinishTimeMs() {
        return finishTimeMs;
    }

    /**
     * 在任务即将进入就绪/排队阶段时调用，标记提交时刻（用于等待时间）。
     */
    public void markSubmitted() {
        this.submitTimeMs = System.currentTimeMillis();
        if (this.status == TaskStatus.NEW) {
            this.status = TaskStatus.WAITING;
        }
    }

    @Override
    public void run() {
        status = TaskStatus.RUNNING;
        startTimeMs = System.currentTimeMillis();
        try {
            Thread.sleep(executeTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            status = TaskStatus.FAILED;
            finishTimeMs = System.currentTimeMillis();
            return;
        }
        finishTimeMs = System.currentTimeMillis();
        status = TaskStatus.FINISHED;
    }
}
